package me.neiru1.udi.listeners;

import me.neiru1.udi.config.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "udi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDropItem {

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getEntity().getItem();

        // Get registry name
        ResourceLocation regName = itemStack.getItem().builtInRegistryHolder().key().location();
        if (regName == null) return;

        String itemType = regName.toString();
        List<? extends String> undroppableItems = ModConfig.undroppableItems.get();
        String cannotDropMessage = ModConfig.cannotDropMessage.get();

        // If item is undroppable, cancel drop and notify player
        if (undroppableItems.contains(itemType)) {
            event.setCanceled(true);
            // Give the item back (for drag-drop safety, though usually not needed)
            player.getInventory().placeItemBackInInventory(itemStack);

            if (cannotDropMessage != null && !cannotDropMessage.isBlank()) {
                player.sendSystemMessage(Component.literal(cannotDropMessage));
            }
        }
    }
}