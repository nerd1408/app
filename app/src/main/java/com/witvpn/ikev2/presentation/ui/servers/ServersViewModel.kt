package com.witvpn.ikev2.presentation.ui.servers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.witvpn.ikev2.domain.model.Resource
import com.witvpn.ikev2.domain.model.Server
import com.witvpn.ikev2.domain.model.User
import com.witvpn.ikev2.domain.repository.ServerRepository
import com.witvpn.ikev2.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServersViewModel @Inject constructor(private val serverRepository: ServerRepository) : BaseViewModel() {
    private val _serversMutableLiveData = MutableLiveData<Resource<List<Server>>>()

    fun getServers(): LiveData<Resource<List<Server>>> = _serversMutableLiveData

    fun execute(user: User) {
        viewModelScope.launch(exceptionHandler) {
            _serversMutableLiveData.postValue(Resource.loading(null))
            val param = mutableMapOf(
                "userId" to user.id as Any
            )
            val servers = serverRepository.getServers(param)
            _serversMutableLiveData.postValue(Resource.success(servers))
        }
    }
}