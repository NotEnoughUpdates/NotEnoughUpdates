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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class PackDevCommand extends ClientCommandBase {

	public PackDevCommand() {
		super("neupackdev");
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(
			args,
			"getnpc",
			"getnpcs",
			"getmob",
			"getmobs",
			"getarmorstand",
			"getarmorstands",
			"getallclose",
			"getall"
		) : null;
	}

	double dist = 5;
	double distSq = 25;

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length >= 1) {

			if (args.length == 2) {
				try {
					distSq = Double.parseDouble(args[1]) * Double.parseDouble(args[1]);
					dist = Double.parseDouble(args[1]);
				} catch (NumberFormatException e) {
					sender.addChatMessage(new ChatComponentText(
						EnumChatFormatting.RED + "Invalid distance! Must be a number, defaulting to a radius of 5."));
				}
			}

			StringBuilder value;
			StringBuilder value2;
			StringBuilder value3;

			switch (args[0].toLowerCase()) {
				case "getnpc":
					value = getNPCData();
					if (value.length() != 0) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getnpcs":
					value = getNPCsData();
					if (value.length() != 0) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getmob":
					value = getMobData();
					if (value.length() != 0) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getmobs":
					value = getMobsData();
					if (value.length() != 0) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getarmorstand":
					value = getArmorStandData();
					if (value.length() != 0) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getarmorstands":
					value = getArmorStandsData();
					if (value.length() != 0) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getallclose":
					value = getMobData();
					value2 = getArmorStandData();
					value3 = getNPCData();
					if (value.length() == 0 && value2.length() == 0 && value3.length() == 0) {
						break;
					}
					MiscUtils.copyToClipboard((value.append(value2).append(value3)).toString());
					break;

				case "getall":
					value = getMobsData();
					value2 = getArmorStandsData();
					value3 = getNPCsData();
					if (value.length() == 0 && value2.length() == 0 && value3.length() == 0) {
						break;
					}
					MiscUtils.copyToClipboard((value.append(value2).append(value3)).toString());
					break;

				default:
					break;
			}

		} else {
			NotEnoughUpdates.INSTANCE.packDevEnabled = !NotEnoughUpdates.INSTANCE.packDevEnabled;
			if (NotEnoughUpdates.INSTANCE.packDevEnabled) {
				sender.addChatMessage(new ChatComponentText(
					EnumChatFormatting.GREEN + "Enabled pack developer mode."));
			} else {
				sender.addChatMessage(new ChatComponentText(
					EnumChatFormatting.RED + "Disabled pack developer mode."));
			}
		}
	}

	public StringBuilder getNPCData() {
		EntityPlayer closestNPC = null;
		for (EntityPlayer entityPlayer : Minecraft.getMinecraft().theWorld.playerEntities) {
			if (entityPlayer instanceof AbstractClientPlayer && entityPlayer != Minecraft.getMinecraft().thePlayer) {
				double dSq = entityPlayer.getDistanceSq(
					Minecraft.getMinecraft().thePlayer.posX,
					Minecraft.getMinecraft().thePlayer.posY,
					Minecraft.getMinecraft().thePlayer.posZ
				);
				if (dSq < distSq) {
					distSq = dSq;
					closestNPC = entityPlayer;
				}
			}
		}

		if (closestNPC == null) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No NPCs found within " + dist + " blocks."));
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied NPC data to clipboard"));
			return npcDataBuilder(closestNPC);
		}
		return new StringBuilder();
	}

	public StringBuilder getNPCsData() {
		StringBuilder npcStringBuilder = new StringBuilder();
		for (EntityPlayer entityPlayer : Minecraft.getMinecraft().theWorld.playerEntities) {
			if (entityPlayer instanceof AbstractClientPlayer && entityPlayer != Minecraft.getMinecraft().thePlayer && entityPlayer.getDistanceSq(
				Minecraft.getMinecraft().thePlayer.posX,
				Minecraft.getMinecraft().thePlayer.posY,
				Minecraft.getMinecraft().thePlayer.posZ
			) <
				distSq) {
				npcStringBuilder.append(npcDataBuilder(entityPlayer));
			}
		}

		if (npcStringBuilder.length() == 0) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No NPCs found within " + dist + " blocks."));
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied NPC data to clipboard"));
			return npcStringBuilder;
		}
		return new StringBuilder();
	}

	public StringBuilder getMobData() {
		Entity closestMob = null;
		for (Entity mob : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (mob != null && mob != Minecraft.getMinecraft().thePlayer && mob instanceof EntityLiving) {
				double dSq = mob.getDistanceSq(
					Minecraft.getMinecraft().thePlayer.posX,
					Minecraft.getMinecraft().thePlayer.posY,
					Minecraft.getMinecraft().thePlayer.posZ
				);
				if (dSq < distSq) {
					distSq = dSq;
					closestMob = mob;
				}
			}
		}

		if (closestMob == null) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No mobs found within " + dist + " blocks."));
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied mob data to clipboard"));

			return mobDataBuilder(closestMob);

		}
		return new StringBuilder();
	}

	public StringBuilder getMobsData() {
		StringBuilder mobStringBuilder = new StringBuilder();
		for (Entity mob : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (mob != null && mob != Minecraft.getMinecraft().thePlayer && mob instanceof EntityLiving &&
				mob.getDistanceSq(
					Minecraft.getMinecraft().thePlayer.posX,
					Minecraft.getMinecraft().thePlayer.posY,
					Minecraft.getMinecraft().thePlayer.posZ
				) < distSq) {
				mobStringBuilder.append(mobDataBuilder(mob));
			}
		}

		if (mobStringBuilder.length() == 0) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No mobs found within " + dist + " blocks."));
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied mob data to clipboard"));
			return mobStringBuilder;
		}
		return new StringBuilder();
	}

	public StringBuilder getArmorStandData() {
		EntityArmorStand closestArmorStand = null;
		for (Entity armorStand : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (armorStand instanceof EntityArmorStand) {
				double dSq = armorStand.getDistanceSq(
					Minecraft.getMinecraft().thePlayer.posX,
					Minecraft.getMinecraft().thePlayer.posY,
					Minecraft.getMinecraft().thePlayer.posZ
				);
				if (dSq < distSq) {
					distSq = dSq;
					closestArmorStand = (EntityArmorStand) armorStand;
				}
			}
		}

		if (closestArmorStand == null) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No armor stands found within " + dist + " blocks."));
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied armor stand data to clipboard"));

			return (armorStandDataBuilder(closestArmorStand));

		}
		return new StringBuilder();
	}

	public StringBuilder getArmorStandsData() {

		StringBuilder armorStandStringBuilder = new StringBuilder();
		for (Entity armorStand : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (armorStand instanceof EntityArmorStand &&
				armorStand.getDistanceSq(
					Minecraft.getMinecraft().thePlayer.posX,
					Minecraft.getMinecraft().thePlayer.posY,
					Minecraft.getMinecraft().thePlayer.posZ
				) < distSq) {
				armorStandStringBuilder.append(armorStandDataBuilder((EntityArmorStand) armorStand));
			}
		}

		if (armorStandStringBuilder.length() == 0) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No armor stands found within " + dist + " blocks."));
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied armor stand data to clipboard"));
			return armorStandStringBuilder;
		}
		return new StringBuilder();
	}

	public StringBuilder npcDataBuilder(EntityPlayer entityPlayer) {
		StringBuilder npcData = new StringBuilder();

		//NPC Information
		npcData
			.append("Player Id: ")
			.append(entityPlayer.getUniqueID() != null ? entityPlayer.getUniqueID().toString() : "null")
			.append(entityPlayer.getCustomNameTag() != null ? entityPlayer.getCustomNameTag() : "null")
			.append("\nEntity Texture Id: ")
			.append(
				((AbstractClientPlayer) entityPlayer).getLocationSkin().getResourcePath() != null
					? ((AbstractClientPlayer) entityPlayer)
					.getLocationSkin()
					.getResourcePath()
					.replace("skins/", "")
					: "null")
			.append(entityDataBuilder(entityPlayer)); //Getting the basic entity data

		//Held Item
		if (entityPlayer.getHeldItem() != null) {
			npcData
				.append("\nItem: ")
				.append(entityPlayer.getHeldItem())
				.append("\nItem Display Name: ")
				.append(entityPlayer.getHeldItem().getDisplayName() != null ? entityPlayer
					.getHeldItem()
					.getDisplayName() : "null")
				.append("\nItem Tag Compound: ");
			if (entityPlayer.getHeldItem().getTagCompound() != null) {
				npcData
					.append(entityPlayer.getHeldItem().getTagCompound().toString() != null ? entityPlayer
						.getHeldItem()
						.getTagCompound()
						.toString() : "null")
					.append(entityPlayer.getHeldItem().getTagCompound().toString() != null ? entityPlayer
						.getHeldItem()
						.getTagCompound()
						.toString() : "null")
					.append("\nItem Tag Compound Extra Attributes: ")
						.append(entityPlayer.getHeldItem().getTagCompound().getTag("ExtraAttributes") != null ? entityPlayer
						.getHeldItem()
						.getTagCompound()
						.getTag("ExtraAttributes") : "null");
			} else {
				npcData.append("null");
			}
		} else {
			npcData.append("\nItem: null");
		}

		//Armor
		npcData
			.append(armorDataBuilder(entityPlayer)) //Getting the armor data
			.append("\n\n");
		return npcData;
	}

	public StringBuilder mobDataBuilder(Entity entity) {
		StringBuilder mobData = new StringBuilder();
		EntityLiving entityLiving = (EntityLiving) entity;
		//Entity Information
		mobData.append(entityDataBuilder(entity)); //Getting the basic entity data

		//Held Item
		if (entityLiving.getHeldItem() != null) {
			mobData
				.append("\nItem: ")
				.append(entityLiving.getHeldItem())
				.append("\nItem Display Name: ")
				.append(entityLiving.getHeldItem().getDisplayName() != null ? entityLiving
					.getHeldItem()
					.getDisplayName() : "null")
				.append("\nItem Tag Compound: ");
			if (entityLiving.getHeldItem().getTagCompound() != null) {
				mobData
					.append(entityLiving.getHeldItem().getTagCompound().toString() != null ? entityLiving
						.getHeldItem()
						.getTagCompound()
						.toString() : "null")
					.append(entityLiving.getHeldItem().getTagCompound().toString() != null ? entityLiving
						.getHeldItem()
						.getTagCompound()
						.toString() : "null")
					.append("\nItem Tag Compound Extra Attributes: ")
					.append(entityLiving.getHeldItem().getTagCompound().getTag("ExtraAttributes") != null ? entityLiving
						.getHeldItem()
						.getTagCompound()
						.getTag("ExtraAttributes") : "null");
			} else {
				mobData.append("null");
			}
		} else {
			mobData.append("\nItem: null");
		}

		//Armor
		mobData
			.append(armorDataBuilder(entityLiving)) //Getting the armor data
			.append("\n\n");

		return mobData;
	}

	public StringBuilder armorStandDataBuilder(EntityArmorStand armorStand) {
		StringBuilder armorStandData = new StringBuilder();

		armorStandData.append(entityDataBuilder(armorStand)); //Getting the basic entity data

		//Armor Stands cannot be cast to EntityLiving, so we have to repeat the basic mob data.
		//Held Item
		if (armorStand.getHeldItem() != null) {
			armorStandData
				.append("\nItem: ")
				.append(armorStand.getHeldItem())
				.append("\nItem Display Name: ")
				.append(armorStand.getHeldItem().getDisplayName() != null ? armorStand.getHeldItem().getDisplayName() : "null")
				.append("\nItem Tag Compound: ");
			if (armorStand.getHeldItem().getTagCompound() != null) {
				armorStandData
				.append(armorStand.getHeldItem().getTagCompound().toString() != null ? armorStand
					.getHeldItem()
					.getTagCompound()
					.toString() : "null")
					.append(armorStand.getHeldItem().getTagCompound().toString() != null ? armorStand
						.getHeldItem()
						.getTagCompound()
						.toString() : "null")
					.append("\nItem Tag Compound Extra Attributes: ")
					.append(armorStand.getHeldItem().getTagCompound().getTag("ExtraAttributes") != null ? armorStand
						.getHeldItem()
						.getTagCompound()
						.getTag("ExtraAttributes") : "null");
			} else {
				armorStandData.append("null");
			}

		} else {
			armorStandData.append("\nItem: null");
		}

		//Armor
		armorStandData
			.append(armorDataBuilder(armorStand)) //Getting the basic armor data
			.append("\n\n");

		return armorStandData;
	}

	public StringBuilder entityDataBuilder(Entity entity) {
		StringBuilder entityData = new StringBuilder();
		//Entity Information
		entityData
			.append("Entity Id: ")
			.append(entity.getEntityId())
			.append("\nMob: ")
			.append(entity.getName() != null ? entity.getName() : "null")
			.append("\nCustom Name: ")
			.append(entity.getCustomNameTag() != null ? entity.getCustomNameTag() : "null");
		return entityData;
	}

	public StringBuilder armorDataBuilder (EntityLivingBase entityLivingBase) {
		StringBuilder armorData = new StringBuilder();
			armorData
			.append("\nBoots: ")
			.append(entityLivingBase.getCurrentArmor(0) != null
				?
				(entityLivingBase.getCurrentArmor(0).getTagCompound() != null
					? entityLivingBase
					.getCurrentArmor(0)
					.getTagCompound()
					.toString() : "null")
				: "null")

			.append("\nLeggings: ")
			.append(entityLivingBase.getCurrentArmor(1) != null ? (
				entityLivingBase.getCurrentArmor(1).getTagCompound() != null
					? entityLivingBase
					.getCurrentArmor(1)
					.getTagCompound()
					.toString()
					: "null") : "null")

			.append("\nChestplate: ")
			.append(entityLivingBase.getCurrentArmor(2) != null ? (
				entityLivingBase.getCurrentArmor(2).getTagCompound() != null
					? entityLivingBase
					.getCurrentArmor(2)
					.getTagCompound()
					.toString()
					: "null") : "null")

			.append("\nHelmet: ")
			.append(entityLivingBase.getCurrentArmor(3) != null ? (
				entityLivingBase.getCurrentArmor(3).getTagCompound() != null
					? entityLivingBase
					.getCurrentArmor(3)
					.getTagCompound()
					.toString()
					: "null") : "null");

		return armorData;
	}
}
