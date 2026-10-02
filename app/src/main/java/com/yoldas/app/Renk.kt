package com.yoldas.app

import androidx.compose.ui.graphics.Color

/** Uygulamanın renkleri. Günlük ekranlar açık, afet ekranları koyu. */
object Renk {
    // Günlük mod
    val Gun = Color(0xFFF2F4F7)
    val GunKart = Color(0xFFFFFFFF)
    val GunCizgi = Color(0xFFDDE3EB)
    val GunYazi = Color(0xFF13203A)
    val GunSoluk = Color(0xFF4A5A72)

    // Afet modu
    val Gece = Color(0xFF0D1420)
    val GeceKart = Color(0xFF172234)
    val GeceCizgi = Color(0xFF3A4A64)
    val GeceYazi = Color(0xFFEEF2F7)
    val GeceSoluk = Color(0xFFA9B6C8)

    // Vurgu ve anlam renkleri
    val Lamba = Color(0xFFF2A541)
    val LambaKoyu = Color(0xFFA85A06)
    val LambaUstuYazi = Color(0xFF1A1206)
    val Kirmizi = Color(0xFFB83A26)
    val KirmiziAcik = Color(0xFFE5624F)
    val Yesil = Color(0xFF1E6B4E)
    val YesilAcik = Color(0xFF5CC79A)
}
