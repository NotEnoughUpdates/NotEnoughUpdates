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

package io.github.moulberry.notenoughupdates.profileviewer.bestiary;

import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.LinkedHashMap;
import java.util.List;

public class BestiaryData {
	private static final LinkedHashMap<ItemStack, List<String>> bestiaryLocations =
		new LinkedHashMap<ItemStack, List<String>>() {{
			put(Utils.createSkull(
				EnumChatFormatting.AQUA + "Private Island",
				"bdee7687-9c85-4e7a-b789-b55e90d21d68",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0="
			), Utils.createList("CAVE_SPIDER", "ENDERMAN", "SKELETON", "SLIME", "SPIDER", "WITCH", "ZOMBIE"
			));
			put(Utils.createSkull(
				EnumChatFormatting.AQUA + "Hub",
				"88208736-41cd-4ed8-8ed7-53179140a7fa",
				"eyJ0aW1lc3RhbXAiOjE1NTkyMTU0MTY5MDksInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q3Y2M2Njg3NDIzZDA1NzBkNTU2YWM1M2UwNjc2Y2I1NjNiYmRkOTcxN2NkODI2OWJkZWJlZDZmNmQ0ZTdiZjgifX19"
			), Utils.createList("CRYPT_GHOUL", "OLD_WOLF", "WOLF", "ZOMBIE_VILLAGER"
			));
			put(Utils.createSkull(
				EnumChatFormatting.AQUA + "Spiders Den",
				"acbeaf98-2081-40c5-b5a3-221a2957d532",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc1NDMxOGEzMzc2ZjQ3MGU0ODFkZmNkNmM4M2E1OWFhNjkwYWQ0YjRkZDc1NzdmZGFkMWMyZWYwOGQ4YWVlNiJ9fX0"
			), Utils.createList("ARACHNE", "ARACHNES_BROOD", "ARACHNES_KEEPER", "BROOD_MOTHER", "DASHER_SPIDER", "GRAVEL_SKELETON",
				"RAIN_SLIME", "SPIDER_JOCKEY", "SPLITTER_SPIDER", "VORACIOUS_SPIDER", "WEAVER_SPIDER"
			));
			put(Utils.createSkull(
				EnumChatFormatting.AQUA + "The End",
				"e39ea8b1-a267-48a9-907a-1b97b85342bc",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg0MGI4N2Q1MjI3MWQyYTc1NWRlZGM4Mjg3N2UwZWQzZGY2N2RjYzQyZWE0NzllYzE0NjE3NmIwMjc3OWE1In19fQ"
			), Utils.createList("DRAGON", "ENDERMAN", "ENDERMITE", "ENDSTONE_PROTECTOR", "OBSIDIAN_DEFENDER", "VOIDLING_EXTREMIST",
				"VOIDLING_FANATIC", "WATCHER", "ZEALOT"
			));
			put(Utils.createSkull(
				EnumChatFormatting.AQUA + "Crimson Isles",
				"d8489bfe-dcd7-41f0-bfbd-fb482bf61ecb",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM2ODdlMjVjNjMyYmNlOGFhNjFlMGQ2NGMyNGU2OTRjM2VlYTYyOWVhOTQ0ZjRjZjMwZGNmYjRmYmNlMDcxIn19fQ"
			), Utils.createList("ASHFANG", "BARBARIAN_DUKE", "BLADESOUL", "BLAZE", "FLAMING_SPIDER", "GHAST", "MAGE_OUTLAW",
				"MAGMA_CUBE", "MAGMA_CUBE_BOSS", "MATCHO", "MUSHROOM_BULL", "PIGMAN", "WITHER_SKELETON", "WITHER_SPECTRE"
			));
			put(Utils.createSkull(
				EnumChatFormatting.AQUA + "Deep Caverns",
				"896b5137-a2dd-4de2-8c63-d5a5649bfc70",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5YTFmMTE0MTUxYjQ1MjEzNzNmMzRiYzE0YzI5NjNhNTAxMWNkYzI1YTY1NTRjNDhjNzA4Y2Q5NmViZmMifX19"
			), Utils.createList("AUTOMATON", "BUTTERFLY", "EMERALD_SLIME", "GHOST", "GOBLIN", "GRUNT", "ICE_WALKER", "LAPIS_ZOMBIE",
				"MINER_SKELETON", "MINER_ZOMBIE", "REDSTONE_PIGMAN", "SLUDGE", "SNEAKY_CREEPER", "THYST", "TREASURE_HOARDER", "WORM", "YOG"
			));
			put(Utils.createSkull(
				EnumChatFormatting.AQUA + "The Park",
				"6473b2ff-0575-4aec-811f-5f0dca2131b6",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTIyMWY4MTNkYWNlZTBmZWY4YzU5Zjc2ODk0ZGJiMjY0MTU0NzhkOWRkZmM0NGMyZTcwOGE2ZDNiNzU0OWIifX19"
			), Utils.createList("HOWLING_SPIRIT", "PACK_SPIRIT", "SOUL_OF_THE_ALPHA"
			));
			put(Utils.createItemStack(Item.getItemFromBlock(Blocks.lit_pumpkin), EnumChatFormatting.AQUA + "Spooky"
			), Utils.createList("CRAZY_WITCH", "HEADLESS_HORSEMAN", "PHANTOM_SPIRIT", "SCARY_JERRY", "TRICK_OR_TREATER",
				"WITHER_GOURD", "WRAITH"
			));
			put(Utils.createSkull(
				EnumChatFormatting.AQUA + "Catacombs",
				"00b3837d-9275-304c-8bf9-656659087e6b",
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY0ZTFjM2UzMTVjOGQ4ZmZmYzM3OTg1YjY2ODFjNWJkMTZhNmY5N2ZmZDA3MTk5ZThhMDVlZmJlZjEwMzc5MyJ9fX0"
			), Utils.createList("ANGRY_ARCHEOLOGIST", "CELLAR_SPIDER", "CRYPT_DREADLORD", "CRYPT_LURKER", "CRYPT_SOULEATER",
				"KING_MIDAS", "LONELY_SPIDER", "LOST_ADVENTURER", "SCARED_SKELETON", "SHADOW_ASSASSIN", "SKELETON_GRUNT", "SKELETON_MASTER",
				"SKELETON_SOLDIER", "SKELETOR", "SNIPER", "SUPER_ARCHER", "SUPER_TANK_ZOMBIE", "TANK_ZOMBIE", "UNDEAD", "UNDEAD_SKELETON",
				"WITHERMANCER", "ZOMBIE_COMMANDER", "ZOMBIE_GRUNT", "ZOMBIE_KNIGHT", "ZOMBIE_SOLDIER"
			));
		}};

	public static LinkedHashMap<ItemStack, List<String>> getBestiaryLocations() {
		return bestiaryLocations;
	}

	private static final LinkedHashMap<String, ItemStack> bestiaryMobs =
		new LinkedHashMap<String, ItemStack>() {{
			// Private Island
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aCave Spider" ,"a8aee72d-0d1d-3db7-8cf8-be1ce6ec2dc4", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE2NDVkZmQ3N2QwOTkyMzEwN2IzNDk2ZTk0ZWViNWMzMDMyOWY5N2VmYzk2ZWQ3NmUyMjZlOTgyMjQifX19"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aEnderman" ,"2005daad-730b-363c-abae-e6f3830816fb", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0="));
			put("ID", Utils.createItemStack(Item.getItemFromBlock(Blocks.skull), EnumChatFormatting.AQUA + "§aSkeleton"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSlime" ,"3b70a2f3-319c-38d5-b7d1-5b2425770184", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSpider" ,"7c63f3cf-a963-311a-aeca-3a075b417806", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aWitch" ,"cf4f97d7-2e1f-3678-9ca3-4a7b9666cc28", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNlNjYwNDE1N2ZjNGFiNTU5MWU0YmNmNTA3YTc0OTkxOGVlOWM0MWUzNTdkNDczNzZlMGVlNzM0MjA3NGM5MCJ9fX0="));
			put("ID", Utils.createItemStack(Item.getItemFromBlock(Blocks.skull), EnumChatFormatting.AQUA + "§aZombie"));

			// Hub
			put("ID", Utils.createItemStack(Items.golden_sword, EnumChatFormatting.AQUA + "§aCrypt Ghoul"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aOld Wolf" ,"26e6f2d9-8a27-3a77-965c-5bd2b5d2dc93", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM1OTUzN2MxNTUzNGY2MWMxY2Q4ODZiYzExODc3NGVkMjIyODBlN2NkYWI2NjEzODcwMTYwYWFkNGNhMzkifX19"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aWolf" ,"7e9af289-f295-3f8c-bd54-58b7667d5759", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjlkMWQzMTEzZWM0M2FjMjk2MWRkNTlmMjgxNzVmYjQ3MTg4NzNjNmM0NDhkZmNhODcyMjMxN2Q2NyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aZombie Villager" ,"3acb9940-fc42-328e-91e8-c9a9a57e8698", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMDhhODc3NmMxNzY0YzNmZTZhNmRkZDQxMmRmY2I4N2Y0MTMzMWRhZDQ3OWFjOTZjMjFkZjRiZjNhYzg5YyJ9fX0="));

			// Spiders Den
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aArachne" ,"7c63f3cf-a963-311a-aeca-3a075b417806", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aArachne's Brood" ,"7c63f3cf-a963-311a-aeca-3a075b417806", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aArachne's Keeper" ,"7c63f3cf-a963-311a-aeca-3a075b417806", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aBrood Mother" ,"d7390e70-1e99-3c24-9b1c-bb098e0bbef1", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2YwNjIyYjM5OThkNDJiMzRkNWJjNzYwYmIyYzgzZmRiYzZlNjhmYWIwNWI3ZWExN2IzNTA5N2VkODExOTBkNiJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aDasher Spider" ,"7c63f3cf-a963-311a-aeca-3a075b417806", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="));
			put("ID", Utils.createItemStack(Item.getItemFromBlock(Blocks.skull), EnumChatFormatting.AQUA + "§aGravel Skeleton"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aRain Slime" ,"3b70a2f3-319c-38d5-b7d1-5b2425770184", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSpider Jockey" ,"4eb8745c-80d2-356b-b4fa-f3ffa74082e7", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzA5MzkzNzNjYWZlNGIxZjUzOTdhYWZkMDlmM2JiMTY2M2U3YjYyOWE0MWE3NWZiZGMxODYwYjZiZjhiNDc1ZiJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSplitter Spider" ,"50010472-fa22-3519-b941-2d6d22f47bf1", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmFjZjY5ZmM3YWY1NDk3YTE3NDE4OTFkMWU1YmYzMmI5NmFlMGQ2YzBiYmQzYzE0NzU4ZWE0NGEwM2M1NzI4MyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aVoracious Spider" ,"3e5474d4-4365-3ea7-b4bc-b4edc54da341", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODMwMDk4NmVkMGEwNGVhNzk5MDRmNmFlNTNmNDllZDNhMGZmNWIxZGY2MmJiYTYyMmVjYmQzNzc3ZjE1NmRmOCJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aWeaver Spider" ,"97414c0c-623b-3df3-b1f6-bbcaddafc7fc", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTIxNDM4ZjY0NmRjMDQ1MTU5NjdlODE5NWNjYzNkMzFlMjNiMDJmOWFhMGFjOTE0ZWRjMjgyMmY5ODM5NGI4NiJ9fX0="));

			// The End
			put("ID", Utils.createItemStack(Item.getItemFromBlock(Blocks.dragon_egg), EnumChatFormatting.AQUA + "§aDragon"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aEnderman" ,"2005daad-730b-363c-abae-e6f3830816fb", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aEndermite" ,"b3224e56-73d2-32f9-9081-a23b7512035b", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aEndstone Protector" ,"a46a9adf-60a3-38f2-a3dd-335d85f1cc10", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJiY2FjZWViNDE2MmY0MDBkNDQ3NDMzMTU5MzJhYzgyMGQzMTE5YWM4OTg2YTAxNjFhNzI2MTYxY2NjOTNmYyJ9fX0="));
			put("ID", Utils.createItemStack(Item.getItemFromBlock(Blocks.skull), EnumChatFormatting.AQUA + "§aObsidian Defender"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§dVoidling Extremist" ,"159dcb01-74e3-382c-87d6-3afa022fb379", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aVoidling Fanatic" ,"e86aab24-6245-3967-bf3d-07e31999b602", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTUzYjdiY2Q1NmYwYjk1Zjg3ZGQ3OWVkMTc2MzZiZWI5ZDgzNDY3NDQwMTQyMjhlYTJmNmIxMTBiMTQ4YzEifX19"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aWatcher" ,"00a702b9-7bad-3205-a04b-52478d8c0e7f", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFhOGZjOGRlNjQxN2I0OGQ0OGM4MGI0NDNjZjUzMjZlM2Q5ZGE0ZGJlOWIyNWZjZDQ5NTQ5ZDk2MTY4ZmMwIn19fQ=="));
			put("ID", Utils.createItemStack(Item.getItemFromBlock(Blocks.ender_chest), EnumChatFormatting.AQUA + "§aZealot"));

			// Crimson Isle



			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aBlaze" ,"118fe834-28aa-3b0d-afe6-f0c52d01afe8", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ=="));

			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aGhast" ,"69725d7d-1933-3dea-87bd-a3052482ab2c", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGU4YTM4ZTlhZmJkM2RhMTBkMTliNTc3YzU1YzdiZmQ2YjRmMmU0MDdlNDRkNDAxN2IyM2JlOTE2N2FiZmYwMiJ9fX0="));

			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aMagma Cube" ,"35f02923-7bec-3869-9ef5-b42a4794cac8", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg5NTdkNTAyM2M5MzdjNGM0MWFhMjQxMmQ0MzQxMGJkYTIzY2Y3OWE5ZjZhYjM2Yjc2ZmVmMmQ3YzQyOSJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§6§lMagma Cube Boss" ,"35f02923-7bec-3869-9ef5-b42a4794cac8", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg5NTdkNTAyM2M5MzdjNGM0MWFhMjQxMmQ0MzQxMGJkYTIzY2Y3OWE5ZjZhYjM2Yjc2ZmVmMmQ3YzQyOSJ9fX0="));


			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aPigman" ,"3fc29372-e78e-3ad6-b0b0-05ca0a84babd", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlOWM2ZTk4NTgyZmZkOGZmOGZlYjMzMjJjZDE4NDljNDNmYjE2YjE1OGFiYjExY2E3YjQyZWRhNzc0M2ViIn19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aWither Skeleton" ,"2141b934-c877-3db1-bc6c-7c9a347ffa95", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk1M2I2YzY4NDQ4ZTdlNmI2YmY4ZmIyNzNkNzIwM2FjZDhlMWJlMTllODE0ODFlYWQ1MWY0NWRlNTlhOCJ9fX0="));

			
			// Deep Caverns
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aAutomaton" ,"a46a9adf-60a3-38f2-a3dd-335d85f1cc10", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJiY2FjZWViNDE2MmY0MDBkNDQ3NDMzMTU5MzJhYzgyMGQzMTE5YWM4OTg2YTAxNjFhNzI2MTYxY2NjOTNmYyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§eButterfly" ,"9dd11ec6-cfea-34df-9336-416c946567bc", "ewogICJ0aW1lc3RhbXAiIDogMTYyNTUxMjE4ODY3NCwKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJJb3lhbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85ZmQ4MDZkZWZkZmRmNTliMWYyNjA5YzhlZTM2NDY2NmRlNjYxMjdhNjIzNDE1YjU0MzBjOTM1OGM2MDFlZjdjIgogICAgfQogIH0KfQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aEmerald Slime" ,"cb762e0d-a1e6-3888-8c05-eddabbbe49a2", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTc3NGU4NmFhNGNmZjc5MjM5NWI3N2FkZDU3YjAwYmIxYTEwMmY4ZjBmMDk4MGY0ZDU1YjNkN2FmZjFlNmRhOSJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aGhost" ,"c5752211-7503-3e77-9890-d1cf6ba1d0e7", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgxOTc3OTE4YTExODBlMGRlYzg3OWU2YmNkMWFhMzk0OTQ5NzdiYjkxM2JlMmFiMDFhZmYxZGIxZmE0In19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aGoblin" ,"7c7d07db-4911-31f1-9a19-1589899cfe25", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZiOTcyZTMyZDc2MWIxOTI2MjZlNWQ2ZDAxZWRjMDk0OTQwOTEwMTAzY2VhNWUyZTJkMWYyMzFhZGI3NTVkNSJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aGrunt" ,"a64ccd19-2a64-39a4-b2f5-cb6799c12a99", "ewogICJ0aW1lc3RhbXAiIDogMTYxODE5NTA2MDUwMCwKICAicHJvZmlsZUlkIiA6ICI0ZTMwZjUwZTdiYWU0M2YzYWZkMmE3NDUyY2ViZTI5YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfdG9tYXRvel8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY1ZTAzYjhkZTExOWY4NTg5YTgwODIyNGNiZWE3MzdmNWRjZjI0MjM1Nzk5YjczNzhhYzViZjA2YWJmNmRkNCIKICAgIH0KICB9Cn0="));


			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aMiner Skeleton" ,"39c843e6-237b-36b2-8a7b-c5ff5d3ebf99", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM2YmJjNDIxNWNlYTFiNmE0ODRlODkzYjExNmU3MzQ1OWVmMzZiZmZjNjIyNzQxZTU3N2U5NDkzYTQxZTZlIn19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aMiner Zombie" ,"468210c9-f4bd-34c7-aa8d-2c3d0d5e05c1", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI4ZDlmZjU0MTg4YTFhZmVlNjViOTRmM2JmY2NlMzIxYzY0M2EzNDU5MGMxNGIxOTJiMmUzZWMyZjUyNWQzIn19fQ=="));

			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSludge" ,"3b70a2f3-319c-38d5-b7d1-5b2425770184", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="));

			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aThyst" ,"b3224e56-73d2-32f9-9081-a23b7512035b", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aTreasure Hoarder" ,"b0f13fc2-07a5-3964-8303-784f802e5f0f", "ewogICJ0aW1lc3RhbXAiIDogMTU5MDE1NjYzNDYzOCwKICAicHJvZmlsZUlkIiA6ICI5MWZlMTk2ODdjOTA0NjU2YWExZmMwNTk4NmRkM2ZlNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJoaGphYnJpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iMmIxMmE4MTRjZWQ4YWYwMmNkZGYyOWEzN2U3ZjMwMTFlNDMwZThhMThiMzhiNzA2ZjI3YzZiZDMxNjUwYjY1IgogICAgfQogIH0KfQ=="));

			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aYog" ,"35f02923-7bec-3869-9ef5-b42a4794cac8", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg5NTdkNTAyM2M5MzdjNGM0MWFhMjQxMmQ0MzQxMGJkYTIzY2Y3OWE5ZjZhYjM2Yjc2ZmVmMmQ3YzQyOSJ9fX0="));

			// The Park
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§bHowling Spirit" ,"802a167c-cbcd-3a1f-becd-5b1a25a4cf15", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdjOGJlZjZiZWI3N2UyOWFmODYyN2VjZGMzOGQ4NmFhMmZlYTdjY2QxNjNkYzczYzAwZjlmMjU4ZjlhMTQ1NyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§bPack Spirit" ,"802a167c-cbcd-3a1f-becd-5b1a25a4cf15", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdjOGJlZjZiZWI3N2UyOWFmODYyN2VjZGMzOGQ4NmFhMmZlYTdjY2QxNjNkYzczYzAwZjlmMjU4ZjlhMTQ1NyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§3Soul of the Alpha" ,"802a167c-cbcd-3a1f-becd-5b1a25a4cf15", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdjOGJlZjZiZWI3N2UyOWFmODYyN2VjZGMzOGQ4NmFhMmZlYTdjY2QxNjNkYzczYzAwZjlmMjU4ZjlhMTQ1NyJ9fX0="));

			// Spooky
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§8Crazy Witch" ,"cf4f97d7-2e1f-3678-9ca3-4a7b9666cc28", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNlNjYwNDE1N2ZjNGFiNTU5MWU0YmNmNTA3YTc0OTkxOGVlOWM0MWUzNTdkNDczNzZlMGVlNzM0MjA3NGM5MCJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§6Headless Horseman" ,"2594a979-1302-3d6e-a1da-c9dbf0959539", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGM2NTcwZjEyNDI5OTJmNmViYTIzZWU1ODI1OThjMzllM2U3NDUzODMyNzNkZWVmOGIzOTc3NTgzZmUzY2Y1In19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§cPhantom Spirit" ,"805d7035-5f25-37ea-8530-7c0d09156c8e", "ewogICJ0aW1lc3RhbXAiIDogMTYwMzcyMjc5NzYzNywKICAicHJvZmlsZUlkIiA6ICJhMmY4MzQ1OTVjODk0YTI3YWRkMzA0OTcxNmNhOTEwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiUHVuY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjUzYjJmN2M1ZTE3N2JkNjdjZWFkMzBkMGVlNTM0MjVjNzY4NGM5NzVjOGMyYTUyNzNhMDljYTQ5YTFmNmNkZCIKICAgIH0KICB9Cn0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§6Scary Jerry" ,"127e3dec-4ab7-3798-9410-5fce3f227632", "ewogICJ0aW1lc3RhbXAiIDogMTYwMzczMzU4OTcxOSwKICAicHJvZmlsZUlkIiA6ICJhMmY4MzQ1OTVjODk0YTI3YWRkMzA0OTcxNmNhOTEwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiUHVuY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYyMDJkYzI0ZDE1ZjdjZTM2ZTAyZmI0YjNlODE1M2IxNDZhYjljMTcyNGFhYTVkNDg0Yzc0MWRhMGVlYjZmZCIKICAgIH0KICB9Cn0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§eTrick or Treater" ,"79dd9434-1fde-3aac-87a7-bb09d91eba77", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYmUwNjFiNDQ1Yjg4Y2IyZGY1OWFjY2M4ZDJjMWMxMjExOGZlMGIyMTI3ZTZlNzU4MTM1NTBhZGFjNjdjZiJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§6Wither Gourd" ,"3263c14e-c555-365e-a244-0ee97a8b2056", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjhmMmZmYzZmYjRlOTk1OWI5YTdhMzE3ZjUxYTY3NzVhMTU5ZGRjMjI0MWRiZDZjNzc0ZDNhYzA4YjYifX19"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§8Wraith" ,"bca22b11-8e4c-386a-8824-7b2bd6364cde", "ewogICJ0aW1lc3RhbXAiIDogMTYwMzczMzcxNjI0MiwKICAicHJvZmlsZUlkIiA6ICJhMmY4MzQ1OTVjODk0YTI3YWRkMzA0OTcxNmNhOTEwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiUHVuY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWVhYmMzMDc1Y2Y0MWYzOGU2ZGYxMjM2Yjk1Y2FhZmNiYTFiZWUyMmM0OWQ4MDRiOTQyNzQ4OGMyZjZlMGVmYyIKICAgIH0KICB9Cn0="));

			// Dungeons
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§d§lAngry Archeologist" ,"db784d7a-fae1-3d60-9a5a-42a1814037f8", "eyJ0aW1lc3RhbXAiOjE1NzU0NzAzOTQwMzEsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M0OGM3ODM0NThlNGNmODUxOGU4YWI1ODYzZmJjNGNiOTQ4ZjkwNTY4ZWViOWE2MGQxNmM0ZmRlMmI5NmMwMzMifX19"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aCellar Spider" ,"a8aee72d-0d1d-3db7-8cf8-be1ce6ec2dc4", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE2NDVkZmQ3N2QwOTkyMzEwN2IzNDk2ZTk0ZWViNWMzMDMyOWY5N2VmYzk2ZWQ3NmUyMjZlOTgyMjQifX19"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aCrypt Dreadlord" ,"68b4c885-7447-3382-b86b-b661b464d76e", "eyJ0aW1lc3RhbXAiOjE1NjI0Mjc0MTA5MTQsInByb2ZpbGVJZCI6ImIwZDczMmZlMDBmNzQwN2U5ZTdmNzQ2MzAxY2Q5OGNhIiwicHJvZmlsZU5hbWUiOiJPUHBscyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZmMzQ5MjcwYTNiODUxODk2Y2RhZDg0MmY1ZWVjNmUxNDBiZDkxMTliNzVjMDc0OTU1YzNiZTc4NjVlMjdjNyJ9fX0="));

			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aCrypt Souleater" ,"68b4c885-7447-3382-b86b-b661b464d76e", "eyJ0aW1lc3RhbXAiOjE1NjI0Mjc0MTA5MTQsInByb2ZpbGVJZCI6ImIwZDczMmZlMDBmNzQwN2U5ZTdmNzQ2MzAxY2Q5OGNhIiwicHJvZmlsZU5hbWUiOiJPUHBscyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZmMzQ5MjcwYTNiODUxODk2Y2RhZDg0MmY1ZWVjNmUxNDBiZDkxMTliNzVjMDc0OTU1YzNiZTc4NjVlMjdjNyJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§d§lKing Midas" ,"1a85d923-f8dd-35b8-899a-8f13b9469b0c", "ewogICJ0aW1lc3RhbXAiIDogMTU5MTU3NjA3MDMwMCwKICAicHJvZmlsZUlkIiA6ICJkYTQ5OGFjNGU5Mzc0ZTVjYjYxMjdiMzgwODU1Nzk4MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOaXRyb2hvbGljXzIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjJiY2EwODU3NTAwNDM1MDNmNWRmOWY3ZGVmODI0YTJlM2FjZmMyNzg0MmJjZDA5ZDJiNjY5NTg4MWU4MzJmNSIKICAgIH0KICB9Cn0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aLonely Spider" ,"7c63f3cf-a963-311a-aeca-3a075b417806", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§d§lLost Adventurer" ,"f69ba621-a8b6-31a7-8de1-dc7ade140e1d", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFlMDMyOWY0MjE5MmVlN2MxYTBjNzA0ZjgyZGJiYmU3YzAwZmJmYTNmMDIwYzEwNjdhMjA4NjMwYjk5MWI5ODgifX19"));

			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§d§lShadow Assassin" ,"ef18719c-db6a-3ffb-97ca-4ed764ce9464", "ewogICJ0aW1lc3RhbXAiIDogMTU5MjI2ODE3MDkxMSwKICAicHJvZmlsZUlkIiA6ICJkYTQ5OGFjNGU5Mzc0ZTVjYjYxMjdiMzgwODU1Nzk4MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOaXRyb2hvbGljXzIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM5OWUwMGY0MDQ0MTFlNDY1ZDc0Mzg4ZGYxMzJkNTFmZTg2OGVjZjg2ZjFjMDczZmFmZmExZDkxNzJlYzBmMyIKICAgIH0KICB9Cn0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSkeleton Grunt" ,"dfed3415-919e-3358-b563-0abd0513f74c", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzA0NzE2NzdiMzdhZTg0MmMyYmQyMzJlMTZlZWI4NGQ1YTQ5MzIzMWVlY2VjMDcyZGEzOGJlMzEyN2RkNWM4In19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSkeleton Master" ,"ce22e0d7-c78e-3c8d-907a-2368c927808c", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlOTVlMWI3ZGM4MmJhNzg0NWE2OGZjNmEzMTJmNGNkOTBlZTJmNmNjZTI2YTY4Yzg4YjA0YjEwNzJkODc5In19fQ=="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSkeleton Soldier" ,"cab75065-c896-338e-a399-c4a6da16d678", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjE5Njg4ZTBjMmYwNWFlYjk3OWQ2YTFiOGM5MTE5NTdiN2QzNjU3ZTE0YjU3YWY5M2M1ZWY2ZjZhNTk1NjlkZCJ9fX0="));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSkeletor" ,"49fcfb3e-da7e-3fda-b4f9-37df5ac8fbd3", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODlkMDc0YWQ5Yjk5NzE4NzllYjMyNWJkZGZmMzY3NWY3MjI0ODU2YmQ2ZDU2OWZjOGQ0ODNjMTMzZDczMDA1ZCJ9fX0K"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSniper" ,"848130dc-9c46-3818-a099-b429cb2f1d75", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjE4YzA3MWYwODBkYmE1MGE2MmE2MjYzZmY3MjRlZGMxNTdjZTRmYjQ4ODNjY2VmZjI0OTFkNWJiZGU4MzBjMSJ9fX0K"));
			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aSuper Archer" ,"8ebf155b-7b8f-386f-91f1-2e425db4230f", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNhZTZkYjBiNTlhNjQzMDUwNzZkOTY2ZDhlN2I5YTk3YmU0NmRhZTNhODA3NzE0ZmE4NmQzNzg0OGY2In19fQ=="));


			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§a§4§lUndead" ,"0ac53e90-4e60-388c-a754-092dd4578592", "eyJ0aW1lc3RhbXAiOjE1ODYwNDAyMDM1NzMsInByb2ZpbGVJZCI6ImRhNDk4YWM0ZTkzNzRlNWNiNjEyN2IzODA4NTU3OTgzIiwicHJvZmlsZU5hbWUiOiJOaXRyb2hvbGljXzIiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y0NjI0YTlhOGM2OWNhMjA0NTA0YWJiMDQzZDQ3NDU2Y2Q5YjA5NzQ5YTM2MzU3NDYyMzAzZjI3NmEyMjlkNCJ9fX0="));




			put("ID", Utils.createSkull(EnumChatFormatting.AQUA + "§aZombie Knight" ,"34af9e21-dff4-3b94-9fb5-07816e41af75", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjVkMmYzMWJhMTYyZmU2MjcyZTgzMWFlZDE3ZjUzMjEzZGI2ZmExYzRjYmU0ZmM4MjdmMzk2M2NjOThiOSJ9fX0="));

			//
		}};

	public static LinkedHashMap<String, ItemStack> getBestiaryMobs() {
		return bestiaryMobs;
	}
}
