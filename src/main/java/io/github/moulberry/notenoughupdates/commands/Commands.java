package io.github.moulberry.notenoughupdates.commands;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUEventListener;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.auction.CustomAHGui;
import io.github.moulberry.notenoughupdates.collectionlog.GuiCollectionLog;
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.core.config.GuiPositionEditor;
import io.github.moulberry.notenoughupdates.core.util.MiscUtils;
import io.github.moulberry.notenoughupdates.cosmetics.GuiCosmetics;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import io.github.moulberry.notenoughupdates.dungeons.GuiDungeonMapEditor;
import io.github.moulberry.notenoughupdates.gamemodes.GuiGamemodes;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBiomes;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.SpecialBlockZone;
import io.github.moulberry.notenoughupdates.miscfeatures.FairySouls;
import io.github.moulberry.notenoughupdates.miscfeatures.FancyPortals;
import io.github.moulberry.notenoughupdates.miscfeatures.FishingHelper;
import io.github.moulberry.notenoughupdates.miscfeatures.NullzeeSphere;
import io.github.moulberry.notenoughupdates.miscgui.*;
import io.github.moulberry.notenoughupdates.miscgui.tutorials.NeuTutorial;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.options.NEUConfigEditor;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.profileviewer.PlayerStats;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.Color;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Commands {
    public Commands() {
        //ClientCommandHandler.instance.registerCommand(collectionLogCommand);
        //ClientCommandHandler.instance.registerCommand(collectionLogCommand2);
        ClientCommandHandler.instance.registerCommand(nullzeeSphereCommand);
        ClientCommandHandler.instance.registerCommand(cosmeticsCommand);
        ClientCommandHandler.instance.registerCommand(linksCommand);
        //ClientCommandHandler.instance.registerCommand(gamemodesCommand);
        ClientCommandHandler.instance.registerCommand(stWhyCommand);
        ClientCommandHandler.instance.registerCommand(buttonsCommand);
        ClientCommandHandler.instance.registerCommand(resetRepoCommand);
        ClientCommandHandler.instance.registerCommand(reloadRepoCommand);
        //ClientCommandHandler.instance.registerCommand(itemRenameCommand);
        ClientCommandHandler.instance.registerCommand(joinDungeonCommand);
        ClientCommandHandler.instance.registerCommand(viewProfileCommand);
        ClientCommandHandler.instance.registerCommand(viewProfileShortCommand);
        ClientCommandHandler.instance.registerCommand(dhCommand);
        ClientCommandHandler.instance.registerCommand(dnCommand);
        ClientCommandHandler.instance.registerCommand(customizeCommand);
        ClientCommandHandler.instance.registerCommand(customizeCommand2);
        ClientCommandHandler.instance.registerCommand(devTestCommand);
        ClientCommandHandler.instance.registerCommand(packDevCommand);
        if (!Loader.isModLoaded("skyblockextras")) ClientCommandHandler.instance.registerCommand(viewCataCommand);
        ClientCommandHandler.instance.registerCommand(peekCommand);
        //ClientCommandHandler.instance.registerCommand(tutorialCommand);
        ClientCommandHandler.instance.registerCommand(overlayPlacementsCommand);
        ClientCommandHandler.instance.registerCommand(enchantColourCommand);
        ClientCommandHandler.instance.registerCommand(neuAhCommand);
        ClientCommandHandler.instance.registerCommand(new StatsCommand());
        ClientCommandHandler.instance.registerCommand(neumapCommand);
        ClientCommandHandler.instance.registerCommand(settingsCommand);
        ClientCommandHandler.instance.registerCommand(settingsCommand2);
        ClientCommandHandler.instance.registerCommand(settingsCommand3);
        ClientCommandHandler.instance.registerCommand(dungeonWinTest);
        ClientCommandHandler.instance.registerCommand(calendarCommand);
        ClientCommandHandler.instance.registerCommand(new FairySouls.FairySoulsCommand());
        ClientCommandHandler.instance.registerCommand(new FairySouls.FairySoulsCommandAlt());
        ClientCommandHandler.instance.registerCommand(neuHelp);
        ClientCommandHandler.instance.registerCommand(neuFeatures);
        ClientCommandHandler.instance.registerCommand(neuRepoMode);
    }


    SimpleCommand.ProcessCommandRunnable collectionLogRun = new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            NotEnoughUpdates.INSTANCE.openGui = new GuiCollectionLog();
        }
    };

    SimpleCommand collectionLogCommand = new SimpleCommand("neucl", collectionLogRun);
    SimpleCommand collectionLogCommand2 = new SimpleCommand("collectionlog", collectionLogRun);

    SimpleCommand nullzeeSphereCommand = new SimpleCommand("neuzeesphere", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length != 1) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: /neuzeesphere [on/off] or /neuzeesphere (radius) or /neuzeesphere setCenter"));
                return;
            }
            if (args[0].equalsIgnoreCase("on")) {
                NullzeeSphere.enabled = true;
            } else if (args[0].equalsIgnoreCase("off")) {
                NullzeeSphere.enabled = false;
            } else if (args[0].equalsIgnoreCase("setCenter")) {
                EntityPlayerSP p = ((EntityPlayerSP) sender);
                NullzeeSphere.centerPos = new BlockPos(p.posX, p.posY, p.posZ);
                NullzeeSphere.overlayVBO = null;
            } else {
                try {
                    float radius = Float.parseFloat(args[0]);
                    NullzeeSphere.size = radius;
                    NullzeeSphere.overlayVBO = null;
                } catch (Exception e) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Can't parse radius: " + args[0]));
                }
            }
        }
    });

    /*SimpleCommand itemRenameCommand = new SimpleCommand("neurename", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 0) {
                args = new String[]{"help"};
            }
            String heldUUID = NotEnoughUpdates.INSTANCE.manager.getUUIDForItem(Minecraft.getMinecraft().thePlayer.getHeldItem());
            switch (args[0].toLowerCase()) {
                case "clearall":
                    NotEnoughUpdates.INSTANCE.manager.itemRenameJson = new JsonObject();
                    NotEnoughUpdates.INSTANCE.manager.saveItemRenameConfig();
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEU] Cleared custom name for all items"));
                    break;
                case "clear":
                    if (heldUUID == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Can't clear rename - no UUID"));
                        return;
                    }
                    NotEnoughUpdates.INSTANCE.manager.itemRenameJson.remove(heldUUID);
                    NotEnoughUpdates.INSTANCE.manager.saveItemRenameConfig();
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEU] Cleared custom name for held item"));
                    break;
                case "copyuuid":
                    if (heldUUID == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Can't clear rename - no UUID"));
                        return;
                    }
                    StringSelection selection = new StringSelection(heldUUID);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEU] UUID copied to clipboard"));
                    break;
                case "uuid":
                    if (heldUUID == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Can't get UUID - no UUID"));
                        return;
                    }
                    ChatStyle style = new ChatStyle();
                    style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText(EnumChatFormatting.GRAY + "Click to copy to clipboard")));
                    style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neurename copyuuid"));

                    ChatComponentText text = new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] The UUID of your currently held item is: " +
                            EnumChatFormatting.GREEN + heldUUID);
                    text.setChatStyle(style);
                    sender.addChatMessage(text);
                    break;
                case "set":
                    if (heldUUID == null) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Can't rename item - no UUID"));
                        return;
                    }
                    if (args.length == 1) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Usage: /neurename set [name...]"));
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        sb.append(args[i]);
                        if (i < args.length - 1) sb.append(" ");
                    }
                    String name = sb.toString()
                            .replace("\\&", "{amp}")
                            .replace("&", "\u00a7")
                            .replace("{amp}", "&");
                    name = new UnicodeUnescaper().translate(name);
                    NotEnoughUpdates.INSTANCE.manager.itemRenameJson.addProperty(heldUUID, name);
                    NotEnoughUpdates.INSTANCE.manager.saveItemRenameConfig();
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[NEU] Set custom name for held item"));
                    break;
                default:
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Unknown subcommand \"" + args[0] + "\""));
                case "help":
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Available commands:"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "help: Print this message"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "clearall: Clears all custom names "
                            + EnumChatFormatting.BOLD + "(Cannot be undone)"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "clear: Clears held item name "
                            + EnumChatFormatting.BOLD + "(Cannot be undone)"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "uuid: Returns the UUID of the currently held item"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "set: Sets the custom name of the currently held item"));
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Usage: /neurename set [name...]"));

            }
        }
    });*/

    SimpleCommand neuHelp = new SimpleCommand("neuhelp", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            ArrayList<String> neuHelpMessages = Lists.newArrayList(
                    "\u00a75\u00a7lNotEnoughUpdates commands",
                    "\u00a76/neu \u00a77- Opens the main neu GUI.",
                    "\u00a76/pv \u00a7b?{name} \u00a72\u2D35 \u00a7r\u00a77- Opens the profile viewer",
                    "\u00a76/neusouls {on/off/clear/unclear} \u00a7r\u00a77- Shows waypoints to fairy souls.",
                    "\u00a76/neubuttons \u00a7r\u00a77- Opens a GUI which allows you to customize inventory buttons.",
                    "\u00a76/neuec \u00a7r\u00a77- Opens the enchant colour GUI.",

                    "\u00a76/join {floor} \u00a7r\u00a77- Short Command to join a Dungeon. \u00a7lNeed a Party of 5 People\u00a7r\u00a77 {4/f7/m5}.",
                    "\u00a76/neucosmetics \u00a7r\u00a77- Opens the cosmetic GUI.",
                    "\u00a76/neurename \u00a7r\u00a77- Opens the NEU Item Customizer.",
                    "\u00a76/cata \u00a7b?{name} \u00a72\u2D35 \u00a7r\u00a77- Opens the profile viewer's catacombs page.",
                    "\u00a76/neulinks \u00a7r\u00a77- Shows links to neu/moulberry.",
                    "\u00a76/neuoverlay \u00a7r\u00a77- Opens GUI Editor for quickcommands and searchbar.",
                    "\u00a76/neuah \u00a7r\u00a77- Opens neu's custom ah GUI.",
                    "\u00a76/neumap \u00a7r\u00a77- Opens the dungeon map GUI.",
                    "\u00a76/neucalendar \u00a7r\u00a77- Opens neu's custom calendar GUI.",
                    "",
                    "\u00a76\u00a7lOld commands:",
                    "\u00a76/peek \u00a7b?{user} \u00a72\u2D35 \u00a7r\u00a77- Shows quickly stats for a user.",
                    "",
                    "\u00a76\u00a7lDebug commands:",
                    "\u00a76/neustats \u00a7r\u00a77- Copies helpful info to the clipboard.",
                    "\u00a76/neustats modlist \u00a7r\u00a77- Copies modlist info to clipboard.",
                    "\u00a76/neuresetrepo \u00a7r\u00a77- Deletes all repo files.",
                    "\u00a76/neureloadrepo \u00a7r\u00a77- Debug command with repo.",
                    "",
                    "\u00a76\u00a7lDev commands:",
                    "\u00a76/neupackdev \u00a7r\u00a77- pack creator command - getnpc");
            for (String neuHelpMessage : neuHelpMessages) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(neuHelpMessage));
            }
            if (NotEnoughUpdates.INSTANCE.config.hidden.dev) {
                ArrayList<String> neuDevHelpMessages = Lists.newArrayList(
                        "\u00a76/neudevtest \u00a7r\u00a77- dev test command",
                        "\u00a76/neuzeephere \u00a7r\u00a77- sphere",
                        "\u00a76/neudungeonwintest \u00a7r\u00a77- displays the dungeon win screen");

                for (String neuDevHelpMessage : neuDevHelpMessages) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(neuDevHelpMessage));
                }
            }
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a77Commands marked with a \u00a72\"\u2D35\"\u00a77 require are api key. You can set your api key via \"/api new\" or by manually putting it in the api field in \"/neu\""));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a77Arguments marked with a \u00a7b\"?\"\u00a77 are optional."));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a76\u00a7lScroll up to see everything"));
        }
    });

    SimpleCommand neuFeatures = new SimpleCommand("neufeatures", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
            if (Constants.MISC == null || !Constants.MISC.has("featureslist")) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "WARNING: " + EnumChatFormatting.RESET + EnumChatFormatting.RED + "Could not load URL from repo."));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + "Please run " + EnumChatFormatting.BOLD + "/neuresetrepo" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " and " + EnumChatFormatting.BOLD + "restart your game" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " in order to fix. " + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "If that doesn't fix it" + EnumChatFormatting.RESET + EnumChatFormatting.RED + ", please join discord.gg/moulberry and post in #neu-support"));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                return;
            }
            String url = Constants.MISC.get("featureslist").getAsString();

            Desktop desk = Desktop.getDesktop();
            try {
                desk.browse(new URI(url));
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "NEU" + EnumChatFormatting.RESET + EnumChatFormatting.GOLD + "> Opening Feature List in browser."));
            } catch (URISyntaxException | IOException ignored) {

                ChatComponentText clickTextFeatures = new ChatComponentText(
                        EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "NEU" + EnumChatFormatting.RESET + EnumChatFormatting.GOLD + "> Click here to open the Feature List in your browser.");
                clickTextFeatures.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, url));
                Minecraft.getMinecraft().thePlayer.addChatMessage(clickTextFeatures);

            }
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));

        }
    });

    SimpleCommand stWhyCommand = new SimpleCommand("neustwhy", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            NEUEventListener.displayNotification(Lists.newArrayList(
                    "\u00a7eStorage Viewer",
                    "\u00a77Currently, the storage viewer requires you to click twice",
                    "\u00a77in order to switch between pages. This is because Hypixel",
                    "\u00a77has not yet added a shortcut command to go to any enderchest/",
                    "\u00a77storage page.",
                    "\u00a77While it is possible to send the second click",
                    "\u00a77automatically, doing so violates Hypixel's new mod rules."), true);
        }
    });

    SimpleCommand gamemodesCommand = new SimpleCommand("neugamemodes", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            boolean upgradeOverride = args.length == 1 && args[0].equals("upgradeOverride");
            NotEnoughUpdates.INSTANCE.openGui = new GuiGamemodes(upgradeOverride);
        }
    });

    SimpleCommand buttonsCommand = new SimpleCommand("neubuttons", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            NotEnoughUpdates.INSTANCE.openGui = new GuiInvButtonEditor();
        }
    });

    SimpleCommand enchantColourCommand = new SimpleCommand("neuec", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            NotEnoughUpdates.INSTANCE.openGui = new GuiEnchantColour();
        }
    });

    SimpleCommand resetRepoCommand = new SimpleCommand("neuresetrepo", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            NotEnoughUpdates.INSTANCE.manager.resetRepo();
        }
    });

    SimpleCommand neuRepoMode = new SimpleCommand("neurepomode", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            NotEnoughUpdates.INSTANCE.config.hidden.dev = !NotEnoughUpdates.INSTANCE.config.hidden.dev;
            NotEnoughUpdates.INSTANCE.config.hidden.enableItemEditing = !NotEnoughUpdates.INSTANCE.config.hidden.enableItemEditing;
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("\u00a75Toggled NEU repo dev mode."));
        }
    });

    SimpleCommand dungeonWinTest = new SimpleCommand("neudungeonwintest", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length > 0) {
                DungeonWin.TEAM_SCORE = new ResourceLocation("notenoughupdates:dungeon_win/" + args[0].toLowerCase() + ".png");
            }

            DungeonWin.displayWin();
        }
    });

    SimpleCommand reloadRepoCommand = new SimpleCommand("neureloadrepo", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            File items = new File(NotEnoughUpdates.INSTANCE.manager.repoLocation, "items");
            if (items.exists()) {
                File[] itemFiles = new File(NotEnoughUpdates.INSTANCE.manager.repoLocation, "items").listFiles();
                if (itemFiles != null) {
                    for (File f : itemFiles) {
                        String internalname = f.getName().substring(0, f.getName().length() - 5);
                        NotEnoughUpdates.INSTANCE.manager.loadItem(internalname);
                    }
                }
            }
            Constants.reload();

            NotEnoughUpdates.INSTANCE.newConfigFile();
            if (NotEnoughUpdates.INSTANCE.getConfigFile().exists()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(NotEnoughUpdates.INSTANCE.getConfigFile()), StandardCharsets.UTF_8))) {
                    NotEnoughUpdates.INSTANCE.config = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, NEUConfig.class);
                } catch (Exception ignored) {
                }
            }
        }
    });

    ScheduledExecutorService peekCommandExecutorService = null;

    SimpleCommand peekCommand = new SimpleCommand("peek", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            String name;
            if (args.length == 0) {
                name = Minecraft.getMinecraft().thePlayer.getName();
            } else {
                name = args[0];
            }
            int id = new Random().nextInt(Integer.MAX_VALUE / 2) + Integer.MAX_VALUE / 2;

            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(
                    EnumChatFormatting.YELLOW + "[PEEK] Getting player information..."), id);
            NotEnoughUpdates.profileViewer.getProfileByName(name, profile -> {
                if (profile == null) {
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(
                            EnumChatFormatting.RED + "[PEEK] Unknown player or api is down."), id);
                } else {
                    profile.resetCache();

                    if (peekCommandExecutorService == null || peekCommandExecutorService.isShutdown()) {
                        peekCommandExecutorService = Executors.newSingleThreadScheduledExecutor();
                    } else {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.RED + "[PEEK] New peek command run, cancelling old one."));
                        peekCommandExecutorService.shutdownNow();
                        peekCommandExecutorService = Executors.newSingleThreadScheduledExecutor();
                    }

                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(
                            EnumChatFormatting.YELLOW + "[PEEK] Getting player skyblock profiles..."), id);

                    long startTime = System.currentTimeMillis();
                    peekCommandExecutorService.schedule(new Runnable() {
                        public void run() {
                            if (System.currentTimeMillis() - startTime > 10 * 1000) {

                                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(
                                        EnumChatFormatting.RED + "[PEEK] Getting profile info took too long, aborting."), id);
                                return;
                            }

                            String g = EnumChatFormatting.GRAY.toString();

                            JsonObject profileInfo = profile.getProfileInformation(null);
                            if (profileInfo != null) {
                                float overallScore = 0;

                                boolean isMe = name.equalsIgnoreCase("moulberry");

                                PlayerStats.Stats stats = profile.getStats(null);
                                JsonObject skill = profile.getSkillInfo(null);

                                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(EnumChatFormatting.GREEN + " " +
                                        EnumChatFormatting.STRIKETHROUGH + "-=-" + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + " " +
                                        Utils.getElementAsString(profile.getHypixelProfile().get("displayname"), name) + "'s Info " +
                                        EnumChatFormatting.STRIKETHROUGH + "-=-"), id);

                                if (skill == null) {
                                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Skills api disabled!"));
                                } else {
                                    float totalSkillLVL = 0;
                                    float totalSkillCount = 0;

                                    for (Map.Entry<String, JsonElement> entry : skill.entrySet()) {
                                        if (entry.getKey().startsWith("level_skill")) {
                                            if (entry.getKey().contains("runecrafting")) continue;
                                            if (entry.getKey().contains("carpentry")) continue;
                                            totalSkillLVL += entry.getValue().getAsFloat();
                                            totalSkillCount++;
                                        }
                                    }

                                    float combat = Utils.getElementAsFloat(skill.get("level_skill_combat"), 0);
                                    float zombie = Utils.getElementAsFloat(skill.get("level_slayer_zombie"), 0);
                                    float spider = Utils.getElementAsFloat(skill.get("level_slayer_spider"), 0);
                                    float wolf = Utils.getElementAsFloat(skill.get("level_slayer_wolf"), 0);
                                    float enderman = Utils.getElementAsFloat(skill.get("level_slayer_enderman"), 0);

                                    float avgSkillLVL = totalSkillLVL / totalSkillCount;

                                    if (isMe) {
                                        avgSkillLVL = 6;
                                        combat = 4;
                                        zombie = 2;
                                        spider = 1;
                                        wolf = 2;
                                        enderman = 0;
                                    }

                                    EnumChatFormatting combatPrefix = combat > 20 ? (combat > 35 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                    EnumChatFormatting zombiePrefix = zombie > 3 ? (zombie > 6 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                    EnumChatFormatting spiderPrefix = spider > 3 ? (spider > 6 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                    EnumChatFormatting wolfPrefix = wolf > 3 ? (wolf > 6 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                    EnumChatFormatting endermanPrefix = enderman > 3 ? (enderman > 6 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                    EnumChatFormatting avgPrefix = avgSkillLVL > 20 ? (avgSkillLVL > 35 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;

                                    overallScore += zombie * zombie / 81f;
                                    overallScore += spider * spider / 81f;
                                    overallScore += wolf * wolf / 81f;
                                    overallScore += enderman * enderman / 81f;
                                    overallScore += avgSkillLVL / 20f;

                                    int cata = (int) Utils.getElementAsFloat(skill.get("level_skill_catacombs"), 0);
                                    EnumChatFormatting cataPrefix = cata > 15 ? (cata > 25 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;

                                    overallScore += cata * cata / 2000f;

                                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                            g + "Combat: " + combatPrefix + (int) Math.floor(combat) +
                                                    (cata > 0 ? g + " - Cata: " + cataPrefix + cata : "") +
                                                    g + " - AVG: " + avgPrefix + (int) Math.floor(avgSkillLVL)));
                                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                            g + "Slayer: " + zombiePrefix + (int) Math.floor(zombie) + g + "-" +
                                                    spiderPrefix + (int) Math.floor(spider) + g + "-" +
                                                    wolfPrefix + (int) Math.floor(wolf) + "-" +
                                                    endermanPrefix + (int) Math.floor(enderman)));
                                }
                                if (stats == null) {
                                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                            EnumChatFormatting.YELLOW + "Skills, collection and/or inventory apis disabled!"));
                                } else {
                                    int health = (int) stats.get("health");
                                    int defence = (int) stats.get("defence");
                                    int strength = (int) stats.get("strength");
                                    int intelligence = (int) stats.get("intelligence");

                                    EnumChatFormatting healthPrefix = health > 800 ? (health > 1600 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                    EnumChatFormatting defencePrefix = defence > 200 ? (defence > 600 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                    EnumChatFormatting strengthPrefix = strength > 100 ? (strength > 300 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                    EnumChatFormatting intelligencePrefix = intelligence > 300 ? (intelligence > 900 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;

                                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                            g + "Stats  : " + healthPrefix + health + EnumChatFormatting.RED + "\u2764 " +
                                                    defencePrefix + defence + EnumChatFormatting.GREEN + "\u2748 " +
                                                    strengthPrefix + strength + EnumChatFormatting.RED + "\u2741 " +
                                                    intelligencePrefix + intelligence + EnumChatFormatting.AQUA + "\u270e "));
                                }
                                float bankBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "banking.balance"), -1);
                                float purseBalance = Utils.getElementAsFloat(Utils.getElement(profileInfo, "coin_purse"), 0);

                                long networth = profile.getNetWorth(null);
                                float money = Math.max(bankBalance + purseBalance, networth);
                                EnumChatFormatting moneyPrefix = money > 50 * 1000 * 1000 ?
                                        (money > 200 * 1000 * 1000 ? EnumChatFormatting.GREEN : EnumChatFormatting.YELLOW) : EnumChatFormatting.RED;
                                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                        g + "Purse: " + moneyPrefix + Utils.shortNumberFormat(purseBalance, 0) + g + " - Bank: " +
                                                (bankBalance == -1 ? EnumChatFormatting.YELLOW + "N/A" : moneyPrefix +
                                                        (isMe ? "4.8b" : Utils.shortNumberFormat(bankBalance, 0))) +
                                                (networth > 0 ? g + " - Net: " + moneyPrefix + Utils.shortNumberFormat(networth, 0) : "")));

                                overallScore += Math.min(2, money / (100f * 1000 * 1000));

                                String activePet = Utils.getElementAsString(Utils.getElement(profile.getPetsInfo(null), "active_pet.type"),
                                        "None Active");
                                String activePetTier = Utils.getElementAsString(Utils.getElement(profile.getPetsInfo(null), "active_pet.tier"), "UNKNOWN");

                                String col = NotEnoughUpdates.petRarityToColourMap.get(activePetTier);
                                if (col == null) col = EnumChatFormatting.LIGHT_PURPLE.toString();

                                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(g + "Pet    : " +
                                        col + WordUtils.capitalizeFully(activePet.replace("_", " "))));

                                String overall = "Skywars Main";
                                if (isMe) {
                                    overall = Utils.chromaString("Literally the best player to exist"); // ego much
                                } else if (overallScore < 5 && (bankBalance + purseBalance) > 500 * 1000 * 1000) {
                                    overall = EnumChatFormatting.GOLD + "Bill Gates";
                                } else if (overallScore > 9) {
                                    overall = Utils.chromaString("Didn't even think this score was possible");
                                } else if (overallScore > 8) {
                                    overall = Utils.chromaString("Mentally unstable");
                                } else if (overallScore > 7) {
                                    overall = EnumChatFormatting.GOLD + "Why though 0.0";
                                } else if (overallScore > 5.5) {
                                    overall = EnumChatFormatting.GOLD + "Bro stop playing";
                                } else if (overallScore > 4) {
                                    overall = EnumChatFormatting.GREEN + "Kinda sweaty";
                                } else if (overallScore > 3) {
                                    overall = EnumChatFormatting.YELLOW + "Alright I guess";
                                } else if (overallScore > 2) {
                                    overall = EnumChatFormatting.YELLOW + "Ender Non";
                                } else if (overallScore > 1) {
                                    overall = EnumChatFormatting.RED + "Played Skyblock";
                                }

                                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(g + "Overall score: " +
                                        overall + g + " (" + Math.round(overallScore * 10) / 10f + ")"));

                                peekCommandExecutorService.shutdownNow();
                            } else {
                                peekCommandExecutorService.schedule(this, 200, TimeUnit.MILLISECONDS);
                            }
                        }
                    }, 200, TimeUnit.MILLISECONDS);
                }
            });
        }
    }, new SimpleCommand.TabCompleteRunnable() {
        @Override
        public java.util.List<String> tabComplete(ICommandSender sender, String[] args, BlockPos pos) {
            if (args.length != 1) return null;

            String lastArg = args[args.length - 1];
            List<String> playerMatches = new ArrayList<>();
            for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                String playerName = player.getName();
                if (playerName.toLowerCase().startsWith(lastArg.toLowerCase())) {
                    playerMatches.add(playerName);
                }
            }
            return playerMatches;
        }
    });

    public SimpleCommand.ProcessCommandRunnable viewProfileRunnable = new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (!OpenGlHelper.isFramebufferEnabled()) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                        "Some parts of the profile viewer do not work with OF Fast Render. Go to ESC > Options > Video Settings > Performance > Fast Render to disable it."));

            }
            if (NotEnoughUpdates.INSTANCE.config.apiKey.apiKey == null || NotEnoughUpdates.INSTANCE.config.apiKey.apiKey.trim().isEmpty()) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                        "Can't view profile, apikey is not set. Run /api new and put the result in settings."));
            } else if (args.length == 0) {
                NotEnoughUpdates.profileViewer.getProfileByName(Minecraft.getMinecraft().thePlayer.getName(), profile -> {
                    if (profile == null) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                                "Invalid player name/api key. Maybe api is down? Try /api new."));
                    } else {
                        profile.resetCache();
                        NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
                    }
                });
            } else if (args.length > 1) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                        "Too many arguments. Usage: /neuprofile [name]"));
            } else {
                NotEnoughUpdates.profileViewer.getProfileByName(args[0], profile -> {
                    if (profile == null) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                                "Invalid player name/api key. Maybe api is down? Try /api new."));
                    } else {
                        profile.resetCache();
                        NotEnoughUpdates.INSTANCE.openGui = new GuiProfileViewer(profile);
                    }
                });
            }
        }
    };

    SimpleCommand joinDungeonCommand = new SimpleCommand("join", new SimpleCommand.ProcessCommandRunnable() {
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/join " + StringUtils.join(args, " "));
            } else {
                if (args.length != 1) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.RED + "Example Usage: /join f7, /join m6 or /join 7"));
                } else {
                    String cataPrefix = "catacombs";
                    if (args[0].startsWith("m")) {
                        cataPrefix = "master_catacombs";
                    }
                    String cmd = "/joindungeon " + cataPrefix + " " + args[0].charAt(args[0].length() - 1);
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.YELLOW + "Running command: " + cmd));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.YELLOW + "The dungeon should start soon. If it doesn't, make sure you have a party of 5 people"));
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
                }
            }
        }
    });

    SimpleCommand viewProfileCommand = new SimpleCommand("neuprofile", viewProfileRunnable, new SimpleCommand.TabCompleteRunnable() {
        @Override
        public List<String> tabComplete(ICommandSender sender, String[] args, BlockPos pos) {
            if (args.length != 1) return null;

            String lastArg = args[args.length - 1];
            List<String> playerMatches = new ArrayList<>();
            for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                String playerName = player.getName();
                if (playerName.toLowerCase().startsWith(lastArg.toLowerCase())) {
                    playerMatches.add(playerName);
                }
            }
            return playerMatches;
        }
    });

    SimpleCommand viewProfileShortCommand = new SimpleCommand("pv", new SimpleCommand.ProcessCommandRunnable() {
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/pv " + StringUtils.join(args, " "));
            } else {
                viewProfileRunnable.processCommand(sender, args);
            }
        }
    }, new SimpleCommand.TabCompleteRunnable() {
        @Override
        public List<String> tabComplete(ICommandSender sender, String[] args, BlockPos pos) {
            if (args.length != 1) return null;

            String lastArg = args[args.length - 1];
            List<String> playerMatches = new ArrayList<>();
            for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                String playerName = player.getName();
                if (playerName.toLowerCase().startsWith(lastArg.toLowerCase())) {
                    playerMatches.add(playerName);
                }
            }
            return playerMatches;
        }
    });

    SimpleCommand dhCommand = new SimpleCommand("dh", new SimpleCommand.ProcessCommandRunnable() {
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp dungeon_hub");
        }
    });

    private final ScheduledExecutorService devES = Executors.newSingleThreadScheduledExecutor();
    private static final String[] devFailStrings = {"No.", "I said no.", "You aren't allowed to use this.",
            "Are you sure you want to use this? Type 'Yes' in chat.", "Are you sure you want to use this? Type 'Yes' in chat.",
            "Lmao you thought", "Ok please stop", "What do you want from me?",
            "This command almost certainly does nothing useful for you",
            "Ok, this is the last message, after this it will repeat", "No.", "I said no.", "Dammit. I thought that would work. Uhh...",
            "\u00a7dFrom \u00a7c[ADMIN] Minikloon\u00a77: If you use that command again, I'll have to ban you", "",
            "Ok, this is actually the last message, use the command again and you'll crash I promise"};
    private int devFailIndex = 0;

    private static final List<String> devTestUsers = new ArrayList<>(Arrays.asList("moulberry", "lucycoconut", "ironm00n", "ariyio", "throwpo", "lrg89", "dediamondpro"));
    SimpleCommand devTestCommand = new SimpleCommand("neudevtest", new SimpleCommand.ProcessCommandRunnable() {
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (!devTestUsers.contains(Minecraft.getMinecraft().thePlayer.getName().toLowerCase())) {
                if (devFailIndex >= devFailStrings.length) {
                    throw new Error("L") {
                        @Override
                        public void printStackTrace() {
                            throw new Error("L");
                        }
                    };
                }
                if (devFailIndex == devFailStrings.length - 2) {
                    devFailIndex++;

                    ChatComponentText component = new ChatComponentText("\u00a7cYou are permanently banned from this server!");
                    component.appendText("\n");
                    component.appendText("\n\u00a77Reason: \u00a7rI told you not to run the command - Moulberry");
                    component.appendText("\n\u00a77Find out more: \u00a7b\u00a7nhttps://www.hypixel.net/appeal");
                    component.appendText("\n");
                    component.appendText("\n\u00a77Ban ID: \u00a7r#49871982");
                    component.appendText("\n\u00a77Sharing your Ban ID may affect the processing of your appeal!");
                    Minecraft.getMinecraft().getNetHandler().getNetworkManager().closeChannel(component);
                    return;
                }
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + devFailStrings[devFailIndex++]));
                return;
            }
            /*if(args.length == 1) {
                DupePOC.doDupe(args[0]);
                return;
            }*/
            if (args.length >= 1 && args[0].equalsIgnoreCase("pricetest")) {
                if (args.length == 1) {
                    NotEnoughUpdates.INSTANCE.manager.auctionManager.updateBazaar();
                } else {
                    NotEnoughUpdates.INSTANCE.openGui = new GuiPriceGraph(args[1]);
                }
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("zone")) {
                BlockPos target = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
                if (target == null) target = Minecraft.getMinecraft().thePlayer.getPosition();
                SpecialBlockZone zone = CustomBiomes.INSTANCE.getSpecialZone(target);
                Arrays.asList(
                        new ChatComponentText("Showing Zone Info for: " + target),
                        new ChatComponentText("Zone: " + (zone != null ? zone.name() : "null")),
                        new ChatComponentText("Location: " + SBInfo.getInstance().getLocation()),
                        new ChatComponentText("Biome: " + CustomBiomes.INSTANCE.getCustomBiome(target))
                ).forEach(Minecraft.getMinecraft().thePlayer::addChatMessage);
                MinecraftForge.EVENT_BUS.post(new LocationChangeEvent(SBInfo.getInstance().getLocation(), SBInfo.getInstance().getLocation()));
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("positiontest")) {
                NotEnoughUpdates.INSTANCE.openGui = new GuiPositionEditor();
                return;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("pt")) {
                EnumParticleTypes t = EnumParticleTypes.valueOf(args[1]);
                FishingHelper.type = t;
                return;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("dev")) {
                NotEnoughUpdates.INSTANCE.config.hidden.dev = true;
                return;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("saveconfig")) {
                NotEnoughUpdates.INSTANCE.saveConfig();
                return;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("searchmode")) {
                NotEnoughUpdates.INSTANCE.config.hidden.firstTimeSearchFocus = true;
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "I would never search"));
                return;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("center")) {
                double x = Math.floor(Minecraft.getMinecraft().thePlayer.posX) + 0.5f;
                double z = Math.floor(Minecraft.getMinecraft().thePlayer.posZ) + 0.5f;
                Minecraft.getMinecraft().thePlayer.setPosition(x, Minecraft.getMinecraft().thePlayer.posY, z);
                return;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("pansc")) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Taking panorama screenshot"));

                AtomicInteger perspective = new AtomicInteger(0);
                FancyPortals.perspectiveId = 0;

                EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
                p.prevRotationYaw = p.rotationYaw = 0;
                p.prevRotationPitch = p.rotationPitch = 90;
                devES.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Minecraft.getMinecraft().addScheduledTask(() -> {
                            ScreenShotHelper.saveScreenshot(new File("C:/Users/James/Desktop/"), "pansc-" + perspective.get() + ".png",
                                    Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight,
                                    Minecraft.getMinecraft().getFramebuffer());
                        });
                        if (perspective.incrementAndGet() >= 6) {
                            FancyPortals.perspectiveId = -1;
                            return;
                        }
                        devES.schedule(() -> {
                            FancyPortals.perspectiveId = perspective.get();
                            if (FancyPortals.perspectiveId == 5) {
                                p.prevRotationYaw = p.rotationYaw = 0;
                                p.prevRotationPitch = p.rotationPitch = -90;
                            } else if (FancyPortals.perspectiveId >= 1 && FancyPortals.perspectiveId <= 4) {
                                float yaw = 90 * FancyPortals.perspectiveId - 180;
                                if (yaw > 180) yaw -= 360;
                                p.prevRotationYaw = p.rotationYaw = yaw;
                                p.prevRotationPitch = p.rotationPitch = 0;
                            }
                            devES.schedule(this, 3000L, TimeUnit.MILLISECONDS);
                        }, 100L, TimeUnit.MILLISECONDS);
                    }
                }, 3000L, TimeUnit.MILLISECONDS);

                return;
            }

            /* if(args.length == 1 && args[0].equalsIgnoreCase("update")) {
                NEUEventListener.displayUpdateMessageIfOutOfDate();
            } */

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Executing dubious code"));
            /*Minecraft.getMinecraft().thePlayer.rotationYaw = 0;
            Minecraft.getMinecraft().thePlayer.rotationPitch = 0;
            Minecraft.getMinecraft().thePlayer.setPosition(
                    Math.floor(Minecraft.getMinecraft().thePlayer.posX) + Float.parseFloat(args[0]),
                    Minecraft.getMinecraft().thePlayer.posY,
                    Minecraft.getMinecraft().thePlayer.posZ);*/
            //Hot reload me yay!
        }
    });

    SimpleCommand packDevCommand = new SimpleCommand("neupackdev", new SimpleCommand.ProcessCommandRunnable() {
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 1 && args[0].equalsIgnoreCase("getnpc")) {
                double distSq = 25;
                EntityPlayer closestNPC = null;
                EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
                for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                    if (player instanceof AbstractClientPlayer && p != player && player.getUniqueID().version() != 4) {
                        double dSq = player.getDistanceSq(p.posX, p.posY, p.posZ);
                        if (dSq < distSq) {
                            distSq = dSq;
                            closestNPC = player;
                        }
                    }
                }

                if (closestNPC == null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "No NPCs found within 5 blocks :("));
                } else {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Copied entity texture id to clipboard"));
                    MiscUtils.copyToClipboard(((AbstractClientPlayer) closestNPC).getLocationSkin().getResourcePath().replace("skins/", ""));
                }
                return;
            }
            NotEnoughUpdates.INSTANCE.packDevEnabled = !NotEnoughUpdates.INSTANCE.packDevEnabled;
            if (NotEnoughUpdates.INSTANCE.packDevEnabled) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Enabled pack developer mode."));
            } else {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Disabled pack developer mode."));
            }
        }
    });

    SimpleCommand dnCommand = new SimpleCommand("dn", new SimpleCommand.ProcessCommandRunnable() {
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp dungeon_hub");
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Warping to:" + EnumChatFormatting.YELLOW + " Deez Nuts lmao"));
        }
    });

    SimpleCommand viewCataCommand = new SimpleCommand("cata", new SimpleCommand.ProcessCommandRunnable() {
        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            GuiProfileViewer.currentPage = GuiProfileViewer.ProfileViewerPage.DUNG;
            viewProfileRunnable.processCommand(sender, args);
        }
    }, new SimpleCommand.TabCompleteRunnable() {
        @Override
        public List<String> tabComplete(ICommandSender sender, String[] args, BlockPos pos) {
            if (args.length != 1) return null;

            String lastArg = args[args.length - 1];
            List<String> playerMatches = new ArrayList<>();
            for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                String playerName = player.getName();
                if (playerName.toLowerCase().startsWith(lastArg.toLowerCase())) {
                    playerMatches.add(playerName);
                }
            }
            return playerMatches;
        }
    });

    SimpleCommand linksCommand = new SimpleCommand("neulinks", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            File repo = NotEnoughUpdates.INSTANCE.manager.repoLocation;
            if (repo.exists()) {
                File updateJson = new File(repo, "update.json");
                try {
                    JsonObject update = NotEnoughUpdates.INSTANCE.manager.getJsonFromFile(updateJson);

                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                    NotEnoughUpdates.INSTANCE.displayLinks(update);
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                } catch (Exception ignored) {}
                }
            }
        }
    });

    SimpleCommand overlayPlacementsCommand = new SimpleCommand("neuoverlay", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            NotEnoughUpdates.INSTANCE.openGui = new NEUOverlayPlacements();
        }
    });

    SimpleCommand tutorialCommand = new SimpleCommand("neututorial", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            NotEnoughUpdates.INSTANCE.openGui = new NeuTutorial();
        }
    });

    SimpleCommand neumapCommand = new SimpleCommand("neumap", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (NotEnoughUpdates.INSTANCE.colourMap == null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(
                        new ResourceLocation("notenoughupdates:maps/F1Full.json")).getInputStream(), StandardCharsets.UTF_8))) {
                    JsonObject json = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(reader, JsonObject.class);

                    NotEnoughUpdates.INSTANCE.colourMap = new Color[128][128];
                    for (int x = 0; x < 128; x++) {
                        for (int y = 0; y < 128; y++) {
                            NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(0, 0, 0, 0);
                        }
                    }
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        int x = Integer.parseInt(entry.getKey().split(":")[0]);
                        int y = Integer.parseInt(entry.getKey().split(":")[1]);

                        NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(entry.getValue().getAsInt(), true);
                    }
                } catch (Exception ignored) {}
            }

            if (!NotEnoughUpdates.INSTANCE.config.hidden.dev) {
                NotEnoughUpdates.INSTANCE.openGui = new GuiDungeonMapEditor();
                return;
            }

            if (args.length == 1 && args[0].equals("reset")) {
                NotEnoughUpdates.INSTANCE.colourMap = null;
                return;
            }

            if (args.length != 2) {
                NotEnoughUpdates.INSTANCE.openGui = new GuiDungeonMapEditor();
                return;
            }

            if (args[0].equals("save")) {
                ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem();
                if (stack != null && stack.getItem() instanceof ItemMap) {
                    ItemMap map = (ItemMap) stack.getItem();
                    MapData mapData = map.getMapData(stack, Minecraft.getMinecraft().theWorld);

                    if (mapData == null) return;

                    JsonObject json = new JsonObject();
                    for (int i = 0; i < 16384; ++i) {
                        int x = i % 128;
                        int y = i / 128;

                        int j = mapData.colors[i] & 255;

                        Color c;
                        if (j / 4 == 0) {
                            c = new Color((i + i / 128 & 1) * 8 + 16 << 24, true);
                        } else {
                            c = new Color(MapColor.mapColorArray[j / 4].getMapColor(j & 3), true);
                        }

                        json.addProperty(x + ":" + y, c.getRGB());
                    }

                    try {
                        new File(NotEnoughUpdates.INSTANCE.manager.configLocation, "maps").mkdirs();
                        NotEnoughUpdates.INSTANCE.manager.writeJson(json, new File(NotEnoughUpdates.INSTANCE.manager.configLocation, "maps/" + args[1] + ".json"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN +
                            "Saved to file."));
                }

                return;
            }

            if (args[0].equals("load")) {
                JsonObject json = NotEnoughUpdates.INSTANCE.manager.getJsonFromFile(new File(NotEnoughUpdates.INSTANCE.manager.configLocation, "maps/" + args[1] + ".json"));

                NotEnoughUpdates.INSTANCE.colourMap = new Color[128][128];
                for (int x = 0; x < 128; x++) {
                    for (int y = 0; y < 128; y++) {
                        NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(0, 0, 0, 0);
                    }
                }
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    int x = Integer.parseInt(entry.getKey().split(":")[0]);
                    int y = Integer.parseInt(entry.getKey().split(":")[1]);

                    NotEnoughUpdates.INSTANCE.colourMap[x][y] = new Color(entry.getValue().getAsInt(), true);
                }

                return;
            }

            NotEnoughUpdates.INSTANCE.openGui = new GuiDungeonMapEditor();
        }
    });

    SimpleCommand cosmeticsCommand = new SimpleCommand("neucosmetics", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (!OpenGlHelper.isFramebufferEnabled()) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                        "NEU cosmetics do not work with OF Fast Render. Go to ESC > Options > Video Settings > Performance > Fast Render to disable it."));

            }

            NotEnoughUpdates.INSTANCE.openGui = new GuiCosmetics();
        }
    });

    SimpleCommand.ProcessCommandRunnable customizeRunnable = new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();

            if (held == null) {
                sender.addChatMessage(new ChatComponentText("\u00a7cYou can't customize your hand..."));
                return;
            }

            String heldUUID = NotEnoughUpdates.INSTANCE.manager.getUUIDForItem(held);

            if (heldUUID == null) {
                sender.addChatMessage(new ChatComponentText("\u00a7cHeld item does not have UUID, cannot be customized"));
                return;
            }

            NotEnoughUpdates.INSTANCE.openGui = new GuiItemCustomize(held, heldUUID);
        }
    };

    SimpleCommand customizeCommand = new SimpleCommand("neucustomize", customizeRunnable);
    SimpleCommand customizeCommand2 = new SimpleCommand("neurename", customizeRunnable);

    SimpleCommand.ProcessCommandRunnable settingsRunnable = new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length > 0) {
                NotEnoughUpdates.INSTANCE.openGui = new GuiScreenElementWrapper(new NEUConfigEditor(NotEnoughUpdates.INSTANCE.config, StringUtils.join(args, " ")));
            } else {
                NotEnoughUpdates.INSTANCE.openGui = new GuiScreenElementWrapper(NEUConfigEditor.editor);
            }
        }
    };

    SimpleCommand settingsCommand = new SimpleCommand("neu", settingsRunnable);
    SimpleCommand settingsCommand2 = new SimpleCommand("neusettings", settingsRunnable);
    SimpleCommand settingsCommand3 = new SimpleCommand("neuconfig", settingsRunnable);

    SimpleCommand calendarCommand = new SimpleCommand("neucalendar", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            Minecraft.getMinecraft().thePlayer.closeScreen();
            CalendarOverlay.setEnabled(true);
            NotEnoughUpdates.INSTANCE.sendChatMessage("/calendar");
        }
    });

    SimpleCommand neuAhCommand = new SimpleCommand("neuah", new SimpleCommand.ProcessCommandRunnable() {
        public void processCommand(ICommandSender sender, String[] args) {
            if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                        "You must be on Skyblock to use this feature."));
            } else if (NotEnoughUpdates.INSTANCE.config.apiKey.apiKey == null || NotEnoughUpdates.INSTANCE.config.apiKey.apiKey.trim().isEmpty()) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
                        "Can't open NeuAH, apikey is not set. Run /api new and put the result in settings."));
            } else {
                NotEnoughUpdates.INSTANCE.openGui = new CustomAHGui();
                NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.lastOpen = System.currentTimeMillis();
                NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.clearSearch();
                NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.updateSearch();

                if (args.length > 0)
                    NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.setSearch(StringUtils.join(args, " "));
            }
        }
    });
}
