package com.yoldas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Seslendirici.baslat(this)
        setContent { YoldasUygulama() }
    }

    override fun onDestroy() {
        // Uygulama kapanırken düdüğü, feneri ve sesi kapat
        if (isFinishing) {
            Duduk.durdur()
            SosIsik.durdur(this)
            Seslendirici.kapat()
        }
        super.onDestroy()
    }
}
