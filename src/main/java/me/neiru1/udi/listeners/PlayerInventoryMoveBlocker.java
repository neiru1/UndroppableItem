package me.neiru1.udi.listeners;

import me.neiru1.udi.config.ModConfig;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

@Mod.EventBusSubscriber(modid = "udi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerInventoryMoveBlocker {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        AbstractContainerMenu container = player.containerMenu;
        List<? extends String> undroppableItems = ModConfig.undroppableItems.get();

        for (Slot slot : container.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            ResourceLocation regName = stack.getItem().builtInRegistryHolder().key().location();
            if (regName == null) continue;

            String itemId = regName.toString();
            if (!undroppableItems.contains(itemId)) continue;

            boolean isInPlayerInventory = slot.container == player.getInventory();

            boolean isInCurios = CuriosApi.getCuriosHelper()
                    .findCurios(player, s -> ItemStack.isSameItemSameTags(s, stack))
                    .size() > 0;

            if (isInPlayerInventory || isInCurios) {
                continue;
            }

            ItemStack copy = stack.copy();
            slot.set(ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(copy);

            String msg = ModConfig.cannotDropMessage.get();
            if (msg != null && !msg.isBlank()) {
                player.sendSystemMessage(Component.literal(msg));
            }
        }
    }
}
