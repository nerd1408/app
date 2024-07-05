package com.witwork.core_networking

import android.util.Log
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter
import java.lang.Exception
import java.net.URISyntaxException

interface INetworkHelper {
    fun connect()
    fun disconnect()
}

class NetworkHelper : INetworkHelper {
    companion object {
        private const val TAG = "NetworkHelper"
    }

    private val socket: Socket by lazy {
        return@lazy try {
            IO.socket("https://streamlineapp.tv/socket.io")
        } catch (e: URISyntaxException) {
            Log.e(TAG, "------> ${e.message}")
            throw Exception(e)
        }
    }

    override fun connect() {
        socket.connect()
        println("connect #call")
        socket.on(Socket.EVENT_CONNECT, onConnect)
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect)
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
    }

    override fun disconnect() {
        println("disconnect #call")
        socket.disconnect()
        socket.off()
    }

    private val onConnect = Emitter.Listener {
        Log.i(TAG,"----> onConnect")
    }

    private val onDisconnect = Emitter.Listener {
        Log.i(TAG,"----> onDisconnect")
    }

    private val onConnectError = Emitter.Listener {
        Log.i(TAG,"----> onConnectError: ${it[0]}")
    }

}