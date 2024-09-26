package me.munzirahmed.horseinv;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class HorseInventory extends JavaPlugin implements Listener {

    private HashMap<UUID, Inventory> horseInventories;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        horseInventories = new HashMap<>();

        // Load inventory data
        loadInventories();

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("HorseInventoryPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save inventory data
        saveInventories();

        getLogger().info("HorseInventoryPlugin has been disabled!");
    }

    @EventHandler
    public void onPlayerInteractHorse(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Horse horse) {
            Player player = event.getPlayer();

            // Check if the player is sneaking (left shift) and right-clicking
            if (player.isSneaking()) {
                // Check if the horse is tamed and wearing a saddle
                if (horse.isTamed() && horse.getInventory().getSaddle() != null) {
                    UUID horseUUID = horse.getUniqueId();

                    if (!horseInventories.containsKey(horseUUID)) {
                        Inventory horseInventory = Bukkit.createInventory(null, 9, ChatColor.BLACK + "Saddle Storage");
                        horseInventories.put(horseUUID, horseInventory);
                    }

                    player.openInventory(horseInventories.get(horseUUID));
                } else if (!horse.isTamed()) {
                    player.sendMessage(ChatColor.RED + "This horse is not tamed!");
                } else {
                    player.sendMessage(ChatColor.RED + "This horse requires a saddle to access the inventory!");
                }

                event.setCancelled(true); // Prevent default behavior
            }
        }
    }

    private void loadInventories() {
        config = getConfig();
        File file = new File(getDataFolder(), "horseInventories.yml");
        if (file.exists()) {
            for (String key : config.getKeys(false)) {
                UUID horseUUID = UUID.fromString(key);
                Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.BLACK + "Saddle Storage");

                for (int i = 0; i < inventory.getSize(); i++) {
                    if (config.contains(key + "." + i)) {
                        inventory.setItem(i, config.getItemStack(key + "." + i));
                    }
                }

                horseInventories.put(horseUUID, inventory);
            }
        }
    }

    private void saveInventories() {
        File file = new File(getDataFolder(), "horseInventories.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        for (UUID horseUUID : horseInventories.keySet()) {
            Inventory inventory = horseInventories.get(horseUUID);
            String key = horseUUID.toString();

            for (int i = 0; i < inventory.getSize(); i++) {
                config.set(key + "." + i, inventory.getItem(i));
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            getLogger().severe("Could not save horse inventories!");
            e.printStackTrace();
        }
    }
}
