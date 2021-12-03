package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.config.annotations.*;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillOverlays {
    @ConfigOption(
            name = "Skill Overlay Info",
            desc = ""
    )
    @ConfigEditorAccordion(id = 2)
    public boolean infoAccordion = false;
    @Expose
    @ConfigOption(
            name = "Skill display info",
            desc = "The skill trackers need you to have an \u00A72api key\u00A77 set (if you dont have one set do \u00A72/api new\u00A77)\n" +
                    "For the overlays to show you need a \u00A7bmathematical hoe\u00A77 or an axe with \u00A7bcultivating\u00A77 " +
                    "enchant for farming, a pickaxe with \u00A7bcompact\u00A77 for mining or a rod with \u00A7bexpertise\u00A77"
    )
    @ConfigEditorFSR(
    runnableId = 12,
    buttonText = ""
    )
    @ConfigAccordionId(id = 2)
    public boolean skillInfo = false;
    @ConfigOption(
            name = "Farming",
            desc = ""
    )
    @ConfigEditorAccordion(id = 0)
    public boolean farmingAccordion = false;
    @Expose
    @ConfigOption(
            name = "Enable Farming Overlay",
            desc = "Show an overlay while farming with useful information"
    )
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 0)
    public boolean farmingOverlay = true;

    @Expose
    @ConfigOption(
            name = "Farming Text",
            desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
                    "\u00a7rHold a mathematical hoe or use an axe with cultivating enchantment while gaining farming xp to show the overlay"
    )
    @ConfigEditorDraggableList(
            exampleText = {"\u00a7bCounter: \u00a7e37,547,860",
                    "\u00a7bCrops/m: \u00a7e38.29",
                    "\u00a7bFarm: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
                    "\u00a7bCurrent XP: \u00a7e6,734",
                    "\u00a7bRemaining XP: \u00a7e3,265",
                    "\u00a7bXP/h: \u00a7e238,129",
                    "\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52",
                    "\u00a7bETA: \u00a7e13h12m",
                    "\u00a7bPitch: \u00a7e69.42\u00a7l\u1D52",
                    "\u00a7bCultivating: \u00a7e10,137,945/20,000,000",
                    "\u00a7bCoins/m \u00a7e57,432"}
    )
    @ConfigAccordionId(id = 0)
    public List<Integer> farmingText = new ArrayList<>(Arrays.asList(0, 9, 10, 1, 2, 3, 4, 5, 7, 6));

    @Expose
    @ConfigOption(
            name = "Use BZ Price For Coins/m",
            desc = "Uses the bazzar price instead of NPC price for coins/m"
    )
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 0)
    public boolean useBZPrice = true;

    @Expose
    @ConfigOption(
            name = "Farming Position",
            desc = "Change the position of the Farming overlay"
    )
    @ConfigEditorButton(
            runnableId = 3,
            buttonText = "Edit"
    )
    @ConfigAccordionId(id = 0)
    public Position farmingPosition = new Position(10, 200);

    @Expose
    @ConfigOption(
            name = "Farming Style",
            desc = "Change the style of the Farming overlay"
    )
    @ConfigEditorDropdown(
            values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
    )
    @ConfigAccordionId(id = 0)
    public int farmingStyle = 0;
    @ConfigOption(
            name = "Mining",
            desc = ""
    )
    @ConfigEditorAccordion(id = 1)
    public boolean miningAccordion = false;
    @Expose
    @ConfigOption(
            name = "Enable Mining Overlay",
            desc = "Show an overlay while Mining with useful information"
    )
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 1)
    public boolean miningSkillOverlay = true;

    @Expose
    @ConfigOption(
            name = "Mining Text",
            desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
                    "\u00a7rHold a pickaxe with compact while gaining mining xp to show the overlay"
    )
    @ConfigEditorDraggableList(
            exampleText = {"\u00a7bCompact: \u00a7e547,860",
                    "\u00a7bBlocks/m: \u00a7e38.29",
                    "\u00a7bMine: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
                    "\u00a7bCurrent XP: \u00a7e6,734",
                    "\u00a7bRemaining XP: \u00a7e3,265",
                    "\u00a7bXP/h: \u00a7e238,129",
                    "\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52",
                    "\u00a7bETA: \u00a7e13h12m",
                    "\u00a7bCompact Progress: \u00a7e137,945/150,000"}
    )
    @ConfigAccordionId(id = 1)
    public List<Integer> miningText = new ArrayList<>(Arrays.asList(0, 8, 1, 2, 3, 4, 5, 7));

    @Expose
    @ConfigOption(
            name = "Mining Position",
            desc = "Change the position of the Mining overlay"
    )
    @ConfigEditorButton(
            runnableId = 11,
            buttonText = "Edit"
    )
    @ConfigAccordionId(id = 1)
    public Position miningPosition = new Position(10, 200);

    @Expose
    @ConfigOption(
            name = "Mining Style",
            desc = "Change the style of the Mining overlay"
    )
    @ConfigEditorDropdown(
            values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
    )
    @ConfigAccordionId(id = 1)
    public int miningStyle = 0;

    @ConfigOption(
            name = "Fishing",
            desc = ""
    )
    @ConfigEditorAccordion(id = 3)
    public boolean fishingAccordion = false;
    @Expose
    @ConfigOption(
            name = "Enable Fishing Overlay",
            desc = "Show an overlay while Fishing with useful information"
    )
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 3)
    public boolean FishingSkillOverlay = true;

    @Expose
    @ConfigOption(
            name = "Fishing Text",
            desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
                    "\u00a7rHold a fishing rod with expertise enchantment while gaining fishing xp to show the overlay"
    )
    @ConfigEditorDraggableList(
            exampleText = {"\u00a7bExpertise: \u00a7e7,945/10,000",
                    //"\u00a7bCatches/m: \u00a7e38.29",
                    "\u00a7bFishing: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
                    "\u00a7bCurrent XP: \u00a7e6,734",
                    "\u00a7bRemaining XP: \u00a7e3,265",
                    "\u00a7bXP/h: \u00a7e238,129",
                    //"\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52",
                    "\u00a7bETA: \u00a7e13h12m",
                    //"\u00a7bExpertise Progress: \u00a7e7,945/10,000",
                    "\u00a7bTimer: \u00a7e1m15s"}
    )
    @ConfigAccordionId(id = 3)
    public List<Integer> fishingText = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6));

    @Expose
    @ConfigOption(
            name = "Fishing Position",
            desc = "Change the position of the Fishing overlay"
    )
    @ConfigEditorButton(
            runnableId = 14,
            buttonText = "Edit"
    )
    @ConfigAccordionId(id = 3)
    public Position fishingPosition = new Position(10, 200);

    @Expose
    @ConfigOption(
            name = "Fishing Style",
            desc = "Change the style of the Fishing overlay"
    )
    @ConfigEditorDropdown(
            values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
    )
    @ConfigAccordionId(id = 3)
    public int fishingStyle = 0;

    @Expose
    @ConfigOption(
            name = "Toggle Fishing timer",
            desc = "Start or stop the timer on the fishing overlay\n" +
                    "Also can plays a ding customizable below"
    )
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_END)
    @ConfigAccordionId(id = 3)
    public int fishKey = Keyboard.KEY_END;

    @Expose
    @ConfigOption(
            name = "Fishing Timer Alert",
            desc = "Change the amount of time (seconds) until the timer dings"
    )
    @ConfigEditorSlider(
            minValue = 0,
            maxValue = 600,
            minStep = 20
    )
    @ConfigAccordionId(id = 3)
    public int customFishTimer = 300;

    @ConfigOption(
            name = "Combat",
            desc = ""
    )
    @ConfigEditorAccordion(id = 4)
    public boolean combatAccordion = false;

    @Expose
    @ConfigOption(
            name = "\u00A7cWarning",
            desc = "The combat display will only show if you have a Book of Stats on the item you are using"
    )
    @ConfigEditorFSR(
            runnableId = 12,
            buttonText = ""
    )
    @ConfigAccordionId(id = 4)
    public boolean combatInfo = false;

    @Expose
    @ConfigOption(
            name = "Enable Combat Overlay",
            desc = "Show an overlay while Combat with useful information"
    )
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 4)
    public boolean combatSkillOverlay = true;

    @Expose
    @ConfigOption(
            name = "Combat Text",
            desc = "\u00a7eDrag text to change the appearance of the overlay\n" +
                    "\u00a7rHold an item with Book of Stats to show the display"
    )
    @ConfigEditorDraggableList(
            exampleText = {"\u00a7bKills: \u00a7e547,860",
                    "\u00a7bCombat: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
                    "\u00a7bCurrent XP: \u00a7e6,734",
                    "\u00a7bRemaining XP: \u00a7e3,265",
                    "\u00a7bXP/h: \u00a7e238,129",
                    "\u00a7bETA: \u00a7e13h12m"}
    )
    @ConfigAccordionId(id = 4)
    public List<Integer> combatText = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5));

    @Expose
    @ConfigOption(
            name = "Combat Position",
            desc = "Change the position of the Combat overlay"
    )
    @ConfigEditorButton(
            runnableId = 19,
            buttonText = "Edit"
    )
    @ConfigAccordionId(id = 4)
    public Position combatPosition = new Position(10, 200);

    @Expose
    @ConfigOption(
            name = "Combat Style",
            desc = "Change the style of the Combat overlay"
    )
    @ConfigEditorDropdown(
            values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
    )
    @ConfigAccordionId(id = 4)
    public int combatStyle = 0;

    @Expose
    @ConfigOption(
            name = "Always show combat overlay",
            desc = "Shows combat overlay even if you dont have Book of Stats"
    )
    @ConfigEditorBoolean
    @ConfigAccordionId(id = 4)
    public boolean alwaysShowCombatOverlay = false;
}
