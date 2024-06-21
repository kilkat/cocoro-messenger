package com.example.cocoro_messenger

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketService {

    private lateinit var mSocket: Socket

    init {
        try {
            mSocket = IO.socket("http://172.30.1.79:80") // 서버 URL로 교체 http://10.0.2.2:80
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun connect() {
        mSocket.connect()
    }

    fun disconnect() {
        mSocket.disconnect()
    }

    fun on(event: String, listener: (args: Array<Any>) -> Unit) {
        mSocket.on(event) { args ->
            listener(args)
        }
    }

    fun emit(event: String, data: Any) {
        mSocket.emit(event, data)
    }
}
