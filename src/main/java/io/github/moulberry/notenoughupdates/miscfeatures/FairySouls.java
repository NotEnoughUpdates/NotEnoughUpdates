package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.SimpleCommand;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FairySouls {

    private static HashMap<String, Set<Integer>> foundSouls = new HashMap<>();
    private static List<BlockPos> currentSoulList = null;
    private static List<BlockPos> currentSoulListClose = null;

    private static boolean enabled = false;

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        currentSoulList = null;
    }

    public static void load(File file, Gson gson) {
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                HashMap<String, List<Number>> foundSoulsList = gson.fromJson(reader, HashMap.class);

                foundSouls = new HashMap<>();
                for (Map.Entry<String, List<Number>> entry : foundSoulsList.entrySet()) {
                    HashSet<Integer> set = new HashSet<>();
                    for (Number n : entry.getValue()) {
                        set.add(n.intValue());
                    }
                    foundSouls.put(entry.getKey(), set);
                }

                return;
            } catch (Exception ignored) {}
        }
        foundSouls = new HashMap<>();
    }

    public static void save(File file, Gson gson) {
        try {
            file.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(gson.toJson(foundSouls));
            }
        } catch (IOException ignored) {}
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (currentSoulList == null) return;

        if (event.message.getFormattedText().equals("\u00A7r\u00A7dYou have already found that Fairy Soul!\u00A7r") ||
                event.message.getFormattedText().equals("\u00A7d\u00A7lSOUL! \u00A7fYou found a \u00A7r\u00A7dFairy Soul\u00A7r\u00A7f!\u00A7r")) {
            String location = SBInfo.getInstance().getLocation();
            if (location == null) return;

            int closestIndex = -1;
            double closestDistSq = 10 * 10;
            for (int i = 0; i < currentSoulList.size(); i++) {
                BlockPos pos = currentSoulList.get(i);

                double distSq = pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition());

                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestIndex = i;
                }
            }
            if (closestIndex != -1) {
                Set<Integer> found = foundSouls.computeIfAbsent(location, k -> new HashSet<>());
                found.add(closestIndex);
            }
        }
    }

    public static void tick() {
        if (!NotEnoughUpdates.INSTANCE.config.misc.fariySoul) return;

        if (Minecraft.getMinecraft().theWorld == null) {
            currentSoulList = null;
            return;
        }

        JsonObject fairySouls = Constants.FAIRYSOULS;
        if (fairySouls == null) return;

        String location = SBInfo.getInstance().getLocation();
        if (location == null) {
            currentSoulList = null;
            return;
        }

        if (currentSoulList == null) {
            if (fairySouls.has(location) && fairySouls.get(location).isJsonArray()) {
                JsonArray locations = fairySouls.get(location).getAsJsonArray();
                currentSoulList = new ArrayList<>();
                for (int i = 0; i < locations.size(); i++) {
                    try {
                        String coord = locations.get(i).getAsString();

                        String[] split = coord.split(",");
                        if (split.length == 3) {
                            String xS = split[0];
                            String yS = split[1];
                            String zS = split[2];

                            int x = Integer.parseInt(xS);
                            int y = Integer.parseInt(yS);
                            int z = Integer.parseInt(zS);

                            currentSoulList.add(new BlockPos(x, y, z));
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        if (currentSoulList != null && !currentSoulList.isEmpty()) {
            TreeMap<Double, BlockPos> distanceSqMap = new TreeMap<>();

            Set<Integer> found = foundSouls.computeIfAbsent(location, k -> new HashSet<>());

            for (int i = 0; i < currentSoulList.size(); i++) {
                if (found.contains(i)) continue;

                BlockPos pos = currentSoulList.get(i);
                double distSq = pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition());
                distanceSqMap.put(distSq, pos);
            }

            int maxSouls = 15;
            int souls = 0;
            currentSoulListClose = new ArrayList<>();
            for (BlockPos pos : distanceSqMap.values()) {
                currentSoulListClose.add(pos);
                if (++souls >= maxSouls) break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        if (!NotEnoughUpdates.INSTANCE.config.misc.fariySoul) return;

        String location = SBInfo.getInstance().getLocation();
        if (location == null) return;
        if (currentSoulList == null || currentSoulList.isEmpty()) return;

        Set<Integer> found = foundSouls.computeIfAbsent(location, k -> new HashSet<>());

        int rgb = 0xa839ce;
        for (BlockPos currentSoul : currentSoulListClose) {
            RenderUtils.renderBeaconBeamOrBoundingBox(currentSoul, rgb, 1.0f, event.partialTicks);
        }
    }

    public static class FairySoulsCommandAlt extends SimpleCommand {
        public FairySoulsCommandAlt() {
            super("fairysouls", fairysoulRunnable);
        }
    }

    public static class FairySoulsCommand extends SimpleCommand {

        public FairySoulsCommand() {
            super("neusouls", fairysoulRunnable);
        }
    }

    private static final SimpleCommand.ProcessCommandRunnable fairysoulRunnable = new SimpleCommand.ProcessCommandRunnable() {
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length != 1) {
                printHelp();
                return;
            }
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "help":
                    printHelp();
                    return;
                case "on":
                case "enable":
                    print(EnumChatFormatting.DARK_PURPLE + "Enabled fairy soul waypoints");
                    NotEnoughUpdates.INSTANCE.config.misc.fariySoul = true;
                    return;
                case "off":
                case "disable":
                    print(EnumChatFormatting.DARK_PURPLE + "Disabled fairy soul waypoints");
                    NotEnoughUpdates.INSTANCE.config.misc.fariySoul = false;
                    return;
                case "clear": {
                    String location = SBInfo.getInstance().getLocation();
                    if (currentSoulList == null || location == null) {
                        print(EnumChatFormatting.RED + "No fairy souls found in your current world");
                    } else {
                        Set<Integer> found = foundSouls.computeIfAbsent(location, k -> new HashSet<>());
                        for (int i = 0; i < currentSoulList.size(); i++) {
                            found.add(i);
                        }
                        print(EnumChatFormatting.DARK_PURPLE + "Marked all fairy souls as found");
                    }
                }
                return;
                case "unclear":
                    String location = SBInfo.getInstance().getLocation();
                    if (location == null) {
                        print(EnumChatFormatting.RED + "No fairy souls found in your current world");
                    } else {
                        print(EnumChatFormatting.DARK_PURPLE + "Marked all fairy souls as not found");
                        foundSouls.remove(location);
                    }
                    return;
            }

            print(EnumChatFormatting.RED + "Unknown subcommand: " + subcommand);
        }
    };

    private static void print(String s) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(s));
    }

    private static void printHelp() {
        print("");
        print(EnumChatFormatting.DARK_PURPLE.toString() + EnumChatFormatting.BOLD + "     NEU Fairy Soul Waypoint Guide");
        print(EnumChatFormatting.LIGHT_PURPLE + "Shows waypoints for every fairy soul in your world");
        print(EnumChatFormatting.LIGHT_PURPLE + "Clicking a fairy soul automatically removes it from the list");
        if (!NotEnoughUpdates.INSTANCE.config.hidden.dev) {
            print(EnumChatFormatting.DARK_RED + "" + EnumChatFormatting.OBFUSCATED + "Ab" + EnumChatFormatting.RESET + EnumChatFormatting.DARK_RED + "!" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " This feature cannot and will not work in Dungeons. " + EnumChatFormatting.DARK_RED + "!" + EnumChatFormatting.OBFUSCATED + "Ab");
        }
        print(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.BOLD + "     Commands:");
        print(EnumChatFormatting.YELLOW + "/neusouls help          - Display this message");
        print(EnumChatFormatting.YELLOW + "/neusouls on/off        - Enable/disable the waypoint markers");
        print(EnumChatFormatting.YELLOW + "/neusouls clear/unclear - Marks every waypoint in your current world as completed/uncompleted");
        print("");
    }

}
