package me.neiru1.udi.tracking;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.event.server.ServerStartedEvent;



import me.neiru1.udi.config.ModConfig;
import me.neiru1.udi.UDI;

import java.util.*;

@Mod.EventBusSubscriber(modid = "udi", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Fallback {

    

@SubscribeEvent
    public static void onPlayerTickAssignOwnership(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        for (ItemStack stack : player.getInventory().items) {
        if (stack.isEmpty()) continue;

        ResourceLocation regName = stack.getItem().builtInRegistryHolder().key().location();
        if (regName == null) continue;

        if (!ModConfig.undroppableItems.get().contains(regName.toString())) continue;

        // assign ownership only if not already tagged using uuid
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("udi:Owner")) {
            tag.putUUID("udi:Owner", player.getUUID());
            stack.setTag(tag);
            System.out.println("Bound item " + regName + " to " + player.getName().getString());
        }

        // begin tracking if not already
        Fallback.trackItem(player, stack);
    }
}

@SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
           MinecraftServer server = UDI.serverInstance;
        if (server != null) {
           enforceOwnership(server);
        }
    }

}

    private static final Map<UUID, TrackedItem> trackedItems = new HashMap<>();

    public static void trackItem(ServerPlayer player, ItemStack stack) {
        UUID playerId = player.getUUID();
        TrackedItem tracked = new TrackedItem(stack.copy(), System.currentTimeMillis());
        trackedItems.put(playerId, tracked);
    }

    public static void tagItemWithOwner(ItemStack stack, UUID owner) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("udi:Owner")) {
            tag.putUUID("udi:Owner", owner);
        }
    }

    public static boolean isItemOwnedBy(ItemStack stack, UUID playerId) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID("udi:Owner") && tag.getUUID("udi:Owner").equals(playerId);
    }

    public static void enforceOwnership(MinecraftServer server) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, TrackedItem>> iterator = trackedItems.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, TrackedItem> entry = iterator.next();
            UUID playerId = entry.getKey();
            TrackedItem tracked = entry.getValue();

            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                if (now - tracked.lastSeenTime > 5000) {
                    iterator.remove();
                }
                continue;
            }

            Inventory inventory = player.getInventory();
            boolean found = false;

            for (ItemStack stack : inventory.items) {
                if (ItemStack.isSameItemSameTags(stack, tracked.stack)) {
                    found = true;
                    break;
                }
            }

            if (!found) { // 5 seconds fallback rule. drops/removes item if is on invalid owner
                if (now - tracked.lastSeenTime > 5000) {
                    boolean success = inventory.add(tracked.stack.copy());
                    if (!success) player.drop(tracked.stack.copy(), false);
                    iterator.remove();
                }
            } else {
                tracked.lastSeenTime = now;
            }
        }
    }

    public static void enforceOwnershipPerPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty()) continue;

            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("udi:Owner")) {
                UUID ownerId = tag.getUUID("udi:Owner");
                if (!ownerId.equals(playerId)) {
                    player.getInventory().items.set(i, ItemStack.EMPTY);
                    ServerPlayer rightfulOwner = player.server.getPlayerList().getPlayer(ownerId);
                    if (rightfulOwner != null) {
                        boolean success = rightfulOwner.getInventory().add(stack.copy());
                        if (!success) rightfulOwner.drop(stack.copy(), false);
                    }
                }
            }
        }
    }

    private static class TrackedItem {
        public final ItemStack stack;
        public long lastSeenTime;

        public TrackedItem(ItemStack stack, long lastSeenTime) {
            this.stack = stack;
            this.lastSeenTime = lastSeenTime;
        }
    }
}
