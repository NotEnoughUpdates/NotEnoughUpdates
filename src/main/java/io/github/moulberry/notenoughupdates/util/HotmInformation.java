package io.github.moulberry.notenoughupdates.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class HotmInformation {
    private final NotEnoughUpdates neu;
    public static final int[] EXPERIENCE_FOR_HOTM_LEVEL = {
            // Taken from the wiki: https://hypixel-skyblock.fandom.com/wiki/Heart_of_the_Mountain#Experience_for_Each_Tier
            0, 3000, 12000, 37000, 97000, 197000, 347000
    };
    private final Map<String, Tree> profiles = new ConcurrentHashMap<>();

    public static class Tree {
        private Map<String, Integer> levels = new HashMap<>();
        private int totalMithrilPowder;
        private int totalGemstonePowder;
        private int hotmExp;

        public int getHotmExp() {
            return hotmExp;
        }

        public int getTotalGemstonePowder() {
            return totalGemstonePowder;
        }

        public int getTotalMithrilPowder() {
            return totalMithrilPowder;
        }

        public Set<String> getAllUnlockedNodes() {
            return levels.keySet();
        }

        public int getHotmLevel() {
            for (int i = EXPERIENCE_FOR_HOTM_LEVEL.length - 1; i >= 0; i--) {
                if (EXPERIENCE_FOR_HOTM_LEVEL[i] >= this.hotmExp)
                    return i;
            }
            return 0;
        }

        public int getLevel(String node) {
            return levels.getOrDefault(node, 0);
        }

    }

    private CompletableFuture<Void> updateTask = CompletableFuture.completedFuture(null);

    public HotmInformation(NotEnoughUpdates neu) {
        this.neu = neu;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public Optional<Tree> getInformationOn(String profile) {
        return Optional.ofNullable(this.profiles.get(profile));
    }

    public Optional<Tree> getInformationOnCurrentProfile() {
        return getInformationOn(neu.manager.getCurrentProfile());
    }

    @SubscribeEvent
    public void onWorldLoad(ClientChatReceivedEvent event) {
        if (event.message.getUnformattedText().equals("Welcome to Hypixel SkyBlock!"))
            requestUpdate(false);
    }

    public synchronized void requestUpdate(boolean force) {
        if (updateTask.isDone() || force) {
            updateTask = neu.manager.hypixelApi.getHypixelApiAsync(neu.config.apiKey.apiKey, "skyblock/profiles", new HashMap<String, String>() {{
                put("uuid", Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""));
            }}).thenAccept(this::updateInformation);
        }
    }

    public void updateInformation(JsonObject entireApiResponse) {
        if (!entireApiResponse.has("success") || !entireApiResponse.get("success").getAsBoolean()) return;
        JsonArray profiles = entireApiResponse.getAsJsonArray("profiles");
        for (JsonElement element : profiles) {
            JsonObject profile = element.getAsJsonObject();
            String profileName = profile.get("cute_name").getAsString();
            JsonObject player = profile.getAsJsonObject("members").getAsJsonObject(Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""));
            if (!player.has("mining_core"))
                continue;
            JsonObject miningCore = player.getAsJsonObject("mining_core");
            Tree tree = new Tree();
            JsonObject nodes = miningCore.getAsJsonObject("nodes");
            for (Map.Entry<String, JsonElement> node : nodes.entrySet()) {
                tree.levels.put(node.getKey(), node.getValue().getAsInt());
            }
            if (miningCore.has("powder_mithril_total")) {
                tree.totalMithrilPowder = miningCore.get("powder_mithril_total").getAsInt();
            }
            if (miningCore.has("powder_gemstone_total")) {
                tree.totalGemstonePowder = miningCore.get("powder_gemstone_total").getAsInt();
            }
            if (miningCore.has("experience")) {
                tree.hotmExp = miningCore.get("experience").getAsInt();
            }
            this.profiles.put(profileName, tree);
        }
    }

}
