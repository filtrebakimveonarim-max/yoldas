package com.yoldas.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Sarsıntı algılandığında başlayan akış:
 *  SORULUYOR → "İyi misin?" diye sorar, geri sayım başlar.
 *  Cevap gelmezse SINYAL → düdük + SOS ışığı + telefon uyanık kalır.
 */
object Alarm {
    enum class Durum { YOK, SORULUYOR, SINYAL }

    var durum by mutableStateOf(Durum.YOK)
        private set
    var kalanSaniye by mutableIntStateOf(0)
        private set
    var cokmeSuphesi by mutableStateOf(false)
        private set
    var tatbikat by mutableStateOf(false)
        private set

    private val anaIs = Handler(Looper.getMainLooper())
    private var uyanik: PowerManager.WakeLock? = null
    private var sayac: Runnable? = null

    const val KANAL_ALARM = "yoldas_alarm"
    const val ALARM_BILDIRIM = 2
    const val EKSTRA_ALARM = "yoldas_alarm"

    fun baslat(ctx: Context, cokme: Boolean = false, deneme: Boolean = false) {
        if (durum != Durum.YOK) return
        val app = ctx.applicationContext
        durum = Durum.SORULUYOR
        cokmeSuphesi = cokme
        tatbikat = deneme
        kalanSaniye = if (deneme) 20 else 60

        uyanikTut(app)
        titret(app)
        bildirimGoster(app, sinyal = false)
        Seslendirici.baslat(app)
        Seslendirici.oku(
            (if (deneme) "Bu bir tatbikat. " else "") +
                "Güçlü bir sarsıntı algıladım. İyi misin? Telefonuna dokun ya da bana cevap ver."
        )

        val r = object : Runnable {
            override fun run() {
                if (durum != Durum.SORULUYOR) return
                kalanSaniye -= 1
                if (kalanSaniye == 30 || kalanSaniye == 10) {
                    titret(app)
                    Seslendirici.oku("İyi misin? Cevap vermezsen yardım sinyalini başlatacağım.")
                }
                if (kalanSaniye <= 0) {
                    sinyalBaslat(app)
                } else {
                    anaIs.postDelayed(this, 1000)
                }
            }
        }
        sayac = r
        anaIs.postDelayed(r, 1000)
    }

    /** Cevap gelmedi: kurtarıcılar bulsun diye ses ve ışık sinyali. */
    fun sinyalBaslat(ctx: Context) {
        val app = ctx.applicationContext
        sayaciDurdur()
        durum = Durum.SINYAL
        Duduk.baslat()
        SosIsik.baslat(app)
        bildirimGoster(app, sinyal = true)
        Seslendirici.oku("Cevap alamadım. Seni bulmaları için düdük ve ışık sinyalini başlattım.")
    }

    /** Kullanıcı iyi olduğunu söyledi: her şeyi kapat. */
    fun iyiyim(ctx: Context) {
        val app = ctx.applicationContext
        sayaciDurdur()
        Duduk.durdur()
        SosIsik.durdur(app)
        durum = Durum.YOK
        NotificationManagerCompat.from(app).cancel(ALARM_BILDIRIM)
        birak()
        Seslendirici.oku("Sevindim. Bir şeye ihtiyacın olursa Acil durum butonuna bas.")
    }

    /** Kullanıcı yardım istedi: geri sayımı durdur, afet moduna geçilecek. Sinyaller açıksa açık kalır. */
    fun yardimLazim(ctx: Context) {
        sayaciDurdur()
        durum = Durum.YOK
        NotificationManagerCompat.from(ctx.applicationContext).cancel(ALARM_BILDIRIM)
    }

    private fun sayaciDurdur() {
        sayac?.let { anaIs.removeCallbacks(it) }
        sayac = null
    }

    private fun uyanikTut(app: Context) {
        if (uyanik?.isHeld == true) return
        val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
        uyanik = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "yoldas:alarm").apply {
            acquire(6 * 60 * 60 * 1000L) // en fazla 6 saat
        }
    }

    private fun birak() {
        uyanik?.let { if (it.isHeld) it.release() }
        uyanik = null
    }

    private fun titret(app: Context) {
        val titresim: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
            (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        runCatching {
            titresim?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 300, 600, 300, 600), -1))
        }
    }

    fun kanallariOlustur(ctx: Context) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(KANAL_ALARM, "Deprem alarmı", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Sarsıntı algılandığında \"İyi misin?\" sorusu"
                enableVibration(true)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(DepremBekcisi.KANAL_BEKCI, "Deprem algılama", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Yoldaş arka planda sarsıntıları izliyor"
            }
        )
    }

    private fun bildirimGoster(app: Context, sinyal: Boolean) {
        kanallariOlustur(app)
        val ac = Intent(app, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EKSTRA_ALARM, true)
        }
        val pi = PendingIntent.getActivity(
            app, 1, ac, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val bildirim = NotificationCompat.Builder(app, KANAL_ALARM)
            .setSmallIcon(R.drawable.ikon_on)
            .setContentTitle(if (sinyal) "Yardım sinyali açık" else "İyi misin?")
            .setContentText(
                if (sinyal) "Düdük ve SOS ışığı çalışıyor. Durdurmak için dokun."
                else "Güçlü bir sarsıntı algılandı. Cevap vermek için dokun."
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(pi)
            .setFullScreenIntent(pi, true)
            .build()
        runCatching { NotificationManagerCompat.from(app).notify(ALARM_BILDIRIM, bildirim) }
        // Uygulama açık değilse ekranı aç
        runCatching { app.startActivity(ac) }
    }
}
