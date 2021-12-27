package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorDropdown;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorFSR;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class AHGraph {
    @Expose
    @ConfigOption(
            name = "\u00A7cWarning",
            desc = "This feature keeps track of prices while you're online.\nIt could use a quite a bit of storage."
    )
    @ConfigEditorFSR(
            runnableId = 12
    )
    public boolean slotLockWarning = false;

    @Expose
    @ConfigOption(
            name = "GUI Style",
            desc = "Change the style of the graph GUI"
    )
    @ConfigEditorDropdown(
            values = {"Minecraft", "Dark", "PacksHQ Dark", "FSR"}
    )
    public int graphStyle = 0;
}
