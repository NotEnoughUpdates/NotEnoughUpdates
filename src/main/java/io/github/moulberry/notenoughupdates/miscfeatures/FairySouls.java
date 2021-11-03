package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class FairySouls {

    private static final String unknownProfile = "unknown";
    private static List<BlockPos> currentSoulList = null;
    private static List<BlockPos> currentSoulListClose = null;
    private static HashMap<String, HashMap<String, Set<Integer>>> loadedFoundSouls = new HashMap<>();

    private static boolean enabled = false;
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
                    enabled = true;
                    return;
                case "off":
                case "disable":
                    print(EnumChatFormatting.DARK_PURPLE + "Disabled fairy soul waypoints");
                    enabled = false;
                    return;
                case "clear": {
                    String location = SBInfo.getInstance().getLocation();
                    if (currentSoulList == null || location == null) {
                        print(EnumChatFormatting.RED + "No fairy souls found in your current world");
                    } else {
                        HashMap<String, Set<Integer>> foundSouls = getFoundSoulsForProfile();
                        Set<Integer> found = foundSouls.computeIfAbsent(location, k -> new HashSet<>());
                        for (int i = 0; i < currentSoulList.size(); i++) {
                            found.add(i);
                        }
                        String profileName = SBInfo.getInstance().currentProfile;
                        if (profileName == null) {
                            if (loadedFoundSouls.containsKey(unknownProfile)) {
                                loadedFoundSouls.get(unknownProfile);
                            }
                        } else {
                            loadedFoundSouls.get(profileName).remove(location);
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
                        String profileName = SBInfo.getInstance().currentProfile;
                        if (profileName == null) {
                            if (loadedFoundSouls.containsKey(unknownProfile)) {
                                loadedFoundSouls.get(unknownProfile).remove(location);
                            }
                        } else {
                            loadedFoundSouls.get(profileName).remove(location);
                        }
                        print(EnumChatFormatting.DARK_PURPLE + "Marked all fairy souls as not found");
                    }
                    return;
            }

            print(EnumChatFormatting.RED + "Unknown subcommand: " + subcommand);
        }
    };

    private static HashMap<String, Set<Integer>> getFoundSoulsForProfile() {
        String profile = SBInfo.getInstance().currentProfile;
        if (profile == null) {
            if (loadedFoundSouls.containsKey(unknownProfile))
                return loadedFoundSouls.get(unknownProfile);
        } else {
            profile = profile.toLowerCase(Locale.getDefault());
            if (loadedFoundSouls.containsKey(unknownProfile)) {
                HashMap<String, Set<Integer>> unknownProfileData = loadedFoundSouls.remove(unknownProfile);
                loadedFoundSouls.put(profile, unknownProfileData);
                return unknownProfileData;
            }
            if (loadedFoundSouls.containsKey(profile)) {
                return loadedFoundSouls.get(profile);
            } else {
                //create a new entry for this profile
                HashMap<String, Set<Integer>> profileData = new HashMap<>();
                loadedFoundSouls.put(profile, profileData);
                return profileData;
            }
        }
        return new HashMap<>();
    }

    public static void load(File file, Gson gson) {
        HashMap<String, HashMap<String, Set<Integer>>> foundSoulsList;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            loadedFoundSouls = new HashMap<>();
            return;
        }
        String fileContent = reader
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));

        if (file.exists()) {
            try {
                //noinspection UnstableApiUsage
                Type type = new TypeToken<HashMap<String, HashMap<String, Set<Integer>>>>() {
                }.getType();

                foundSoulsList = gson.fromJson(fileContent, type);
                for (Map.Entry<String, HashMap<String, Set<Integer>>> soulsJson : foundSoulsList.entrySet()) {
                    String profileName = soulsJson.getKey();
                    for (Map.Entry<String, Set<Integer>> souls : soulsJson.getValue().entrySet()) {
                        HashMap<String, Set<Integer>> map = new HashMap<>();
                        map.put(souls.getKey(), souls.getValue());
                        loadedFoundSouls.put(profileName, map);
                    }
                }
                return;
            } catch (JsonSyntaxException e) {
                //The file is in the old format, convert it to the new one and set the profile to unknown
                try {
                    //noinspection UnstableApiUsage
                    Type type = new TypeToken<HashMap<String, Set<Integer>>>() {
                    }.getType();

                    HashMap<String, Set<Number>> foundSoulsListOldFormat = gson.fromJson(fileContent, type);
                    HashMap<String, Set<Integer>> map = new HashMap<>();
                    for (Map.Entry<String, Set<Number>> entry : foundSoulsListOldFormat.entrySet()) {
                        HashSet<Integer> set = new HashSet<>();
                        for (Number n : entry.getValue()) {
                            set.add(n.intValue());
                        }
                        map.put(entry.getKey(), set);
                    }
                    loadedFoundSouls.put(unknownProfile, map);
                    return;
                } catch (JsonSyntaxException e2) {
                    System.err.println("Can't read file containing collected fairy souls, resetting.");
                    loadedFoundSouls = new HashMap<>();
                    return;
                }
            }
        }
        loadedFoundSouls = new HashMap<>();
    }

    public static void save(File file, Gson gson) {
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(gson.toJson(loadedFoundSouls));
            }
        } catch (IOException ignored) {
        }
    }

    public static void tick() {
        if (!enabled) return;

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

            HashMap<String, Set<Integer>> foundSouls = getFoundSoulsForProfile();

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

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        currentSoulList = null;
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
                HashMap<String, Set<Integer>> foundSouls = getFoundSoulsForProfile();
                Set<Integer> found = foundSouls.computeIfAbsent(location, k -> new HashSet<>());
                found.add(closestIndex);
            }
        }
    }

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        if (!enabled) return;

        String location = SBInfo.getInstance().getLocation();
        if (location == null) return;
        if (currentSoulList == null || currentSoulList.isEmpty()) return;


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

}
