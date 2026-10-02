# Yoldaş · Android ilk sürüm

Bu paket, Yoldaş'ın internetsiz çalışan ilk sürümünün Kotlin kodudur:

- İnternetsiz ilk yardım rehberleri (kanama, enkaz altı, gaz kokusu, korku/panik)
- Gerçekten çalan düdük (15 saniyede bir, alarm ses kanalından)
- Telefonun fenerini Mors alfabesiyle SOS olarak yakan ışık
- Telefonda saklanan deprem hazırlık listesi
- Toplanma alanları ekranı (şimdilik örnek veri)
- Sakinleşme için yönlendirmeli nefes ekranı

## 1. Android Studio'yu kur

1. https://developer.android.com/studio adresinden Android Studio'yu indir ve kur (Windows için).
2. İlk açılışta önerilen kurulumu (Standard) seç; Android SDK'yı kendisi indirir.

## 2. Boş projeyi oluştur

1. **New Project → Empty Activity** seç.
2. Şu bilgileri gir:
   - **Name:** `Yoldas`
   - **Package name:** `com.yoldas.app`  ← bu önemli, birebir aynı olmalı
   - **Language:** Kotlin
   - **Minimum SDK:** API 26 (Android 8.0)
   - **Build configuration language:** Kotlin DSL
3. **Finish**'e bas ve alttaki ilerleme çubuğu bitene kadar bekle (ilk seferde birkaç dakika sürebilir).

## 3. Dosyaları yerleştir

Bu paketteki `app/src/main/java/com/yoldas/app/` klasöründeki **tüm .kt dosyalarını**, projendeki aynı klasöre kopyala:

```
Yoldas\app\src\main\java\com\yoldas\app\
```

- `MainActivity.kt` dosyası zaten var; **üzerine yaz** (değiştir).
- Diğer dosyalar yeni eklenecek: `Renk.kt`, `Rehberler.kt`, `Duduk.kt`, `SosIsik.kt`, `Hazirlik.kt`, `Ekranlar.kt`.
- Android Studio'nun oluşturduğu `ui/theme` klasörü kalabilir, sorun olmaz.

Ek izin veya kütüphane gerekmiyor; "Empty Activity" şablonunun getirdikleri yeterli.

## 4. Telefonunda çalıştır

1. Telefonda **Ayarlar → Telefon hakkında → Yapım numarası**'na 7 kez dokun; geliştirici seçenekleri açılır.
2. **Ayarlar → Geliştirici seçenekleri → USB hata ayıklama**'yı aç.
3. Telefonu USB kablosuyla bilgisayara bağla, telefonda çıkan izni onayla.
4. Android Studio'nun üstünde telefonunun adını seç ve yeşil **Run (▶)** düğmesine bas.

## 5. Dene

- Ana sayfada **Acil durum** → afet modu.
- **Düdük çal** → telefon yüksek sesli üçlü düdük çalar (alarm sesi seviyesini kontrol et).
- **SOS ışığı** → arka fener ··· ––– ··· şeklinde yanıp söner.
- **Yaralı var / Enkaz altındayım / Gaz kokusu** → adım adım rehber.
- Uçak modunu açıp tekrar dene: hepsi internetsiz çalışır.

## Bir hata çıkarsa

Android Studio'nun altındaki **Build** penceresinde çıkan kırmızı hata metnini olduğu gibi kopyalayıp bana gönder, birlikte düzeltelim.

## Sıradaki adımlar

1. Gerçek toplanma alanları ve çevrimdışı harita
2. Sarsıntı algılama ve "İyi misin?" ekranı
3. Tıbbi kimlik ve aile kişileri
4. Telefonda çalışan yapay zeka ve sesli konuşma
5. Bluetooth ile telefondan telefona yardım sinyali

> Not: Rehber metinleri genel ilk yardım bilgilerine dayanır. Yayından önce bir sağlık / afet uzmanına kontrol ettirilmelidir.
