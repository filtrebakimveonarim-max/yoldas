package com.yoldas.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Seslendirici.baslat(this)
        Alarm.kanallariOlustur(this)
        DepremBekcisi.gerekirseBaslat(this)
        alarmIcinHazirla(intent)
        setContent { YoldasUygulama() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        alarmIcinHazirla(intent)
    }

    /** Alarm sırasında uygulama kilit ekranının üstünde açılabilsin ve ekranı yaksın. */
    private fun alarmIcinHazirla(niyet: Intent?) {
        val alarm = niyet?.getBooleanExtra(Alarm.EKSTRA_ALARM, false) == true || Alarm.durum != Alarm.Durum.YOK
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(alarm)
            setTurnScreenOn(alarm)
        }
    }

    override fun onDestroy() {
        // Uygulama kapanırken düdüğü, feneri ve sesi kapat (alarm sürüyorsa dokunma)
        if (isFinishing && Alarm.durum == Alarm.Durum.YOK) {
            Duduk.durdur()
            SosIsik.durdur(this)
        }
        super.onDestroy()
    }
}
