package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.config.annotations.*;
import net.minecraft.util.EnumChatFormatting;

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
    @ConfigEditorButton(
    runnableId = 12,
    buttonText = "Info"
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
                    "\u00a7bPitch: \u00a7e69.42\u00a7l\u1D52"}
    )
    @ConfigAccordionId(id = 0)
    public List<Integer> farmingText = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 7, 6));

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
            exampleText = {"\u00a7bCatches: \u00a7e547,860",
                    //"\u00a7bCatches/m: \u00a7e38.29",
                    "\u00a7bFish: \u00a7e12\u00a77 [\u00a7e|||||||||||||||||\u00a78||||||||\u00a77] \u00a7e67%",
                    "\u00a7bCurrent XP: \u00a7e6,734",
                    "\u00a7bRemaining XP: \u00a7e3,265",
                    "\u00a7bXP/h: \u00a7e238,129",
                    //"\u00a7bYaw: \u00a7e68.25\u00a7l\u1D52",
                    "\u00a7bETA: \u00a7e13h12m"}
    )
    @ConfigAccordionId(id = 3)
    public List<Integer> fishingText = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5));

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
}
