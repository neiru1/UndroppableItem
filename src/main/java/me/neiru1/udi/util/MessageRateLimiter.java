// Compatible with Minecraft 1.20.1 and Forge 47.4.0

package me.neiru1.udi.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "udi", bus = Mod.EventBusSubscriber.Bus.FORGE)

public class MessageRateLimiter {
    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000; // 2 seconds

    public static boolean canSendMessage(UUID playerId) {
        long now = System.currentTimeMillis();
        long last = lastMessageTime.getOrDefault(playerId, 0L);
        if (now - last >= COOLDOWN_MS) {
            lastMessageTime.put(playerId, now);
            return true;
        }
        return false;
    }
}
