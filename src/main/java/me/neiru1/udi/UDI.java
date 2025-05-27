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
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.TickEvent;

import me.neiru1.udi.commands.UDIMainCmds;
import me.neiru1.udi.listeners.PlayerDeath;
import me.neiru1.udi.listeners.PlayerDropItem;
import me.neiru1.udi.config.ModConfig;
import me.neiru1.udi.tracking.Fallback;
import me.neiru1.udi.util.MessageRateLimiter;
import me.neiru1.udi.util.UDIModState;

@Mod("udi")


public class UDI {
    public static final String MODID = "udi";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String version = "1.0.4";
    public static MinecraftServer serverInstance;
    

    public UDI() {
        // config
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_CONFIG);
        // lifecycle event listener
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // event listeners
        MinecraftForge.EVENT_BUS.register(new PlayerDropItem());
        MinecraftForge.EVENT_BUS.register(new PlayerDeath());
        MinecraftForge.EVENT_BUS.register(this);

        // registers the event bus
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);

        // commands
        FMLJavaModLoadingContext.get().getModEventBus().register(new UDIMainCmds());
        MinecraftForge.EVENT_BUS.register(UDIMainCmds.class);

        // util
        MinecraftForge.EVENT_BUS.register(new UDIModState());
        MinecraftForge.EVENT_BUS.register(new MessageRateLimiter());

    }

    @SubscribeEvent
        public void onServerStarted(ServerStartedEvent event) {
    serverInstance = event.getServer();
    System.out.println("[UDI] Server instance cached");
}


    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("[UDI] Setup complete. Version: ", version);
    }

    public static ModConfig getConfig() {
        return ModConfig.INSTANCE;
    }

    public String getVersion() {
        return version;
    }

}

    