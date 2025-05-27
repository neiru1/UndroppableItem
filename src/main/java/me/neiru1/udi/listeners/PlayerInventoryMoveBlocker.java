package me.neiru1.udi.listeners;

import me.neiru1.udi.config.ModConfig;
import me.neiru1.udi.util.MessageRateLimiter;
import me.neiru1.udi.UDI;
import me.neiru1.udi.util.UDIModState;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

@Mod.EventBusSubscriber(modid = "udi", bus = Mod.EventBusSubscriber.Bus.FORGE)



public class PlayerInventoryMoveBlocker {

    @SubscribeEvent
    // checks when container is closed  
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        if (!UDIModState.isModEnabled) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        AbstractContainerMenu container = event.getContainer();
        List<? extends String> undroppableItems = ModConfig.undroppableItems.get();
        BlockEntity containerEntity = getContainerBlockEntity(player);
        boolean isAllowedContainer = false;

        if (containerEntity != null) {
            Block block = containerEntity.getBlockState().getBlock();
            ResourceLocation blockId = block.builtInRegistryHolder().key().location();
            isAllowedContainer = ModConfig.allowedContainers.get().contains(blockId.toString());
        }
        if (!isAllowedContainer) return; // only apply logic if UI was allowed and now closed

        returnUndroppableItems(player, container);
    }

    @SubscribeEvent
    // checks playerticks
    public static void onPlayerTick(PlayerTickEvent event) {
        if (!UDIModState.isModEnabled) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        // obtains modconfig
        AbstractContainerMenu container = player.containerMenu;
        List<? extends String> undroppableItems = ModConfig.undroppableItems.get();
        List<? extends String> allowedContainers = ModConfig.allowedContainers.get(); 

        // checks for undroppable violations
        for (Slot slot : container.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            ResourceLocation regName = stack.getItem().builtInRegistryHolder().key().location();
            if (regName == null || !undroppableItems.contains(regName.toString())) continue;

            // skip check if is in inventory or curios
            if (slot.container == player.getInventory()) continue;

            boolean isInCurios = CuriosApi.getCuriosHelper()
                .findCurios(player, s -> ItemStack.isSameItemSameTags(s, stack))
                .size() > 0;
            if (isInCurios) continue;

            // check if container is valid
            BlockEntity containerEntity = getContainerBlockEntity(player);
            boolean isAllowed = isAllowedContainer(containerEntity);


            // return item to inventory, if moved
            if (!isAllowed) {
            ItemStack copy = stack.copy();
            slot.set(ItemStack.EMPTY);
            //player.getInventory().placeItemBackInInventory(copy);
            boolean success = player.getInventory().add(copy);
            if (!success) player.drop(copy, false);

            // returns message to player
            String msg = ModConfig.cannotDropMessage.get();
            if (msg != null && !msg.isBlank() && MessageRateLimiter.canSendMessage(player.getUUID())) {
            String itemName = stack.getHoverName().getString();
            Component message = Component.literal(msg + " (" + itemName + ")")
            .withStyle(style -> style.withColor(ChatFormatting.RED));
            player.sendSystemMessage(message);
            }
        }
    }

    // if no UI is open and player is not near a container, return undroppable items
        boolean noUIOpen = player.containerMenu == player.inventoryMenu;
        boolean notNearContainer = getContainerBlockEntity(player) == null;
        if (noUIOpen && notNearContainer) {
            returnUndroppableItems(player, player.containerMenu);
        }
    }

    private static void returnUndroppableItems(ServerPlayer player, AbstractContainerMenu container) {
        List<? extends String> undroppableItems = ModConfig.undroppableItems.get();

        for (Slot slot : container.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;

            ResourceLocation regName = stack.getItem().builtInRegistryHolder().key().location();
            if (regName == null || !undroppableItems.contains(regName.toString())) continue;

            // Skip player inventory and Curios
            if (slot.container == player.getInventory()) continue;

            boolean isInCurios = CuriosApi.getCuriosHelper()
                .findCurios(player, s -> ItemStack.isSameItemSameTags(s, stack))
                .size() > 0;
            if (isInCurios) continue;

            ItemStack copy = stack.copy();
            slot.set(ItemStack.EMPTY);
            boolean success = player.getInventory().add(copy);
            if (!success) player.drop(copy, false);

            // returns message to player
            String msg = ModConfig.cannotDropMessage.get();
            if (msg != null && !msg.isBlank() && MessageRateLimiter.canSendMessage(player.getUUID())) {
            String itemName = stack.getHoverName().getString();
            Component message = Component.literal(msg + " (" + itemName + ")")
            .withStyle(style -> style.withColor(ChatFormatting.RED));
            player.sendSystemMessage(message);
            }
        }
    }

    // static method to find a nearby container block entity
    private static BlockEntity getContainerBlockEntity(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        Level level = player.level();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos nearby = pos.offset(dx, dy, dz);
                    BlockEntity entity = level.getBlockEntity(nearby);
                    if (entity != null) {
                        return entity;
                    }
                }
            }
        }
        return null;
    }
    
    private static boolean isAllowedContainer(BlockEntity entity) {
        if (entity == null) return false;
        Block block = entity.getBlockState().getBlock();
        ResourceLocation blockId = block.builtInRegistryHolder().key().location();
        return ModConfig.allowedContainers.get().contains(blockId.toString());
    }
}