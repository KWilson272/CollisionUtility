package me.kwilson272.collisionutility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class CollisionUtility extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("collisions.txt", false);

        Bukkit.getPluginManager().registerEvents(new ReloadListener(this), this);

        // Run later to ensure all addons have loaded their abilities
        Bukkit.getScheduler().runTaskLater(this, () -> new CollisionParser(this), 2);
        getLogger().log(Level.INFO, "CollisionUtility by KWilson272 has been enabled!");
    }

    protected void reload() {
        Bukkit.getScheduler().runTaskLater(this, () -> new CollisionParser(this), 2);
        getLogger().log(Level.INFO, "CollisionUtility has reloaded.");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "CollisionUtility by KWilson272 has been disabled.");
    }
}
