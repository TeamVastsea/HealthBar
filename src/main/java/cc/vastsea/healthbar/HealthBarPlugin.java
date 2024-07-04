package cc.vastsea.healthbar;

import org.bukkit.plugin.java.JavaPlugin;

public class HealthBarPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
        getLogger().info("HealthBar plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HealthBar plugin disabled!");
    }
}
