package com.witvpn.ikev2.presentation.ui.splash

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.witvpn.ikev2.R
import com.witvpn.ikev2.databinding.FragmentSplashBinding
import com.witvpn.ikev2.domain.model.Status
import com.witvpn.ikev2.presentation.base.BaseFragment
import com.witvpn.ikev2.presentation.ui.ShareViewModel
import com.witvpn.ikev2.presentation.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    private val viewModel: SplashViewModel by viewModels()
    private val shareViewModel: ShareViewModel by activityViewModels()

    override fun initBinding(view: View): FragmentSplashBinding {
        return FragmentSplashBinding.bind(view)
    }

    override fun initView() {
        viewModel.execute()
    }

    override fun initObserve() {
        viewModel.userLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    shareViewModel.setUser(it.data)
                    findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                }
                Status.ERROR -> {
                    context?.showToast(it.message)
                }
                else -> {
                }
            }
        }
    }
}