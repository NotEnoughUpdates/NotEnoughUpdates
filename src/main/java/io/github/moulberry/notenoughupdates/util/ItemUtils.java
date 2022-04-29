package io.github.moulberry.notenoughupdates.util;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.List;

public class ItemUtils {

	public static ItemStack getCoinItemStack(int coinAmount) {
		ItemStack itemStack = new ItemStack(Items.gold_nugget);
		itemStack.setStackDisplayName("\u00A7r\u00A76" + Utils.formatNumberWithDots(coinAmount) + " Coins");
		return itemStack;
	}

	public static void appendLore(ItemStack is, List<String> moreLore) {
		NBTTagCompound tagCompound = is.getTagCompound();
		if (tagCompound == null) {
			tagCompound = new NBTTagCompound();
		}
		NBTTagCompound display = tagCompound.getCompoundTag("display");
		NBTTagList lore = display.getTagList("Lore", 8);
		for (String s : moreLore) {
			lore.appendTag(new NBTTagString(s));
		}
		display.setTag("Lore", lore);
		tagCompound.setTag("display", display);
		is.setTagCompound(tagCompound);
	}
}
