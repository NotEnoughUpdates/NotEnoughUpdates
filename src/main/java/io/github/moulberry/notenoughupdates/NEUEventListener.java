package io.github.moulberry.notenoughupdates;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import io.github.moulberry.notenoughupdates.auction.CustomAHGui;
import io.github.moulberry.notenoughupdates.commands.profile.ViewProfileCommand;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.core.util.MiscUtils;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.cosmetics.CapeManager;
import io.github.moulberry.notenoughupdates.dungeons.DungeonBlocks;
import io.github.moulberry.notenoughupdates.dungeons.DungeonWin;
import io.github.moulberry.notenoughupdates.miscfeatures.*;
import io.github.moulberry.notenoughupdates.miscgui.*;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.overlays.*;
import io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer;
import io.github.moulberry.notenoughupdates.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.*;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.moulberry.notenoughupdates.overlays.SlayerOverlay.*;
import static io.github.moulberry.notenoughupdates.util.GuiTextures.dungeon_chest_worth;

public class NEUEventListener {

    private final NotEnoughUpdates neu;

    private boolean hoverInv = false;
    private boolean focusInv = false;

    private boolean joinedSB = false;

    public NEUEventListener(NotEnoughUpdates neu) {
        this.neu = neu;
    }

    private void displayUpdateMessageIfOutOfDate() {
        File repo = neu.manager.repoLocation;
        if (repo.exists()) {
            File updateJson = new File(repo, "update.json");
            try {
                JsonObject o = neu.manager.getJsonFromFile(updateJson);

                String version = o.get("version").getAsString();
                String preVersion = o.get("pre_version").getAsString();

                boolean shouldUpdate = !NotEnoughUpdates.VERSION.equalsIgnoreCase(version);
                boolean shouldPreUpdate = !NotEnoughUpdates.PRE_VERSION.equalsIgnoreCase(preVersion);

                if (o.has("version_id") && o.get("version_id").isJsonPrimitive()) {
                    int version_id = o.get("version_id").getAsInt();
                    shouldUpdate = version_id > NotEnoughUpdates.VERSION_ID;
                }
                if (o.has("pre_version_id") && o.get("pre_version_id").isJsonPrimitive()) {
                    int pre_version_id = o.get("pre_version_id").getAsInt();
                    shouldPreUpdate = pre_version_id > NotEnoughUpdates.PRE_VERSION_ID;
                }

                if (shouldUpdate) {
                    String update_msg = o.get("update_msg").getAsString();

                    int first_len = -1;
                    for (String line : update_msg.split("\n")) {
                        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                        int len = fr.getStringWidth(line);
                        if (first_len == -1) {
                            first_len = len;
                        }
                        int missing_len = first_len - len;
                        if (missing_len > 0) {
                            StringBuilder sb = new StringBuilder(line);
                            for (int i = 0; i < missing_len / 8; i++) {
                                sb.insert(0, " ");
                            }
                            line = sb.toString();
                        }
                        line = line.replaceAll("\\{version}", version);
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(line));
                    }

                    neu.displayLinks(o);

                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                } else if (shouldPreUpdate && NotEnoughUpdates.VERSION_ID == o.get("version").getAsInt()) {
                    String pre_update_msg = o.get("pre_update_msg").getAsString();

                    int first_len = -1;
                    for (String line : pre_update_msg.split("\n")) {
                        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                        int len = fr.getStringWidth(line);
                        if (first_len == -1) {
                            first_len = len;
                        }
                        int missing_len = first_len - len;
                        if (missing_len > 0) {
                            StringBuilder sb = new StringBuilder(line);
                            for (int i = 0; i < missing_len / 8; i++) {
                                sb.insert(0, " ");
                            }
                            line = sb.toString();
                        }
                        line = line.replaceAll("\\{pre_version}", preVersion);
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(line));
                    }

                    neu.displayLinks(o);

                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                }
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Unload event) {
        NotEnoughUpdates.INSTANCE.saveConfig();
        CrystalMetalDetectorSolver.reset(false);
    }

    private static long notificationDisplayMillis = 0;
    private static List<String> notificationLines = null;
    private static boolean showNotificationOverInv = false;

    private static final Pattern BAD_ITEM_REGEX = Pattern.compile("x[0-9]{1,2}$");
    private static final Pattern SLAYER_XP = Pattern.compile("   (Spider|Zombie|Wolf|Enderman) Slayer LVL (\\d) - (?:Next LVL in ([\\d,]+) XP!|LVL MAXED OUT!)");

    /**
     * 1)Will send the cached message from #sendChatMessage when at least 200ms has passed since the last message.
     * This is used in order to prevent the mod spamming messages.
     * 2)Adds unique items to the collection log
     */
    private boolean preloadedItems = false;
    private long lastLongUpdate = 0;
    private long lastSkyblockScoreboard = 0;

    private final ExecutorService itemPreloader = Executors.newFixedThreadPool(10);
    private final List<ItemStack> toPreload = new ArrayList<>();

    private int inventoryLoadedTicks = 0;
    private String loadedInvName = "";
    public static boolean inventoryLoaded = false;

    public static void displayNotification(List<String> lines, boolean showForever) {
        displayNotification(lines, showForever, false);
    }

    public static void displayNotification(List<String> lines, boolean showForever, boolean overInventory) {
        if (showForever) {
            notificationDisplayMillis = -420;
        } else {
            notificationDisplayMillis = System.currentTimeMillis();
        }
        notificationLines = lines;
        showNotificationOverInv = overInventory;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null && (Minecraft.getMinecraft().currentScreen instanceof GuiChat
                || Minecraft.getMinecraft().currentScreen instanceof GuiEditSign || Minecraft.getMinecraft().currentScreen instanceof GuiScreenBook)) {
            Keyboard.enableRepeatEvents(true);
        } else {
            Keyboard.enableRepeatEvents(false);
        }
        if (event.phase != TickEvent.Phase.START) return;
        if (Minecraft.getMinecraft().theWorld == null) return;
        if (Minecraft.getMinecraft().thePlayer == null) return;

        if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
            GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
            ContainerChest cc = (ContainerChest) chest.inventorySlots;

            if (!loadedInvName.equals(cc.getLowerChestInventory().getDisplayName().getUnformattedText())) {
                loadedInvName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
                inventoryLoaded = false;
                inventoryLoadedTicks = 3;
            }

            if (!inventoryLoaded) {
                if (cc.getLowerChestInventory().getStackInSlot(cc.getLowerChestInventory().getSizeInventory() - 1) != null) {
                    inventoryLoaded = true;
                } else {
                    for (ItemStack stack : chest.inventorySlots.getInventory()) {
                        if (stack != null) {
                            if (--inventoryLoadedTicks <= 0) {
                                inventoryLoaded = true;
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            inventoryLoaded = false;
            inventoryLoadedTicks = 3;
        }

        if ((Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1) && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4) && Keyboard.isKeyDown(Keyboard.KEY_NUMPAD9))) {
            ChatComponentText component = new ChatComponentText("\u00a7cYou are permanently banned from this server!");
            component.appendText("\n");
            component.appendText("\n\u00a77Reason: \u00a7rSuspicious account activity/Other");
            component.appendText("\n\u00a77Find out more: \u00a7b\u00a7nhttps://www.hypixel.net/appeal");
            component.appendText("\n");
            component.appendText("\n\u00a77Ban ID: \u00a7r#49871982");
            component.appendText("\n\u00a77Sharing your Ban ID may affect the processing of your appeal!");
            Minecraft.getMinecraft().getNetHandler().getNetworkManager().closeChannel(component);
            return;
        }

        if (neu.hasSkyblockScoreboard()) {
            if (!preloadedItems) {
                preloadedItems = true;
                List<JsonObject> list = new ArrayList<>(neu.manager.getItemInformation().values());
                for (JsonObject json : list) {
                    itemPreloader.submit(() -> {
                        ItemStack stack = neu.manager.jsonToStack(json, true, true);
                        if (stack.getItem() == Items.skull) toPreload.add(stack);
                    });
                }
            } else if (!toPreload.isEmpty()) {
                Utils.drawItemStack(toPreload.get(0), -100, -100);
                toPreload.remove(0);
            } else {
                itemPreloader.shutdown();
            }

            for (TextOverlay overlay : OverlayManager.textOverlays) {
                overlay.shouldUpdateFrequent = true;
            }
        }

        boolean longUpdate = false;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLongUpdate > 1000) {
            longUpdate = true;
            lastLongUpdate = currentTime;
        }
        if (!NotEnoughUpdates.INSTANCE.config.dungeons.slowDungeonBlocks) {
            DungeonBlocks.tick();
        }
        DungeonWin.tick();

        String containerName = null;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
            GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
            ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
            containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();

            if (GuiCustomEnchant.getInstance().shouldOverride(containerName)) {
                GuiCustomEnchant.getInstance().tick();
            }
        }

        if (longUpdate) {
            CrystalOverlay.tick();
            FairySouls.tick();
            XPInformation.getInstance().tick();
            ProfileApiSyncer.getInstance().tick();
            ItemCustomizeManager.tick();
            BackgroundBlur.markDirty();
            NPCRetexturing.getInstance().tick();
            StorageOverlay.getInstance().markDirty();

            if (neu.hasSkyblockScoreboard()) {
                for (TextOverlay overlay : OverlayManager.textOverlays) {
                    overlay.tick();
                }
            }

            NotEnoughUpdates.INSTANCE.overlay.redrawItems();
            CapeManager.onTickSlow();

            NotEnoughUpdates.profileViewer.putNameUuid(Minecraft.getMinecraft().thePlayer.getName(),
                    Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""));

            if (NotEnoughUpdates.INSTANCE.config.dungeons.slowDungeonBlocks) {
                DungeonBlocks.tick();
            }

            if (System.currentTimeMillis() - SBInfo.getInstance().joinedWorld > 500 &&
                    System.currentTimeMillis() - SBInfo.getInstance().unloadedWorld > 500) {
                neu.updateSkyblockScoreboard();
            }
            CapeManager.getInstance().tick();

            if (containerName != null) {
                if (!containerName.trim().startsWith("Accessory Bag")) {
                    AccessoryBagOverlay.resetCache();
                }
            } else {
                AccessoryBagOverlay.resetCache();
            }

            if (neu.hasSkyblockScoreboard()) {
                SBInfo.getInstance().tick();
                lastSkyblockScoreboard = currentTime;
                if (!joinedSB) {
                    joinedSB = true;

                    //SBGamemodes.loadFromFile();

                    if (NotEnoughUpdates.INSTANCE.config.notifications.showUpdateMsg) {
                        displayUpdateMessageIfOutOfDate();
                    }

                    if (NotEnoughUpdates.INSTANCE.config.notifications.doRamNotif) {
                        long maxMemoryMB = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
                        if (maxMemoryMB > 4100) {
                            notificationDisplayMillis = System.currentTimeMillis();
                            notificationLines = new ArrayList<>();
                            notificationLines.add(EnumChatFormatting.GRAY + "Too much memory allocated!");
                            notificationLines.add(String.format(EnumChatFormatting.DARK_GRAY + "NEU has detected %03dMB of memory allocated to Minecraft!", maxMemoryMB));
                            notificationLines.add(EnumChatFormatting.GRAY + "It is recommended to allocated between 2-4GB of memory");
                            notificationLines.add(EnumChatFormatting.GRAY + "More than 4GB MAY cause FPS issues, EVEN if you have 16GB+ available");
                            notificationLines.add("");
                            notificationLines.add(EnumChatFormatting.GRAY + "For more information, visit #ram-info in discord.gg/moulberry");
                        }
                    }

                    if (!NotEnoughUpdates.INSTANCE.config.hidden.loadedModBefore) {
                        NotEnoughUpdates.INSTANCE.config.hidden.loadedModBefore = true;
                        if (Constants.MISC == null || !Constants.MISC.has("featureslist")) {
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "WARNING: " + EnumChatFormatting.RESET + EnumChatFormatting.RED + "Could not load Feature List URL from repo."));
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.RED + "Please run " + EnumChatFormatting.BOLD + "/neuresetrepo" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " and " + EnumChatFormatting.BOLD + "restart your game" + EnumChatFormatting.RESET + EnumChatFormatting.RED + " in order to fix. " + EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + "If that doesn't fix it" + EnumChatFormatting.RESET + EnumChatFormatting.RED + ", please join discord.gg/moulberry and post in #neu-support"));
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("" + EnumChatFormatting.GOLD + "To view the feature list after restarting type /neufeatures"));
                        } else {
                            String url = Constants.MISC.get("featureslist").getAsString();
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                    EnumChatFormatting.BLUE + "It seems this is your first time using NotEnoughUpdates."));
                            ChatComponentText clickTextFeatures = new ChatComponentText(
                                    EnumChatFormatting.YELLOW + "Click this message if you would like to view a list of NotEnoughUpdate's Features.");
                            clickTextFeatures.setChatStyle(Utils.createClickStyle(ClickEvent.Action.OPEN_URL, url));
                            Minecraft.getMinecraft().thePlayer.addChatMessage(clickTextFeatures);
                        }
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                        ChatComponentText clickTextHelp = new ChatComponentText(
                                EnumChatFormatting.YELLOW + "Click this message if you would like to view a list of NotEnoughUpdate's commands.");
                        clickTextHelp.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND, "/neuhelp"));
                        Minecraft.getMinecraft().thePlayer.addChatMessage(clickTextHelp);
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
                    }
                }
            }
            if (currentTime - lastSkyblockScoreboard < 5 * 60 * 1000) { //5 minutes
                neu.manager.auctionManager.tick();
            } else {
                neu.manager.auctionManager.markNeedsUpdate();
            }
        }

        /*if(longUpdate && neu.hasSkyblockScoreboard()) {
            if(neu.manager.getCurrentProfile() == null || neu.manager.getCurrentProfile().length() == 0) {
                ProfileViewer.Profile profile = NotEnoughUpdates.profileViewer.getProfile(Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""),
                        callback->{});
                if(profile != null) {
                    String latest = profile.getLatestProfile();
                    if(latest != null) {
                        neu.manager.setCurrentProfileBackup(profile.getLatestProfile());
                    }
                }
            }*/
            /*if(neu.manager.getCurrentProfile() != null && neu.manager.getCurrentProfile().length() > 0) {
                HashSet<String> newItem = new HashSet<>();
                if(Minecraft.getMinecraft().currentScreen instanceof GuiContainer &&
                        !(Minecraft.getMinecraft().currentScreen instanceof GuiCrafting)) {
                    boolean usableContainer = true;
                    for(ItemStack stack : Minecraft.getMinecraft().thePlayer.openContainer.getInventory()) {
                        if(stack == null) {
                            continue;
                        }
                        if(stack.hasTagCompound()) {
                            NBTTagCompound tag = stack.getTagCompound();
                            if(tag.hasKey("ExtraAttributes", 10)) {
                                continue;
                            }
                        }
                        usableContainer = false;
                        break;
                    }
                    if(!usableContainer) {
                        if(Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
                            GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
                            ContainerChest container = (ContainerChest) chest.inventorySlots;
                            String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();

                            if(containerName.equals("Accessory Bag") || containerName.startsWith("Wardrobe")) {
                                usableContainer = true;
                            }
                        }
                    }
                    if(usableContainer) {
                        for(ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
                            processUniqueStack(stack, newItem);
                        }
                        for(ItemStack stack : Minecraft.getMinecraft().thePlayer.openContainer.getInventory()) {
                            processUniqueStack(stack, newItem);
                        }
                    }
                } else {
                    for(ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
                        processUniqueStack(stack, newItem);
                    }
                }
                newItemAddMap.keySet().retainAll(newItem);
            }
        }*/
    }

    /*private void processUniqueStack(ItemStack stack, HashSet<String> newItem) {
        if(stack != null && stack.hasTagCompound()) {
            String internalname = neu.manager.getInternalNameForItem(stack);
            if(internalname != null) {
                /*ArrayList<String> log = neu.manager.config.collectionLog.value.computeIfAbsent(
                        neu.manager.getCurrentProfile(), k -> new ArrayList<>());
                if(!log.contains(internalname)) {
                    newItem.add(internalname);
                    if(newItemAddMap.containsKey(internalname)) {
                        if(System.currentTimeMillis() - newItemAddMap.get(internalname) > 1000) {
                            log.add(internalname);
                            try { neu.manager.saveConfig(); } catch(IOException ignored) {}
                        }
                    } else {
                        newItemAddMap.put(internalname, System.currentTimeMillis());
                    }
                }
            }
        }
    }*/

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderEntitySpecials(RenderLivingEvent.Specials.Pre<EntityPlayer> event) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiProfileViewer) {
            if (((GuiProfileViewer) Minecraft.getMinecraft().currentScreen).getEntityPlayer() == event.entity) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.type != null && event.type.equals(RenderGameOverlayEvent.ElementType.BOSSHEALTH) &&
                Minecraft.getMinecraft().currentScreen instanceof GuiContainer && neu.overlay.isUsingMobsFilter()) {
            event.setCanceled(true);
        }
        if (event.type != null && event.type.equals(RenderGameOverlayEvent.ElementType.PLAYER_LIST)) {
            GlStateManager.enableDepth();
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event) {
        if (neu.hasSkyblockScoreboard() && event.type.equals(RenderGameOverlayEvent.ElementType.ALL)) {
            DungeonWin.render(event.partialTicks);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, -200);
            for (TextOverlay overlay : OverlayManager.textOverlays) {
                if (OverlayManager.dontRenderOverlay != null && OverlayManager.dontRenderOverlay.isAssignableFrom(overlay.getClass())) {
                    continue;
                }
                GlStateManager.translate(0, 0, -1);
                GlStateManager.enableDepth();
                overlay.render();
            }
            GlStateManager.popMatrix();
            OverlayManager.dontRenderOverlay = null;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
            notificationDisplayMillis = 0;
        }

        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            renderNotification();
        }

    }

    private static void renderNotification() {

        long timeRemaining = 15000 - (System.currentTimeMillis() - notificationDisplayMillis);
        boolean display = timeRemaining > 0 || notificationDisplayMillis == -420;
        if (display && notificationLines != null && notificationLines.size() > 0) {
            int width = 0;
            int height = notificationLines.size() * 10 + 10;

            for (String line : notificationLines) {
                int len = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line) + 8;
                if (len > width) {
                    width = len;
                }
            }

            ScaledResolution sr = Utils.pushGuiScale(2);

            int midX = sr.getScaledWidth() / 2;
            int topY = sr.getScaledHeight() * 3 / 4 - height / 2;
            RenderUtils.drawFloatingRectDark(midX - width / 2, sr.getScaledHeight() * 3 / 4 - height / 2, width, height);
            /*Gui.drawRect(midX-width/2, sr.getScaledHeight()*3/4-height/2,
                    midX+width/2, sr.getScaledHeight()*3/4+height/2, 0xFF3C3C3C);
            Gui.drawRect(midX-width/2+2, sr.getScaledHeight()*3/4-height/2+2,
                    midX+width/2-2, sr.getScaledHeight()*3/4+height/2-2, 0xFFC8C8C8);*/

            int xLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth("[X] Close");
            Minecraft.getMinecraft().fontRendererObj.drawString("[X] Close", midX + width / 2f - 3 - xLen,
                    topY + 3, 0xFFFF5555, false);

            if (notificationDisplayMillis > 0) {
                Minecraft.getMinecraft().fontRendererObj.drawString((timeRemaining / 1000) + "s", midX - width / 2f + 3,
                        topY + 3, 0xFFaaaaaa, false);
            }

            Utils.drawStringCentered(notificationLines.get(0), Minecraft.getMinecraft().fontRendererObj,
                    midX, topY + 4 + 5, false, -1);
            for (int i = 1; i < notificationLines.size(); i++) {
                String line = notificationLines.get(i);
                Utils.drawStringCentered(line, Minecraft.getMinecraft().fontRendererObj,
                        midX, topY + 4 + 5 + 2 + i * 10, false, -1);
            }

            Utils.pushGuiScale(-1);
        }
    }

    public static long lastGuiClosed = 0;

    /**
     * When opening a GuiContainer, will reset the overlay and load the config.
     * When closing a GuiContainer, will save the config.
     * Also includes a dev feature used for automatically acquiring crafting information from the "Crafting Table" GUI.
     */
    AtomicBoolean missingRecipe = new AtomicBoolean(false);

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        NEUApi.disableInventoryButtons = false;

        if ((Minecraft.getMinecraft().currentScreen instanceof GuiScreenElementWrapper ||
                Minecraft.getMinecraft().currentScreen instanceof GuiItemRecipe) &&
                event.gui == null && !(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) &&
                System.currentTimeMillis() - NotEnoughUpdates.INSTANCE.lastOpenedGui < 500) {
            NotEnoughUpdates.INSTANCE.lastOpenedGui = 0;
            event.setCanceled(true);
            return;
        }

        if (!(event.gui instanceof GuiContainer) && Minecraft.getMinecraft().currentScreen != null) {
            CalendarOverlay.setEnabled(false);
        }

        if (Minecraft.getMinecraft().currentScreen != null) {
            lastGuiClosed = System.currentTimeMillis();
        }

        neu.manager.auctionManager.customAH.lastGuiScreenSwitch = System.currentTimeMillis();
        BetterContainers.reset();
        inventoryLoaded = false;
        inventoryLoadedTicks = 3;

        if (event.gui == null && neu.manager.auctionManager.customAH.isRenderOverAuctionView() &&
                !(Minecraft.getMinecraft().currentScreen instanceof CustomAHGui)) {
            event.gui = new CustomAHGui();
        }

        if (!(event.gui instanceof GuiChest || event.gui instanceof GuiEditSign)) {
            neu.manager.auctionManager.customAH.setRenderOverAuctionView(false);
        } else if (event.gui instanceof GuiChest && (neu.manager.auctionManager.customAH.isRenderOverAuctionView() ||
                Minecraft.getMinecraft().currentScreen instanceof CustomAHGui)) {
            GuiChest chest = (GuiChest) event.gui;
            ContainerChest container = (ContainerChest) chest.inventorySlots;
            String containerName = container.getLowerChestInventory().getDisplayName().getUnformattedText();

            neu.manager.auctionManager.customAH.setRenderOverAuctionView(containerName.trim().equals("Auction View") ||
                    containerName.trim().equals("BIN Auction View") || containerName.trim().equals("Confirm Bid") ||
                    containerName.trim().equals("Confirm Purchase"));
        }

        //OPEN
        if (Minecraft.getMinecraft().currentScreen == null
                && event.gui instanceof GuiContainer) {
            neu.overlay.reset();
        }
        if (event.gui != null && NotEnoughUpdates.INSTANCE.config.hidden.dev) {
            if (event.gui instanceof GuiChest) {
                GuiChest eventGui = (GuiChest) event.gui;
                ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
                IInventory lower = cc.getLowerChestInventory();
                ses.schedule(() -> {
                    if (Minecraft.getMinecraft().currentScreen != event.gui) {
                        return;
                    }
                    if (lower.getStackInSlot(23).getDisplayName().endsWith("Crafting Table")) {
                        try {
                            ItemStack res = lower.getStackInSlot(25);
                            String resInternalname = neu.manager.getInternalNameForItem(res);

                            if (lower.getStackInSlot(48) != null) {
                                String backName = null;
                                NBTTagCompound tag = lower.getStackInSlot(48).getTagCompound();
                                if (tag.hasKey("display", 10)) {
                                    NBTTagCompound nbttagcompound = tag.getCompoundTag("display");
                                    if (nbttagcompound.getTagId("Lore") == 9) {
                                        NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);
                                        backName = nbttaglist1.getStringTagAt(0);
                                    }
                                }

                                if (backName != null) {
                                    String[] split = backName.split(" ");
                                    if (split[split.length - 1].contains("Rewards")) {
                                        String col = backName.substring(split[0].length() + 1,
                                                backName.length() - split[split.length - 1].length() - 1);

                                        JsonObject json = neu.manager.getItemInformation().get(resInternalname);
                                        json.addProperty("crafttext", "Requires: " + col);

                                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Added: " + resInternalname));
                                        neu.manager.writeJsonDefaultDir(json, resInternalname + ".json");
                                        neu.manager.loadItem(resInternalname);
                                    }
                                }
                            }

                            /*JsonArray arr = null;
                            File f = new File(neu.manager.configLocation, "missing.json");
                            try(InputStream instream = new FileInputStream(f)) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(instream, StandardCharsets.UTF_8));
                                JsonObject json = neu.manager.gson.fromJson(reader, JsonObject.class);
                                arr = json.getAsJsonArray("missing");
                            } catch(IOException e) {}
                            try {
                                JsonObject json = new JsonObject();
                                JsonArray newArr = new JsonArray();
                                for(JsonElement e : arr) {
                                    if(!e.getAsString().equals(resInternalname)) {
                                        newArr.add(e);
                                    }
                                }
                                json.add("missing", newArr);
                                neu.manager.writeJson(json, f);
                            } catch(IOException e) {}*/



                            /*JsonObject recipe = new JsonObject();

                            String[] x = {"1","2","3"};
                            String[] y = {"A","B","C"};

                            for(int i=0; i<=18; i+=9) {
                                for(int j=0; j<3; j++) {
                                    ItemStack stack = lower.getStackInSlot(10+i+j);
                                    String internalname = "";
                                    if(stack != null) {
                                        internalname = neu.manager.getInternalNameForItem(stack);
                                        if(!neu.manager.getItemInformation().containsKey(internalname)) {
                                            neu.manager.writeItemToFile(stack);
                                        }
                                        internalname += ":"+stack.stackSize;
                                    }
                                    recipe.addProperty(y[i/9]+x[j], internalname);
                                }
                            }

                            JsonObject json = neu.manager.getJsonForItem(res);
                            json.add("recipe", recipe);
                            json.addProperty("internalname", resInternalname);
                            json.addProperty("clickcommand", "viewrecipe");
                            json.addProperty("modver", NotEnoughUpdates.VERSION);

                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Added: " + resInternalname));
                            neu.manager.writeJsonDefaultDir(json, resInternalname+".json");
                            neu.manager.loadItem(resInternalname);*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 200, TimeUnit.MILLISECONDS);
            }
        }
    }

    /*@SubscribeEvent
    public void onPlayerInteract(EntityInteractEvent event) {
        if(!event.isCanceled() && NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() &&
                Minecraft.getMinecraft().thePlayer.isSneaking() &&
                Minecraft.getMinecraft().ingameGUI != null) {
            if(event.target instanceof EntityPlayer) {
                for(NetworkPlayerInfo info : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
                    String name = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(info);
                    if(name.contains("Status: "+EnumChatFormatting.RESET+EnumChatFormatting.BLUE+"Guest")) {
                        NotEnoughUpdates.INSTANCE.sendChatMessage("/trade " + event.target.getName());
                        event.setCanceled(true);
                        break;
                    }
                }
            }
        }
    }*/

    private IChatComponent processChatComponent(IChatComponent chatComponent) {
        IChatComponent newComponent;
        if (chatComponent instanceof ChatComponentText) {
            ChatComponentText text = (ChatComponentText) chatComponent;

            newComponent = new ChatComponentText(processText(text.getUnformattedTextForChat()));
            newComponent.setChatStyle(text.getChatStyle().createShallowCopy());

            for (IChatComponent sibling : text.getSiblings()) {
                newComponent.appendSibling(processChatComponent(sibling));
            }
        } else if (chatComponent instanceof ChatComponentTranslation) {
            ChatComponentTranslation trans = (ChatComponentTranslation) chatComponent;

            Object[] args = trans.getFormatArgs();
            Object[] newArgs = new Object[args.length];
            for (int i = 0; i < trans.getFormatArgs().length; i++) {
                if (args[i] instanceof IChatComponent) {
                    newArgs[i] = processChatComponent((IChatComponent) args[i]);
                } else {
                    newArgs[i] = args[i];
                }
            }
            newComponent = new ChatComponentTranslation(trans.getKey(), newArgs);

            for (IChatComponent sibling : trans.getSiblings()) {
                newComponent.appendSibling(processChatComponent(sibling));
            }
        } else {
            newComponent = chatComponent.createCopy();
        }

        return newComponent;
    }

    private String processText(String text) {
        if (SBInfo.getInstance().getLocation() == null) return text;
        if (!SBInfo.getInstance().getLocation().startsWith("mining_") && !SBInfo.getInstance().getLocation().equals("crystal_hollows"))
            return text;

        if (Minecraft.getMinecraft().thePlayer == null) return text;
        if (!NotEnoughUpdates.INSTANCE.config.mining.drillFuelBar) return text;

        return Utils.trimIgnoreColour(text.replaceAll(EnumChatFormatting.DARK_GREEN + "\\S+ Drill Fuel", ""));
    }

    private IChatComponent replaceSocialControlsWithPV(IChatComponent chatComponent) {

        if (NotEnoughUpdates.INSTANCE.config.misc.replaceSocialOptions1 > 0 && chatComponent.getChatStyle() != null && chatComponent.getChatStyle().getChatClickEvent() != null && chatComponent.getChatStyle().getChatClickEvent().getAction() == ClickEvent.Action.RUN_COMMAND && NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
            if (chatComponent.getChatStyle().getChatClickEvent().getValue().startsWith("/socialoptions")) {
                String username = chatComponent.getChatStyle().getChatClickEvent().getValue().substring(15);
                if (NotEnoughUpdates.INSTANCE.config.misc.replaceSocialOptions1 == 1) {
                    chatComponent.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND, "/pv " + username, "" + EnumChatFormatting.YELLOW + "Click to open " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + username + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + "'s profile in " + EnumChatFormatting.DARK_PURPLE + EnumChatFormatting.BOLD + "NEU's" + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + " profile viewer."));
                    return chatComponent;
                } else if (NotEnoughUpdates.INSTANCE.config.misc.replaceSocialOptions1 == 2) {
                    chatComponent.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND, "/ah " + username, "" + EnumChatFormatting.YELLOW + "Click to open " + EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + username + EnumChatFormatting.RESET + EnumChatFormatting.YELLOW + "'s /ah page"));
                    return chatComponent;
                }
            } // wanted to add this for guild but guild uses uuid :sad:
        }
        return chatComponent;
    }

    /**
     * 1) When receiving "You are playing on profile" messages, will set the current profile.
     * 2) When a /viewrecipe command fails (i.e. player does not have recipe unlocked, will open the custom recipe GUI)
     * 3) Replaces lobby join notifications when streamer mode is active
     */
    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    public void onGuiChat(ClientChatReceivedEvent e) {
        if (e.type == 2) {
            CrystalMetalDetectorSolver.process(e.message);
            e.message = processChatComponent(e.message);
            return;
        } else if (e.type == 0) {
            e.message = replaceSocialControlsWithPV(e.message);
        }

        DungeonWin.onChatMessage(e);

        String r = null;
        String unformatted = Utils.cleanColour(e.message.getUnformattedText());
        Matcher matcher = SLAYER_XP.matcher(unformatted);
        if (unformatted.startsWith("You are playing on profile: ")) {
            neu.manager.setCurrentProfile(unformatted.substring("You are playing on profile: ".length()).split(" ")[0].trim());
        } else if (unformatted.startsWith("Your profile was changed to: ")) {//Your profile was changed to:
            neu.manager.setCurrentProfile(unformatted.substring("Your profile was changed to: ".length()).split(" ")[0].trim());
        } else if (unformatted.startsWith("Your new API key is ")) {
            NotEnoughUpdates.INSTANCE.config.apiKey.apiKey = unformatted.substring("Your new API key is ".length());
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
                    "[NEU] API Key automatically configured"));
            NotEnoughUpdates.INSTANCE.config.apiKey.apiKey = NotEnoughUpdates.INSTANCE.config.apiKey.apiKey.substring(0, 36);
        } else if (unformatted.startsWith("Player List Info is now disabled!")) {
            SBInfo.getInstance().hasNewTab = false;
        } else if (unformatted.startsWith("Player List Info is now enabled!")) {
            SBInfo.getInstance().hasNewTab = true;
        }
        if (e.message.getFormattedText().equals(EnumChatFormatting.RESET.toString() +
                EnumChatFormatting.RED + "You haven't unlocked this recipe!" + EnumChatFormatting.RESET)) {
            r = EnumChatFormatting.RED + "You haven't unlocked this recipe!";
        } else if (e.message.getFormattedText().startsWith(EnumChatFormatting.RESET.toString() +
                EnumChatFormatting.RED + "Invalid recipe ")) {
            r = "";
        } else if (unformatted.equals("  NICE! SLAYER BOSS SLAIN!")) {
            SlayerOverlay.isSlain = true;
        } else if (unformatted.equals("  SLAYER QUEST STARTED!")) {
            SlayerOverlay.isSlain = false;
            if (timeSinceLastBoss == 0) {
                SlayerOverlay.timeSinceLastBoss = System.currentTimeMillis();
            } else {
                timeSinceLastBoss2 = timeSinceLastBoss;
                timeSinceLastBoss = System.currentTimeMillis();
            }
        } else if (unformatted.startsWith("   RNGesus Meter:")) {
            RNGMeter = unformatted.substring("   RNGesus Meter: -------------------- ".length());
        } else if (matcher.matches()) {
            //matcher.group(1);
            SlayerOverlay.slayerLVL = matcher.group(2);
            if (!SlayerOverlay.slayerLVL.equals("9")) {
                SlayerOverlay.slayerXp = matcher.group(3);
            } else {
                slayerXp = "maxed";
            }
        } else if (unformatted.startsWith("Sending to server") || (unformatted.startsWith("Your Slayer Quest has been cancelled!"))) {
            SlayerOverlay.slayerQuest = false;
            SlayerOverlay.unloadOverlayTimer = System.currentTimeMillis();
        }
        if (e.message.getFormattedText().contains(EnumChatFormatting.YELLOW + "Visit the Auction House to collect your item!")) {
            if (NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.latestBid != null &&
                    System.currentTimeMillis() - NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.latestBidMillis < 5000) {
                NotEnoughUpdates.INSTANCE.sendChatMessage("/viewauction " +
                        NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.niceAucId(
                                NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.latestBid));
            }
        }
        if (r != null) {
            if (neu.manager.failViewItem(r)) {
                e.setCanceled(true);
            }
            missingRecipe.set(true);
        }
        //System.out.println(e.message);
        if (unformatted.startsWith("Sending to server") &&
                neu.isOnSkyblock() && NotEnoughUpdates.INSTANCE.config.misc.streamerMode && e.message instanceof ChatComponentText) {
            String m = e.message.getFormattedText();
            String m2 = StreamerMode.filterChat(e.message.getFormattedText());
            if (!m.equals(m2)) {
                e.message = new ChatComponentText(m2);
            }
        }
        if (unformatted.startsWith("You found ") && SBInfo.getInstance().getLocation() != null && SBInfo.getInstance().getLocation().equals("crystal_hollows")) {
            CrystalMetalDetectorSolver.reset(true);
        }
        if (unformatted.startsWith("[NPC] Keeper of ") | unformatted.startsWith("[NPC] Professor Robot: ") || unformatted.startsWith("  ") || unformatted.startsWith("✦") ||
                unformatted.equals("  You've earned a Crystal Loot Bundle!"))
            OverlayManager.crystalHollowOverlay.message(unformatted);
    }

    public static boolean drawingGuiScreen = false;

    /**
     * Sets hoverInv and focusInv variables, representing whether the NEUOverlay should render behind the inventory when
     * (hoverInv == true) and whether mouse/kbd inputs shouldn't be sent to NEUOverlay (focusInv == true).
     * <p>
     * If hoverInv is true, will render the overlay immediately (resulting in the inventory being drawn over the GUI)
     * If hoverInv is false, the overlay will render in #onGuiScreenDraw (resulting in the GUI being drawn over the inv)
     * <p>
     * All of this only matters if players are using gui scale auto which may result in the inventory being drawn
     * over the various panes.
     */
    @SubscribeEvent
    public void onGuiBackgroundDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (showNotificationOverInv) {

            renderNotification();

        }
        if ((shouldRenderOverlay(event.gui) || event.gui instanceof CustomAHGui) && neu.isOnSkyblock()) {
            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int width = scaledresolution.getScaledWidth();

            boolean hoverPane = event.getMouseX() < width * neu.overlay.getInfoPaneOffsetFactor() ||
                    event.getMouseX() > width * neu.overlay.getItemPaneOffsetFactor();

            if (event.gui instanceof GuiContainer) {
                try {
                    int xSize = ((GuiContainer) event.gui).xSize;
                    int ySize = ((GuiContainer) event.gui).ySize;
                    int guiLeft = ((GuiContainer) event.gui).guiLeft;
                    int guiTop = ((GuiContainer) event.gui).guiTop;

                    hoverInv = event.getMouseX() > guiLeft && event.getMouseX() < guiLeft + xSize &&
                            event.getMouseY() > guiTop && event.getMouseY() < guiTop + ySize;

                    if (hoverPane) {
                        if (!hoverInv) focusInv = false;
                    } else {
                        focusInv = true;
                    }
                } catch (NullPointerException npe) {
                    focusInv = !hoverPane;
                }
            }
            if (event.gui instanceof GuiItemRecipe) {
                GuiItemRecipe guiItemRecipe = ((GuiItemRecipe) event.gui);
                hoverInv = event.getMouseX() > guiItemRecipe.guiLeft && event.getMouseX() < guiItemRecipe.guiLeft + guiItemRecipe.xSize &&
                        event.getMouseY() > guiItemRecipe.guiTop && event.getMouseY() < guiItemRecipe.guiTop + guiItemRecipe.ySize;

                if (hoverPane) {
                    if (!hoverInv) focusInv = false;
                } else {
                    focusInv = true;
                }
            }
            if (focusInv) {
                try {
                    neu.overlay.render(hoverInv);
                } catch (ConcurrentModificationException e) {
                    e.printStackTrace();
                }
                GL11.glTranslatef(0, 0, 10);
            }
            if (hoverInv) {
                renderDungeonChestOverlay(event.gui);
                if (NotEnoughUpdates.INSTANCE.config.accessoryBag.enableOverlay) {
                    AccessoryBagOverlay.renderOverlay();
                }
            }
        }

        drawingGuiScreen = true;
    }

    private boolean doInventoryButtons = false;

    @SubscribeEvent
    public void onGuiScreenDrawPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        doInventoryButtons = false;

        if (AuctionSearchOverlay.shouldReplace()) {
            AuctionSearchOverlay.render();
            event.setCanceled(true);
            return;
        }
        if (RancherBootOverlay.shouldReplace()) {
            RancherBootOverlay.render();
            event.setCanceled(true);
            return;
        }

        String containerName = null;
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (guiScreen instanceof GuiChest) {
            GuiChest eventGui = (GuiChest) guiScreen;
            ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
            containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
        }

        if (GuiCustomEnchant.getInstance().shouldOverride(containerName)) {
            GuiCustomEnchant.getInstance().render(event.renderPartialTicks);
            event.setCanceled(true);
            return;
        }

        boolean tradeWindowActive = TradeWindow.tradeWindowActive(containerName);
        boolean storageOverlayActive = StorageManager.getInstance().shouldRenderStorageOverlay(containerName);
        boolean customAhActive = event.gui instanceof CustomAHGui || neu.manager.auctionManager.customAH.isRenderOverAuctionView();

        if (storageOverlayActive) {
            StorageOverlay.getInstance().render();
            event.setCanceled(true);
            return;
        }

        if (tradeWindowActive || customAhActive) {
            event.setCanceled(true);

            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            int width = scaledResolution.getScaledWidth();
            int height = scaledResolution.getScaledHeight();

            //Dark background
            Utils.drawGradientRect(0, 0, width, height, -1072689136, -804253680);

            if (event.mouseX < width * neu.overlay.getWidthMult() / 3 || event.mouseX > width - width * neu.overlay.getWidthMult() / 3) {
                if (customAhActive) {
                    neu.manager.auctionManager.customAH.drawScreen(event.mouseX, event.mouseY);
                } else if (tradeWindowActive) {
                    TradeWindow.render(event.mouseX, event.mouseY);
                }
                neu.overlay.render(false);
            } else {
                neu.overlay.render(false);
                if (customAhActive) {
                    neu.manager.auctionManager.customAH.drawScreen(event.mouseX, event.mouseY);
                } else if (tradeWindowActive) {
                    TradeWindow.render(event.mouseX, event.mouseY);
                }
            }
        }

        if (CalendarOverlay.isEnabled() || event.isCanceled()) return;
        if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() && shouldRenderOverlay(event.gui)
                && event.gui instanceof GuiContainer) {
            doInventoryButtons = true;

            int zOffset = 50;

            GlStateManager.translate(0, 0, zOffset);

            int xSize = ((GuiContainer) event.gui).xSize;
            int ySize = ((GuiContainer) event.gui).ySize;
            int guiLeft = ((GuiContainer) event.gui).guiLeft;
            int guiTop = ((GuiContainer) event.gui).guiTop;

            if (!NEUApi.disableInventoryButtons) {
                for (NEUConfig.InventoryButton button : NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons) {
                    if (!button.isActive()) continue;
                    if (button.playerInvOnly && !(event.gui instanceof GuiInventory)) continue;

                    int x = guiLeft + button.x;
                    int y = guiTop + button.y;
                    if (button.anchorRight) {
                        x += xSize;
                    }
                    if (button.anchorBottom) {
                        y += ySize;
                    }
                    if (AccessoryBagOverlay.isInAccessoryBag()) {
                        if (x > guiLeft + xSize && x < guiLeft + xSize + 80 + 28 + 5 && y > guiTop - 18 && y < guiTop + 150) {
                            x += 80 + 28;
                        }
                    }
                    if (NEUOverlay.isRenderingArmorHud()) {
                        if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop && y < guiTop + 84) {
                            x -= 25;
                        }
                    }
                    if (NEUOverlay.isRenderingPetHud()) {
                        if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop + 60 && y < guiTop + 120) {
                            x -= 25;
                        }
                    }

                    GlStateManager.color(1, 1, 1, 1f);

                    GlStateManager.enableDepth();
                    GlStateManager.enableAlpha();
                    Minecraft.getMinecraft().getTextureManager().bindTexture(EDITOR);
                    Utils.drawTexturedRect(x, y, 18, 18,
                            button.backgroundIndex * 18 / 256f, (button.backgroundIndex * 18 + 18) / 256f,
                            18 / 256f, 36 / 256f, GL11.GL_NEAREST);

                    if (button.icon != null && !button.icon.trim().isEmpty()) {
                        GuiInvButtonEditor.renderIcon(button.icon, x + 1, y + 1);
                    }
                }
            }
            GlStateManager.translate(0, 0, -zOffset);
        }
    }

    private static boolean shouldRenderOverlay(Gui gui) {
        boolean validGui = gui instanceof GuiContainer || gui instanceof GuiItemRecipe;
        if (gui instanceof GuiChest) {
            GuiChest eventGui = (GuiChest) gui;
            ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
            String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
            if (containerName.trim().equals("Fast Travel")) {
                validGui = false;
            }
        }
        return validGui;
    }

    private static final ResourceLocation EDITOR = new ResourceLocation("notenoughupdates:invbuttons/editor.png");
    private NEUConfig.InventoryButton buttonHovered = null;
    private long buttonHoveredMillis = 0;
    public static boolean disableCraftingText = false;

    /**
     * Will draw the NEUOverlay over the inventory if focusInv == false. (z-translation of 300 is so that NEUOverlay
     * will draw over Items in the inventory (which render at a z value of about 250))
     */
    @SubscribeEvent
    public void onGuiScreenDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        drawingGuiScreen = false;
        disableCraftingText = false;

        String containerName = null;
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (guiScreen instanceof GuiChest) {
            GuiChest eventGui = (GuiChest) guiScreen;
            ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
            containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();

            if (GuiCustomEnchant.getInstance().shouldOverride(containerName))
                return;
        }

        boolean tradeWindowActive = TradeWindow.tradeWindowActive(containerName);
        boolean storageOverlayActive = StorageManager.getInstance().shouldRenderStorageOverlay(containerName);
        boolean customAhActive = event.gui instanceof CustomAHGui || neu.manager.auctionManager.customAH.isRenderOverAuctionView();

        if (!(tradeWindowActive || storageOverlayActive || customAhActive)) {
            if (shouldRenderOverlay(event.gui) && neu.isOnSkyblock()) {
                GlStateManager.pushMatrix();
                if (!focusInv) {
                    GL11.glTranslatef(0, 0, 300);
                    neu.overlay.render(hoverInv && focusInv);
                    GL11.glTranslatef(0, 0, -300);
                }
                GlStateManager.popMatrix();
            }
        }

        if (shouldRenderOverlay(event.gui) && neu.isOnSkyblock() && !hoverInv) {
            renderDungeonChestOverlay(event.gui);
            if (NotEnoughUpdates.INSTANCE.config.accessoryBag.enableOverlay) {
                AccessoryBagOverlay.renderOverlay();
            }
        }

        boolean hoveringButton = false;
        if (!doInventoryButtons) return;
        if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() && shouldRenderOverlay(event.gui) &&
                event.gui instanceof GuiContainer) {
            int xSize = ((GuiContainer) event.gui).xSize;
            int ySize = ((GuiContainer) event.gui).ySize;
            int guiLeft = ((GuiContainer) event.gui).guiLeft;
            int guiTop = ((GuiContainer) event.gui).guiTop;

            if (!NEUApi.disableInventoryButtons) {
                for (NEUConfig.InventoryButton button : NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons) {
                    if (!button.isActive()) continue;
                    if (button.playerInvOnly && !(event.gui instanceof GuiInventory)) continue;

                    int x = guiLeft + button.x;
                    int y = guiTop + button.y;
                    if (button.anchorRight) {
                        x += xSize;
                    }
                    if (button.anchorBottom) {
                        y += ySize;
                    }
                    if (AccessoryBagOverlay.isInAccessoryBag()) {
                        if (x > guiLeft + xSize && x < guiLeft + xSize + 80 + 28 + 5 && y > guiTop - 18 && y < guiTop + 150) {
                            x += 80 + 28;
                        }
                    }
                    if (NEUOverlay.isRenderingArmorHud()) {
                        if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop && y < guiTop + 84) {
                            x -= 25;
                        }
                    }
                    if (NEUOverlay.isRenderingPetHud()) {
                        if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop + 60 && y < guiTop + 120) {
                            x -= 25;
                        }
                    }

                    if (x - guiLeft >= 85 && x - guiLeft <= 115 && y - guiTop >= 4 && y - guiTop <= 25) {
                        disableCraftingText = true;
                    }

                    if (event.mouseX >= x && event.mouseX <= x + 18 &&
                            event.mouseY >= y && event.mouseY <= y + 18) {
                        hoveringButton = true;
                        long currentTime = System.currentTimeMillis();

                        if (buttonHovered != button) {
                            buttonHoveredMillis = currentTime;
                            buttonHovered = button;
                        }

                        if (currentTime - buttonHoveredMillis > 600) {
                            String command = button.command.trim();
                            if (!command.startsWith("/")) {
                                command = "/" + command;
                            }

                            Utils.drawHoveringText(Lists.newArrayList("\u00a77" + command), event.mouseX, event.mouseY,
                                    event.gui.width, event.gui.height, -1, Minecraft.getMinecraft().fontRendererObj);
                        }
                    }
                }
            }
        }
        if (!hoveringButton) buttonHovered = null;

        if (AuctionBINWarning.getInstance().shouldShow()) {
            AuctionBINWarning.getInstance().render();
        }
    }

    private void renderDungeonChestOverlay(GuiScreen gui) {
        if (NotEnoughUpdates.INSTANCE.config.dungeons.profitDisplayLoc == 3) return;

        if (gui instanceof GuiChest && NotEnoughUpdates.INSTANCE.config.dungeons.profitDisplayLoc != 2) {
            try {
                int xSize = ((GuiContainer) gui).xSize;
                int ySize = ((GuiContainer) gui).ySize;
                int guiLeft = ((GuiContainer) gui).guiLeft;
                int guiTop = ((GuiContainer) gui).guiTop;

                GuiChest eventGui = (GuiChest) gui;
                ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
                IInventory lower = cc.getLowerChestInventory();

                ItemStack rewardChest = lower.getStackInSlot(31);
                if (rewardChest != null && rewardChest.getDisplayName().endsWith(EnumChatFormatting.GREEN + "Open Reward Chest")) {
                    int chestCost = 0;
                    try {
                        String line6 = Utils.cleanColour(neu.manager.getLoreFromNBT(rewardChest.getTagCompound())[6]);
                        StringBuilder cost = new StringBuilder();
                        for (int i = 0; i < line6.length(); i++) {
                            char c = line6.charAt(i);
                            if ("0123456789".indexOf(c) >= 0) {
                                cost.append(c);
                            }
                        }
                        if (cost.length() > 0) {
                            chestCost = Integer.parseInt(cost.toString());
                        }
                    } catch (Exception ignored) {}

                    String missingItem = null;
                    int totalValue = 0;
                    HashMap<String, Float> itemValues = new HashMap<>();
                    for (int i = 0; i < 5; i++) {
                        ItemStack item = lower.getStackInSlot(11 + i);
                        String internal = neu.manager.getInternalNameForItem(item);
                        if (internal != null) {
                            internal = internal.replace("\u00CD", "I").replace("\u0130", "I");
                            float bazaarPrice = -1;
                            JsonObject bazaarInfo = neu.manager.auctionManager.getBazaarInfo(internal);
                            if (bazaarInfo != null && bazaarInfo.has("curr_sell")) {
                                bazaarPrice = bazaarInfo.get("curr_sell").getAsFloat();
                            }
                            if (bazaarPrice < 5000000 && internal.equals("RECOMBOBULATOR_3000")) bazaarPrice = 5000000;

                            float worth = -1;
                            if (bazaarPrice > 0) {
                                worth = bazaarPrice;
                            } else {
                                switch (NotEnoughUpdates.INSTANCE.config.dungeons.profitType) {
                                    case 1:
                                        worth = neu.manager.auctionManager.getItemAvgBin(internal);
                                        break;
                                    case 2:
                                        JsonObject auctionInfo = neu.manager.auctionManager.getItemAuctionInfo(internal);
                                        if (auctionInfo != null) {
                                            if (auctionInfo.has("clean_price")) {
                                                worth = (int) auctionInfo.get("clean_price").getAsFloat();
                                            } else {
                                                worth = (int) (auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsFloat());
                                            }
                                        }
                                        break;
                                    default:
                                        worth = neu.manager.auctionManager.getLowestBin(internal);
                                }
                                if (worth <= 0) {
                                    worth = neu.manager.auctionManager.getLowestBin(internal);
                                    if (worth <= 0) {
                                        worth = neu.manager.auctionManager.getItemAvgBin(internal);
                                        if (worth <= 0) {
                                            JsonObject auctionInfo = neu.manager.auctionManager.getItemAuctionInfo(internal);
                                            if (auctionInfo != null) {
                                                if (auctionInfo.has("clean_price")) {
                                                    worth = (int) auctionInfo.get("clean_price").getAsFloat();
                                                } else {
                                                    worth = (int) (auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsFloat());
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (worth > 0 && totalValue >= 0) {
                                totalValue += worth;
                                String display = item.getDisplayName();

                                if (display.contains("Enchanted Book")) {
                                    NBTTagCompound tag = item.getTagCompound();
                                    if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
                                        NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
                                        NBTTagCompound enchants = ea.getCompoundTag("enchantments");

                                        int highestLevel = -1;
                                        for (String enchname : enchants.getKeySet()) {
                                            int level = enchants.getInteger(enchname);
                                            if (level > highestLevel) {
                                                display = EnumChatFormatting.BLUE + WordUtils.capitalizeFully(
                                                        enchname.replace("_", " ")
                                                                .replace("Ultimate", "")
                                                                .trim()) + " " + level;
                                            }
                                        }
                                    }
                                }

                                itemValues.put(display, worth);
                            } else {
                                if (totalValue != -1) {
                                    missingItem = internal;
                                }
                                totalValue = -1;
                            }
                        }
                    }

                    NumberFormat format = NumberFormat.getInstance(Locale.US);
                    String valueStringBIN1;
                    String valueStringBIN2;
                    if (totalValue >= 0) {
                        valueStringBIN1 = EnumChatFormatting.YELLOW + "Value (BIN): ";
                        valueStringBIN2 = EnumChatFormatting.GOLD + format.format(totalValue) + " coins";
                    } else {
                        valueStringBIN1 = EnumChatFormatting.YELLOW + "Can't find BIN: ";
                        valueStringBIN2 = missingItem;
                    }

                    int profitLossBIN = totalValue - chestCost;

                    boolean kismetUsed = false;
                    // checking for kismet
                    Slot slot = (eventGui.inventorySlots.getSlot(50));
                    if(slot.getHasStack()) {
                        String[] lore = NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(slot.getStack().getTagCompound());
                        for (String line : lore) {
                            if (line.contains("You already rerolled a chest!")) {
                                kismetUsed = true;
                                break;
                            }
                        }
                    }
                    int kismetPrice = neu.manager.auctionManager.getLowestBin("KISMET_FEATHER");
                    String kismetStr = EnumChatFormatting.RED + format.format(kismetPrice) + " coins";
                    if(neu.config.dungeons.useKismetOnDungeonProfit)
                    profitLossBIN = kismetUsed ? profitLossBIN-kismetPrice : profitLossBIN;

                    String profitPrefix = EnumChatFormatting.DARK_GREEN.toString();
                    String lossPrefix = EnumChatFormatting.RED.toString();
                    String prefix = profitLossBIN >= 0 ? profitPrefix : lossPrefix;

                    String plStringBIN;
                    if (profitLossBIN >= 0) {
                        plStringBIN = prefix + "+" + format.format(profitLossBIN) + " coins";
                    } else {
                        plStringBIN = prefix + "-" + format.format(-profitLossBIN) + " coins";
                    }

                    if (NotEnoughUpdates.INSTANCE.config.dungeons.profitDisplayLoc == 1 && !valueStringBIN2.equals(missingItem)) {
                        int w = Minecraft.getMinecraft().fontRendererObj.getStringWidth(plStringBIN);
                        GlStateManager.disableLighting();
                        GlStateManager.translate(0, 0, 200);
                        Minecraft.getMinecraft().fontRendererObj.drawString(plStringBIN, guiLeft + xSize - 5 - w, guiTop + 5,
                                0xffffffff, true);
                        GlStateManager.translate(0, 0, -200);
                        return;
                    }

                    Minecraft.getMinecraft().getTextureManager().bindTexture(dungeon_chest_worth);
                    GL11.glColor4f(1, 1, 1, 1);
                    GlStateManager.disableLighting();
                    Utils.drawTexturedRect(guiLeft + xSize + 4, guiTop, 180, 101, 0, 180 / 256f, 0, 101 / 256f, GL11.GL_NEAREST);

                    Utils.renderAlignedString(valueStringBIN1, valueStringBIN2,
                            guiLeft + xSize + 4 + 10, guiTop + 14, 160);
                    if (neu.config.dungeons.useKismetOnDungeonProfit && kismetUsed) {
                        Utils.renderAlignedString(EnumChatFormatting.YELLOW + "Kismet Feather: ", kismetStr,
                                guiLeft + xSize + 4 + 10, guiTop + 24, 160);
                    }
                    if (totalValue >= 0) {
                        Utils.renderAlignedString(EnumChatFormatting.YELLOW + "Profit/Loss: ", plStringBIN,
                                guiLeft + xSize + 4 + 10, guiTop + (neu.config.dungeons.useKismetOnDungeonProfit ? (kismetUsed ? 34 : 24) : 24) , 160);
                    }

                    int index = 0;
                    for (Map.Entry<String, Float> entry : itemValues.entrySet()) {
                        Utils.renderAlignedString(entry.getKey(), prefix +
                                        format.format(entry.getValue().intValue()),
                                guiLeft + xSize + 4 + 10, guiTop + (neu.config.dungeons.useKismetOnDungeonProfit ?  (kismetUsed ? 39 :29) : 29) + (++index) * 10, 160);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void drawStringShadow(String str, float x, float y, int len) {
        for (int xOff = -2; xOff <= 2; xOff++) {
            for (int yOff = -2; yOff <= 2; yOff++) {
                if (Math.abs(xOff) != Math.abs(yOff)) {
                    Utils.drawStringCenteredScaledMaxWidth(Utils.cleanColourNotModifiers(str),
                            Minecraft.getMinecraft().fontRendererObj,
                            x + xOff / 2f, y + yOff / 2f, false, len,
                            new Color(20, 20, 20, 100 / Math.max(Math.abs(xOff), Math.abs(yOff))).getRGB());
                }
            }
        }

        Utils.drawStringCenteredScaledMaxWidth(str,
                Minecraft.getMinecraft().fontRendererObj,
                x, y, false, len,
                new Color(64, 64, 64, 255).getRGB());
    }

    /**
     * Sends a mouse event to NEUOverlay if the inventory isn't hovered AND focused.
     * Will also cancel the event if if NEUOverlay#mouseInput returns true.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiScreenMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (Mouse.getEventButtonState() && StorageManager.getInstance().onAnyClick()) {
            event.setCanceled(true);
            return;
        }

        final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        final int scaledWidth = scaledresolution.getScaledWidth();
        final int scaledHeight = scaledresolution.getScaledHeight();
        int mouseX = Mouse.getX() * scaledWidth / Minecraft.getMinecraft().displayWidth;
        int mouseY = scaledHeight - Mouse.getY() * scaledHeight / Minecraft.getMinecraft().displayHeight - 1;

        if (AuctionBINWarning.getInstance().shouldShow()) {
            AuctionBINWarning.getInstance().mouseInput(mouseX, mouseY);
            event.setCanceled(true);
            return;
        }

        if (!event.isCanceled()) {
            Utils.scrollTooltip(Mouse.getEventDWheel());
        }
        if (AuctionSearchOverlay.shouldReplace()) {
            AuctionSearchOverlay.mouseEvent();
            event.setCanceled(true);
            return;
        }
        if (RancherBootOverlay.shouldReplace()) {
            RancherBootOverlay.mouseEvent();
            event.setCanceled(true);
            return;
        }

        String containerName = null;
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (guiScreen instanceof GuiChest) {
            GuiChest eventGui = (GuiChest) guiScreen;
            ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
            containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
            if (containerName.contains(" Profile") && BetterContainers.profileViewerStackIndex != -1 &&
                    eventGui.isMouseOverSlot(cc.inventorySlots.get(BetterContainers.profileViewerStackIndex), mouseX, mouseY) && Mouse.getEventButton() >= 0) {
                event.setCanceled(true);
                if (Mouse.getEventButtonState() && eventGui.inventorySlots.inventorySlots.get(22).getStack() != null &&
                        eventGui.inventorySlots.inventorySlots.get(22).getStack().getTagCompound() != null) {
                    NBTTagCompound tag = eventGui.inventorySlots.inventorySlots.get(22).getStack().getTagCompound();
                    if (tag.hasKey("SkullOwner") && tag.getCompoundTag("SkullOwner").hasKey("Name")) {
                        String username = tag.getCompoundTag("SkullOwner").getString("Name");
                        Utils.playPressSound();
                        ViewProfileCommand.RUNNABLE.accept(new String[]{username});
                    }
                }
            }
        }

        if (GuiCustomEnchant.getInstance().shouldOverride(containerName) &&
                GuiCustomEnchant.getInstance().mouseInput(mouseX, mouseY)) {
            event.setCanceled(true);
            return;
        }

        boolean tradeWindowActive = TradeWindow.tradeWindowActive(containerName);
        boolean storageOverlayActive = StorageManager.getInstance().shouldRenderStorageOverlay(containerName);
        boolean customAhActive = event.gui instanceof CustomAHGui || neu.manager.auctionManager.customAH.isRenderOverAuctionView();

        if (storageOverlayActive) {
            if (StorageOverlay.getInstance().mouseInput(mouseX, mouseY)) {
                event.setCanceled(true);
            }
            return;
        }

        if (tradeWindowActive || customAhActive) {
            event.setCanceled(true);
            if (customAhActive) {
                neu.manager.auctionManager.customAH.handleMouseInput();
            } else if (tradeWindowActive) {
                TradeWindow.handleMouseInput();
            }
            neu.overlay.mouseInput();
            return;
        }

        if (shouldRenderOverlay(event.gui) && neu.isOnSkyblock()) {
            if (!NotEnoughUpdates.INSTANCE.config.accessoryBag.enableOverlay || !AccessoryBagOverlay.mouseClick()) {
                if (!(hoverInv && focusInv)) {
                    if (neu.overlay.mouseInput()) {
                        event.setCanceled(true);
                    }
                } else {
                    neu.overlay.mouseInputInv();
                }
            }
        }
        if (event.isCanceled()) return;
        if (!doInventoryButtons) return;
        if (NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard() && shouldRenderOverlay(event.gui) && Mouse.getEventButton() >= 0
                && event.gui instanceof GuiContainer) {
            int xSize = ((GuiContainer) event.gui).xSize;
            int ySize = ((GuiContainer) event.gui).ySize;
            int guiLeft = ((GuiContainer) event.gui).guiLeft;
            int guiTop = ((GuiContainer) event.gui).guiTop;
            if (!NEUApi.disableInventoryButtons) {
                for (NEUConfig.InventoryButton button : NotEnoughUpdates.INSTANCE.config.hidden.inventoryButtons) {
                    if (!button.isActive()) continue;
                    if (button.playerInvOnly && !(event.gui instanceof GuiInventory)) continue;

                    int x = guiLeft + button.x;
                    int y = guiTop + button.y;
                    if (button.anchorRight) {
                        x += xSize;
                    }
                    if (button.anchorBottom) {
                        y += ySize;
                    }
                    if (AccessoryBagOverlay.isInAccessoryBag()) {
                        if (x > guiLeft + xSize && x < guiLeft + xSize + 80 + 28 + 5 && y > guiTop - 18 && y < guiTop + 150) {
                            x += 80 + 28;
                        }
                    }
                    if (NEUOverlay.isRenderingArmorHud()) {
                        if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop && y < guiTop + 84) {
                            x -= 25;
                        }
                    }
                    if (NEUOverlay.isRenderingPetHud()) {
                        if (x < guiLeft + xSize - 150 && x > guiLeft + xSize - 200 && y > guiTop + 60 && y < guiTop + 120) {
                            x -= 25;
                        }
                    }

                    if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18) {
                        if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
                            int clickType = NotEnoughUpdates.INSTANCE.config.inventoryButtons.clickType;
                            if ((clickType == 0 && Mouse.getEventButtonState()) || (clickType == 1 && !Mouse.getEventButtonState())) {
                                String command = button.command.trim();
                                if (!command.startsWith("/")) {
                                    command = "/" + command;
                                }
                                if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, command) == 0) {
                                    NotEnoughUpdates.INSTANCE.sendChatMessage(command);
                                }
                            }
                        } else {
                            event.setCanceled(true);
                        }
                        return;
                    }
                }
            }
        }
    }

    ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    JsonObject essenceJson = new JsonObject();

    /**
     * Sends a kbd event to NEUOverlay, cancelling if NEUOverlay#keyboardInput returns true.
     * Also includes a dev function used for creating custom named json files with recipes.
     */
    @SubscribeEvent
    public void onGuiScreenKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (AuctionBINWarning.getInstance().shouldShow()) {
            AuctionBINWarning.getInstance().keyboardInput();
            event.setCanceled(true);
            return;
        }

        if (AuctionSearchOverlay.shouldReplace()) {
            AuctionSearchOverlay.keyEvent();
            event.setCanceled(true);
            return;
        }
        if (RancherBootOverlay.shouldReplace()) {
            RancherBootOverlay.keyEvent();
            event.setCanceled(true);
            return;
        }

        String containerName = null;
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;

        if (guiScreen instanceof GuiChest) {
            containerName = ((ContainerChest) ((GuiChest) guiScreen).inventorySlots).getLowerChestInventory().getDisplayName().getUnformattedText();
        }

        if (GuiCustomEnchant.getInstance().shouldOverride(containerName) &&
                GuiCustomEnchant.getInstance().keyboardInput()) {
            event.setCanceled(true);
            return;
        }

        boolean tradeWindowActive = TradeWindow.tradeWindowActive(containerName);
        boolean storageOverlayActive = StorageManager.getInstance().shouldRenderStorageOverlay(containerName);
        boolean customAhActive = event.gui instanceof CustomAHGui || neu.manager.auctionManager.customAH.isRenderOverAuctionView();

        if (storageOverlayActive) {
            if (StorageOverlay.getInstance().keyboardInput()) {
                event.setCanceled(true);
                return;
            }
        }

        if (tradeWindowActive || customAhActive) {
            if (customAhActive) {
                if (neu.manager.auctionManager.customAH.keyboardInput()) {
                    event.setCanceled(true);
                    Minecraft.getMinecraft().dispatchKeypresses();
                } else if (neu.overlay.keyboardInput(focusInv)) {
                    event.setCanceled(true);
                }
            } else if (tradeWindowActive) {
                TradeWindow.keyboardInput();
                if (Keyboard.getEventKey() != Keyboard.KEY_ESCAPE) {
                    event.setCanceled(true);
                    Minecraft.getMinecraft().dispatchKeypresses();
                    neu.overlay.keyboardInput(focusInv);
                }
            }
            return;
        }

        if (shouldRenderOverlay(event.gui) && neu.isOnSkyblock()) {
            if (neu.overlay.keyboardInput(focusInv)) {
                event.setCanceled(true);
            }
        }
        if (NotEnoughUpdates.INSTANCE.config.hidden.dev && NotEnoughUpdates.INSTANCE.config.hidden.enableItemEditing && Minecraft.getMinecraft().theWorld != null &&
                Keyboard.getEventKey() == Keyboard.KEY_N && Keyboard.getEventKeyState()) {
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof GuiChest) {
                GuiChest eventGui = (GuiChest) event.gui;
                ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
                IInventory lower = cc.getLowerChestInventory();

                if (!lower.getDisplayName().getUnformattedText().endsWith("Essence")) return;

                for (int i = 0; i < lower.getSizeInventory(); i++) {
                    ItemStack stack = lower.getStackInSlot(i);

                    String internalname = neu.manager.getInternalNameForItem(stack);
                    if (internalname != null) {
                        String[] lore = neu.manager.getLoreFromNBT(stack.getTagCompound());

                        for (String line : lore) {
                            if (line.contains(":") && (line.startsWith("\u00A77Upgrade to") ||
                                    line.startsWith("\u00A77Convert to Dungeon Item"))) {
                                String[] split = line.split(":");
                                String after = Utils.cleanColour(split[1]);
                                StringBuilder costS = new StringBuilder();
                                for (char c : after.toCharArray()) {
                                    if (c >= '0' && c <= '9') {
                                        costS.append(c);
                                    }
                                }
                                int cost = Integer.parseInt(costS.toString());
                                String[] afterSplit = after.split(" ");
                                String type = afterSplit[afterSplit.length - 2];

                                if (!essenceJson.has(internalname)) {
                                    essenceJson.add(internalname, new JsonObject());
                                }
                                JsonObject obj = essenceJson.get(internalname).getAsJsonObject();
                                obj.addProperty("type", type);

                                if (line.startsWith("\u00A77Convert to Dungeon Item")) {
                                    obj.addProperty("dungeonize", cost);
                                } else if (line.startsWith("\u00A77Upgrade to")) {
                                    int stars = 0;
                                    for (char c : line.toCharArray()) {
                                        if (c == '\u272A') stars++;
                                    }
                                    if (stars > 0) {
                                        obj.addProperty(stars + "", cost);
                                    }
                                }
                            }
                        }
                    }
                }
                System.out.println(essenceJson);
            }
        }
        if (NotEnoughUpdates.INSTANCE.config.hidden.dev && NotEnoughUpdates.INSTANCE.config.hidden.enableItemEditing && Minecraft.getMinecraft().theWorld != null &&
                Keyboard.getEventKey() == Keyboard.KEY_O && Keyboard.getEventKeyState()) {
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof GuiChest) {
                GuiChest eventGui = (GuiChest) event.gui;
                ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
                IInventory lower = cc.getLowerChestInventory();

                if (lower.getStackInSlot(23) != null &&
                        lower.getStackInSlot(23).getDisplayName().endsWith("Crafting Table")) {
                    ItemStack res = lower.getStackInSlot(25);
                    String resInternalname = neu.manager.getInternalNameForItem(res);
                    JTextField tf = new JTextField();
                    tf.setText(resInternalname);
                    tf.addAncestorListener(new RequestFocusListener());
                    JOptionPane.showOptionDialog(null,
                            tf,
                            "Enter Name:",
                            JOptionPane.NO_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null, new String[]{"Enter"}, "Enter");
                    resInternalname = tf.getText();
                    if (resInternalname.trim().length() == 0) {
                        return;
                    }

                    JsonObject recipe = new JsonObject();

                    String[] x = {"1", "2", "3"};
                    String[] y = {"A", "B", "C"};

                    for (int i = 0; i <= 18; i += 9) {
                        for (int j = 0; j < 3; j++) {
                            ItemStack stack = lower.getStackInSlot(10 + i + j);
                            String internalname = "";
                            if (stack != null) {
                                internalname = neu.manager.getInternalNameForItem(stack);
                                if (!neu.manager.getItemInformation().containsKey(internalname)) {
                                    neu.manager.writeItemToFile(stack);
                                }
                                internalname += ":" + stack.stackSize;
                            }
                            recipe.addProperty(y[i / 9] + x[j], internalname);
                        }
                    }

                    JsonObject json = neu.manager.getJsonForItem(res);
                    json.add("recipe", recipe);
                    json.addProperty("internalname", resInternalname);
                    json.addProperty("clickcommand", "viewrecipe");
                    json.addProperty("modver", NotEnoughUpdates.VERSION);
                    try {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Added: " + resInternalname));
                        neu.manager.writeJsonDefaultDir(json, resInternalname + ".json");
                        neu.manager.loadItem(resInternalname);
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    private final HashSet<String> percentStats = new HashSet<>();

    {
        percentStats.add("bonus_attack_speed");
        percentStats.add("crit_damage");
        percentStats.add("crit_chance");
        percentStats.add("sea_creature_chance");
        percentStats.add("ability_damage");
    }

    private String currentRarity = "COMMON";
    private boolean showReforgeStoneStats = true;
    private boolean pressedArrowLast = false;
    private boolean pressedShiftLast = false;

    private boolean copied = false;

    //just to try and optimize it a bit
    private int sbaloaded = -1;

    private boolean isSbaloaded() {
        if (sbaloaded == -1) {
            if (Loader.isModLoaded("skyblockaddons")) {
                sbaloaded = 1;
            } else {
                sbaloaded = 0;
            }
        }
        return sbaloaded == 1;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltipLow(ItemTooltipEvent event) {
        if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) return;

        String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(event.itemStack);
        if (internalname == null) {
            onItemToolTipInternalNameNull(event);
            return;
        }

        boolean hasEnchantments = event.itemStack.getTagCompound().getCompoundTag("ExtraAttributes").hasKey("enchantments", 10);
        Set<String> enchantIds = new HashSet<>();
        if (hasEnchantments)
            enchantIds = event.itemStack.getTagCompound().getCompoundTag("ExtraAttributes").getCompoundTag("enchantments").getKeySet();

        JsonObject enchantsConst = Constants.ENCHANTS;
        JsonArray allItemEnchs = null;
        Set<String> ignoreFromPool = new HashSet<>();
        if (enchantsConst != null && hasEnchantments && NotEnoughUpdates.INSTANCE.config.tooltipTweaks.missingEnchantList) {
            try {
                JsonArray enchantPools = enchantsConst.get("enchant_pools").getAsJsonArray();
                for (JsonElement element : enchantPools) {
                    Set<String> currentPool = new HashSet<>();
                    for (JsonElement poolElement : element.getAsJsonArray()) {
                        String poolS = poolElement.getAsString();
                        currentPool.add(poolS);
                    }
                    for (JsonElement poolElement : element.getAsJsonArray()) {
                        String poolS = poolElement.getAsString();
                        if (enchantIds.contains(poolS)) {
                            ignoreFromPool.addAll(currentPool);
                            break;
                        }
                    }
                }

                JsonObject enchantsObj = enchantsConst.get("enchants").getAsJsonObject();
                NBTTagCompound tag = event.itemStack.getTagCompound();
                if (tag != null) {
                    NBTTagCompound display = tag.getCompoundTag("display");
                    if (display.hasKey("Lore", 9)) {
                        NBTTagList list = display.getTagList("Lore", 8);
                        out:
                        for (int i = list.tagCount(); i >= 0; i--) {
                            String line = list.getStringTagAt(i);
                            for (int j = 0; j < Utils.rarityArrC.length; j++) {
                                for (Map.Entry<String, JsonElement> entry : enchantsObj.entrySet()) {
                                    if (line.contains(Utils.rarityArrC[j] + " " + entry.getKey()) || line.contains(Utils.rarityArrC[j] + " DUNGEON " + entry.getKey())) {
                                        allItemEnchs = entry.getValue().getAsJsonArray();
                                        break out;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        boolean gotToEnchants = false;
        boolean passedEnchants = false;

        boolean dungeonProfit = false;
        int index = 0;
        List<String> newTooltip = new ArrayList<>();

        for (String line : event.toolTip) {
            if (line.endsWith(EnumChatFormatting.DARK_GRAY + "Reforge Stone") && NotEnoughUpdates.INSTANCE.config.tooltipTweaks.showReforgeStats) {
                JsonObject reforgeStones = Constants.REFORGESTONES;

                if (reforgeStones != null && reforgeStones.has(internalname)) {
                    boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                    if (!pressedShiftLast && shift) {
                        showReforgeStoneStats = !showReforgeStoneStats;
                    }
                    pressedShiftLast = shift;

                    newTooltip.add(line);
                    newTooltip.add("");
                    if (!showReforgeStoneStats) {
                        newTooltip.add(EnumChatFormatting.DARK_GRAY + "[Press SHIFT to show extra info]");
                    } else {
                        newTooltip.add(EnumChatFormatting.DARK_GRAY + "[Press SHIFT to hide extra info]");
                    }

                    JsonObject reforgeInfo = reforgeStones.get(internalname).getAsJsonObject();
                    JsonArray requiredRaritiesArray = reforgeInfo.get("requiredRarities").getAsJsonArray();

                    if (showReforgeStoneStats && requiredRaritiesArray.size() > 0) {
                        String reforgeName = Utils.getElementAsString(reforgeInfo.get("reforgeName"), "");

                        String[] requiredRarities = new String[requiredRaritiesArray.size()];
                        for (int i = 0; i < requiredRaritiesArray.size(); i++) {
                            requiredRarities[i] = requiredRaritiesArray.get(i).getAsString();
                        }

                        int rarityIndex = requiredRarities.length - 1;
                        String rarity = requiredRarities[rarityIndex];
                        for (int i = 0; i < requiredRarities.length; i++) {
                            String rar = requiredRarities[i];
                            if (rar.equalsIgnoreCase(currentRarity)) {
                                rarity = rar;
                                rarityIndex = i;
                                break;
                            }
                        }

                        boolean left = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
                        boolean right = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
                        if (!pressedArrowLast && (left || right)) {
                            if (left) {
                                rarityIndex--;
                            } else if (right) {
                                rarityIndex++;
                            }
                            if (rarityIndex < 0) rarityIndex = 0;
                            if (rarityIndex >= requiredRarities.length) rarityIndex = requiredRarities.length - 1;
                            currentRarity = requiredRarities[rarityIndex];
                            rarity = currentRarity;
                        }
                        pressedArrowLast = left || right;

                        JsonElement statsE = reforgeInfo.get("reforgeStats");

                        String rarityFormatted = Utils.rarityArrMap.getOrDefault(rarity, rarity);

                        JsonElement reforgeAbilityE = reforgeInfo.get("reforgeAbility");
                        String reforgeAbility = null;
                        if (reforgeAbilityE != null) {
                            if (reforgeAbilityE.isJsonPrimitive() && reforgeAbilityE.getAsJsonPrimitive().isString()) {
                                reforgeAbility = Utils.getElementAsString(reforgeInfo.get("reforgeAbility"), "");

                            } else if (reforgeAbilityE.isJsonObject()) {
                                if (reforgeAbilityE.getAsJsonObject().has(rarity)) {
                                    reforgeAbility = reforgeAbilityE.getAsJsonObject().get(rarity).getAsString();
                                }
                            }
                        }

                        if (reforgeAbility != null && !reforgeAbility.isEmpty()) {
                            String text = EnumChatFormatting.BLUE + (reforgeName.isEmpty() ? "Bonus: " : reforgeName + " Bonus: ") +
                                    EnumChatFormatting.GRAY + reforgeAbility;
                            boolean first = true;
                            for (String s : Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(text, 150)) {
                                newTooltip.add((first ? "" : "  ") + s);
                                first = false;
                            }
                            newTooltip.add("");
                        }

                        newTooltip.add(EnumChatFormatting.BLUE + "Stats for " + rarityFormatted + "\u00a79: [\u00a7l\u00a7m< \u00a79Switch\u00a7l\u27a1\u00a79]");

                        if (statsE != null && statsE.isJsonObject()) {
                            JsonObject stats = statsE.getAsJsonObject();

                            JsonElement statsRarE = stats.get(rarity);
                            if (statsRarE != null && statsRarE.isJsonObject()) {

                                JsonObject statsRar = statsRarE.getAsJsonObject();

                                TreeSet<Map.Entry<String, JsonElement>> sorted = new TreeSet<>(Map.Entry.comparingByKey());
                                sorted.addAll(statsRar.entrySet());

                                for (Map.Entry<String, JsonElement> entry : sorted) {
                                    if (entry.getValue().isJsonPrimitive() && ((JsonPrimitive) entry.getValue()).isNumber()) {
                                        float statNumF = entry.getValue().getAsFloat();
                                        String statNumS;
                                        if (statNumF % 1 == 0) {
                                            statNumS = String.valueOf(Math.round(statNumF));
                                        } else {
                                            statNumS = Utils.floatToString(statNumF, 1);
                                        }
                                        String reforgeNamePretty = WordUtils.capitalizeFully(entry.getKey().replace("_", " "));
                                        String text = EnumChatFormatting.GRAY + reforgeNamePretty + ": " + EnumChatFormatting.GREEN + "+" + statNumS;
                                        if (percentStats.contains(entry.getKey())) {
                                            text += "%";
                                        }
                                        newTooltip.add("  " + text);
                                    }
                                }
                            }
                        }

                        JsonElement reforgeCostsE = reforgeInfo.get("reforgeCosts");
                        int reforgeCost = -1;
                        if (reforgeCostsE != null) {
                            if (reforgeCostsE.isJsonPrimitive() && reforgeCostsE.getAsJsonPrimitive().isNumber()) {
                                reforgeCost = (int) Utils.getElementAsFloat(reforgeInfo.get("reforgeAbility"), -1);

                            } else if (reforgeCostsE.isJsonObject()) {
                                if (reforgeCostsE.getAsJsonObject().has(rarity)) {
                                    reforgeCost = (int) Utils.getElementAsFloat(reforgeCostsE.getAsJsonObject().get(rarity), -1);
                                }
                            }
                        }

                        if (reforgeCost >= 0) {
                            String text = EnumChatFormatting.BLUE + "Apply Cost: " + EnumChatFormatting.GOLD + NumberFormat.getNumberInstance().format(reforgeCost) + " coins";
                            newTooltip.add("");
                            newTooltip.add(text);
                        }

                    }

                    continue;
                }

            } else if (line.contains("\u00A7cR\u00A76a\u00A7ei\u00A7an\u00A7bb\u00A79o\u00A7dw\u00A79 Rune")) {
                line = line.replace("\u00A7cR\u00A76a\u00A7ei\u00A7an\u00A7bb\u00A79o\u00A7dw\u00A79 Rune",
                        Utils.chromaString("Rainbow Rune", index, false) + EnumChatFormatting.BLUE);
            } else if (hasEnchantments) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && NotEnoughUpdates.INSTANCE.config.tooltipTweaks.missingEnchantList) {
                    boolean lineHasEnch = false;
                    for (String s : enchantIds) {
                        String enchantName = WordUtils.capitalizeFully(s.replace("_", " "));
                        if (line.contains(enchantName)) {
                            lineHasEnch = true;
                            break;
                        }
                    }
                    if (lineHasEnch) {
                        gotToEnchants = true;
                    } else {
                        if (gotToEnchants && !passedEnchants && Utils.cleanColour(line).trim().length() == 0) {
                            if (enchantsConst != null && allItemEnchs != null) {
                                List<String> missing = new ArrayList<>();
                                for (JsonElement enchIdElement : allItemEnchs) {
                                    String enchId = enchIdElement.getAsString();
                                    if (!enchId.startsWith("ultimate_") && !ignoreFromPool.contains(enchId) && !enchantIds.contains(enchId)) {
                                        missing.add(enchId);
                                    }
                                }
                                if (!missing.isEmpty()) {
                                    newTooltip.add("");
                                    StringBuilder currentLine = new StringBuilder(EnumChatFormatting.RED + "Missing: " + EnumChatFormatting.GRAY);
                                    for (int i = 0; i < missing.size(); i++) {
                                        String enchName = WordUtils.capitalizeFully(missing.get(i).replace("_", " "));
                                        if (currentLine.length() != 0 && (Utils.cleanColour(currentLine.toString()).length() + enchName.length()) > 40) {
                                            newTooltip.add(currentLine.toString());
                                            currentLine = new StringBuilder();
                                        }
                                        if (currentLine.length() != 0 && i != 0) {
                                            currentLine.append(", ").append(enchName);
                                        } else {
                                            currentLine.append(EnumChatFormatting.GRAY).append(enchName);
                                        }
                                    }
                                    if (currentLine.length() != 0) {
                                        newTooltip.add(currentLine.toString());
                                    }
                                }
                            }
                            passedEnchants = true;
                        }
                    }
                }
                for (String op : NotEnoughUpdates.INSTANCE.config.hidden.enchantColours) {
                    List<String> colourOps = GuiEnchantColour.splitter.splitToList(op);
                    String enchantName = GuiEnchantColour.getColourOpIndex(colourOps, 0);
                    String comparator = GuiEnchantColour.getColourOpIndex(colourOps, 1);
                    String comparison = GuiEnchantColour.getColourOpIndex(colourOps, 2);
                    String colourCode = GuiEnchantColour.getColourOpIndex(colourOps, 3);
                    String modifier = GuiEnchantColour.getColourOpIndex(colourOps, 4);

                    int modifierI = GuiEnchantColour.getIntModifier(modifier);

                    if (enchantName.length() == 0) continue;
                    if (comparator.length() == 0) continue;
                    if (comparison.length() == 0) continue;
                    if (colourCode.length() == 0) continue;

                    int comparatorI = ">=<".indexOf(comparator.charAt(0));

                    int levelToFind = -1;
                    try {
                        levelToFind = Integer.parseInt(comparison);
                    } catch (Exception e) {
                        continue;
                    }

                    if (comparatorI < 0) continue;
                    String regexText = "0123456789abcdefz";
                    if (isSbaloaded()) {
                        regexText = regexText + "Z";
                    }

                    if (regexText.indexOf(colourCode.charAt(0)) < 0) continue;

                    //item_lore = item_lore.replaceAll("\\u00A79("+lvl4Max+" IV)", EnumChatFormatting.DARK_PURPLE+"$1");
                    //9([a-zA-Z ]+?) ([0-9]+|(I|II|III|IV|V|VI|VII|VIII|IX|X))(,|$)
                    Pattern pattern;
                    try {
                        pattern = Pattern.compile("(\\u00A79|\\u00A7(9|l)\\u00A7d\\u00A7l)(?<enchantName>" + enchantName + ") " +
                                "(?<level>[0-9]+|(I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX))((\\u00A79)?,|( \\u00A78(?:,?[0-9]+)*)?$)");
                    } catch (Exception e) {
                        continue;
                    } //malformed regex
                    Matcher matcher = pattern.matcher(line);
                    int matchCount = 0;
                    while (matcher.find() && matchCount < 5) {
                        if (Utils.cleanColour(matcher.group("enchantName")).startsWith(" ")) continue;

                        matchCount++;
                        int level = -1;
                        String levelStr = matcher.group("level");
                        if (levelStr == null) continue;
                        try {
                            level = Integer.parseInt(levelStr);
                        } catch (Exception e) {
                            switch (levelStr) {
                                case "I":
                                    level = 1;
                                    break;
                                case "II":
                                    level = 2;
                                    break;
                                case "III":
                                    level = 3;
                                    break;
                                case "IV":
                                    level = 4;
                                    break;
                                case "V":
                                    level = 5;
                                    break;
                                case "VI":
                                    level = 6;
                                    break;
                                case "VII":
                                    level = 7;
                                    break;
                                case "VIII":
                                    level = 8;
                                    break;
                                case "IX":
                                    level = 9;
                                    break;
                                case "X":
                                    level = 10;
                                    break;
                                case "XI":
                                    level = 11;
                                    break;
                                case "XII":
                                    level = 12;
                                    break;
                                case "XIII":
                                    level = 13;
                                    break;
                                case "XIV":
                                    level = 14;
                                    break;
                                case "XV":
                                    level = 15;
                                    break;
                                case "XVI":
                                    level = 16;
                                    break;
                                case "XVII":
                                    level = 17;
                                    break;
                                case "XVIII":
                                    level = 18;
                                    break;
                                case "XIX":
                                    level = 19;
                                    break;
                                case "XX":
                                    level = 20;
                                    break;
                            }
                        }
                        boolean matches = false;
                        if (level > 0) {
                            switch (comparator) {
                                case ">":
                                    matches = level > levelToFind;
                                    break;
                                case "=":
                                    matches = level == levelToFind;
                                    break;
                                case "<":
                                    matches = level < levelToFind;
                                    break;
                            }
                        }
                        if (matches) {
                            String enchantText = matcher.group("enchantName");
                            StringBuilder extraModifiersBuilder = new StringBuilder();

                            if ((modifierI & GuiEnchantColour.BOLD_MODIFIER) != 0) {
                                extraModifiersBuilder.append(EnumChatFormatting.BOLD);
                            }
                            if ((modifierI & GuiEnchantColour.ITALIC_MODIFIER) != 0) {
                                extraModifiersBuilder.append(EnumChatFormatting.ITALIC);
                            }
                            if ((modifierI & GuiEnchantColour.UNDERLINE_MODIFIER) != 0) {
                                extraModifiersBuilder.append(EnumChatFormatting.UNDERLINE);
                            }
                            if ((modifierI & GuiEnchantColour.OBFUSCATED_MODIFIER) != 0) {
                                extraModifiersBuilder.append(EnumChatFormatting.OBFUSCATED);
                            }
                            if ((modifierI & GuiEnchantColour.STRIKETHROUGH_MODIFIER) != 0) {
                                extraModifiersBuilder.append(EnumChatFormatting.STRIKETHROUGH);
                            }

                            String extraMods = extraModifiersBuilder.toString();

                            if (!colourCode.equals("z")) {
                                line = line.replace("\u00A79" + enchantText,
                                        "\u00A7" + colourCode + extraMods + enchantText);
                                line = line.replace("\u00A79\u00A7d\u00A7l" + enchantText,
                                        "\u00A7" + colourCode + extraMods + enchantText);
                                line = line.replace("\u00A7l\u00A7d\u00A7l" + enchantText,
                                        "\u00A7" + colourCode + extraMods + enchantText);
                            } else {
                                int offset = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line.replaceAll(
                                        "\\u00A79" + enchantText + ".*", ""));
                                line = line.replace("\u00A79" + enchantText, Utils.chromaString(enchantText, offset / 12f + index, false));

                                offset = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line.replaceAll(
                                        "\\u00A79\\u00A7d\\u00A7l" + enchantText + ".*", ""));
                                line = line.replace("\u00A79\u00A7d\u00A7l" + enchantText, Utils.chromaString(enchantText,
                                        offset / 12f + index, true));
                                offset = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line.replaceAll(
                                        "\\u00A7l\\u00A7d\\u00A7l" + enchantText + ".*", ""));
                                line = line.replace("\u00A7l\u00A7d\u00A7l" + enchantText, Utils.chromaString(enchantText,
                                        offset / 12f + index, true));
                            }
                        }
                    }
                }
            }

            newTooltip.add(line);

            if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.showPriceInfoAucItem) {
                if (line.contains(EnumChatFormatting.GRAY + "Buy it now: ") ||
                        line.contains(EnumChatFormatting.GRAY + "Bidder: ") ||
                        line.contains(EnumChatFormatting.GRAY + "Starting bid: ")) {

                    if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                        newTooltip.add("");
                        newTooltip.add(EnumChatFormatting.GRAY + "[SHIFT for Price Info]");
                    } else {
                        ItemPriceInformation.addToTooltip(newTooltip, internalname, event.itemStack);
                    }
                }
            }

            if (NotEnoughUpdates.INSTANCE.config.dungeons.profitDisplayLoc == 2 && Minecraft.getMinecraft().currentScreen instanceof GuiChest) {
                if (line.contains(EnumChatFormatting.GREEN + "Open Reward Chest")) {
                    dungeonProfit = true;
                } else if (index == 7 && dungeonProfit) {
                    GuiChest eventGui = (GuiChest) Minecraft.getMinecraft().currentScreen;
                    ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
                    IInventory lower = cc.getLowerChestInventory();

                    int chestCost = 0;
                    try {
                        String line6 = Utils.cleanColour(line);
                        StringBuilder cost = new StringBuilder();
                        for (int i = 0; i < line6.length(); i++) {
                            char c = line6.charAt(i);
                            if ("0123456789".indexOf(c) >= 0) {
                                cost.append(c);
                            }
                        }
                        if (cost.length() > 0) {
                            chestCost = Integer.parseInt(cost.toString());
                        }
                    } catch (Exception ignored) {}

                    String missingItem = null;
                    int totalValue = 0;
                    HashMap<String, Float> itemValues = new HashMap<>();
                    for (int i = 0; i < 5; i++) {
                        ItemStack item = lower.getStackInSlot(11 + i);
                        String internal = neu.manager.getInternalNameForItem(item);
                        if (internal != null) {
                            internal = internal.replace("\u00CD", "I").replace("\u0130", "I");
                            float bazaarPrice = -1;
                            JsonObject bazaarInfo = neu.manager.auctionManager.getBazaarInfo(internal);
                            if (bazaarInfo != null && bazaarInfo.has("curr_sell")) {
                                bazaarPrice = bazaarInfo.get("curr_sell").getAsFloat();
                            }
                            if (bazaarPrice < 5000000 && internal.equals("RECOMBOBULATOR_3000")) bazaarPrice = 5000000;

                            float worth = -1;
                            if (bazaarPrice > 0) {
                                worth = bazaarPrice;
                            } else {
                                switch (NotEnoughUpdates.INSTANCE.config.dungeons.profitType) {
                                    case 1:
                                        worth = neu.manager.auctionManager.getItemAvgBin(internal);
                                        break;
                                    case 2:
                                        JsonObject auctionInfo = neu.manager.auctionManager.getItemAuctionInfo(internal);
                                        if (auctionInfo != null) {
                                            if (auctionInfo.has("clean_price")) {
                                                worth = (int) auctionInfo.get("clean_price").getAsFloat();
                                            } else {
                                                worth = (int) (auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsFloat());
                                            }
                                        }
                                        break;
                                    default:
                                        worth = neu.manager.auctionManager.getLowestBin(internal);
                                }
                                if (worth <= 0) {
                                    worth = neu.manager.auctionManager.getLowestBin(internal);
                                    if (worth <= 0) {
                                        worth = neu.manager.auctionManager.getItemAvgBin(internal);
                                        if (worth <= 0) {
                                            JsonObject auctionInfo = neu.manager.auctionManager.getItemAuctionInfo(internal);
                                            if (auctionInfo != null) {
                                                if (auctionInfo.has("clean_price")) {
                                                    worth = (int) auctionInfo.get("clean_price").getAsFloat();
                                                } else {
                                                    worth = (int) (auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsFloat());
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (worth > 0 && totalValue >= 0) {
                                totalValue += worth;

                                String display = item.getDisplayName();

                                if (display.contains("Enchanted Book")) {
                                    NBTTagCompound tag = item.getTagCompound();
                                    if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
                                        NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
                                        NBTTagCompound enchants = ea.getCompoundTag("enchantments");

                                        int highestLevel = -1;
                                        for (String enchname : enchants.getKeySet()) {
                                            int level = enchants.getInteger(enchname);
                                            if (level > highestLevel) {
                                                display = EnumChatFormatting.BLUE + WordUtils.capitalizeFully(
                                                        enchname.replace("_", " ")
                                                                .replace("Ultimate", "")
                                                                .trim()) + " " + level;
                                            }
                                        }
                                    }
                                }

                                itemValues.put(display, worth);
                            } else {
                                if (totalValue != -1) {
                                    missingItem = internal;
                                }
                                totalValue = -1;
                            }
                        }
                    }

                    NumberFormat format = NumberFormat.getInstance(Locale.US);
                    String valueStringBIN1;
                    String valueStringBIN2;
                    if (totalValue >= 0) {
                        valueStringBIN1 = EnumChatFormatting.YELLOW + "Value (BIN): ";
                        valueStringBIN2 = EnumChatFormatting.GOLD + format.format(totalValue) + " coins";
                    } else {
                        valueStringBIN1 = EnumChatFormatting.YELLOW + "Can't find BIN: ";
                        valueStringBIN2 = missingItem;
                    }

                    int profitLossBIN = totalValue - chestCost;
                    String profitPrefix = EnumChatFormatting.DARK_GREEN.toString();
                    String lossPrefix = EnumChatFormatting.RED.toString();
                    String prefix = profitLossBIN >= 0 ? profitPrefix : lossPrefix;

                    String plStringBIN;
                    if (profitLossBIN >= 0) {
                        plStringBIN = prefix + "+" + format.format(profitLossBIN) + " coins";
                    } else {
                        plStringBIN = prefix + "-" + format.format(-profitLossBIN) + " coins";
                    }

                    String neu = EnumChatFormatting.YELLOW + "[NEU] ";

                    newTooltip.add(neu + valueStringBIN1 + " " + valueStringBIN2);
                    if (totalValue >= 0) {
                        newTooltip.add(neu + EnumChatFormatting.YELLOW + "Profit/Loss: " + plStringBIN);
                    }

                    for (Map.Entry<String, Float> entry : itemValues.entrySet()) {
                        newTooltip.add(neu + entry.getKey() + prefix + "+" +
                                format.format(entry.getValue().intValue()));
                    }
                }
            }

            index++;
        }

        for (int i = newTooltip.size() - 1; i >= 0; i--) {
            String line = Utils.cleanColour(newTooltip.get(i));
            for (int i1 = 0; i1 < Utils.rarityArr.length; i1++) {
                if (line.equals(Utils.rarityArr[i1])) {
                    if (i - 2 < 0) {
                        break;
                    }
                    newTooltip.addAll(i - 1, petToolTipXPExtend(event));
                    break;
                }
            }
        }

        pressedShiftLast = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        pressedArrowLast = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT);

        event.toolTip.clear();
        event.toolTip.addAll(newTooltip);

        HashMap<String, List<String>> loreBuckets = new HashMap<>();

        List<String> hypixelOrder = new ArrayList<>();

        hypixelOrder.add("attributes");
        hypixelOrder.add("enchants");
        hypixelOrder.add("ability");
        hypixelOrder.add("reforge_bonus");
        hypixelOrder.add("rarity");

        if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.showPriceInfoInvItem) {
            ItemPriceInformation.addToTooltip(event.toolTip, internalname, event.itemStack);
        }

        if (event.itemStack.getTagCompound() != null && event.itemStack.getTagCompound().getBoolean("NEUHIDEPETTOOLTIP") && NotEnoughUpdates.INSTANCE.config.petOverlay.hidePetTooltip) {
            event.toolTip.clear();
        }
    }

    private final Pattern xpLevelPattern = Pattern.compile("(.*) (\\xA7e(.*)\\xA76/\\xA7e(.*))");

    private void onItemToolTipInternalNameNull(ItemTooltipEvent event) {
        petToolTipXPExtendPetMenu(event);
    }

    private List<String> petToolTipXPExtend(ItemTooltipEvent event) {
        List<String> tooltipText = new ArrayList<>();
        if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.petExtendExp) {
            if (event.itemStack.getTagCompound().hasKey("DisablePetExp")) {
                if (event.itemStack.getTagCompound().getBoolean("DisablePetExp")) {
                    return tooltipText;
                }
            }
            //7 is just a random number i chose, prob no pets with less lines than 7
            if (event.toolTip.size() > 7) {
                if (Utils.cleanColour(event.toolTip.get(1)).matches(petToolTipRegex)) {

                    GuiProfileViewer.PetLevel petlevel = null;

                    //this is the item itself
                    NBTTagCompound tag = event.itemStack.getTagCompound();
                    if (tag.hasKey("ExtraAttributes")) {
                        if (tag.getCompoundTag("ExtraAttributes").hasKey("petInfo")) {
                            JsonObject petInfo = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(
                                    tag.getCompoundTag("ExtraAttributes").getString("petInfo"), JsonObject.class);
                            if (petInfo.has("exp") && petInfo.get("exp").isJsonPrimitive()) {
                                JsonPrimitive exp = petInfo.getAsJsonPrimitive("exp");
                                String petName = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(event.itemStack);
                                //Utils.getRarityFromInt(Utils.checkItemTypePet(event.toolTip))).getAsInt();
                                petlevel = GuiProfileViewer.getPetLevel(petName, Utils.getRarityFromInt(Utils.checkItemTypePet(event.toolTip)), exp.getAsLong());
                            }
                        }
                    }

                    if (petlevel != null) {
                        tooltipText.add("");
                        if (petlevel.totalXp > petlevel.maxXP) {
                            tooltipText.add(EnumChatFormatting.AQUA + "" + EnumChatFormatting.BOLD + "MAX LEVEL");
                        } else {
                            tooltipText.add(EnumChatFormatting.GRAY + "Progress to Level " + (int) Math.floor(petlevel.level + 1) + ": " + EnumChatFormatting.YELLOW + Utils.round(petlevel.levelPercentage * 100, 1) + "%");
                            int levelpercentage = Math.round(petlevel.levelPercentage * 20);
                            tooltipText.add(EnumChatFormatting.DARK_GREEN + String.join("", Collections.nCopies(levelpercentage, "-")) + EnumChatFormatting.WHITE + String.join("", Collections.nCopies(20 - levelpercentage, "-")));
                            tooltipText.add(EnumChatFormatting.GRAY + "EXP: " + EnumChatFormatting.YELLOW + myFormatter.format(petlevel.levelXp) +
                                    EnumChatFormatting.GOLD + "/" + EnumChatFormatting.YELLOW + myFormatter.format(petlevel.currentLevelRequirement) + " EXP");
                        }
                    }
                }
            }
        }
        return tooltipText;
    }

    private static final String petToolTipRegex = "((Farming)|(Combat)|(Fishing)|(Mining)|(Foraging)|(Enchanting)|(Alchemy)) ((Mount)|(Pet)|(Morph)).*";

    private void petToolTipXPExtendPetMenu(ItemTooltipEvent event) {
        if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.petExtendExp) {
            //7 is just a random number i chose, prob no pets with less lines than 7
            if (event.toolTip.size() > 7) {
                if (Utils.cleanColour(event.toolTip.get(1)).matches(petToolTipRegex)) {
                    GuiProfileViewer.PetLevel petlevel = null;

                    int xpLine = -1;
                    for (int i = event.toolTip.size() - 1; i >= 0; i--) {
                        Matcher matcher = xpLevelPattern.matcher(event.toolTip.get(i));
                        if (matcher.matches()) {
                            xpLine = i;
                            event.toolTip.set(xpLine, matcher.group(1));
                            break;
                        } else if (event.toolTip.get(i).matches("MAX LEVEL")) {
                            return;
                        }
                    }

                    PetInfoOverlay.Pet pet = PetInfoOverlay.getPetFromStack(event.itemStack.getDisplayName(), NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(event.itemStack.getTagCompound()));
                    if (pet == null) {
                        return;
                    }
                    petlevel = pet.petLevel;

                    if (petlevel == null || xpLine == -1) {
                        return;
                    }

                    event.toolTip.add(xpLine + 1, EnumChatFormatting.GRAY + "EXP: " + EnumChatFormatting.YELLOW + myFormatter.format(petlevel.levelXp) +
                            EnumChatFormatting.GOLD + "/" + EnumChatFormatting.YELLOW + myFormatter.format(petlevel.currentLevelRequirement));

                }
            }
        }
    }

    DecimalFormat myFormatter = new DecimalFormat("###,###.###");

    /**
     * This makes it so that holding LCONTROL while hovering over an item with NBT will show the NBT of the item.
     */
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!neu.isOnSkyblock()) return;
        //Render the pet inventory display tooltip to the left to avoid things from other mods rendering over the tooltip
        if (event.itemStack.getTagCompound() != null && event.itemStack.getTagCompound().getBoolean("NEUPETINVDISPLAY")) {
            GlStateManager.translate(-200, 0, 0);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && NotEnoughUpdates.INSTANCE.config.hidden.dev &&
                event.toolTip.size() > 0 && event.toolTip.get(event.toolTip.size() - 1).startsWith(EnumChatFormatting.DARK_GRAY + "NBT: ")) {
            event.toolTip.remove(event.toolTip.size() - 1);

            StringBuilder sb = new StringBuilder();
            String nbt = event.itemStack.getTagCompound().toString();
            int indent = 0;
            for (char c : nbt.toCharArray()) {
                boolean newline = false;
                if (c == '{' || c == '[') {
                    indent++;
                    newline = true;
                } else if (c == '}' || c == ']') {
                    indent--;
                    sb.append("\n");
                    for (int i = 0; i < indent; i++) sb.append("  ");
                } else if (c == ',') {
                    newline = true;
                } else if (c == '\"') {
                    sb.append(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY);
                }

                sb.append(c);
                if (newline) {
                    sb.append("\n");
                    for (int i = 0; i < indent; i++) sb.append("  ");
                }
            }
            event.toolTip.add(sb.toString());
            if (Keyboard.isKeyDown(Keyboard.KEY_H)) {
                if (!copied) {
                    copied = true;
                    StringSelection selection = new StringSelection(sb.toString());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                }
            } else {
                copied = false;
            }
        } else if (NotEnoughUpdates.INSTANCE.packDevEnabled) {
            event.toolTip.add("");
            event.toolTip.add(EnumChatFormatting.AQUA + "NEU Pack Dev Info:");
            event.toolTip.add("Press " + EnumChatFormatting.GOLD + "[KEY]" + EnumChatFormatting.GRAY + " to copy line");

            String internal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(event.itemStack);

            boolean k = Keyboard.isKeyDown(Keyboard.KEY_K);
            boolean m = Keyboard.isKeyDown(Keyboard.KEY_M);
            boolean n = Keyboard.isKeyDown(Keyboard.KEY_N);

            event.toolTip.add(EnumChatFormatting.AQUA + "Internal Name: " + EnumChatFormatting.GRAY + internal + EnumChatFormatting.GOLD + " [K]");
            if (!copied && k) {
                MiscUtils.copyToClipboard(internal);
            }

            if (event.itemStack.getTagCompound() != null) {
                NBTTagCompound tag = event.itemStack.getTagCompound();

                if (tag.hasKey("SkullOwner", 10)) {
                    GameProfile gameprofile = NBTUtil.readGameProfileFromNBT(tag.getCompoundTag("SkullOwner"));

                    if (gameprofile != null) {
                        event.toolTip.add(EnumChatFormatting.AQUA + "Skull UUID: " + EnumChatFormatting.GRAY + gameprofile.getId() + EnumChatFormatting.GOLD + " [M]");
                        if (!copied && m) {
                            MiscUtils.copyToClipboard(gameprofile.getId().toString());
                        }

                        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Minecraft.getMinecraft().getSkinManager().loadSkinFromCache(gameprofile);

                        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                            MinecraftProfileTexture profTex = map.get(MinecraftProfileTexture.Type.SKIN);
                            event.toolTip.add(EnumChatFormatting.AQUA + "Skull Texture Link: " + EnumChatFormatting.GRAY + profTex.getUrl() + EnumChatFormatting.GOLD + " [N]");

                            if (!copied && n) {
                                MiscUtils.copyToClipboard(profTex.getUrl());
                            }
                        }
                    }
                }
            }

            copied = k || m || n;
        }
    }

    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event) {
        CrystalMetalDetectorSolver.render(event.partialTicks);
    }
}
