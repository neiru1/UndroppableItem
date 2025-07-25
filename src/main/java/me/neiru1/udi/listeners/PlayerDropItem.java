package me.neiru1.udi.listeners;

import me.neiru1.udi.config.ModConfig;
import me.neiru1.udi.util.MessageRateLimiter;
import me.neiru1.udi.UDI;
import me.neiru1.udi.util.UDIModState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.ChatFormatting;

import java.util.List;

@Mod.EventBusSubscriber(modid = "udi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDropItem {

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        if (!UDIModState.isModEnabled) return;
        Player player = event.getPlayer();
        ItemStack itemStack = event.getEntity().getItem();

        ResourceLocation regName = itemStack.getItem().builtInRegistryHolder().key().location();
        if (regName == null) return;

        String itemType = regName.toString();
        List<? extends String> undroppableItems = ModConfig.undroppableItems.get();
        String cannotDropMessage = ModConfig.cannotDropMessage.get();

        // drop script
        if (undroppableItems.contains(itemType)) {
            event.setCanceled(true);
            // give the item back for drag-drop safety
            player.getInventory().placeItemBackInInventory(itemStack);

        if (cannotDropMessage != null && !cannotDropMessage.isBlank()) {
        if (MessageRateLimiter.canSendMessage(player.getUUID())) {
        String itemName = itemStack.getHoverName().getString();
        Component message = Component.literal(cannotDropMessage + " (" + itemName + ")")
                .withStyle(style -> style.withColor(ChatFormatting.RED));
            player.sendSystemMessage(message);
            }
        }
        }
    }
}