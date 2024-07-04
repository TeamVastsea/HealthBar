package cc.vastsea.healthbar;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class HealthBarPlugin extends JavaPlugin {

    public static JavaPlugin INSTANCE;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        getLogger().info("HealthBar plugin enabled!");
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        getLogger().info("HealthBar plugin disabled!");
    }
}
