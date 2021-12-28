package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.config.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlayerOverlay {
    @Expose
    @ConfigOption(
            name = "\u00A7cWarning",
            desc = "You may have to do 2 bosses before everything shows"
    )
    @ConfigEditorFSR(
            runnableId = 12
    )
    public boolean slayerWarning = false;

    @Expose
    @ConfigOption(
            name = "Slayer Overlay",
            desc = "Toggles the slayer overlay"
    )
    @ConfigEditorBoolean
    public boolean slayerOverlay = false;

    @Expose
    @ConfigOption(
            name = "Slayer Text",
            desc = "\u00a7eDrag text to change the appearance of the overlay"
    )
    @ConfigEditorDraggableList(
            exampleText = {"\u00a7eSlayer: \u00a74Sven",
                    "\u00a7eRNG Meter: \u00a75100%",
                    "\u00a7eLvl: \u00a7d7",
                    "\u00a7eKill time: \u00a7c1:30",
                    "\u00a7eXP: \u00a7d75,450/100,000",
                    "\u00a7eBosses till next Lvl: \u00a7d17",
                    "\u00a7eAverage kill time: \u00a7c3:20"
                    }
    )
    public List<Integer> slayerText = new ArrayList<>(Arrays.asList(0, 1, 4, 5, 3, 6));

    @Expose
    @ConfigOption(
            name = "Slayer Position",
            desc = "Change the position of the Slayer overlay"
    )
    @ConfigEditorButton(
            runnableId = 18,
            buttonText = "Edit"
    )
    public Position slayerPosition = new Position(10, 200);

    @Expose
    @ConfigOption(
            name = "Slayer Style",
            desc = "Change the style of the Slayer overlay"
    )
    @ConfigEditorDropdown(
            values = {"Background", "No Shadow", "Shadow", "Full Shadow"}
    )
    public int slayerStyle = 0;
}
