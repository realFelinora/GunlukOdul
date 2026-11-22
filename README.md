# GunlukOdul

Paper/Spigot sunucuları için **seri (streak) bonuslu günlük ödül** eklentisi.
Oyuncular her gün `/gunluk` yazarak ödül alır; üst üste giriş yaptıkça ödüller büyür.

**⬇️ İndir:** [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/daily-streak-rewards-gunlukodul) (önerilen) • [GitHub Releases](../../releases)

## Özellikler
- `/gunluk` ile günlük ödül, her gün yeni ödül
- **Seri sistemi:** Üst üste alınan günler sayılır, bir gün kaçırılırsa seri sıfırlanır
- 7 günlük (ayarlanabilir) ödül döngüsü
- Ödül olarak **eşya** ve **konsol komutu** (ör. `eco give %oyuncu% 500`)
- Envanter doluysa eşyalar oyuncunun yanına bırakılır
- Sunucuya girişte "ödülün seni bekliyor" hatırlatması
- Türkiye saatine göre gün değişimi (ayarlanabilir)
- Tüm mesajlar Türkçe ve `config.yml` üzerinden değiştirilebilir, `&` renk kodları desteklenir

## Komutlar
| Komut | Açıklama | Yetki |
|---|---|---|
| `/gunluk` | Günlük ödülü al | `gunlukodul.kullan` (herkes) |
| `/gunluk bilgi` | Serini ve bugünkü durumu gör | `gunlukodul.kullan` |
| `/gunluk yenile` | `config.yml` dosyasını yeniden yükle | `gunlukodul.yonetici` (OP) |

## Kurulum
1. [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/daily-streak-rewards-gunlukodul) ya da [Releases](../../releases) kısmından `GunlukOdul-x.x.x.jar` dosyasını indirin.
2. Sunucunuzun `plugins` klasörüne atın ve sunucuyu yeniden başlatın.
3. `plugins/GunlukOdul/config.yml` dosyasından ödülleri ve mesajları düzenleyin, ardından `/gunluk yenile` yazın.

**Uyumluluk:** Paper / Spigot 1.21+ , Java 21+

## Derleme
```sh
./gradlew build
# çıktı: build/libs/GunlukOdul-1.0.0.jar
```
