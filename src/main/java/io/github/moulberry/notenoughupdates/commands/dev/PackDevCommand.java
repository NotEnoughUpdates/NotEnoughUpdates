package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.core.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class PackDevCommand extends ClientCommandBase {
	static Minecraft mc = Minecraft.getMinecraft();
	public PackDevCommand() {
		super("neupackdev");
	}

	@SuppressWarnings("unchecked")
	private static final HashMap<String, Command> commands = new HashMap<String, Command>() {{
		put("getnpc",
			new Command<AbstractClientPlayer>(
				"NPC",
				PackDevCommand::npcDataBuilder,
				() -> (List) mc.theWorld.playerEntities,
				true,
				AbstractClientPlayer.class
			));
		put("getnpcs",
			new Command<AbstractClientPlayer>(
				"NPC",
				PackDevCommand::npcDataBuilder,
				() -> (List) mc.theWorld.playerEntities,
				false,
				AbstractClientPlayer.class
			));
		put("getmob",
			new Command<EntityLiving>(
				"mob",
				PackDevCommand::livingBaseDataBuilder,
				() -> (List) mc.theWorld.loadedEntityList,
				true,
				EntityLiving.class
			));
		put("getmobs",
			new Command<EntityLiving>(
				"mob",
				PackDevCommand::livingBaseDataBuilder,
				() -> (List) mc.theWorld.loadedEntityList,
				false,
				EntityLiving.class
			));
		put("getarmorstand",
			new Command<EntityArmorStand>("armor stand",
				PackDevCommand::livingBaseDataBuilder,
				() -> (List) mc.theWorld.loadedEntityList,
				true,
				EntityArmorStand.class
			));
		put("getarmorstands",
			new Command<EntityArmorStand>("armor stand",
				PackDevCommand::livingBaseDataBuilder,
				() -> (List) mc.theWorld.loadedEntityList,
				false,
				EntityArmorStand.class
			));
	}};

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, commands.keySet()) : null;
	}

	public static void togglePackDeveloperMode(ICommandSender sender) {
		NotEnoughUpdates.INSTANCE.packDevEnabled = !NotEnoughUpdates.INSTANCE.packDevEnabled;
		if (NotEnoughUpdates.INSTANCE.packDevEnabled) {
			sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Enabled pack developer mode."));
		} else {
			sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "Disabled pack developer mode."));
		}
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			togglePackDeveloperMode(sender);
			return;
		}

		double dist = 5.0;
		if (args.length >= 2) {
			try {
				dist = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED + "Invalid distance! Must be a number, defaulting to a radius of 5."));
			}
		}

		StringBuilder output;
		String subCommand = args[0].toLowerCase();
		if (commands.containsKey(subCommand)) {
			Command command = commands.get(subCommand);
			output = command.getData(dist);
		} else if (subCommand.equals("getall")) {
			output = getAll(dist);
		} else if (subCommand.equals("getallclose")) {
			output = getAllClose(dist);
		} else {
			sender.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "Invalid sub-command."));
			return;
		}

		if (output.length() != 0) {
			MiscUtils.copyToClipboard(output.toString());
		}
	}

	private static StringBuilder getAllClose(Double dist) {
		StringBuilder sb = new StringBuilder();
		sb.append(commands.get("getmob").getData(dist));
		sb.append(commands.get("getarmorstand").getData(dist));
		sb.append(commands.get("getnpc").getData(dist));
		return sb;
	}

	private static StringBuilder getAll(Double dist) {
		StringBuilder sb = new StringBuilder();
		sb.append(commands.get("getmobs").getData(dist));
		sb.append(commands.get("getarmorstands").getData(dist));
		sb.append(commands.get("getnpcs").getData(dist));
		return sb;
	}

	public static <T extends EntityLivingBase> StringBuilder livingBaseDataBuilder(T entity, Class<T> clazz) {
		StringBuilder entityData = new StringBuilder();
		if (!clazz.isAssignableFrom(entity.getClass())) {
			return entityData;
		}
		T entityLivingBase = (T) entity;

		//Entity Information
		entityData
			.append("Entity Id: ")
			.append(entity.getEntityId())
			.append("\nMob: ")
			.append(entity.getName() != null ? entity.getName() : "null")
			.append("\nCustom Name: ")
			.append(entity.getCustomNameTag() != null ? entity.getCustomNameTag() : "null");

		//Held Item
		if (entityLivingBase.getHeldItem() != null) {
			entityData
				.append("\nItem: ")
				.append(entityLivingBase.getHeldItem())
				.append("\nItem Display Name: ")
				.append(entityLivingBase.getHeldItem().getDisplayName() != null
					? entityLivingBase.getHeldItem().getDisplayName()
					: "null")
				.append("\nItem Tag Compound: ");
			NBTTagCompound heldItemTagCompound = entityLivingBase.getHeldItem().getTagCompound();
			if (heldItemTagCompound != null) {
				String heldItemString = heldItemTagCompound.toString();
				NBTBase extraAttrTag = heldItemTagCompound.getTag("ExtraAttributes");
				entityData
					.append(heldItemString != null ? heldItemString	: "null")
					.append("\nItem Tag Compound Extra Attributes: ")
					.append(extraAttrTag != null ? extraAttrTag : "null");
			} else {
				entityData.append("null");
			}

		} else {
			entityData.append("\nItem: null");
		}

		entityData.append(armorDataBuilder(entity)).append("\n\n");

		return entityData;
	}

	public static <T extends EntityLivingBase> StringBuilder npcDataBuilder(T entity, Class<T> clazz) {
		StringBuilder entityData = new StringBuilder();
		if (!EntityPlayer.class.isAssignableFrom(entity.getClass())) {
			return entityData;
		}
		EntityPlayer entityPlayer = (EntityPlayer) entity;

		// NPC Information
		String skinResourcePath = ((AbstractClientPlayer) entityPlayer).getLocationSkin().getResourcePath();
		entityData
			.append("Player Id: ")
			.append(entityPlayer.getUniqueID() != null ? entityPlayer.getUniqueID().toString() : "null")
			.append(entityPlayer.getCustomNameTag() != null ? entityPlayer.getCustomNameTag() : "null")
			.append("\nEntity Texture Id: ")
			.append(skinResourcePath != null ? skinResourcePath.replace("skins/", "") : "null");

		entityData.append(livingBaseDataBuilder(entity, clazz));
		return entityData;
	}

	private static final String[] armorPieceTypes = {"Boots", "Leggings", "Chestplate", "Helmet"};
	public static <T extends EntityLivingBase> StringBuilder armorDataBuilder (T entity) {
		StringBuilder armorData = new StringBuilder();
		for (int i=0; i < 4; i++) {
			ItemStack currentArmor = entity.getCurrentArmor(0);
			armorData.append(String.format("\n%s: ", armorPieceTypes[i]));
		 	if (currentArmor == null) {
				armorData.append("null");
			} else {
				armorData.append(currentArmor.getTagCompound() != null ? currentArmor.getTagCompound().toString() : "null");
			}
		}
		return armorData;
	}

	static class Command<T extends Entity> {
		String typeFriendlyName;
		BiFunction<T, Class, StringBuilder> dataBuilder;
		Supplier<List<T>> entitySupplier;
		Class<T> clazz;
		boolean single;

		Command(String typeFriendlyName,
						BiFunction<T, Class, StringBuilder> dataBuilder,
						Supplier<List<T>> entitySupplier,
						boolean single,
						Class<T> clazz) {
			this.typeFriendlyName = typeFriendlyName;
			this.dataBuilder = dataBuilder;
			this.entitySupplier = entitySupplier;
			this.single = single;
			this.clazz = clazz;
		}

		public StringBuilder getData(double dist) {
			StringBuilder result = new StringBuilder();
			double distSq = dist * dist;
			T closest = null;
			for (T entity : entitySupplier.get()) {
				if (!clazz.isAssignableFrom(entity.getClass()) || entity == mc.thePlayer) {
					continue;
				}
				double entitydistanceSq = entity.getDistanceSq(mc.thePlayer.posX,	mc.thePlayer.posY, mc.thePlayer.posZ);
				if (entitydistanceSq <	distSq) {
					if (single) {
						distSq = entitydistanceSq;
						closest = entity;
					} else {
						result.append(dataBuilder.apply(entity, clazz));
					}
				}
			}

			if ((single && closest == null) || (!single && result.length() == 0)) {
				mc.thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED + "No " + typeFriendlyName + "s found within " + dist + " blocks."));
			} else {
				mc.thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.GREEN + "Copied " + typeFriendlyName + " data to clipboard"));
				return single ? dataBuilder.apply(closest, clazz) : result;
			}

			return result;
		}
	}
}
