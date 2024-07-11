package cc.vastsea.healthbar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
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
    private final Map<UUID, BossBarRecord> bossBars = new HashMap<>();
    private final Map<UUID, Short> bossBarTimers = new HashMap<>();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damagee = event.getEntity();
        Entity damager = event.getDamager();
        if (!(damager instanceof Player player)) return;

        if (isMonitoredEntity(damagee)) return;

        BossBarRecord bossBarRecord = bossBars.computeIfAbsent(damagee.getUniqueId(), BossBarRecord::new);
        bossBarRecord.bossBar.addPlayer(player);
        setBossBar(bossBarRecord.bossBar, damagee, event.getDamage());
        resetBossBarTimer(damagee.getUniqueId());
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!bossBars.containsKey(entity.getUniqueId())) return;
        if (isMonitoredEntity(entity)) return;

        BossBarRecord bossBarRecord = bossBars.get(entity.getUniqueId());
        setBossBar(bossBarRecord.bossBar, entity, event.getDamage());
        resetBossBarTimer(entity.getUniqueId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (!bossBars.containsKey(entity.getUniqueId())) return;
        bossBarTimers.put(entity.getUniqueId(), (short) 1);
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

    private void resetBossBarTimer(UUID entityUUID) {
        bossBarTimers.put(entityUUID, (short) 3);
    }

    private boolean isMonitoredEntity(Entity entity) {
        return !(entity instanceof Player) && !(entity instanceof Monster) && !(entity instanceof Animals) && !(entity instanceof EnderDragon);
    }

    class BossBarRecord {
        BossBar bossBar;

        BossBarRecord(UUID uuid) {
            this.bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SEGMENTED_10);
            bossBarTimers.put(uuid, (short) 3);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Short timer = bossBarTimers.get(uuid);
                    if (timer != null && timer > 0) {
                        bossBarTimers.put(uuid, (short) (timer - 1));
                        return;
                    }

                    bossBar.removeAll();
                    bossBar.setVisible(false);
                    bossBars.remove(uuid);
                    bossBarTimers.remove(uuid);
                    this.cancel();
                }
            }.runTaskTimer(HealthBarPlugin.INSTANCE, 20L, 20L);
        }
    }
}
