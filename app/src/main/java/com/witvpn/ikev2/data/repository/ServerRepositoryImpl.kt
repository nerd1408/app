package com.witvpn.ikev2.data.repository

import com.witvpn.ikev2.data.remote.ApiService
import com.witvpn.ikev2.domain.model.Server
import com.witvpn.ikev2.domain.repository.ServerRepository
import javax.inject.Inject

class ServerRepositoryImpl @Inject constructor() : ServerRepository {
    @Inject
    lateinit var apiService: ApiService


    override suspend fun getServers(param: MutableMap<String, Any>): List<Server> {
        val serversResponse = apiService.getServers(param)
        return serversResponse.data.map { return@map Server.fromObject(it) }
    }
}