package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.*;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TooltipTweaks {
    @ConfigOption(
            name = "Tooltip Price Information",
            desc = ""
    )
    @ConfigEditorAccordion(id = 0)
    public boolean priceInfoAccordion = false;

    @Expose
    @ConfigOption(
            name = "Price Info (Auc)",
            desc = "\u00a7rSelect what price information you would like to see on auctionable item tooltips\n" +
                    "\u00a7eDrag text to rearrange"
    )
    @ConfigEditorDraggableList(
            exampleText = {"\u00a7eLowest BIN",
                    "\u00a7eAH Price",
                    "\u00a7eAH Sales",
                    "\u00a7eRaw Craft Cost",
                    "\u00a7eAVG Lowest BIN",
                    "\u00a7eDungeon Costs"}
    )
    @ConfigAccordionId(id = 0)
    public List<Integer> priceInfoAuc = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 5));

    @Expose
    @ConfigOption(
            name = "Price Info (Baz)",
            desc = "\u00a7rSelect what price information you would like to see on bazaar item tooltips\n" +
                    "\u00a7eDrag text to rearrange"
    )
    @ConfigEditorDraggableList(
            exampleText = {"\u00a7eBuy", "\u00a7eSell", "\u00a7eBuy (Insta)", "\u00a7eSell (Insta)", "\u00a7eRaw Craft Cost"}
    )
    @ConfigAccordionId(id = 0)
    public List<Integer> priceInfoBaz = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));

    @Expose
    @ConfigOption(
            name = "Use Short Number Format",
            desc = "Use Short Numbers (5.1m) instead of 5,130,302"
    )
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 0)
    public boolean shortNumberFormatPrices = true;

    @Expose
    @ConfigOption(
            name = "Price Info (Inv)",
            desc = "Show price information for items in your inventory"
    )
    @ConfigEditorBoolean
    public boolean showPriceInfoInvItem = true;

    @Expose
    @ConfigOption(
            name = "Price Info (AH)",
            desc = "Show price information for auctioned items"
    )
    @ConfigEditorBoolean
    public boolean showPriceInfoAucItem = true;

    @Expose
    @ConfigOption(
            name = "Price info keybind",
            desc = "Only show price info if holding a key."
    )
    @ConfigEditorBoolean
    public boolean disablePriceKey = false;

    @Expose
    @ConfigOption(
            name = "Show Price info Keybind",
            desc = "Hold this key to show a price info tooltip"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_NONE)
    public int disablePriceKeyKeybind = Keyboard.KEY_NONE;

    @Expose
    @ConfigOption(
            name = "Show reforge stats",
            desc = "Show statistics a reforge stone will apply."
    )
    @ConfigEditorBoolean
    public boolean showReforgeStats = true;

    @Expose
    @ConfigOption(
            name = "Missing Enchant List",
            desc = "Show which enchants are missing on an item when pressing LSHIFT"
    )
    @ConfigEditorBoolean
    public boolean missingEnchantList = true;

    @Expose
    @ConfigOption(
            name = "Expand Pet Exp Requirement",
            desc = "Show which the full amount of pet xp required"
    )
    @ConfigEditorBoolean
    public boolean petExtendExp = false;

    @Expose
    @ConfigOption(
            name = "Tooltip Border Colours",
            desc = "Make the borders of tooltips match the rarity of the item (NEU Tooltips Only)"
    )
    @ConfigEditorBoolean
    public boolean tooltipBorderColours = true;

    @Expose
    @ConfigOption(
            name = "Tooltip Border Opacity",
            desc = "Change the opacity of the rarity highlight (NEU Tooltips Only)"
    )
    @ConfigEditorSlider(
            minValue = 0f,
            maxValue = 255f,
            minStep = 1f
    )
    public int tooltipBorderOpacity = 200;
}