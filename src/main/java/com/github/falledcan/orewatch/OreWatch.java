package com.github.falledcan.orewatch;

import org.bukkit.plugin.java.JavaPlugin;

public final class OreWatch extends JavaPlugin {

    private GlowManager manager;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new Messages(this);
        manager = new GlowManager(this);
        manager.removeLeftovers();
        reload();

        getServer().getPluginManager().registerEvents(new Listeners(manager), this);
        getCommand("orewatch").setExecutor(new OreWatchCommand(this, manager, messages));
        getCommand("orewatch").setTabCompleter(new OreWatchTabCompleter());
    }

    void reload() {
        reloadConfig();
        messages.load();
        manager.start();
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.shutdown();
        }
    }
}
