/*
 * Copyright (C) 2023 NotEnoughUpdates contributors
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

package io.github.moulberry.notenoughupdates.miscfeatures;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@NEUAutoSubscribe
public class DefaultArmorColour {

	private static Map<ItemStack, Integer> armorColourCache = new IdentityHashMap<>();
	private static Set<String> erroredItems = new HashSet<>();

	public static int getDefaultArmorColour(ItemArmor item, ItemStack stack) {
		if (armorColourCache.containsKey(stack)) return armorColourCache.get(stack);

		JsonObject itemJson = NotEnoughUpdates.INSTANCE.manager
			.createItemResolutionQuery()
			.withItemStack(stack)
			.resolveToItemListJson();

		if (itemJson != null && itemJson.has("nbttag")) {
			try {
				NBTTagCompound nbt = JsonToNBT.getTagFromJson(itemJson.get("nbttag").getAsString());
				NBTTagCompound display;

				if (nbt.hasKey("display") && (display = nbt.getCompoundTag("display")).hasKey("color")) {
					int colour = display.getInteger("color");

					if (colour != 0) {
						armorColourCache.put(stack, colour);
						return colour;
					}
				}
			} catch (NBTException exception) {
				if (!erroredItems.contains(itemJson.get("internalname").getAsString())) {
					erroredItems.add(itemJson.get("internalname").getAsString());
					System.out.println("[NEU] Ran into NBTException whilst converting Json into NBT with the JsonObject: " + itemJson);
					exception.printStackTrace();
				}
			}
		}

		return item.getColor(stack);
	}

	@SubscribeEvent
	public void onWorldChange(WorldEvent.Unload event) {
		armorColourCache.clear();
	}
}
