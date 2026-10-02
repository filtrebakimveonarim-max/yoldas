package com.yoldas.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/**
 * Arka planda telefonun sensörlerini izleyen bekçi.
 * Uygulama kapalıyken de çalışır; bildirim çubuğunda küçük bir "Yoldaş seni koruyor" bildirimi görünür.
 */
class DepremBekcisi : Service(), SensorEventListener {
    private lateinit var sensorler: SensorManager
    private val algilayici = Algilayici()
    private lateinit var guc: PowerManager

    override fun onCreate() {
        super.onCreate()
        Alarm.kanallariOlustur(this)
        val bildirim = NotificationCompat.Builder(this, KANAL_BEKCI)
            .setSmallIcon(R.drawable.ikon_on)
            .setContentTitle("Yoldaş seni koruyor")
            .setContentText("Güçlü bir sarsıntı olursa sana \"İyi misin?\" diye soracağım.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(BEKCI_BILDIRIM, bildirim, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(BEKCI_BILDIRIM, bildirim)
        }

        guc = getSystemService(Context.POWER_SERVICE) as PowerManager
        sensorler = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Mümkünse ekran kapalıyken de telefonu uyandırabilen ivmeölçeri kullan
        val ivme = sensorler.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true)
            ?: sensorler.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        ivme?.let { sensorler.registerListener(this, it, 40_000, 2_000_000) } // 25 ölçüm/sn, 2 sn'lik paketler
        sensorler.getDefaultSensor(Sensor.TYPE_PRESSURE)?.let {
            sensorler.registerListener(this, it, 200_000, 2_000_000)
        }
        calisiyor = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onSensorChanged(olay: SensorEvent) {
        when (olay.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val v = olay.values
                val tetik = algilayici.ivme(olay.timestamp, v[0], v[1], v[2])
                // Ekran açıksa telefonu biri kullanıyor: uyanık, gerekirse kendisi basar.
                // Elde sallamak gibi yanlış alarmları önlemek için yalnızca ekran kapalıyken sor.
                if (tetik && !guc.isInteractive) {
                    Alarm.baslat(this, cokme = algilayici.cokmeSuphesi)
                }
            }
            Sensor.TYPE_PRESSURE -> algilayici.basinc(olay.timestamp, olay.values[0])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        sensorler.unregisterListener(this)
        calisiyor = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val KANAL_BEKCI = "yoldas_bekci"
        private const val BEKCI_BILDIRIM = 1
        var calisiyor = false
            private set

        private fun tercihler(ctx: Context) = ctx.getSharedPreferences("ayarlar", Context.MODE_PRIVATE)

        fun acikMi(ctx: Context) = tercihler(ctx).getBoolean("algilama", false)

        fun ac(ctx: Context) {
            tercihler(ctx).edit().putBoolean("algilama", true).apply()
            runCatching { ContextCompat.startForegroundService(ctx, Intent(ctx, DepremBekcisi::class.java)) }
        }

        fun kapat(ctx: Context) {
            tercihler(ctx).edit().putBoolean("algilama", false).apply()
            ctx.stopService(Intent(ctx, DepremBekcisi::class.java))
        }

        /** Ayar açıksa ve bekçi çalışmıyorsa başlat. */
        fun gerekirseBaslat(ctx: Context) {
            if (acikMi(ctx) && !calisiyor) {
                runCatching { ContextCompat.startForegroundService(ctx, Intent(ctx, DepremBekcisi::class.java)) }
            }
        }
    }
}
