package cc.vastsea.healthbar;

import org.bukkit.Bukkit;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EntityDamageListener implements Listener {
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damagee = event.getEntity();
        Entity damager = event.getDamager();
        if (!(damager instanceof Player player)) return;

        BossBar bossBar = bossBars.computeIfAbsent(damagee.getUniqueId(),
                uuid -> Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID));
        bossBar.addPlayer(player);
        setBossBar(bossBar, damagee, event.getDamage());
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!bossBars.containsKey(entity.getUniqueId())) return;
        BossBar bossBarRecord = bossBars.get(entity.getUniqueId());

        setBossBar(bossBarRecord, entity, event.getDamage());
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


    private void setBossBar(BossBar bossBar, Entity entity, double damage) {
        if (!(entity instanceof Damageable damageable) || !(entity instanceof Attributable attributable)) return;

        double maxHealth = Objects.requireNonNull(attributable.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        double health = damageable.getHealth();
        double progress = health / maxHealth;

        String name = entity.getCustomName() == null ? entity.getType().name().toLowerCase() : entity.getCustomName();
        String title = String.format("[%s] %.2f/%.2f -%.2f", name, health, maxHealth, damage);

        if (progress > 0.6) {
            bossBar.setColor(BarColor.GREEN);
        } else if (progress > 0.3) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.RED);
        }
        bossBar.setTitle(title);
        bossBar.setProgress(progress);
        bossBar.setVisible(true);
    }
}
