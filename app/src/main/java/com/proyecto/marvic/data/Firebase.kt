package com.proyecto.marvic.data

import android.app.Application
import com.google.firebase.FirebaseApp

object FirebaseInitializer {
    fun init(app: Application) {
        if (FirebaseApp.getApps(app).isEmpty()) {
            FirebaseApp.initializeApp(app)
        }
    }
}



