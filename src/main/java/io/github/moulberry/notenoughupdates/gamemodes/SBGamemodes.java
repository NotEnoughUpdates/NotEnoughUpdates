package io.github.moulberry.notenoughupdates.gamemodes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.HashMap;

public class SBGamemodes {
	private static final Gson gson = new Gson();

	public static final int MODIFIER_DEVILISH = 0b1;
	public static final int MODIFIER_NOBANK = 0b10;
	public static final int MODIFIER_SMALLISLAND = 0b100;

	public static final String MODIFIER_DEVILISH_DISPLAY = EnumChatFormatting.DARK_PURPLE + "Devilish";
	public static final String MODIFIER_NOBANK_DISPLAY = EnumChatFormatting.RED + "No" + EnumChatFormatting.GOLD + "Bank";
	public static final String MODIFIER_SMALLISLAND_DISPLAY = EnumChatFormatting.GREEN + "SmallIsland";

	public static final String MODIFIER_DEVILISH_DESC = EnumChatFormatting.DARK_PURPLE + "Devilish\n" +
		"You are NOT allowed to use fairy souls.";
	public static final String MODIFIER_NOBANK_DESC = EnumChatFormatting.RED + "No" + EnumChatFormatting.GOLD + "Bank\n" +
		"You are NOT allowed to use the bank.";
	public static final String MODIFIER_SMALLISLAND_DESC = EnumChatFormatting.GREEN + "SmallIsland\n" +
		"Your private island is 1/4 the normal size.";

	private static HashMap<String, Gamemode> currentGamemode = new HashMap<>();
	private static long lastDeathExemption = 0;

	public static class Gamemode {
		public HardcoreMode hardcoreMode = HardcoreMode.NORMAL;
		public IronmanMode ironmanMode = IronmanMode.NORMAL;
		public int gamemodeModifiers = 0;

		public boolean locked = true;
	}

	public enum HardcoreMode {
		NORMAL("Normal", "Normal"),
		SOFTCORE(EnumChatFormatting.RED + "Soft" + EnumChatFormatting.DARK_RED + "core\n" +
			"You only have 1 life.\nDying will remove your hardcore status.\nDeaths to the void or 'unknown' are exempted.",
			"You died.", "You fell into the void"
		),
		HARDCORE(EnumChatFormatting.DARK_RED + "Hardcore\n" +
			"You only have 1 life.\nDying will remove your hardcore status.");

		public final String display;
		public final String desc;
		private final String[] exemptions;

		HardcoreMode(String display, String... exemptions) {
			this.display = display.split("\n")[0];
			this.desc = display;
			this.exemptions = exemptions;
		}

		public boolean isExemption(String line) {
			for (String exemption : exemptions) {
				if (line.contains(exemption)) return true;
			}
			return false;
		}
	}

	public enum IronmanMode {
		NORMAL("Normal"),
		IRONMAN(EnumChatFormatting.WHITE + "Ironman\n" +
			"You are NOT allowed to trade or use the auction house.",
			"You   ", "Auction House", "Auctions Browser", "Auction View"
		),
		IRONMANPLUS(EnumChatFormatting.WHITE + "Ironman" + EnumChatFormatting.GOLD + "+\n" +
			"You are NOT allowed to trade, use the auction house or bazaar.",
			"You   ", "Auction House", "Auctions Browser", "Auction View", "Bazaar"
		),
		ULTIMATE_IRONMAN(EnumChatFormatting.DARK_AQUA + "Ultimate " + EnumChatFormatting.WHITE + "Ironman\n" +
			"You are NOT allowed to trade or use the auction house.\n" +
			"You are restricted to 1 inventory. (No containers, no echest, no wardrobe).",
			"You   ", "Auction House", "Auctions Browser", "Auction View", "Chest",
			"Wardrobe", "Weapon Rack", "Shelves"
		),
		ULTIMATE_IRONMANPLUS(
			EnumChatFormatting.DARK_AQUA + "Ultimate " + EnumChatFormatting.WHITE + "Ironman" + EnumChatFormatting.GOLD +
				"+\n" +
				"You are NOT allowed to trade, use the auction house or bazaar.\n" +
				"You are restricted to 1 inventory. (No containers, no echest, no wardrobe).",
			"You   ",
			"Auction House",
			"Auctions Browser",
			"Auction View",
			"Bazaar",
			"Chest",
			"Wardrobe",
			"Weapon Rack",
			"Shelves"
		);

		public final String display;
		public final String desc;
		private final String[] bannedInventories;

		IronmanMode(String display, String... bannedInventories) {
			this.display = display.split("\n")[0];
			this.desc = display;
			this.bannedInventories = bannedInventories;
		}

		public boolean isBanned(String inventoryName) {
			for (String banned : bannedInventories) {
				if (inventoryName.contains(banned + " ") || inventoryName.endsWith(banned)) return true;
			}
			return false;
		}
	}

	public static Gamemode getGamemode() {
		String currentProfile = SBInfo.getInstance().currentProfile;

		if (currentProfile == null || currentProfile.isEmpty()) return null;

		return currentGamemode.computeIfAbsent(currentProfile, k -> new Gamemode());
	}

	public static void loadFromFile() {
		File configDir = NotEnoughUpdates.INSTANCE.manager.configLocation;
		File gamemodeFile = new File(
			configDir,
			"gamemodes/gamemodes-" + Minecraft.getMinecraft().thePlayer.getUniqueID().toString() + ".json"
		);
		gamemodeFile.getParentFile().mkdirs();

		if (!gamemodeFile.exists()) {
			return;
		}

		try (
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(gamemodeFile),
				StandardCharsets.UTF_8
			))
		) {
			String line = reader.readLine();
			String decoded = decrypt(line);
			currentGamemode = gson.fromJson(decoded, GamemodeWrapper.class).currentGamemode;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class GamemodeWrapper {
		private final HashMap<String, Gamemode> currentGamemode;

		public GamemodeWrapper(HashMap<String, Gamemode> currentGamemode) {
			this.currentGamemode = currentGamemode;
		}
	}

	public static void saveToFile() {
		File configDir = NotEnoughUpdates.INSTANCE.manager.configLocation;
		File gamemodeFile = new File(
			configDir,
			"gamemodes/gamemodes-" + Minecraft.getMinecraft().thePlayer.getUniqueID().toString() + ".json"
		);
		gamemodeFile.getParentFile().mkdirs();

		try {
			gamemodeFile.createNewFile();

			try (
				BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(gamemodeFile), StandardCharsets.UTF_8))
			) {
				JsonObject obj = new JsonObject();
				writer.write(encrypt(gson.toJson(new GamemodeWrapper(currentGamemode), GamemodeWrapper.class)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Key getKeyFromPlayerUUID() {
		byte[] bytes = ByteBuffer.allocate(2 * Long.SIZE / Byte.SIZE)
														 .putLong(Minecraft.getMinecraft().thePlayer.getUniqueID().getLeastSignificantBits())
														 .putLong(Minecraft.getMinecraft().thePlayer.getUniqueID().getMostSignificantBits())
														 .array();
		SecretKeySpec key = new SecretKeySpec(bytes, "AES");

		return key;
	}

	public static String encrypt(String value) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPlayerUUID());
			String encrypt = Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
			return encrypt;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String decrypt(String encrypted) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, getKeyFromPlayerUUID());
			byte[] b64Decoded = Base64.getDecoder().decode(encrypted);
			byte[] bytes = cipher.doFinal(b64Decoded);

			return new String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void setGamemode(Gamemode gamemode) {
		String currentProfile = NotEnoughUpdates.INSTANCE.manager.getCurrentProfile();

		if (currentProfile == null || currentProfile.isEmpty()) return;

		currentGamemode.put(currentProfile, gamemode);
	}

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (getGamemode() == null || !NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;

		if (!"Your Island".equals(SBInfo.getInstance().location)) return;

		if ((getGamemode().gamemodeModifiers & MODIFIER_SMALLISLAND) != 0) {
			if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
				if (Math.abs(event.pos.getX()) > 40 || Math.abs(event.pos.getZ()) > 40) {
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
						EnumChatFormatting.YELLOW + "[NPC] Builder" +
							EnumChatFormatting.WHITE + ": Sorry, " + Minecraft.getMinecraft().thePlayer.getName() +
							", due to budget cuts your skyblock island is now only 80 blocks wide."));
					Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
						EnumChatFormatting.AQUA + "(Use " + EnumChatFormatting.YELLOW + "/neugamemodes" +
							EnumChatFormatting.AQUA + " if you would like to build further out)"));

					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;

		if (getGamemode() == null || !NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;

		boolean inDungeons = SBInfo.getInstance().getLocation() != null && SBInfo.getInstance().getLocation().equals(
			"dungeon");

		getGamemode().locked = !(EnumChatFormatting.YELLOW + "Break a log").equals(SBInfo.getInstance().objective);

		IronmanMode ironmanMode = getGamemode().ironmanMode;
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui instanceof GuiChest) {
			GuiChest eventGui = (GuiChest) gui;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();

			if (containerName.equals("Bank") && (getGamemode().gamemodeModifiers & MODIFIER_NOBANK) != 0) {
				Minecraft.getMinecraft().thePlayer.closeScreen();

				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.YELLOW + "[NPC] Banker" +
						EnumChatFormatting.WHITE + ": Hi, " + Minecraft.getMinecraft().thePlayer.getName() +
						", you would like to create an account and make a deposit?"));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.YELLOW + "[NPC] Banker" +
						EnumChatFormatting.WHITE + ": Alright, I've invested your money into ..."));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[" +
					EnumChatFormatting.WHITE + "YouTube" + EnumChatFormatting.RED + "] Nullzee" +
					EnumChatFormatting.WHITE + ": Hows it going everyone, welcome to my ultimate bazaar flipping guide ..."));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.YELLOW + "[NPC] Banker" +
						EnumChatFormatting.WHITE +
						": Hmm, it seems as though the economy has crashed. All your money is gone. Poof. Vanished."));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.YELLOW + "[IDIOT] You" +
						EnumChatFormatting.WHITE + ": ... never again ..."));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.AQUA + "(Use " + EnumChatFormatting.YELLOW + "/neugamemodes" +
						EnumChatFormatting.AQUA + " if you would like to use the bank)"));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
			} else if (containerName.equals("Fairy") && (getGamemode().gamemodeModifiers & MODIFIER_DEVILISH) != 0) {
				Minecraft.getMinecraft().thePlayer.closeScreen();

				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.YELLOW + "[NPC] " + EnumChatFormatting.LIGHT_PURPLE + "Tia the Fairy" +
						EnumChatFormatting.WHITE + ": Oh no, " + Minecraft.getMinecraft().thePlayer.getName() +
						", you have sold your soul to the devil... please go away!"));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.AQUA + "(Use " + EnumChatFormatting.YELLOW + "/neugamemodes" +
						EnumChatFormatting.AQUA + " if you would like to use fairy souls)"));
			} else if (!inDungeons && ironmanMode.isBanned(containerName)) {
				Minecraft.getMinecraft().thePlayer.closeScreen();

				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.AQUA + "You cannot access this inventory/menu because of your"));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					"                " + ironmanMode.display + EnumChatFormatting.AQUA + " status!"));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.AQUA + "(Use " + EnumChatFormatting.YELLOW + "/neugamemodes" +
						EnumChatFormatting.AQUA + " if you would like to downgrade the status)"));
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(""));
			}
		}
	}

	@SubscribeEvent
	public void onChatMessage(ClientChatReceivedEvent event) {
		if (event.type != 0) return;

        /*if(Keyboard.isKeyDown(Keyboard.KEY_K)) {
            boolean has = false;
            for(char c : event.message.getFormattedText().toCharArray()) {
                if((int)c > 200) {
                    if(!has) System.out.println("-----START");
                    has = true;
                    System.out.println((int)c);
                }
            }
            if(has) System.out.println("-----END");
        }*/
		if (getGamemode() == null || !NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) return;

		String message = event.message.getFormattedText();
		if (message.contains("\u2620")) { //Death symbol ( ☠ )
			HardcoreMode hardcoreMode = getGamemode().hardcoreMode;
			if (hardcoreMode != HardcoreMode.NORMAL) {
				if (hardcoreMode.isExemption(message)) {
					lastDeathExemption = System.currentTimeMillis();
				}
			}
		}

		if (System.currentTimeMillis() - lastDeathExemption > 1000 &&
			message.contains("!") && message.startsWith(
			EnumChatFormatting.RESET.toString() + EnumChatFormatting.RED + "You died")) {
			if (getGamemode().hardcoreMode != HardcoreMode.NORMAL) {
				Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED.toString() + EnumChatFormatting.OBFUSCATED + "AAA" +
						EnumChatFormatting.RED + " You have lost your " +
						getGamemode().hardcoreMode.display + EnumChatFormatting.RED + " status! " +
						EnumChatFormatting.RED + EnumChatFormatting.OBFUSCATED + "AAA"));
				getGamemode().hardcoreMode = HardcoreMode.NORMAL;
			}
		}
	}
}
