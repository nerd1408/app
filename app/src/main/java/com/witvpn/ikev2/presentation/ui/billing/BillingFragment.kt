package com.witvpn.ikev2.presentation.ui.billing

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.witvpn.ikev2.R
import com.witvpn.ikev2.databinding.FragmentBillingBinding
import com.witvpn.ikev2.domain.model.Package
import com.witvpn.ikev2.domain.model.Status
import com.witvpn.ikev2.presentation.base.BaseFragment
import com.witvpn.ikev2.presentation.base.BaseViewModel
import com.witvpn.ikev2.presentation.ui.MainDelegate
import com.witvpn.ikev2.presentation.ui.MainTabFragment
import com.witvpn.ikev2.presentation.ui.ShareViewModel
import com.witvpn.ikev2.presentation.utils.*
import com.witvpn.ikev2.presentation.widget.bottomnav.BottomNavBar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class BillingFragment : BaseFragment<FragmentBillingBinding>(R.layout.fragment_billing), MainTabFragment.OnTabChanged {
    companion object {
        const val ACTION_GET = 0
    }

    private val mainDelegate: MainDelegate? by lazy {
        return@lazy FragmentUtils.getParent(this, MainDelegate::class.java)
    }

    private val shareViewModel: ShareViewModel by activityViewModels()

    override fun initBinding(view: View): FragmentBillingBinding {
        return FragmentBillingBinding.bind(view)
    }

    override fun initView() {
        toggleButton(false)

        binding.sivMonthly.setOnClickListener { toggleButton(false) }
        binding.sivYearly.setOnClickListener { toggleButton(true) }
        binding.btnGetPremium.apply {
            togglePremiumButton()
            setOnClickListener {
                if (shareViewModel.skuDetails.size != 2) {
                    return@setOnClickListener
                }
                val index = if ((binding.sivMonthly.isSelected && binding.sivMonthly.isEnabled)) 0 else 1
                val skuDetails = shareViewModel.skuDetails[index]
                mainDelegate?.startPlan(skuDetails)
            }
        }
    }

    override fun initViewModel(): BaseViewModel? = null

    override fun initObserve() {
        super.initObserve()
        shareViewModel.skuDetailsLiveData.observe(viewLifecycleOwner, Observer { skuDetailsList ->
            val packages = shareViewModel.user?.packages ?: listOf()
            val packageIdMonth = packages.firstOrNull { it.packageDuration == "monthly" }?.packageId
            val packageIdYear = packages.firstOrNull { it.packageDuration == "yearly" }?.packageId

            val monthPackage = skuDetailsList.firstOrNull { skuDetails ->
                skuDetails.sku == packageIdMonth
            }

            val yearPackage = skuDetailsList.firstOrNull { skuDetails ->
                skuDetails.sku == packageIdYear
            }

            binding.sivMonthly.setPrice(monthPackage?.price)
            binding.sivYearly.setPrice(yearPackage?.price)
            val monthPrice = monthPackage?.originalPriceAmountMicros ?: 1
            val yearPrice = monthPackage?.originalPriceAmountMicros ?: 1
            val save = yearPrice * 100f / (monthPrice * 12)
            binding.sivYearly.setDescription(String.format("save \$%.2f - 2 month free", save))
        })

        shareViewModel.purchaseLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.viewPackages.show2(false)
                    binding.tvPurchaseInfo.show(true)

                    val packages = shareViewModel.user?.packages ?: listOf()
                    val packageIdMonth = packages.firstOrNull { p: Package -> p.packageDuration == "monthly" }?.packageId

                    val isMonthly = it.data?.skus?.firstOrNull { sku -> sku == packageIdMonth } != null
                    val text = if (isMonthly) "monthly" else "yearly"

                    val calendar = Calendar.getInstance()
                        .apply {
                            timeInMillis = it.data?.purchaseTime ?: 0L
                            if (isMonthly) {
                                add(Calendar.MONTH, 1)
                            } else {
                                add(Calendar.YEAR, 1)
                            }
                        }

                    binding.tvPurchaseInfo.text = getString(R.string.label_purchase_info, text, calendar.toStringWithPattern())
                }
                else -> {
                    binding.viewPackages.show2(true)
                    binding.tvPurchaseInfo.show(false)
                }
            }
        })
    }

    override fun onChange(tabIndex: Int) {
        if (tabIndex != BottomNavBar.TAB_PREMIUM) {
            return
        }
        activity?.updateColorStatusBar(R.color.colorBackgroundPremium, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.updateColorStatusBar(R.color.colorPrimary)
    }

    private fun toggleButton(isYearly: Boolean) {
        binding.sivMonthly.isSelected = !isYearly
        binding.sivYearly.isSelected = isYearly
        togglePremiumButton()
    }

    private fun togglePremiumButton() {
        binding.btnGetPremium.isActivated = (binding.sivMonthly.isSelected && binding.sivMonthly.isEnabled) || (binding.sivYearly.isSelected && binding.sivYearly.isEnabled)
        binding.btnGetPremium.isEnabled = binding.btnGetPremium.isActivated
    }

}