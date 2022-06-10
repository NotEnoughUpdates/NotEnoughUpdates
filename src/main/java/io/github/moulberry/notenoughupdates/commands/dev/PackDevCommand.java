/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

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
import java.util.function.Supplier;

public class PackDevCommand extends ClientCommandBase {
	static Minecraft mc = Minecraft.getMinecraft();

	public PackDevCommand() {
		super("neupackdev");
	}

	private static final HashMap<String, Command<?, ?>> commands = new HashMap<String, Command<?, ?>>() {{
		put(
			"getnpc",
			new Command<>(
				"NPC",
				() -> mc.theWorld.playerEntities,
				true,
				AbstractClientPlayer.class
			)
		);
		put(
			"getnpcs",
			new Command<>(
				"NPC",
				() -> mc.theWorld.playerEntities,
				false,
				AbstractClientPlayer.class
			)
		);
		put(
			"getmob",
			new Command<>(
				"mob",
				() -> mc.theWorld.loadedEntityList,
				true,
				EntityLiving.class
			)
		);
		put(
			"getmobs",
			new Command<>(
				"mob",
				() -> mc.theWorld.loadedEntityList,
				false,
				EntityLiving.class
			)
		);
		put(
			"getarmorstand",
			new Command<>(
				"armor stand",
				() -> mc.theWorld.loadedEntityList,
				true,
				EntityArmorStand.class
			)
		);
		put(
			"getarmorstands",
			new Command<>(
				"armor stand",
				() -> mc.theWorld.loadedEntityList,
				false,
				EntityArmorStand.class
			)
		);
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
			Command<?, ?> command = commands.get(subCommand);
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
		if (EntityPlayer.class.isAssignableFrom(entity.getClass())) {
			EntityPlayer entityPlayer = (EntityPlayer) entity;

			// NPC Information
			String skinResourcePath = ((AbstractClientPlayer) entityPlayer).getLocationSkin().getResourcePath();
			entityData
				.append("Player Id: ")
				.append(entityPlayer.getUniqueID() != null ? entityPlayer.getUniqueID().toString() : "null")
				.append(entityPlayer.getCustomNameTag() != null ? entityPlayer.getCustomNameTag() : "null")
				.append("\nEntity Texture Id: ")
				.append(skinResourcePath != null ? skinResourcePath.replace("skins/", "") : "null");
		}

		if (!clazz.isAssignableFrom(entity.getClass())) {
			return entityData;
		}

		//Entity Information
		entityData
			.append("Entity Id: ")
			.append(entity.getEntityId())
			.append("\nMob: ")
			.append(entity.getName() != null ? entity.getName() : "null")
			.append("\nCustom Name: ")
			.append(entity.getCustomNameTag() != null ? entity.getCustomNameTag() : "null");

		//Held Item
		if (entity.getHeldItem() != null) {
			entityData
				.append("\nItem: ")
				.append(entity.getHeldItem())
				.append("\nItem Display Name: ")
				.append(entity.getHeldItem().getDisplayName() != null
					? entity.getHeldItem().getDisplayName()
					: "null")
				.append("\nItem Tag Compound: ");
			NBTTagCompound heldItemTagCompound = entity.getHeldItem().getTagCompound();
			if (heldItemTagCompound != null) {
				String heldItemString = heldItemTagCompound.toString();
				NBTBase extraAttrTag = heldItemTagCompound.getTag("ExtraAttributes");
				entityData
					.append(heldItemString != null ? heldItemString : "null")
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

	private static final String[] armorPieceTypes = {"Boots", "Leggings", "Chestplate", "Helmet"};

	public static <T extends EntityLivingBase> StringBuilder armorDataBuilder(T entity) {
		StringBuilder armorData = new StringBuilder();
		for (int i = 0; i < 4; i++) {
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

	static class Command<T extends EntityLivingBase, U extends Entity> {
		String typeFriendlyName;
		Supplier<List<U>> entitySupplier;
		Class<T> clazz;
		boolean single;

		Command(
			String typeFriendlyName,
			Supplier<List<U>> entitySupplier,
			boolean single,
			Class<T> clazz
		) {
			this.typeFriendlyName = typeFriendlyName;
			this.entitySupplier = entitySupplier;
			this.single = single;
			this.clazz = clazz;
		}

		@SuppressWarnings("unchecked")
		public StringBuilder getData(double dist) {
			StringBuilder result = new StringBuilder();
			double distSq = dist * dist;
			T closest = null;
			for (Entity entity : entitySupplier.get()) {
				if (!clazz.isAssignableFrom(entity.getClass()) || entity == mc.thePlayer) {
					continue;
				}
				T entityT = (T) entity;
				double entityDistanceSq = entity.getDistanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
				if (entityDistanceSq < distSq) {
					if (single) {
						distSq = entityDistanceSq;
						closest = entityT;
					} else {
						result.append(livingBaseDataBuilder(entityT, clazz));
					}
				}
			}

			if ((single && closest == null) || (!single && result.length() == 0)) {
				mc.thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED + "No " + typeFriendlyName + "s found within " + dist + " blocks."));
			} else {
				mc.thePlayer.addChatMessage(new ChatComponentText(
					EnumChatFormatting.GREEN + "Copied " + typeFriendlyName + " data to clipboard"));
				return single ? livingBaseDataBuilder(closest, clazz) : result;
			}

			return result;
		}
	}
}
