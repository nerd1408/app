package com.witvpn.ikev2.domain.model

import com.google.gson.Gson
import com.witvpn.ikev2.data.remote.model.ServerObject
import com.witvpn.ikev2.presentation.utils.SharePrefs
import com.witvpn.ikev2.presentation.utils.getStringPref
import com.witvpn.ikev2.presentation.utils.putStringPref

data class Server(
    val id: String,
    val country: String?,
    val ipAddress: String?,
    val premium: Boolean?,
    val recommend: Boolean?,
    val state: String?,
    val countryCode: String?,
    val ca_file: String?,
    val ca_fileName: String?,
    val p_nsm: String?,
    val u_nsm: String?,
    var uuid: String? = null
) {
    companion object {

        fun fromObject(serverObject: ServerObject): Server {
            return Server(
                id = serverObject.id,
                country = serverObject.country,
                ipAddress = serverObject.ipAddress,
                premium = serverObject.premium as? Boolean,
                recommend = serverObject.recommend as? Boolean,
                state = serverObject.state,
                countryCode = serverObject.countryCode,
                ca_file = serverObject.caFile,
                ca_fileName = serverObject.caFileName,
                p_nsm = serverObject.p_nsm,
                u_nsm = serverObject.u_nsm
            )
        }

        fun getDraft(): Server? {
            val gson = Gson()
            return getStringPref(SharePrefs.KEY_SERVER)
                ?.let {
                    return gson.fromJson<Server>(it, Server::class.java)
                } ?: run {
                return null
            }
        }
    }

    fun saveDraft() {
        val gson = Gson()
        val json = gson.toJson(this)
        putStringPref(SharePrefs.KEY_SERVER, json)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Server) {
            return other.id == this.id
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}