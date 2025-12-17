package com.example.messageapp


import android.app.Application
import com.example.messageapp.data.local.AppDatabase
import com.example.messageapp.data.repository.MessagingRepositoryImpl
import com.example.messageapp.domain.repository.MessagingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


class Controller : Application() {

    companion object {
        private lateinit var instance: Controller
        fun getInstance(): Controller = instance

    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val database: AppDatabase by lazy { AppDatabase.getInstance(this, applicationScope) }
    val repository: MessagingRepository by lazy { MessagingRepositoryImpl(this, database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}