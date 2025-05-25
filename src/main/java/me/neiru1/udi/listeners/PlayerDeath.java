package me.neiru1.udi.listeners;

import me.neiru1.udi.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Iterator;

public class PlayerDeath {

    // In Forge, permissions are usually managed by OP level, no direct "permission nodes" like Bukkit.
    // Here, players with permission level 2+ (OP) are excluded:
    private boolean hasPermission(Player player) {
        return player.hasPermissions(2);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDropsEvent event) {
        // Fix 1: Check if the entity is a Player first
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity(); // Safe cast now

        if (hasPermission(player)) {
            // Player has permission to bypass undroppable checks
            return;
        }

        Iterator<ItemEntity> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            ItemEntity itemEntity = iterator.next();
            ItemStack item = itemEntity.getItem();

            if (item == null) continue;

            // Fix 2: Use ForgeRegistries correctly (already imported)
            String registryName = ForgeRegistries.ITEMS.getKey(item.getItem()).toString();

            // If the item is in the undroppable list, remove it from drops
            if (ModConfig.undroppableItems.get().contains(registryName)) {
                iterator.remove();
            }
        }
    }
}
