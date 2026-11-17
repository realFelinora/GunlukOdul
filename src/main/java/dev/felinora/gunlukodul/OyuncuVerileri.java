package dev.felinora.gunlukodul;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Oyuncuların son ödül tarihini ve serisini data.yml dosyasında saklar.
 */
public final class OyuncuVerileri {

    private final File dosya;
    private final Logger log;
    private YamlConfiguration veri;

    public OyuncuVerileri(File klasor, Logger log) {
        this.dosya = new File(klasor, "data.yml");
        this.log = log;
        this.veri = YamlConfiguration.loadConfiguration(dosya);
    }

    /** Oyuncunun en son ödül aldığı gün. Hiç almadıysa null. */
    public LocalDate sonAlim(UUID oyuncu) {
        String tarih = veri.getString(oyuncu + ".son");
        return tarih == null ? null : LocalDate.parse(tarih);
    }

    public int seri(UUID oyuncu) {
        return veri.getInt(oyuncu + ".seri", 0);
    }

    public void kaydet(UUID oyuncu, LocalDate gun, int seri) {
        veri.set(oyuncu + ".son", gun.toString());
        veri.set(oyuncu + ".seri", seri);
        try {
            veri.save(dosya);
        } catch (IOException e) {
            log.severe("data.yml kaydedilemedi: " + e.getMessage());
        }
    }
}
