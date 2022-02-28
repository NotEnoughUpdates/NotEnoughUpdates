package io.github.moulberry.notenoughupdates.options;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.core.config.Config;
import io.github.moulberry.notenoughupdates.core.config.Position;
import io.github.moulberry.notenoughupdates.core.config.annotations.Category;
import io.github.moulberry.notenoughupdates.core.config.gui.GuiPositionEditor;
import io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour;
import io.github.moulberry.notenoughupdates.miscgui.GuiInvButtonEditor;
import io.github.moulberry.notenoughupdates.miscgui.NEUOverlayPlacements;
import io.github.moulberry.notenoughupdates.options.customtypes.DebugFlags;
import io.github.moulberry.notenoughupdates.options.seperateSections.*;
import io.github.moulberry.notenoughupdates.options.seperateSections.CustomArmour;
import io.github.moulberry.notenoughupdates.overlays.MiningOverlay;
import io.github.moulberry.notenoughupdates.overlays.OverlayManager;
import io.github.moulberry.notenoughupdates.overlays.TextOverlay;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NEUConfig extends Config {
	private void editOverlay(String activeConfig, TextOverlay overlay, Position position) {
		Vector2f size = overlay.getDummySize();
		int width = (int) size.x;
		int height = (int) size.y;
		Minecraft.getMinecraft().displayGuiScreen(new GuiPositionEditor(position, width, height, () -> {
			overlay.renderDummy();
			OverlayManager.dontRenderOverlay = overlay.getClass();
		}, () -> {
		}, () -> NotEnoughUpdates.INSTANCE.openGui = new GuiScreenElementWrapper(NEUConfigEditor.editor)));
	}

	@Override
	public void executeRunnable(int runnableId) {
		String activeConfigCategory = null;
		if (Minecraft.getMinecraft().currentScreen instanceof GuiScreenElementWrapper) {
			GuiScreenElementWrapper wrapper = (GuiScreenElementWrapper) Minecraft.getMinecraft().currentScreen;
			if (wrapper.element instanceof NEUConfigEditor) {
				activeConfigCategory = ((NEUConfigEditor) wrapper.element).getSelectedCategoryName();
			}
		}

		switch (runnableId) {
			case 0:
				ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/neumap");
				return;
			case 1:
				editOverlay(activeConfigCategory, OverlayManager.miningOverlay, mining.overlayPosition);
				return;
			case 2:
				Minecraft.getMinecraft().displayGuiScreen(new GuiPositionEditor(
					NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarPosition,
					NotEnoughUpdates.INSTANCE.config.mining.drillFuelBarWidth, 12, () -> {
				},
					() -> {
					}, () -> NotEnoughUpdates.INSTANCE.openGui = new GuiScreenElementWrapper(NEUConfigEditor.editor)
				));
				return;
			case 3:
				editOverlay(activeConfigCategory, OverlayManager.farmingOverlay, skillOverlays.farmingPosition);
				return;
			case 4:
				editOverlay(activeConfigCategory, OverlayManager.petInfoOverlay, petOverlay.petInfoPosition);
				return;
			case 5:
				editOverlay(activeConfigCategory, OverlayManager.timersOverlay, miscOverlays.todoPosition);
				return;
			case 6:
				NotEnoughUpdates.INSTANCE.openGui = new NEUOverlayPlacements();
				return;
			case 7:
				NotEnoughUpdates.INSTANCE.openGui = new GuiInvButtonEditor();
				return;
			case 8:
				NotEnoughUpdates.INSTANCE.openGui = new GuiEnchantColour();
				return;
			case 9:
				editOverlay(activeConfigCategory, OverlayManager.bonemerangOverlay, itemOverlays.bonemerangPosition);
				return;
			case 10:
				editOverlay(activeConfigCategory, OverlayManager.crystalHollowOverlay, mining.crystalHollowOverlayPosition);
				return;
			case 11:
				editOverlay(activeConfigCategory, OverlayManager.miningSkillOverlay, skillOverlays.miningPosition);
				return;
			case 12:
				ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/dn");
				return;
			case 13:
				ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/pv");
				return;
			case 14:
				editOverlay(activeConfigCategory, OverlayManager.fishingSkillOverlay, skillOverlays.fishingPosition);
				return;
			case 16:
				ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/neusouls clear");
				return;
			case 17:
				ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/neusouls unclear");
				return;
			case 18:
				editOverlay(activeConfigCategory, OverlayManager.slayerOverlay, slayerOverlay.slayerPosition);
				return;
			case 19:
				editOverlay(activeConfigCategory, OverlayManager.combatSkillOverlay, skillOverlays.combatPosition);

		}
	}

	@Expose
	@Category(
		name = "Misc",
		desc = "Miscellaneous options which don't fit into any other category"
	)
	public Misc misc = new Misc();

	@Expose
	@Category(
		name = "GUI Locations",
		desc = "Edit the GUI locations of everything here"
	)
	public LocationEdit locationedit = new LocationEdit();

	@Expose
	@Category(
		name = "Notifications",
		desc = "Notifications"
	)
	public Notifications notifications = new Notifications();

	@Expose
	@Category(
		name = "Item List",
		desc = "Item List"
	)
	public Itemlist itemlist = new Itemlist();

	@Expose
	@Category(
		name = "Toolbar",
		desc = "Toolbar"
	)
	public Toolbar toolbar = new Toolbar();

	@Expose
	@Category(
		name = "Inventory Buttons",
		desc = "Inventory Buttons"
	)
	public InventoryButtons inventoryButtons = new InventoryButtons();

	@Expose
	@Category(
		name = "Slot Locking",
		desc = "Slot Locking"
	)
	public SlotLocking slotLocking = new SlotLocking();

	@Expose
	@Category(
		name = "Tooltip Tweaks",
		desc = "Tooltip Tweaks"
	)
	public TooltipTweaks tooltipTweaks = new TooltipTweaks();

	@Expose
	@Category(
		name = "Item Overlays",
		desc = "Item Overlays"
	)
	public ItemOverlays itemOverlays = new ItemOverlays();

	@Expose
	@Category(
		name = "Skill Overlays",
		desc = "Skill Overlays"
	)
	public SkillOverlays skillOverlays = new SkillOverlays();

	@Expose
	@Category(
		name = "Todo Overlay",
		desc = "Todo Overlay"
	)
	public MiscOverlays miscOverlays = new MiscOverlays();

	@Expose
	@Category(
		name = "Slayer Overlay",
		desc = "Slayer Overlay"
	)
	public SlayerOverlay slayerOverlay = new SlayerOverlay();

	@Expose
	@Category(
		name = "Storage GUI",
		desc = "Storage GUI"
	)
	public StorageGUI storageGUI = new StorageGUI();

	@Expose
	@Category(
		name = "Dungeons",
		desc = "Dungeons"
	)
	public Dungeons dungeons = new Dungeons();

	@Expose
	@Category(
		name = "Enchanting GUI/Solvers",
		desc = "Enchanting GUI/Solvers"
	)
	public Enchanting enchantingSolvers = new Enchanting();

	@Expose
	@Category(
		name = "Mining",
		desc = "Mining"
	)
	public Mining mining = new Mining();

	@Expose
	@Category(
		name = "Fishing",
		desc = "Fishing"
	)
	public Fishing fishing = new Fishing();

	@Expose
	@Category(
		name = "NEU Auction House",
		desc = "NEU Auction House"
	)
	public NeuAuctionHouse neuAuctionHouse = new NeuAuctionHouse();

	@Expose
	@Category(
		name = "Improved SB Menus",
		desc = "Improved SB Menus"
	)
	public ImprovedSBMenu improvedSBMenu = new ImprovedSBMenu();

	@Expose
	@Category(
		name = "Custom Armour Hud",
		desc = "Custom Armour Hud"
	)
	public CustomArmour customArmour = new CustomArmour();

	@Expose
	@Category(
		name = "Calendar",
		desc = "Calendar"
	)
	public Calendar calendar = new Calendar();

	@Expose
	@Category(
		name = "Trade Menu",
		desc = "Trade Menu"
	)
	public TradeMenu tradeMenu = new TradeMenu();

	@Expose
	@Category(
		name = "Pet Overlay",
		desc = "Pet Overlay"
	)
	public PetOverlay petOverlay = new PetOverlay();

	@Expose
	@Category(
		name = "AH Tweaks",
		desc = "Tweaks for Hypixel's (Not NEU's) Auction House"
	)
	public AHTweaks ahTweaks = new AHTweaks();

	@Expose
	@Category(
		name = "AH/BZ Graph",
		desc = "Graph of auction and bazaar prices"
	)
	public AHGraph ahGraph = new AHGraph();

	@Expose
	@Category(
		name = "Accessory Bag Overlay",
		desc = "Accessory Bag Overlay"
	)
	public AccessoryBag accessoryBag = new AccessoryBag();

	@Expose
	@Category(
		name = "Api Key",
		desc = "Api Key"
	)
	public ApiKey apiKey = new ApiKey();

	@Expose
	public Hidden hidden = new Hidden();

	@Expose
	public DungeonMapConfig dungeonMap = new DungeonMapConfig();

	public static class Hidden {
		@Expose
		public HashMap<String, NEUConfig.HiddenProfileSpecific> profileSpecific = new HashMap<>();
		@Expose
		public HashMap<String, NEUConfig.HiddenLocationSpecific> locationSpecific = new HashMap<>();
		@Expose
		public List<NEUConfig.InventoryButton> inventoryButtons = createDefaultInventoryButtons();

		@Expose
		public DebugFlags debugFlags = new DebugFlags(0);
		@Expose
		public boolean enableItemEditing = false;
		@Expose
		public boolean cacheRenderedItempane = true;
		@Expose
		public boolean autoupdate = true;
		@Expose
		public String overlaySearchBar = "";
		@Expose
		public String overlayQuickCommand = "";
		@Expose
		public boolean dev = false;
		@Expose
		public boolean loadedModBefore = false;
		@Expose
		public String selectedCape = null;
		@Expose
		public int compareMode = 0;
		@Expose
		public int sortMode = 0;
		@Expose
		public ArrayList<Boolean> compareAscending = Lists.newArrayList(true, true, true);
		@Expose
		public ArrayList<String> favourites = new ArrayList<>();
		@Expose
		public ArrayList<String> previousAuctionSearches = new ArrayList<>();
		@Expose
		public ArrayList<String> eventFavourites = new ArrayList<>();
		@Expose
		public ArrayList<String> quickCommands = createDefaultQuickCommands();
		@Expose
		public ArrayList<String> enchantColours = createDefaultEnchantColours();
		@Expose
		public String repoURL = "https://github.com/Moulberry/NotEnoughUpdates-REPO/archive/master.zip";
		@Expose
		public String repoCommitsURL = "https://api.github.com/repos/Moulberry/NotEnoughUpdates-REPO/commits/master";

		@Expose
		public boolean firstTimeSearchFocus = true;

		//These config options were added due to a graphical bug that caused the player to be unable to see the screen
		@Expose
		public boolean disableBrokenCapes = false;

		//Ery wanted to texture himself because its ery
		@Expose
		public boolean npcRetextureOnSelf = false;

	}

	public static ArrayList<String> createDefaultEnchantColours() {
		return Lists.newArrayList(
			"[a-zA-Z\\- ]+:\u003e:9:6:0",
			"[a-zA-Z\\- ]+:\u003e:6:c:0",
			"[a-zA-Z\\- ]+:\u003e:5:5:0",
			"Experience:\u003e:3:5:0",
			"Life Steal:\u003e:3:5:0",
			"Scavenger:\u003e:3:5:0",
			"Looting:\u003e:3:5:0"
		);
	}

	private static ArrayList<String> createDefaultQuickCommands() {
		return new ArrayList<String>() {{
			add(
				"/warp home:Warp Home:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0=");
			add(
				"/warp hub:Warp Hub:eyJ0aW1lc3RhbXAiOjE1NTkyMTU0MTY5MDksInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q3Y2M2Njg3NDIzZDA1NzBkNTU2YWM1M2UwNjc2Y2I1NjNiYmRkOTcxN2NkODI2OWJkZWJlZDZmNmQ0ZTdiZjgifX19");
			add(
				"/warp dungeon_hub:Dungeon Hub:eyJ0aW1lc3RhbXAiOjE1Nzg0MDk0MTMxNjksInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzliNTY4OTViOTY1OTg5NmFkNjQ3ZjU4NTk5MjM4YWY1MzJkNDZkYjljMWIwMzg5YjhiYmViNzA5OTlkYWIzM2QiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQ==");
			add("/craft:Crafting Table:CRAFTING_TABLE");
			add("/storage:Storage:CHEST");
			add("/wardrobe:Wardrobe:LEATHER_CHESTPLATE");
			add("/pets:Pets:BONE");
			add("neuah:NEU Auction House:GOLD_BLOCK");
			add("/bz:Bazaar:GOLD_BARDING");
		}};
	}

	public HiddenProfileSpecific getProfileSpecific() {
		if (SBInfo.getInstance().currentProfile == null) {
			return null;
		}
		return hidden.profileSpecific.computeIfAbsent(
			SBInfo.getInstance().currentProfile,
			k -> new HiddenProfileSpecific()
		);
	}

	public static class HiddenProfileSpecific {

		@Expose
		public long godPotionDuration = 0L;
		@Expose
		public long puzzlerCompleted = 0L;
		@Expose
		public long firstCakeAte = 0L;
		@Expose
		public long fetchurCompleted = 0L;
		@Expose
		public long commissionsCompleted = 0L;
		@Expose
		public long experimentsCompleted = 0L;
		@Expose
		public long cookieBuffRemaining = 0L;
		@Expose
		public List<MiningOverlay.ForgeItem> forgeItems = new ArrayList<>();

		@Expose
		public int commissionMilestone = 0;

		@Expose
		public HashMap<String, Boolean> automatonParts = new HashMap<String, Boolean>() {{
			put("Electron Transmitter", false);
			put("FTX 3070", false);
			put("Robotron Reflector", false);
			put("Superlite Motor", false);
			put("Control Switch", false);
			put("Synthetic Heart", false);
		}};

		@Expose
		public HashMap<String, Boolean> divanMinesParts = new HashMap<String, Boolean>() {{
			put("Scavenged Lapis Sword", false);
			put("Scavenged Golden Hammer", false);
			put("Scavenged Diamond Axe", false);
			put("Scavenged Emerald Hammer", false);
		}};

		@Expose
		public HashMap<String, Integer> crystals = new HashMap<String, Integer>() {{
			put("Jade", 0);
			put("Amber", 0);
			put("Amethyst", 0);
			put("Sapphire", 0);
			put("Topaz", 0);
		}};
		@Expose
		public long dailyGemstonePowderCompleted = 0L;
		@Expose
		public long dailyMithrilPowerCompleted = 0L;
	}

	public HiddenLocationSpecific getLocationSpecific() {
		String location = SBInfo.getInstance().getLocation();
		if (location == null || location.isEmpty()) {
			return null;
		}

		return getLocationSpecific(location);
	}

	public HiddenLocationSpecific getLocationSpecific(String location) {
		return hidden.locationSpecific.computeIfAbsent(location, k -> new HiddenLocationSpecific());
	}

	public static class HiddenLocationSpecific {
		@Expose
		public Map<String, Integer> commissionMaxes = new HashMap<>();
	}

	public static List<InventoryButton> createDefaultInventoryButtons() {
		return new ArrayList<InventoryButton>() {{
			//Below crafting
			add(new InventoryButton(87, 63, null, true, false, false, 0, ""));
			add(new InventoryButton(87 + 21, 63, null, true, false, false, 0, ""));
			add(new InventoryButton(87 + 21 * 2, 63, null, true, false, false, 0, ""));
			add(new InventoryButton(87 + 21 * 3, 63, null, true, false, false, 0, ""));

			//Above crafting
			add(new InventoryButton(87, 5, null, true, false, false, 0, ""));
			add(new InventoryButton(87 + 21, 5, null, true, false, false, 0, ""));
			add(new InventoryButton(87 + 21 * 2, 5, null, true, false, false, 0, ""));
			add(new InventoryButton(87 + 21 * 3, 5, null, true, false, false, 0, ""));

			//Crafting square
			add(new InventoryButton(87, 25, null, true, false, false, 0, ""));
			add(new InventoryButton(87 + 18, 25, null, true, false, false, 0, ""));
			add(new InventoryButton(87, 25 + 18, null, true, false, false, 0, ""));
			add(new InventoryButton(87 + 18, 25 + 18, null, true, false, false, 0, ""));

			//Crafting result
			add(new InventoryButton(143, 35, null, true, false, false, 0, ""));

			//Player menu area
			add(new InventoryButton(60, 8, null, true, false, false, 0, ""));
			add(new InventoryButton(60, 60, null, true, false, false, 0, ""));
			add(new InventoryButton(26, 8, null, true, false, false, 0, ""));
			add(new InventoryButton(26, 60, null, true, false, false, 0, ""));

			//Right side
			for (int i = 0; i < 8; i++) {
				int y = 2 + 20 * i;
				if (y < 80) {
					add(new InventoryButton(2, 2 + 20 * i, null, false, true, false, 0, ""));
				} else {
					add(new InventoryButton(2, 2 + 20 * i - 166, null, false, true, true, 0, ""));
				}
			}

			//Top side
			for (int i = 0; i < 8; i++) {
				add(new InventoryButton(4 + 21 * i, -19, null, false, false, false, 0, ""));
			}

			//Left side
			for (int i = 0; i < 8; i++) {
				int y = 2 + 20 * i;
				if (y < 80) {
					add(new InventoryButton(-19, 2 + 20 * i, null, false, false, false, 0, ""));
				} else {
					add(new InventoryButton(-19, 2 + 20 * i - 166, null, false, false, true, 0, ""));
				}
			}

			//Bottom side
			for (int i = 0; i < 8; i++) {
				add(new InventoryButton(4 + 21 * i, 2, null, false, false, true, 0, ""));
			}
		}};
	}

	public static class InventoryButton {
		@Expose
		public int x;
		@Expose
		public int y;
		@Expose
		public boolean playerInvOnly;

		@Expose
		public boolean anchorRight;
		@Expose
		public boolean anchorBottom;

		@Expose
		public int backgroundIndex;
		@Expose
		public String command;
		@Expose
		public String icon;

		public boolean isActive() {
			return command.trim().length() > 0;
		}

		public InventoryButton(
			int x,
			int y,
			String icon,
			boolean playerInvOnly,
			boolean anchorRight,
			boolean anchorBottom,
			int backgroundIndex,
			String command
		) {
			this.x = x;
			this.y = y;
			this.icon = icon;
			this.playerInvOnly = playerInvOnly;
			this.anchorRight = anchorRight;
			this.anchorBottom = anchorBottom;
			this.backgroundIndex = backgroundIndex;
			this.command = command;
		}
	}

}
