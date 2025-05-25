package me.neiru1.udi.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ModConfig {
    public static final ModConfig INSTANCE = new ModConfig();
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> undroppableItems;
    public static ForgeConfigSpec.ConfigValue<String> cannotDropMessage;

    static {
        BUILDER.push("general");

        undroppableItems = BUILDER.comment("List of item registry names that cannot be dropped")
            .defineList("undroppableItems", List.of("minecraft:diamond", "minecraft:nether_star"), o -> o instanceof String);

        cannotDropMessage = BUILDER.comment("Message shown when player tries to drop undroppable items")
            .define("cannotDropMessage", "You cannot drop or move this item away!");

        BUILDER.pop();

        COMMON_CONFIG = BUILDER.build();
    }
}