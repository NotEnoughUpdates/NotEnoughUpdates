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

	private static final LinkedHashMap<ItemStack, List<String>> bestiaryLocations = new LinkedHashMap<ItemStack, List<String>>() {
		{
			put(
				Utils.createSkull(
					EnumChatFormatting.AQUA + "Private Island",
					"bdee7687-9c85-4e7a-b789-b55e90d21d68",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0="
				),
				Utils.createList(
					"family_cave_spider",
					"family_enderman_private",
					"family_skeleton",
					"family_slime",
					"family_spider",
					"family_witch",
					"family_zombie"
				)
			);
			put(
				Utils.createSkull(
					EnumChatFormatting.AQUA + "Hub",
					"88208736-41cd-4ed8-8ed7-53179140a7fa",
					"eyJ0aW1lc3RhbXAiOjE1NTkyMTU0MTY5MDksInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q3Y2M2Njg3NDIzZDA1NzBkNTU2YWM1M2UwNjc2Y2I1NjNiYmRkOTcxN2NkODI2OWJkZWJlZDZmNmQ0ZTdiZjgifX19"
				),
				Utils.createList("family_unburried_zombie", "family_old_wolf", "family_ruin_wolf", "family_zombie_villager")
			);
			put(
				Utils.createSkull(
					EnumChatFormatting.AQUA + "Spiders Den",
					"acbeaf98-2081-40c5-b5a3-221a2957d532",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc1NDMxOGEzMzc2ZjQ3MGU0ODFkZmNkNmM4M2E1OWFhNjkwYWQ0YjRkZDc1NzdmZGFkMWMyZWYwOGQ4YWVlNiJ9fX0"
				),
				Utils.createList(
					"family_arachne",
					"family_arachne_brood",
					"family_arachne_keeper",
					"family_brood_mother_spider",
					"family_dasher_spider",
					"family_respawning_skeleton",
					"family_random_slime",
					"family_spider_jockey",
					"family_splitter_spider",
					"family_voracious_spider",
					"family_weaver_spider"
				)
			);
			put(
				Utils.createSkull(
					EnumChatFormatting.AQUA + "The End",
					"e39ea8b1-a267-48a9-907a-1b97b85342bc",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg0MGI4N2Q1MjI3MWQyYTc1NWRlZGM4Mjg3N2UwZWQzZGY2N2RjYzQyZWE0NzllYzE0NjE3NmIwMjc3OWE1In19fQ"
				),
				Utils.createList(
					"family_dragon",
					"family_enderman",
					"family_endermite",
					"family_corrupted_protector",
					"family_obsidian_wither",
					"family_voidling_extremist",
					"family_voidling_fanatic",
					"family_watcher",
					"family_zealot_enderman"
				)
			);
			put(
				Utils.createSkull(
					EnumChatFormatting.AQUA + "Crimson Isles",
					"d8489bfe-dcd7-41f0-bfbd-fb482bf61ecb",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM2ODdlMjVjNjMyYmNlOGFhNjFlMGQ2NGMyNGU2OTRjM2VlYTYyOWVhOTQ0ZjRjZjMwZGNmYjRmYmNlMDcxIn19fQ"
				),
				Utils.createList(
					"family_ashfang",
					"family_barbarian_duke_x",
					"family_bladesoul",
					"family_blaze",
					"family_flaming_spider",
					"family_ghast",
					"family_mage_outlaw",
					"family_magma_cube",
					"family_magma_boss",
					"family_matcho",
					"family_charging_mushroom_cow",
					"family_pigman",
					"family_wither_skeleton",
					"family_wither_spectre"
				)
			);
			put(
				Utils.createSkull(
					EnumChatFormatting.AQUA + "Deep Caverns",
					"896b5137-a2dd-4de2-8c63-d5a5649bfc70",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5YTFmMTE0MTUxYjQ1MjEzNzNmMzRiYzE0YzI5NjNhNTAxMWNkYzI1YTY1NTRjNDhjNzA4Y2Q5NmViZmMifX19"
				),
				Utils.createList(
					"family_automaton",
					"family_butterfly",
					"family_emerald_slime",
					"family_caverns_ghost",
					"family_goblin",
					"family_team_treasurite",
					"family_ice_walker",
					"family_lapis_zombie",
					"family_diamond_skeleton",
					"family_diamond_zombie",
					"family_redstone_pigman",
					"family_sludge",
					"family_invisible_creeper",
					"family_thyst",
					"family_treasure_hoarder",
					"family_worms",
					"family_yog"
				)
			);
			put(
				Utils.createSkull(
					EnumChatFormatting.AQUA + "The Park",
					"6473b2ff-0575-4aec-811f-5f0dca2131b6",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTIyMWY4MTNkYWNlZTBmZWY4YzU5Zjc2ODk0ZGJiMjY0MTU0NzhkOWRkZmM0NGMyZTcwOGE2ZDNiNzU0OWIifX19"
				),
				Utils.createList("family_howling_spirit", "family_pack_spirit", "family_soul_of_the_alpha")
			);
			put(
				Utils.createItemStack(Item.getItemFromBlock(Blocks.lit_pumpkin), EnumChatFormatting.AQUA + "Spooky"),
				Utils.createList(
					"family_batty_witch",
					"family_headless_horseman",
					"family_phantom_spirit",
					"family_scary_jerry",
					"family_trick_or_treater",
					"family_wither_gourd",
					"family_wraith"
				)
			);
			put(
				Utils.createSkull(
					EnumChatFormatting.AQUA + "Catacombs",
					"00b3837d-9275-304c-8bf9-656659087e6b",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY0ZTFjM2UzMTVjOGQ4ZmZmYzM3OTg1YjY2ODFjNWJkMTZhNmY5N2ZmZDA3MTk5ZThhMDVlZmJlZjEwMzc5MyJ9fX0"
				),
				Utils.createList(
					"family_diamond_guy",
					"family_cellar_spider",
					"family_crypt_dreadlord",
					"family_crypt_lurker",
					"family_crypt_souleater",
					"family_king_midas",
					"family_lonely_spider",
					"family_lost_adventurer",
					"family_scared_skeleton",
					"family_shadow_assassin",
					"family_skeleton_grunt",
					"family_skeleton_master",
					"family_skeleton_soldier",
					"family_skeletor",
					"family_sniper_skeleton",
					"family_super_archer",
					"family_super_tank_zombie",
					"family_crypt_tank_zombie",
					"family_watcher_summon_undead",
					"family_dungeon_respawning_skeleton",
					"family_crypt_witherskeleton",
					"family_zombie_commander",
					"family_zombie_grunt",
					"family_zombie_knight",
					"family_zombie_soldier"
				)
			);
		}
	};
	private static final LinkedHashMap<String, ItemStack> bestiaryMobs = new LinkedHashMap<String, ItemStack>() {
		{
			// Private Island
			put(
				"family_cave_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aCave Spider",
					"a8aee72d-0d1d-3db7-8cf8-be1ce6ec2dc4",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE2NDVkZmQ3N2QwOTkyMzEwN2IzNDk2ZTk0ZWViNWMzMDMyOWY5N2VmYzk2ZWQ3NmUyMjZlOTgyMjQifX19"
				)
			);
			put(
				"family_enderman_private",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aEnderman",
					"2005daad-730b-363c-abae-e6f3830816fb",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0="
				)
			);
			put(
				"family_skeleton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSkeleton",
					"53924f1a-87e6-4709-8e53-f1c7d13dc239",
					"ewogICJ0aW1lc3RhbXAiIDogMTY1NTYzMTgzNjcwNCwKICAicHJvZmlsZUlkIiA6ICI1MzkyNGYxYTg3ZTY0NzA5OGU1M2YxYzdkMTNkYzIzOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaHJvd3BvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg4ZWI2OGE0ZDM4ZTlmNDQ2YjhlOTkyNzVmMTYwMzAyZjM2NmVmMTAyMTZhYmY5NDg0ODdlNTgyNTEyYmQwZjMiCiAgICB9CiAgfQp9="
				)
			);
			put(
				"family_slime",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSlime",
					"3b70a2f3-319c-38d5-b7d1-5b2425770184",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="
				)
			);
			put(
				"family_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSpider",
					"7c63f3cf-a963-311a-aeca-3a075b417806",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="
				)
			);
			put(
				"family_witch",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aWitch",
					"cf4f97d7-2e1f-3678-9ca3-4a7b9666cc28",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNlNjYwNDE1N2ZjNGFiNTU5MWU0YmNmNTA3YTc0OTkxOGVlOWM0MWUzNTdkNDczNzZlMGVlNzM0MjA3NGM5MCJ9fX0="
				)
			);
			put(
				"family_zombie",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aZombie",
					"9673d491-d589-44cb-b63e-5f8b3148b3df",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0Njc1NjU2MzM1MiwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NjZkMWJkZjQ1NThiMGQxNjk5MGIyNDFlODE4NWNiZjU4YzNlMDNjMjRkOTA0NTQ0ZThjYzY1YjFjMzhkMSIKICAgIH0KICB9Cn0=\", \"n8vct7fj3NdiODy/h6PJhjxSR2U7d8gQxHjdVq553HqG60SSczt1Tl9XhvQbZP14ZrJGWxCziauuYb/HjZza0ugNAhfwh9z8hqR1PjBJGdvp+wpxoWpP2wd5RT9i5/GYEqpiAIAt9vHY1YeyejFqKZhXaGgp7gZZNjOQWQqB0SZzTUTAPzRW9FiY8x2re7H7Y1POThRXOvvkeQ6qWdPV6Hk5hTumV0rfEGm971jQRIbfdzBZDJRcro+8y+dlje/NpF5qf0JLy78Xr4hc2cwbT9+wqOeoUTcM/r9mwL15OKgFLjB44jszauKRHNfoqb6B3+1fNQEJrJK/7hIyvswpde7C5uOxkE7oMFib6X68VVEhb6PGC1+HWNaMaGjI0wWEkCahp48ihN9+sBEBFXOxIAhXG/pvJcbEi742/cBS1CTtOI8qui6JSL9MKX5jmyhtjjibOYRZbacosqayCnwsJAdMmwS7zIxc8jGhpfKECiSni/baS4zRla0bns4hfEM0l7ASv0Dh99WQD0We2ZMqltmix8lMEaAILXJVBln3CFrjYvncfku2hrSaqu1lNAtfMYYSITEUCBJ7McVAVmUHOrER0XGoVs9L237H0BQNSLqlGGEQ7OM8HV6G0YfXGlbNgNBx6O9k8ZijuK60JeEoJapcmbNj/LVHEU+dsgcFAbo="
				)
			);

			// Hub
			put("family_unburried_zombie", Utils.createItemStack(Items.golden_sword, EnumChatFormatting.AQUA + "§aCrypt Ghoul"));
			put(
				"family_old_wolf",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aOld Wolf",
					"26e6f2d9-8a27-3a77-965c-5bd2b5d2dc93",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM1OTUzN2MxNTUzNGY2MWMxY2Q4ODZiYzExODc3NGVkMjIyODBlN2NkYWI2NjEzODcwMTYwYWFkNGNhMzkifX19"
				)
			);
			put(
				"family_ruin_wolf",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aWolf",
					"7e9af289-f295-3f8c-bd54-58b7667d5759",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjlkMWQzMTEzZWM0M2FjMjk2MWRkNTlmMjgxNzVmYjQ3MTg4NzNjNmM0NDhkZmNhODcyMjMxN2Q2NyJ9fX0="
				)
			);
			put(
				"family_zombie_villager",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aZombie Villager",
					"3acb9940-fc42-328e-91e8-c9a9a57e8698",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMDhhODc3NmMxNzY0YzNmZTZhNmRkZDQxMmRmY2I4N2Y0MTMzMWRhZDQ3OWFjOTZjMjFkZjRiZjNhYzg5YyJ9fX0="
				)
			);

			// Spiders Den
			put(
				"family_arachne",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aArachne",
					"7c63f3cf-a963-311a-aeca-3a075b417806",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="
				)
			);
			put(
				"family_arachne_brood",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aArachne's Brood",
					"7c63f3cf-a963-311a-aeca-3a075b417806",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="
				)
			);
			put(
				"family_arachne_keeper",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aArachne's Keeper",
					"7c63f3cf-a963-311a-aeca-3a075b417806",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="
				)
			);
			put(
				"family_brood_mother_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aBrood Mother",
					"d7390e70-1e99-3c24-9b1c-bb098e0bbef1",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2YwNjIyYjM5OThkNDJiMzRkNWJjNzYwYmIyYzgzZmRiYzZlNjhmYWIwNWI3ZWExN2IzNTA5N2VkODExOTBkNiJ9fX0="
				)
			);
			put(
				"family_dasher_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aDasher Spider",
					"7c63f3cf-a963-311a-aeca-3a075b417806",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="
				)
			);
			put(
				"family_respawning_skeleton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aGravel Skeleton",
					"53924f1a-87e6-4709-8e53-f1c7d13dc239",
					"ewogICJ0aW1lc3RhbXAiIDogMTY1NTYzMTgzNjcwNCwKICAicHJvZmlsZUlkIiA6ICI1MzkyNGYxYTg3ZTY0NzA5OGU1M2YxYzdkMTNkYzIzOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaHJvd3BvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg4ZWI2OGE0ZDM4ZTlmNDQ2YjhlOTkyNzVmMTYwMzAyZjM2NmVmMTAyMTZhYmY5NDg0ODdlNTgyNTEyYmQwZjMiCiAgICB9CiAgfQp9="
				)
			);
			put(
				"family_random_slime",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aRain Slime",
					"3b70a2f3-319c-38d5-b7d1-5b2425770184",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="
				)
			);
			put(
				"family_spider_jockey",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSpider Jockey",
					"4eb8745c-80d2-356b-b4fa-f3ffa74082e7",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzA5MzkzNzNjYWZlNGIxZjUzOTdhYWZkMDlmM2JiMTY2M2U3YjYyOWE0MWE3NWZiZGMxODYwYjZiZjhiNDc1ZiJ9fX0="
				)
			);
			put(
				"family_splitter_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSplitter Spider",
					"50010472-fa22-3519-b941-2d6d22f47bf1",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmFjZjY5ZmM3YWY1NDk3YTE3NDE4OTFkMWU1YmYzMmI5NmFlMGQ2YzBiYmQzYzE0NzU4ZWE0NGEwM2M1NzI4MyJ9fX0="
				)
			);
			put(
				"family_voracious_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aVoracious Spider",
					"3e5474d4-4365-3ea7-b4bc-b4edc54da341",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODMwMDk4NmVkMGEwNGVhNzk5MDRmNmFlNTNmNDllZDNhMGZmNWIxZGY2MmJiYTYyMmVjYmQzNzc3ZjE1NmRmOCJ9fX0="
				)
			);
			put(
				"family_weaver_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aWeaver Spider",
					"97414c0c-623b-3df3-b1f6-bbcaddafc7fc",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTIxNDM4ZjY0NmRjMDQ1MTU5NjdlODE5NWNjYzNkMzFlMjNiMDJmOWFhMGFjOTE0ZWRjMjgyMmY5ODM5NGI4NiJ9fX0="
				)
			);

			// The End
			put("family_dragon", Utils.createItemStack(Item.getItemFromBlock(Blocks.dragon_egg), EnumChatFormatting.AQUA + "§aDragon"));
			put(
				"family_enderman",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aEnderman",
					"2005daad-730b-363c-abae-e6f3830816fb",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0="
				)
			);
			put(
				"family_endermite",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aEndermite",
					"b3224e56-73d2-32f9-9081-a23b7512035b",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0="
				)
			);
			put(
				"family_corrupted_protector",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aEndstone Protector",
					"a46a9adf-60a3-38f2-a3dd-335d85f1cc10",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJiY2FjZWViNDE2MmY0MDBkNDQ3NDMzMTU5MzJhYzgyMGQzMTE5YWM4OTg2YTAxNjFhNzI2MTYxY2NjOTNmYyJ9fX0="
				)
			);
			put(
				"family_obsidian_wither",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aObsidian Defender",
					"d0e05de7-6067-454d-beae-c6d19d886191",
					"ewogICJ0aW1lc3RhbXAiIDogMTY1NTYzMjg1MTE1NCwKICAicHJvZmlsZUlkIiA6ICJkMGUwNWRlNzYwNjc0NTRkYmVhZWM2ZDE5ZDg4NjE5MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNb3VsYmVycnkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNlMTU1MjI0ZWE0YmM0OWE4ZTkxOTA3MzdjYjA0MTdkOGE3YzM4YTAzN2Q4ZDAzODJkZGU0ODI5YzEwMzU5MCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
				)
			);
			put(
				"family_voidling_extremist",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§dVoidling Extremist",
					"159dcb01-74e3-382c-87d6-3afa022fb379",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0="
				)
			);
			put(
				"family_voidling_fanatic",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aVoidling Fanatic",
					"e86aab24-6245-3967-bf3d-07e31999b602",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTUzYjdiY2Q1NmYwYjk1Zjg3ZGQ3OWVkMTc2MzZiZWI5ZDgzNDY3NDQwMTQyMjhlYTJmNmIxMTBiMTQ4YzEifX19"
				)
			);
			put(
				"family_watcher",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aWatcher",
					"00a702b9-7bad-3205-a04b-52478d8c0e7f",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFhOGZjOGRlNjQxN2I0OGQ0OGM4MGI0NDNjZjUzMjZlM2Q5ZGE0ZGJlOWIyNWZjZDQ5NTQ5ZDk2MTY4ZmMwIn19fQ=="
				)
			);
			put(
				"family_zealot_enderman",
				Utils.createItemStack(Item.getItemFromBlock(Blocks.ender_chest), EnumChatFormatting.AQUA + "§aZealot")
			);

			// Crimson Isle
			put(
				"family_ashfang",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aAshfang",
					"1bc8810e-2b57-3a89-8e00-a47a057d6ecc",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2QyYTViNGIxMDliZDc4OGVkYmEwMTcxZDBhYWI4YTU1MzA1YWMyZjU2MTg0ZGY3MGEzMTljZDQ4OGEzNmMzZSJ9fX0="
				)
			);
			put(
				"family_barbarian_duke_x",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aBarbarian Duke X",
					"6ddece1d-8227-35f3-b9ca-476a9f6cd8c5",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0MTI0OTYyNTU3NSwKICAicHJvZmlsZUlkIiA6ICIyNzZlMDQ2YjI0MDM0M2VkOTk2NmU0OTRlN2U2Y2IzNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBRFJBTlM3MTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmVlOWZjN2MxODFlMmY2MzBmNmIxYWY4NWQ0OTUxMzU5Y2FmY2ZhODJmZjVlYTNiYzI4M2UwZTYwODhjNmU1NCIKICAgIH0KICB9Cn0"
				)
			);
			put(
				"family_bladesoul",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aBladesoul",
					"9a1699a4-9b61-37a5-be7a-ca23a1f092a1",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0NDA4Mjg1NzcxMCwKICAicHJvZmlsZUlkIiA6ICIwNTVhOTk2NTk2M2E0YjRmOGMwMjRmMTJmNDFkMmNmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVWb3hlbGxlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdmNWYzMzg0Mzg0ZDdmMDNiZjk3YTczMDk5YjBiYWZiNzJjNTM4ZmMwNDE1YWM4NjEzYjY2NGY4NzU3OWEzNzkiCiAgICB9CiAgfQp9"
				)
			);
			put(
				"family_blaze",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aBlaze",
					"118fe834-28aa-3b0d-afe6-f0c52d01afe8",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ=="
				)
			);
			put(
				"family_flaming_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aFlaming Spider",
					"d27e14a2-f35e-3c7b-8062-089fa201a533",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0NDQ5OTUzOTQ2NywKICAicHJvZmlsZUlkIiA6ICJhYTZhNzUwNWVkYmU0NjNiYjk1NWYyMWY0MjNiYTM1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJub3RhbmR5d2FyaG9sIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVhNjVlZjIzZWEzNTA0NzE1MGQzMzg4MDQ3M2E0N2ZlNjM1ZjBjMGUzYzgyM2JkNzZkYzg0OWNiMDI0NDE2NTUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="
				)
			);
			put(
				"family_ghast",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aGhast",
					"69725d7d-1933-3dea-87bd-a3052482ab2c",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGU4YTM4ZTlhZmJkM2RhMTBkMTliNTc3YzU1YzdiZmQ2YjRmMmU0MDdlNDRkNDAxN2IyM2JlOTE2N2FiZmYwMiJ9fX0="
				)
			);
			put(
				"family_mage_outlaw",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aMage Outlaw",
					"1d16c26c-d937-336f-821a-371968d050c2",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0NDA4Mjg3Mzk2NywKICAicHJvZmlsZUlkIiA6ICJiNzQ3OWJhZTI5YzQ0YjIzYmE1NjI4MzM3OGYwZTNjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTeWxlZXgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWJlYzk5YjNhMDUwZmQyNzc1Mjg0MDc2NzYzNjZlMjBiOTIwMDZhZDg4ZDE0NzI3YTRkOTllYjhjYjI3M2I2MiIKICAgIH0KICB9Cn0"
				)
			);
			put(
				"family_magma_cube",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aMagma Cube",
					"35f02923-7bec-3869-9ef5-b42a4794cac8",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg5NTdkNTAyM2M5MzdjNGM0MWFhMjQxMmQ0MzQxMGJkYTIzY2Y3OWE5ZjZhYjM2Yjc2ZmVmMmQ3YzQyOSJ9fX0="
				)
			);
			put(
				"family_magma_boss",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§4§lMagma Boss",
					"35f02923-7bec-3869-9ef5-b42a4794cac8",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg5NTdkNTAyM2M5MzdjNGM0MWFhMjQxMmQ0MzQxMGJkYTIzY2Y3OWE5ZjZhYjM2Yjc2ZmVmMmQ3YzQyOSJ9fX0="
				)
			);
			put(
				"family_matcho",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aMatcho",
					"61db73be-677f-554a-9450-e306a7ff0449",
					"e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWYyZGFhYmI3OGExZjdhYTEyZDE0NWQ4OGMwY2E0NmI5ZTg1NmY1NTM0ZTkyODZlNTU1ZmFmMGMyOTFmNGZkNSJ9fX0="
				)
			);
			put(
				"family_charging_mushroom_cow",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aMushroom Bull",
					"2b9eb675-2097-4b51-8fec-c1a51562f19c",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE2M2JjNDE2YjhlNjA1OGY5MmIyMzFlOWE1MjRiN2ZlMTE4ZWI2ZTdlZWFiNGFkMTZkMWI1MmEzZWMwNGZjZCJ9fX0="
				)
			);
			put(
				"family_pigman",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aPigman",
					"3fc29372-e78e-3ad6-b0b0-05ca0a84babd",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlOWM2ZTk4NTgyZmZkOGZmOGZlYjMzMjJjZDE4NDljNDNmYjE2YjE1OGFiYjExY2E3YjQyZWRhNzc0M2ViIn19fQ=="
				)
			);
			put(
				"family_wither_skeleton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aWither Skeleton",
					"2141b934-c877-3db1-bc6c-7c9a347ffa95",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk1M2I2YzY4NDQ4ZTdlNmI2YmY4ZmIyNzNkNzIwM2FjZDhlMWJlMTllODE0ODFlYWQ1MWY0NWRlNTlhOCJ9fX0="
				)
			);
			put(
				"family_wither_spectre",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aWither Spectre",
					"9a1699a4-9b61-37a5-be7a-ca23a1f092a1",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0NDA4Mjg1NzcxMCwKICAicHJvZmlsZUlkIiA6ICIwNTVhOTk2NTk2M2E0YjRmOGMwMjRmMTJmNDFkMmNmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVWb3hlbGxlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdmNWYzMzg0Mzg0ZDdmMDNiZjk3YTczMDk5YjBiYWZiNzJjNTM4ZmMwNDE1YWM4NjEzYjY2NGY4NzU3OWEzNzkiCiAgICB9CiAgfQp9"
				)
			);

			// Deep Caverns
			put(
				"family_automaton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aAutomaton",
					"a46a9adf-60a3-38f2-a3dd-335d85f1cc10",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJiY2FjZWViNDE2MmY0MDBkNDQ3NDMzMTU5MzJhYzgyMGQzMTE5YWM4OTg2YTAxNjFhNzI2MTYxY2NjOTNmYyJ9fX0="
				)
			);
			put(
				"family_butterfly",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§eButterfly",
					"9dd11ec6-cfea-34df-9336-416c946567bc",
					"ewogICJ0aW1lc3RhbXAiIDogMTYyNTUxMjE4ODY3NCwKICAicHJvZmlsZUlkIiA6ICI3MzgyZGRmYmU0ODU0NTVjODI1ZjkwMGY4OGZkMzJmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJJb3lhbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85ZmQ4MDZkZWZkZmRmNTliMWYyNjA5YzhlZTM2NDY2NmRlNjYxMjdhNjIzNDE1YjU0MzBjOTM1OGM2MDFlZjdjIgogICAgfQogIH0KfQ=="
				)
			);
			put(
				"family_emerald_slime",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aEmerald Slime",
					"cb762e0d-a1e6-3888-8c05-eddabbbe49a2",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTc3NGU4NmFhNGNmZjc5MjM5NWI3N2FkZDU3YjAwYmIxYTEwMmY4ZjBmMDk4MGY0ZDU1YjNkN2FmZjFlNmRhOSJ9fX0="
				)
			);
			put(
				"family_caverns_ghost",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aGhost",
					"c5752211-7503-3e77-9890-d1cf6ba1d0e7",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTgxOTc3OTE4YTExODBlMGRlYzg3OWU2YmNkMWFhMzk0OTQ5NzdiYjkxM2JlMmFiMDFhZmYxZGIxZmE0In19fQ=="
				)
			);
			put(
				"family_goblin",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aGoblin",
					"7c7d07db-4911-31f1-9a19-1589899cfe25",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZiOTcyZTMyZDc2MWIxOTI2MjZlNWQ2ZDAxZWRjMDk0OTQwOTEwMTAzY2VhNWUyZTJkMWYyMzFhZGI3NTVkNSJ9fX0="
				)
			);
			put(
				"family_team_treasurite",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aGrunt",
					"a64ccd19-2a64-39a4-b2f5-cb6799c12a99",
					"ewogICJ0aW1lc3RhbXAiIDogMTYxODE5NTA2MDUwMCwKICAicHJvZmlsZUlkIiA6ICI0ZTMwZjUwZTdiYWU0M2YzYWZkMmE3NDUyY2ViZTI5YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfdG9tYXRvel8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY1ZTAzYjhkZTExOWY4NTg5YTgwODIyNGNiZWE3MzdmNWRjZjI0MjM1Nzk5YjczNzhhYzViZjA2YWJmNmRkNCIKICAgIH0KICB9Cn0="
				)
			);
			put(
				"family_ice_walker",
				Utils.createItemStack(Item.getItemFromBlock(Blocks.packed_ice), EnumChatFormatting.AQUA + "§aIce Walker")
			);
			put("family_lapis_zombie", Utils.createItemStack(Items.dye, EnumChatFormatting.AQUA + "§aLapis Zombie", 4));
			put(
				"family_diamond_skeleton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aMiner Skeleton",
					"39c843e6-237b-36b2-8a7b-c5ff5d3ebf99",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM2YmJjNDIxNWNlYTFiNmE0ODRlODkzYjExNmU3MzQ1OWVmMzZiZmZjNjIyNzQxZTU3N2U5NDkzYTQxZTZlIn19fQ=="
				)
			);
			put(
				"family_diamond_zombie",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aMiner Zombie",
					"468210c9-f4bd-34c7-aa8d-2c3d0d5e05c1",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI4ZDlmZjU0MTg4YTFhZmVlNjViOTRmM2JmY2NlMzIxYzY0M2EzNDU5MGMxNGIxOTJiMmUzZWMyZjUyNWQzIn19fQ=="
				)
			);
			put("family_redstone_pigman", Utils.createItemStack(Items.redstone, EnumChatFormatting.AQUA + "§aRedstone Pigman"));
			put(
				"family_sludge",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSludge",
					"3b70a2f3-319c-38d5-b7d1-5b2425770184",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk1YWVlYzZiODQyYWRhODY2OWY4NDZkNjViYzQ5NzYyNTk3ODI0YWI5NDRmMjJmNDViZjNiYmI5NDFhYmU2YyJ9fX0="
				)
			);
			put(
				"family_invisible_creeper",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSneaky Creeper",
					"81fb1385-b2fc-4d4f-b5bb-0fe9b1d37d60",
					"ewogICJ0aW1lc3RhbXAiIDogMTYxOTE5MjI5MzI5OCwKICAicHJvZmlsZUlkIiA6ICI0ZjU2ZTg2ODk2OGU0ZWEwYmNjM2M2NzRlNzQ3ODdjOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E2ODIyNGNmOGVkNGMwM2I0NTdiZjQ5YmViNmY1NDQxOTM2NzkyNjhiODQyMWIwMWZmY2U2ZDI3YjI1YWMzMmQiCiAgICB9CiAgfQp9"
				)
			);
			put(
				"family_thyst",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aThyst",
					"b3224e56-73d2-32f9-9081-a23b7512035b",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0="
				)
			);
			put(
				"family_treasure_hoarder",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aTreasure Hoarder",
					"b0f13fc2-07a5-3964-8303-784f802e5f0f",
					"ewogICJ0aW1lc3RhbXAiIDogMTU5MDE1NjYzNDYzOCwKICAicHJvZmlsZUlkIiA6ICI5MWZlMTk2ODdjOTA0NjU2YWExZmMwNTk4NmRkM2ZlNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJoaGphYnJpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iMmIxMmE4MTRjZWQ4YWYwMmNkZGYyOWEzN2U3ZjMwMTFlNDMwZThhMThiMzhiNzA2ZjI3YzZiZDMxNjUwYjY1IgogICAgfQogIH0KfQ=="
				)
			);
			put(
				"family_worms",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aWorm",
					"29f95759-1a6f-3e85-9941-91a7a2275274",
					"ewogICJ0aW1lc3RhbXAiIDogMTYyMDQ0NTc2NDQ1MSwKICAicHJvZmlsZUlkIiA6ICJmNDY0NTcxNDNkMTU0ZmEwOTkxNjBlNGJmNzI3ZGNiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWxhcGFnbzA1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RmMDNhZDk2MDkyZjNmNzg5OTAyNDM2NzA5Y2RmNjlkZTZiNzI3YzEyMWIzYzJkYWVmOWZmYTFjY2FlZDE4NmMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="
				)
			);
			put(
				"family_yog",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aYog",
					"35f02923-7bec-3869-9ef5-b42a4794cac8",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg5NTdkNTAyM2M5MzdjNGM0MWFhMjQxMmQ0MzQxMGJkYTIzY2Y3OWE5ZjZhYjM2Yjc2ZmVmMmQ3YzQyOSJ9fX0="
				)
			);

			// The Park
			put(
				"family_howling_spirit",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§bHowling Spirit",
					"802a167c-cbcd-3a1f-becd-5b1a25a4cf15",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdjOGJlZjZiZWI3N2UyOWFmODYyN2VjZGMzOGQ4NmFhMmZlYTdjY2QxNjNkYzczYzAwZjlmMjU4ZjlhMTQ1NyJ9fX0="
				)
			);
			put(
				"family_pack_spirit",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§bPack Spirit",
					"802a167c-cbcd-3a1f-becd-5b1a25a4cf15",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdjOGJlZjZiZWI3N2UyOWFmODYyN2VjZGMzOGQ4NmFhMmZlYTdjY2QxNjNkYzczYzAwZjlmMjU4ZjlhMTQ1NyJ9fX0="
				)
			);
			put(
				"family_soul_of_the_alpha",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§3Soul of the Alpha",
					"802a167c-cbcd-3a1f-becd-5b1a25a4cf15",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdjOGJlZjZiZWI3N2UyOWFmODYyN2VjZGMzOGQ4NmFhMmZlYTdjY2QxNjNkYzczYzAwZjlmMjU4ZjlhMTQ1NyJ9fX0="
				)
			);

			// Spooky
			put(
				"family_batty_witch",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§8Crazy Witch",
					"cf4f97d7-2e1f-3678-9ca3-4a7b9666cc28",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmNlNjYwNDE1N2ZjNGFiNTU5MWU0YmNmNTA3YTc0OTkxOGVlOWM0MWUzNTdkNDczNzZlMGVlNzM0MjA3NGM5MCJ9fX0="
				)
			);
			put(
				"family_headless_horseman",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§6Headless Horseman",
					"2594a979-1302-3d6e-a1da-c9dbf0959539",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGM2NTcwZjEyNDI5OTJmNmViYTIzZWU1ODI1OThjMzllM2U3NDUzODMyNzNkZWVmOGIzOTc3NTgzZmUzY2Y1In19fQ=="
				)
			);
			put(
				"family_phantom_spirit",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§cPhantom Spirit",
					"805d7035-5f25-37ea-8530-7c0d09156c8e",
					"ewogICJ0aW1lc3RhbXAiIDogMTYwMzcyMjc5NzYzNywKICAicHJvZmlsZUlkIiA6ICJhMmY4MzQ1OTVjODk0YTI3YWRkMzA0OTcxNmNhOTEwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiUHVuY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjUzYjJmN2M1ZTE3N2JkNjdjZWFkMzBkMGVlNTM0MjVjNzY4NGM5NzVjOGMyYTUyNzNhMDljYTQ5YTFmNmNkZCIKICAgIH0KICB9Cn0="
				)
			);
			put(
				"family_scary_jerry",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§6Scary Jerry",
					"127e3dec-4ab7-3798-9410-5fce3f227632",
					"ewogICJ0aW1lc3RhbXAiIDogMTYwMzczMzU4OTcxOSwKICAicHJvZmlsZUlkIiA6ICJhMmY4MzQ1OTVjODk0YTI3YWRkMzA0OTcxNmNhOTEwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiUHVuY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYyMDJkYzI0ZDE1ZjdjZTM2ZTAyZmI0YjNlODE1M2IxNDZhYjljMTcyNGFhYTVkNDg0Yzc0MWRhMGVlYjZmZCIKICAgIH0KICB9Cn0="
				)
			);
			put(
				"family_trick_or_treater",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§eTrick or Treater",
					"79dd9434-1fde-3aac-87a7-bb09d91eba77",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDdjYmUwNjFiNDQ1Yjg4Y2IyZGY1OWFjY2M4ZDJjMWMxMjExOGZlMGIyMTI3ZTZlNzU4MTM1NTBhZGFjNjdjZiJ9fX0="
				)
			);
			put(
				"family_wither_gourd",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§6Wither Gourd",
					"3263c14e-c555-365e-a244-0ee97a8b2056",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjhmMmZmYzZmYjRlOTk1OWI5YTdhMzE3ZjUxYTY3NzVhMTU5ZGRjMjI0MWRiZDZjNzc0ZDNhYzA4YjYifX19"
				)
			);
			put(
				"family_wraith",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§8Wraith",
					"bca22b11-8e4c-386a-8824-7b2bd6364cde",
					"ewogICJ0aW1lc3RhbXAiIDogMTYwMzczMzcxNjI0MiwKICAicHJvZmlsZUlkIiA6ICJhMmY4MzQ1OTVjODk0YTI3YWRkMzA0OTcxNmNhOTEwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiUHVuY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWVhYmMzMDc1Y2Y0MWYzOGU2ZGYxMjM2Yjk1Y2FhZmNiYTFiZWUyMmM0OWQ4MDRiOTQyNzQ4OGMyZjZlMGVmYyIKICAgIH0KICB9Cn0="
				)
			);

			// Dungeons
			put(
				"family_diamond_guy",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§d§lAngry Archeologist",
					"db784d7a-fae1-3d60-9a5a-42a1814037f8",
					"eyJ0aW1lc3RhbXAiOjE1NzU0NzAzOTQwMzEsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M0OGM3ODM0NThlNGNmODUxOGU4YWI1ODYzZmJjNGNiOTQ4ZjkwNTY4ZWViOWE2MGQxNmM0ZmRlMmI5NmMwMzMifX19"
				)
			);
			put(
				"family_cellar_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aCellar Spider",
					"a8aee72d-0d1d-3db7-8cf8-be1ce6ec2dc4",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDE2NDVkZmQ3N2QwOTkyMzEwN2IzNDk2ZTk0ZWViNWMzMDMyOWY5N2VmYzk2ZWQ3NmUyMjZlOTgyMjQifX19"
				)
			);
			put(
				"family_crypt_dreadlord",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aCrypt Dreadlord",
					"68b4c885-7447-3382-b86b-b661b464d76e",
					"eyJ0aW1lc3RhbXAiOjE1NjI0Mjc0MTA5MTQsInByb2ZpbGVJZCI6ImIwZDczMmZlMDBmNzQwN2U5ZTdmNzQ2MzAxY2Q5OGNhIiwicHJvZmlsZU5hbWUiOiJPUHBscyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZmMzQ5MjcwYTNiODUxODk2Y2RhZDg0MmY1ZWVjNmUxNDBiZDkxMTliNzVjMDc0OTU1YzNiZTc4NjVlMjdjNyJ9fX0="
				)
			);
			put(
				"family_crypt_lurker",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aCrypt Lurker",
					"9673d491-d589-44cb-b63e-5f8b3148b3df",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0Njc1NjU2MzM1MiwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NjZkMWJkZjQ1NThiMGQxNjk5MGIyNDFlODE4NWNiZjU4YzNlMDNjMjRkOTA0NTQ0ZThjYzY1YjFjMzhkMSIKICAgIH0KICB9Cn0=\", \"n8vct7fj3NdiODy/h6PJhjxSR2U7d8gQxHjdVq553HqG60SSczt1Tl9XhvQbZP14ZrJGWxCziauuYb/HjZza0ugNAhfwh9z8hqR1PjBJGdvp+wpxoWpP2wd5RT9i5/GYEqpiAIAt9vHY1YeyejFqKZhXaGgp7gZZNjOQWQqB0SZzTUTAPzRW9FiY8x2re7H7Y1POThRXOvvkeQ6qWdPV6Hk5hTumV0rfEGm971jQRIbfdzBZDJRcro+8y+dlje/NpF5qf0JLy78Xr4hc2cwbT9+wqOeoUTcM/r9mwL15OKgFLjB44jszauKRHNfoqb6B3+1fNQEJrJK/7hIyvswpde7C5uOxkE7oMFib6X68VVEhb6PGC1+HWNaMaGjI0wWEkCahp48ihN9+sBEBFXOxIAhXG/pvJcbEi742/cBS1CTtOI8qui6JSL9MKX5jmyhtjjibOYRZbacosqayCnwsJAdMmwS7zIxc8jGhpfKECiSni/baS4zRla0bns4hfEM0l7ASv0Dh99WQD0We2ZMqltmix8lMEaAILXJVBln3CFrjYvncfku2hrSaqu1lNAtfMYYSITEUCBJ7McVAVmUHOrER0XGoVs9L237H0BQNSLqlGGEQ7OM8HV6G0YfXGlbNgNBx6O9k8ZijuK60JeEoJapcmbNj/LVHEU+dsgcFAbo="
				)
			);
			put(
				"family_crypt_souleater",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aCrypt Souleater",
					"68b4c885-7447-3382-b86b-b661b464d76e",
					"eyJ0aW1lc3RhbXAiOjE1NjI0Mjc0MTA5MTQsInByb2ZpbGVJZCI6ImIwZDczMmZlMDBmNzQwN2U5ZTdmNzQ2MzAxY2Q5OGNhIiwicHJvZmlsZU5hbWUiOiJPUHBscyIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjZmMzQ5MjcwYTNiODUxODk2Y2RhZDg0MmY1ZWVjNmUxNDBiZDkxMTliNzVjMDc0OTU1YzNiZTc4NjVlMjdjNyJ9fX0="
				)
			);
			put(
				"family_king_midas",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§d§lKing Midas",
					"1a85d923-f8dd-35b8-899a-8f13b9469b0c",
					"ewogICJ0aW1lc3RhbXAiIDogMTU5MTU3NjA3MDMwMCwKICAicHJvZmlsZUlkIiA6ICJkYTQ5OGFjNGU5Mzc0ZTVjYjYxMjdiMzgwODU1Nzk4MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOaXRyb2hvbGljXzIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjJiY2EwODU3NTAwNDM1MDNmNWRmOWY3ZGVmODI0YTJlM2FjZmMyNzg0MmJjZDA5ZDJiNjY5NTg4MWU4MzJmNSIKICAgIH0KICB9Cn0="
				)
			);
			put(
				"family_lonely_spider",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aLonely Spider",
					"7c63f3cf-a963-311a-aeca-3a075b417806",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Q1NDE1NDFkYWFmZjUwODk2Y2QyNThiZGJkZDRjZjgwYzNiYTgxNjczNTcyNjA3OGJmZTM5MzkyN2U1N2YxIn19fQ=="
				)
			);
			put(
				"family_lost_adventurer",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§d§lLost Adventurer",
					"f69ba621-a8b6-31a7-8de1-dc7ade140e1d",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFlMDMyOWY0MjE5MmVlN2MxYTBjNzA0ZjgyZGJiYmU3YzAwZmJmYTNmMDIwYzEwNjdhMjA4NjMwYjk5MWI5ODgifX19"
				)
			);
			put(
				"family_scared_skeleton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aScared Skeleton",
					"53924f1a-87e6-4709-8e53-f1c7d13dc239",
					"ewogICJ0aW1lc3RhbXAiIDogMTY1NTYzMTgzNjcwNCwKICAicHJvZmlsZUlkIiA6ICI1MzkyNGYxYTg3ZTY0NzA5OGU1M2YxYzdkMTNkYzIzOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaHJvd3BvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg4ZWI2OGE0ZDM4ZTlmNDQ2YjhlOTkyNzVmMTYwMzAyZjM2NmVmMTAyMTZhYmY5NDg0ODdlNTgyNTEyYmQwZjMiCiAgICB9CiAgfQp9="
				)
			);
			put(
				"family_shadow_assassin",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§d§lShadow Assassin",
					"ef18719c-db6a-3ffb-97ca-4ed764ce9464",
					"ewogICJ0aW1lc3RhbXAiIDogMTU5MjI2ODE3MDkxMSwKICAicHJvZmlsZUlkIiA6ICJkYTQ5OGFjNGU5Mzc0ZTVjYjYxMjdiMzgwODU1Nzk4MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOaXRyb2hvbGljXzIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM5OWUwMGY0MDQ0MTFlNDY1ZDc0Mzg4ZGYxMzJkNTFmZTg2OGVjZjg2ZjFjMDczZmFmZmExZDkxNzJlYzBmMyIKICAgIH0KICB9Cn0="
				)
			);
			put(
				"family_skeleton_grunt",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSkeleton Grunt",
					"dfed3415-919e-3358-b563-0abd0513f74c",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzA0NzE2NzdiMzdhZTg0MmMyYmQyMzJlMTZlZWI4NGQ1YTQ5MzIzMWVlY2VjMDcyZGEzOGJlMzEyN2RkNWM4In19fQ=="
				)
			);
			put(
				"family_skeleton_master",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSkeleton Master",
					"ce22e0d7-c78e-3c8d-907a-2368c927808c",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzRlOTVlMWI3ZGM4MmJhNzg0NWE2OGZjNmEzMTJmNGNkOTBlZTJmNmNjZTI2YTY4Yzg4YjA0YjEwNzJkODc5In19fQ=="
				)
			);
			put(
				"family_skeleton_soldier",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSkeleton Soldier",
					"cab75065-c896-338e-a399-c4a6da16d678",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjE5Njg4ZTBjMmYwNWFlYjk3OWQ2YTFiOGM5MTE5NTdiN2QzNjU3ZTE0YjU3YWY5M2M1ZWY2ZjZhNTk1NjlkZCJ9fX0="
				)
			);
			put(
				"family_skeletor",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSkeletor",
					"49fcfb3e-da7e-3fda-b4f9-37df5ac8fbd3",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODlkMDc0YWQ5Yjk5NzE4NzllYjMyNWJkZGZmMzY3NWY3MjI0ODU2YmQ2ZDU2OWZjOGQ0ODNjMTMzZDczMDA1ZCJ9fX0K"
				)
			);
			put(
				"family_sniper_skeleton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSniper",
					"848130dc-9c46-3818-a099-b429cb2f1d75",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjE4YzA3MWYwODBkYmE1MGE2MmE2MjYzZmY3MjRlZGMxNTdjZTRmYjQ4ODNjY2VmZjI0OTFkNWJiZGU4MzBjMSJ9fX0K"
				)
			);
			put(
				"family_super_archer",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSuper Archer",
					"8ebf155b-7b8f-386f-91f1-2e425db4230f",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNhZTZkYjBiNTlhNjQzMDUwNzZkOTY2ZDhlN2I5YTk3YmU0NmRhZTNhODA3NzE0ZmE4NmQzNzg0OGY2In19fQ=="
				)
			);
			put(
				"family_super_tank_zombie",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aSuper Tank Zombie",
					"9673d491-d589-44cb-b63e-5f8b3148b3df",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0Njc1NjU2MzM1MiwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NjZkMWJkZjQ1NThiMGQxNjk5MGIyNDFlODE4NWNiZjU4YzNlMDNjMjRkOTA0NTQ0ZThjYzY1YjFjMzhkMSIKICAgIH0KICB9Cn0=\", \"n8vct7fj3NdiODy/h6PJhjxSR2U7d8gQxHjdVq553HqG60SSczt1Tl9XhvQbZP14ZrJGWxCziauuYb/HjZza0ugNAhfwh9z8hqR1PjBJGdvp+wpxoWpP2wd5RT9i5/GYEqpiAIAt9vHY1YeyejFqKZhXaGgp7gZZNjOQWQqB0SZzTUTAPzRW9FiY8x2re7H7Y1POThRXOvvkeQ6qWdPV6Hk5hTumV0rfEGm971jQRIbfdzBZDJRcro+8y+dlje/NpF5qf0JLy78Xr4hc2cwbT9+wqOeoUTcM/r9mwL15OKgFLjB44jszauKRHNfoqb6B3+1fNQEJrJK/7hIyvswpde7C5uOxkE7oMFib6X68VVEhb6PGC1+HWNaMaGjI0wWEkCahp48ihN9+sBEBFXOxIAhXG/pvJcbEi742/cBS1CTtOI8qui6JSL9MKX5jmyhtjjibOYRZbacosqayCnwsJAdMmwS7zIxc8jGhpfKECiSni/baS4zRla0bns4hfEM0l7ASv0Dh99WQD0We2ZMqltmix8lMEaAILXJVBln3CFrjYvncfku2hrSaqu1lNAtfMYYSITEUCBJ7McVAVmUHOrER0XGoVs9L237H0BQNSLqlGGEQ7OM8HV6G0YfXGlbNgNBx6O9k8ZijuK60JeEoJapcmbNj/LVHEU+dsgcFAbo="
				)
			);
			put(
				"family_crypt_tank_zombie",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aTank Zombie",
					"9673d491-d589-44cb-b63e-5f8b3148b3df",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0Njc1NjU2MzM1MiwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NjZkMWJkZjQ1NThiMGQxNjk5MGIyNDFlODE4NWNiZjU4YzNlMDNjMjRkOTA0NTQ0ZThjYzY1YjFjMzhkMSIKICAgIH0KICB9Cn0=\", \"n8vct7fj3NdiODy/h6PJhjxSR2U7d8gQxHjdVq553HqG60SSczt1Tl9XhvQbZP14ZrJGWxCziauuYb/HjZza0ugNAhfwh9z8hqR1PjBJGdvp+wpxoWpP2wd5RT9i5/GYEqpiAIAt9vHY1YeyejFqKZhXaGgp7gZZNjOQWQqB0SZzTUTAPzRW9FiY8x2re7H7Y1POThRXOvvkeQ6qWdPV6Hk5hTumV0rfEGm971jQRIbfdzBZDJRcro+8y+dlje/NpF5qf0JLy78Xr4hc2cwbT9+wqOeoUTcM/r9mwL15OKgFLjB44jszauKRHNfoqb6B3+1fNQEJrJK/7hIyvswpde7C5uOxkE7oMFib6X68VVEhb6PGC1+HWNaMaGjI0wWEkCahp48ihN9+sBEBFXOxIAhXG/pvJcbEi742/cBS1CTtOI8qui6JSL9MKX5jmyhtjjibOYRZbacosqayCnwsJAdMmwS7zIxc8jGhpfKECiSni/baS4zRla0bns4hfEM0l7ASv0Dh99WQD0We2ZMqltmix8lMEaAILXJVBln3CFrjYvncfku2hrSaqu1lNAtfMYYSITEUCBJ7McVAVmUHOrER0XGoVs9L237H0BQNSLqlGGEQ7OM8HV6G0YfXGlbNgNBx6O9k8ZijuK60JeEoJapcmbNj/LVHEU+dsgcFAbo="
				)
			);
			put(
				"family_watcher_summon_undead",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§a§4§lUndead",
					"0ac53e90-4e60-388c-a754-092dd4578592",
					"eyJ0aW1lc3RhbXAiOjE1ODYwNDAyMDM1NzMsInByb2ZpbGVJZCI6ImRhNDk4YWM0ZTkzNzRlNWNiNjEyN2IzODA4NTU3OTgzIiwicHJvZmlsZU5hbWUiOiJOaXRyb2hvbGljXzIiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y0NjI0YTlhOGM2OWNhMjA0NTA0YWJiMDQzZDQ3NDU2Y2Q5YjA5NzQ5YTM2MzU3NDYyMzAzZjI3NmEyMjlkNCJ9fX0="
				)
			);
			put(
				"family_dungeon_respawning_skeleton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aUndead Skeleton",
					"53924f1a-87e6-4709-8e53-f1c7d13dc239",
					"ewogICJ0aW1lc3RhbXAiIDogMTY1NTYzMTgzNjcwNCwKICAicHJvZmlsZUlkIiA6ICI1MzkyNGYxYTg3ZTY0NzA5OGU1M2YxYzdkMTNkYzIzOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaHJvd3BvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg4ZWI2OGE0ZDM4ZTlmNDQ2YjhlOTkyNzVmMTYwMzAyZjM2NmVmMTAyMTZhYmY5NDg0ODdlNTgyNTEyYmQwZjMiCiAgICB9CiAgfQp9="
				)
			);
			put(
				"family_crypt_witherskeleton",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aWithermancer",
					"d0e05de7-6067-454d-beae-c6d19d886191",
					"ewogICJ0aW1lc3RhbXAiIDogMTY1NTYzMjg1MTE1NCwKICAicHJvZmlsZUlkIiA6ICJkMGUwNWRlNzYwNjc0NTRkYmVhZWM2ZDE5ZDg4NjE5MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNb3VsYmVycnkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNlMTU1MjI0ZWE0YmM0OWE4ZTkxOTA3MzdjYjA0MTdkOGE3YzM4YTAzN2Q4ZDAzODJkZGU0ODI5YzEwMzU5MCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
				)
			);
			put(
				"family_zombie_commander",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aZombie Commander",
					"9673d491-d589-44cb-b63e-5f8b3148b3df",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0Njc1NjU2MzM1MiwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NjZkMWJkZjQ1NThiMGQxNjk5MGIyNDFlODE4NWNiZjU4YzNlMDNjMjRkOTA0NTQ0ZThjYzY1YjFjMzhkMSIKICAgIH0KICB9Cn0=\", \"n8vct7fj3NdiODy/h6PJhjxSR2U7d8gQxHjdVq553HqG60SSczt1Tl9XhvQbZP14ZrJGWxCziauuYb/HjZza0ugNAhfwh9z8hqR1PjBJGdvp+wpxoWpP2wd5RT9i5/GYEqpiAIAt9vHY1YeyejFqKZhXaGgp7gZZNjOQWQqB0SZzTUTAPzRW9FiY8x2re7H7Y1POThRXOvvkeQ6qWdPV6Hk5hTumV0rfEGm971jQRIbfdzBZDJRcro+8y+dlje/NpF5qf0JLy78Xr4hc2cwbT9+wqOeoUTcM/r9mwL15OKgFLjB44jszauKRHNfoqb6B3+1fNQEJrJK/7hIyvswpde7C5uOxkE7oMFib6X68VVEhb6PGC1+HWNaMaGjI0wWEkCahp48ihN9+sBEBFXOxIAhXG/pvJcbEi742/cBS1CTtOI8qui6JSL9MKX5jmyhtjjibOYRZbacosqayCnwsJAdMmwS7zIxc8jGhpfKECiSni/baS4zRla0bns4hfEM0l7ASv0Dh99WQD0We2ZMqltmix8lMEaAILXJVBln3CFrjYvncfku2hrSaqu1lNAtfMYYSITEUCBJ7McVAVmUHOrER0XGoVs9L237H0BQNSLqlGGEQ7OM8HV6G0YfXGlbNgNBx6O9k8ZijuK60JeEoJapcmbNj/LVHEU+dsgcFAbo="
				)
			);
			put(
				"family_zombie_grunt",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aZombie Grunt",
					"9673d491-d589-44cb-b63e-5f8b3148b3df",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0Njc1NjU2MzM1MiwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NjZkMWJkZjQ1NThiMGQxNjk5MGIyNDFlODE4NWNiZjU4YzNlMDNjMjRkOTA0NTQ0ZThjYzY1YjFjMzhkMSIKICAgIH0KICB9Cn0=\", \"n8vct7fj3NdiODy/h6PJhjxSR2U7d8gQxHjdVq553HqG60SSczt1Tl9XhvQbZP14ZrJGWxCziauuYb/HjZza0ugNAhfwh9z8hqR1PjBJGdvp+wpxoWpP2wd5RT9i5/GYEqpiAIAt9vHY1YeyejFqKZhXaGgp7gZZNjOQWQqB0SZzTUTAPzRW9FiY8x2re7H7Y1POThRXOvvkeQ6qWdPV6Hk5hTumV0rfEGm971jQRIbfdzBZDJRcro+8y+dlje/NpF5qf0JLy78Xr4hc2cwbT9+wqOeoUTcM/r9mwL15OKgFLjB44jszauKRHNfoqb6B3+1fNQEJrJK/7hIyvswpde7C5uOxkE7oMFib6X68VVEhb6PGC1+HWNaMaGjI0wWEkCahp48ihN9+sBEBFXOxIAhXG/pvJcbEi742/cBS1CTtOI8qui6JSL9MKX5jmyhtjjibOYRZbacosqayCnwsJAdMmwS7zIxc8jGhpfKECiSni/baS4zRla0bns4hfEM0l7ASv0Dh99WQD0We2ZMqltmix8lMEaAILXJVBln3CFrjYvncfku2hrSaqu1lNAtfMYYSITEUCBJ7McVAVmUHOrER0XGoVs9L237H0BQNSLqlGGEQ7OM8HV6G0YfXGlbNgNBx6O9k8ZijuK60JeEoJapcmbNj/LVHEU+dsgcFAbo="
				)
			);
			put(
				"family_zombie_knight",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aZombie Knight",
					"34af9e21-dff4-3b94-9fb5-07816e41af75",
					"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjVkMmYzMWJhMTYyZmU2MjcyZTgzMWFlZDE3ZjUzMjEzZGI2ZmExYzRjYmU0ZmM4MjdmMzk2M2NjOThiOSJ9fX0="
				)
			);
			put(
				"family_zombie_soldier",
				Utils.createSkull(
					EnumChatFormatting.AQUA + "§aZombie Soldier",
					"9673d491-d589-44cb-b63e-5f8b3148b3df",
					"ewogICJ0aW1lc3RhbXAiIDogMTY0Njc1NjU2MzM1MiwKICAicHJvZmlsZUlkIiA6ICJkYmQ4MDQ2M2EwMzY0Y2FjYjI3OGNhODBhMDBkZGIxMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4bG9nMjEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NjZkMWJkZjQ1NThiMGQxNjk5MGIyNDFlODE4NWNiZjU4YzNlMDNjMjRkOTA0NTQ0ZThjYzY1YjFjMzhkMSIKICAgIH0KICB9Cn0=\", \"n8vct7fj3NdiODy/h6PJhjxSR2U7d8gQxHjdVq553HqG60SSczt1Tl9XhvQbZP14ZrJGWxCziauuYb/HjZza0ugNAhfwh9z8hqR1PjBJGdvp+wpxoWpP2wd5RT9i5/GYEqpiAIAt9vHY1YeyejFqKZhXaGgp7gZZNjOQWQqB0SZzTUTAPzRW9FiY8x2re7H7Y1POThRXOvvkeQ6qWdPV6Hk5hTumV0rfEGm971jQRIbfdzBZDJRcro+8y+dlje/NpF5qf0JLy78Xr4hc2cwbT9+wqOeoUTcM/r9mwL15OKgFLjB44jszauKRHNfoqb6B3+1fNQEJrJK/7hIyvswpde7C5uOxkE7oMFib6X68VVEhb6PGC1+HWNaMaGjI0wWEkCahp48ihN9+sBEBFXOxIAhXG/pvJcbEi742/cBS1CTtOI8qui6JSL9MKX5jmyhtjjibOYRZbacosqayCnwsJAdMmwS7zIxc8jGhpfKECiSni/baS4zRla0bns4hfEM0l7ASv0Dh99WQD0We2ZMqltmix8lMEaAILXJVBln3CFrjYvncfku2hrSaqu1lNAtfMYYSITEUCBJ7McVAVmUHOrER0XGoVs9L237H0BQNSLqlGGEQ7OM8HV6G0YfXGlbNgNBx6O9k8ZijuK60JeEoJapcmbNj/LVHEU+dsgcFAbo="
				)
			);
		}
	};
	private static final LinkedHashMap<String, String> mobTypeMap = new LinkedHashMap<String, String>() {
		{
			// Island
			put("family_cave_spider", "ISLAND");
			put("family_enderman_private", "ISLAND");
			put("family_skeleton", "ISLAND");
			put("family_slime", "ISLAND");
			put("family_spider", "ISLAND");
			put("family_witch", "ISLAND");
			put("family_zombie", "ISLAND");

			// Hub
			put("family_unburried_zombie", "MOB");
			put("family_old_wolf", "MOB");
			put("family_ruin_wolf", "MOB");
			put("family_zombie_villager", "MOB");

			// Spiders Den
			put("family_arachne", "BOSS");
			put("family_arachne_brood", "MOB");
			put("family_arachne_keeper", "MOB");
			put("family_brood_mother_spider", "BOSS");
			put("family_dasher_spider", "MOB");
			put("family_respawning_skeleton", "MOB");
			put("family_random_slime", "MOB");
			put("family_spider_jockey", "MOB");
			put("family_splitter_spider", "MOB");
			put("family_voracious_spider", "MOB");
			put("family_weaver_spider", "MOB");

			// The End
			put("family_dragon", "BOSS");
			put("family_enderman", "MOB");
			put("family_endermite", "MOB");
			put("family_corrupted_protector", "BOSS");
			put("family_obsidian_wither", "MOB");
			put("family_voidling_extremist", "MOB");
			put("family_voidling_fanatic", "MOB");
			put("family_watcher", "MOB");
			put("family_zealot_enderman", "MOB");

			// Crimson Isles
			put("family_ashfang", "BOSS");
			put("family_barbarian_duke_x", "BOSS");
			put("family_bladesoul", "BOSS");
			put("family_blaze", "MOB");
			put("family_flaming_spider", "MOB");
			put("family_ghast", "MOB");
			put("family_mage_outlaw", "BOSS");
			put("family_magma_cube", "MOB");
			put("family_magma_boss", "BOSS");
			put("family_matcho", "MOB");
			put("family_charging_mushroom_cow", "MOB");
			put("family_pigman", "MOB");
			put("family_wither_skeleton", "MOB");
			put("family_wither_spectre", "MOB");

			// Deep Caverns
			put("family_automaton", "MOB");
			put("family_butterfly", "MOB");
			put("family_emerald_slime", "MOB");
			put("family_caverns_ghost", "MOB");
			put("family_goblin", "MOB");
			put("family_team_treasurite", "MOB");
			put("family_ice_walker", "MOB");
			put("family_lapis_zombie", "MOB");
			put("family_diamond_skeleton", "MOB");
			put("family_diamond_zombie", "MOB");
			put("family_redstone_pigman", "MOB");
			put("family_sludge", "MOB");
			put("family_invisible_creeper", "MOB");
			put("family_thyst", "MOB");
			put("family_treasure_hoarder", "MOB");
			put("family_worms", "MOB");
			put("family_yog", "MOB");

			// The Park
			put("family_howling_spirit", "MOB");
			put("family_pack_spirit", "MOB");
			put("family_soul_of_the_alpha", "MOB");

			// Spooky
			put("family_batty_witch", "MOB");
			put("family_headless_horseman", "BOSS");
			put("family_phantom_spirit", "MOB");
			put("family_scary_jerry", "MOB");
			put("family_trick_or_treater", "MOB");
			put("family_wither_gourd", "MOB");
			put("family_wraith", "MOB");

			// Catacombs
			put("family_diamond_guy", "MOB");
			put("family_cellar_spider", "MOB");
			put("family_crypt_dreadlord", "MOB");
			put("family_crypt_lurker", "MOB");
			put("family_crypt_souleater", "MOB");
			put("family_king_midas", "MOB");
			put("family_lonely_spider", "MOB");
			put("family_lost_adventurer", "MOB");
			put("family_scared_skeleton", "MOB");
			put("family_shadow_assassin", "MOB");
			put("family_skeleton_grunt", "MOB");
			put("family_skeleton_master", "MOB");
			put("family_skeleton_soldier", "MOB");
			put("family_skeletor", "MOB");
			put("family_sniper_skeleton", "MOB");
			put("family_super_archer", "MOB");
			put("family_super_tank_zombie", "MOB");
			put("family_crypt_tank_zombie", "MOB");
			put("family_watcher_summon_undead", "MOB");
			put("family_dungeon_respawning_skeleton", "MOB");
			put("family_crypt_witherskeleton", "MOB");
			put("family_zombie_commander", "MOB");
			put("family_zombie_grunt", "MOB");
			put("family_zombie_knight", "MOB");
			put("family_zombie_soldier", "MOB");
		}
	};

	public static LinkedHashMap<ItemStack, List<String>> getBestiaryLocations() {
		return bestiaryLocations;
	}

	public static LinkedHashMap<String, ItemStack> getBestiaryMobs() {
		return bestiaryMobs;
	}

	public static LinkedHashMap<String, String> getMobType() {
		return mobTypeMap;
	}
}
