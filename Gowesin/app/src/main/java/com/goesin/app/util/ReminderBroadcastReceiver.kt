package com.goesin.app.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bikeName = intent.getStringExtra("BIKE_NAME") ?: "Sepeda"
        val notificationHelper = NotificationHelper(context)
        
        // Memakai helper yang ada, atau membuat method baru khusus reminder
        notificationHelper.showReminder(bikeName) 
    }
}
