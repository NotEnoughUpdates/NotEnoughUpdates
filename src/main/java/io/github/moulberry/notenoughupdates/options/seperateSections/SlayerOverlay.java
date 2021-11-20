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
                    "\u00a75RNG Meter: 100%",
                    "\u00a7eSLayer level",
                    "\u00a7eTime Since Last Slayer: \u00a7b1:30",
                    "\u00a7eXP: \u00a7d17"
                    }
    )
    public List<Integer> slayerText = new ArrayList<>(Arrays.asList(0, 1, 4, 3));

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
