package io.github.moulberry.notenoughupdates.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.List;

public class ItemUtils {
	public static void appendLore(ItemStack is, List<String> moreLore) {
		NBTTagCompound display = is.getTagCompound().getCompoundTag("display");
		NBTTagList lore = display.getTagList("Lore", 8);
		for (String s : moreLore) {
			lore.appendTag(new NBTTagString(s));
		}
		display.setTag("Lore", lore);
		is.getTagCompound().setTag("display", display);
	}
}
