package com.witvpn.ikev2.presentation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.android.billingclient.api.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.witvpn.ikev2.BuildConfig
import com.witvpn.ikev2.R
import com.witvpn.ikev2.databinding.ActivityMainBinding
import com.witvpn.ikev2.domain.model.Resource
import com.witvpn.ikev2.domain.model.Status
import com.witvpn.ikev2.domain.model.User
import com.witvpn.ikev2.presentation.base.BaseActivity
import com.witvpn.ikev2.presentation.utils.getDeviceId
import com.witvpn.ikev2.presentation.utils.show
import com.witvpn.ikev2.presentation.utils.updateColorNavigationBar
import com.witwork.core_networking.INetworkHelper
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

interface MainDelegate {
    fun recreateApp()
    fun startPlan(skuDetails: SkuDetails)
    fun showInterstitialAd()
}

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(), MainDelegate, PurchasesUpdatedListener, BillingClientStateListener {

    @Inject
    lateinit var iNetworkHelper: INetworkHelper

    private val viewModel: ShareViewModel by viewModels()
    private val handleUserResource = Observer<User> {
        it?.let {
            connect()
        }
    }
    private val handlePurchaseResource = Observer<Resource<Purchase>> {
        when (it.status) {
            Status.SUCCESS -> {
                Timber.i(it.data.toString())
            }
            else -> {
                initAdmob()
            }
        }
    }

    private val adRequest = AdRequest.Builder().build()
    private var mInterstitialAd: InterstitialAd? = null
        set(value) {
            field = value
            field?.fullScreenContentCallback = screenContentCallback
        }
    private val screenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            initAdmob()
            mInterstitialAd = null
        }

        override fun onAdShowedFullScreenContent() {
            mInterstitialAd = null
        }
    }
    private val interstitialAdLoadCallback = object : InterstitialAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
            mInterstitialAd = null
        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            mInterstitialAd = interstitialAd
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBilling()
        viewModel.userLiveData.observe(this, handleUserResource)
        viewModel.purchaseLiveData.observe(this, handlePurchaseResource)

        iNetworkHelper.connect()
    }

    override fun initBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val hostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = hostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Timber.i("destination = ${destination.label}")
            val colorNav = when (destination.label) {
                "MainFragment" -> R.color.colorNavBottomBackground
                else -> R.color.colorPrimary
            }
            this.updateColorNavigationBar(colorNav)
        }
    }

    private fun initAdmob(refresh: Boolean = false) {
        val ads = viewModel.user?.ads
        var bannerId = ads?.firstOrNull { ad -> ad.adsType == "banner" }?.adsId ?: ""
        var interstitialAdId = ads?.firstOrNull { ad -> ad.adsType == "interstitial" }?.adsId ?: ""
        if (BuildConfig.DEBUG) {
            bannerId = "ca-app-pub-3940256099942544/6300978111"
            interstitialAdId = "ca-app-pub-3940256099942544/1033173712"
        }
        Timber.i("Banner: $bannerId\nInterstitial: $interstitialAdId")

        if (refresh) {
            InterstitialAd.load(this, interstitialAdId, adRequest, interstitialAdLoadCallback)
            return
        }

        val adView = AdView(this)
            .apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerId
                loadAd(adRequest)
            }

        binding.adsContainer.removeAllViews()
        binding.adsContainer.addView(adView)
        binding.adsContainer.show(true)
        InterstitialAd.load(this, interstitialAdId, adRequest, interstitialAdLoadCallback)
    }

    //region#Billing
    private var billingClient: BillingClient? = null

    private fun initBilling() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases() // required or app will crash
            .setListener(this)
            .build()
    }

    fun connect() {
        billingClient?.startConnection(this)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // will handle server verification, consumables, and updating the local cache
                purchases?.let {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
            }
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> connect()
            else -> Timber.d(billingResult.debugMessage)
        }
    }

    override fun onBillingServiceDisconnected() {
        connect()
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        when (p0.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                querySkuDetailsAsync()
                getPurchase()
            }
            else -> {
                Timber.d(p0.debugMessage)
            }
        }
    }

    private fun getPurchase() {
        val purchases = billingClient?.queryPurchases(BillingClient.SkuType.SUBS)?.purchasesList?.firstOrNull()
        viewModel.updatePurchase(purchases)
    }

    private fun querySkuDetailsAsync() {
        val packageIds = viewModel.user?.packages?.map { return@map it.packageId } ?: listOf()

        val params = SkuDetailsParams.newBuilder()
            .setSkusList(packageIds)
            .setType(BillingClient.SkuType.SUBS)
            .build()

        billingClient?.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Timber.i("querySkuDetailsAsync #success = $skuDetailsList")
                    viewModel.updateSkuDetailsList(skuDetailsList ?: mutableListOf())
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient?.acknowledgePurchase(params) { billingResult ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    val duration = viewModel.user
                        ?.packages
                        ?.firstOrNull { it.packageId == tmpSkuDetails?.sku }
                        ?.packageDuration ?: ""

                    val subscriptionId = tmpSkuDetails?.sku ?: ""
                    val subscriptionPricing = tmpSkuDetails?.price ?: ""
                    val userPurchase = viewModel.user?.id ?: ""
                    val param = mutableMapOf(
                        "subscriptionId" to subscriptionId as Any,
                        "subscriptionType" to duration,
                        "subscriptionPricing" to subscriptionPricing,
                        "subscriptionPlatform" to "android",
                        "userPurchase" to userPurchase
                    )
                    viewModel.updateSubscription(param) {
                        Timber.i("acknowledgePurchase: OK ---> $purchase")
                        recreateApp()
                    }
                }
                else -> {
                    Timber.d("acknowledgeNonConsumablePurchasesAsync response is ${billingResult.debugMessage}")
                }
            }
        }
    }
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        billingClient?.endConnection()
        iNetworkHelper.disconnect()
    }

    //region#MainDelegate
    override fun recreateApp() {
        finish()
        startActivity(intent)
    }

    private var tmpSkuDetails: SkuDetails? = null
    override fun startPlan(skuDetails: SkuDetails) {
        this.tmpSkuDetails = skuDetails
        val billingFlowParams = BillingFlowParams
            .newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        billingClient?.launchBillingFlow(this, billingFlowParams)
    }

    override fun showInterstitialAd() {
        mInterstitialAd?.show(this)
    }
    //endregion
}
