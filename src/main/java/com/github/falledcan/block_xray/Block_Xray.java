package com.github.falledcan.block_xray;

import org.bukkit.plugin.java.JavaPlugin;

public final class Block_Xray extends JavaPlugin {

    private XrayManager manager;
    private Messages messages;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        messages = new Messages(this);
        manager = new XrayManager(this);
        manager.removeLeftovers();
        reload();

        getServer().getPluginManager().registerEvents(new Listeners(manager), this);
        getCommand("xray").setExecutor(new XrayCmd(this, manager, messages));
        getCommand("xray").setTabCompleter(new XrayCmdTab());
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
