package com.example.personalfinancetracker

import android.app.Application
import android.content.Context

class PersonalFinanceTrackerApp : Application() {
    companion object {
        private lateinit var instance: PersonalFinanceTrackerApp

        fun getContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
} 