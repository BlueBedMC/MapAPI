package com.bluebed.mapapi.spigot;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class MapAPISpigot implements Listener {
    private static final Set<MapAPI> INSTANCES = new HashSet<>();

    // call this vro!!1
    public void init(JavaPlugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // this too vro!11
    public void shutdown(JavaPlugin plugin) {
        for (MapAPI api : INSTANCES) {
            api.remove();
        }
    }

    public static void addMapInstance(MapAPI instance) {
        INSTANCES.add(instance);
    }

    public static void removeMapInstance(MapAPI instance) {
        INSTANCES.remove(instance);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (!hasFrame(event.getEntity().getEntityId())) return;
        event.setCancelled(true);
    }

    public boolean hasFrame(int eId) {
        for (MapAPI mapAPI : INSTANCES) {
            if (mapAPI.hasEntityId(eId)) return true;
        }
        return false;
    }
}
