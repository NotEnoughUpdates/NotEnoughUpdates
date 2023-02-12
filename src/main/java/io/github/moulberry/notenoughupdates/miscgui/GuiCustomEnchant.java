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

package io.github.moulberry.notenoughupdates.miscgui;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.GlScissorStack;
import io.github.moulberry.notenoughupdates.core.GuiElementTextField;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingFloat;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingInteger;
import io.github.moulberry.notenoughupdates.miscfeatures.SlotLocking;
import io.github.moulberry.notenoughupdates.miscgui.util.OrbDisplay;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.ItemUtils;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuiCustomEnchant extends Gui {
	private static final GuiCustomEnchant INSTANCE = new GuiCustomEnchant();
	private static final ResourceLocation TEXTURE = new ResourceLocation("notenoughupdates:custom_enchant_gui.png");
	private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE = new ResourceLocation(
		"textures/entity/enchanting_table_book.png");
	private static final ModelBook MODEL_BOOK = new ModelBook();

	private static final Pattern XP_COST_PATTERN = Pattern.compile("\\u00a73(\\d+) Exp Levels");
	private static final Pattern ENCHANT_LEVEL_PATTERN = Pattern.compile("(.*)_(.*)");
	private static final Pattern ENCHANT_NAME_PATTERN = Pattern.compile("([^IVX]*) ([IVX]*)");

	private enum EnchantState {
		NO_ITEM,
		ADDING_ENCHANT,
		SWITCHING_DONT_UPDATE,
		INVALID_ITEM,
		HAS_ITEM
	}

	private class Enchantment {
		public int slotIndex;
		public String enchantName;
		public String enchId;
		public List<String> displayLore;
		public int level;
		public int xpCost = -1;
		public boolean overMaxLevel = false;
		public boolean conflicts = false;

		public Enchantment(
			int slotIndex, String enchantName, String enchId, List<String> displayLore, int level,
			boolean useMaxLevelForCost, boolean checkConflicts
		) {
			this.slotIndex = slotIndex;
			this.enchantName = enchantName;
			this.enchId = enchId;
			this.displayLore = displayLore;
			this.level = level;
			this.enchId = ItemUtils.fixEnchantId(enchId, true);

			if (Constants.ENCHANTS != null) {
				if (checkConflicts && Constants.ENCHANTS.has("enchant_pools")) {
					JsonArray pools = Constants.ENCHANTS.getAsJsonArray("enchant_pools");
					out:
					for (int i = 0; i < pools.size(); i++) {
						JsonArray pool = pools.get(i).getAsJsonArray();

						boolean hasThis = false;
						boolean hasApplied = false;

						for (int j = 0; j < pool.size(); j++) {
							String enchIdPoolElement = pool.get(j).getAsString();
							if (enchId.equalsIgnoreCase(enchIdPoolElement)) {
								hasThis = true;
							} else if (playerEnchantIds.containsKey(enchIdPoolElement)) {
								hasApplied = true;
							}
							if (hasThis && hasApplied) {
								this.conflicts = true;
								break out;
							}
						}
					}
				}

				if (level >= 1 && Constants.ENCHANTS.has("enchants_xp_cost")) {
					JsonObject allCosts = Constants.ENCHANTS.getAsJsonObject("enchants_xp_cost");
					JsonObject maxLevel = null;
					if (Constants.ENCHANTS.has("max_xp_table_levels")) {
						maxLevel = Constants.ENCHANTS.getAsJsonObject("max_xp_table_levels");
					}

					if (allCosts.has(this.enchId)) {
						JsonArray costs = allCosts.getAsJsonArray(this.enchId);

						if (costs.size() >= 1) {
							if (useMaxLevelForCost) {
								int cost =
									(maxLevel != null && maxLevel.has(this.enchId) ? maxLevel.get(this.enchId).getAsInt() : costs.size());
								this.xpCost = costs.get(cost - 1).getAsInt();
							} else if (level - 1 < costs.size()) {
								this.xpCost = costs.get(level - 1).getAsInt();
							} else {
								overMaxLevel = true;
							}
						}
					}

				}
			}
		}
	}

	public OrbDisplay orbDisplay = new OrbDisplay();

	private int guiLeft;
	private int guiTop;
	private boolean shouldOverrideFast = false;

	public float pageOpen;
	public float pageOpenLast;
	public float pageOpenRandom;
	public float pageOpenVelocity;
	public float bookOpen;
	public float bookOpenLast;

	private int currentPage;
	private int expectedMaxPage;

	private boolean isScrollingLeft = true;

	private ItemStack enchantingItem = null;

	private int removingEnchantPlayerLevel = -1;

	private final GuiElementTextField searchField = new GuiElementTextField("", GuiElementTextField.SCISSOR_TEXT);

	private final HashMap<String, Integer> playerEnchantIds = new HashMap<>();

	private boolean searchRemovedFromApplicable = false;
	private boolean searchRemovedFromRemovable = false;
	private final List<Enchantment> applicable = new ArrayList<>();
	private final List<Enchantment> removable = new ArrayList<>();

	private final HashMap<Integer, Enchantment> enchanterEnchLevels = new HashMap<>();
	private Enchantment enchanterCurrentEnch = null;

	public Random random = new Random();

	private EnchantState currentState = EnchantState.NO_ITEM;
	private EnchantState lastState = EnchantState.NO_ITEM;

	private final LerpingInteger leftScroll = new LerpingInteger(0, 150);
	private final LerpingInteger rightScroll = new LerpingInteger(0, 150);

	private final LerpingFloat arrowAmount = new LerpingFloat(0, 100);

	private static final int X_SIZE = 364;
	private static final int Y_SIZE = 215;

	private int clickedScrollOffset = -1;
	private boolean isClickedScrollLeft = true;

	private boolean isChangingEnchLevel = false;

	private long cancelButtonAnimTime = 0;
	private long confirmButtonAnimTime = 0;

	public static GuiCustomEnchant getInstance() {
		return INSTANCE;
	}

	public boolean shouldOverride(String containerName) {
		if (containerName == null) {
			shouldOverrideFast = false;
			return false;
		}
		shouldOverrideFast = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enableTableGUI &&
			(containerName.length() >= 12 && Objects.equals(
				"Enchant Item",
				containerName.substring(0, "Enchant Item".length())
			)) &&
			NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard();
		if (!shouldOverrideFast) {
			currentState = EnchantState.NO_ITEM;
			applicable.clear();
			removable.clear();
			expectedMaxPage = 1;
		}
		GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);
		ContainerChest cc = (ContainerChest) chest.inventorySlots;
		ItemStack hexStack = cc.getLowerChestInventory().getStackInSlot(50);
		if (hexStack != null && hexStack.getItem() == Items.experience_bottle) {
			shouldOverrideFast = false;
			return false;
		}
		return shouldOverrideFast;
	}

	private int tickCounter = 0;

	public void tick() {
		GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);
		ContainerChest cc = (ContainerChest) chest.inventorySlots;

		ItemStack stack = cc.getLowerChestInventory().getStackInSlot(23);
		ItemStack arrowStack = cc.getLowerChestInventory().getStackInSlot(45);
		ItemStack enchantingItemStack = cc.getLowerChestInventory().getStackInSlot(19);
		ItemStack hexStack = cc.getLowerChestInventory().getStackInSlot(50);

		int lastPage = currentPage;

		this.lastState = currentState;
		if (hexStack != null && hexStack.getItem() == Items.experience_bottle) return;
		if (arrowStack != null && arrowStack.getItem() == Items.arrow && enchantingItem != null) {
			currentState = EnchantState.ADDING_ENCHANT;
		} else if (stack == null || enchantingItemStack == null) {
			if (currentState == EnchantState.SWITCHING_DONT_UPDATE || currentState == EnchantState.NO_ITEM) {
				currentState = EnchantState.NO_ITEM;
			} else {
				currentState = EnchantState.SWITCHING_DONT_UPDATE;
			}
		} else if (stack.getItem() != Items.dye) {
			ItemStack sanityCheckStack = cc.getLowerChestInventory().getStackInSlot(12);
			if (sanityCheckStack == null || sanityCheckStack.getItem() == Items.enchanted_book) {
				currentState = EnchantState.HAS_ITEM;
				enchantingItem = enchantingItemStack;
			} else {
				currentState = EnchantState.SWITCHING_DONT_UPDATE;
			}
		} else if (stack.getItemDamage() == 1) {
			currentState = EnchantState.INVALID_ITEM;
		} else {
			currentState = EnchantState.NO_ITEM;
		}

		if (currentState == EnchantState.HAS_ITEM) {
			ItemStack pageUpStack = cc.getLowerChestInventory().getStackInSlot(17);
			ItemStack pageDownStack = cc.getLowerChestInventory().getStackInSlot(35);
			if (pageUpStack != null && pageDownStack != null) {
				currentPage = 0;
				boolean upIsGlass = pageUpStack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane);
				boolean downIsGlass = pageDownStack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane);
				int page = -1;

				expectedMaxPage = 1;
				if (!downIsGlass) {
					try {
						page = Integer.parseInt(Utils.getRawTooltip(pageDownStack).get(1).substring(11)) - 1;
						expectedMaxPage = page + 1;
					} catch (Exception ignored) {
					}
				}
				if (page == -1 && !upIsGlass) {
					try {
						page = Integer.parseInt(Utils.getRawTooltip(pageUpStack).get(1).substring(11)) + 1;
						expectedMaxPage = page;
					} catch (Exception ignored) {
					}
				}
				if (page == -1) {
					currentPage = 1;
				} else {
					currentPage = page;
				}
			}
		}

		orbDisplay.physicsTickOrbs();

		if (++tickCounter >= 20) {
			tickCounter = 0;
		}

		boolean updateItems = tickCounter == 0;

		if (currentState == EnchantState.ADDING_ENCHANT) {
			if (arrowAmount.getTarget() != 1) {
				arrowAmount.setTarget(1);
				arrowAmount.resetTimer();
			}
		} else {
			if (arrowAmount.getTarget() != 0) {
				arrowAmount.setTarget(0);
				arrowAmount.resetTimer();
			}
		}

		// Set<EnchantState> allowedSwitchStates = Sets.newHashSet(EnchantState.ADDING_ENCHANT, EnchantState.HAS_ITEM, EnchantState.SWITCHING_DONT_UPDATE);
		if (lastState != currentState || lastPage != currentPage) {
			// if (!allowedSwitchStates.contains(lastState) || !allowedSwitchStates.contains(currentState)) {
			leftScroll.setValue(0);
			rightScroll.setValue(0);
			// }
			updateItems = true;
		}

		if (updateItems && currentState != EnchantState.SWITCHING_DONT_UPDATE) {
			enchanterEnchLevels.clear();

			if (enchantingItem != null) {
				playerEnchantIds.clear();
				NBTTagCompound tag = enchantingItem.getTagCompound();
				if (tag != null) {
					NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
					if (ea != null) {
						NBTTagCompound enchantments = ea.getCompoundTag("enchantments");
						if (enchantments != null) {
							for (String enchId : enchantments.getKeySet()) {
								playerEnchantIds.put(enchId, enchantments.getInteger(enchId));
							}
						}
					}
				}
			}

			if (currentState == EnchantState.ADDING_ENCHANT) {
				removingEnchantPlayerLevel = -1;
				boolean updateLevel = enchanterCurrentEnch == null;
				for (int i = 0; i < 27; i++) {
					int slotIndex = 9 + i;
					ItemStack book = cc.getLowerChestInventory().getStackInSlot(slotIndex);
					if (book != null && book.getItem() == Items.enchanted_book) {
						NBTTagCompound tagBook = book.getTagCompound();
						if (tagBook != null) {
							NBTTagCompound ea = tagBook.getCompoundTag("ExtraAttributes");
							if (ea != null) {
								NBTTagCompound enchantments = ea.getCompoundTag("enchantments");
								if (enchantments != null) {
									String enchId = Utils
										.cleanColour(book.getDisplayName())
										.toLowerCase()
										.replace(" ", "_")
										.replace("-", "_")
										.replaceAll("[^a-z_]", "");
									String name = Utils.cleanColour(book.getDisplayName());
									int enchLevel = -1;
									if (name.equalsIgnoreCase("Bane of Arthropods")) {
										name = "Bane of Arth.";
									} else if (name.equalsIgnoreCase("Projectile Protection")) {
										name = "Projectile Prot";
									} else if (name.equalsIgnoreCase("Blast Protection")) {
										name = "Blast Prot";
									}
									Matcher levelMatcher = ENCHANT_LEVEL_PATTERN.matcher(enchId);
									if (levelMatcher.matches()) {
										enchLevel = Utils.parseRomanNumeral(levelMatcher.group(2).toUpperCase());
										enchId = levelMatcher.group(1);
									}
									Enchantment enchantment = new Enchantment(slotIndex, name, enchId,
										Utils.getRawTooltip(book), enchLevel, false, true
									);
									enchantment.displayLore.remove(0);

									if (removingEnchantPlayerLevel == -1 && playerEnchantIds.containsKey(enchId)) {
										removingEnchantPlayerLevel = playerEnchantIds.get(enchId);
									}

									if (removingEnchantPlayerLevel >= 0 && enchantment.level < removingEnchantPlayerLevel) {
										continue;
									}

									if (enchanterCurrentEnch == null) {
										enchanterCurrentEnch = enchantment;
									} else if (updateLevel) {
										if (removingEnchantPlayerLevel < 0 && enchantment.level > enchanterCurrentEnch.level) {
											enchanterCurrentEnch = enchantment;
										} else if (removingEnchantPlayerLevel >= 0 && enchantment.level < enchanterCurrentEnch.level) {
											enchanterCurrentEnch = enchantment;
										}
									}

									enchanterEnchLevels.put(enchantment.level, enchantment);
								}
							}
						}
					}
				}
				if (enchanterCurrentEnch != null && removingEnchantPlayerLevel >= 0) {
					for (String line : enchanterCurrentEnch.displayLore) {
						Matcher matcher = XP_COST_PATTERN.matcher(line);
						if (matcher.find()) {
							enchanterCurrentEnch.xpCost = Integer.parseInt(matcher.group(1));
						}
					}
				}
			} else {
				isChangingEnchLevel = false;
				enchanterCurrentEnch = null;

				searchRemovedFromRemovable = false;
				searchRemovedFromApplicable = false;
				applicable.clear();
				removable.clear();
				if (currentState == EnchantState.HAS_ITEM) {
					for (int i = 0; i < 15; i++) {
						int slotIndex = 12 + (i % 5) + (i / 5) * 9;
						ItemStack book = cc.getLowerChestInventory().getStackInSlot(slotIndex);
						if (book != null) {
							NBTTagCompound tagBook = book.getTagCompound();
							if (tagBook != null) {
								NBTTagCompound ea = tagBook.getCompoundTag("ExtraAttributes");
								if (ea != null) {
									NBTTagCompound enchantments = ea.getCompoundTag("enchantments");
									if (enchantments != null) {
										String enchId = Utils
											.cleanColour(book.getDisplayName())
											.toLowerCase()
											.replace(" ", "_")
											.replace("-", "_")
											.replaceAll("[^a-z_]", "");
										if (enchId.equalsIgnoreCase("_")) continue;
										enchId = ItemUtils.fixEnchantId(enchId, true);
										String name = Utils.cleanColour(book.getDisplayName());

										if (searchField.getText().trim().isEmpty() ||
											name.toLowerCase().contains(searchField.getText().trim().toLowerCase())) {
											if (name.equalsIgnoreCase("Bane of Arthropods")) {
												name = "Bane of Arth.";
											} else if (name.equalsIgnoreCase("Projectile Protection")) {
												name = "Projectile Prot";
											} else if (name.equalsIgnoreCase("Blast Protection")) {
												name = "Blast Prot";
											} else if (name.equalsIgnoreCase("Luck of the Sea")) {
												name = "Luck of Sea";
											}
											Matcher nameMatcher = ENCHANT_NAME_PATTERN.matcher(name);
											if (nameMatcher.matches()) {
												name = nameMatcher.group(1);
											}

											if (playerEnchantIds.containsKey(enchId)) {
												Enchantment enchantment = new Enchantment(slotIndex, name, enchId,
													Utils.getRawTooltip(book), playerEnchantIds.get(enchId), false, false
												);
												if (!enchantment.overMaxLevel) {
													removable.add(enchantment);
												}
											} else {
												Enchantment enchantment = new Enchantment(slotIndex, name, enchId,
													Utils.getRawTooltip(book), 1, true, true
												);
												applicable.add(enchantment);
											}
										} else {
											if (playerEnchantIds.containsKey(enchId)) {
												searchRemovedFromRemovable = true;
											} else {
												searchRemovedFromApplicable = true;
											}
										}

									}
								}
							}
						}
					}
					NEUConfig cfg = NotEnoughUpdates.INSTANCE.config;
					int mult = cfg.enchantingSolvers.enchantOrdering == 0 ? 1 : -1;
					Comparator<Enchantment> comparator = cfg.enchantingSolvers.enchantSorting == 0 ?
						Comparator.comparingInt(e -> mult * e.xpCost) :
						(c1, c2) -> mult *
							c1.enchId.toLowerCase().compareTo(c2.enchId.toLowerCase());
					removable.sort(comparator);
					applicable.sort(comparator);
				}
			}
		}

		//Update book model state
		if (lastState != currentState) {
			while (true) {
				this.pageOpenRandom += (float) (this.random.nextInt(4) - this.random.nextInt(4));

				if (this.pageOpen > this.pageOpenRandom + 1.0F || this.pageOpen < this.pageOpenRandom - 1.0F) {
					break;
				}
			}
		}

		this.pageOpenLast = this.pageOpen;
		this.bookOpenLast = this.bookOpen;

		if (currentState == EnchantState.HAS_ITEM || currentState == EnchantState.ADDING_ENCHANT) {
			this.bookOpen += 0.2F;
		} else {
			this.bookOpen -= 0.2F;
		}

		this.bookOpen = MathHelper.clamp_float(this.bookOpen, 0.0F, 1.0F);
		float f1 = (this.pageOpenRandom - this.pageOpen) * 0.4F;
		f1 = MathHelper.clamp_float(f1, -0.2F, 0.2F);
		this.pageOpenVelocity += (f1 - this.pageOpenVelocity) * 0.9F;
		this.pageOpen += this.pageOpenVelocity;
	}

	private List<String> createTooltip(String title, int selectedOption, String... options) {
		String selPrefix = EnumChatFormatting.DARK_AQUA + " \u25b6 ";
		String unselPrefix = EnumChatFormatting.GRAY.toString();

		for (int i = 0; i < options.length; i++) {
			if (i == selectedOption) {
				options[i] = selPrefix + options[i];
			} else {
				options[i] = unselPrefix + options[i];
			}
		}

		List<String> list = Lists.newArrayList(options);
		list.add(0, "");
		list.add(0, EnumChatFormatting.GREEN + title);
		return list;
	}

	public void render(float partialTicks) {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return;

		long currentTime = System.currentTimeMillis();
		int playerXpLevel = Minecraft.getMinecraft().thePlayer.experienceLevel;

		GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);
		ContainerChest cc = (ContainerChest) chest.inventorySlots;

		leftScroll.tick();
		rightScroll.tick();
		arrowAmount.tick();

		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledResolution.getScaledWidth();
		int height = scaledResolution.getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		guiLeft = (width - X_SIZE) / 2;
		guiTop = (height - Y_SIZE) / 2;

		List<String> tooltipToDisplay = null;
		boolean disallowClick = false;
		ItemStack stackOnMouse = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
		int itemHoverX = -1;
		int itemHoverY = -1;
		boolean hoverLocked = false;

		drawGradientRect(0, 0, width, height, 0xc0101010, 0xd0101010);

		//Base Texture
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		Utils.drawTexturedRect(guiLeft, guiTop, X_SIZE, Y_SIZE,
			0, X_SIZE / 512f, 0, Y_SIZE / 512f, GL11.GL_NEAREST
		);

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		fr.drawString("Applicable", guiLeft + 7, guiTop + 7, 0x404040, false);
		fr.drawString("Removable", guiLeft + 247, guiTop + 7, 0x404040, false);

		//Page Text
		if (currentState == EnchantState.HAS_ITEM || currentState == EnchantState.ADDING_ENCHANT) {
			String pageStr = "Page: " + currentPage + "/" + expectedMaxPage;
			int pageStrLen = fr.getStringWidth(pageStr);
			Utils.drawStringCentered(pageStr, guiLeft + X_SIZE / 2, guiTop + 14, false, 0x404040);

			//Page Arrows
			Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(guiLeft + X_SIZE / 2 - pageStrLen / 2 - 2 - 15, guiTop + 6, 15, 15,
				0, 15 / 512f, 372 / 512f, 387 / 512f, GL11.GL_NEAREST
			);
			Utils.drawTexturedRect(guiLeft + X_SIZE / 2 + pageStrLen / 2 + 2, guiTop + 6, 15, 15,
				15 / 512f, 30 / 512f, 372 / 512f, 387 / 512f, GL11.GL_NEAREST
			);
		}

		//Settings Buttons
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		//On Settings Button
		Utils.drawTexturedRect(guiLeft + 295, guiTop + 147, 16, 16,
			0, 16 / 512f, 387 / 512f, (387 + 16) / 512f, GL11.GL_NEAREST
		);
		//Incompatible Settings Button
        /*float incompatibleMinU = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.incompatibleEnchants * 16 / 512f;
        Utils.drawTexturedRect(guiLeft + 295 + 18, guiTop + 147, 16, 16,
                incompatibleMinU, incompatibleMinU + 16 / 512f, 403 / 512f, (403 + 16) / 512f, GL11.GL_NEAREST);*/
		//Sorting Settings Button
		float sortingMinU = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enchantSorting * 16 / 512f;
		Utils.drawTexturedRect(guiLeft + 295, guiTop + 147 + 18, 16, 16,
			sortingMinU, sortingMinU + 16 / 512f, 419 / 512f, (419 + 16) / 512f, GL11.GL_NEAREST
		);
		//Ordering Settings Button
		float orderingMinU = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enchantOrdering * 16 / 512f;
		Utils.drawTexturedRect(guiLeft + 295 + 18, guiTop + 147 + 18, 16, 16,
			orderingMinU, orderingMinU + 16 / 512f, 435 / 512f, (435 + 16) / 512f, GL11.GL_NEAREST
		);

		if (mouseX >= guiLeft + 294 && mouseX < guiLeft + 294 + 36 &&
			mouseY >= guiTop + 146 && mouseY < guiTop + 146 + 36) {
			int index = (mouseX - (guiLeft + 295)) / 18 + (mouseY - (guiTop + 147)) / 18 * 2;
			switch (index) {
				case 0:
					Gui.drawRect(guiLeft + 295, guiTop + 147, guiLeft + 295 + 16, guiTop + 147 + 16, 0x80ffffff);
					tooltipToDisplay = createTooltip("Enable GUI", 0, "On", "Off");
					break;
                /*case 1:
                    Gui.drawRect(guiLeft + 295 + 18, guiTop + 147, guiLeft + 295 + 16 + 18, guiTop + 147 + 16, 0x80ffffff);
                    tooltipToDisplay = createTooltip("Incompatible Enchants",
                            NotEnoughUpdates.INSTANCE.config.enchantingSolvers.incompatibleEnchants,
                            "Highlight", "Hide");
                    tooltipToDisplay.add(1, EnumChatFormatting.GRAY + "How to display enchants that are");
                    tooltipToDisplay.add(2, EnumChatFormatting.GRAY + "incompatible with your current item,");
                    tooltipToDisplay.add(3, EnumChatFormatting.GRAY + "eg. Smite on a sword with Sharpness");
                    break;*/
				case 2:
					Gui.drawRect(guiLeft + 295, guiTop + 147 + 18, guiLeft + 295 + 16, guiTop + 147 + 16 + 18, 0x80ffffff);
					tooltipToDisplay = createTooltip("Sort enchants...",
						NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enchantSorting,
						"By Cost", "Alphabetically"
					);
					break;
				case 3:
					Gui.drawRect(
						guiLeft + 295 + 18,
						guiTop + 147 + 18,
						guiLeft + 295 + 16 + 18,
						guiTop + 147 + 16 + 18,
						0x80ffffff
					);
					tooltipToDisplay = createTooltip("Order enchants...",
						NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enchantOrdering,
						"Ascending", "Descending"
					);
					break;
			}
		}

		//Left scroll bar
		{
			int offset;
			if (applicable.size() <= 6) {
				offset = 0;
			} else if (isScrollingLeft && clickedScrollOffset >= 0) {
				offset = mouseY - clickedScrollOffset;
				if (offset < 0) offset = 0;
				if (offset > 96 - 15) offset = 96 - 15;
			} else {
				offset = Math.round((96 - 15) * (leftScroll.getValue() / (float) ((applicable.size() - 6) * 16)));
			}
			Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(guiLeft + 104, guiTop + 18 + offset, 12, 15,
				0, 12 / 512f, 313 / 512f, (313 + 15) / 512f, GL11.GL_NEAREST
			);
		}
		//Right scroll bar
		{
			int offset;
			if (removable.size() <= 6) {
				offset = 0;
			} else if (!isScrollingLeft && clickedScrollOffset >= 0) {
				offset = mouseY - clickedScrollOffset;
				if (offset < 0) offset = 0;
				if (offset > 96 - 15) offset = 96 - 15;
			} else {
				offset = Math.round((96 - 15) * (rightScroll.getValue() / (float) ((removable.size() - 6) * 16)));
			}
			Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(guiLeft + 344, guiTop + 18 + offset, 12, 15,
				0, 12 / 512f, 313 / 512f, (313 + 15) / 512f, GL11.GL_NEAREST
			);
		}

		//Enchant book model
		renderEnchantBook(scaledResolution, partialTicks);

		//Can't be enchanted text
		if (currentState == EnchantState.INVALID_ITEM) {
			GlStateManager.disableDepth();
			Utils.drawStringCentered("This item can't", guiLeft + X_SIZE / 2, guiTop + 88, true, 0xffff5555);
			Utils.drawStringCentered("be enchanted", guiLeft + X_SIZE / 2, guiTop + 98, true, 0xffff5555);
			GlStateManager.enableDepth();
		}

		//Enchant arrow
		if (arrowAmount.getValue() > 0) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
			GlStateManager.color(1, 1, 1, 1);
			float w = 22 * arrowAmount.getValue();
			if (removingEnchantPlayerLevel < 0) {
				Utils.drawTexturedRect(guiLeft + 134, guiTop + 58, w, 16,
					0, w / 512f, 297 / 512f, (297 + 16) / 512f, GL11.GL_NEAREST
				);
			} else {
				Utils.drawTexturedRect(guiLeft + 230 - w, guiTop + 58, w, 16,
					(44 - w) / 512f, 44 / 512f, 297 / 512f, (297 + 16) / 512f, GL11.GL_NEAREST
				);
			}
		}

		//Text if no enchants appear
		if (currentState == EnchantState.HAS_ITEM || currentState == EnchantState.ADDING_ENCHANT) {
			if (applicable.isEmpty() && removable.isEmpty() && searchRemovedFromApplicable) {
				Utils.drawStringCentered("Can't find that", guiLeft + 8 + 48, guiTop + 28, true, 0xffff5555);
				Utils.drawStringCentered("enchant, perhaps", guiLeft + 8 + 48, guiTop + 38, true, 0xffff5555);
				Utils.drawStringCentered("it is on", guiLeft + 8 + 48, guiTop + 48, true, 0xffff5555);
				Utils.drawStringCentered("another page?", guiLeft + 8 + 48, guiTop + 58, true, 0xffff5555);
			} else if (applicable.isEmpty() && !searchRemovedFromApplicable) {
				Utils.drawStringCentered("No applicable", guiLeft + 8 + 48, guiTop + 28, true, 0xffff5555);
				Utils.drawStringCentered("enchants on", guiLeft + 8 + 48, guiTop + 38, true, 0xffff5555);
				Utils.drawStringCentered("this page...", guiLeft + 8 + 48, guiTop + 48, true, 0xffff5555);
			}
			if (applicable.isEmpty() && removable.isEmpty() && searchRemovedFromRemovable) {
				Utils.drawStringCentered("Can't find that", guiLeft + 248 + 48, guiTop + 28, true, 0xffff5555);
				Utils.drawStringCentered("enchant, perhaps", guiLeft + 248 + 48, guiTop + 38, true, 0xffff5555);
				Utils.drawStringCentered("it is on", guiLeft + 248 + 48, guiTop + 48, true, 0xffff5555);
				Utils.drawStringCentered("another page?", guiLeft + 248 + 48, guiTop + 58, true, 0xffff5555);
			} else if (removable.isEmpty() && !searchRemovedFromRemovable) {
				Utils.drawStringCentered("No removable", guiLeft + 248 + 48, guiTop + 28, true, 0xffff5555);
				Utils.drawStringCentered("enchants on", guiLeft + 248 + 48, guiTop + 38, true, 0xffff5555);
				Utils.drawStringCentered("this page...", guiLeft + 248 + 48, guiTop + 48, true, 0xffff5555);
			}
		}
		//Available enchants (left)
		GlScissorStack.push(0, guiTop + 18, width, guiTop + 18 + 96, scaledResolution);
		for (int i = 0; i < 7; i++) {
			int index = i + leftScroll.getValue() / 16;

			if (applicable.size() <= index) break;
			Enchantment ench = applicable.get(index);

			int top = guiTop - (leftScroll.getValue() % 16) + 18 + 16 * i;
			int vOffset = enchanterCurrentEnch != null && enchanterCurrentEnch.enchId.equals(ench.enchId) ? 16 : 0;
			int uOffset = ench.conflicts ? 112 : 0;
			int textOffset = vOffset / 16;

			Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(guiLeft + 8, top, 96, 16,
				uOffset / 512f, (96 + uOffset) / 512f, (249 + vOffset) / 512f, (249 + 16 + vOffset) / 512f, GL11.GL_NEAREST
			);

			if (mouseX > guiLeft + 8 && mouseX <= guiLeft + 8 + 96 &&
				mouseY > top && mouseY <= top + 16) {
				disallowClick = true;
				if (ench.displayLore != null) {
					tooltipToDisplay = ench.displayLore;
				}
			}

			String levelStr = "" + ench.xpCost;
			int colour = 0xc8ff8f;
			if (ench.xpCost > playerXpLevel) {
				colour = 0xff5555;
			}

			int levelWidth = fr.getStringWidth(levelStr);
			fr.drawString(levelStr, guiLeft + 16 - levelWidth / 2 - 1, top + 4, 0x2d2102, false);
			fr.drawString(levelStr, guiLeft + 16 - levelWidth / 2 + 1, top + 4, 0x2d2102, false);
			fr.drawString(levelStr, guiLeft + 16 - levelWidth / 2, top + 4 - 1, 0x2d2102, false);
			fr.drawString(levelStr, guiLeft + 16 - levelWidth / 2, top + 4 + 1, 0x2d2102, false);
			fr.drawString(levelStr, guiLeft + 16 - levelWidth / 2, top + 4, colour, false);

			fr.drawString(ench.enchantName, guiLeft + 8 + 16 + 2 + textOffset, top + 4 + textOffset, 0xffffffdd, true);
		}
		GlScissorStack.pop(scaledResolution);

		//Removable enchants (right)
		GlScissorStack.push(0, guiTop + 18, width, guiTop + 18 + 96, scaledResolution);
		for (int i = 0; i < 7; i++) {
			int index = i + rightScroll.getValue() / 16;

			if (removable.size() <= index) break;
			Enchantment ench = removable.get(index);

			int top = guiTop - (rightScroll.getValue() % 16) + 18 + 16 * i;
			int vOffset = enchanterCurrentEnch != null && enchanterCurrentEnch.enchId.equals(ench.enchId) ? 16 : 0;
			int textOffset = vOffset / 16;

			Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(guiLeft + 248, top, 96, 16,
				0, 96 / 512f, (249 + vOffset) / 512f, (249 + 16 + vOffset) / 512f, GL11.GL_NEAREST
			);

			if (mouseX > guiLeft + 248 && mouseX <= guiLeft + 248 + 96 &&
				mouseY > top && mouseY <= top + 16) {
				disallowClick = true;
				if (ench.displayLore != null) {
					tooltipToDisplay = ench.displayLore;
				}
			}

			String levelStr = "" + ench.xpCost;
			if (ench.xpCost < 0) levelStr = "?";
			int colour = 0xc8ff8f;
			if (ench.xpCost > playerXpLevel) {
				colour = 0xff5555;
			}

			int levelWidth = fr.getStringWidth(levelStr);
			fr.drawString(levelStr, guiLeft + 256 - levelWidth / 2 - 1, top + 4, 0x2d2102, false);
			fr.drawString(
				levelStr,
				guiLeft + 256 - levelWidth / 2 + 1,
				top + 4,
				0x2d2102,
				false
			);
			fr.drawString(levelStr, guiLeft + 256 - levelWidth / 2, top + 4 - 1, 0x2d2102, false);
			fr.drawString(levelStr, guiLeft + 256 - levelWidth / 2, top + 4 + 1, 0x2d2102, false);
			fr.drawString(levelStr, guiLeft + 256 - levelWidth / 2, top + 4, colour, false);

			fr.drawString(ench.enchantName, guiLeft + 248 + 16 + 2 + textOffset, top + 4 + textOffset, 0xffffffdd, true);
		}
		GlScissorStack.pop(scaledResolution);

		//Player Inventory Items
		fr.drawString(
			Minecraft.getMinecraft().thePlayer.inventory.getDisplayName().getUnformattedText(),
			guiLeft + 102, guiTop + Y_SIZE - 96 + 2, 0x404040
		);
		int inventoryStartIndex = cc.getLowerChestInventory().getSizeInventory();
		GlStateManager.enableDepth();
		for (int i = 0; i < 36; i++) {
			int itemX = guiLeft + 102 + 18 * (i % 9);
			int itemY = guiTop + 133 + 18 * (i / 9);

			if (i >= 27) {
				itemY += 4;
			}

			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft + 102 - 8, guiTop + 191 - (inventoryStartIndex / 9 * 18 + 89), 0);
			Slot slot = cc.getSlot(inventoryStartIndex + i);
			((AccessorGuiContainer) chest).doDrawSlot(slot);
			GlStateManager.popMatrix();

			if (mouseX >= itemX && mouseX < itemX + 18 &&
				mouseY >= itemY && mouseY < itemY + 18) {
				itemHoverX = itemX;
				itemHoverY = itemY;
				hoverLocked = SlotLocking.getInstance().isSlotLocked(slot);

				if (slot.getHasStack()) {
					tooltipToDisplay = slot.getStack().getTooltip(
						Minecraft.getMinecraft().thePlayer,
						Minecraft.getMinecraft().gameSettings.advancedItemTooltips
					);
				}
			}
		}

		//Search bar
		if (currentState == EnchantState.HAS_ITEM) {
			if (searchField.getText().isEmpty() && !searchField.getFocus()) {
				searchField.setSize(90, 14);
				searchField.setPrependText("\u00a77Search...");
			} else {
				if (searchField.getFocus()) {
					int len = fr.getStringWidth(searchField.getTextDisplay()) + 10;
					searchField.setSize(Math.max(90, len), 14);
				} else {
					searchField.setSize(90, 14);
				}
				searchField.setPrependText("");
			}
			searchField.render(guiLeft + X_SIZE / 2 - searchField.getWidth() / 2, guiTop + 83);
		} else if (currentState == EnchantState.ADDING_ENCHANT &&
			enchanterCurrentEnch != null && !enchanterEnchLevels.isEmpty()) {
			int left = guiLeft + X_SIZE / 2 - 56;
			int top = guiTop + 83;

			int uOffset = enchanterCurrentEnch.conflicts ? 112 : 0;

			Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect(left, top, 112, 16,
				uOffset / 512f, (112 + uOffset) / 512f, 249 / 512f, (249 + 16) / 512f, GL11.GL_NEAREST
			);

			if (mouseX > left + 16 && mouseX <= left + 96 &&
				mouseY > top && mouseY <= top + 16) {
				disallowClick = true;
				if (enchanterCurrentEnch.displayLore != null) {
					tooltipToDisplay = enchanterCurrentEnch.displayLore;
				}
			}

			//Enchant cost
			String levelStr = "" + enchanterCurrentEnch.xpCost;
			if (enchanterCurrentEnch.xpCost < 0) levelStr = "?";

			int colour = 0xc8ff8f;
			if (enchanterCurrentEnch.xpCost > playerXpLevel) {
				colour = 0xff5555;
			}

			int levelWidth = fr.getStringWidth(levelStr);
			fr.drawString(levelStr, left + 8 - levelWidth / 2 - 1, top + 4, 0x2d2102, false);
			fr.drawString(levelStr, left + 8 - levelWidth / 2 + 1, top + 4, 0x2d2102, false);
			fr.drawString(levelStr, left + 8 - levelWidth / 2, top + 4 - 1, 0x2d2102, false);
			fr.drawString(levelStr, left + 8 - levelWidth / 2, top + 4 + 1, 0x2d2102, false);
			fr.drawString(levelStr, left + 8 - levelWidth / 2, top + 4, colour, false);

			//Enchant name
			String name = WordUtils.capitalizeFully(ItemUtils
				.fixEnchantId(enchanterCurrentEnch.enchId, false)
				.replace("_", " "));
			if (name.equalsIgnoreCase("Bane of Arthropods")) {
				name = "Bane of Arth.";
			} else if (name.equalsIgnoreCase("Projectile Protection")) {
				name = "Projectile Prot";
			} else if (name.equalsIgnoreCase("Blast Protection")) {
				name = "Blast Prot";
			} else if (name.equalsIgnoreCase("Luck of the Sea")) {
				name = "Luck of Sea";
			}
			Utils.drawStringCentered(name, guiLeft + X_SIZE / 2, top + 8, true, 0xffffffdd);

			if (isChangingEnchLevel) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(left + 96, top, 16, 16,
					96 / 512f, 112 / 512f, 265 / 512f, (265 + 16) / 512f, GL11.GL_NEAREST
				);
			}

			//Enchant level
			levelStr = "" + enchanterCurrentEnch.level;
			if (enchanterCurrentEnch.xpCost < 0) levelStr = "?";
			levelWidth = fr.getStringWidth(levelStr);
			fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2 - 1, top + 4, 0x2d2102, false);
			fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2 + 1, top + 4, 0x2d2102, false);
			fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2, top + 4 - 1, 0x2d2102, false);
			fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2, top + 4 + 1, 0x2d2102, false);
			fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2, top + 4, 0xea82ff, false);

			//Confirm button

			String confirmText = "Apply";
			if (removingEnchantPlayerLevel >= 0) {
				if (removingEnchantPlayerLevel == enchanterCurrentEnch.level) {
					confirmText = "Remove";
				} else if (enchanterCurrentEnch.level > removingEnchantPlayerLevel) {
					confirmText = "Upgrade";
				} else {
					confirmText = "Bad Level";
				}
			}
			if (System.currentTimeMillis() - confirmButtonAnimTime < 500 && !(playerXpLevel < enchanterCurrentEnch.xpCost)) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(guiLeft + X_SIZE / 2 - 1 - 48, top + 18, 48, 14,
					0, 48 / 512f, 342 / 512f, (342 + 14) / 512f, GL11.GL_NEAREST
				);
				Utils.drawStringCentered(confirmText, guiLeft + X_SIZE / 2 - 1 - 23, top + 18 + 9, false, 0x408040);
			} else {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(guiLeft + X_SIZE / 2 - 1 - 48, top + 18, 48, 14,
					0, 48 / 512f, 328 / 512f, (328 + 14) / 512f, GL11.GL_NEAREST
				);
				Utils.drawStringCentered(confirmText, guiLeft + X_SIZE / 2 - 1 - 24, top + 18 + 8, false, 0x408040);

				if (playerXpLevel < enchanterCurrentEnch.xpCost) {
					Gui.drawRect(guiLeft + X_SIZE / 2 - 1 - 48, top + 18, guiLeft + X_SIZE / 2 - 1, top + 18 + 14, 0x80000000);
				}
			}

			//Cancel button
			if (System.currentTimeMillis() - cancelButtonAnimTime < 500) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(guiLeft + X_SIZE / 2 + 1, top + 18, 48, 14,
					0, 48 / 512f, 342 / 512f, (342 + 14) / 512f, GL11.GL_NEAREST
				);
				Utils.drawStringCentered("Cancel", guiLeft + X_SIZE / 2 + 1 + 25, top + 18 + 9, false, 0xa04040);
			} else {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(guiLeft + X_SIZE / 2 + 1, top + 18, 48, 14,
					0, 48 / 512f, 328 / 512f, (328 + 14) / 512f, GL11.GL_NEAREST
				);
				Utils.drawStringCentered("Cancel", guiLeft + X_SIZE / 2 + 1 + 24, top + 18 + 8, false, 0xa04040);
			}

			if (mouseY > top + 18 && mouseY <= top + 18 + 16) {
				if (mouseX > guiLeft + X_SIZE / 2 - 1 - 48 && mouseX <= guiLeft + X_SIZE / 2 - 1) {
					disallowClick = true;
					if (enchanterCurrentEnch.displayLore != null) {
						tooltipToDisplay = enchanterCurrentEnch.displayLore;
					}
				} else if (mouseX > guiLeft + X_SIZE / 2 + 1 && mouseX <= guiLeft + X_SIZE / 2 + 1 + 48) {
					disallowClick = true;
					tooltipToDisplay = Lists.newArrayList("\u00a7cCancel");
				}
			}

			//Enchant level switcher
			if (isChangingEnchLevel) {
				tooltipToDisplay = null;

				List<Enchantment> before = new ArrayList<>();
				List<Enchantment> after = new ArrayList<>();

				for (Enchantment ench : enchanterEnchLevels.values()) {
					if (ench.level < enchanterCurrentEnch.level) {
						before.add(ench);
					} else if (ench.level > enchanterCurrentEnch.level) {
						after.add(ench);
					}
				}

				before.sort(Comparator.comparingInt(o -> -o.level));
				after.sort(Comparator.comparingInt(o -> o.level));

				int bSize = before.size();
				int aSize = after.size();
				GlStateManager.disableDepth();
				for (int i = 0; i < bSize + aSize; i++) {
					Enchantment ench;
					int yIndex;
					if (i < bSize) {
						ench = before.get(i);
						yIndex = -i - 1;
					} else {
						ench = after.get(i - bSize);
						yIndex = i - bSize + 1;
					}

					Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
					GlStateManager.color(1, 1, 1, 1);

					int type = 0;
					if (i == bSize) {
						type = 2;
					} else if (i == 0) {
						type = 1;
					}

					if (mouseX > left + 96 && mouseX <= left + 96 + 16 &&
						mouseY > top + 16 * yIndex && mouseY <= top + 16 * yIndex + 16) {
						tooltipToDisplay = new ArrayList<>(ench.displayLore);
						if (tooltipToDisplay.size() > 2) {
							tooltipToDisplay.remove(tooltipToDisplay.size() - 1);
							tooltipToDisplay.remove(tooltipToDisplay.size() - 1);
						}
						itemHoverX = -1;
						itemHoverY = -1;
					}

					Utils.drawTexturedRect(left + 96, top + 16 * yIndex, 16, 16,
						16 * type / 512f, (16 + 16 * type) / 512f, 356 / 512f, (356 + 16) / 512f, GL11.GL_NEAREST
					);

					levelStr = "" + ench.level;
					levelWidth = fr.getStringWidth(levelStr);
					fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2 - 1, top + 16 * yIndex + 4, 0x2d2102, false);
					fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2 + 1, top + 16 * yIndex + 4, 0x2d2102, false);
					fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2, top + 16 * yIndex + 4 - 1, 0x2d2102, false);
					fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2, top + 16 * yIndex + 4 + 1, 0x2d2102, false);
					fr.drawString(levelStr, left + 96 + 8 - levelWidth / 2, top + 16 * yIndex + 4, 0xea82ff, false);
				}
				GlStateManager.enableDepth();
			}

			if (mouseX > left + 96 && mouseX <= left + 96 + 16 &&
				mouseY > top && mouseY <= top + 16) {
				if (isChangingEnchLevel) {
					tooltipToDisplay = Lists.newArrayList("\u00a7cCancel level change");
				} else {
					tooltipToDisplay = Lists.newArrayList("\u00a7aChange enchant level");
				}
			}
		}

		//Item enchant input
		ItemStack itemEnchantInput = cc.getSlot(19).getStack();
		if (itemEnchantInput != null && itemEnchantInput.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) {
			itemEnchantInput = enchantingItem;
		}
		{
			int itemX = guiLeft + 174;
			int itemY = guiTop + 58;

			if (itemEnchantInput == null) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(itemX, itemY, 16, 16,
					0, 16 / 512f, 281 / 512f, (281 + 16) / 512f, GL11.GL_NEAREST
				);
			} else {
				Utils.drawItemStack(itemEnchantInput, itemX, itemY);
			}

			if (mouseX >= itemX && mouseX < itemX + 18 &&
				mouseY >= itemY && mouseY < itemY + 18) {
				itemHoverX = itemX;
				itemHoverY = itemY;

				if (itemEnchantInput != null) {
					tooltipToDisplay = itemEnchantInput.getTooltip(
						Minecraft.getMinecraft().thePlayer,
						Minecraft.getMinecraft().gameSettings.advancedItemTooltips
					);
				}
			}
		}

		if (!isChangingEnchLevel && itemHoverX >= 0 && itemHoverY >= 0) {
			GlStateManager.disableDepth();
			GlStateManager.colorMask(true, true, true, false);
			Gui.drawRect(itemHoverX, itemHoverY, itemHoverX + 16, itemHoverY + 16,
				hoverLocked ? 0x80ff8080 : 0x80ffffff
			);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableDepth();
		}

		GlStateManager.translate(0, 0, 300);

		//Orb animation
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		GlStateManager.disableDepth();
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTop, 0);
		orbDisplay.renderOrbs(partialTicks);
		GlStateManager.popMatrix();
		GlStateManager.enableDepth();

		if (stackOnMouse != null) {
			if (disallowClick) {
				Utils.drawItemStack(new ItemStack(Item.getItemFromBlock(Blocks.barrier)), mouseX - 8, mouseY - 8);
			} else {
				Utils.drawItemStack(stackOnMouse, mouseX - 8, mouseY - 8);
			}
		} else if (tooltipToDisplay != null) {
			Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1);
		}
		GlStateManager.translate(0, 0, -300);
	}

	private void renderEnchantBook(ScaledResolution scaledresolution, float partialTicks) {
		GlStateManager.enableDepth();

		GlStateManager.pushMatrix();
		GlStateManager.matrixMode(5889);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.viewport((scaledresolution.getScaledWidth() - 320) / 2 * scaledresolution.getScaleFactor(),
			(scaledresolution.getScaledHeight() - 240) / 2 * scaledresolution.getScaleFactor(),
			320 * scaledresolution.getScaleFactor(), 240 * scaledresolution.getScaleFactor()
		);
		GlStateManager.translate(0.0F, 0.33F, 0.0F);
		Project.gluPerspective(90.0F, 1.3333334F, 9.0F, 80.0F);
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(0.0F, 3.3F, -16.0F);
		GlStateManager.scale(5, 5, 5);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ENCHANTMENT_TABLE_BOOK_TEXTURE);
		GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
		float bookOpenAngle = this.bookOpenLast + (this.bookOpen - this.bookOpenLast) * partialTicks;
		GlStateManager.translate(
			(1.0F - bookOpenAngle) * 0.2F,
			(1.0F - bookOpenAngle) * 0.1F,
			(1.0F - bookOpenAngle) * 0.25F
		);
		GlStateManager.rotate(-(1.0F - bookOpenAngle) * 90.0F - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
		float pageAngle1 = this.pageOpenLast + (this.pageOpen - this.pageOpenLast) * partialTicks + 0.25F;
		float pageAngle2 = this.pageOpenLast + (this.pageOpen - this.pageOpenLast) * partialTicks + 0.75F;
		pageAngle1 = (pageAngle1 - (float) MathHelper.truncateDoubleToInt(pageAngle1)) * 1.6F - 0.3F;
		pageAngle2 = (pageAngle2 - (float) MathHelper.truncateDoubleToInt(pageAngle2)) * 1.6F - 0.3F;

		if (pageAngle1 < 0.0F) pageAngle1 = 0.0F;
		if (pageAngle1 > 1.0F) pageAngle1 = 1.0F;
		if (pageAngle2 < 0.0F) pageAngle2 = 0.0F;
		if (pageAngle2 > 1.0F) pageAngle2 = 1.0F;

		GlStateManager.enableRescaleNormal();
		MODEL_BOOK.render(null, 0.0F, pageAngle1, pageAngle2, bookOpenAngle, 0.0F, 0.0625F);
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.matrixMode(5889);
		GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5888);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.enableDepth();
	}

	public void overrideIsMouseOverSlot(Slot slot, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
		if (shouldOverrideFast && currentState != EnchantState.ADDING_ENCHANT) {
			boolean playerInv = slot.inventory == Minecraft.getMinecraft().thePlayer.inventory;
			int slotId = slot.getSlotIndex();
			if (playerInv && slotId < 36) {
				slotId -= 9;
				if (slotId < 0) slotId += 36;

				int itemX = guiLeft + 102 + 18 * (slotId % 9);
				int itemY = guiTop + 133 + 18 * (slotId / 9);

				if (slotId >= 27) {
					itemY += 4;
				}

				if (mouseX >= itemX && mouseX < itemX + 18 &&
					mouseY >= itemY && mouseY < itemY + 18) {
					cir.setReturnValue(true);
				} else {
					cir.setReturnValue(false);
				}
			} else if (slotId == 19) {
				cir.setReturnValue(mouseX >= guiLeft + 173 && mouseX < guiLeft + 173 + 18 &&
					mouseY >= guiTop + 57 && mouseY < guiTop + 57 + 18);
			}
		}
	}

	public boolean mouseInput(int mouseX, int mouseY) {
		if (Mouse.getEventButtonState() &&
			(currentState == EnchantState.HAS_ITEM || currentState == EnchantState.ADDING_ENCHANT)) {
			if (mouseY > guiTop + 6 && mouseY < guiTop + 6 + 15) {
				String pageStr = "Page: " + currentPage + "/" + expectedMaxPage;
				int pageStrLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(pageStr);

				int click = -1;
				if (mouseX > guiLeft + X_SIZE / 2 - pageStrLen / 2 - 2 - 15 &&
					mouseX <= guiLeft + X_SIZE / 2 - pageStrLen / 2 - 2) {
					click = 17;
				} else if (mouseX > guiLeft + X_SIZE / 2 + pageStrLen / 2 + 2 &&
					mouseX <= guiLeft + X_SIZE / 2 + pageStrLen / 2 + 2 + 15) {
					click = 35;
				}

				if (click >= 0) {
					if (currentState == EnchantState.ADDING_ENCHANT) {
						if (Mouse.getEventButtonState()) {
							if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
							GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

							EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
							short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
							ItemStack stack = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(45);
							Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
								chest.inventorySlots.windowId, 45, 0, 0, stack, transactionID));

							cancelButtonAnimTime = System.currentTimeMillis();
						}
					} else {
						if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
						GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

						EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
						short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
						ItemStack stack = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(click);
						Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
							chest.inventorySlots.windowId, click, 0, 0, stack, transactionID));
					}
					return true;
				}
			}
		}

		if (currentState == EnchantState.HAS_ITEM) {
			if (Mouse.getEventButtonState()) {
				if (mouseX > guiLeft + X_SIZE / 2 - searchField.getWidth() / 2 &&
					mouseX < guiLeft + X_SIZE / 2 + searchField.getWidth() / 2 &&
					mouseY > guiTop + 80 && mouseY < guiTop + 96) {
					searchField.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
				} else {
					searchField.setFocus(false);
				}
			} else if (Mouse.getEventButton() < 0 && searchField.getFocus() && Mouse.isButtonDown(0)) {
				searchField.mouseClickMove(mouseX, mouseY, 0, 0);
			}
		} else if (currentState == EnchantState.ADDING_ENCHANT && !enchanterEnchLevels.isEmpty()) {
			if (Mouse.getEventButtonState()) {
				int left = guiLeft + X_SIZE / 2 - 56;
				int top = guiTop + 83;

				Utils.drawTexturedRect(guiLeft + X_SIZE / 2 - 1 - 48, top + 18, 48, 14,
					0, 48 / 512f, 328 / 512f, (328 + 14) / 512f, GL11.GL_NEAREST
				);
				Utils.drawTexturedRect(guiLeft + X_SIZE / 2 + 1, top + 18, 48, 14,
					0, 48 / 512f, 328 / 512f, (328 + 14) / 512f, GL11.GL_NEAREST
				);

				if (!isChangingEnchLevel && mouseX > guiLeft + X_SIZE / 2 + 1 && mouseX <= guiLeft + X_SIZE / 2 + 1 + 48 &&
					mouseY > top + 18 && mouseY <= top + 18 + 14) {
					if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
					GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

					EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
					short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
					ItemStack stack = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(45);
					Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
						chest.inventorySlots.windowId, 45, 0, 0, stack, transactionID));

					cancelButtonAnimTime = System.currentTimeMillis();
				} else if (!isChangingEnchLevel && enchanterCurrentEnch != null &&
					(mouseX > left + 16 && mouseX <= left + 96 &&
						mouseY > top && mouseY <= top + 16) ||
					(mouseX > guiLeft + X_SIZE / 2 - 1 - 48 && mouseX <= guiLeft + X_SIZE / 2 - 1 &&
						mouseY > top + 18 && mouseY <= top + 18 + 14)) {
					if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
					GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

					EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
					short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
					ItemStack stack = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(
						enchanterCurrentEnch.slotIndex);
					Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
						chest.inventorySlots.windowId,
						enchanterCurrentEnch.slotIndex, 0, 0, stack, transactionID
					));

					int playerXpLevel = Minecraft.getMinecraft().thePlayer.experienceLevel;
					if (playerXpLevel >= enchanterCurrentEnch.xpCost) {
						if (removingEnchantPlayerLevel >= 0 && enchanterCurrentEnch.level == removingEnchantPlayerLevel) {
							orbDisplay.spawnExperienceOrbs(X_SIZE / 2, 66, X_SIZE / 2, 36, 3);
						} else {
							orbDisplay.spawnExperienceOrbs(mouseX - guiLeft, mouseY - guiTop, X_SIZE / 2, 66, 0);
						}
					}

					confirmButtonAnimTime = System.currentTimeMillis();
				} else if (mouseX > left + 96 && mouseX <= left + 96 + 16) {
					if (!isChangingEnchLevel) {
						if (mouseY > top && mouseY < top + 16) {
							isChangingEnchLevel = true;
							return true;
						}
					} else {
						List<Enchantment> before = new ArrayList<>();
						List<Enchantment> after = new ArrayList<>();

						for (Enchantment ench : enchanterEnchLevels.values()) {
							if (ench.level < enchanterCurrentEnch.level) {
								before.add(ench);
							} else if (ench.level > enchanterCurrentEnch.level) {
								after.add(ench);
							}
						}

						before.sort(Comparator.comparingInt(o -> -o.level));
						after.sort(Comparator.comparingInt(o -> o.level));

						int bSize = before.size();
						int aSize = after.size();
						for (int i = 0; i < bSize + aSize; i++) {
							Enchantment ench;
							int yIndex;
							if (i < bSize) {
								yIndex = -i - 1;
								ench = before.get(i);
							} else {
								yIndex = i - bSize + 1;
								ench = after.get(i - bSize);
							}

							if (mouseY > top + 16 * yIndex && mouseY <= top + 16 * yIndex + 16) {
								enchanterCurrentEnch = ench;
								isChangingEnchLevel = false;
								return true;
							}
						}
					}
				}

				if (isChangingEnchLevel) {
					isChangingEnchLevel = false;
					return true;
				}
			}
		}

		if (!Mouse.getEventButtonState() && Mouse.getEventButton() < 0 && clickedScrollOffset != -1) {
			LerpingInteger lerpingInteger = isClickedScrollLeft ? leftScroll : rightScroll;
			List<Enchantment> enchantsList = isClickedScrollLeft ? applicable : removable;

			if (enchantsList.size() > 6) {
				int newOffset = mouseY - clickedScrollOffset;

				int newScroll = Math.round(newOffset * (float) ((enchantsList.size() - 6) * 16) / (96 - 15));
				int max = (enchantsList.size() - 6) * 16;

				if (newScroll > max) newScroll = max;
				if (newScroll < 0) newScroll = 0;

				lerpingInteger.setValue(newScroll);
			}
		}

		//Config options
		if (Mouse.getEventButtonState()) {
			if (mouseX >= guiLeft + 294 && mouseX < guiLeft + 294 + 36 &&
				mouseY >= guiTop + 146 && mouseY < guiTop + 146 + 36) {
				int index = (mouseX - (guiLeft + 295)) / 18 + (mouseY - (guiTop + 147)) / 18 * 2;

				int direction = Mouse.getEventButton() == 0 ? 1 : -1;

				switch (index) {
					case 0: {
						NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enableTableGUI = false;
						break;
					}
                   /*case 1: {
                        int val = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.incompatibleEnchants;
                        val += direction;
                        if (val < 0) val = 1;
                        if (val > 1) val = 0;
                        NotEnoughUpdates.INSTANCE.config.enchantingSolvers.incompatibleEnchants = val;
                        break;
                    }*/
					case 2: {
						int val = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enchantSorting;
						val += direction;
						if (val < 0) val = 1;
						if (val > 1) val = 0;
						NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enchantSorting = val;
						break;
					}
					case 3: {
						int val = NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enchantOrdering;
						val += direction;
						if (val < 0) val = 1;
						if (val > 1) val = 0;
						NotEnoughUpdates.INSTANCE.config.enchantingSolvers.enchantOrdering = val;
						break;
					}
				}
			}
		}

		if (Mouse.getEventButton() == 0) {
			if (Mouse.getEventButtonState()) {
				if (mouseX > guiLeft + 104 && mouseX < guiLeft + 104 + 12) {
					int offset;
					if (applicable.size() <= 6) {
						offset = 0;
					} else {
						offset = Math.round((96 - 15) * (leftScroll.getValue() / (float) ((applicable.size() - 6) * 16)));
					}
					if (mouseY >= guiTop + 18 + offset && mouseY < guiTop + 18 + offset + 15) {
						isClickedScrollLeft = true;
						clickedScrollOffset = mouseY - offset;
					}
				} else if (mouseX > guiLeft + 344 && mouseX < guiLeft + 344 + 12) {
					int offset;
					if (removable.size() <= 6) {
						offset = 0;
					} else {
						offset = Math.round((96 - 15) * (rightScroll.getValue() / (float) ((removable.size() - 6) * 16)));
					}
					if (mouseY >= guiTop + 18 + offset && mouseY < guiTop + 18 + offset + 15) {
						isClickedScrollLeft = false;
						clickedScrollOffset = mouseY - offset;
					}
				}
			} else {
				clickedScrollOffset = -1;
			}
		}

		if (mouseY > guiTop + 18 && mouseY < guiTop + 18 + 96) {
			if (mouseX > guiLeft + 8 && mouseX < guiLeft + 8 + 96) {
				if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() &&
					Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
					for (int i = 0; i < 7; i++) {
						int index = i + leftScroll.getValue() / 16;
						if (applicable.size() <= index) break;

						int top = guiTop - (leftScroll.getValue() % 16) + 18 + 16 * i;
						if (mouseX > guiLeft + 8 && mouseX <= guiLeft + 8 + 96 &&
							mouseY > top && mouseY <= top + 16) {
							Enchantment ench = applicable.get(index);

							if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
							GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

							if (currentState == EnchantState.HAS_ITEM) {
								EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
								short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
								ItemStack stack =
									((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(ench.slotIndex);
								Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
									chest.inventorySlots.windowId,
									ench.slotIndex, 0, 0, stack, transactionID
								));
							} else if (currentState == EnchantState.ADDING_ENCHANT) {
								EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
								short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
								ItemStack stack = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(45);
								Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
									chest.inventorySlots.windowId, 45, 0, 0, stack, transactionID));

								cancelButtonAnimTime = System.currentTimeMillis();
							}

							return true;
						}
					}
				}

				isScrollingLeft = true;
			} else if (mouseX > guiLeft + 248 && mouseX < guiLeft + 248 + 96) {
				if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() &&
					Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
					for (int i = 0; i < 7; i++) {
						int index = i + rightScroll.getValue() / 16;
						if (removable.size() <= index) break;

						int top = guiTop - (rightScroll.getValue() % 16) + 18 + 16 * i;
						if (mouseX > guiLeft + 248 && mouseX <= guiLeft + 248 + 96 &&
							mouseY > top && mouseY <= top + 16) {
							Enchantment ench = removable.get(index);

							if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
							GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

							if (currentState == EnchantState.HAS_ITEM) {
								EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
								short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
								ItemStack stack =
									((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(ench.slotIndex);
								Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
									chest.inventorySlots.windowId,
									ench.slotIndex, 0, 0, stack, transactionID
								));
							} else if (currentState == EnchantState.ADDING_ENCHANT) {
								EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
								short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
								ItemStack stack = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(45);
								Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
									chest.inventorySlots.windowId, 45, 0, 0, stack, transactionID));

								cancelButtonAnimTime = System.currentTimeMillis();
							}

							return true;
						}
					}
				}
				isScrollingLeft = false;
			}
		}
		if (Mouse.getEventDWheel() != 0) {
			int scroll = Mouse.getEventDWheel();
			if (scroll > 0) {
				scroll = -16;
			} else {
				scroll = 16;
			}

			LerpingInteger lerpingInteger = isScrollingLeft ? leftScroll : rightScroll;

			int elementsCount = isScrollingLeft ? applicable.size() : removable.size();
			int max = (elementsCount - 6) * 16;

			int newTarget = lerpingInteger.getTarget() + scroll;
			if (newTarget > max) newTarget = max;
			if (newTarget < 0) newTarget = 0;

			if (newTarget != lerpingInteger.getTarget()) {
				lerpingInteger.resetTimer();
				lerpingInteger.setTarget(newTarget);
			}
		}

		if (mouseX > guiLeft + 102 && mouseX < guiLeft + 102 + 160) {
			if ((mouseY > guiTop + 133 && mouseY < guiTop + 133 + 54) ||
				(mouseY > guiTop + 133 + 54 + 4 && mouseY < guiTop + 133 + 54 + 4 + 18)) {
				if (currentState == EnchantState.ADDING_ENCHANT) {
					if (Mouse.getEventButtonState()) {
						if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
						GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

						EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
						short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
						ItemStack stack = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(45);
						Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
							chest.inventorySlots.windowId, 45, 0, 0, stack, transactionID));

						cancelButtonAnimTime = System.currentTimeMillis();
					}
					return true;
				} else {
					return false;
				}
			}
		}
		if (mouseX >= guiLeft + 173 && mouseX < guiLeft + 173 + 18 &&
			mouseY >= guiTop + 57 && mouseY < guiTop + 57 + 18) {
			if (currentState == EnchantState.ADDING_ENCHANT) {
				if (Mouse.getEventButtonState()) {
					if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) return true;
					GuiContainer chest = ((GuiContainer) Minecraft.getMinecraft().currentScreen);

					EntityPlayerSP playerIn = Minecraft.getMinecraft().thePlayer;
					short transactionID = playerIn.openContainer.getNextTransactionID(playerIn.inventory);
					ItemStack stack = ((ContainerChest) chest.inventorySlots).getLowerChestInventory().getStackInSlot(45);
					Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0EPacketClickWindow(
						chest.inventorySlots.windowId, 45, 0, 0, stack, transactionID));

					cancelButtonAnimTime = System.currentTimeMillis();
				}
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	public boolean keyboardInput() {
		if (currentState == EnchantState.HAS_ITEM && searchField.getFocus()) {
			if (Keyboard.getEventKeyState()) {
				searchField.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
			}
			return true;
		}
		if (Keyboard.getEventKey() == Minecraft.getMinecraft().gameSettings.keyBindScreenshot.getKeyCode()) {
			return false;
		}

		return Keyboard.getEventKey() != Keyboard.KEY_ESCAPE &&
			Keyboard.getEventKey() != Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode() &&
			(!NotEnoughUpdates.INSTANCE.config.slotLocking.enableSlotLocking ||
				Keyboard.getEventKey() != NotEnoughUpdates.INSTANCE.config.slotLocking.slotLockKey);
	}
}
