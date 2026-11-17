package dev.felinora.gunlukodul;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GunlukOdul extends JavaPlugin implements Listener {

    private OyuncuVerileri veriler;
    private ZoneId saatDilimi;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ayarlariYukle();
        veriler = new OyuncuVerileri(getDataFolder(), getLogger());
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("GunlukOdul aktif!");
    }

    private void ayarlariYukle() {
        reloadConfig();
        try {
            saatDilimi = ZoneId.of(getConfig().getString("saat-dilimi", "Europe/Istanbul"));
        } catch (Exception e) {
            getLogger().warning("Geçersiz saat-dilimi, Europe/Istanbul kullanılıyor.");
            saatDilimi = ZoneId.of("Europe/Istanbul");
        }
    }

    // ---------------- Komut ----------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("yenile")) {
            if (!sender.hasPermission("gunlukodul.yonetici")) {
                mesaj(sender, "yetki-yok");
                return true;
            }
            ayarlariYukle();
            mesaj(sender, "yenilendi");
            return true;
        }

        if (!(sender instanceof Player oyuncu)) {
            mesaj(sender, "sadece-oyuncu");
            return true;
        }
        if (!oyuncu.hasPermission("gunlukodul.kullan")) {
            mesaj(oyuncu, "yetki-yok");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("bilgi")) {
            bilgiGoster(oyuncu);
        } else {
            odulVer(oyuncu);
        }
        return true;
    }

    private void bilgiGoster(Player oyuncu) {
        UUID id = oyuncu.getUniqueId();
        LocalDate bugun = LocalDate.now(saatDilimi);
        LocalDate son = veriler.sonAlim(id);
        boolean seriDevam = son != null && !son.isBefore(bugun.minusDays(1));
        int seri = seriDevam ? veriler.seri(id) : 0;
        String durum = bugun.equals(son) ? "&aEvet" : "&cHayır";
        mesaj(oyuncu, "bilgi", "%seri%", String.valueOf(seri), "%durum%", durum);
    }

    private void odulVer(Player oyuncu) {
        UUID id = oyuncu.getUniqueId();
        LocalDate bugun = LocalDate.now(saatDilimi);
        LocalDate son = veriler.sonAlim(id);

        if (bugun.equals(son)) {
            mesaj(oyuncu, "zaten-alindi", "%kalan%", geceyarisinaKalan());
            return;
        }

        int seri;
        if (son != null && son.equals(bugun.minusDays(1))) {
            seri = veriler.seri(id) + 1;
        } else {
            if (son != null) mesaj(oyuncu, "seri-bozuldu");
            seri = 1;
        }

        int dongu = Math.max(1, getConfig().getInt("seri-dongu", 7));
        int gun = ((seri - 1) % dongu) + 1;

        // Önce kaydet ki aynı anda iki kez komut yazılırsa çift ödül çıkmasın.
        veriler.kaydet(id, bugun, seri);
        odulleriDagit(oyuncu, gun);
        mesaj(oyuncu, "alindi", "%gun%", String.valueOf(gun), "%seri%", String.valueOf(seri));
    }

    private void odulleriDagit(Player oyuncu, int gun) {
        ConfigurationSection bolum = getConfig().getConfigurationSection("odul." + gun);
        if (bolum == null) {
            getLogger().warning(gun + ". gün için config.yml'de ödül tanımlı değil.");
            return;
        }

        boolean yereDustu = false;
        for (String satir : bolum.getStringList("esyalar")) {
            ItemStack esya = esyaOku(satir);
            if (esya == null) continue;
            Map<Integer, ItemStack> artan = oyuncu.getInventory().addItem(esya);
            for (ItemStack fazla : artan.values()) {
                oyuncu.getWorld().dropItemNaturally(oyuncu.getLocation(), fazla);
                yereDustu = true;
            }
        }
        if (yereDustu) mesaj(oyuncu, "envanter-dolu");

        List<String> komutlar = bolum.getStringList("komutlar");
        for (String komut : komutlar) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), komut.replace("%oyuncu%", oyuncu.getName()));
        }
    }

    /** "DIAMOND:4" gibi bir satırı eşyaya çevirir. Hatalıysa konsola uyarı yazar. */
    private ItemStack esyaOku(String satir) {
        String[] parca = satir.split(":");
        Material malzeme = Material.matchMaterial(parca[0].trim());
        if (malzeme == null || !malzeme.isItem()) {
            getLogger().warning("Bilinmeyen eşya: " + satir);
            return null;
        }
        int adet = 1;
        if (parca.length > 1) {
            try {
                adet = Math.max(1, Integer.parseInt(parca[1].trim()));
            } catch (NumberFormatException e) {
                getLogger().warning("Geçersiz adet: " + satir);
            }
        }
        return new ItemStack(malzeme, adet);
    }

    private String geceyarisinaKalan() {
        ZonedDateTime simdi = ZonedDateTime.now(saatDilimi);
        ZonedDateTime yarin = simdi.toLocalDate().plusDays(1).atStartOfDay(saatDilimi);
        Duration kalan = Duration.between(simdi, yarin);
        long saat = kalan.toHours();
        long dakika = kalan.toMinutesPart();
        return saat > 0 ? saat + " saat " + dakika + " dakika" : dakika + " dakika";
    }

    // ---------------- Giriş hatırlatması ----------------

    @EventHandler
    public void girisYapti(PlayerJoinEvent event) {
        if (!getConfig().getBoolean("giris-hatirlatma", true)) return;
        Player oyuncu = event.getPlayer();
        if (!oyuncu.hasPermission("gunlukodul.kullan")) return;
        if (LocalDate.now(saatDilimi).equals(veriler.sonAlim(oyuncu.getUniqueId()))) return;
        // 2 saniye sonra göster ki giriş mesajlarının arasında kaybolmasın.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (oyuncu.isOnline()) mesaj(oyuncu, "hatirlatma");
        }, 40L);
    }

    // ---------------- Mesaj yardımcısı ----------------

    private void mesaj(CommandSender kime, String anahtar, String... degisenler) {
        String metin = getConfig().getString("mesajlar." + anahtar, anahtar);
        for (int i = 0; i + 1 < degisenler.length; i += 2) {
            metin = metin.replace(degisenler[i], degisenler[i + 1]);
        }
        String onek = getConfig().getString("mesajlar.onek", "");
        // ChatColor hem Paper'da hem Spigot'ta çalışır.
        kime.sendMessage(ChatColor.translateAlternateColorCodes('&', onek + metin));
    }
}
