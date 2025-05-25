package me.neiru1.udi;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.neiru1.UDI.commands.UDIMainCmds;
import me.neiru1.UDI.listeners.PlayerDeath;
import me.neiru1.UDI.listeners.PlayerDropItem;
import me.neiru1.UDI.listeners.SendUpdates;

@Mod(UDI.MODID)
public class UDI {
    public static final String MODID = "udi";
    private static final Logger LOGGER = LogUtils.getLogger();
    private String latestVersion;
    private final String version = "1.0.0"; 
    

    public UDI() {
        // config
         ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfig.COMMON_CONFIG, "undroppableitems.toml");



        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // event listeners
        MinecraftForge.EVENT_BUS.register(new PlayerDropItem());
        MinecraftForge.EVENT_BUS.register(new PlayerDeath());
        MinecraftForge.EVENT_BUS.register(new SendUpdates());

        // commands
        FMLJavaModLoadingContext.get().getModEventBus().addListener(UDIMainCmds::registerCommands);

        // updates
        checkUpdates();
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("[UnDroppableItems] Setup complete. Version: {}", version);
    }

    private void checkUpdates() {
        new Thread(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(
                        "https://api.spigotmc.org/legacy/update.php?resource=92280")
                        .openConnection();
                int timeout = 1250;
                con.setConnectTimeout(timeout);
                con.setReadTimeout(timeout);
                latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();

                if (latestVersion.length() <= 7 && !version.equals(latestVersion)) {
                    LOGGER.warn("[UnDroppableItems] There is a new version available: {}", latestVersion);
                    LOGGER.warn("[UnDroppableItems] You can download it at: https://www.spigotmc.org/resources/undroppableitems-make-the-items-you-want-undroppable.92280/");
                }
            } catch (Exception ex) {
                LOGGER.error("[UnDroppableItems] Error while checking update.", ex);
            }
        }).start();
    }

    public static ModConfig getConfig() {
        return ModConfig.INSTANCE;
    }

    public String getVersion() {
        return version;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}