package io.github.moulberry.notenoughupdates.util;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent;
import io.github.moulberry.notenoughupdates.overlays.SlayerOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SBInfo {
    private static final SBInfo INSTANCE = new SBInfo();

    public static SBInfo getInstance() {
        return INSTANCE;
    }

    private static final Pattern timePattern = Pattern.compile(".+(am|pm)");

    public IChatComponent footer;
    public IChatComponent header;

    public String location = "";
    public String date = "";
    public String time = "";
    public String objective = "";
    public String slayer = "";

    public String mode = "";

    public Date currentTimeDate = null;

    public String lastOpenContainerName = "";

    private long lastManualLocRaw = -1;
    private long lastLocRaw = -1;
    public long joinedWorld = -1;
    public long unloadedWorld = -1;
    private JsonObject locraw = null;
    public boolean isInDungeon = false;
    public boolean hasNewTab = false;

    public String currentProfile = null;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;

        if (event.gui instanceof GuiChest) {
            GuiChest chest = (GuiChest) event.gui;
            ContainerChest container = (ContainerChest) chest.inventorySlots;
            String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();

            lastOpenContainerName = containerName;
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        lastLocRaw = -1;
        locraw = null;
        this.setLocation(null);
        joinedWorld = System.currentTimeMillis();
        lastOpenContainerName = "";
        hasNewTab = false;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        unloadedWorld = System.currentTimeMillis();
    }

    private static final Pattern JSON_BRACKET_PATTERN = Pattern.compile("^\\{.+}");

    public void onSendChatMessage(String msg) {
        if (msg.trim().startsWith("/locraw") || msg.trim().startsWith("/locraw ")) {
            lastManualLocRaw = System.currentTimeMillis();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public void onChatMessage(ClientChatReceivedEvent event) {
        Matcher matcher = JSON_BRACKET_PATTERN.matcher(event.message.getUnformattedText());
        if (matcher.find()) {
            try {
                JsonObject obj = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(matcher.group(), JsonObject.class);
                if (obj.has("server")) {
                    if (System.currentTimeMillis() - lastManualLocRaw > 5000) event.setCanceled(true);
                    if (obj.has("gametype") && obj.has("mode") && obj.has("map")) {
                        locraw = obj;
                        setLocation(locraw.get("mode").getAsString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getLocation() {
        return mode;
    }

    public void setLocation(String location) {
        if (!Objects.equals(this.mode, location)) {
            MinecraftForge.EVENT_BUS.post(new LocationChangeEvent(location, this.mode));
        }
        this.mode = location;
    }

    private static final String profilePrefix = "\u00a7r\u00a7e\u00a7lProfile: \u00a7r\u00a7a";
    private static final String skillsPrefix = "\u00a7r\u00a7e\u00a7lSkills: \u00a7r\u00a7a";

    private static final Pattern SKILL_LEVEL_PATTERN = Pattern.compile("([^0-9:]+) (\\d{1,2})");

    public void tick() {
        boolean tempIsInDungeon = false;

        long currentTime = System.currentTimeMillis();

        if (Minecraft.getMinecraft().thePlayer != null &&
                Minecraft.getMinecraft().theWorld != null &&
                locraw == null &&
                (currentTime - joinedWorld) > 1000 &&
                (currentTime - lastLocRaw) > 15000) {
            lastLocRaw = System.currentTimeMillis();
            NotEnoughUpdates.INSTANCE.sendChatMessage("/locraw");
        }

        try {
            for (NetworkPlayerInfo info : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
                String name = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(info);
                if (name.startsWith(profilePrefix)) {
                    currentProfile = Utils.cleanColour(name.substring(profilePrefix.length()));
                    hasNewTab = true;
                } else if (name.startsWith(skillsPrefix)) {
                    String levelInfo = name.substring(skillsPrefix.length()).trim();
                    Matcher matcher = SKILL_LEVEL_PATTERN.matcher(Utils.cleanColour(levelInfo).split(":")[0]);
                    if (matcher.find()) {
                        try {
                            int level = Integer.parseInt(matcher.group(2).trim());
                            XPInformation.getInstance().updateLevel(matcher.group(1).toLowerCase().trim(), level);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();

            ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);

            List<Score> scores = new ArrayList<>(scoreboard.getSortedScores(sidebarObjective));

            List<String> lines = new ArrayList<>();
            for (int i = scores.size() - 1; i >= 0; i--) {
                Score score = scores.get(i);
                ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score.getPlayerName());
                String line = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score.getPlayerName());
                line = Utils.cleanDuplicateColourCodes(line);

                String cleanLine = Utils.cleanColour(line);

                if (cleanLine.contains("Dungeon") && cleanLine.contains("Cleared:") && cleanLine.contains("%")) {
                    tempIsInDungeon = true;
                }

                lines.add(line);
            }
            isInDungeon = tempIsInDungeon;

            for (String line : lines) { //Slayer stuff
                //System.out.println(line);
                if (line.contains("Tarantula Broodfather")) {
                    slayer = "Tarantula";
                } else if (line.contains("Revenant Horror")) {
                    slayer = "Revenant";
                } else if (line.contains("Sven Packmaster")) {
                    slayer = "Sven";
                } else if (line.contains("Voidgloom Seraph")) {
                    slayer = "Enderman";
                }
                if (lines.contains("Slayer Quest") && SlayerOverlay.unloadOverlayTimer == -1 ||
                        lines.contains("Slayer Quest") && System.currentTimeMillis() - SlayerOverlay.unloadOverlayTimer > 500) {
                    SlayerOverlay.slayerQuest = true;
                }
                if (SlayerOverlay.slayerQuest) {
                    if (line.contains(" I")) {
                        SlayerOverlay.slayerTier = 1;
                    }
                    if (line.contains(" II")) {
                        SlayerOverlay.slayerTier = 2;
                    }
                    if (line.contains(" III")) {
                        SlayerOverlay.slayerTier = 3;
                    }
                    if (line.contains(" IV")) {
                        SlayerOverlay.slayerTier = 4;
                    }
                    if (line.contains(" V")) {
                        SlayerOverlay.slayerTier = 5;
                    }
                }
            }

            if (lines.size() >= 5) {
                date = Utils.cleanColour(lines.get(1)).trim();
                //§74:40am
                Matcher matcher = timePattern.matcher(lines.get(2));
                if (matcher.find()) {
                    time = Utils.cleanColour(matcher.group()).trim();
                    try {
                        String timeSpace = time.replace("am", " am").replace("pm", " pm");
                        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
                        currentTimeDate = parseFormat.parse(timeSpace);
                    } catch (ParseException ignored) {
                    }
                }
                //Replaced with for loop because in crystal hollows with events the line it's on can shift.
                for (String line : lines) {
                    if (line.contains("⏣")) {
                        location = Utils.cleanColour(line).replaceAll("[^A-Za-z0-9() ]", "").trim();
                        break;
                    }
                }
            }
            objective = null;

            boolean objTextLast = false;
            for (String line : lines) {
                if (objTextLast) {
                    objective = line;
                }

                objTextLast = line.equals("Objective");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
