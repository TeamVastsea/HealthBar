package cc.vastsea.healthbar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EntityDamageListener implements Listener {
    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private final Map<UUID, BukkitRunnable> bossBarTimers = new HashMap<>();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damagee = event.getEntity();
        Entity damager = event.getDamager();
        if (!(damager instanceof Player player)) return;

        BossBar bossBar = bossBars.computeIfAbsent(damagee.getUniqueId(),
                uuid -> Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID));
        bossBar.addPlayer(player);
        setBossBar(bossBar, damagee, event.getDamage());
        resetBossBarTimer(damagee.getUniqueId(), bossBar);
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!bossBars.containsKey(entity.getUniqueId())) return;
        BossBar bossBarRecord = bossBars.get(entity.getUniqueId());

        setBossBar(bossBarRecord, entity, event.getDamage());
        resetBossBarTimer(entity.getUniqueId(), bossBarRecord);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (!bossBars.containsKey(entity.getUniqueId())) return;
        removeBossBar(entity.getUniqueId());
    }

    private void setBossBar(BossBar bossBar, Entity entity, double damage) {
        if (!(entity instanceof Damageable damageable) || !(entity instanceof Attributable attributable)) return;

        double maxHealth = Objects.requireNonNull(attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        double health = damageable.getHealth() - damage;
        double progress = health / maxHealth;
        if (progress < 0) {
            progress = 0;
            health = 0;
        }

        String name = entity.getCustomName() == null ? entity.getType().name().toLowerCase() : entity.getCustomName();

        if (progress > 0.6) {
            bossBar.setColor(BarColor.GREEN);
        } else if (progress > 0.3) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.RED);
        }

        String title = String.format(
                "%s %s%.2f%s/%s%.2f%s (%s-%.2f%s)",
                name,
                ChatColor.YELLOW, health,
                ChatColor.WHITE,
                ChatColor.YELLOW, maxHealth,
                ChatColor.WHITE,
                ChatColor.RED, damage,
                ChatColor.WHITE
        );
        bossBar.setTitle(title);
        bossBar.setProgress(progress);
        bossBar.setVisible(true);
    }

    private void resetBossBarTimer(UUID entityUUID, BossBar bossBar) {
        if (bossBarTimers.containsKey(entityUUID)) {
            bossBarTimers.get(entityUUID).cancel();
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.removeAll();
                bossBar.setVisible(false);
                bossBars.remove(entityUUID);
                bossBarTimers.remove(entityUUID);
            }
        };
        int coolDownTime = 3;
        task.runTaskLater(HealthBarPlugin.INSTANCE, coolDownTime * 20L); // 60 ticks = 3 seconds
        bossBarTimers.put(entityUUID, task);
    }

    private void removeBossBar(UUID entityUUID) {
        if (!bossBars.containsKey(entityUUID)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                BossBar bossBar = bossBars.get(entityUUID);
                bossBar.removeAll();
                bossBar.setVisible(false);
                bossBars.remove(entityUUID);
                bossBarTimers.remove(entityUUID);
            }
        }.runTaskLater(HealthBarPlugin.INSTANCE, 20L);
    }
}
