package com.witvpn.ikev2.presentation.ui.otp

import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.internal.ServiceSpecificExtraArgs
import com.witvpn.ikev2.R
import com.witvpn.ikev2.databinding.FragmentOtpBinding
import com.witvpn.ikev2.presentation.base.BaseFragment
import com.witvpn.ikev2.presentation.ui.ShareViewModel
import com.witvpn.ikev2.presentation.utils.hideSoftKeyboard
import com.witvpn.ikev2.presentation.utils.isValidEmail
import com.witvpn.ikev2.presentation.widget.MyClickSpan
import com.witvpn.ikev2.presentation.widget.snackbar.CustomSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OTPFragment : BaseFragment<FragmentOtpBinding>(R.layout.fragment_otp), View.OnClickListener {
    private val shareViewModel: ShareViewModel by activityViewModels()

    override fun initBinding(view: View): FragmentOtpBinding {
        return FragmentOtpBinding.bind(view)
    }

    override fun initView() {
        binding.toolbar.apply {
            onBtnLeftClicked = {
                findNavController().popBackStack()
                true
            }
        }
        binding.tvPrivatePolicy.apply {
            val input = this.text
            val spannable = SpannableString(input)
                .apply {
                    val textHighLight = "private policy"
                    val indexOf = input.indexOf(textHighLight)
                    val color = ContextCompat.getColor(context, R.color.colorAccent)
                    setSpan(ForegroundColorSpan(color), indexOf, indexOf + textHighLight.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(object : MyClickSpan() {
                        override fun onClick(p0: View) {
                            findNavController().navigate(R.id.action_OTPFragment_to_policyFragment)
                        }
                    }, indexOf, indexOf + textHighLight.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            this.text = spannable
            movementMethod = LinkMovementMethod.getInstance()
        }
        binding.inputAddress.onTextChanged = {
            toggleButton()
        }
        binding.tvBottomAction.setOnClickListener(this)
        toggleButton()
    }

    private fun toggleButton() {
        val disableButton = binding.inputAddress.text.isEmpty()
        binding.tvBottomAction.isActivated = !disableButton
        binding.tvBottomAction.isClickable = !disableButton
    }

    private fun clearFocus() {
        activity?.hideSoftKeyboard()
        binding.inputAddress.clearFocus()
    }

    override fun onClick(p0: View?) {
        clearFocus()
        val email = binding.inputAddress.text
        val isValid = email.isValidEmail()
        if (isValid) {
            val action = OTPFragmentDirections.actionOTPFragmentToConfirmOTPFragment(email)
            findNavController().navigate(action)
        } else {
            binding.inputAddress.error = true
            CustomSnackbar.make(binding.snackbarContainer, R.string.message_invalid_email, R.drawable.ic_warning).show()
        }
    }

}