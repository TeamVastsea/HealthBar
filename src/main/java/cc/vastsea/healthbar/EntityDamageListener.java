package cc.vastsea.healthbar;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class EntityDamageListener implements Listener {
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damagee = event.getEntity();
        Entity damager = event.getDamager();
        if (!(damager instanceof Player player)) return;
        if (!(damagee instanceof Damageable damageable) || !(damagee instanceof Attributable attributable)) return;

        double maxHealth = Objects.requireNonNull(attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        double health = damageable.getHealth();
        double progress = health / maxHealth;

        BossBar bossBar = bossBars.computeIfAbsent(damagee.getUniqueId(), uuid -> Bukkit.createBossBar(String.valueOf(damagee.getType()), BarColor.RED, BarStyle.SOLID));
        bossBar.setProgress(progress);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (!bossBars.containsKey(entity.getUniqueId())) return;
        BossBar bossBar = bossBars.get(entity.getUniqueId());
        bossBar.removeAll();
        bossBar.setVisible(false);
        bossBars.remove(entity.getUniqueId());
    }
}
