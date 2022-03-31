package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.core.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class PackDevCommand extends ClientCommandBase {

	public PackDevCommand() {
		super("neupackdev");
	}

	EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
	double dist  = 5;
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
			switch (args[0].toLowerCase()) {
				case "getnpc":
					getNPCData();
					break;

				case "getmob":
					value	= getMobData();
					if (value != null) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getmobs":
					value = getMobsData();
					if (value != null) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getarmorstand":
					value = getArmorStandData();
					if (value != null) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getarmorstands":
					value = getArmorStandsData();
					if (value != null) MiscUtils.copyToClipboard(value.toString());
					break;

				case "getall":
					value = getMobsData();
					StringBuilder value2 = getArmorStandsData();
					if (value == null && value2 == null) {
						break;
					} else if (value != null && value2 != null) {
						MiscUtils.copyToClipboard(value.append(value2).toString());
					} else if (value == null) {
						MiscUtils.copyToClipboard(value2.toString());
					} else {
						MiscUtils.copyToClipboard(value.toString());
					}
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

	public void getNPCData() {
		EntityPlayer closestNPC = null;
		for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
			if (player instanceof AbstractClientPlayer && p != player && player.getUniqueID().version() != 4) {
				double dSq = player.getDistanceSq(p.posX, p.posY, p.posZ);
				if (dSq < distSq) {
					distSq = dSq;
					closestNPC = player;
				}
			}
		}

		if (closestNPC == null) {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No NPCs found within " + dist + " blocks. :("));
		} else {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied NPC entity texture id to clipboard"));
			MiscUtils.copyToClipboard(((AbstractClientPlayer) closestNPC)
				.getLocationSkin()
				.getResourcePath()
				.replace("skins/", ""));
		}
	}

	public StringBuilder getMobData(){
		Entity closestMob = null;
		for (Entity mob : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (mob != null && mob != Minecraft.getMinecraft().thePlayer && mob instanceof EntityLiving) {
				double dSq = mob.getDistanceSq(p.posX, p.posY, p.posZ);
				if (dSq < distSq) {
					distSq = dSq;
					closestMob = mob;
				}
			}
		}


		if (closestMob == null) {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No mobs found within" + dist + " blocks. :("));
		} else {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied mob data to clipboard"));

				return mobDataBuilder(closestMob);

		}
		return null;
	}

	public StringBuilder getMobsData(){
		StringBuilder mobStringBuilder = new StringBuilder();
		for (Entity mob : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (mob != null && mob != Minecraft.getMinecraft().thePlayer && mob instanceof EntityLiving &&
				mob.getDistanceSq(p.posX, p.posY, p.posZ) < distSq) {
				mobStringBuilder.append(mobDataBuilder(mob));
			}
		}

		if (mobStringBuilder.toString().equals("")) {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No mobs found within" + dist + " blocks. :("));
		} else {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied mob data to clipboard"));
			return mobStringBuilder;
		}
		return null;
	}

	public StringBuilder getArmorStandData(){
		EntityArmorStand closestArmorStand = null;
		for (Entity armorStand : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (armorStand instanceof EntityArmorStand) {
				double dSq = armorStand.getDistanceSq(p.posX, p.posY, p.posZ);
				if (dSq < distSq) {
					distSq = dSq;
					closestArmorStand = (EntityArmorStand) armorStand;
				}
			}
		}

		if (closestArmorStand == null) {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No armor stands found within " + dist + " blocks. :("));
		} else {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied armor stand data to clipboard"));

			return(armorStandDataBuilder(closestArmorStand));

		}
		return null;
	}

	public StringBuilder getArmorStandsData(){

		StringBuilder armorStandStringBuilder = new StringBuilder();
		for (Entity armorStand : Minecraft.getMinecraft().theWorld.loadedEntityList) {
			if (armorStand instanceof EntityArmorStand &&
				armorStand.getDistanceSq(p.posX, p.posY, p.posZ) < distSq) {
				armorStandStringBuilder.append(armorStandDataBuilder((EntityArmorStand) armorStand));
			}
		}

		if (armorStandStringBuilder.toString().equals("")) {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.RED + "No armor stands found within" + dist + " blocks. :("));
		} else {
			p.addChatMessage(new ChatComponentText(
				EnumChatFormatting.GREEN + "Copied armor stand data to clipboard"));
			return armorStandStringBuilder;
		}
		return null;
	}




	public StringBuilder mobDataBuilder(Entity mob) {
		StringBuilder mobData = new StringBuilder();

		//Preventing Null Pointer Exception
		//Entity Information
		mobData
			.append("Entity Id: ")
			.append(mob.getEntityId() != -1 ? mob.getEntityId() : "null")
			.append("\nMob: ")
			.append(mob.getName() != null ? mob.getName() : "null")
			.append("\nCuston Name: ")
			.append(mob.getCustomNameTag() != null ? mob.getCustomNameTag() : "null");

		//Held Item
		if (((EntityLiving) mob).getHeldItem() != null) {
			mobData
				.append("\nItem: ")
				.append(((EntityLiving) mob).getHeldItem())
				.append("\nItem Display Name: ")
				.append(((EntityLiving) mob).getHeldItem().getDisplayName()!= null ? ((EntityLiving) mob).getHeldItem().getDisplayName() : "null")
				.append("\nItem Tag Compound: ")
				.append(((EntityLiving) mob).getHeldItem().getTagCompound().toString() != null ? ((EntityLiving) mob).getHeldItem().getTagCompound().toString() : "null")
				.append("\nItem Tag Compound Extra Attributes: ")
				.append(((EntityLiving) mob).getHeldItem().getTagCompound().getTag("ExtraAttributes") != null ? ((EntityLiving) mob).getHeldItem().getTagCompound().getTag("ExtraAttributes").toString() : "null");
		} else {
			mobData.append("\nItem: null");
		}

		//Armor
			mobData
				.append("\nBoots: ")
				.append(((EntityLiving) mob).getCurrentArmor(0).getTagCompound() != null ? ((EntityLiving) mob).getCurrentArmor(0).getTagCompound().toString() : "null")
				.append("\nLeggings: ")
				.append(((EntityLiving) mob).getCurrentArmor(1).getTagCompound() != null ? ((EntityLiving) mob).getCurrentArmor(1).getTagCompound() : "null")
				.append("\nChestplate: ")
				.append(((EntityLiving) mob).getCurrentArmor(2).getTagCompound() != null ? ((EntityLiving) mob).getCurrentArmor(2).getTagCompound() : "null")
				.append("\nHelmet: ")
				.append(((EntityLiving) mob).getCurrentArmor(3).getTagCompound() != null ? ((EntityLiving) mob).getCurrentArmor(3).getTagCompound() : "null")
				.append("\n\n");

		return mobData;
	}

	public StringBuilder armorStandDataBuilder(EntityArmorStand armorStand) {
		StringBuilder armorStandData = new StringBuilder();

		//Preventing Null Pointer Exception
		//Entity Information
		armorStandData
			.append("Entity Id: ")
			.append(armorStand.getEntityId())
			.append("\nMob: ")
			.append(armorStand.getName() != null ? armorStand.getName() : "null")
			.append("\nCustom Name: ")
			.append(armorStand.getCustomNameTag() != null ? armorStand.getCustomNameTag() : "null");

		//Held Item
		if (armorStand.getHeldItem() != null) {
			armorStandData
				.append("\nItem: ")
				.append(armorStand.getHeldItem())
				.append("\nItem Display Name: ")
				.append(armorStand.getHeldItem().getDisplayName() != null ? armorStand.getHeldItem().getDisplayName() : "null")
					.append("\nItem Tag Compound: ")
					.append(armorStand.getHeldItem().getTagCompound().toString() != null ? armorStand.getHeldItem().getTagCompound().toString() : "null")
					.append("\nItem Tag Compound Extra Attributes: ")
					.append(armorStand.getHeldItem().getTagCompound().getTag("ExtraAttributes") != null ? armorStand.getHeldItem().getTagCompound().getTag("ExtraAttributes") : "null");

		} else {
			armorStandData.append("\nItem: null");
		}

		//Armor
			armorStandData
				.append("\nBoots: ")
				.append(armorStand.getCurrentArmor(0).getTagCompound() != null ? armorStand.getCurrentArmor(0).getTagCompound() : "null")
				.append("\nLeggings: ")
				.append(armorStand.getCurrentArmor(1).getTagCompound() != null ? armorStand.getCurrentArmor(1).getTagCompound() : "null")
				.append("\nChestplate: ")
				.append(armorStand.getCurrentArmor(2).getTagCompound() != null ? armorStand.getCurrentArmor(2).getTagCompound() : "null")
				.append("\nHelmet: ")
				.append(armorStand.getCurrentArmor(3).getTagCompound() != null ? armorStand.getCurrentArmor(3).getTagCompound() : "null");
		armorStandData.append("\n\n");
		return armorStandData;
	}
}
