/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures.dev;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.moulconfig.internal.ClipboardUtils;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

@NEUAutoSubscribe
public class AnimatedSkullExporter {

	static RecordingType recordingState = RecordingType.NOT_RECORDING;
	static ArrayList<NBTTagCompound> skullsList = new ArrayList<>();
	public static ArrayList<String> lastSkullsList = new ArrayList<>();

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (!isRecording()) return;

		if (recordingState == RecordingType.HEAD) {
			EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
			ItemStack currentArmor = player.getCurrentArmor(3);
			if (currentArmor != null && currentArmor.hasDisplayName() && currentArmor.getItem() == Items.skull) {
				if (currentArmor.hasTagCompound() && currentArmor.getTagCompound().hasKey("SkullOwner")) {
					NBTTagCompound skullOwner = currentArmor.getTagCompound().getCompoundTag("SkullOwner");
					skullsList.add(skullOwner);
				}
			}
		} else if (recordingState == RecordingType.PET) {
			for (Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
				if (entity instanceof EntityArmorStand) {
					EntityArmorStand armorStand = (EntityArmorStand) entity;
					ItemStack currentArmor = armorStand.getInventory()[0];
					if (currentArmor == null) continue;
					String displayName = currentArmor.getDisplayName();
					if (displayName.contains("'s Head")) {
						if (currentArmor.getItem() == Items.skull && currentArmor.hasTagCompound() &&
							currentArmor.getTagCompound().hasKey("SkullOwner")) {
							NBTTagCompound skullOwner = currentArmor.getTagCompound().getCompoundTag("SkullOwner");
							skullsList.add(skullOwner);
						}
					}
				}
			}
		}
	}

	public static void startRecording(boolean pet) {
		if (isRecording()) {
			restartRecording(pet);
			return;
		}
		if (!pet) {
			recordingState = RecordingType.HEAD;
			Utils.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Started recording skull frames"));
		} else if (pet) {
			recordingState = RecordingType.PET;
			Utils.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Started recording pet skull frames"));
			Utils.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Make sure you are near NO OTHER armour stands"));
			Utils.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] The corner of my island /visit throwpo works"));
		}
		Utils.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Wait for the animation to play out"));
		Utils.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Use /neuskull stop to stop recording"));
	}

	public static void restartRecording(boolean pet) {
		Utils.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Restarting..."));
		AnimatedSkullExporter.finishRecording(pet);
		AnimatedSkullExporter.startRecording(pet);
	}

	public static void finishRecording(boolean save) {
		recordingState = RecordingType.NOT_RECORDING;
		ArrayList<NBTTagCompound> noDuplicates = removeDuplicates(skullsList);
		skullsList.clear();
		if (save) {
			JsonArray jsonArray = new JsonArray();
			for (NBTTagCompound noDuplicate : noDuplicates) {
				String id = noDuplicate.getString("Id");
				String value = noDuplicate.getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString(
					"Value");
				jsonArray.add(new JsonPrimitive(id + ":" + value));
			}
			if (jsonArray.size() == 0) {
				Utils.addChatMessage(EnumChatFormatting.YELLOW + "[NEU] No skull frames recorded.");
				return;
			}
			Utils.addChatMessage(
				EnumChatFormatting.YELLOW + "[NEU] " + jsonArray.size() + " skull frame" + (jsonArray.size() == 1 ? "" : "s") + " copied to clipboard.");
			ClipboardUtils.copyToClipboard(jsonArray.toString());
			lastSkullsList.clear();
			for (int i = 0; i < jsonArray.size(); i++) {
				lastSkullsList.add(jsonArray.get(i).getAsString());
			}
		}
	}

	public static ArrayList<NBTTagCompound> removeDuplicates(ArrayList<NBTTagCompound> list) {
		Set<NBTTagCompound> set = new LinkedHashSet<>();
		set.addAll(list);
		list.clear();
		list.addAll(set);
		return list;
	}

	enum RecordingType {
		NOT_RECORDING,
		HEAD,
		PET
	}

	public static boolean isRecording() {
		return recordingState != RecordingType.NOT_RECORDING;
	}

}
