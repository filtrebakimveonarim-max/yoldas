package com.yoldas.app

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Telefonun fenerini Mors alfabesiyle SOS (··· ––– ···) olarak yakıp söndürür.
 * Fener için kamera izni gerekmez.
 */
object SosIsik {
    private const val BIRIM = 250L // ms; kısa sinyal = 1 birim, uzun = 3 birim
    private val kapsam = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var gorev: Job? = null

    val calisiyor: Boolean get() = gorev?.isActive == true

    private fun kamera(ctx: Context): Pair<CameraManager, String>? {
        val yonetici = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = yonetici.cameraIdList.firstOrNull {
            yonetici.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return null
        return yonetici to id
    }

    fun destekleniyor(ctx: Context): Boolean = runCatching { kamera(ctx) != null }.getOrDefault(false)

    private fun fener(yonetici: CameraManager, id: String, acik: Boolean) {
        runCatching { yonetici.setTorchMode(id, acik) }
    }

    fun baslat(ctx: Context) {
        if (calisiyor) return
        val (yonetici, id) = kamera(ctx.applicationContext) ?: return
        val desen = listOf(1, 1, 1, 3, 3, 3, 1, 1, 1)
        gorev = kapsam.launch {
            while (isActive) {
                desen.forEachIndexed { i, uzunluk ->
                    fener(yonetici, id, true)
                    delay(BIRIM * uzunluk)
                    fener(yonetici, id, false)
                    // harf arası 3 birim, harf içi 1 birim
                    delay(if (i == 2 || i == 5) BIRIM * 3 else BIRIM)
                }
                delay(BIRIM * 7) // kelime arası
            }
        }
    }

    fun durdur(ctx: Context) {
        gorev?.cancel()
        gorev = null
        kamera(ctx.applicationContext)?.let { (yonetici, id) -> fener(yonetici, id, false) }
    }
}
