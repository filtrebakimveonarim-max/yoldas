package com.yoldas.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private enum class Ekran { ANA, HAZIRLIK, AFET, REHBER, ALAN, SAKIN }

/** Uygulamanın kökü: hangi ekranın gösterileceğine karar verir. */
@Composable
fun YoldasUygulama() {
    var ekran by remember { mutableStateOf(Ekran.ANA) }
    var rehberId by remember { mutableStateOf("kanama") }

    // Telefonun geri tuşu: alt ekranlardan afet moduna, oradan ana sayfaya
    BackHandler(enabled = ekran != Ekran.ANA) {
        ekran = when (ekran) {
            Ekran.REHBER, Ekran.ALAN, Ekran.SAKIN -> Ekran.AFET
            else -> Ekran.ANA
        }
    }

    val karanlik = ekran != Ekran.ANA && ekran != Ekran.HAZIRLIK
    Surface(modifier = Modifier.fillMaxSize(), color = if (karanlik) Renk.Gece else Renk.Gun) {
        Box(Modifier.fillMaxSize().systemBarsPadding()) {
            when (ekran) {
                Ekran.ANA -> AnaSayfa(
                    hazirligaGit = { ekran = Ekran.HAZIRLIK },
                    acilDurum = { ekran = Ekran.AFET },
                )
                Ekran.HAZIRLIK -> HazirlikEkrani(geri = { ekran = Ekran.ANA })
                Ekran.AFET -> AfetModu(
                    rehberAc = { rehberId = it; ekran = Ekran.REHBER },
                    alanAc = { ekran = Ekran.ALAN },
                    sakinlesAc = { ekran = Ekran.SAKIN },
                    cik = { ekran = Ekran.ANA },
                )
                Ekran.REHBER -> RehberEkrani(Rehberler.bul(rehberId), bitti = { ekran = Ekran.AFET })
                Ekran.ALAN -> ToplanmaEkrani(geri = { ekran = Ekran.AFET })
                Ekran.SAKIN -> SakinlesEkrani(geri = { ekran = Ekran.AFET })
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Ortak parçalar                                                      */
/* ------------------------------------------------------------------ */

@Composable
private fun EkranSutunu(icerik: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = icerik,
    )
}

@Composable
private fun BuyukButon(
    yazi: String,
    zemin: Color,
    yaziRengi: Color,
    onClick: () -> Unit,
    cerceve: Color? = null,
    yukseklik: Int = 72,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = yukseklik.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = zemin, contentColor = yaziRengi),
        border = cerceve?.let { BorderStroke(2.dp, it) },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(yazi, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Kart(koyu: Boolean = false, icerik: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (koyu) Renk.GeceKart else Renk.GunKart, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = icerik,
    )
}

@Composable
private fun GeriSatiri(baslik: String, geri: () -> Unit, koyu: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (koyu) Renk.GeceKart else Renk.GunKart, CircleShape)
                .clickable(onClickLabel = "Geri", onClick = geri),
            contentAlignment = Alignment.Center,
        ) {
            Text("←", fontSize = 22.sp, color = if (koyu) Renk.GeceYazi else Renk.GunYazi)
        }
        Spacer(Modifier.size(12.dp))
        Text(
            baslik,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = if (koyu) Renk.GeceYazi else Renk.GunYazi,
        )
    }
}

@Composable
private fun IlerlemeCubugu(oran: Float, zemin: Color, dolgu: Color, kalinlik: Int = 10) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(kalinlik.dp)
            .background(zemin, RoundedCornerShape(50)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(oran.coerceIn(0f, 1f))
                .height(kalinlik.dp)
                .background(dolgu, RoundedCornerShape(50)),
        )
    }
}

/* ------------------------------------------------------------------ */
/* Ana sayfa (günlük mod)                                               */
/* ------------------------------------------------------------------ */

@Composable
private fun AnaSayfa(hazirligaGit: () -> Unit, acilDurum: () -> Unit) {
    val ctx = LocalContext.current
    val durum = remember { Hazirlik.yukle(ctx) }
    val tamamlanan = durum.count { it }
    val oran = tamamlanan.toFloat() / Hazirlik.gorevler.size
    val siradaki = Hazirlik.gorevler.indices.firstOrNull { !durum[it] }?.let { Hazirlik.gorevler[it] }

    EkranSutunu {
        Text("Yoldaş", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Renk.GunYazi)
        Text("Afet anında ve her gün yanında.", fontSize = 16.sp, color = Renk.GunSoluk)

        Kart {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Depreme hazırlık", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.GunYazi)
                Text("%${(oran * 100).toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.LambaKoyu)
            }
            IlerlemeCubugu(oran, zemin = Color(0xFFEDF0F4), dolgu = Renk.Lamba)
            Text(
                siradaki?.let { "Sıradaki görev: $it" } ?: "Tüm görevler tamam. Hazırsın.",
                fontSize = 16.sp,
                color = Renk.GunYazi,
            )
            BuyukButon("Görevlere git", Renk.GunYazi, Color.White, hazirligaGit, yukseklik = 52)
        }

        Kart {
            Text("İnternetsiz çalışır", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.GunYazi)
            Text(
                "İlk yardım rehberleri, düdük ve SOS ışığı telefonunda kayıtlı. Şebeke çökse de çalışır.",
                fontSize = 16.sp,
                color = Renk.GunSoluk,
            )
        }

        Spacer(Modifier.height(24.dp))
        BuyukButon("⚠  Acil durum", Renk.Kirmizi, Color.White, acilDurum)
    }
}

/* ------------------------------------------------------------------ */
/* Hazırlık listesi                                                     */
/* ------------------------------------------------------------------ */

@Composable
private fun HazirlikEkrani(geri: () -> Unit) {
    val ctx = LocalContext.current
    val durum = remember { mutableStateListOf<Boolean>().apply { addAll(Hazirlik.yukle(ctx)) } }
    val oran = durum.count { it }.toFloat() / durum.size

    EkranSutunu {
        GeriSatiri("Depreme hazırlık", geri, koyu = false)
        Kart {
            Text("%${(oran * 100).toInt()} hazırsın", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.GunYazi)
            IlerlemeCubugu(oran, zemin = Color(0xFFEDF0F4), dolgu = Renk.Lamba)
        }
        Kart {
            Hazirlik.gorevler.forEachIndexed { i, gorev ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clickable {
                            durum[i] = !durum[i]
                            Hazirlik.kaydet(ctx, i, durum[i])
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = durum[i],
                        onCheckedChange = { yeni ->
                            durum[i] = yeni
                            Hazirlik.kaydet(ctx, i, yeni)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = Renk.Yesil),
                    )
                    Text(
                        gorev,
                        fontSize = 17.sp,
                        color = if (durum[i]) Renk.GunSoluk else Renk.GunYazi,
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Afet modu                                                            */
/* ------------------------------------------------------------------ */

@Composable
private fun AfetModu(
    rehberAc: (String) -> Unit,
    alanAc: () -> Unit,
    sakinlesAc: () -> Unit,
    cik: () -> Unit,
) {
    val ctx = LocalContext.current
    var dudukAcik by remember { mutableStateOf(Duduk.calisiyor) }
    var isikAcik by remember { mutableStateOf(SosIsik.calisiyor) }
    val fenerVar = remember { SosIsik.destekleniyor(ctx) }

    EkranSutunu {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "AFET MODU",
                modifier = Modifier
                    .background(Renk.Lamba, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
                color = Renk.LambaUstuYazi,
            )
            Text("İnternetsiz çalışıyor", color = Renk.GeceSoluk, fontSize = 15.sp)
        }

        Text("Ne oldu?", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Renk.GeceYazi)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kutu("Yaralı var", Renk.KirmiziAcik, Modifier.weight(1f)) { rehberAc("kanama") }
            Kutu("Enkaz altındayım", Renk.Lamba, Modifier.weight(1f)) { rehberAc("enkaz") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kutu("Gaz kokusu", Color(0xFF8FB4E8), Modifier.weight(1f)) { rehberAc("gaz") }
            Kutu("Toplanma alanı", Renk.YesilAcik, Modifier.weight(1f), alanAc)
        }
        Kutu("Çok korkuyorum", Renk.GeceSoluk, Modifier.fillMaxWidth()) { rehberAc("panik") }

        Text(
            "KURTARILMANA YARDIMCI OLUR",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Renk.GeceSoluk,
        )
        BuyukButon(
            yazi = if (dudukAcik) "Düdüğü durdur" else "Düdük çal (15 sn'de bir)",
            zemin = if (dudukAcik) Renk.Lamba else Color.Transparent,
            yaziRengi = if (dudukAcik) Renk.LambaUstuYazi else Renk.GeceYazi,
            cerceve = if (dudukAcik) null else Renk.GeceCizgi,
            yukseklik = 60,
            onClick = {
                if (Duduk.calisiyor) Duduk.durdur() else Duduk.baslat()
                dudukAcik = Duduk.calisiyor
            },
        )
        if (fenerVar) {
            BuyukButon(
                yazi = if (isikAcik) "SOS ışığını durdur" else "SOS ışığı (fener)",
                zemin = if (isikAcik) Renk.Lamba else Color.Transparent,
                yaziRengi = if (isikAcik) Renk.LambaUstuYazi else Renk.GeceYazi,
                cerceve = if (isikAcik) null else Renk.GeceCizgi,
                yukseklik = 60,
                onClick = {
                    if (SosIsik.calisiyor) SosIsik.durdur(ctx) else SosIsik.baslat(ctx)
                    isikAcik = SosIsik.calisiyor
                },
            )
        }
        BuyukButon("Sakinleş: nefes egzersizi", Color.Transparent, Renk.GeceYazi, sakinlesAc, Renk.GeceCizgi, 60)

        Spacer(Modifier.height(8.dp))
        Text(
            "Afet modundan çık",
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = cik)
                .padding(14.dp),
            textAlign = TextAlign.Center,
            color = Renk.Lamba,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Kutu(yazi: String, isaret: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .heightIn(min = 92.dp)
            .background(Renk.GeceKart, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(Modifier.size(14.dp).background(isaret, CircleShape))
        Spacer(Modifier.height(18.dp))
        Text(yazi, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.GeceYazi)
    }
}

/* ------------------------------------------------------------------ */
/* Adım adım rehber                                                     */
/* ------------------------------------------------------------------ */

@Composable
private fun RehberEkrani(rehber: Rehber, bitti: () -> Unit) {
    var sira by remember(rehber.id) { mutableIntStateOf(0) }
    val adim = rehber.adimlar[sira]
    val son = sira == rehber.adimlar.lastIndex

    EkranSutunu {
        GeriSatiri(rehber.ad, bitti)
        Text("Adım ${sira + 1} / ${rehber.adimlar.size}", color = Renk.GeceSoluk, fontSize = 15.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            rehber.adimlar.indices.forEach { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(if (i <= sira) Renk.Lamba else Renk.GeceCizgi, RoundedCornerShape(50)),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(adim.baslik, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold, color = Renk.GeceYazi)
        Text(adim.aciklama, fontSize = 19.sp, lineHeight = 28.sp, color = Color(0xFFC9D3E0))
        Spacer(Modifier.height(24.dp))
        BuyukButon(
            yazi = if (son) "Tamam, afet moduna dön" else "Yaptım, sonraki adım",
            zemin = Renk.Lamba,
            yaziRengi = Renk.LambaUstuYazi,
            onClick = { if (son) bitti() else sira++ },
        )
        if (sira > 0) {
            BuyukButon("Önceki adım", Color.Transparent, Renk.GeceYazi, { sira-- }, Renk.GeceCizgi, 52)
        }
        Text(rehber.altNot, color = Renk.GeceSoluk, fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

/* ------------------------------------------------------------------ */
/* Toplanma alanları (şimdilik örnek veri)                              */
/* ------------------------------------------------------------------ */

private data class Alan(val ad: String, val mesafe: String, val sure: String)

@Composable
private fun ToplanmaEkrani(geri: () -> Unit) {
    // TODO: Gerçek veriler AFAD toplanma alanı listesinden alınıp telefona kaydedilecek.
    val alanlar = listOf(
        Alan("Sahil Parkı (örnek)", "350 m", "~6 dk yürüme"),
        Alan("Okul bahçesi (örnek)", "800 m", "~11 dk yürüme"),
        Alan("Spor sahası (örnek)", "1,2 km", "~17 dk yürüme"),
    )
    EkranSutunu {
        GeriSatiri("Toplanma alanları", geri)
        alanlar.forEach { a ->
            Kart(koyu = true) {
                Text(a.ad, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.GeceYazi)
                Text("${a.mesafe} · ${a.sure}", fontSize = 16.sp, color = Renk.GeceSoluk)
            }
        }
        Text(
            "Bunlar örnek veriler. Bir sonraki adımda gerçek toplanma alanlarını ve haritayı ekleyeceğiz.",
            fontSize = 14.sp,
            color = Renk.GeceSoluk,
        )
    }
}

/* ------------------------------------------------------------------ */
/* Sakinleşme: yönlendirmeli nefes                                      */
/* ------------------------------------------------------------------ */

@Composable
private fun SakinlesEkrani(geri: () -> Unit) {
    var evre by remember { mutableStateOf("Nefes al") }
    var sayi by remember { mutableIntStateOf(4) }

    LaunchedEffect(Unit) {
        val evreler = listOf("Nefes al" to 4, "Tut" to 4, "Yavaşça ver" to 6)
        while (true) {
            for ((ad, sure) in evreler) {
                evre = ad
                for (s in sure downTo 1) {
                    sayi = s
                    delay(1000)
                }
            }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth()) { GeriSatiri("Sakinleş", geri) }
        Spacer(Modifier.weight(1f))
        Text(evre, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Renk.Lamba)
        Text("$sayi", fontSize = 72.sp, fontWeight = FontWeight.ExtraBold, color = Renk.GeceYazi)
        Spacer(Modifier.height(16.dp))
        Text(
            "Yalnız değilsin. Burnundan yavaşça nefes al, ağzından ver.",
            fontSize = 18.sp,
            color = Renk.GeceSoluk,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
    }
}
