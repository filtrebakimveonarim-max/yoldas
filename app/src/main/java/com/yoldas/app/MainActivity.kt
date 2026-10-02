package com.yoldas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { YoldasUygulama() }
    }

    override fun onDestroy() {
        // Uygulama kapanırken düdüğü ve feneri kapat
        Duduk.durdur()
        SosIsik.durdur(this)
        super.onDestroy()
    }
}
