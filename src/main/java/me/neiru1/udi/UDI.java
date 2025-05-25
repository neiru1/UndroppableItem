package me.neiru1.udi;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.slf4j.Logger;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.event.RegisterCommandsEvent;


import me.neiru1.udi.commands.UDIMainCmds;
import me.neiru1.udi.listeners.PlayerDeath;
import me.neiru1.udi.listeners.PlayerDropItem;
import me.neiru1.udi.config.ModConfig;

@Mod("udi")
public class UDI {
    public static final String MODID = "udi";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String version = "1.0.1"; 
    

    public UDI() {
        // config
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_CONFIG);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // event listeners
        MinecraftForge.EVENT_BUS.register(new PlayerDropItem());
        MinecraftForge.EVENT_BUS.register(new PlayerDeath());

        // commands 
        FMLJavaModLoadingContext.get().getModEventBus().register(new UDIMainCmds());
        MinecraftForge.EVENT_BUS.register(new UDIMainCmds());
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("[UDI] Setup complete. Version: {}", version);
    }

    public static ModConfig getConfig() {
        return ModConfig.INSTANCE;
    }

    public String getVersion() {
        return version;
    }

}
