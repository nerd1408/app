package com.witvpn.ikev2.presentation.ui.connect

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.witvpn.ikev2.domain.model.User
import com.witvpn.ikev2.domain.repository.UserRepository
import com.witvpn.ikev2.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.strongswan.android.logic.VpnStateService
import org.strongswan.android.utils.traffic.ITrafficSpeedListener
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class ConnectViewModel @Inject constructor(
    @ApplicationContext private val contextApp: Context,
    private val userRepository: UserRepository
) : BaseViewModel(), VpnStateService.VpnStateListener, ITrafficSpeedListener {
    //region #Vpn state
    private val _stateMutableLiveData = MutableLiveData<VpnStateService.State?>()
    val stateLiveData: LiveData<VpnStateService.State?> = _stateMutableLiveData

    private val _trafficMutableLiveData = MutableLiveData<Pair<Long, Long>>()
    val trafficLiveData: LiveData<Pair<Long, Long>> = _trafficMutableLiveData

    private var _service: VpnStateService? = null

    private val _serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            _service = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            _service = (service as VpnStateService.LocalBinder).service
            _service?.registerListener(this@ConnectViewModel, this@ConnectViewModel)
            updateState()
        }

    }
    //endregion

    init {
        contextApp.bindService(
            Intent(contextApp, VpnStateService::class.java),
            _serviceConnection,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onCleared() {
        super.onCleared()
        _service?.let {
            contextApp.unbindService(_serviceConnection)
        }
    }

    /**
     * VpnStateListener
     */
    override fun stateChanged() {
        updateState()
    }

    /**
     * ITrafficSpeedListener
     */
    override fun onTrafficSpeedMeasured(upStream: Double, downStream: Double, totalUpStream: Long, totalDownStream: Long) {
        _trafficMutableLiveData.postValue(Pair(totalUpStream, totalDownStream))
    }

    private fun updateState() {
        _stateMutableLiveData.postValue(_service?.state)
    }

    fun syncDataIfNeed(value: User?) {
        viewModelScope.launch(exceptionHandler) {
            if (stateLiveData.value != VpnStateService.State.DISCONNECTING) {
                return@launch
            }

            val userId = value?.id ?: return@launch

            val totalUpload = _trafficMutableLiveData.value?.first ?: 0
            val totalDownload = _trafficMutableLiveData.value?.second ?: 0

            if (totalDownload == 0L && totalUpload == 0L) {
                return@launch
            }

            val params = mutableMapOf(
                "userId" to userId as Any,
                "totalUpload" to totalUpload as Any,
                "totalDownload" to totalDownload as Any
            )
            userRepository.updateTotalUploadDownload(params)
        }
    }
}