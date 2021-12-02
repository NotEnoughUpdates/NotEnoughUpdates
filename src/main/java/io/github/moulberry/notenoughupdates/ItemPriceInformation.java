package io.github.moulberry.notenoughupdates;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.auction.APIManager;
import io.github.moulberry.notenoughupdates.core.config.KeybindHelper;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemPriceInformation {

    public static boolean addToTooltip(List<String> tooltip, String internalname, ItemStack stack) {
        return addToTooltip(tooltip, internalname, stack, true);
    }

    public static boolean addToTooltip(List<String> tooltip, String internalname, ItemStack stack, boolean useStackSize) {
        if (stack.getTagCompound().hasKey("disableNeuTooltip") && stack.getTagCompound().getBoolean("disableNeuTooltip")) {
            return false;
        }
        if (NotEnoughUpdates.INSTANCE.config.tooltipTweaks.disablePriceKey && !KeybindHelper.isKeyDown(NotEnoughUpdates.INSTANCE.config.tooltipTweaks.disablePriceKeyKeybind)) {
            return false;
        }
        JsonObject auctionInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAuctionInfo(internalname);
        JsonObject bazaarInfo = NotEnoughUpdates.INSTANCE.manager.auctionManager.getBazaarInfo(internalname);
        float lowestBinAvg = NotEnoughUpdates.INSTANCE.manager.auctionManager.getItemAvgBin(internalname);

        int lowestBin = NotEnoughUpdates.INSTANCE.manager.auctionManager.getLowestBin(internalname);
        APIManager.CraftInfo craftCost = NotEnoughUpdates.INSTANCE.manager.auctionManager.getCraftCost(internalname);

        boolean auctionItem = lowestBin > 0 || lowestBinAvg > 0;
        boolean auctionInfoErrored = auctionInfo == null;
        if (auctionItem) {
            long currentTime = System.currentTimeMillis();
            long lastUpdate = NotEnoughUpdates.INSTANCE.manager.auctionManager.getLastLowestBinUpdateTime();
            //check if info is older than 10 minutes
            if (currentTime - lastUpdate > 600 * 1000) {
                tooltip.add(EnumChatFormatting.RED + "[NEU] Price info is outdated by more than 10 minutes.\nIt will updated again as soon as the server can be reached again.");
            }
        }

        boolean bazaarItem = bazaarInfo != null;

        NumberFormat format = NumberFormat.getInstance(Locale.US);
        boolean shortNumber = NotEnoughUpdates.INSTANCE.config.tooltipTweaks.shortNumberFormatPrices;
        if (bazaarItem) {
            List<Integer> lines = NotEnoughUpdates.INSTANCE.config.tooltipTweaks.priceInfoBaz;

            boolean added = false;

            boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

            int stackMultiplier = 1;
            int shiftStackMultiplier = useStackSize && stack.stackSize > 1 ? stack.stackSize : 64;
            if (shiftPressed) {
                stackMultiplier = shiftStackMultiplier;
            }

            //values = {"", "Buy", "Sell", "Buy (Insta)", "Sell (Insta)", "Raw Craft Cost"}
            for (int lineId : lines) {
                switch (lineId) {
                    case 0:
                        if (bazaarInfo.has("avg_buy")) {
                            if (!added) {
                                tooltip.add("");
                                if (!shiftPressed)
                                    tooltip.add(EnumChatFormatting.DARK_GRAY + "[SHIFT show x" + shiftStackMultiplier + "]");
                                added = true;
                            }
                            int bazaarBuyPrice = (int) bazaarInfo.get("avg_buy").getAsFloat() * stackMultiplier;
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Bazaar Buy: " +
                                    EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + (shortNumber && bazaarBuyPrice > 1000 ? Utils.shortNumberFormat(bazaarBuyPrice, 0) : format.format(bazaarBuyPrice)) + " coins");
                        }
                        break;
                    case 1:
                        if (bazaarInfo.has("avg_sell")) {
                            if (!added) {
                                tooltip.add("");
                                if (!shiftPressed)
                                    tooltip.add(EnumChatFormatting.DARK_GRAY + "[SHIFT show x" + shiftStackMultiplier + "]");
                                added = true;
                            }
                            int bazaarSellPrice = (int) bazaarInfo.get("avg_sell").getAsFloat() * stackMultiplier;
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Bazaar Sell: " +
                                    EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + (shortNumber && bazaarSellPrice > 1000 ? Utils.shortNumberFormat(bazaarSellPrice, 0) : format.format(bazaarSellPrice)) + " coins");
                        }
                        break;
                    case 2:
                        if (bazaarInfo.has("curr_buy")) {
                            if (!added) {
                                tooltip.add("");
                                if (!shiftPressed)
                                    tooltip.add(EnumChatFormatting.DARK_GRAY + "[SHIFT show x" + shiftStackMultiplier + "]");
                                added = true;
                            }
                            int bazaarInstantBuyPrice = (int) bazaarInfo.get("curr_buy").getAsFloat() * stackMultiplier;
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Bazaar Insta-Buy: " +
                                    EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + (shortNumber && bazaarInstantBuyPrice > 1000 ? Utils.shortNumberFormat(bazaarInstantBuyPrice, 0) : format.format(bazaarInstantBuyPrice)) + " coins");
                        }
                        break;
                    case 3:
                        if (bazaarInfo.has("curr_sell")) {
                            if (!added) {
                                tooltip.add("");
                                if (!shiftPressed)
                                    tooltip.add(EnumChatFormatting.DARK_GRAY + "[SHIFT show x" + shiftStackMultiplier + "]");
                                added = true;
                            }
                            int bazaarInstantSellPrice = (int) bazaarInfo.get("curr_sell").getAsFloat() * stackMultiplier;
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Bazaar Insta-Sell: " +
                                    EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + (shortNumber && bazaarInstantSellPrice > 1000 ? Utils.shortNumberFormat(bazaarInstantSellPrice, 0) : format.format(bazaarInstantSellPrice)) + " coins");
                        }
                        break;
                    case 4:
                        if (craftCost.fromRecipe) {
                            if ((int) craftCost.craftCost == 0) {
                                continue;
                            }
                            if (!added) {
                                tooltip.add("");
                                added = true;
                            }
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Raw Craft Cost: " + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
                                    (shortNumber && craftCost.craftCost > 1000 ? Utils.shortNumberFormat(craftCost.craftCost, 0) : format.format((int) craftCost.craftCost)) + " coins");
                        }
                        break;
                }
            }

            return added;
        } else if (auctionItem) {
            List<Integer> lines = NotEnoughUpdates.INSTANCE.config.tooltipTweaks.priceInfoAuc;

            boolean added = false;

            for (int lineId : lines) {
                switch (lineId) {
                    case 0:
                        if (lowestBin > 0) {
                            if (!added) {
                                tooltip.add("");
                                added = true;
                            }
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Lowest BIN: " +
                                    EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + format.format(lowestBin) + " coins");
                        }
                        break;
                    case 1:
                        if (auctionInfo != null) {
                            if (!added) {
                                tooltip.add("");
                                added = true;
                            }

                            if (auctionInfo.has("clean_price")) {
                                tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AH Price (Clean): " + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
                                        (shortNumber && auctionInfo.get("clean_price").getAsFloat() > 1000 ? Utils.shortNumberFormat(auctionInfo.get("clean_price").getAsFloat(), 0) : format.format((int) auctionInfo.get("clean_price").getAsFloat())
                                                + " coins"));
                            } else {
                                int auctionPrice = (int) (auctionInfo.get("price").getAsFloat() / auctionInfo.get("count").getAsFloat());
                                tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AH Price: " + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
                                        (shortNumber && auctionPrice > 1000 ? Utils.shortNumberFormat(auctionPrice, 0) : format.format(auctionPrice)) + " coins");
                            }

                        }
                        break;
                    case 2:
                        if (auctionInfo != null) {
                            if (!added) {
                                tooltip.add("");
                                added = true;
                            }
                            if (auctionInfo.has("clean_price")) {
                                tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AH Sales (Clean): " +
                                        EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
                                        (auctionInfo.get("clean_sales").getAsFloat() < 2 ? format.format(auctionInfo.get("clean_sales").getAsFloat()) + " sale/day"
                                                : format.format(auctionInfo.get("clean_sales").getAsFloat()) + " sales/day"));
                            } else {
                                tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AH Sales: " +
                                        EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
                                        (auctionInfo.get("sales").getAsFloat() < 2 ? format.format(auctionInfo.get("sales").getAsFloat()) + " sale/day"
                                                : format.format(auctionInfo.get("sales").getAsFloat()) + " sales/day"));
                            }
                        }
                        break;
                    case 3:
                        if (craftCost.fromRecipe) {
                            if ((int) craftCost.craftCost == 0) {
                                continue;
                            }
                            if (!added) {
                                tooltip.add("");
                                added = true;
                            }
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Raw Craft Cost: " + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
                                    (shortNumber && craftCost.craftCost > 1000 ? Utils.shortNumberFormat(craftCost.craftCost, 0) : format.format((int) craftCost.craftCost)) + " coins");
                        }
                        break;
                    case 4:
                        if (lowestBinAvg > 0) {
                            if (!added) {
                                tooltip.add("");
                                added = true;
                            }
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "AVG Lowest BIN: " +
                                    EnumChatFormatting.GOLD + EnumChatFormatting.BOLD +
                                    (shortNumber && lowestBinAvg > 1000 ? Utils.shortNumberFormat(lowestBinAvg, 0) : format.format(lowestBinAvg)) + " coins");
                        }
                        break;
                    case 5:
                        if (Constants.ESSENCECOSTS == null) break;
                        JsonObject essenceCosts = Constants.ESSENCECOSTS;
                        if (!essenceCosts.has(internalname)) {
                            break;
                        }
                        JsonObject itemCosts = essenceCosts.get(internalname).getAsJsonObject();
                        String essenceType = itemCosts.get("type").getAsString();

                        int dungeonItemLevel = -1;
                        if (stack != null && stack.hasTagCompound() &&
                                stack.getTagCompound().hasKey("ExtraAttributes", 10)) {
                            NBTTagCompound ea = stack.getTagCompound().getCompoundTag("ExtraAttributes");

                            if (ea.hasKey("dungeon_item_level", 99)) {
                                dungeonItemLevel = ea.getInteger("dungeon_item_level");
                            }
                        }
                        if (dungeonItemLevel == -1) {
                            int dungeonizeCost = 0;
                            if (itemCosts.has("dungeonize")) {
                                dungeonizeCost = itemCosts.get("dungeonize").getAsInt();
                            }
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Dungeonize Cost: " +
                                    EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + dungeonizeCost + " " + essenceType);
                        } else if (dungeonItemLevel >= 0 && dungeonItemLevel <= 4) {
                            String costType = (dungeonItemLevel + 1) + "";

                            int upgradeCost = itemCosts.get(costType).getAsInt();
                            StringBuilder star = new StringBuilder();
                            for (int i = 0; i <= dungeonItemLevel; i++) {
                                star.append('\u272A');
                            }
                            tooltip.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Upgrade to " +
                                    EnumChatFormatting.GOLD + star + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + ": " +
                                    EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + upgradeCost + " " + essenceType);
                        }
                        break;
                }
            }

            return added;
        } else if (auctionInfoErrored) {
            tooltip.add(EnumChatFormatting.RED.toString() + EnumChatFormatting.BOLD + "[NEU] Can't find price info! Please be patient.");
            return true;
        }

        return false;
    }

}
