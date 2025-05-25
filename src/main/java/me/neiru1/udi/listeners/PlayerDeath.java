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

    private boolean hasPermission(Player player) {
        return player.hasPermissions(2);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (hasPermission(player)) {
            return;
        }

        Iterator<ItemEntity> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            ItemEntity itemEntity = iterator.next();
            ItemStack item = itemEntity.getItem();

            if (item == null) continue;

            String registryName = ForgeRegistries.ITEMS.getKey(item.getItem()).toString();

            if (ModConfig.undroppableItems.get().contains(registryName)) {
                iterator.remove();
            }
        }
    }
}
