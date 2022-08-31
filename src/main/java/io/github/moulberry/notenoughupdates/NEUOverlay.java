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

package io.github.moulberry.notenoughupdates;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.auction.CustomAHGui;
import io.github.moulberry.notenoughupdates.core.BackgroundBlur;
import io.github.moulberry.notenoughupdates.core.GuiScreenElementWrapper;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingInteger;
import io.github.moulberry.notenoughupdates.infopanes.DevInfoPane;
import io.github.moulberry.notenoughupdates.infopanes.InfoPane;
import io.github.moulberry.notenoughupdates.infopanes.TextInfoPane;
import io.github.moulberry.notenoughupdates.itemeditor.NEUItemEditor;
import io.github.moulberry.notenoughupdates.mbgui.MBAnchorPoint;
import io.github.moulberry.notenoughupdates.mbgui.MBGuiElement;
import io.github.moulberry.notenoughupdates.mbgui.MBGuiGroupAligned;
import io.github.moulberry.notenoughupdates.mbgui.MBGuiGroupFloating;
import io.github.moulberry.notenoughupdates.miscfeatures.EnchantingSolvers;
import io.github.moulberry.notenoughupdates.miscfeatures.PetInfoOverlay;
import io.github.moulberry.notenoughupdates.miscfeatures.SunTzu;
import io.github.moulberry.notenoughupdates.miscgui.GuiPriceGraph;
import io.github.moulberry.notenoughupdates.miscgui.NeuSearchCalculator;
import io.github.moulberry.notenoughupdates.options.NEUConfigEditor;
import io.github.moulberry.notenoughupdates.util.Constants;
import io.github.moulberry.notenoughupdates.util.GuiTextures;
import io.github.moulberry.notenoughupdates.util.LerpingFloat;
import io.github.moulberry.notenoughupdates.util.NotificationHandler;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class NEUOverlay extends Gui {
	private static final ResourceLocation SUPERGEHEIMNISVERMOGEN = new ResourceLocation(
		"notenoughupdates:supersecretassets/bald.png");

	private static final ResourceLocation ATMOULBERRYWHYISMYLUNARCLIENTBUGGING = new ResourceLocation(
		"notenoughupdates:supersecretassets/lunar.png");
	private static final ResourceLocation SEARCH_BAR = new ResourceLocation("notenoughupdates:search_bar.png");
	private static final ResourceLocation SEARCH_BAR_GOLD = new ResourceLocation("notenoughupdates:search_bar_gold.png");

	private static final ResourceLocation ARMOR_DISPLAY = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay.png");
	private static final ResourceLocation ARMOR_DISPLAY_GREY = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_grey.png");
	private static final ResourceLocation ARMOR_DISPLAY_DARK = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_phq_dark.png");
	private static final ResourceLocation ARMOR_DISPLAY_FSR = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_fsr.png");
	private static final ResourceLocation ARMOR_DISPLAY_TRANSPARENT = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_transparent.png");
	private static final ResourceLocation ARMOR_DISPLAY_TRANSPARENT_PET = new ResourceLocation(
		"notenoughupdates:armordisplay/armordisplay_transparent_pet.png");

	private static final ResourceLocation QUESTION_MARK = new ResourceLocation("notenoughupdates:pv_unknown.png");

	private static final ResourceLocation PET_DISPLAY = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo.png");
	private static final ResourceLocation PET_DISPLAY_GREY = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo_dark.png");
	private static final ResourceLocation PET_DISPLAY_DARK = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo_phqdark.png");
	private static final ResourceLocation PET_DISPLAY_FSR = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo_fsr.png");
	private static final ResourceLocation PET_DISPLAY_TRANSPARENT = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplaysolo_transparent.png");

	private static final ResourceLocation PET_ARMOR_DISPLAY = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor.png");
	private static final ResourceLocation PET_ARMOR_DISPLAY_GREY = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor_dark.png");
	private static final ResourceLocation PET_ARMOR_DISPLAY_DARK = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor_phqdark.png");
	private static final ResourceLocation PET_ARMOR_DISPLAY_FSR = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor_fsr.png");
	private static final ResourceLocation PET_ARMOR_DISPLAY_TRANSPARENT = new ResourceLocation(
		"notenoughupdates:petdisplay/petdisplayarmor_transparent.png");

	private static boolean renderingArmorHud;
	private static boolean renderingPetHud;
	public static boolean shouldUseCachedPet;
	public static long cachedPetTimer;

	private final NEUManager manager;

	private final String mobRegex = ".*?((_MONSTER)|(_NPC)|(_ANIMAL)|(_MINIBOSS)|(_BOSS)|(_SC))$";
	private final String petRegex = ".*?;[0-5]$";

	private final ResourceLocation[] sortIcons = new ResourceLocation[]{
		GuiTextures.sort_all,
		GuiTextures.sort_mob,
		GuiTextures.sort_pet,
		GuiTextures.sort_tool,
		GuiTextures.sort_armor,
		GuiTextures.sort_accessory
	};
	private final ResourceLocation[] sortIconsActive = new ResourceLocation[]{
		GuiTextures.sort_all_active,
		GuiTextures.sort_mob_active,
		GuiTextures.sort_pet_active,
		GuiTextures.sort_tool_active,
		GuiTextures.sort_armor_active,
		GuiTextures.sort_accessory_active
	};

	private final ResourceLocation[] orderIcons = new ResourceLocation[]{
		GuiTextures.order_alphabetical, GuiTextures.order_rarity, GuiTextures.order_value
	};
	private final ResourceLocation[] orderIconsActive = new ResourceLocation[]{
		GuiTextures.order_alphabetical_active, GuiTextures.order_rarity_active, GuiTextures.order_value_active
	};

	//Various constants used for GUI structure
	private final int searchBarYOffset = 10;
	private final int searchBarPadding = 2;
	private long lastSearchMode = 0;

	private float oldWidthMult = 0;

	public static final int ITEM_PADDING = 4;
	public static final int ITEM_SIZE = 16;

	private Color bg = new Color(90, 90, 140, 50);
	private Color fg = new Color(100, 100, 100, 255);

	private InfoPane activeInfoPane = null;

	private TreeSet<JsonObject> searchedItems = null;
	private final List<JsonObject> searchedItemsArr = new ArrayList<>();

	private HashMap<String, List<String>> searchedItemsSubgroup = new HashMap<>();

	private long selectedItemMillis = 0;
	private int selectedItemGroupX = -1;
	private int selectedItemGroupY = -1;
	private List<JsonObject> selectedItemGroup = null;

	private boolean itemPaneOpen = false;

	private int page = 0;

	private final LerpingFloat itemPaneOffsetFactor = new LerpingFloat(1);
	private final LerpingInteger itemPaneTabOffset = new LerpingInteger(20, 50);
	private final LerpingFloat infoPaneOffsetFactor = new LerpingFloat(0);

	public boolean searchMode = false;
	private long millisLastLeftClick = 0;
	private long millisLastMouseMove = 0;
	private int lastMouseX = 0;
	private int lastMouseY = 0;

	public static final int overlayColourDark = new Color(0, 0, 0, 120).getRGB();
	public static final int overlayColourLight = new Color(255, 255, 255, 120).getRGB();

	boolean mouseDown = false;

	private boolean redrawItems = false;

	public static boolean searchBarHasFocus = false;
	private static final GuiTextField textField = new GuiTextField(0, null, 0, 0, 0, 0);

	private static final int COMPARE_MODE_ALPHABETICAL = 0;
	private static final int COMPARE_MODE_RARITY = 1;
	private static final int COMPARE_MODE_VALUE = 2;

	private static final int SORT_MODE_ALL = 0;
	private static final int SORT_MODE_MOB = 1;
	private static final int SORT_MODE_PET = 2;
	private static final int SORT_MODE_TOOL = 3;
	private static final int SORT_MODE_ARMOR = 4;
	private static final int SORT_MODE_ACCESSORY = 5;

	private boolean disabled = false;

	private int lastScreenWidth;
	private int lastScreenHeight;
	private int lastScale;

	private CompletableFuture<Void> infoPaneLoadingJob = CompletableFuture.completedFuture(null);

	private List<String> textToDisplay = null;

	public MBGuiGroupFloating guiGroup = null;

	public NEUOverlay(NEUManager manager) {
		this.manager = manager;
		textField.setFocused(true);
		textField.setCanLoseFocus(false);

		guiGroup = createGuiGroup();
	}

	private MBGuiElement createSearchBar() {
		return new MBGuiElement() {
			public int getWidth() {
				int paddingUnscaled = getPaddingUnscaled();

				return getSearchBarXSize() + 2 * paddingUnscaled;
			}

			public int getHeight() {
				int paddingUnscaled = getPaddingUnscaled();

				return getSearchBarYSize() + 2 * paddingUnscaled;
			}

			@Override
			public void mouseClick(float x, float y, int mouseX, int mouseY) {
				if (!NotEnoughUpdates.INSTANCE.config.toolbar.searchBar) {
					return;
				}
				if (Mouse.getEventButtonState()) {
					setSearchBarFocus(true);
					if (Mouse.getEventButton() == 1) { //Right mouse button down
						textField.setText("");
						updateSearch();
					} else {
						if (System.currentTimeMillis() - millisLastLeftClick < 300) {
							searchMode = !searchMode;
							lastSearchMode = System.currentTimeMillis();
							if (searchMode && NotEnoughUpdates.INSTANCE.config.hidden.firstTimeSearchFocus) {
								NotificationHandler.displayNotification(Lists.newArrayList(
									"\u00a7eSearch Highlight",
									"\u00a77In this mode NEU will gray out non matching items in",
									"\u00a77your inventory or chests.",
									"\u00a77This allows you easily find items as the item will stand out.",
									"\u00a77To toggle this please double click on the search bar in your inventory.",
									"\u00a77",
									"\u00a77Press X on your keyboard to close this notification"
								), true, true);
								NotEnoughUpdates.INSTANCE.config.hidden.firstTimeSearchFocus = false;

							}
						}
						textField.setCursorPosition(getClickedIndex(mouseX, mouseY));
						millisLastLeftClick = System.currentTimeMillis();
						if (searchMode) {
							lastSearchMode = System.currentTimeMillis();
						}
					}
				}
			}

			@Override
			public void mouseClickOutside() {
				setSearchBarFocus(false);
			}

			@Override
			public void render(float x, float y) {
				if (!NotEnoughUpdates.INSTANCE.config.toolbar.searchBar) {
					return;
				}
				FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
				int paddingUnscaled = getPaddingUnscaled();

				GlStateManager.color(1, 1, 1, 1);

				Minecraft.getMinecraft().getTextureManager().bindTexture(searchMode ? SEARCH_BAR_GOLD : SEARCH_BAR);

				int w = getWidth();
				int h = getHeight();

				for (int yIndex = 0; yIndex <= 2; yIndex++) {
					for (int xIndex = 0; xIndex <= 2; xIndex++) {
						float uMin = 0;
						float uMax = 4 / 20f;
						int partX = (int) x;
						int partW = 4;
						if (xIndex == 1) {
							partX += 4;
							uMin = 4 / 20f;
							uMax = 16 / 20f;
							partW = w - 8;
						} else if (xIndex == 2) {
							partX += w - 4;
							uMin = 16 / 20f;
							uMax = 20 / 20f;
						}

						float vMin = 0;
						float vMax = 4 / 20f;
						int partY = (int) y;
						int partH = 4;
						if (yIndex == 1) {
							partY += 4;
							vMin = 4 / 20f;
							vMax = 16 / 20f;
							partH = h - 8;
						} else if (yIndex == 2) {
							partY += h - 4;
							vMin = 16 / 20f;
							vMax = 20 / 20f;
						}

						Utils.drawTexturedRect(partX, partY, partW, partH, uMin, uMax, vMin, vMax, GL11.GL_NEAREST);
					}
				}

				//Search bar text
				fr.drawString(NeuSearchCalculator.format(textField.getText()), (int) x + 5,
					(int) y - 4 + getHeight() / 2, Color.WHITE.getRGB()
				);

				//Determines position of cursor. Cursor blinks on and off every 500ms.
				if (searchBarHasFocus && System.currentTimeMillis() % 1000 > 500) {
					String textBeforeCursor = textField.getText().substring(0, textField.getCursorPosition());
					int textBeforeCursorWidth = fr.getStringWidth(textBeforeCursor);
					drawRect((int) x + 5 + textBeforeCursorWidth,
						(int) y - 5 + getHeight() / 2,
						(int) x + 5 + textBeforeCursorWidth + 1,
						(int) y - 4 + 9 + getHeight() / 2, Color.WHITE.getRGB()
					);
				}

				String selectedText = textField.getSelectedText();
				if (!selectedText.isEmpty()) {
					int selectionWidth = fr.getStringWidth(selectedText);

					int leftIndex = Math.min(textField.getCursorPosition(), textField.getSelectionEnd());
					String textBeforeSelection = textField.getText().substring(0, leftIndex);
					int textBeforeSelectionWidth = fr.getStringWidth(textBeforeSelection);

					drawRect((int) x + 5 + textBeforeSelectionWidth,
						(int) y - 5 + getHeight() / 2,
						(int) x + 5 + textBeforeSelectionWidth + selectionWidth,
						(int) y - 4 + 9 + getHeight() / 2, Color.LIGHT_GRAY.getRGB()
					);

					fr.drawString(selectedText,
						(int) x + 5 + textBeforeSelectionWidth,
						(int) y - 4 + getHeight() / 2, Color.BLACK.getRGB()
					);
				}

			}

			@Override
			public void recalculate() {}
		};
	}

	private MBGuiElement createSettingsButton(NEUOverlay overlay) {
		return new MBGuiElement() {
			@Override
			public int getWidth() {
				return getSearchBarYSize() + getPaddingUnscaled() * 2;
			}

			@Override
			public int getHeight() {
				return getWidth();
			}

			@Override
			public void recalculate() {}

			@Override
			public void mouseClick(float x, float y, int mouseX, int mouseY) {
				if (!NotEnoughUpdates.INSTANCE.config.toolbar.enableSettingsButton) {
					return;
				}
				if (Mouse.getEventButtonState()) {
					NotEnoughUpdates.INSTANCE.openGui = new GuiScreenElementWrapper(NEUConfigEditor.editor);
				}
			}

			@Override
			public void mouseClickOutside() {}

			@Override
			public void render(float x, float y) {
				int paddingUnscaled = getPaddingUnscaled();
				int searchYSize = getSearchBarYSize();

				if (!NotEnoughUpdates.INSTANCE.config.toolbar.enableSettingsButton) {
					return;
				}
				Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.quickcommand_background);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(x, y,
					searchYSize + paddingUnscaled * 2, searchYSize + paddingUnscaled * 2, GL11.GL_NEAREST
				);

				Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.settings);
				GlStateManager.color(1f, 1f, 1f, 1f);
				Utils.drawTexturedRect((int) x + paddingUnscaled, (int) y + paddingUnscaled,
					searchYSize, searchYSize
				);

				GlStateManager.bindTexture(0);
			}
		};
	}

	private MBGuiElement createHelpButton(NEUOverlay overlay) {
		return new MBGuiElement() {
			@Override
			public int getWidth() {
				return getSearchBarYSize() + getPaddingUnscaled() * 2;
			}

			@Override
			public int getHeight() {
				return getWidth();
			}

			@Override
			public void recalculate() {}

			@Override
			public void mouseClick(float x, float y, int mouseX, int mouseY) {
				if (!NotEnoughUpdates.INSTANCE.config.toolbar.enableHelpButton) {
					return;
				}
				if (Mouse.getEventButtonState()) {
					//displayInformationPane(HTMLInfoPane.createFromWikiUrl(overlay, manager, "Help",
					//        "https://moulberry.github.io/files/neu_help.html"));
					//Minecraft.getMinecraft().displayGuiScreen(new HelpGUI());
					ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/neuhelp");
					Utils.playPressSound();
				}
			}

			@Override
			public void mouseClickOutside() {}

			@Override
			public void render(float x, float y) {
				int paddingUnscaled = getPaddingUnscaled();
				int searchYSize = getSearchBarYSize();

				if (!NotEnoughUpdates.INSTANCE.config.toolbar.enableHelpButton) {
					return;
				}

				Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.quickcommand_background);
				GlStateManager.color(1, 1, 1, 1);
				Utils.drawTexturedRect(x, y,
					searchYSize + paddingUnscaled * 2, searchYSize + paddingUnscaled * 2, GL11.GL_NEAREST
				);

				Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.help);
				GlStateManager.color(1f, 1f, 1f, 1f);
				Utils.drawTexturedRect((int) x + paddingUnscaled, (int) y + paddingUnscaled,
					getSearchBarYSize(), getSearchBarYSize()
				);
				GlStateManager.bindTexture(0);

			}
		};
	}

	private MBGuiElement createQuickCommand(String quickCommandStr) {
		return new MBGuiElement() {
			@Override
			public int getWidth() {
				return getSearchBarYSize() + getPaddingUnscaled() * 2;
			}

			@Override
			public int getHeight() {
				return getWidth();
			}

			@Override
			public void recalculate() {}

			@Override
			public void mouseClick(float x, float y, int mouseX, int mouseY) {
				if (!NotEnoughUpdates.INSTANCE.config.toolbar.quickCommands) return;
				if (EnchantingSolvers.disableButtons()) return;

				if ((NotEnoughUpdates.INSTANCE.config.toolbar.quickCommandsClickType != 0 && Mouse.getEventButtonState()) ||
					(NotEnoughUpdates.INSTANCE.config.toolbar.quickCommandsClickType == 0 && !Mouse.getEventButtonState() &&
						Mouse.getEventButton() != -1)) {
					if (quickCommandStr.contains(":")) {
						String command = quickCommandStr.split(":")[0].trim();
						if (command.startsWith("/")) {
							NotEnoughUpdates.INSTANCE.sendChatMessage(command);
						} else {
							ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/" + command);
						}
						Utils.playPressSound();
					}
				}
			}

			@Override
			public void mouseClickOutside() {}

			@Override
			public void render(float x, float y) {
				if (!NotEnoughUpdates.INSTANCE.config.toolbar.quickCommands) return;
				if (EnchantingSolvers.disableButtons()) return;

				int paddingUnscaled = getPaddingUnscaled();
				int bigItemSize = getSearchBarYSize();

				String[] quickCommandStrSplit = quickCommandStr.split(":");
				if (quickCommandStrSplit.length != 3) {
					return;
				}
				String display = quickCommandStrSplit[2];
				ItemStack render = null;
				float extraScale = 1;
				if (display.length() > 20) { //Custom head
					render = new ItemStack(Items.skull, 1, 3);
					NBTTagCompound nbt = new NBTTagCompound();
					NBTTagCompound skullOwner = new NBTTagCompound();
					NBTTagCompound properties = new NBTTagCompound();
					NBTTagList textures = new NBTTagList();
					NBTTagCompound textures_0 = new NBTTagCompound();

					String uuid = UUID.nameUUIDFromBytes(display.getBytes()).toString();
					skullOwner.setString("Id", uuid);
					skullOwner.setString("Name", uuid);

					textures_0.setString("Value", display);
					textures.appendTag(textures_0);

					properties.setTag("textures", textures);
					skullOwner.setTag("Properties", properties);
					nbt.setTag("SkullOwner", skullOwner);
					render.setTagCompound(nbt);

					extraScale = 1.3f;
				} else if (manager.getItemInformation().containsKey(display)) {
					render = manager.jsonToStack(manager.getItemInformation().get(display), true, true);
				} else {
					Item item = Item.itemRegistry.getObject(new ResourceLocation(display.toLowerCase()));
					if (item != null) {
						render = new ItemStack(item);
					}
				}
				if (render != null) {
					NBTTagCompound tag = render.getTagCompound() != null ? render.getTagCompound() : new NBTTagCompound();
					tag.setString("qc_id", quickCommandStrSplit[0].toLowerCase().trim());
					render.setTagCompound(tag);

					Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.quickcommand_background);
					GlStateManager.color(1, 1, 1, 1);
					Utils.drawTexturedRect(x, y,
						bigItemSize + paddingUnscaled * 2, bigItemSize + paddingUnscaled * 2, GL11.GL_NEAREST
					);

					int mouseX = Mouse.getX() * Utils.peekGuiScale().getScaledWidth() / Minecraft.getMinecraft().displayWidth;
					int mouseY = Utils.peekGuiScale().getScaledHeight() -
						Mouse.getY() * Utils.peekGuiScale().getScaledHeight() / Minecraft.getMinecraft().displayHeight - 1;

					if (mouseX > x && mouseX < x + bigItemSize) {
						if (mouseY > y && mouseY < y + bigItemSize) {
							textToDisplay = new ArrayList<>();
							textToDisplay.add(EnumChatFormatting.GRAY + quickCommandStrSplit[1]);
						}
					}

					GlStateManager.enableDepth();
					float itemScale = bigItemSize / (float) ITEM_SIZE * extraScale;
					GlStateManager.pushMatrix();
					GlStateManager.scale(itemScale, itemScale, 1);
					GlStateManager.translate((x - (extraScale - 1) * bigItemSize / 2 + paddingUnscaled) / itemScale,
						(y - (extraScale - 1) * bigItemSize / 2 + paddingUnscaled) / itemScale, 0f
					);
					Utils.drawItemStack(render, 0, 0);
					GlStateManager.popMatrix();
				}
			}
		};
	}

	private MBGuiGroupAligned createQuickCommandGroup() {
		List<MBGuiElement> children = new ArrayList<>();
		for (String quickCommand : NotEnoughUpdates.INSTANCE.config.hidden.quickCommands) {
			children.add(createQuickCommand(quickCommand));
		}
		return new MBGuiGroupAligned(children, false) {
			public int getPadding() {
				return getPaddingUnscaled() * 4;
			}
		};
	}

	private MBGuiGroupAligned createSearchBarGroup() {
		List<MBGuiElement> children = Lists.newArrayList(
			createSettingsButton(this),
			createSearchBar(),
			createHelpButton(this)
		);
		return new MBGuiGroupAligned(children, false) {
			public int getPadding() {
				return getPaddingUnscaled() * 4;
			}
		};
	}

	private MBGuiGroupFloating createGuiGroup() {
		LinkedHashMap<MBGuiElement, MBAnchorPoint> map = new LinkedHashMap<>();

		MBAnchorPoint searchBarAnchor =
			MBAnchorPoint.createFromString(NotEnoughUpdates.INSTANCE.config.hidden.overlaySearchBar);
		MBAnchorPoint quickCommandAnchor =
			MBAnchorPoint.createFromString(NotEnoughUpdates.INSTANCE.config.hidden.overlayQuickCommand);

		searchBarAnchor = searchBarAnchor != null ? searchBarAnchor :
			new MBAnchorPoint(MBAnchorPoint.AnchorPoint.BOTMID, new Vector2f(0, -searchBarYOffset));
		quickCommandAnchor = quickCommandAnchor != null ? quickCommandAnchor :
			new MBAnchorPoint(MBAnchorPoint.AnchorPoint.BOTMID, new Vector2f(
				0,
				-searchBarYOffset - getSearchBarYSize() - getPaddingUnscaled() * 4
			));

		map.put(createSearchBarGroup(), searchBarAnchor);
		map.put(createQuickCommandGroup(), quickCommandAnchor);

		return new MBGuiGroupFloating(Utils.peekGuiScale().getScaledWidth(), Utils.peekGuiScale().getScaledHeight(), map);
	}

	public void resetAnchors(boolean onlyIfNull) {
		MBAnchorPoint searchBarAnchor =
			MBAnchorPoint.createFromString(NotEnoughUpdates.INSTANCE.config.hidden.overlaySearchBar);
		MBAnchorPoint quickCommandAnchor =
			MBAnchorPoint.createFromString(NotEnoughUpdates.INSTANCE.config.hidden.overlayQuickCommand);

		if (onlyIfNull) {
			searchBarAnchor = searchBarAnchor != null ? null :
				new MBAnchorPoint(MBAnchorPoint.AnchorPoint.BOTMID, new Vector2f(0, -searchBarYOffset));
			quickCommandAnchor = quickCommandAnchor != null ? null :
				new MBAnchorPoint(MBAnchorPoint.AnchorPoint.BOTMID, new Vector2f(
					0,
					-searchBarYOffset - getSearchBarYSize() - getPaddingUnscaled() * 4
				));
		} else {
			searchBarAnchor = searchBarAnchor != null ? searchBarAnchor :
				new MBAnchorPoint(MBAnchorPoint.AnchorPoint.BOTMID, new Vector2f(0, -searchBarYOffset));
			quickCommandAnchor = quickCommandAnchor != null ? quickCommandAnchor :
				new MBAnchorPoint(MBAnchorPoint.AnchorPoint.BOTMID, new Vector2f(
					0,
					-searchBarYOffset - getSearchBarYSize() - getPaddingUnscaled() * 4
				));
		}

		int index = 0;
		Set<MBGuiElement> set = new LinkedHashSet<>(guiGroup.getChildrenMap().keySet());
		for (MBGuiElement element : set) {
			switch (index) {
				case 0:
					if (searchBarAnchor == null) continue;
					guiGroup.getChildrenMap().get(element).anchorPoint = searchBarAnchor.anchorPoint;
					guiGroup.getChildrenMap().get(element).offset = searchBarAnchor.offset;
					break;
				case 1:
					if (quickCommandAnchor == null) continue;
					guiGroup.getChildrenMap().get(element).anchorPoint = quickCommandAnchor.anchorPoint;
					guiGroup.getChildrenMap().get(element).offset = quickCommandAnchor.offset;
					break;
			}
			index++;
		}
	}

	/**
	 * Disables searchBarFocus and resets the item pane position. Called whenever NEUOverlay is opened.
	 */
	public void reset() {
		searchBarHasFocus = false;
		if (!(searchMode || (NotEnoughUpdates.INSTANCE.config.itemlist.keepopen && itemPaneOpen))) {
			itemPaneOpen = false;
			displayInformationPane(null);
			itemPaneOffsetFactor.setValue(1);
			itemPaneTabOffset.setValue(20);
		}
		if (activeInfoPane != null) activeInfoPane.reset();
		guiGroup.recalculate();
	}

	/**
	 * Calls #displayInformationPane with a HTMLInfoPane created from item.info and item.infoType.
	 */
	public void showInfo(JsonObject item) {
		if (item.has("info") && item.has("infoType")) {
			JsonArray lore = item.get("info").getAsJsonArray();
			String infoType = item.get("infoType").getAsString();
			String infoText = "";
			if (infoType.equals("WIKI_URL")) {
				for (JsonElement url : lore) {
					infoText = url.getAsString();
					if (
						url.getAsString().startsWith("https://wiki.hypixel.net/") && NotEnoughUpdates.INSTANCE.config.misc.wiki == 0
							|| url.getAsString().startsWith("https://hypixel-skyblock.fandom.com/") &&
							NotEnoughUpdates.INSTANCE.config.misc.wiki == 1) break;
				}
			} else {
				StringBuilder loreBuilder = new StringBuilder();
				for (int i = 0; i < lore.size(); i++) {
					loreBuilder.append(lore.get(i).getAsString());
					if (i != lore.size() - 1)
						loreBuilder.append("\n");
				}
				infoText = loreBuilder.toString();
			}
			String internalname = item.get("internalname").getAsString();
			String name = item.get("displayname").getAsString();
			displayInformationPane(new TextInfoPane(
				this,
				manager,
				EnumChatFormatting.GRAY + "Loading",
				EnumChatFormatting.GRAY + "Loading your requested information about " + name + EnumChatFormatting.GRAY + "."
			));
			infoPaneLoadingJob = InfoPane.create(this, manager, infoType, name, internalname, infoText)
																	 .thenAccept(this::displayInformationPane);
		}
	}

	public void mouseInputInv() {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer) {
			if (Mouse.getEventButton() == manager.keybindItemSelect.getKeyCode() + 100 &&
				NotEnoughUpdates.INSTANCE.config.toolbar.searchBar) {
				Slot slot = Utils.getSlotUnderMouse((GuiContainer) Minecraft.getMinecraft().currentScreen);
				if (slot != null) {
					ItemStack hover = slot.getStack();
					if (hover != null) {
						textField.setText("id:" + manager.getInternalNameForItem(hover));
						itemPaneOpen = true;
						updateSearch();
					}
				}
			}
		}
	}

	/**
	 * Handles the mouse input, cancelling the forge event if a NEU gui element is clicked.
	 */
	public synchronized boolean mouseInput() {
		if (disabled) {
			return false;
		}

		Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.itemlist.paneGuiScale);

		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		//if(lastMouseX != mouseX || lastMouseY != mouseY) {
		//    millisLastMouseMove = System.currentTimeMillis();
		//}

		lastMouseX = mouseX;
		lastMouseY = mouseY;

		if (Mouse.getEventButtonState()) {
			mouseDown = true;
		} else if (Mouse.getEventButton() != -1) {
			mouseDown = false;
		}

		//Unfocuses the search bar by default. Search bar is focused if the click is on the bar itself.
		if (Mouse.getEventButtonState()) setSearchBarFocus(false);

		guiGroup.mouseClick(0, 0, mouseX, mouseY);

		if (selectedItemGroup != null) {
			int selectedX = Math.min(selectedItemGroupX, width - getBoxPadding() - 18 * selectedItemGroup.size());
			if (mouseY > selectedItemGroupY + 17 && mouseY < selectedItemGroupY + 35) {
				if (!Mouse.getEventButtonState()) {
					Utils.pushGuiScale(-1);
					return true; //End early if the mouse isn't pressed, but still cancel event.
				}
				for (int i = 0; i < selectedItemGroup.size(); i++) {
					if (mouseX >= selectedX - 1 + 18 * i && mouseX <= selectedX + 17 + 18 * i) {
						JsonObject item = selectedItemGroup.get(i);
						if (item != null) {
							if (Mouse.getEventButton() == 0) {
								manager.showRecipe(item);
							} else if (Mouse.getEventButton() == 1) {
								showInfo(item);
							} else if (Mouse.getEventButton() == manager.keybindItemSelect.getKeyCode() + 100 &&
								NotEnoughUpdates.INSTANCE.config.toolbar.searchBar) {
								textField.setText("id:" + item.get("internalname").getAsString());
								updateSearch();
								searchMode = true;
							}
						}
						Utils.pushGuiScale(-1);
						return true;
					}
				}
			}
		}

		//Item selection (right) gui
		if (mouseX > width * getItemPaneOffsetFactor()) {
			if (!Mouse.getEventButtonState()) {
				Utils.pushGuiScale(-1);
				return true; //End early if the mouse isn't pressed, but still cancel event.
			}

			AtomicBoolean clickedItem = new AtomicBoolean(false);
			iterateItemSlots(new ItemSlotConsumer() {
				public void consume(int x, int y, int id) {
					if (mouseX >= x - 1 && mouseX <= x + ITEM_SIZE + 1) {
						if (mouseY >= y - 1 && mouseY <= y + ITEM_SIZE + 1) {
							clickedItem.set(true);

							JsonObject item = getSearchedItemPage(id);
							if (item != null) {
								if (Mouse.getEventButton() == 0) {
									manager.showRecipe(item);
								} else if (Mouse.getEventButton() == 1) {
									showInfo(item);
								} else if (Mouse.getEventButton() == manager.keybindItemSelect.getKeyCode() + 100 &&
									NotEnoughUpdates.INSTANCE.config.toolbar.searchBar) {
									textField.setText("id:" + item.get("internalname").getAsString());
									updateSearch();
									searchMode = true;
								}
							}
						}
					}
				}
			});
			if (!clickedItem.get()) {
				int paneWidth = (int) (width / 3 * getWidthMult());
				int leftSide = (int) (width * getItemPaneOffsetFactor());
				int rightSide = leftSide + paneWidth - getBoxPadding() - getItemBoxXPadding();
				leftSide = leftSide + getBoxPadding() + getItemBoxXPadding();

				FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
				int maxPages = getMaxPages();
				String name = Utils.peekGuiScale().getScaleFactor() < 4 ? "Page: " : "";
				float maxStrLen = fr.getStringWidth(EnumChatFormatting.BOLD + name + maxPages + "/" + maxPages);
				float maxButtonXSize = (rightSide - leftSide + 2 - maxStrLen * 0.5f - 10) / 2f;
				int buttonXSize = (int) Math.min(maxButtonXSize, getSearchBarYSize() * 480 / 160f);
				int ySize = (int) (buttonXSize / 480f * 160);
				int yOffset = (int) ((getSearchBarYSize() - ySize) / 2f);
				int top = getBoxPadding() + yOffset;

				if (mouseY >= top && mouseY <= top + ySize) {
					int leftPrev = leftSide - 1;
					if (mouseX > leftPrev && mouseX < leftPrev + buttonXSize) { //"Previous" button
						setPage(page - 1);
						Utils.playPressSound();
					}
					int leftNext = rightSide + 1 - buttonXSize;
					if (mouseX > leftNext && mouseX < leftNext + buttonXSize) { //"Next" button
						setPage(page + 1);
						Utils.playPressSound();
					}
				}

				float sortIconsMinX = (sortIcons.length + orderIcons.length) * (ITEM_SIZE + ITEM_PADDING) + ITEM_SIZE;
				float availableX = rightSide - leftSide;
				float sortOrderScaleFactor = Math.min(1, availableX / sortIconsMinX);

				int scaledITEM_SIZE = (int) (ITEM_SIZE * sortOrderScaleFactor);
				int scaledItemPaddedSize = (int) ((ITEM_SIZE + ITEM_PADDING) * sortOrderScaleFactor);
				int iconTop = height - getBoxPadding() - (ITEM_SIZE + scaledITEM_SIZE) / 2 - 1;

				if (mouseY >= iconTop && mouseY <= iconTop + scaledITEM_SIZE) {
					for (int i = 0; i < orderIcons.length; i++) {
						int orderIconX = leftSide + i * scaledItemPaddedSize;
						if (mouseX >= orderIconX && mouseX <= orderIconX + scaledITEM_SIZE) {
							if (Mouse.getEventButton() == 0) {
								NotEnoughUpdates.INSTANCE.config.hidden.compareMode = i;
								updateSearch();
								Utils.playPressSound();
							} else if (Mouse.getEventButton() == 1) {
								NotEnoughUpdates.INSTANCE.config.hidden.compareAscending.set(
									i,
									!NotEnoughUpdates.INSTANCE.config.hidden.compareAscending.get(i)
								);
								updateSearch();
								Utils.playPressSound();
							}
						}
					}

					for (int i = 0; i < sortIcons.length; i++) {
						int sortIconX = rightSide - scaledITEM_SIZE - i * scaledItemPaddedSize;
						if (mouseX >= sortIconX && mouseX <= sortIconX + scaledITEM_SIZE) {
							NotEnoughUpdates.INSTANCE.config.hidden.sortMode = i;
							updateSearch();
							Utils.playPressSound();
						}
					}
				}
			}
			Utils.pushGuiScale(-1);
			return true;
		}

		//Clicking on "close info pane" button
		if (mouseX > width * getInfoPaneOffsetFactor() - getBoxPadding() - 8 &&
			mouseX < width * getInfoPaneOffsetFactor() - getBoxPadding() + 8) {
			if (mouseY > getBoxPadding() - 8 && mouseY < getBoxPadding() + 8) {
				if (Mouse.getEventButtonState() && Mouse.getEventButton() < 2) { //Left or right click up
					displayInformationPane(null);
					Utils.pushGuiScale(-1);
					return true;
				}
			}
		}

		if (activeInfoPane != null) {
			if (mouseX < width * getInfoPaneOffsetFactor()) {
				activeInfoPane.mouseInput(width, height, mouseX, mouseY, mouseDown);
				Utils.pushGuiScale(-1);
				return true;
			} else if (Mouse.getEventButton() <= 1 && Mouse.getEventButtonState()) { //Left or right click
				activeInfoPane.mouseInputOutside();
			}
		}

		Utils.pushGuiScale(-1);
		return false;
	}

	public int getPaddingUnscaled() {
		int paddingUnscaled = searchBarPadding / Utils.peekGuiScale().getScaleFactor();
		if (paddingUnscaled < 1) paddingUnscaled = 1;

		return paddingUnscaled;
	}

	public static GuiTextField getTextField() {
		return textField;
	}

	/**
	 * Returns searchBarXSize, scaled by 0.8 if gui scale == AUTO.
	 */
	public int getSearchBarXSize() {
		int searchBarXSize = NotEnoughUpdates.INSTANCE.config.toolbar.searchBarWidth;
		if (Utils.peekGuiScale().getScaleFactor() == 4) return (int) (searchBarXSize * 0.8);
		return searchBarXSize;
	}

	/**
	 * Sets the activeInfoPane and sets the target of the infoPaneOffsetFactor to make the infoPane "slide" out.
	 */
	public void displayInformationPane(InfoPane pane) {
		infoPaneLoadingJob.cancel(false);
		if (pane == null) {
			infoPaneOffsetFactor.setTarget(0);
		} else {
			infoPaneOffsetFactor.setTarget(1 / 3f);
		}
		infoPaneOffsetFactor.resetTimer();
		this.activeInfoPane = pane;
	}

	public InfoPane getActiveInfoPane() {
		return activeInfoPane;
	}

	/**
	 * Finds the index of the character inside the search bar that was clicked, used to set the caret.
	 */
	public int getClickedIndex(int mouseX, int mouseY) {
		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();

		int xComp = mouseX - (width / 2 - getSearchBarXSize() / 2 + 5);

		String trimmed = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(textField.getText(), xComp);
		int linePos = trimmed.length();
		if (linePos != textField.getText().length()) {
			char after = textField.getText().charAt(linePos);
			int trimmedWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(trimmed);
			int charWidth = Minecraft.getMinecraft().fontRendererObj.getCharWidth(after);
			if (trimmedWidth + charWidth / 2 < xComp - 5) {
				linePos++;
			}
		}
		return linePos;
	}

	public void setSearchBarFocus(boolean focus) {
		if (focus) {
			itemPaneOpen = true;
		}
		searchBarHasFocus = focus;
	}

	/**
	 * Handles the keyboard input, cancelling the forge event if the search bar has focus.
	 */
	public boolean keyboardInput(boolean hoverInv) {
		if (Minecraft.getMinecraft().currentScreen == null) return false;

		int keyPressed = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();

		if (disabled) {
			if (Keyboard.getEventKeyState() && keyPressed == manager.keybindToggleDisplay.getKeyCode()) {
				disabled = !disabled;
			}
			return false;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_Y) && NotEnoughUpdates.INSTANCE.config.hidden.dev) {
			displayInformationPane(new DevInfoPane(this, manager));
		}

		if (Keyboard.getEventKeyState()) {
			if (!NotEnoughUpdates.INSTANCE.config.toolbar.searchBar) {
				searchBarHasFocus = false;
			}
			if (searchBarHasFocus) {
				if (keyPressed == 1) {
					searchBarHasFocus = false;
				} else {
					if (textField.textboxKeyTyped(Keyboard.getEventCharacter(), keyPressed)) {
						updateSearch();
					}
				}
			} else {
				if (activeInfoPane != null) {
					if (activeInfoPane.keyboardInput()) {
						return true;
					}
				}

				if (keyPressed == manager.keybindClosePanes.getKeyCode()) {
					itemPaneOffsetFactor.setValue(1);
					itemPaneTabOffset.setValue(20);
					itemPaneOpen = false;
					displayInformationPane(null);
				}

				if (keyPressed == manager.keybindToggleDisplay.getKeyCode()) {
					disabled = !disabled;
					return true;
				}

				AtomicReference<String> internalname = new AtomicReference<>(null);
				AtomicReference<ItemStack> itemstack = new AtomicReference<>(null);
				if (Minecraft.getMinecraft().currentScreen instanceof GuiContainer &&
					Utils.getSlotUnderMouse((GuiContainer) Minecraft.getMinecraft().currentScreen) != null) {
					Slot slot = Utils.getSlotUnderMouse((GuiContainer) Minecraft.getMinecraft().currentScreen);
					ItemStack hover = slot.getStack();
					if (hover != null) {
						internalname.set(manager.getInternalNameForItem(hover));
						itemstack.set(hover);
					}
				} else {
					Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.itemlist.paneGuiScale);

					int width = Utils.peekGuiScale().getScaledWidth();
					int height = Utils.peekGuiScale().getScaledHeight();
					int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
					int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

					if (selectedItemGroup != null) {
						int selectedX = Math.min(selectedItemGroupX, width - getBoxPadding() - 18 * selectedItemGroup.size());

						if (mouseY > selectedItemGroupY + 17 && mouseY < selectedItemGroupY + 35) {
							for (int i = 0; i < selectedItemGroup.size(); i++) {
								if (mouseX >= selectedX - 1 + 18 * i && mouseX <= selectedX + 17 + 18 * i) {
									internalname.set(selectedItemGroup.get(i).get("internalname").getAsString());
								}
							}
						}
					} else {
						iterateItemSlots(new ItemSlotConsumer() {
							public void consume(int x, int y, int id) {
								if (mouseX >= x - 1 && mouseX <= x + ITEM_SIZE + 1) {
									if (mouseY >= y - 1 && mouseY <= y + ITEM_SIZE + 1) {
										JsonObject json = getSearchedItemPage(id);
										if (json != null) internalname.set(json.get("internalname").getAsString());
									}
								}
							}
						});
					}

					Utils.pushGuiScale(-1);
				}
				if (internalname.get() != null) {
					if (itemstack.get() != null) {
						if (NotEnoughUpdates.INSTANCE.config.apiData.repositoryEditing && Keyboard.getEventCharacter() == 'k') {
							Minecraft.getMinecraft().displayGuiScreen(new NEUItemEditor(
								internalname.get(),
								manager.getJsonForItem(itemstack.get())
							));
							return true;
						}
					}
					JsonObject item = manager.getItemInformation().get(internalname.get());
					if (item != null) {
						if (keyPressed == manager.keybindViewUsages.getKeyCode()) {
							manager.displayGuiItemUsages(internalname.get());
							return true;
						} else if (keyPressed == manager.keybindFavourite.getKeyCode()) {
							toggleFavourite(item.get("internalname").getAsString());
							return true;
						} else if (keyPressed == manager.keybindViewRecipe.getKeyCode()) {
							manager.showRecipe(item);
							return true;
						} else if (keyPressed == manager.keybindGive.getKeyCode()) {
							if (Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode) {
								Minecraft.getMinecraft().thePlayer.inventory.addItemStackToInventory(
									manager.jsonToStack(item));
							}
						} else if (NotEnoughUpdates.INSTANCE.config.apiData.repositoryEditing &&
							Keyboard.getEventCharacter() == 'k') {
							Minecraft.getMinecraft().displayGuiScreen(new NEUItemEditor(internalname.get(), item));
							return true;
						} else if (keyPressed == manager.keybindItemSelect.getKeyCode() &&
							NotEnoughUpdates.INSTANCE.config.toolbar.searchBar) {
							textField.setText("id:" + internalname.get());
							itemPaneOpen = true;
							updateSearch();
						} else if (keyPressed == NotEnoughUpdates.INSTANCE.config.ahGraph.graphKey &&
							NotEnoughUpdates.INSTANCE.config.ahGraph.graphEnabled) {
							NotEnoughUpdates.INSTANCE.openGui = new GuiPriceGraph(internalname.get());
							return true;
						}
					}
				}
			}
		}

		return searchBarHasFocus; //Cancels keyboard events if the search bar has focus
	}

	public void toggleFavourite(String internalname) {
		if (getFavourites().contains(internalname)) {
			getFavourites().remove(internalname);
		} else {
			getFavourites().add(internalname);
		}
		updateSearch();
	}

	/**
	 * Convenience functions that get various compare/sort modes from the config.
	 */
	private int getCompareMode() {
		return NotEnoughUpdates.INSTANCE.config.hidden.compareMode;
	}

	private int getSortMode() {
		return NotEnoughUpdates.INSTANCE.config.hidden.sortMode;
	}

	private List<Boolean> getCompareAscending() {
		return NotEnoughUpdates.INSTANCE.config.hidden.compareAscending;
	}

	private List<String> getFavourites() {
		return NotEnoughUpdates.INSTANCE.config.hidden.favourites;
	}

	/**
	 * Creates an item comparator used to sort the list of items according to the favourite set then compare mode.
	 * Defaults to alphabetical sorting if the above factors cannot distinguish between two items.
	 */
	private Comparator<JsonObject> getItemComparator() {
		return (o1, o2) -> {
			//1 (mult) if o1 should appear after o2
			//-1 (-mult) if o2 should appear after o1
			if (getFavourites().contains(o1.get("internalname").getAsString()) && !getFavourites().contains(o2
				.get("internalname")
				.getAsString())) {
				return -1;
			}
			if (!getFavourites().contains(o1.get("internalname").getAsString()) && getFavourites().contains(o2
				.get("internalname")
				.getAsString())) {
				return 1;
			}

			int mult = getCompareAscending().get(getCompareMode()) ? 1 : -1;
			if (getCompareMode() == COMPARE_MODE_RARITY) {
				int rarity1 = Utils.getRarityFromLore(o1.get("lore").getAsJsonArray());
				int rarity2 = Utils.getRarityFromLore(o2.get("lore").getAsJsonArray());

				if (rarity1 < rarity2) return mult;
				if (rarity1 > rarity2) return -mult;
			} else if (getCompareMode() == COMPARE_MODE_VALUE) {
				String internal1 = o1.get("internalname").getAsString();
				String internal2 = o2.get("internalname").getAsString();

				double cost1 = manager.auctionManager.getLowestBin(internal1);
				double cost2 = manager.auctionManager.getLowestBin(internal2);

				if (cost1 < cost2) return mult;
				if (cost1 > cost2) return -mult;
			}

			String i1 = o1.get("internalname").getAsString();
			String[] split1 = i1.split("_");
			String last1 = split1[split1.length - 1];
			String start1 = i1.substring(0, i1.length() - last1.length());

			String i2 = o2.get("internalname").getAsString();
			String[] split2 = i2.split("_");
			String last2 = split2[split2.length - 1];
			String start2 = i2.substring(0, i2.length() - last2.length());

			mult = getCompareAscending().get(COMPARE_MODE_ALPHABETICAL) ? 1 : -1;
			if (start1.equals(start2)) {
				String[] order = new String[]{"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"};
				int type1 = checkItemType(o1.get("lore").getAsJsonArray(), order);
				int type2 = checkItemType(o2.get("lore").getAsJsonArray(), order);

				if (type1 < type2) return -mult;
				if (type1 > type2) return mult;
			}

			int nameComp = mult * o1.get("displayname").getAsString().replaceAll("(?i)\\u00A7.", "")
															.compareTo(o2.get("displayname").getAsString().replaceAll("(?i)\\u00A7.", ""));
			if (nameComp != 0) {
				return nameComp;
			}
			return mult * o1.get("internalname").getAsString().compareTo(o2.get("internalname").getAsString());
		};
	}

	/**
	 * Checks whether an item matches a certain type, i.e. whether the item lore ends in "{rarity} {item type}"
	 * eg. "SHOVEL" will return >0 for "COMMON SHOVEL", "EPIC SHOVEL", etc.
	 *
	 * @return the index of the type that matched, or -1 otherwise.
	 */
	public int checkItemType(JsonArray lore, String... typeMatches) {
		for (int i = lore.size() - 1; i >= 0; i--) {
			String line = lore.get(i).getAsString();

			for (String rarity : Utils.rarityArrC) {
				for (int j = 0; j < typeMatches.length; j++) {
					if (line.trim().equals(rarity + " " + typeMatches[j])) {
						return j;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * Checks whether an item matches the current sort mode.
	 */
	public boolean checkMatchesSort(String internalname, JsonObject item) {
		if (!NotEnoughUpdates.INSTANCE.config.itemlist.showVanillaItems && item.has("vanilla") &&
			item.get("vanilla").getAsBoolean()) {
			return false;
		}

		if (getSortMode() == SORT_MODE_ALL) {
			return NotEnoughUpdates.INSTANCE.config.itemlist.alwaysShowMonsters || !internalname.matches(mobRegex);
		} else if (getSortMode() == SORT_MODE_MOB) {
			return internalname.matches(mobRegex);
		} else if (getSortMode() == SORT_MODE_PET) {
			return internalname.matches(petRegex) && item.get("displayname").getAsString().contains("[");
		} else if (getSortMode() == SORT_MODE_TOOL) {
			return checkItemType(
				item.get("lore").getAsJsonArray(),
				"SWORD",
				"BOW",
				"AXE",
				"PICKAXE",
				"FISHING ROD",
				"WAND",
				"SHOVEL",
				"HOE",
				"DUNGEON SWORD",
				"DUNGEON BOW",
				"DRILL",
				"GAUNTLET",
				"LONGSWORD",
				"DEPLOYABLE"
			) >= 0;
		} else if (getSortMode() == SORT_MODE_ARMOR) {
			return checkItemType(
				item.get("lore").getAsJsonArray(),
				"HELMET",
				"CHESTPLATE",
				"LEGGINGS",
				"BOOTS",
				"DUNGEON HELMET",
				"DUNGEON CHESTPLATE",
				"DUNGEON LEGGINGS",
				"DUNGEON BOOTS",
				"BELT",
				"GLOVES",
				"CLOAK",
				"NECKLACE",
				"BRACELET"
			) >= 0;
		} else if (getSortMode() == SORT_MODE_ACCESSORY) {
			return checkItemType(item.get("lore").getAsJsonArray(), "ACCESSORY", "HATCCESSORY", "DUNGEON ACCESSORY") >= 0;
		}
		return true;
	}

	private final HashMap<String, JsonObject> parentMap = new HashMap<>();

	private final ExecutorService searchES = Executors.newSingleThreadExecutor();

	/**
	 * Clears the current item list, creating a new TreeSet if necessary.
	 * Adds all items that match the search AND match the sort mode to the current item list.
	 * Also adds some easter egg items. (Also I'm very upset if you came here to find them :'( )
	 */
	public void updateSearch() {
		SunTzu.randomizeQuote();

		if (searchedItems == null) searchedItems = new TreeSet<>(getItemComparator());

		searchES.submit(() -> {
			TreeSet<JsonObject> searchedItems = new TreeSet<>(getItemComparator());
			HashMap<String, List<String>> searchedItemsSubgroup = new HashMap<>();

			Set<JsonObject> removeChildItems = new HashSet<>();
			Set<String> itemsMatch = manager.search(textField.getText(), true);
			for (String itemname : itemsMatch) {
				JsonObject item = manager.getItemInformation().get(itemname);
				if (checkMatchesSort(itemname, item)) {
					if (Constants.PARENTS != null) {
						if (Constants.PARENTS.has(itemname) && Constants.PARENTS.get(itemname).isJsonArray()) {
							List<String> children = new ArrayList<>();
							for (JsonElement e : Constants.PARENTS.get(itemname).getAsJsonArray()) {
								if (e.isJsonPrimitive()) {
									children.add(e.getAsString());
								}
							}
							children.retainAll(itemsMatch);
							for (String child : children) {
								removeChildItems.add(manager.getItemInformation().get(child));
							}
							searchedItemsSubgroup.put(itemname, children);
						}
					}
					searchedItems.add(item);
				}
			}
			searchedItems.removeAll(removeChildItems);
			out:
			for (Map.Entry<String, List<String>> entry : searchedItemsSubgroup.entrySet()) {
				if (searchedItems.contains(manager.getItemInformation().get(entry.getKey()))) {
					continue;
				}
				for (String itemname : entry.getValue()) {
					JsonObject item = manager.getItemInformation().get(itemname);
					if (item != null) searchedItems.add(item);
				}
			}
			switch (textField.getText().toLowerCase().trim()) {
				case "nullzee":
					searchedItems.add(CustomItems.NULLZEE);
					break;
				case "rune":
					searchedItems.add(CustomItems.RUNE);
					break;
				case "2b2t":
					searchedItems.add(CustomItems.TWOBEETWOTEE);
					break;
				case "ducttape":
				case "ducttapedigger":
					searchedItems.add(CustomItems.DUCTTAPE);
					break;
				case "thirtyvirus":
					searchedItems.add(manager.getItemInformation().get("SPIKED_BAIT"));
					break;
				case "leocthl":
					searchedItems.add(CustomItems.LEOCTHL);
					break;
				case "spinaxx":
					searchedItems.add(CustomItems.SPINAXX);
					break;
				case "credits":
				case "credit":
				case "who made this mod":
					searchedItems.add(CustomItems.CREDITS);
					break;
				case "ironmoon":
				case "ironm00n":
					searchedItems.add(CustomItems.IRONM00N);
					break;
				case "nopo":
				case "nopothegamer":
					searchedItems.add(CustomItems.NOPO);
					break;
			}

			this.searchedItems = searchedItems;
			this.searchedItemsSubgroup = searchedItemsSubgroup;

			synchronized (this.searchedItemsArr) {
				this.searchedItemsArr.clear();
			}

			redrawItems = true;
		});
	}

	/**
	 * Returns an index-able array containing the elements in searchedItems.
	 * Whenever searchedItems is updated in updateSearch(), the array is recreated here.
	 */
	public synchronized List<JsonObject> getSearchedItems() {
		if (searchedItems == null) {
			updateSearch();
			return new ArrayList<>();
		}

		if (searchedItems.size() > 0 && searchedItemsArr.size() == 0) {
			synchronized (searchedItemsArr) {
				searchedItemsArr.addAll(searchedItems);
			}
		}
		return searchedItemsArr;
	}

	/**
	 * Gets the item in searchedItemArr corresponding to the certain index on the current page.
	 *
	 * @return item, if the item exists. null, otherwise.
	 */
	public JsonObject getSearchedItemPage(int index) {
		if (index < getSlotsXSize() * getSlotsYSize()) {
			int actualIndex = index + getSlotsXSize() * getSlotsYSize() * page;
			List<JsonObject> searchedItems = getSearchedItems();
			if (actualIndex < searchedItems.size()) {
				return searchedItems.get(actualIndex);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public int getItemBoxXPadding() {
		int width = Utils.peekGuiScale().getScaledWidth();
		return (((int) (width / 3 * getWidthMult()) - 2 * getBoxPadding()) % (ITEM_SIZE + ITEM_PADDING) + ITEM_PADDING) / 2;
	}

	public int getBoxPadding() {
		double panePadding = Math.max(0, Math.min(20, NotEnoughUpdates.INSTANCE.config.itemlist.panePadding));
		return (int) (panePadding * 2 / Utils.peekGuiScale().getScaleFactor() + 5);
	}

	private abstract static class ItemSlotConsumer {
		public abstract void consume(int x, int y, int id);
	}

	public void iterateItemSlots(ItemSlotConsumer itemSlotConsumer) {
		int width = Utils.peekGuiScale().getScaledWidth();
		int itemBoxXPadding = getItemBoxXPadding();
		iterateItemSlots(itemSlotConsumer, (int) (width * getItemPaneOffsetFactor()) + getBoxPadding() + itemBoxXPadding);
	}

	/**
	 * Iterates through all the item slots in the right panel and calls a ItemSlotConsumer for each slot with
	 * arguments equal to the slot's x and y position respectively. This is used in order to prevent
	 * code duplication issues.
	 */
	public void iterateItemSlots(ItemSlotConsumer itemSlotConsumer, int xStart) {
		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();

		int paneWidth = (int) (width / 3 * getWidthMult());
		int itemBoxYPadding =
			((height - getSearchBarYSize() - 2 * getBoxPadding() - ITEM_SIZE - 2) % (ITEM_SIZE + ITEM_PADDING) +
				ITEM_PADDING) / 2;

		int yStart = getBoxPadding() + getSearchBarYSize() + itemBoxYPadding;
		int itemBoxXPadding = getItemBoxXPadding();
		int xEnd = xStart + paneWidth - getBoxPadding() * 2 - ITEM_SIZE - itemBoxXPadding;
		int yEnd = height - getBoxPadding() - ITEM_SIZE - 2 - itemBoxYPadding;

		//Render the items, displaying the tooltip if the cursor is over the item
		int id = 0;
		for (int y = yStart; y < yEnd; y += ITEM_SIZE + ITEM_PADDING) {
			for (int x = xStart; x < xEnd; x += ITEM_SIZE + ITEM_PADDING) {
				itemSlotConsumer.consume(x, y, id++);
			}
		}
	}

	public float getWidthMult() {
		float scaleFMult = 1;
		if (Utils.peekGuiScale().getScaleFactor() == 4) scaleFMult *= 0.9f;
		if (manager.auctionManager.customAH.isRenderOverAuctionView() ||
			Minecraft.getMinecraft().currentScreen instanceof CustomAHGui)
			scaleFMult *= 0.8f;
		return (float) Math.max(0.5, Math.min(1.5, NotEnoughUpdates.INSTANCE.config.itemlist.paneWidthMult)) * scaleFMult;
	}

	/**
	 * Calculates the number of horizontal item slots.
	 */
	public int getSlotsXSize() {
		int width = Utils.peekGuiScale().getScaledWidth();

		int paneWidth = (int) (width / 3 * getWidthMult());
		int itemBoxXPadding =
			(((int) (width - width * getItemPaneOffsetFactor()) - 2 * getBoxPadding()) % (ITEM_SIZE + ITEM_PADDING) +
				ITEM_PADDING) / 2;
		int xStart = (int) (width * getItemPaneOffsetFactor()) + getBoxPadding() + itemBoxXPadding;
		int xEnd = (int) (width * getItemPaneOffsetFactor()) + paneWidth - getBoxPadding() - ITEM_SIZE;

		return (int) Math.ceil((xEnd - xStart) / ((float) (ITEM_SIZE + ITEM_PADDING)));
	}

	/**
	 * Calculates the number of vertical item slots.
	 */
	public int getSlotsYSize() {
		int height = Utils.peekGuiScale().getScaledHeight();

		int itemBoxYPadding =
			((height - getSearchBarYSize() - 2 * getBoxPadding() - ITEM_SIZE - 2) % (ITEM_SIZE + ITEM_PADDING) +
				ITEM_PADDING) / 2;
		int yStart = getBoxPadding() + getSearchBarYSize() + itemBoxYPadding;
		int yEnd = height - getBoxPadding() - ITEM_SIZE - 2 - itemBoxYPadding;

		return (int) Math.ceil((yEnd - yStart) / ((float) (ITEM_SIZE + ITEM_PADDING)));
	}

	public int getMaxPages() {
		if (getSearchedItems().size() == 0) return 1;
		return (int) Math.ceil(getSearchedItems().size() / (float) getSlotsYSize() / getSlotsXSize());
	}

	public int getSearchBarYSize() {
		int searchBarYSize = NotEnoughUpdates.INSTANCE.config.toolbar.searchBarHeight;
		return Math.max(searchBarYSize / Utils.peekGuiScale().getScaleFactor(), ITEM_SIZE);
	}

	/**
	 * Renders the top navigation bar, can be used by InfoPane implementations (such as SettingsInfoPane).
	 * Renders "prev" button, index/maxIndex string, "next" button.
	 */
	public void renderNavElement(int leftSide, int rightSide, int maxPages, int page, String name) {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		String pageText = EnumChatFormatting.BOLD + name + page + "/" + maxPages;

		float maxStrLen = fr.getStringWidth(EnumChatFormatting.BOLD + name + maxPages + "/" + maxPages);
		float maxButtonXSize = (rightSide - leftSide + 2 - maxStrLen * 0.5f - 10) / 2f;
		int buttonXSize = (int) Math.min(maxButtonXSize, getSearchBarYSize() * 480 / 160f);
		int ySize = (int) (buttonXSize / 480f * 160);
		int yOffset = (int) ((getSearchBarYSize() - ySize) / 2f);
		int top = getBoxPadding() + yOffset;

		int leftPressed = 0;
		int rightPressed = 0;

		if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
			int width = Utils.peekGuiScale().getScaledWidth();
			int height = Utils.peekGuiScale().getScaledHeight();

			int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
			int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

			if (mouseY >= top && mouseY <= top + ySize) {
				int leftPrev = leftSide - 1;
				if (mouseX > leftPrev && mouseX < leftPrev + buttonXSize) { //"Previous" button
					leftPressed = 1;
				}
				int leftNext = rightSide + 1 - buttonXSize;
				if (mouseX > leftNext && mouseX < leftNext + buttonXSize) { //"Next" button
					rightPressed = 1;
				}
			}
		}

		drawRect(leftSide - 1, top, leftSide - 1 + buttonXSize, top + ySize, fg.getRGB());
		GlStateManager.color(1f, 1f, 1f, 1f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.rightarrow);
		Utils.drawTexturedRect(leftSide - 1 + leftPressed,
			top + leftPressed,
			buttonXSize, ySize, 1, 0, 0, 1
		);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.rightarrow_overlay);
		Utils.drawTexturedRect(leftSide - 1,
			top,
			buttonXSize, ySize, 1 - leftPressed, leftPressed, 1 - leftPressed, leftPressed
		);
		GlStateManager.bindTexture(0);
		Utils.drawStringCenteredScaled(EnumChatFormatting.BOLD + "Prev", fr,
			leftSide - 1 + buttonXSize * 300 / 480f + leftPressed,
			top + ySize / 2f + leftPressed, false,
			(int) (buttonXSize * 240 / 480f), Color.BLACK.getRGB()
		);

		drawRect(rightSide + 1 - buttonXSize, top, rightSide + 1, top + ySize, fg.getRGB());
		GlStateManager.color(1f, 1f, 1f, 1f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.rightarrow);
		Utils.drawTexturedRect(rightSide + 1 - buttonXSize + rightPressed,
			top + rightPressed,
			buttonXSize, ySize
		);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.rightarrow_overlay);
		Utils.drawTexturedRect(rightSide + 1 - buttonXSize,
			top,
			buttonXSize, ySize, 1 - rightPressed, rightPressed, 1 - rightPressed, rightPressed
		);
		GlStateManager.bindTexture(0);
		Utils.drawStringCenteredScaled(EnumChatFormatting.BOLD + "Next", fr,
			rightSide + 1 - buttonXSize * 300 / 480f + rightPressed,
			top + ySize / 2f + rightPressed, false,
			(int) (buttonXSize * 240 / 480f), Color.BLACK.getRGB()
		);

		int strMaxLen = rightSide - leftSide - 2 * buttonXSize - 10;

		drawRect(leftSide - 1 + buttonXSize + 3, top, rightSide + 1 - buttonXSize - 3, top + ySize,
			new Color(177, 177, 177).getRGB()
		);
		drawRect(leftSide + buttonXSize + 3, top + 1, rightSide + 1 - buttonXSize - 3, top + ySize,
			new Color(50, 50, 50).getRGB()
		);
		drawRect(leftSide + buttonXSize + 3, top + 1, rightSide - buttonXSize - 3, top + ySize - 1, fg.getRGB());
		Utils.drawStringCenteredScaledMaxWidth(pageText, fr, (leftSide + rightSide) / 2,
			top + ySize / 2f, false, strMaxLen, Color.BLACK.getRGB()
		);
	}

	private int limCol(int col) {
		return Math.min(255, Math.max(0, col));
	}

	public boolean isUsingMobsFilter() {
		return getSortMode() == SORT_MODE_MOB;
	}

	public float yaw = 0;
	public float pitch = 20;

	/**
	 * Renders an entity onto the GUI at a certain x and y position.
	 */
	private void renderEntity(
		float posX,
		float posY,
		float scale,
		String name,
		Class<? extends EntityLivingBase>... classes
	) {
		EntityLivingBase[] entities = new EntityLivingBase[classes.length];
		try {
			EntityLivingBase last = null;
			for (int i = 0; i < classes.length; i++) {
				Class<? extends EntityLivingBase> clazz = classes[i];
				if (clazz == null) continue;

				EntityLivingBase newEnt =
					clazz.getConstructor(new Class[]{World.class}).newInstance(Minecraft.getMinecraft().theWorld);

				//newEnt.renderYawOffset = yaw;
				//newEnt.rotationYaw = yaw;
				newEnt.rotationPitch = pitch;
				//newEnt.rotationYawHead = yaw;
				//newEnt.prevRotationYawHead = yaw-1;

				newEnt.setCustomNameTag(name);

				if (last != null) {
					last.riddenByEntity = newEnt;
					newEnt.ridingEntity = last;
					last.updateRiderPosition();
				}
				last = newEnt;

				entities[i] = newEnt;
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return;
		}

		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 50.0F);
		GlStateManager.scale(-scale, scale, scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);

		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180.0F);
		rendermanager.setRenderShadow(false);
		for (EntityLivingBase ent : entities) {
			GL11.glColor4f(1, 1, 1, 1);
			if (ent != null) rendermanager.renderEntityWithPosYaw(ent, ent.posX, ent.posY, ent.posZ, 0.0F, 1.0F);
		}
		rendermanager.setRenderShadow(true);

		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	Shader blurShaderHorz = null;
	Framebuffer blurOutputHorz = null;
	Shader blurShaderVert = null;
	Framebuffer blurOutputVert = null;

	/**
	 * Creates a projection matrix that projects from our coordinate space [0->width; 0->height] to OpenGL coordinate
	 * space [-1 -> 1; 1 -> -1] (Note: flipped y-axis).
	 * <p>
	 * This is so that we can render to and from the framebuffer in a way that is familiar to us, instead of needing to
	 * apply scales and translations manually.
	 */
	private Matrix4f createProjectionMatrix(int width, int height) {
		Matrix4f projMatrix = new Matrix4f();
		projMatrix.setIdentity();
		projMatrix.m00 = 2.0F / (float) width;
		projMatrix.m11 = 2.0F / (float) (-height);
		projMatrix.m22 = -0.0020001999F;
		projMatrix.m33 = 1.0F;
		projMatrix.m03 = -1.0F;
		projMatrix.m13 = 1.0F;
		projMatrix.m23 = -1.0001999F;
		return projMatrix;
	}

	public void updateGuiGroupSize() {
		Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.itemlist.paneGuiScale);
		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();

		if (lastScreenWidth != width || lastScreenHeight != height || Utils.peekGuiScale().getScaleFactor() != lastScale) {
			guiGroup.width = width;
			guiGroup.height = height;

			resetAnchors(true);
			guiGroup.recalculate();

			lastScreenWidth = width;
			lastScreenHeight = height;
			lastScale = Utils.peekGuiScale().getScaleFactor();
		}

		Utils.pushGuiScale(-1);
	}

	int guiScaleLast = 0;
	private boolean showVanillaLast = false;

	private boolean wardrobeOpen = false;

	private boolean isInNamedGui(String guiName) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			ContainerChest container = (ContainerChest) chest.inventorySlots;
			IInventory lower = container.getLowerChestInventory();
			String containerName = lower.getDisplayName().getUnformattedText();
			wardrobeOpen = containerName.contains(guiName);
		}
		if (guiScreen instanceof GuiInventory) {
			wardrobeOpen = false;
		}
		return wardrobeOpen;
	}

	private ItemStack getChestSlotsAsItemStack(int slot) {
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
		if (guiScreen instanceof GuiChest) {
			GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
			return chest.inventorySlots.getSlot(slot).getStack();
		} else {
			return null;
		}
	}

	public ItemStack slot1 = null;
	public ItemStack slot2 = null;
	public ItemStack slot3 = null;
	public ItemStack slot4 = null;
	public ItemStack petSlot = null;
	private String lastProfile;

	private ItemStack getWardrobeSlot(int armourSlot) {
		if (SBInfo.getInstance().currentProfile == null) {
			return null;
		}

		if (!Objects.equals(SBInfo.getInstance().currentProfile, lastProfile)) {
			lastProfile = SBInfo.getInstance().currentProfile;
			slot1 = null;
			slot2 = null;
			slot3 = null;
			slot4 = null;
			petSlot = null;
		}

		if (isInNamedGui("Your Equipment")) {
			ItemStack itemStack = getChestSlotsAsItemStack(armourSlot);
			if (itemStack != null) {
				JsonObject itemToSave = NotEnoughUpdates.INSTANCE.manager.getJsonForItem(itemStack);
				if (!itemToSave.has("internalname")) {
					//would crash without internalName when trying to construct the ItemStack again
					itemToSave.add("internalname", new JsonPrimitive("_"));
				}
				NotEnoughUpdates.INSTANCE.config.getProfileSpecific().savedEquipment.put(armourSlot, itemToSave);
				return itemStack;
			}
		} else {
			if (NotEnoughUpdates.INSTANCE.config.getProfileSpecific().savedEquipment.containsKey(armourSlot)) {
				//don't use cache since the internalName is identical in most cases
				return NotEnoughUpdates.INSTANCE.manager.jsonToStack(NotEnoughUpdates.INSTANCE.config.getProfileSpecific().savedEquipment
					.get(armourSlot)
					.getAsJsonObject(), false);
			}
		}
		return null;
	}

	public static boolean isRenderingArmorHud() {
		return renderingArmorHud;
	}

	public static boolean isRenderingPetHud() {
		return renderingPetHud;
	}

	/**
	 * Renders the search bar, quick commands, item selection (right), item info (left) and armor hud gui elements.
	 */
	public void render(boolean hoverInv) {
		if (disabled) {
			return;
		}
		renderingArmorHud = false;
		renderingPetHud = false;
		GlStateManager.enableDepth();

		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		Utils.resetGuiScale();
		Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.itemlist.paneGuiScale);

		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

		if (showVanillaLast != NotEnoughUpdates.INSTANCE.config.itemlist.showVanillaItems) {
			showVanillaLast = NotEnoughUpdates.INSTANCE.config.itemlist.showVanillaItems;
			updateSearch();
		}

		if (textField.getText().toLowerCase().contains("bald")) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(SUPERGEHEIMNISVERMOGEN);
			GlStateManager.color(1, 1, 1, 1);
			Utils.drawTexturedRect((width - 64) / 2f, (height - 64) / 2f - 114, 64, 64, GL11.GL_LINEAR);
			GlStateManager.bindTexture(0);
		}

		if (textField.getText().toLowerCase().contains("lunar")) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(ATMOULBERRYWHYISMYLUNARCLIENTBUGGING);
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.translate(0, 0, 100);
			Utils.drawTexturedRect((width + 410) / 2f, (height + 450) / 2f - 114, 113, 64, GL11.GL_LINEAR);
			GlStateManager.bindTexture(0);
		}

		SunTzu.setEnabled(textField.getText().toLowerCase().startsWith("potato"));

		updateGuiGroupSize();

		if (guiScaleLast != Utils.peekGuiScale().getScaleFactor()) {
			guiScaleLast = Utils.peekGuiScale().getScaleFactor();
			redrawItems = true;
		}

		if (oldWidthMult != getWidthMult()) {
			oldWidthMult = getWidthMult();
			redrawItems = true;
		}

		yaw++;
		yaw %= 360;

		bg = new Color(SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.itemlist.backgroundColour), true);
		fg = new Color(SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.itemlist.foregroundColour));
		Color fgCustomOpacity =
			new Color(SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.itemlist.foregroundColour), true);

		Color fgFavourite2 =
			new Color(SpecialColour.specialToChromaRGB(NotEnoughUpdates.INSTANCE.config.itemlist.favouriteColour), true);
		Color fgFavourite = new Color((int) (fgFavourite2.getRed() * 0.8f), (int) (fgFavourite2.getGreen() * 0.8f),
			(int) (fgFavourite2.getBlue() * 0.8f), fgFavourite2.getAlpha()
		);

		if (itemPaneOpen) {
			if (itemPaneTabOffset.getValue() == 0) {
				if (itemPaneOffsetFactor.getTarget() != 2 / 3f) {
					itemPaneOffsetFactor.setTarget(2 / 3f);
					itemPaneOffsetFactor.resetTimer();
				}
			} else {
				if (itemPaneTabOffset.getTarget() != 0) {
					itemPaneTabOffset.setTarget(0);
					itemPaneTabOffset.resetTimer();
				}
			}
		} else {
			if (itemPaneOffsetFactor.getValue() == 1) {
				if (itemPaneTabOffset.getTarget() != 20) {
					itemPaneTabOffset.setTarget(20);
					itemPaneTabOffset.resetTimer();
				}
			} else {
				if (itemPaneOffsetFactor.getTarget() != 1f) {
					itemPaneOffsetFactor.setTarget(1f);
					itemPaneOffsetFactor.resetTimer();
				}
			}
		}

		itemPaneOffsetFactor.tick();
		itemPaneTabOffset.tick();
		infoPaneOffsetFactor.tick();

		if (page > getMaxPages() - 1) setPage(getMaxPages() - 1);
		if (page < 0) setPage(0);

		GlStateManager.disableLighting();

		/*
		 * Item selection (right) gui element rendering
		 */
		int paneWidth = (int) (width / 3 * getWidthMult());
		int leftSide = (int) (width * getItemPaneOffsetFactor());
		int rightSide = leftSide + paneWidth - getBoxPadding() - getItemBoxXPadding();

		//Tab
		if (NotEnoughUpdates.INSTANCE.config.itemlist.tabOpen) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.itemPaneTabArrow);
			GlStateManager.color(1f, 1f, 1f, 0.3f);
			Utils.drawTexturedRect(width - itemPaneTabOffset.getValue() * 64 / 20f, height / 2f - 32, 64, 64);
			GlStateManager.bindTexture(0);

			if (!itemPaneOpen && mouseX > width - itemPaneTabOffset.getValue() && mouseY > height / 2 - 32
				&& mouseY < height / 2 + 32) {
				itemPaneOpen = true;
			}
		}

		//Atomic reference used so that below lambda doesn't complain about non-effectively-final variable
		AtomicReference<JsonObject> tooltipToDisplay = new AtomicReference<>(null);
		//System.out.println(itemPaneOffsetFactor.getValue());
		if (itemPaneOffsetFactor.getValue() < 0.99) {
			if (NotEnoughUpdates.INSTANCE.config.itemlist.bgBlurFactor > 0.5) {
				BackgroundBlur.renderBlurredBackground(NotEnoughUpdates.INSTANCE.config.itemlist.bgBlurFactor,
					width, height,
					leftSide + getBoxPadding() - 5, getBoxPadding() - 5,
					paneWidth - getBoxPadding() * 2 + 10, height - getBoxPadding() * 2 + 10,
					itemPaneOffsetFactor.getValue() > 0.01
				);
				Gui.drawRect(leftSide + getBoxPadding() - 5, getBoxPadding() - 5,
					leftSide + getBoxPadding() - 5 + paneWidth - getBoxPadding() * 2 + 10,
					getBoxPadding() - 5 + height - getBoxPadding() * 2 + 10, 0xc8101010
				);
			}

			drawRect(leftSide + getBoxPadding() - 5, getBoxPadding() - 5,
				leftSide + paneWidth - getBoxPadding() + 5, height - getBoxPadding() + 5, bg.getRGB()
			);

			renderNavElement(leftSide + getBoxPadding() + getItemBoxXPadding(), rightSide, getMaxPages(), page + 1,
				Utils.peekGuiScale().getScaleFactor() < 4 ? "Page: " : ""
			);

			//Sort bar
			drawRect(leftSide + getBoxPadding() + getItemBoxXPadding() - 1,
				height - getBoxPadding() - ITEM_SIZE - 2,
				rightSide + 1,
				height - getBoxPadding(), fgCustomOpacity.getRGB()
			);

			float sortIconsMinX = (sortIcons.length + orderIcons.length) * (ITEM_SIZE + ITEM_PADDING) + ITEM_SIZE;
			float availableX = rightSide - (leftSide + getBoxPadding() + getItemBoxXPadding());
			float sortOrderScaleFactor = Math.min(1, availableX / sortIconsMinX);

			int scaledITEM_SIZE = (int) (ITEM_SIZE * sortOrderScaleFactor);
			int scaledItemPaddedSize = (int) ((ITEM_SIZE + ITEM_PADDING) * sortOrderScaleFactor);
			int iconTop = height - getBoxPadding() - (ITEM_SIZE + scaledITEM_SIZE) / 2 - 1;

			boolean hoveredOverControl = false;
			for (int i = 0; i < orderIcons.length; i++) {
				int orderIconX = leftSide + getBoxPadding() + getItemBoxXPadding() + i * scaledItemPaddedSize;
				drawRect(orderIconX, iconTop, scaledITEM_SIZE + orderIconX, iconTop + scaledITEM_SIZE, fg.getRGB());

				Minecraft.getMinecraft().getTextureManager().bindTexture(
					getCompareMode() == i ? orderIconsActive[i] : orderIcons[i]);
				GlStateManager.color(1f, 1f, 1f, 1f);
				Utils.drawTexturedRect(orderIconX, iconTop, scaledITEM_SIZE, scaledITEM_SIZE, 0, 1, 0, 1, GL11.GL_NEAREST);

				Minecraft.getMinecraft().getTextureManager().bindTexture(getCompareAscending().get(i)
					? GuiTextures.ascending_overlay
					: GuiTextures.descending_overlay);
				GlStateManager.color(1f, 1f, 1f, 1f);
				Utils.drawTexturedRect(orderIconX, iconTop, scaledITEM_SIZE, scaledITEM_SIZE, 0, 1, 0, 1, GL11.GL_NEAREST);
				GlStateManager.bindTexture(0);

				if (mouseY > iconTop && mouseY < iconTop + scaledITEM_SIZE) {
					if (mouseX > orderIconX && mouseX < orderIconX + scaledITEM_SIZE) {
						hoveredOverControl = true;
						if (System.currentTimeMillis() - millisLastMouseMove > 400) {
							String text = EnumChatFormatting.GRAY + "Order ";
							if (i == COMPARE_MODE_ALPHABETICAL) text += "Alphabetically";
							else if (i == COMPARE_MODE_RARITY) text += "by Rarity";
							else if (i == COMPARE_MODE_VALUE) text += "by Item Worth";
							else text = null;
							if (text != null) textToDisplay = Utils.createList(text);
						}
					}
				}
			}

			for (int i = 0; i < sortIcons.length; i++) {
				int sortIconX = rightSide - scaledITEM_SIZE - i * scaledItemPaddedSize;
				drawRect(sortIconX, iconTop, scaledITEM_SIZE + sortIconX, iconTop + scaledITEM_SIZE, fg.getRGB());
				Minecraft.getMinecraft().getTextureManager().bindTexture(
					getSortMode() == i ? sortIconsActive[i] : sortIcons[i]);
				GlStateManager.color(1f, 1f, 1f, 1f);
				Utils.drawTexturedRect(sortIconX, iconTop, scaledITEM_SIZE, scaledITEM_SIZE, 0, 1, 0, 1, GL11.GL_NEAREST);
				GlStateManager.bindTexture(0);

				if (mouseY > iconTop && mouseY < iconTop + scaledITEM_SIZE) {
					if (mouseX > sortIconX && mouseX < sortIconX + scaledITEM_SIZE) {
						hoveredOverControl = true;
						if (System.currentTimeMillis() - millisLastMouseMove > 400) {
							String text = EnumChatFormatting.GRAY + "Filter ";
							if (i == SORT_MODE_ALL) text = EnumChatFormatting.GRAY + "No Filter";
							else if (i == SORT_MODE_MOB) text += "Mobs";
							else if (i == SORT_MODE_PET) text += "Pets";
							else if (i == SORT_MODE_TOOL) text += "Tools";
							else if (i == SORT_MODE_ARMOR) text += "Armor";
							else if (i == SORT_MODE_ACCESSORY) text += "Accessories";
							else text = null;
							if (text != null) textToDisplay = Utils.createList(text);
						}
					}
				}
			}

			if (!hoveredOverControl) {
				millisLastMouseMove = System.currentTimeMillis();
			}

			if (selectedItemGroup != null) {
				if (mouseX < selectedItemGroupX - 1 || mouseX > selectedItemGroupX + 17 ||
					mouseY < selectedItemGroupY - 1 || mouseY > selectedItemGroupY + 17) {
					int selectedX = Math.min(selectedItemGroupX, width - getBoxPadding() - 18 * selectedItemGroup.size());
					if (mouseX < selectedX - 1 || mouseX > selectedX - 1 + 18 * selectedItemGroup.size() ||
						mouseY < selectedItemGroupY + 17 || mouseY > selectedItemGroupY + 35) {
						selectedItemGroup = null;
						selectedItemMillis = -1;
					}
				}
			}

			if (!hoverInv) {
				iterateItemSlots(new ItemSlotConsumer() {
					public void consume(int x, int y, int id) {
						JsonObject json = getSearchedItemPage(id);
						if (json == null) {
							return;
						}
						if (mouseX > x - 1 && mouseX < x + ITEM_SIZE + 1) {
							if (mouseY > y - 1 && mouseY < y + ITEM_SIZE + 1) {
								String internalname = json.get("internalname").getAsString();
								if (searchedItemsSubgroup.containsKey(internalname)) {
									if (selectedItemMillis == -1) selectedItemMillis = System.currentTimeMillis();
									if (System.currentTimeMillis() - selectedItemMillis > 200 &&
										(selectedItemGroup == null || selectedItemGroup.isEmpty())) {

										ArrayList<JsonObject> children = new ArrayList<>();
										children.add(json);
										for (String itemname : searchedItemsSubgroup.get(internalname)) {
											children.add(manager.getItemInformation().get(itemname));
										}

										selectedItemGroup = children;
										selectedItemGroupX = x;
										selectedItemGroupY = y;
									}
								} else {
									tooltipToDisplay.set(json);
								}
							}
						}
					}
				});
			}

			//Iterate through all item slots and display the appropriate item
			int itemBoxXPadding = getItemBoxXPadding();
			int xStart = (int) (width * getItemPaneOffsetFactor()) + getBoxPadding() + itemBoxXPadding;

			if (OpenGlHelper.isFramebufferEnabled()) {
				renderItemsFromImage(xStart, width, height);
				renderEnchOverlay();

				checkFramebufferSizes(width, height);

				if (redrawItems || !NotEnoughUpdates.INSTANCE.config.hidden.cacheRenderedItempane) {
					renderItemsToImage(width, height, fgFavourite2, fgFavourite, fgCustomOpacity, true, true);
					redrawItems = false;
				}
			} else {
				renderItems(xStart, true, true, true);
			}

			if (selectedItemGroup != null) {
				GL11.glTranslatef(0, 0, 10);

				int selectedX = Math.min(selectedItemGroupX, width - getBoxPadding() - 18 * selectedItemGroup.size());

				GlStateManager.enableDepth();
				GlStateManager.depthFunc(GL11.GL_LESS);
				drawRect(selectedX, selectedItemGroupY + 18,
					selectedX - 2 + 18 * selectedItemGroup.size(), selectedItemGroupY + 34, fgCustomOpacity.getRGB()
				);
				drawRect(selectedX - 1, selectedItemGroupY + 17,
					selectedX - 2 + 18 * selectedItemGroup.size(), selectedItemGroupY + 34, new Color(180, 180, 180).getRGB()
				);
				drawRect(selectedX, selectedItemGroupY + 18,
					selectedX - 1 + 18 * selectedItemGroup.size(), selectedItemGroupY + 35, new Color(30, 30, 30).getRGB()
				);
				drawRect(selectedX - 1 + 2, selectedItemGroupY + 17 + 2,
					selectedX - 1 + 18 * selectedItemGroup.size() + 2, selectedItemGroupY + 35 + 2, 0xa0000000
				);
				GlStateManager.depthFunc(GL11.GL_LEQUAL);

				GL11.glTranslatef(0, 0, 10);

				tooltipToDisplay.set(null);
				if (mouseY > selectedItemGroupY + 17 && mouseY < selectedItemGroupY + 35) {
					for (int i = 0; i < selectedItemGroup.size(); i++) {
						if (mouseX >= selectedX - 1 + 18 * i && mouseX <= selectedX + 17 + 18 * i) {
							tooltipToDisplay.set(selectedItemGroup.get(i));
						}
					}
				}
				for (int i = 0; i < selectedItemGroup.size(); i++) {
					JsonObject item = selectedItemGroup.get(i);
					Utils.drawItemStack(manager.jsonToStack(item), selectedX + 18 * i, selectedItemGroupY + 18);
				}

				GL11.glTranslatef(0, 0, -20);
			}

			GlStateManager.enableBlend();
			GL14.glBlendFuncSeparate(
				GL11.GL_SRC_ALPHA,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.disableLighting();
		}

		/*
		 * Search bar & quickcommand elements
		 */
		guiGroup.render(0, 0);
		resetAnchors(true);

		/*
		 * Item info (left) gui element rendering
		 */

		rightSide = (int) (width * getInfoPaneOffsetFactor());
		leftSide = rightSide - paneWidth;

		if (activeInfoPane != null) {
			activeInfoPane.tick();
			activeInfoPane.render(width, height, bg, fg, Utils.peekGuiScale(), mouseX, mouseY);

			GlStateManager.color(1f, 1f, 1f, 1f);
			Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.close);
			Utils.drawTexturedRect(rightSide - getBoxPadding() - 8, getBoxPadding() - 8, 16, 16);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}

		//Render tooltip
		JsonObject json = tooltipToDisplay.get();
		if (json != null) {

			ItemStack stack = manager.jsonToStack(json);
			{
				NBTTagCompound tag = stack.getTagCompound();
				tag.setBoolean("DisablePetExp", true);
				stack.setTagCompound(tag);
			}

			List<String> text = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);

			String internalname = json.get("internalname").getAsString();
			if (!NotEnoughUpdates.INSTANCE.config.tooltipTweaks.showPriceInfoInvItem) {
				ItemPriceInformation.addToTooltip(text, internalname, stack);
			}

			boolean hasClick =
				(json.has("clickcommand") && !json.get("clickcommand").getAsString().isEmpty())
					|| !manager.getAvailableRecipesFor(internalname).isEmpty();
			boolean hasInfo = json.has("info") && json.get("info").getAsJsonArray().size() > 0;

			if (hasClick || hasInfo) text.add("");
			if (hasClick)
				text.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "LMB/R : View recipe!");
			if (hasInfo)
				text.add(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "RMB : View additional information!");

			textToDisplay = text;
		}
		if (textToDisplay != null) {
			Utils.drawHoveringText(textToDisplay, mouseX, mouseY, width, height, -1, fr);
			textToDisplay = null;
		}

		GlStateManager.enableBlend();
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.disableLighting();
		renderInfoHuds();
		Utils.pushGuiScale(-1);

		if (System.currentTimeMillis() - lastSearchMode > 120000 &&
			NotEnoughUpdates.INSTANCE.config.toolbar.autoTurnOffSearchMode
			|| !NotEnoughUpdates.INSTANCE.config.toolbar.searchBar) {
			searchMode = false;
		}
	}

	void renderInfoHuds() {
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

		Utils.resetGuiScale();
		Utils.pushGuiScale(Minecraft.getMinecraft().gameSettings.guiScale);

		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();
		int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;
		GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;

		if (NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud &&
			NotEnoughUpdates.INSTANCE.config.misc.hidePotionEffect
			&& NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			if (getWardrobeSlot(10) != null) {
				slot1 = getWardrobeSlot(10);
				slot2 = getWardrobeSlot(19);
				slot3 = getWardrobeSlot(28);
				slot4 = getWardrobeSlot(37);
			}
			if (guiScreen instanceof GuiInventory) {
				renderingArmorHud = true;

				List<String> tooltipToDisplay = null;
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 0) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY);
				}
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 1) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_GREY);
				}
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 2) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_DARK);
				}
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 3) {
					if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 3 &&
						NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay && petSlot != null) {
						Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_TRANSPARENT_PET);
					} else {
						Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_TRANSPARENT);
					}
				}
				if (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle == 4) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(ARMOR_DISPLAY_FSR);
				}

				GlStateManager.color(1, 1, 1, 1);
				GL11.glTranslatef(0, 0, 401);
				float yNumber = (float) (height - 167) / 2f;
				Utils.drawTexturedRect((float) ((width - 224.1) / 2f), yNumber, 31, 86, GL11.GL_NEAREST);
				GlStateManager.bindTexture(0);

				Utils.drawItemStack(slot1, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105), true);
				Utils.drawItemStack(slot2, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105) + 18, true);
				Utils.drawItemStack(slot3, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105) + 36, true);
				Utils.drawItemStack(slot4, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105) + 54, true);
				if (slot1 == null) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(QUESTION_MARK);
					GlStateManager.color(1, 1, 1, 1);
					Utils.drawTexturedRect(((width - 208) / 2f), ((height + 60) / 2f - 105), 16, 16, GL11.GL_NEAREST);
					GlStateManager.bindTexture(0);

					tooltipToDisplay = Lists.newArrayList(
						EnumChatFormatting.RED + "Warning",
						EnumChatFormatting.GREEN + "You need to open /equipment",
						EnumChatFormatting.GREEN + "To cache your armour"
					);
					if (mouseX >= ((width - 208) / 2f) && mouseX < ((width - 208) / 2f) + 16) {
						if (mouseY >= ((height + 60) / 2f - 105) && mouseY <= ((height + 60) / 2f - 105) + 70 &&
							NotEnoughUpdates.INSTANCE.config.customArmour.sendWardrobeCommand) {
							if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
								if (Mouse.getEventButtonState()) {
									if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/equipment") ==
										0) {
										NotEnoughUpdates.INSTANCE.sendChatMessage("/equipment");
									}
								}
							}
						}
						if (mouseY >= ((height + 60) / 2f - 105) && mouseY <= ((height + 60) / 2f - 105) + 16) {
							Utils.drawHoveringText(tooltipToDisplay, mouseX, mouseY, width, height, -1, fr);
						}
					}
					GL11.glTranslatef(0, 0, -401);
				}
				if (slot1 != null && slot2 != null && slot3 != null && slot4 != null) {
					if (mouseX >= ((width - 208) / 2f) && mouseX < ((width - 208) / 2f) + 16) {
						if (mouseY >= ((height + 60) / 2f - 105) && mouseY <= ((height + 60) / 2f - 105) + 70 &&
							NotEnoughUpdates.INSTANCE.config.customArmour.sendWardrobeCommand) {
							if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
								if (Mouse.getEventButtonState()) {
									if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/equipment") ==
										0) {
										NotEnoughUpdates.INSTANCE.sendChatMessage("/equipment");
									}
								}
							}
						}

						if (mouseY >= ((height + 60) / 2f - 105) && mouseY <= ((height + 60) / 2f - 105) + 16) {
							tooltipToDisplay = slot1.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							if (shouldShowEquipmentTooltip(tooltipToDisplay)) {
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
							}
						}
						if (mouseY >= ((height + 60) / 2f - 105) + 18 && mouseY <= ((height + 60) / 2f - 105) + 34) {
							tooltipToDisplay = slot2.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							if (shouldShowEquipmentTooltip(tooltipToDisplay)) {
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
							}
						}
						if (mouseY >= ((height + 60) / 2f - 105) + 36 && mouseY <= ((height + 60) / 2f - 105) + 52) {
							tooltipToDisplay = slot3.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							if (shouldShowEquipmentTooltip(tooltipToDisplay)) {
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
							}
						}
						if (mouseY >= ((height + 60) / 2f - 105) + 54 && mouseY <= ((height + 60) / 2f - 105) + 70) {
							tooltipToDisplay = slot4.getTooltip(Minecraft.getMinecraft().thePlayer, false);
							if (shouldShowEquipmentTooltip(tooltipToDisplay)) {
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
							}
						}
					}
					GL11.glTranslatef(0, 0, -401);
				}
			}
		}
		if (PetInfoOverlay.getCurrentPet() != null) {
			if (NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay
				&& (NotEnoughUpdates.INSTANCE.manager
				.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get(PetInfoOverlay.getCurrentPet().petType + ";" + PetInfoOverlay.getCurrentPet().rarity.petId))
				.hasDisplayName()
				|| NotEnoughUpdates.INSTANCE.manager
				.jsonToStack(NotEnoughUpdates.INSTANCE.manager
					.getItemInformation()
					.get(PetInfoOverlay.getCurrentPet().petType + ";" + (PetInfoOverlay.getCurrentPet().rarity.petId - 1)))
				.hasDisplayName())
				&& NotEnoughUpdates.INSTANCE.config.misc.hidePotionEffect &&
				NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
				if (!NotEnoughUpdates.INSTANCE.manager
					.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager
							.getItemInformation()
							.get(PetInfoOverlay.getCurrentPet().petType + ";" + PetInfoOverlay.getCurrentPet().rarity.petId))
					.hasDisplayName()) {
					petSlot = NotEnoughUpdates.INSTANCE.manager.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
							PetInfoOverlay.getCurrentPet().petType + ";" + (PetInfoOverlay.getCurrentPet().rarity.petId - 1)));
				} else {
					petSlot = NotEnoughUpdates.INSTANCE.manager.jsonToStack(
						NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(
							PetInfoOverlay.getCurrentPet().petType + ";" + PetInfoOverlay.getCurrentPet().rarity.petId));
				}
				if (petSlot == null) {
					return;
				}
				petSlot.getTagCompound().setBoolean(
					"NEUHIDEPETTOOLTIP",
					NotEnoughUpdates.INSTANCE.config.petOverlay.hidePetTooltip
				);
				ItemStack petInfo = petSlot;

				if (guiScreen instanceof GuiInventory) {
					GL11.glTranslatef(0, 0, 401);
					if (!NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud) {
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 0) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 1) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY_GREY);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 2) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY_DARK);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 3) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY_TRANSPARENT);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 4) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_DISPLAY_FSR);
						}
					} else {
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 0) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 1) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY_GREY);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 2) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY_DARK);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 3) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY_TRANSPARENT);
						}
						if (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle == 4) {
							Minecraft.getMinecraft().getTextureManager().bindTexture(PET_ARMOR_DISPLAY_FSR);
						}
					}

					GlStateManager.color(1, 1, 1, 1);
					float yNumber = (float) (height - 23) / 2f;
					Utils.drawTexturedRect((float) ((width - 224.1) / 2f), yNumber, 31, 32, GL11.GL_NEAREST);
					GlStateManager.bindTexture(0);

					Utils.drawItemStack(petInfo, (int) ((width - 208) / 2f), (int) ((height + 60) / 2f - 105) + 72, true);
					renderingPetHud = true;

					List<String> tooltipToDisplay = null;
					if (petInfo != null) {
						if (mouseX >= ((width - 208) / 2f) && mouseX < ((width - 208) / 2f) + 16) {
							if (mouseY >= ((height + 60) / 2f - 105) + 72 && mouseY <= ((height + 60) / 2f - 105) + 88 &&
								NotEnoughUpdates.INSTANCE.config.petOverlay.sendPetsCommand) {
								if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null) {
									if (Mouse.getEventButtonState()) {
										if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, "/pets") ==
											0) {
											NotEnoughUpdates.INSTANCE.sendChatMessage("/pets");
										}
									}
								}
								tooltipToDisplay = petInfo.getTooltip(Minecraft.getMinecraft().thePlayer, false);
								Utils.drawHoveringText(
									tooltipToDisplay,
									mouseX - calculateTooltipXOffset(tooltipToDisplay, fr),
									mouseY,
									width,
									height,
									-1,
									fr
								);
								GL11.glTranslatef(0, 0, -80);
							}
						}

					}
				}
			}
		}
	}

	/**
	 * Calculates the width of the longest String in the tooltip, which can be used to offset the entire tooltip to the left more precisely
	 *
	 * @param tooltipToDisplay tooltip
	 * @param fr               FontRenderer object
	 * @return offset to apply
	 */
	private int calculateTooltipXOffset(List<String> tooltipToDisplay, FontRenderer fr) {
		int offset = 0;
		if (tooltipToDisplay != null) {
			for (String line : tooltipToDisplay) {
				int lineWidth = fr.getStringWidth(line);
				if (lineWidth > offset) {
					offset = lineWidth;
				}
			}
		}
		return offset + 20;
	}

	private boolean shouldShowEquipmentTooltip(List<String> toolTip) {
		return !toolTip.get(0).equals("§o§7Empty Equipment Slot§r");
	}

	/**
	 * Used in SettingsInfoPane to redraw the items when a setting changes.
	 */
	public void redrawItems() {
		redrawItems = true;
	}

	/**
	 * Sets the current page and marks that the itemsPane should be redrawn
	 */
	public void setPage(int page) {
		this.page = page;
		redrawItems = true;
	}

	private final Framebuffer[] itemFramebuffers = new Framebuffer[2];

	/**
	 * Checks whether the screen size has changed, if so it reconstructs the itemPane framebuffer and marks that the
	 * itemPane should be redrawn.
	 */
	private void checkFramebufferSizes(int width, int height) {
		int sw = width * Utils.peekGuiScale().getScaleFactor();
		int sh = height * Utils.peekGuiScale().getScaleFactor();
		for (int i = 0; i < itemFramebuffers.length; i++) {
			if (itemFramebuffers[i] == null || itemFramebuffers[i].framebufferWidth != sw ||
				itemFramebuffers[i].framebufferHeight != sh) {
				if (itemFramebuffers[i] == null) {
					itemFramebuffers[i] = new Framebuffer(sw, sh, true);
				} else {
					itemFramebuffers[i].createBindFramebuffer(sw, sh);
				}
				itemFramebuffers[i].setFramebufferFilter(GL11.GL_NEAREST);
				redrawItems = true;
			}
		}
	}

	private void prepareFramebuffer(Framebuffer buffer, int sw, int sh) {
		buffer.framebufferClear();
		buffer.bindFramebuffer(false);
		GL11.glViewport(0, 0, sw, sh);
	}

	private void cleanupFramebuffer(Framebuffer buffer, int sw, int sh) {
		buffer.unbindFramebuffer();
		Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
	}

	/**
	 * Renders all items to a framebuffer so that it can be reused later, drastically improving performance.
	 * Unfortunately using this feature will mean that animated textures will not work, but oh well.
	 * Mojang please optimize item rendering thanks.
	 */
	private void renderItemsToImage(
		int width, int height, Color fgFavourite2,
		Color fgFavourite, Color fgCustomOpacity, boolean items, boolean entities
	) {
		int sw = width * Utils.peekGuiScale().getScaleFactor();
		int sh = height * Utils.peekGuiScale().getScaleFactor();

		GL11.glPushMatrix();
		prepareFramebuffer(itemFramebuffers[0], sw, sh);
		renderItems(10, items, entities, false);
		cleanupFramebuffer(itemFramebuffers[0], sw, sh);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		prepareFramebuffer(itemFramebuffers[1], sw, sh);
		renderItemBackgrounds(fgFavourite2, fgFavourite, fgCustomOpacity);
		cleanupFramebuffer(itemFramebuffers[1], sw, sh);
		GL11.glPopMatrix();
	}

	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	/**
	 * Renders the framebuffer created by #renderItemsToImage to the screen.
	 * itemRenderOffset is a magic number that makes the z-level of the rendered items equal to the z-level of
	 * the item glint overlay model, meaning that a depthFunc of GL_EQUAL can correctly render on to the item.
	 */
	float itemRenderOffset = 7.5001f;

	private void renderItemsFromImage(int xOffset, int width, int height) {
		if (itemFramebuffers[0] != null && itemFramebuffers[1] != null) {
			itemFramebuffers[1].bindFramebufferTexture();
			GlStateManager.color(1f, 1f, 1f, 1f);
			Utils.drawTexturedRect(xOffset - 10, 0, width, height, 0, 1, 1, 0);
			itemFramebuffers[1].unbindFramebufferTexture();

			GL11.glTranslatef(0, 0, itemRenderOffset);
			itemFramebuffers[0].bindFramebufferTexture();
			GlStateManager.color(1f, 1f, 1f, 1f);
			Utils.drawTexturedRect(xOffset - 10, 0, width, height, 0, 1, 1, 0);
			itemFramebuffers[0].unbindFramebufferTexture();
			GL11.glTranslatef(0, 0, -itemRenderOffset);
		}
	}

	/**
	 * Renders the enchant overlay, since only the items have the specific z-offset of 7.5001, this will only apply
	 * the enchant overlay to the actual items and not anything else.
	 * <p>
	 * (I tried very hard to replicate the enchant rendering overlay code from vanilla, but I couldn't get it to
	 * work without rendering with the "ITEM" vertex model like in vanilla, so I choose to render an arbitrary 2D
	 * item. If a texture pack sets a custom 3D model for an apple, this will probably break.)
	 */
	private void renderEnchOverlay() {
		ItemStack stack = new ItemStack(Items.apple);
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
																 .getItemModel(stack);
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		Minecraft.getMinecraft().getTextureManager().bindTexture(RES_ITEM_GLINT);

		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0, -7.5001f + itemRenderOffset);
		iterateItemSlots(new ItemSlotConsumer() {
			public void consume(int x, int y, int id) {
				JsonObject json = getSearchedItemPage(id);
				if (json == null) {
					return;
				}
				ItemStack stack = manager.jsonToStack(json, true, true, false);
				if (stack == null || !stack.hasEffect()) {
					return;
				}

				GlStateManager.pushMatrix();
				GlStateManager.enableRescaleNormal();
				GlStateManager.enableAlpha();
				GlStateManager.alphaFunc(516, 0.1F);
				GlStateManager.enableBlend();

				GlStateManager.disableLighting();

				GlStateManager.translate(x, y, 0);
				GlStateManager.scale(16f, 16f, 16f);

				GlStateManager.depthMask(false);
				GlStateManager.depthFunc(GL11.GL_EQUAL);
				GlStateManager.blendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
				GlStateManager.matrixMode(5890);
				GlStateManager.pushMatrix();
				GlStateManager.scale(8.0F, 8.0F, 8.0F);
				GlStateManager.translate(f, 0.0F, 0.0F);
				GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);

				renderModel(model, -8372020, null);

				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.scale(8.0F, 8.0F, 8.0F);
				GlStateManager.translate(-f1, 0.0F, 0.0F);
				GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);

				renderModel(model, -8372020, null);

				GlStateManager.popMatrix();
				GlStateManager.matrixMode(5888);
				GlStateManager.blendFunc(770, 771);
				GlStateManager.depthFunc(515);
				GlStateManager.depthMask(true);

				GlStateManager.popMatrix();
			}
		});
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GL11.glTranslatef(0, 0, 7.5001f - itemRenderOffset);
		GL11.glPopMatrix();

		GlStateManager.bindTexture(0);
	}

	private void renderModel(IBakedModel model, int color, ItemStack stack) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.ITEM);

		for (EnumFacing enumfacing : EnumFacing.values()) {
			this.renderQuads(worldrenderer, model.getFaceQuads(enumfacing), color);
		}

		this.renderQuads(worldrenderer, model.getGeneralQuads(), color);

		tessellator.draw();
	}

	private void renderQuads(WorldRenderer renderer, List<BakedQuad> quads, int color) {
		if (quads == null) return;

		for (BakedQuad quad : quads) {
			renderer.addVertexData(quad.getVertexData());
			renderer.putColor4(color);
		}
	}

	/**
	 * Renders all the item backgrounds, either squares or squircles.
	 */
	private void renderItemBackgrounds(Color fgFavourite2, Color fgFavourite, Color fgCustomOpacity) {
		if (fgCustomOpacity.getAlpha() == 0) return;
		iterateItemSlots(new ItemSlotConsumer() {
			public void consume(int x, int y, int id) {
				JsonObject json = getSearchedItemPage(id);
				if (json == null) {
					return;
				}

				Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.item_mask);
				if (getFavourites().contains(json.get("internalname").getAsString())) {
					if (NotEnoughUpdates.INSTANCE.config.itemlist.itemStyle == 0) {
						GlStateManager.color(fgFavourite2.getRed() / 255f, fgFavourite2.getGreen() / 255f,
							fgFavourite2.getBlue() / 255f, fgFavourite2.getAlpha() / 255f
						);
						Utils.drawTexturedRect(x - 1, y - 1, ITEM_SIZE + 2, ITEM_SIZE + 2, GL11.GL_NEAREST);

						GlStateManager.color(fgFavourite.getRed() / 255f, fgFavourite.getGreen() / 255f,
							fgFavourite.getBlue() / 255f, fgFavourite.getAlpha() / 255f
						);
						Utils.drawTexturedRect(x, y, ITEM_SIZE, ITEM_SIZE, GL11.GL_NEAREST);
					} else {
						drawRect(x - 1, y - 1, x + ITEM_SIZE + 1, y + ITEM_SIZE + 1, fgFavourite2.getRGB());
						drawRect(x, y, x + ITEM_SIZE, y + ITEM_SIZE, fgFavourite.getRGB());
					}
				} else {
					if (NotEnoughUpdates.INSTANCE.config.itemlist.itemStyle == 0) {
						GlStateManager.color(fgCustomOpacity.getRed() / 255f, fgCustomOpacity.getGreen() / 255f,
							fgCustomOpacity.getBlue() / 255f, fgCustomOpacity.getAlpha() / 255f
						);
						Utils.drawTexturedRect(x - 1, y - 1, ITEM_SIZE + 2, ITEM_SIZE + 2, GL11.GL_NEAREST);
					} else {
						drawRect(x - 1, y - 1, x + ITEM_SIZE + 1, y + ITEM_SIZE + 1, fgCustomOpacity.getRGB());
					}
				}
				GlStateManager.bindTexture(0);
			}
		}, 10);
	}

	private void renderItems(int xStart, boolean items, boolean entities, boolean glint) {
		iterateItemSlots(new ItemSlotConsumer() {
			public void consume(int x, int y, int id) {
				JsonObject json = getSearchedItemPage(id);
				if (json == null) {
					return;
				}

				if (json.has("entityrender")) {
					if (!entities) return;
					String name = json.get("displayname").getAsString();
					String[] split = name.split(" \\(");
					name = name.substring(0, name.length() - split[split.length - 1].length() - 2);

					Class<? extends EntityLivingBase>[] entities = new Class[1];
					if (json.get("entityrender").isJsonArray()) {
						JsonArray entityrender = json.get("entityrender").getAsJsonArray();
						entities = new Class[entityrender.size()];
						for (int i = 0; i < entityrender.size(); i++) {
							Class<? extends Entity> clazz = EntityList.stringToClassMapping.get(entityrender.get(i).getAsString());
							if (clazz != null && EntityLivingBase.class.isAssignableFrom(clazz)) {
								entities[i] = (Class<? extends EntityLivingBase>) clazz;
							}
						}
					} else if (json.get("entityrender").isJsonPrimitive()) {
						Class<? extends Entity> clazz = EntityList.stringToClassMapping.get(json.get("entityrender").getAsString());
						if (clazz != null && EntityLivingBase.class.isAssignableFrom(clazz)) {
							entities[0] = (Class<? extends EntityLivingBase>) clazz;
						}
					}

					float scale = 8;
					if (json.has("entityscale")) {
						scale *= json.get("entityscale").getAsFloat();
					}

					renderEntity(x + ITEM_SIZE / 2, y + ITEM_SIZE, scale, name, entities);
				} else {
					if (!items) return;
					ItemStack stack = manager.jsonToStack(json, true, true, false);
					if (stack != null) {
						if (glint) {
							Utils.drawItemStack(stack, x, y);
						} else {
							Utils.drawItemStackWithoutGlint(stack, x, y);
						}
					}
				}

				GlStateManager.translate(0, 0, 50);
				if (searchedItemsSubgroup.containsKey(json.get("internalname").getAsString())) {
					Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTextures.item_haschild);
					GlStateManager.color(1, 1, 1, 1);
					Utils.drawTexturedRect(x - 1, y - 1, ITEM_SIZE + 2, ITEM_SIZE + 2, GL11.GL_NEAREST);
				}
				GlStateManager.translate(0, 0, -50);
			}
		}, xStart);
	}

	public float getItemPaneOffsetFactor() {
		return itemPaneOffsetFactor.getValue() * getWidthMult() + (1 - getWidthMult());
	}

	public float getInfoPaneOffsetFactor() {
		return infoPaneOffsetFactor.getValue() * getWidthMult();
	}

	public ResourceLocation getCustomEquipmentTexture(boolean isPetRendering) {
		switch (NotEnoughUpdates.INSTANCE.config.customArmour.colourStyle) {
			case 0:
				return ARMOR_DISPLAY;
			case 1:
				return ARMOR_DISPLAY_GREY;
			case 2:
				return ARMOR_DISPLAY_DARK;
			case 3:
				return isPetRendering ? ARMOR_DISPLAY_TRANSPARENT_PET : ARMOR_DISPLAY_TRANSPARENT;
			case 4:
				return ARMOR_DISPLAY_FSR;
		}
		return null;
	}

	public ResourceLocation getCustomPetTexture(boolean isArmorRendering) {
		switch (NotEnoughUpdates.INSTANCE.config.petOverlay.colourStyle) {
			case 0:
				return isArmorRendering ? PET_ARMOR_DISPLAY : PET_DISPLAY;
			case 1:
				return isArmorRendering ? PET_ARMOR_DISPLAY_GREY : PET_DISPLAY_GREY;
			case 2:
				return isArmorRendering ? PET_ARMOR_DISPLAY_DARK : PET_DISPLAY_DARK;
			case 3:
				return isArmorRendering ? PET_ARMOR_DISPLAY_TRANSPARENT : PET_DISPLAY_TRANSPARENT;
			case 4:
				return isArmorRendering ? PET_ARMOR_DISPLAY_FSR : PET_DISPLAY_FSR;
		}
		return null;
	}

	public void renderPreviewArmorHud() {
		if (!NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud) return;

		Utils.resetGuiScale();
		Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.itemlist.paneGuiScale);

		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();

		Minecraft.getMinecraft().getTextureManager().bindTexture(getCustomEquipmentTexture(NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay && petSlot != null));

		GlStateManager.color(1, 1, 1, 1);
		GL11.glTranslatef(0, 0, 401);
		float yNumber = (float) (height - 167) / 2f;
		Utils.drawTexturedRect((float) ((width - 224.1) / 2f), yNumber, 31, 86, GL11.GL_NEAREST);
		GlStateManager.bindTexture(0);
	}

	public void renderPreviewPetInvHud() {
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.petInvDisplay) return;

		Utils.resetGuiScale();
		Utils.pushGuiScale(NotEnoughUpdates.INSTANCE.config.itemlist.paneGuiScale);

		int width = Utils.peekGuiScale().getScaledWidth();
		int height = Utils.peekGuiScale().getScaledHeight();

		Minecraft.getMinecraft().getTextureManager().bindTexture(getCustomPetTexture(NotEnoughUpdates.INSTANCE.config.customArmour.enableArmourHud));

		GlStateManager.color(1, 1, 1, 1);
		GL11.glTranslatef(0, 0, 401);
		float yNumber = (float) (height - 23) / 2f;
		Utils.drawTexturedRect((float) ((width - 224.1) / 2f), yNumber, 31, 32, GL11.GL_NEAREST);
		GlStateManager.bindTexture(0);
	}
}
