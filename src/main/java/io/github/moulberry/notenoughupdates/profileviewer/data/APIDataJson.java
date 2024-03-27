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

package io.github.moulberry.notenoughupdates.profileviewer.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.github.moulberry.notenoughupdates.profileviewer.rift.RiftPage.readBase64;

public class APIDataJson {
	public Leveling leveling = new Leveling();

	public static class Leveling {
		public int highest_pet_score = 0;
		public int mining_fiesta_ores_mined = 0;
		public int fishing_festival_sharks_killed = 0;

		public Completions completions = new Completions();

		public static class Completions {
			public int NUCLEUS_RUNS = 0;
		}
	}

	public Currencies currencies = new Currencies();

	public static class Currencies {
		public float coin_purse = 0;
		public float motes_purse = 0;
	}

	public Profile profile = new Profile();

	public static class Profile {
		public int personal_bank_upgrade = 0;
	}

	public Experimentation experimentation = new Experimentation();

	public static class Experimentation {
		public int serums_drank = 0;
	}

	public Player_Stats player_stats;

	public static class Player_Stats {
		public Pets pets = new Pets();

		public static class Pets {
			public Milestone milestone = new Milestone();

			public static class Milestone {
				public float ores_mined = 0;
				public float sea_creatures_killed = 0;
			}
		}

		public Auctions auctions = new Auctions();

		public static class Auctions {
			public float highest_bid = 0;
			public float bids = 0;
			public float won = 0;
			public float created = 0;
			public float gold_spent = 0;
			public float gold_earned = 0;
		}

		public ItemsFished items_fished = new ItemsFished();

		public static class ItemsFished {
			public float total = 0;
			public float treasure = 0;
			public float large_treasure = 0;
		}

		public Rift rift = new Rift();

		public static class Rift {
			public int lifetime_motes_earned = 0;
		}
	}

	public FairySouls fairy_soul = new FairySouls();

	public static class FairySouls {
		public int total_collected = 0;
	}

	public @Nullable NetherData nether_island_player_data;

	public static class NetherData {
		public @Nullable JsonObject dojo;

		public @Nullable Abiphone abiphone;

		public static class Abiphone {

			public OperatorChip operator_chip = new OperatorChip();

			public static class OperatorChip {
				public int repaired_index = 0;
			}
		}
	}

	public MiningCore mining_core = new MiningCore();

	public static class MiningCore {
		public float powder_mithril = 0;
		public float powder_gemstone = 0;
		public float powder_spent_mithril = 0;
		public float powder_spent_gemstone = 0;

		public Nodes nodes = new Nodes();

		public static class Nodes {
			public int mining_fortune = 0;
			public int mining_speed = 0;
			public int daily_powder = 0;
			public int efficient_miner = 0;
			public int titanium_insanium = 0;
			public int random_event = 0;
			public int mining_madness = 0;
			public int daily_effect = 0;
			public int goblin_killer = 0;
			public int mining_experience = 0;
			public int forge_time = 0;
			public int front_loaded = 0;
			public int experience_orbs = 0;
			public int fallen_star_bonus = 0;
			public int professional = 0;
			public int great_explorer = 0;
			public int fortunate = 0;
			public int lonesome_miner = 0;
			public int mining_fortune_2 = 0;
			public int mining_speed_2 = 0;
			public int mining_speed_boost = 0;
			public int vein_seeker = 0;
			public int powder_buff = 0;
			public int special_0 = 0;
			public int precision_mining = 0;
			public int star_powder = 0;
			public int pickaxe_toss = 0;
			public int maniac_miner = 0;
			public int mole = 0;
		}
	}

	public @Nullable Rift rift;

	public static class Rift {
		public @Nullable RiftDeadCats dead_cats;

		public static class RiftDeadCats {
			public List<String> found_cats = new ArrayList<>();
			public Pet montezuma = new Pet();

			public static class Pet {
				public String type = "UNKNOWN";
				public String tier = "EPIC";
				public Long exp = 0L;
				public int candyUsed = 0;
			}
		}

		public RiftInventory inventory = new RiftInventory();

		public static class RiftInventory {
			public @Nullable Inventory inv_contents;
			public @Nullable Inventory inv_armor;
			public @Nullable Inventory equipment_contents;
			public @Nullable Inventory ender_chest_contents;

			public static class Inventory {
				private String data;

				public List<JsonObject> readItems() {
					if (data == null) return null;
					return readBase64(data);
				}
			}
		}

		public @Nullable RiftGallery gallery;

		public static class RiftGallery {
			public @Nullable JsonArray secured_trophies;
		}

		public RiftCastle castle = new RiftCastle();

		public static class RiftCastle {
			public int grubber_stacks = 0;
		}

		public RiftEnigma enigma = new RiftEnigma();

		public static class RiftEnigma {
			public List<String> found_souls = new ArrayList<>();
		}
	}

	public PlayerData player_data = new PlayerData();

	public static class PlayerData {
		public int reaper_peppers_eaten = 0;
	}

	public @Nullable AccessoryBagStorage accessory_bag_storage;

	public static class AccessoryBagStorage {
		public int bag_upgrades_purchased = 0;
		public int highest_magical_power = 0;
		public List<String> unlocked_powers = new ArrayList<>();
	}

	public JacobsContest jacobs_contest = new JacobsContest();

	public static class JacobsContest {
		public Perks perks = new Perks();

		public static class Perks {
			public int double_drops = 0;
			public int farming_level_cap = 0;
		}
	}
}
