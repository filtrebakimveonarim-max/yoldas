package com.yoldas.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Telefon yeniden açıldığında, algılama açıksa bekçiyi tekrar başlatır. */
class AcilisAlici : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DepremBekcisi.gerekirseBaslat(ctx)
        }
    }
}
