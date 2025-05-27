package me.neiru1.udi.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import me.neiru1.udi.config.ModConfig;
import me.neiru1.udi.UDI;
import me.neiru1.udi.util.UDIModState;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class UDIMainCmds {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("udi")
                .then(Commands.literal("additem")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        try {
                            return addItem(ctx.getSource());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }))
                .then(Commands.literal("removeitem")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        try {
                            return removeItem(ctx.getSource());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }))
                .then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        try {
                            return reloadConfig(ctx.getSource());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }))
                .then(Commands.literal("version")
                    .executes(ctx -> {
                        try {
                            return showVersion(ctx.getSource());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }))
                .then(Commands.literal("toggle")
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> {
                        UDIModState.isModEnabled = !UDIModState.isModEnabled;
                        ModConfig.modEnabled.set(UDIModState.isModEnabled);
                        ModConfig.COMMON_CONFIG.save();

                        ctx.getSource().sendSystemMessage(Component.literal(
                        "UDI Mod " + (UDIModState.isModEnabled ? "enabled" : "disabled") + "!"
                        ).withStyle(style -> style.withColor(UDIModState.isModEnabled ? TextColor.fromRgb(0x55FF55) : TextColor.fromRgb(0xFF5555))));
                            return 1;
                    }))

        );
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    // Your existing command handlers here, unchanged...
    private static int addItem(CommandSourceStack source) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack heldItem = player.getInventory().getSelected();

        if (heldItem.isEmpty()) {
            player.sendSystemMessage(Component.literal("You must hold an item!").withStyle(style -> style.withColor(TextColor.fromRgb(0xFF5555))));
            return 0;
        }

        String regName = ForgeRegistries.ITEMS.getKey(heldItem.getItem()).toString();
        List<? extends String> currentList = ModConfig.undroppableItems.get();

        if (currentList.contains(regName)) {
            player.sendSystemMessage(Component.literal("Item already in undroppable list!").withStyle(style -> style.withColor(TextColor.fromRgb(0xFFAA00))));
            return 0;
        }

        List<String> newList = new ArrayList<>(currentList);
        newList.add(regName);

        ModConfig.undroppableItems.set(newList);
        ModConfig.COMMON_CONFIG.save();

        player.sendSystemMessage(Component.literal("Item added to undroppable list!").withStyle(style -> style.withColor(TextColor.fromRgb(0x55FF55))));
        return 1;
    }

    private static int removeItem(CommandSourceStack source) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack heldItem = player.getInventory().getSelected();

        if (heldItem.isEmpty()) {
            player.sendSystemMessage(Component.literal("You must hold an item!").withStyle(style -> style.withColor(TextColor.fromRgb(0xFF5555))));
            return 0;
        }

        String regName = ForgeRegistries.ITEMS.getKey(heldItem.getItem()).toString();
        List<? extends String> currentList = ModConfig.undroppableItems.get();

        if (!currentList.contains(regName)) {
            player.sendSystemMessage(Component.literal("Item not in undroppable list!").withStyle(style -> style.withColor(TextColor.fromRgb(0xFFAA00))));
            return 0;
        }

        List<String> newList = new ArrayList<>(currentList);
        newList.remove(regName);

        ModConfig.undroppableItems.set(newList);
        ModConfig.COMMON_CONFIG.save();

        player.sendSystemMessage(Component.literal("Item removed from undroppable list!").withStyle(style -> style.withColor(TextColor.fromRgb(0x55FF55))));
        return 1;
    }

    private static int reloadConfig(CommandSourceStack source) throws Exception {
        UDIModState.isModEnabled = ModConfig.modEnabled.get();
        ServerPlayer player = source.getPlayerOrException();
        player.sendSystemMessage(Component.literal("Config reloaded!").withStyle(style -> style.withColor(TextColor.fromRgb(0xFFAA00))));
        return 1;
    }

    private static int showVersion(CommandSourceStack source) throws Exception {
        ServerPlayer player = source.getPlayerOrException();
        player.sendSystemMessage(Component.literal("UDI Mod Version 1.0.4").withStyle(style -> style.withColor(TextColor.fromRgb(0x55FF55))));
        return 1;
        
    }

}
