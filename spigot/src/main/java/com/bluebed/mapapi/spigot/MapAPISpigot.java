package com.bluebed.mapapi.spigot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class MapAPISpigot implements Listener {
    private static final Set<Integer> ITEM_FRAMES = new HashSet<>();

    // call this vro!!1
    public void init(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void addItemFrame(Hanging hanging) {
        ITEM_FRAMES.add(hanging.getEntityId());
    }

    public static void removeItemFrame(Hanging hanging) {
        ITEM_FRAMES.remove(hanging.getEntityId());
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (!ITEM_FRAMES.contains(event.getEntity().getEntityId())) return;
        event.setCancelled(true);
    }
}
