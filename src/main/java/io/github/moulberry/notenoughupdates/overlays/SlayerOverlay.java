package io.github.moulberry.notenoughupdates.overlays;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class SlayerOverlay extends TextOverlay {
    public static boolean slayerQuest;
    public static String RNGMeter = "?";
    public static boolean isSlain = false;
    public static String slayerLVL = "-1";
    public static String slayerXp = "0";
    private static String slayerEXP = "0";
    private static int slayerIntXP;
    private static int untilNextSlayerLevel;
    private static int xpToLevelUp;
    private static boolean useSmallXpNext = true;
    public static long timeSinceLastBoss = 0;
    public static long timeSinceLastBoss2 = 0;
    private static long agvSlayerTime = 0;
    private static boolean isSlayerNine = false;
    public static int slayerTier = 0;
    private static int xpPerBoss = 0;
    private static int bossesUntilNextLevel = 0;
    public static long unloadOverlayTimer = -1;

    public SlayerOverlay(Position position, Supplier<List<String>> dummyStrings, Supplier<TextOverlayStyle> styleSupplier) {
        super(position, dummyStrings, styleSupplier);
    }

    @Override
    public void update() {
        if (!NotEnoughUpdates.INSTANCE.config.slayerOverlay.slayerOverlay) {
            overlayStrings = null;
            return;
        }

        if (Minecraft.getMinecraft().thePlayer == null) return;

        if (!slayerQuest) {
            slayerTier = 0;
        }

        if (slayerXp.equals("maxed")) {
            isSlayerNine = true;
        } else if (!slayerXp.equals("0")) {
            slayerEXP = slayerXp.replace(",", "");
            slayerIntXP = Integer.parseInt(slayerEXP);
            isSlayerNine = false;
        } else {
            slayerIntXP = 0;
            isSlayerNine = false;
        }
        //System.out.println(slayerEXP);
        if (SBInfo.getInstance().slayer.equals("Tarantula") || SBInfo.getInstance().slayer.equals("Revenant")) {
            useSmallXpNext = true;
        } else if (SBInfo.getInstance().slayer.equals("Sven") || SBInfo.getInstance().slayer.equals("Enderman")) {
            useSmallXpNext = false;
        }
        switch (slayerLVL) {
            case "9":
                xpToLevelUp = 2000000;
                break;
            case "8":
                xpToLevelUp = 1000000;
                break;
            case "7":
                xpToLevelUp = 400000;
                break;
            case "6":
                xpToLevelUp = 100000;
                break;
            case "5":
                xpToLevelUp = 20000;
                break;
            case "4":
                xpToLevelUp = 5000;
                break;
            case "3":
                if (useSmallXpNext) {
                    xpToLevelUp = 1000;
                } else {
                    xpToLevelUp = 1500;
                }
                break;
            case "2":
                if (useSmallXpNext) {
                    xpToLevelUp = 200;
                } else {
                    xpToLevelUp = 250;
                }
                break;
            case "1":
                if (SBInfo.getInstance().slayer.equals("Revenant")) {
                    xpToLevelUp = 15;
                } else if (SBInfo.getInstance().slayer.equals("Tarantula")) {
                    xpToLevelUp = 25;
                } else {
                    xpToLevelUp = 30;
                }
                break;
            case "0":
                if (useSmallXpNext) {
                    xpToLevelUp = 5;
                } else {
                    xpToLevelUp = 10;
                }
                break;
        }
        if (slayerTier == 5) {
            xpPerBoss = 1500;
        } else if (slayerTier == 4) {
            xpPerBoss = 500;
        } else if (slayerTier == 3) {
            xpPerBoss = 100;
        } else if (slayerTier == 2) {
            xpPerBoss = 25;
        } else if (slayerTier == 1) {
            xpPerBoss = 5;
        } else {
            xpPerBoss = 0;
        }
        untilNextSlayerLevel = xpToLevelUp - slayerIntXP;
        if (xpPerBoss != 0 && untilNextSlayerLevel != 0 && xpToLevelUp != 0) {
            bossesUntilNextLevel = (xpToLevelUp - untilNextSlayerLevel) / xpPerBoss;
        } else {
            bossesUntilNextLevel = 0;
        }
        agvSlayerTime = (timeSinceLastBoss+timeSinceLastBoss2)/2;
    }

    @Override
    public void updateFrequent() {
        super.updateFrequent();

        if (!slayerQuest || !NotEnoughUpdates.INSTANCE.config.slayerOverlay.slayerOverlay) {
            overlayStrings = null;
        } else {
            HashMap<Integer, String> lineMap = new HashMap<>();

            NumberFormat format = NumberFormat.getIntegerInstance();
            //System.out.println(SBInfo.getInstance().isSlain);
            overlayStrings = new ArrayList<>();
            lineMap.put(0, EnumChatFormatting.YELLOW + "Slayer: " + EnumChatFormatting.DARK_RED + SBInfo.getInstance().slayer
                    + EnumChatFormatting.GREEN + (isSlain ? " (Killed) " : " ")/* + slayerTier*/);

           if (!RNGMeter.equals("?")) {
               lineMap.put(1, EnumChatFormatting.YELLOW + "RNG Meter: " + EnumChatFormatting.DARK_PURPLE + RNGMeter);
           }

            if (!slayerLVL.equals("-1")) {
                lineMap.put(2, EnumChatFormatting.YELLOW + "Lvl: " + EnumChatFormatting.LIGHT_PURPLE + slayerLVL);
            }

            if (timeSinceLastBoss > 0) {
                lineMap.put(3, EnumChatFormatting.YELLOW + "Kill time: " + EnumChatFormatting.RED
                        + Utils.prettyTime((System.currentTimeMillis() - timeSinceLastBoss)));
            }

            if (slayerIntXP > 0) {
                lineMap.put(4, EnumChatFormatting.YELLOW + "XP: " + EnumChatFormatting.LIGHT_PURPLE
                        + format.format(untilNextSlayerLevel) + "/" + format.format(xpToLevelUp));
            } else if (isSlayerNine) {
                lineMap.put(4, EnumChatFormatting.YELLOW + "XP: " + EnumChatFormatting.LIGHT_PURPLE + "MAXED");
            }

            if (xpPerBoss != 0 && slayerIntXP > 0) {
                lineMap.put(5, EnumChatFormatting.YELLOW + "Bosses till next Lvl: " + EnumChatFormatting.LIGHT_PURPLE +
                        (bossesUntilNextLevel > 1000 ? "?" : bossesUntilNextLevel));
            }

            if (timeSinceLastBoss > 0 && timeSinceLastBoss2 > 0) {
                lineMap.put(6, EnumChatFormatting.YELLOW + "Average kill time: " + EnumChatFormatting.RED +
                        Utils.prettyTime((System.currentTimeMillis() - agvSlayerTime)));
            }
            for (int strIndex : NotEnoughUpdates.INSTANCE.config.slayerOverlay.slayerText) {
                if (lineMap.get(strIndex) != null) {
                    overlayStrings.add(lineMap.get(strIndex));
                }
            }
            if (overlayStrings != null && overlayStrings.isEmpty()) overlayStrings = null;
        }
    }
}
