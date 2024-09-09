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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

@NEUAutoSubscribe
public class AnimatedSkullExporter {

	public static boolean enabled = false;
	static ArrayList<NBTTagCompound> skullsList = new ArrayList<>();

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (!enabled) return;

		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		ItemStack currentArmor = player.getCurrentArmor(3);
		if (currentArmor != null && currentArmor.hasDisplayName() && currentArmor.getItem() == Items.skull) {
			if (currentArmor.hasTagCompound() && currentArmor.getTagCompound().hasKey("SkullOwner")) {
				NBTTagCompound skullOwner = currentArmor.getTagCompound().getCompoundTag("SkullOwner");
				skullsList.add(skullOwner);
			}
		}

	}

	public static void finishRecording(boolean save) {
		enabled = false;
		ArrayList<NBTTagCompound> noDuplicates = removeDuplicates(skullsList);
		JsonArray jsonArray = new JsonArray();
		for (NBTTagCompound noDuplicate : noDuplicates) {
			String id = noDuplicate.getString("Id");
			String value = noDuplicate.getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString(
				"Value");
			jsonArray.add(new JsonPrimitive(id + ":" + value));
		}
		skullsList.clear();
		if (save) {
			if (jsonArray.size() == 0) {
				Utils.addChatMessage(EnumChatFormatting.YELLOW + "[NEU] No skull frames recorded.");
				return;
			}
			Utils.addChatMessage(
				EnumChatFormatting.YELLOW + "[NEU] " + jsonArray.size() + " skull frames copied to clipboard.");
			ClipboardUtils.copyToClipboard(jsonArray.toString());
		}
	}

	public static ArrayList<NBTTagCompound> removeDuplicates(ArrayList<NBTTagCompound> list) {
		Set<NBTTagCompound> set = new LinkedHashSet<>();
		set.addAll(list);
		list.clear();
		list.addAll(set);
		return list;
	}

}
