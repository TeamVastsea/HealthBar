package cc.vastsea.healthbar;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityDamageListener extends JavaPlugin implements Listener {

    private Map<UUID, BossBar> bossBars;

    @Override
    public void onEnable() {
        bossBars = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        bossBars.values().forEach(BossBar::removeAll);
        bossBars.clear();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        double maxHealth = player.getMaxHealth();
        double currentHealth = player.getHealth();
        double healthPercentage = currentHealth / maxHealth;

        // Ensure progress is between 0.0 and 1.0
        healthPercentage = Math.min(1.0, Math.max(0.0, healthPercentage));

        if (bossBars.containsKey(player.getUniqueId())) {
            BossBar bossBar = bossBars.get(player.getUniqueId());
            bossBar.setProgress(healthPercentage);
        } else {
            BossBar bossBar = Bukkit.createBossBar("Health", BarColor.RED, BarStyle.SOLID);
            bossBar.setProgress(healthPercentage);
            bossBar.addPlayer(player);
            bossBars.put(player.getUniqueId(), bossBar);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        UUID playerId = player.getUniqueId();

        if (bossBars.containsKey(playerId)) {
            BossBar bossBar = bossBars.remove(playerId);
            bossBar.removeAll();
        }
    }
}
