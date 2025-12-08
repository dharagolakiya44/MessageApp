package com.example.messageapp


import android.app.Application
import android.util.Log


class Controller : Application() {

    companion object {
        private lateinit var instance: Controller
        fun getInstance(): Controller = instance

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
    }

}