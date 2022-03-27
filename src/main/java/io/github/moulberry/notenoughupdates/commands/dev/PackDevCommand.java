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

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length >= 1) {
			double distSq = 25;
			EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;

			if (args.length == 2) {
				try {
					distSq = Double.parseDouble(args[1]) * Double.parseDouble(args[1]);
				} catch (NumberFormatException e) {
					sender.addChatMessage(new ChatComponentText(
						EnumChatFormatting.RED + "Invalid distance! Must be a number, defaulting to a radius of 5."));
				}
			}

			switch (args[0].toLowerCase()) {
				case "getnpc":
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
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.RED + "No NPCs found within " + (Double.parseDouble(args[1])) + " blocks. :("));
					} else {
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.GREEN + "Copied NPC entity texture id to clipboard"));
						MiscUtils.copyToClipboard(((AbstractClientPlayer) closestNPC)
							.getLocationSkin()
							.getResourcePath()
							.replace("skins/", ""));
					}
					break;

				case "getmob":
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
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.RED + "No mobs found within" + (Double.parseDouble(args[1])) + " blocks. :("));
					} else {
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.GREEN + "Copied mob data to clipboard"));

						MiscUtils.copyToClipboard(mobDataBuilder(closestMob).toString());

					}
					break;

				case "getmobs":
					String mobData;
					StringBuilder mobStringBuilder = new StringBuilder();
					for (Entity mob : Minecraft.getMinecraft().theWorld.loadedEntityList) {
						if (mob != null && mob != Minecraft.getMinecraft().thePlayer && mob instanceof EntityLiving &&
							mob.getDistanceSq(p.posX, p.posY, p.posZ) < distSq) {
							mobStringBuilder.append(mobDataBuilder(mob));
						}
					}
					mobData = mobStringBuilder.toString();
					if (mobData.equals("")) {
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.RED + "No mobs found within" + (Double.parseDouble(args[1])) + " blocks. :("));
					} else {
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.GREEN + "Copied mob data to clipboard"));
						MiscUtils.copyToClipboard(mobData);
					}
					break;

				case "getarmorstand":
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
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.RED + "No armor stands found within " + (Double.parseDouble(args[1])) + " blocks. :("));
					} else {
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.GREEN + "Copied armor stand data to clipboard"));
						
						MiscUtils.copyToClipboard(armorStandDataBuilder(closestArmorStand).toString());
							
					}
					break;

				case "getarmorstands":
					String armorStandData;
					StringBuilder armorStandStringBuilder = new StringBuilder();
					for (Entity armorStand : Minecraft.getMinecraft().theWorld.loadedEntityList) {
						if (armorStand instanceof EntityArmorStand &&
							armorStand.getDistanceSq(p.posX, p.posY, p.posZ) < distSq) {
							armorStandStringBuilder.append(armorStandDataBuilder((EntityArmorStand) armorStand));
						}
					}
					armorStandData = armorStandStringBuilder.toString();
					if (armorStandData.equals("")) {
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.RED + "No armor stands found within" + (Double.parseDouble(args[1])) + " blocks. :("));
					} else {
						sender.addChatMessage(new ChatComponentText(
							EnumChatFormatting.GREEN + "Copied armor stand data to clipboard"));
						MiscUtils.copyToClipboard(armorStandData);
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

	public StringBuilder mobDataBuilder(Entity mob) {
		StringBuilder mobData = new StringBuilder();

		mobData
			.append("Entity Id: ")
			.append(mob.getEntityId())
			.append("\nMob: ")
			.append(mob.getName());

		//Preventing null pointers, checking armor slots and hand to see if item is present.
		if (((EntityLiving) mob).getHeldItem() != null) {
			mobData
				.append("\nItem: ")
				.append(((EntityLiving) mob).getHeldItem())
				.append("\nItem Display Name: ")
				.append(((EntityLiving) mob).getHeldItem().getDisplayName())
				.append("\nItem Tag Compound: ")
				.append(((EntityLiving) mob).getHeldItem().getTagCompound().toString())
				.append("\nItem Tag Compound Extra Attributes: ")
				.append(((EntityLiving) mob).getHeldItem().getTagCompound().getTag("ExtraAttributes"));
		} else {
			mobData.append("\nItem: null");
		}

		if (((EntityLiving) mob).getCurrentArmor(0) != null) {
			mobData
				.append("\nBoots: ")
				.append(((EntityLiving) mob).getCurrentArmor(0).getTagCompound());
		} else {
			mobData.append("\nBoots: null");
		}

		if (((EntityLiving) mob).getCurrentArmor(1) != null) {
			mobData
				.append("\nLeggings: ")
				.append(((EntityLiving) mob).getCurrentArmor(1).getTagCompound());
		} else {
			mobData.append("\nLeggings: null");
		}

		if (((EntityLiving) mob).getCurrentArmor(2) != null) {
			mobData
				.append("\nChestplate: ")
				.append(((EntityLiving) mob).getCurrentArmor(2).getTagCompound());
		} else {
			mobData.append("\nChestplate: null");
		}

		if (((EntityLiving) mob).getCurrentArmor(3) != null) {
			mobData
				.append("\nHelmet: ")
				.append(((EntityLiving) mob).getCurrentArmor(3).getTagCompound());
		} else {
			mobData.append("\nHelmet: null");
		}
		mobData.append("\n\n");
		return mobData;
	}

	public StringBuilder armorStandDataBuilder(EntityArmorStand armorStand) {
		StringBuilder armorStandData = new StringBuilder();

		armorStandData
			.append("Entity Id: ")
			.append(armorStand.getEntityId())
			.append("\nMob: ")
			.append(armorStand.getName())
			.append("\nCustom Name: ")
			.append(armorStand.getCustomNameTag());

		//Preventing null pointers, checking armor slots and hand to see if item is present.
		if (armorStand.getHeldItem() != null) {
			armorStandData
				.append("\nItem: ")
				.append(armorStand.getHeldItem())
				.append("\nItem Display Name: ")
				.append(armorStand.getHeldItem().getDisplayName());
			if (armorStand.getHeldItem().getTagCompound() != null) {
				armorStandData
					.append("\nItem Tag Compound: ")
					.append(armorStand.getHeldItem().getTagCompound().toString())
					.append("\nItem Tag Compound Extra Attributes: ")
					.append(armorStand.getHeldItem().getTagCompound().getTag("ExtraAttributes"));
			} else {
				armorStandData.append("\nItem Tag Compound: null");
			}
		} else {
			armorStandData.append("\nItem: null");
		}

		if (armorStand.getCurrentArmor(0) != null) {
			armorStandData
				.append("\nBoots: ")
				.append(armorStand.getCurrentArmor(0).getTagCompound());
		} else {
			armorStandData.append("\nBoots: null");
		}

		if (armorStand.getCurrentArmor(1) != null) {
			armorStandData
				.append("\nLeggings: ")
				.append(armorStand.getCurrentArmor(1).getTagCompound());
		} else {
			armorStandData.append("\nLeggings: null");
		}

		if (armorStand.getCurrentArmor(2) != null) {
			armorStandData
				.append("\nChestplate: ")
				.append(armorStand.getCurrentArmor(2).getTagCompound());
		} else {
			armorStandData.append("\nChestplate: null");
		}

		if (armorStand.getCurrentArmor(3) != null) {
			armorStandData
				.append("\nHelmet: ")
				.append(armorStand.getCurrentArmor(3).getTagCompound());
		} else {
			armorStandData.append("\nHelmet: null");
		}
		armorStandData.append("\n\n");
		return armorStandData;
	}
}
