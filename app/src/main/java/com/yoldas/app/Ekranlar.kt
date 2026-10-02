package com.yoldas.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
                Ekran.REHBER -> RehberEkrani(
                    Rehberler.bul(rehberId),
                    rehberAc = { rehberId = it },
                    bitti = { ekran = Ekran.AFET },
                )
                Ekran.ALAN -> ToplanmaEkrani(geri = { ekran = Ekran.AFET })
                Ekran.SAKIN -> SakinlesEkrani(geri = { ekran = Ekran.AFET })
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Ortak parçalar                                                      */
/* ------------------------------------------------------------------ */

/** Afet ekranlarında ekranın kendiliğinden kararmasını engeller. */
@Composable
private fun EkranAcikKalsin() {
    val gorunum = LocalView.current
    DisposableEffect(Unit) {
        gorunum.keepScreenOn = true
        onDispose { gorunum.keepScreenOn = false }
    }
}

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
    yaziBoyu: Int = 20,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = yukseklik.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = zemin, contentColor = yaziRengi),
        border = cerceve?.let { BorderStroke(2.dp, it) },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(yazi, fontSize = yaziBoyu.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
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

/** Sesli okumayı açıp kapatan satır. */
@Composable
private fun SesSatiri() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Renk.GeceKart, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Sesli okuma", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Renk.GeceYazi)
            if (Seslendirici.hazir && !Seslendirici.turkceVar) {
                Text(
                    "Telefonunda Türkçe ses yok. Ayarlar → Metin okuma'dan Türkçe sesi indir.",
                    fontSize = 13.sp,
                    color = Renk.GeceSoluk,
                )
            }
        }
        Switch(
            checked = Seslendirici.acik,
            onCheckedChange = {
                Seslendirici.acik = it
                if (!it) Seslendirici.sus()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Renk.LambaUstuYazi,
                checkedTrackColor = Renk.Lamba,
            ),
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

    Column(Modifier.fillMaxSize()) {
        // Kaydırılabilen üst kısım
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Yoldaş", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Renk.GunYazi)
            Text("Afet anında ve her gün yanında.", fontSize = 17.sp, color = Renk.GunSoluk)

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
                BuyukButon("Görevlere git", Renk.GunYazi, Color.White, hazirligaGit, yukseklik = 52, yaziBoyu = 18)
            }

            Kart {
                Text("İnternetsiz çalışır", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.GunYazi)
                Text(
                    "İlk yardım rehberleri, düdük ve SOS ışığı telefonunda kayıtlı. Şebeke çökse de çalışır.",
                    fontSize = 16.sp,
                    color = Renk.GunSoluk,
                )
            }
        }

        // Her zaman altta duran acil durum alanı
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Deprem ya da acil bir durumda bu butona bas.",
                fontSize = 15.sp,
                color = Renk.GunSoluk,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            BuyukButon("⚠  ACİL DURUM", Renk.Kirmizi, Color.White, acilDurum, yukseklik = 84, yaziBoyu = 24)
        }
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
            Text("Yaptığın görevlere dokunarak işaretle.", fontSize = 15.sp, color = Renk.GunSoluk)
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
/* Dinleme: mikrofon izni ve "Konuş" paneli                             */
/* ------------------------------------------------------------------ */

@Composable
private fun rememberDinleyici(): Dinleyici {
    val ctx = LocalContext.current
    val dinleyici = remember { Dinleyici(ctx) }
    DisposableEffect(Unit) {
        onDispose { dinleyici.kapat() }
    }
    return dinleyici
}

/** Mikrofon izni varsa işi hemen yapar, yoksa önce izin ister. */
@Composable
private fun rememberMikrofonla(): (() -> Unit) -> Unit {
    val ctx = LocalContext.current
    var bekleyen by remember { mutableStateOf<(() -> Unit)?>(null) }
    val izinIste = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { verildi ->
        val is_ = bekleyen
        bekleyen = null
        if (verildi) {
            is_?.invoke()
        } else {
            Seslendirici.oku("Seni duyabilmem için mikrofon iznine ihtiyacım var. Butonlarla da devam edebilirsin.")
        }
    }
    return { is_ ->
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            is_()
        } else {
            bekleyen = is_
            izinIste.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

private fun izinVar(ctx: android.content.Context) =
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

/** Büyük "Konuş" butonu ve altında ne duyduğunu / ne olduğunu gösteren satır. */
@Composable
private fun KonusPaneli(
    dinleyici: Dinleyici,
    duyulan: String?,
    ipucu: String,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BuyukButon(
            yazi = if (dinleyici.dinliyor) "●  Dinliyorum… konuş" else "🎤  Konuş",
            zemin = if (dinleyici.dinliyor) Renk.KirmiziAcik else Renk.Lamba,
            yaziRengi = if (dinleyici.dinliyor) Color.White else Renk.LambaUstuYazi,
            onClick = { if (dinleyici.dinliyor) dinleyici.durdur() else onClick() },
            yukseklik = 72,
            yaziBoyu = 22,
        )
        val satir = when {
            dinleyici.dinliyor -> "Seni dinliyorum…"
            dinleyici.uyari != null -> dinleyici.uyari!!
            duyulan != null -> "Duyduğum: \"$duyulan\""
            else -> ipucu
        }
        Text(satir, fontSize = 15.sp, color = Renk.GeceSoluk, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
    val dinleyici = rememberDinleyici()
    val mikrofonla = rememberMikrofonla()
    var duyulan by remember { mutableStateOf<String?>(null) }

    fun dudukAc() {
        Duduk.baslat()
        dudukAcik = true
    }

    fun isikAc() {
        if (fenerVar) {
            SosIsik.baslat(ctx)
            isikAcik = true
        }
    }

    fun anlat() {
        mikrofonla {
            dinleyici.dinle { metin ->
                duyulan = metin
                if (metin == null) {
                    if (dinleyici.uyari == "Seni duyamadım.") {
                        Seslendirici.oku("Seni duyamadım. Tekrar Konuş'a basıp söyle ya da bir kutuya dokun.")
                    }
                    return@dinle
                }
                when (Anlayici.durum(metin)) {
                    Niyet.KANAMA -> rehberAc("kanama")
                    Niyet.ENKAZ -> rehberAc("enkaz")
                    Niyet.GAZ -> rehberAc("gaz")
                    Niyet.PANIK -> rehberAc("panik")
                    Niyet.ALAN -> alanAc()
                    Niyet.DUDUK -> {
                        dudukAc()
                        Seslendirici.oku("Düdüğü başlattım. On beş saniyede bir çalacak.")
                    }
                    Niyet.ISIK -> {
                        isikAc()
                        Seslendirici.oku(if (fenerVar) "SOS ışığını yaktım." else "Bu telefonda fener bulunamadı.")
                    }
                    else -> Seslendirici.oku(
                        "Seni tam anlayamadım. Kanama, enkaz, gaz kokusu ya da korkuyorum gibi kısa söyle, ya da bir kutuya dokun."
                    )
                }
            }
        }
    }

    EkranAcikKalsin()
    LaunchedEffect(Unit) {
        Seslendirici.oku("Afet modu açık. Konuş butonuna basıp ne olduğunu anlatabilir ya da bir kutuya dokunabilirsin.")
    }

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

        KonusPaneli(
            dinleyici = dinleyici,
            duyulan = duyulan,
            ipucu = "Örnek: \"Annemin bacağı kanıyor\", \"Enkaz altındayım\"",
            onClick = { anlat() },
        )

        Text("Ya da durumuna uyan kutuya dokun:", fontSize = 16.sp, color = Renk.GeceSoluk)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kutu("Yaralı var", "Kanama, yara", Renk.KirmiziAcik, Modifier.weight(1f)) { rehberAc("kanama") }
            Kutu("Enkaz altındayım", "Sıkıştım, çıkamıyorum", Renk.Lamba, Modifier.weight(1f)) { rehberAc("enkaz") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kutu("Gaz kokusu", "Kaçak şüphesi", Color(0xFF8FB4E8), Modifier.weight(1f)) { rehberAc("gaz") }
            Kutu("Toplanma alanı", "Nereye gideyim?", Renk.YesilAcik, Modifier.weight(1f), alanAc)
        }
        Kutu("Çok korkuyorum", "Sakinleşmeme yardım et", Renk.GeceSoluk, Modifier.fillMaxWidth()) { rehberAc("panik") }

        Text(
            "KURTARILMANA YARDIMCI OLUR",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Renk.GeceSoluk,
        )
        BuyukButon(
            yazi = if (dudukAcik) "Düdük çalıyor · Durdur" else "Düdük çal (15 sn'de bir)",
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
                yazi = if (isikAcik) "SOS ışığı yanıyor · Durdur" else "SOS ışığı (fener)",
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

        SesSatiri()

        Spacer(Modifier.height(4.dp))
        Text(
            "Afet modundan çık",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Çıkarken düdüğü, ışığı, sesi ve dinlemeyi kapat
                    Duduk.durdur()
                    SosIsik.durdur(ctx)
                    Seslendirici.sus()
                    dinleyici.durdur()
                    cik()
                }
                .padding(14.dp),
            textAlign = TextAlign.Center,
            color = Renk.Lamba,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Kutu(
    yazi: String,
    aciklama: String,
    isaret: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .heightIn(min = 104.dp)
            .background(Renk.GeceKart, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(Modifier.size(14.dp).background(isaret, CircleShape))
        Spacer(Modifier.height(14.dp))
        Text(yazi, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.GeceYazi)
        Text(aciklama, fontSize = 14.sp, color = Renk.GeceSoluk)
    }
}

/* ------------------------------------------------------------------ */
/* Adım adım rehber: sesle ilerleme ve soru-cevap                        */
/* ------------------------------------------------------------------ */

@Composable
private fun RehberEkrani(rehber: Rehber, rehberAc: (String) -> Unit, bitti: () -> Unit) {
    val ctx = LocalContext.current
    var sira by remember(rehber.id) { mutableIntStateOf(0) }
    var yanit by remember(rehber.id, sira) { mutableStateOf<String?>(null) }
    var duyulan by remember { mutableStateOf<String?>(null) }
    // Kullanıcı bir kez Konuş'a basınca, sonraki adımlarda okuma bitince kendiliğinden dinler
    var elleriSerbest by remember { mutableStateOf(false) }
    val dinleyici = rememberDinleyici()
    val mikrofonla = rememberMikrofonla()

    val adim = rehber.adimlar[sira]
    val son = sira == rehber.adimlar.lastIndex

    fun cevapla(evet: Boolean) {
        val metin = if (evet) adim.evetCevap else adim.hayirCevap
        if ((evet && adim.evetteDuduk) || (!evet && adim.hayirdaDuduk)) Duduk.baslat()
        yanit = metin
        metin?.let { Seslendirici.oku(it) }
    }

    fun ileri() {
        if (son) bitti() else sira++
    }

    // dinle() ve isle() birbirini çağırır; bu yüzden önce bildirip sonra tanımlıyoruz
    lateinit var dinle: () -> Unit

    fun isle(metin: String?) {
        duyulan = metin
        if (metin == null) return
        when (Anlayici.komut(metin)) {
            Niyet.SONRAKI -> ileri()
            Niyet.ONCEKI -> if (sira > 0) sira--
            Niyet.TEKRAR -> Seslendirici.oku("${adim.baslik} ${adim.aciklama} ${adim.soru ?: ""}") { if (elleriSerbest) dinle() }
            Niyet.DUR -> {
                Seslendirici.sus()
                elleriSerbest = false
            }
            Niyet.EVET -> if (adim.soru != null) {
                val m = adim.evetCevap ?: ""
                if (adim.evetteDuduk) Duduk.baslat()
                yanit = m
                Seslendirici.oku("$m Hazırsan sonraki de.") { if (elleriSerbest) dinle() }
            } else ileri()
            Niyet.HAYIR -> if (adim.soru != null) {
                val m = adim.hayirCevap ?: ""
                if (adim.hayirdaDuduk) Duduk.baslat()
                yanit = m
                Seslendirici.oku("$m Hazırsan sonraki de.") { if (elleriSerbest) dinle() }
            }
            Niyet.DUDUK -> {
                Duduk.baslat()
                Seslendirici.oku("Düdüğü başlattım.")
            }
            Niyet.ISIK -> {
                SosIsik.baslat(ctx)
                Seslendirici.oku("SOS ışığını yaktım.")
            }
            Niyet.KANAMA -> if (rehber.id != "kanama") rehberAc("kanama")
            Niyet.ENKAZ -> if (rehber.id != "enkaz") rehberAc("enkaz")
            Niyet.GAZ -> if (rehber.id != "gaz") rehberAc("gaz")
            Niyet.PANIK -> if (rehber.id != "panik") rehberAc("panik")
            else -> Seslendirici.oku("Anlayamadım. Sonraki, tekrar ya da dur diyebilirsin.")
        }
    }

    dinle = {
        mikrofonla {
            elleriSerbest = true
            dinleyici.dinle { metin -> isle(metin) }
        }
    }

    EkranAcikKalsin()

    // Her adım açıldığında sesli oku; eller serbest moddaysa okuma bitince dinle
    LaunchedEffect(rehber.id, sira) {
        duyulan = null
        val soruMetni = adim.soru?.let { " $it Evet ya da hayır de." } ?: ""
        Seslendirici.oku("Adım ${sira + 1}. ${adim.baslik} ${adim.aciklama}$soruMetni") {
            if (elleriSerbest && izinVar(ctx)) dinle()
        }
    }
    DisposableEffect(Unit) {
        onDispose { Seslendirici.sus() }
    }

    EkranSutunu {
        GeriSatiri(rehber.ad, bitti)
        Text("Adım ${sira + 1} / ${rehber.adimlar.size}", color = Renk.GeceSoluk, fontSize = 16.sp)
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
        Spacer(Modifier.height(8.dp))
        Text(adim.baslik, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold, color = Renk.GeceYazi)
        Text(adim.aciklama, fontSize = 20.sp, lineHeight = 29.sp, color = Color(0xFFC9D3E0))

        // Bu adımda bir soru varsa: evet / hayır
        adim.soru?.let { soru ->
            Kart(koyu = true) {
                Text(soru, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Renk.Lamba)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        BuyukButon("Evet", Renk.Yesil, Color.White, { cevapla(true) }, yukseklik = 60)
                    }
                    Box(Modifier.weight(1f)) {
                        BuyukButon("Hayır", Renk.Kirmizi, Color.White, { cevapla(false) }, yukseklik = 60)
                    }
                }
                yanit?.let {
                    Text(it, fontSize = 18.sp, lineHeight = 26.sp, color = Renk.GeceYazi)
                }
            }
        }

        KonusPaneli(
            dinleyici = dinleyici,
            duyulan = duyulan,
            ipucu = if (adim.soru != null) "\"Evet\", \"hayır\", \"tekrar\" ya da \"sonraki\" diyebilirsin"
            else "Ellerin meşgulse \"sonraki\", \"tekrar\" ya da \"dur\" de",
            onClick = { dinle() },
        )

        BuyukButon(
            yazi = if (son) "Tamam, afet moduna dön" else "Yaptım, sonraki adım",
            zemin = Color.Transparent,
            yaziRengi = Renk.GeceYazi,
            cerceve = Renk.GeceCizgi,
            onClick = { ileri() },
            yukseklik = 64,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                BuyukButon(
                    "Tekrar oku",
                    Color.Transparent,
                    Renk.GeceYazi,
                    { Seslendirici.oku("${adim.baslik} ${adim.aciklama} ${adim.soru ?: ""}") },
                    Renk.GeceCizgi,
                    52,
                    17,
                )
            }
            if (sira > 0) {
                Box(Modifier.weight(1f)) {
                    BuyukButon("Önceki adım", Color.Transparent, Renk.GeceYazi, { sira-- }, Renk.GeceCizgi, 52, 17)
                }
            }
        }
        SesSatiri()
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
        Text(
            "Bu bölüm henüz hazırlanıyor. Şimdilik e-Devlet'teki AFAD toplanma alanı sorgulamasından kendi alanını öğrenip not et.",
            fontSize = 16.sp,
            color = Renk.GeceYazi,
        )
        alanlar.forEach { a ->
            Kart(koyu = true) {
                Text(a.ad, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Renk.GeceYazi)
                Text("${a.mesafe} · ${a.sure}", fontSize = 16.sp, color = Renk.GeceSoluk)
            }
        }
        Text(
            "Yukarıdakiler örnek veridir; gerçek alanları bir sonraki sürümlerde ekleyeceğiz.",
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

    EkranAcikKalsin()
    DisposableEffect(Unit) {
        onDispose { Seslendirici.sus() }
    }

    LaunchedEffect(Unit) {
        Seslendirici.oku("Yalnız değilsin. Birlikte nefes alalım.")
        delay(2500)
        val evreler = listOf("Nefes al" to 4, "Tut" to 4, "Yavaşça ver" to 6)
        while (true) {
            for ((ad, sure) in evreler) {
                evre = ad
                Seslendirici.oku(ad)
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
        SesSatiri()
    }
}
