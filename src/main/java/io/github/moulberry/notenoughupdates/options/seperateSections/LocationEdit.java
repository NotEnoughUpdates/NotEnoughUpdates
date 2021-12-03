package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigAccordionId;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorButton;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class LocationEdit {
    @Expose
    @ConfigOption(
            name = "Edit Dungeon Map",
            desc = "The NEU dungeon map has it's own editor (/neumap).\n" +
                    "Click the button on the left to open it"
    )
    @ConfigEditorButton(
            runnableId = 0,
            buttonText = "Edit"
    )
    public int editDungeonMap = 0;

    @Expose
    @ConfigOption(
            name = "Overlay Position",
            desc = "Change the position of the Dwarven Mines information Overlay (commisions, powder & forge statuses)"
    )
    @ConfigEditorButton(
            runnableId = 1,
            buttonText = "Edit"
    )
    public Position overlayPosition = new Position(10, 100);

    @Expose
    @ConfigOption(
            name = "Fuel Bar Position",
            desc = "Set the position of the drill fuel bar"
    )
    @ConfigEditorButton(
            runnableId = 2,
            buttonText = "Edit"
    )
    public Position drillFuelBarPosition = new Position(0, -100, true, false);

    @Expose
    @ConfigOption(
            name = "Farming Position",
            desc = "Change the position of the Farming overlay"
    )
    @ConfigEditorButton(
            runnableId = 3,
            buttonText = "Edit"
    )
    public Position farmingPosition = new Position(10, 200);

    @Expose
    @ConfigOption(
            name = "Pet Info Position",
            desc = "The position of the pet info."
    )
    @ConfigEditorButton(
            runnableId = 4,
            buttonText = "Edit"
    )
    public Position petInfoPosition = new Position(-1, -1);

    @Expose
    @ConfigOption(
            name = "Todo Position",
            desc = "Change the position of the Todo overlay"
    )
    @ConfigEditorButton(
            runnableId = 5,
            buttonText = "Edit"
    )
    @ConfigAccordionId(id = 0)
    public Position todoPosition = new Position(100, 0);

    @Expose
    @ConfigOption(
            name = "Edit Toolbar Positions",
            desc = "Edit the position of the QuickCommands / Search Bar"
    )
    @ConfigEditorButton(runnableId = 6, buttonText = "Edit")
    public boolean positionButton = true;

    @Expose
    @ConfigOption(
            name = "Open Button Editor",
            desc = "Open button editor GUI (/neubuttons)"
    )
    @ConfigEditorButton(runnableId = 7, buttonText = "Open")
    public boolean openEditorButton = true;

    @Expose
    @ConfigOption(
            name = "Bonemerang Overlay Position",
            desc = "The position of the Bonemerang overlay."
    )
    @ConfigEditorButton(
            runnableId = 9,
            buttonText = "Edit"
    )
    public Position bonemerangPosition = new Position(-1, -1);

    @Expose
    @ConfigOption(
            name = "Overlay Position",
            desc = "Change the position of the Crystal Hollows Overlay."
    )
    @ConfigEditorButton(
            runnableId = 10,
            buttonText = "Edit"
    )
    public Position crystalHollowOverlayPosition = new Position(200, 0);

    @Expose
    @ConfigOption(
            name = "Mining Position",
            desc = "Change the position of the Mining overlay"
    )
    @ConfigEditorButton(
            runnableId = 11,
            buttonText = "Edit"
    )
    public Position miningPosition = new Position(10, 200);

    @Expose
    @ConfigOption(
            name = "Fishing Position",
            desc = "Change the position of the Fishing overlay"
    )
    @ConfigEditorButton(
            runnableId = 14,
            buttonText = "Edit"
    )
    public Position fishingPosition = new Position(10, 200);

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
            name = "Combat Position",
            desc = "Change the position of the Combat overlay"
    )
    @ConfigEditorButton(
            runnableId = 19,
            buttonText = "Edit"
    )
    public Position combatPosition = new Position(10, 200);

}
