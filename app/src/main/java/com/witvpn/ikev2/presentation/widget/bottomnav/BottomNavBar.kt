package com.witvpn.ikev2.presentation.widget.bottomnav

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.witvpn.ikev2.R

class BottomNavBar(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs), View.OnClickListener {
    companion object {
        const val TAB_HOME = 0
        const val TAB_PREMIUM = 1
        const val TAB_PROFILE = 2
        const val TAB_SERVERS = 3
    }

    private lateinit var tabHome: BottomNavItem
    private lateinit var tabPremium: BottomNavItem
    private lateinit var tabProfile: BottomNavItem

    var listener: OnTabChangedListener? = null

    var currentTabSelected = -1
        set(value) {
            val changed = field != value
            if (changed) {
                if (toggle(value)) {
                    field = value
                }
            } else {
                listener?.reSelected(field)
            }
        }

    override fun onFinishInflate() {
        super.onFinishInflate()
        tabHome = findViewById(R.id.tab_home)
        tabPremium = findViewById(R.id.tab_premium)
        tabProfile = findViewById(R.id.tab_profile)

        tabHome.setOnClickListener(this)
        tabPremium.setOnClickListener(this)
        tabProfile.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        this.currentTabSelected = when (p0?.id) {
            R.id.tab_home -> {
                TAB_HOME
            }
            R.id.tab_premium -> {
                TAB_PREMIUM
            }
            else -> {
                TAB_PROFILE
            }
        }
    }

    private fun toggle(tabSelected: Int): Boolean {
        val change = listener?.changed(tabSelected)

        if (change == false) {
            return false
        }
        tabHome.isSelected = tabSelected == TAB_HOME || tabSelected == TAB_SERVERS
        tabPremium.isSelected = tabSelected == TAB_PREMIUM
        tabProfile.isSelected = tabSelected == TAB_PROFILE
        return true
    }

    interface OnTabChangedListener {
        fun changed(tabIndex: Int): Boolean
        fun reSelected(tabIndex: Int)
    }

}