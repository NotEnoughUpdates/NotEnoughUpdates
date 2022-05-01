package io.github.moulberry.notenoughupdates.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemUtils {

	public static ItemStack getCoinItemStack(long coinAmount) {
		String uuid = "2070f6cb-f5db-367a-acd0-64d39a7e5d1b";
		String texture =
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0=";
		if (coinAmount >= 100000) {
			uuid = "94fa2455-2881-31fe-bb4e-e3e24d58dbe3";
			texture =
				"eyJ0aW1lc3RhbXAiOjE2MzU5NTczOTM4MDMsInByb2ZpbGVJZCI6ImJiN2NjYTcxMDQzNDQ0MTI4ZDMwODllMTNiZGZhYjU5IiwicHJvZmlsZU5hbWUiOiJsYXVyZW5jaW8zMDMiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M5Yjc3OTk5ZmVkM2EyNzU4YmZlYWYwNzkzZTUyMjgzODE3YmVhNjQwNDRiZjQzZWYyOTQzM2Y5NTRiYjUyZjYiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQo=";
		}
		if (coinAmount >= 10000000) {
			uuid = "0af8df1f-098c-3b72-ac6b-65d65fd0b668";
			texture =
				"ewogICJ0aW1lc3RhbXAiIDogMTYzNTk1NzQ4ODQxNywKICAicHJvZmlsZUlkIiA6ICJmNThkZWJkNTlmNTA0MjIyOGY2MDIyMjExZDRjMTQwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ1bnZlbnRpdmV0YWxlbnQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I5NTFmZWQ2YTdiMmNiYzIwMzY5MTZkZWM3YTQ2YzRhNTY0ODE1NjRkMTRmOTQ1YjZlYmMwMzM4Mjc2NmQzYiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
		}
		ItemStack skull = Utils.createSkull(
			"\u00A7r\u00A76" + Utils.formatNumberWithDots(coinAmount) + " Coins",
			uuid,
			texture
		);
		NBTTagCompound extraAttributes = skull.getTagCompound().getCompoundTag("ExtraAttributes");
		extraAttributes.setString("id", "SKYBLOCK_COIN");
		skull.getTagCompound().setTag("ExtraAttributes", extraAttributes);
		return skull;
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

	public static List<String> getLore(ItemStack is) {
		NBTTagCompound tagCompound = is.getTagCompound();
		if (tagCompound == null) {
			return Collections.emptyList();
		}
		NBTTagList tagList = tagCompound.getCompoundTag("display").getTagList("Lore", 8);
		List<String> list = new ArrayList<>();
		for (int i = 0; i < tagList.tagCount(); i++) {
			list.add(tagList.getStringTagAt(i));
		}
		return list;
	}
}
