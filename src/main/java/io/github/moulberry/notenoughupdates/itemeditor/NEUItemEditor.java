package io.github.moulberry.notenoughupdates.itemeditor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.core.util.lerp.LerpingInteger;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField.COLOUR;
import static io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField.FORCE_CAPS;
import static io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField.MULTILINE;
import static io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField.NO_SPACE;
import static io.github.moulberry.notenoughupdates.itemeditor.GuiElementTextField.NUM_ONLY;

public class NEUItemEditor extends GuiScreen {
	private final NEUManager manager;

	private final List<GuiElement> options = new ArrayList<>();
	private final List<GuiElement> rightOptions = new ArrayList<>();

	private final JsonObject item;

	private static final int PADDING = 10;
	private static final int SCROLL_AMOUNT = 20;

	private final LerpingInteger scrollHeight = new LerpingInteger(0);

	private final Supplier<String> internalname;
	private final Supplier<String> itemid;
	private final Supplier<String> displayname;
	private final Supplier<String> lore;
	private final Supplier<String> crafttext;
	private final Supplier<String> infoType;
	private final Supplier<String> info;
	private final Supplier<String> clickcommand;
	private final Supplier<String> damage;
	private NBTTagCompound nbttag;
	private boolean hasChanges = false;

	public NEUItemEditor(NEUManager manager, String internalname, JsonObject item) {
		this.manager = manager;
		this.item = item;

		if (item.has("nbttag")) {
			try {
				nbttag = JsonToNBT.getTagFromJson(item.get("nbttag").getAsString());
			} catch (NBTException ignored) {
			}
		}

		internalname = internalname == null ? "" : internalname;
		options.add(new GuiElementText("Internal Name: ", Color.WHITE.getRGB()));
		this.internalname = addTextFieldWithSupplier(internalname, NO_SPACE | FORCE_CAPS);

		options.add(new GuiElementText("Item ID: ", Color.WHITE.getRGB()));
		String itemid = item.has("itemid") ? item.get("itemid").getAsString() : "";
		this.itemid = addTextFieldWithSupplier(itemid, NO_SPACE);

		options.add(new GuiElementText("Display name: ", Color.WHITE.getRGB()));
		String displayname = item.has("displayname") ? item.get("displayname").getAsString() : "";
		this.displayname = addTextFieldWithSupplier(displayname, COLOUR);

		options.add(new GuiElementText("Lore: ", Color.WHITE.getRGB()));
		JsonArray lore = item.has("lore") ? item.get("lore").getAsJsonArray() : new JsonArray();
		String[] loreA = new String[lore.size()];
		for (int i = 0; i < lore.size(); i++) loreA[i] = lore.get(i).getAsString();
		this.lore = addTextFieldWithSupplier(String.join("\n", loreA), COLOUR | MULTILINE);

		options.add(new GuiElementText("Craft text: ", Color.WHITE.getRGB()));
		String crafttext = item.has("crafttext") ? item.get("crafttext").getAsString() : "";
		this.crafttext = addTextFieldWithSupplier(crafttext, COLOUR);

		options.add(new GuiElementText("Info type: ", Color.WHITE.getRGB()));
		String infoType = item.has("infoType") ? item.get("infoType").getAsString() : "";
		this.infoType = addTextFieldWithSupplier(infoType, NO_SPACE | FORCE_CAPS);

		options.add(new GuiElementText("Additional information: ", Color.WHITE.getRGB()));
		JsonArray info = item.has("info") ? item.get("info").getAsJsonArray() : new JsonArray();
		String[] infoA = new String[info.size()];
		for (int i = 0; i < info.size(); i++) infoA[i] = info.get(i).getAsString();
		this.info = addTextFieldWithSupplier(String.join("\n", infoA), COLOUR | MULTILINE);

		options.add(new GuiElementText("Click-command (viewrecipe or viewpotion): ", Color.WHITE.getRGB()));
		String clickcommand = item.has("clickcommand") ? item.get("clickcommand").getAsString() : "";
		this.clickcommand = addTextFieldWithSupplier(clickcommand, NO_SPACE);

		options.add(new GuiElementText("Damage: ", Color.WHITE.getRGB()));
		String damage = item.has("damage") ? item.get("damage").getAsString() : "";
		this.damage = addTextFieldWithSupplier(damage, NO_SPACE | NUM_ONLY);

		rightOptions.add(new GuiElementButton("Close (discards changes)", Color.LIGHT_GRAY.getRGB(), () ->
			Minecraft.getMinecraft().displayGuiScreen(null)));
		GuiElementButton button = new Object() { //Used to make the compiler shut the fuck up
			final GuiElementButton b = new GuiElementButton("Save to local disk", Color.GREEN.getRGB(), new Runnable() {
				public void run() {
					if (save()) {
						b.setText("Save to local disk (SUCCESS)");
					} else {
						b.setText("Save to local disk (FAILED)");
					}
				}
			});
		}.b;
		rightOptions.add(button);

		rightOptions.add(new GuiElementText("", Color.WHITE.getRGB()));

		rightOptions.add(new GuiElementButton("Remove enchants", Color.RED.getRGB(), () -> {
			nbttag.removeTag("ench");
			nbttag.getCompoundTag("ExtraAttributes").removeTag("enchantments");
		}));
		rightOptions.add(new GuiElementButton(
			"Add enchant glint",
			Color.ORANGE.getRGB(),
			() -> nbttag.setTag("ench", new NBTTagList())
		));

		rightOptions.add(new GuiElementButton("Remove timestamp/uuid", Color.RED.getRGB(), () -> {
			nbttag.getCompoundTag("ExtraAttributes").removeTag("uuid");
			nbttag.getCompoundTag("ExtraAttributes").removeTag("timestamp");
		}));

		resetScrollToTop();
	}

	public boolean save() {
		int damageI = 0;
		try {
			damageI = Integer.parseInt(damage.get());
		} catch (NumberFormatException ignored) {
		}
		resyncNbttag();
		String[] infoA = info.get().trim().split("\n");
		if (infoA.length == 0 || infoA[0].isEmpty()) {
			infoA = new String[0];
		}
		return manager.writeItemJson(item, internalname.get(), itemid.get(), displayname.get(), lore.get().split("\n"),
			crafttext.get(), infoType.get(), infoA, clickcommand.get(), damageI, nbttag
		);
	}

	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public Supplier<String> addTextFieldWithSupplier(String initialText, int options) {
		GuiElementTextField textField = new GuiElementTextField(initialText, options);
		this.options.add(textField);
		return textField::toString;
	}

	public void resyncNbttag() {
		if (nbttag == null) nbttag = new NBTTagCompound();

		//Item lore
		NBTTagList list = new NBTTagList();
		for (String lore : this.lore.get().split("\n")) {
			list.appendTag(new NBTTagString(lore));
		}

		NBTTagCompound display = nbttag.getCompoundTag("display");
		display.setTag("Lore", list);

		//Name
		display.setString("Name", displayname.get());
		nbttag.setTag("display", display);

		//Internal ID
		NBTTagCompound ea = nbttag.getCompoundTag("ExtraAttributes");
		ea.setString("id", internalname.get());
		nbttag.setTag("ExtraAttributes", ea);
	}

	public void resetScrollToTop() {
		int totalHeight = PADDING;
		for (GuiElement gui : options) {
			totalHeight += gui.getHeight();
		}

		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int height = scaledresolution.getScaledHeight();

		scrollHeight.setValue(totalHeight - height + PADDING);
	}

	public int calculateYScroll() {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int height = scaledresolution.getScaledHeight();

		int totalHeight = PADDING;
		for (GuiElement gui : options) {
			totalHeight += gui.getHeight();
		}

		if (scrollHeight.getValue() < 0) scrollHeight.setValue(0);

		int yScroll = 0;
		if (totalHeight > height - PADDING) {
			yScroll = totalHeight - height + PADDING - scrollHeight.getValue();
		} else {
			scrollHeight.setValue(0);
		}
		if (yScroll < 0) {
			yScroll = 0;
			scrollHeight.setValue(totalHeight - height + PADDING);
		}

		return yScroll;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		scrollHeight.tick();

		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledresolution.getScaledWidth();
		int height = scaledresolution.getScaledHeight();

		GlStateManager.disableLighting();

		Color backgroundColour = new Color(10, 10, 10, 240);
		drawRect(0, 0, width, height, backgroundColour.getRGB());

		int yScroll = calculateYScroll();
		if (yScroll > 0) {
			//Render scroll bar
		}

		int currentY = PADDING - yScroll;
		for (GuiElement gui : options) {
			gui.render(PADDING, currentY);
			currentY += gui.getHeight();
		}

		currentY = PADDING;
		for (GuiElement gui : rightOptions) {
			gui.render(width - PADDING - gui.getWidth(), currentY);
			currentY += gui.getHeight();
		}

		int itemX = 424;
		int itemY = 32;
		int itemSize = 128;
		Color itemBorder = new Color(100, 50, 150, 255);
		Color itemBackground = new Color(120, 120, 120, 255);
		drawRect(itemX - 10, itemY - 10, itemX + itemSize + 10, itemY + itemSize + 10, Color.DARK_GRAY.getRGB());
		drawRect(itemX - 9, itemY - 9, itemX + itemSize + 9, itemY + itemSize + 9, itemBorder.getRGB());
		drawRect(itemX - 6, itemY - 6, itemX + itemSize + 6, itemY + itemSize + 6, Color.DARK_GRAY.getRGB());
		drawRect(itemX - 5, itemY - 5, itemX + itemSize + 5, itemY + itemSize + 5, itemBackground.getRGB());
		ItemStack stack = new ItemStack(Item.itemRegistry.getObject(new ResourceLocation(itemid.get())));

		if (stack.getItem() != null) {
			try {
				stack.setItemDamage(Integer.parseInt(damage.get()));
			} catch (NumberFormatException ignored) {
			}

			resyncNbttag();
			stack.setTagCompound(nbttag);

			int scaleFactor = itemSize / 16;
			GL11.glPushMatrix();
			GlStateManager.scale(scaleFactor, scaleFactor, 1);
			drawItemStack(stack, itemX / scaleFactor, itemY / scaleFactor, null);
			GL11.glPopMatrix();
		}

		//Tooltip
		List<String> text = new ArrayList<>();
		text.add(displayname.get());
		text.addAll(Arrays.asList(lore.get().split("\n")));

		Utils.drawHoveringText(text, itemX - 20, itemY + itemSize + 28, width, height, -1,
			Minecraft.getMinecraft().fontRendererObj
		);

		GlStateManager.disableLighting();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE && !hasChanges) {
			Minecraft.getMinecraft().displayGuiScreen(null);
			return;
		}

		for (GuiElement gui : options) {
			gui.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledresolution.getScaledWidth();

		int yScroll = calculateYScroll();
		int currentY = PADDING - yScroll;
		for (GuiElement gui : options) {
			if (mouseY > currentY && mouseY < currentY + gui.getHeight()
				&& mouseX < gui.getWidth()) {
				gui.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
				return;
			}
			currentY += gui.getHeight();
		}

		currentY = PADDING;
		for (GuiElement gui : rightOptions) {
			if (mouseY > currentY && mouseY < currentY + gui.getHeight()
				&& mouseX > width - PADDING - gui.getWidth()) {
				gui.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
				return;
			}
			currentY += gui.getHeight();
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());

		int maxWidth = 0;
		for (GuiElement gui : options) {
			if (gui.getWidth() > maxWidth) maxWidth = gui.getWidth();
		}

		if (Mouse.getX() < maxWidth * scaledresolution.getScaleFactor()) {
			int dWheel = Mouse.getEventDWheel();

			if (dWheel < 0) {
				scrollHeight.setTarget(scrollHeight.getTarget() - SCROLL_AMOUNT);
				scrollHeight.resetTimer();
			} else if (dWheel > 0) {
				scrollHeight.setTarget(scrollHeight.getTarget() + SCROLL_AMOUNT);
				scrollHeight.resetTimer();
			}
		}

		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
		int width = scaledresolution.getScaledWidth();

		int yScroll = calculateYScroll();
		int currentY = PADDING - yScroll;
		for (GuiElement gui : options) {
			if (mouseY > currentY && mouseY < currentY + gui.getHeight()
				&& mouseX < gui.getWidth()) {
				gui.mouseClicked(mouseX, mouseY, mouseButton);
				for (GuiElement gui2 : options) {
					if (gui2 != gui) {
						gui2.otherComponentClick();
					}
				}
				for (GuiElement gui2 : rightOptions) {
					if (gui2 != gui) {
						gui2.otherComponentClick();
					}
				}
				return;
			}
			currentY += gui.getHeight();
		}

		currentY = PADDING;
		for (GuiElement gui : rightOptions) {
			if (mouseY > currentY && mouseY < currentY + gui.getHeight()
				&& mouseX > width - PADDING - gui.getWidth()) {
				gui.mouseClicked(mouseX, mouseY, mouseButton);
				for (GuiElement gui2 : options) {
					if (gui2 != gui) {
						gui2.otherComponentClick();
					}
				}
				for (GuiElement gui2 : rightOptions) {
					if (gui2 != gui) {
						gui2.otherComponentClick();
					}
				}
				return;
			}
			currentY += gui.getHeight();
		}
	}

	private void drawItemStack(ItemStack stack, int x, int y, String altText) {
		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
		FontRenderer font = Minecraft.getMinecraft().fontRendererObj;

		RenderHelper.enableGUIStandardItemLighting();
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		RenderHelper.disableStandardItemLighting();

		itemRender.renderItemOverlayIntoGUI(font, stack, x, y, altText);
	}

}
