package com.witvpn.ikev2.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import com.witvpn.ikev2.R
import com.witvpn.ikev2.databinding.FragmentMainTabBinding
import com.witvpn.ikev2.domain.model.Server
import com.witvpn.ikev2.presentation.base.BaseFragment
import com.witvpn.ikev2.presentation.ui.billing.BillingFragment
import com.witvpn.ikev2.presentation.ui.connect.ConnectFragment
import com.witvpn.ikev2.presentation.ui.profile.ProfileFragment
import com.witvpn.ikev2.presentation.ui.servers.ServersFragment
import com.witvpn.ikev2.presentation.utils.FragmentUtils
import com.witvpn.ikev2.presentation.utils.connectivity.CONNECTED
import com.witvpn.ikev2.presentation.utils.connectivity.DISCONNECTED
import com.witvpn.ikev2.presentation.utils.connectivity.NetWorkManger
import com.witvpn.ikev2.presentation.widget.bottomnav.BottomNavBar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

interface MainTabUIDelegate {
    fun setCurrentTab(tabIndex: Int)
    fun setServerToConnect(server: Server?)
}

@AndroidEntryPoint
class MainTabFragment : BaseFragment<FragmentMainTabBinding>(R.layout.fragment_main_tab), MainTabUIDelegate {

    private val shareViewModel: ShareViewModel by activityViewModels()

    private val fragments = listOf(
        ConnectFragment(),
        BillingFragment(),
        ProfileFragment(),
        ServersFragment()
    )

    private val delegate: MainDelegate? by lazy {
        return@lazy FragmentUtils.getParent(this, MainDelegate::class.java)
    }

    override fun initBinding(view: View): FragmentMainTabBinding {
        return FragmentMainTabBinding.bind(view)
    }

    override fun initView() {
        binding.viewPager.apply {
            adapter = ViewPagerAdapter(childFragmentManager)
            offscreenPageLimit = 4
        }

        binding.bottomNavBar.root.apply {
            listener = object : BottomNavBar.OnTabChangedListener {
                override fun changed(tabIndex: Int): Boolean {

                    if (tabIndex == BottomNavBar.TAB_PROFILE && shareViewModel.userLiveData.value?.isAnonymous == true) {
                        findNavController().navigate(R.id.action_mainFragment_to_OTPFragment)
                        return false
                    }

                    binding.viewPager.setCurrentItem(tabIndex, false)
                    fragments.forEach { (it as? OnTabChanged)?.onChange(tabIndex) }
                    return true
                }

                override fun reSelected(tabIndex: Int) {
                }
            }
        }
    }

    override fun initObserve() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.viewPager.currentItem == 0) {
                activity?.finish()
            } else {
                setCurrentTab(BottomNavBar.TAB_HOME)
            }
        }

        NetWorkManger.networkStatus.observe(viewLifecycleOwner) {
            when (it) {
                CONNECTED -> Timber.i("Internet is connected")
                DISCONNECTED -> Timber.i("Internet disconnected")
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setCurrentTab(binding.viewPager.currentItem)
    }

    inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.count()
        }
    }

    interface OnTabChanged {
        fun onChange(tabIndex: Int)
    }

    //--------------------------- MainTabUIDelegate -----------------------------
    override fun setCurrentTab(tabIndex: Int) {
        binding.bottomNavBar.root.currentTabSelected = tabIndex
    }

    override fun setServerToConnect(server: Server?) {
        shareViewModel.execute(server)
        setCurrentTab(BottomNavBar.TAB_HOME)
        delegate?.showInterstitialAd()
        (fragments[BottomNavBar.TAB_HOME] as? ConnectFragment)?.autoConnect()
    }
}