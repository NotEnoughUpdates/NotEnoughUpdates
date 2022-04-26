package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.config.annotations.*;

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
		name = "Edit Pet Info Position",
		desc = "Change the position of the pet info overlay"
	)
	@ConfigEditorButton(
		runnableId = 4,
		buttonText = "Edit"
	)
	public Position petInfoPosition = new Position(-1, -1);

	@Expose
	@ConfigOption(
		name = "Edit Todo Position",
		desc = "Change the position of the Todo overlay"
	)
	@ConfigEditorButton(
		runnableId = 5,
		buttonText = "Edit"
	)
	public Position todoPosition = new Position(100, 0);

	@Expose
	@ConfigOption(
		name = "Edit Bonemerang Overlay Position",
		desc = "Change the position of the Bonemerang overlay"
	)
	@ConfigEditorButton(
		runnableId = 9,
		buttonText = "Edit"
	)
	public Position bonemerangPosition = new Position(-1, -1);

	@Expose
	@ConfigOption(
		name = "Edit Slayer Overlay Position",
		desc = "Change the position of the Slayer overlay"
	)
	@ConfigEditorButton(
		runnableId = 18,
		buttonText = "Edit"
	)
	public Position slayerPosition = new Position(10, 200);

	@ConfigOption(
		name = "Inventory",
		desc = ""
	)
	@ConfigEditorAccordion(id = 1)
	public boolean inventoryAccordion = false;

	@Expose
	@ConfigOption(
		name = "Edit Toolbar Positions",
		desc = "Change the position of the QuickCommands / Search Bar"
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorButton(runnableId = 6, buttonText = "Edit")
	public boolean positionButton = true;

	@Expose
	@ConfigOption(
		name = "Open Button Editor",
		desc = "Open button editor GUI (/neubuttons)"
	)
	@ConfigAccordionId(id = 1)
	@ConfigEditorButton(runnableId = 7, buttonText = "Open")
	public boolean openEditorButton = true;

	@ConfigOption(
		name = "Mining Overlays",
		desc = ""
	)
	@ConfigEditorAccordion(id = 2)
	public boolean miningoverlayAccordion = false;

	@Expose
	@ConfigOption(
		name = "Edit Dwarven Overlay Position",
		desc = "Change the position of the Dwarven Mines information Overlay (commisions, powder & forge statuses)"
	)
	@ConfigEditorButton(
		runnableId = 1,
		buttonText = "Edit"
	)
	@ConfigAccordionId(id = 2)
	public Position overlayPosition = new Position(10, 100);

	@Expose
	@ConfigOption(
		name = "Edit Crystal Overlay Position",
		desc = "Change the position of the Crystal Hollows Overlay"
	)
	@ConfigEditorButton(
		runnableId = 10,
		buttonText = "Edit"
	)
	@ConfigAccordionId(id = 2)
	public Position crystalHollowOverlayPosition = new Position(200, 0);

	@Expose
	@ConfigOption(
		name = "Edit Fuel Bar Position",
		desc = "Change the position of the drill fuel bar"
	)
	@ConfigEditorButton(
		runnableId = 2,
		buttonText = "Edit"
	)
	@ConfigAccordionId(id = 2)
	public Position drillFuelBarPosition = new Position(0, -100, true, false);

	@ConfigOption(
		name = "Skill Overlays",
		desc = ""
	)
	@ConfigEditorAccordion(id = 3)
	public boolean skilloverlayAccordion = false;

	@Expose
	@ConfigOption(
		name = "Edit Farming Overlay Position",
		desc = "Change the position of the Farming overlay"
	)
	@ConfigEditorButton(
		runnableId = 3,
		buttonText = "Edit"
	)
	@ConfigAccordionId(id = 3)
	public Position farmingPosition = new Position(10, 200);

	@Expose
	@ConfigOption(
		name = "Edit Mining Overlay Position",
		desc = "Change the position of the Mining overlay"
	)
	@ConfigEditorButton(
		runnableId = 11,
		buttonText = "Edit"
	)
	@ConfigAccordionId(id = 3)
	public Position miningPosition = new Position(10, 200);

	@Expose
	@ConfigOption(
		name = "Edit Fishing Overlay Position",
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
		name = "Edit Combat Overlay Position",
		desc = "Change the position of the Combat overlay"
	)
	@ConfigEditorButton(
		runnableId = 19,
		buttonText = "Edit"
	)
	@ConfigAccordionId(id = 3)
	public Position combatPosition = new Position(10, 200);
}
