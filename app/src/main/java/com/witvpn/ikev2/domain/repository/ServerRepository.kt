package com.witvpn.ikev2.domain.repository

import com.witvpn.ikev2.domain.model.Server

interface ServerRepository {
    suspend fun getServers(param: MutableMap<String, Any>): List<Server>
}