package com.lopez.guillen.dogfeeder.model

import java.net.Socket

class Session private constructor(){
    lateinit var clientSocket: Socket

    companion object {
        private var instance: Session? = null

        @Synchronized
        fun getInstance(): Session {
            if (instance == null) {
                instance = Session()
            }
            return instance!!
        }
    }

    constructor(clientSocket: Socket) : this() {
        this.clientSocket = clientSocket
    }

    fun close(){
        this.clientSocket.close()
        instance = null;
    }
}