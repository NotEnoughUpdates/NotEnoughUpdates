package io.github.moulberry.notenoughupdates.mbgui;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.auction.CustomAHGui;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class MBGuiGroupFloating extends MBGuiGroup {
	private GuiScreen lastScreen = null;
	private final HashMap<MBGuiElement, Vector2f> childrenPositionOffset = new HashMap<>();

	//Serialized
	private final LinkedHashMap<MBGuiElement, MBAnchorPoint> children;

	public MBGuiGroupFloating(int width, int height, LinkedHashMap<MBGuiElement, MBAnchorPoint> children) {
		this.width = width;
		this.height = height;
		this.children = children;
		recalculate();
	}

	public Map<MBGuiElement, MBAnchorPoint> getChildrenMap() {
		return Collections.unmodifiableMap(children);
	}

	@Override
	public Map<MBGuiElement, Vector2f> getChildrenPosition() {
		GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

		if (currentScreen instanceof GuiContainer || currentScreen instanceof GuiItemRecipe
			|| currentScreen instanceof CustomAHGui ||
			NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.isRenderOverAuctionView()) {

			if (lastScreen != currentScreen) {
				lastScreen = currentScreen;

				ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
				int screenWidth = scaledResolution.getScaledWidth();
				int screenHeight = scaledResolution.getScaledHeight();

				int xSize = -1;
				int ySize = -1;
				int guiLeft = -1;
				int guiTop = -1;

				if (currentScreen instanceof GuiContainer) {
					GuiContainer currentContainer = (GuiContainer) currentScreen;

					try {
						xSize = (int) Utils.getField(GuiContainer.class, currentContainer, "xSize", "field_146999_f");
						ySize = (int) Utils.getField(GuiContainer.class, currentContainer, "ySize", "field_147000_g");
						guiLeft = (int) Utils.getField(GuiContainer.class, currentContainer, "guiLeft", "field_147003_i");
						guiTop = (int) Utils.getField(GuiContainer.class, currentContainer, "guiTop", "field_147009_r");
					} catch (Exception ignored) {
					}
				} else if (currentScreen instanceof GuiItemRecipe) {
					xSize = ((GuiItemRecipe) currentScreen).xSize;
					ySize = ((GuiItemRecipe) currentScreen).ySize;
					guiLeft = ((GuiItemRecipe) currentScreen).guiLeft;
					guiTop = ((GuiItemRecipe) currentScreen).guiTop;
				} else if (currentScreen instanceof CustomAHGui ||
					NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.isRenderOverAuctionView()) {
					xSize = NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.getXSize();
					ySize = NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.getYSize();
					guiLeft = NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.guiLeft;
					guiTop = NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.guiTop;
				}

				if (xSize <= 0 && ySize <= 0 && guiLeft <= 0 && guiTop <= 0) {
					lastScreen = null;
					return Collections.unmodifiableMap(childrenPosition);
				}

				for (Map.Entry<MBGuiElement, MBAnchorPoint> entry : children.entrySet()) {
					MBGuiElement child = entry.getKey();
					MBAnchorPoint anchorPoint = entry.getValue();

					Vector2f childPos;
					if (childrenPosition.containsKey(child)) {
						childPos = new Vector2f(childrenPosition.get(child));
					} else {
						childPos = new Vector2f();
					}

					if (anchorPoint.inventoryRelative) {
						int defGuiLeft = (screenWidth - xSize) / 2;
						int defGuiTop = (screenHeight - ySize) / 2;

						childPos.x += guiLeft - defGuiLeft + (0.5f - anchorPoint.anchorPoint.x) * xSize;
						childPos.y += guiTop - defGuiTop + (0.5f - anchorPoint.anchorPoint.y) * ySize;
					}

					childrenPositionOffset.put(child, childPos);
				}
			}
			return Collections.unmodifiableMap(childrenPositionOffset);
		} else {
			return Collections.unmodifiableMap(childrenPosition);
		}
	}

	@Override
	public void recalculate() {
		lastScreen = null;

		for (MBGuiElement child : children.keySet()) {
			child.recalculate();
		}

		for (Map.Entry<MBGuiElement, MBAnchorPoint> entry : children.entrySet()) {
			MBGuiElement child = entry.getKey();
			MBAnchorPoint anchorPoint = entry.getValue();
			float x = anchorPoint.anchorPoint.x * width - anchorPoint.anchorPoint.x * child.getWidth() + anchorPoint.offset.x;
			float y =
				anchorPoint.anchorPoint.y * height - anchorPoint.anchorPoint.y * child.getHeight() + anchorPoint.offset.y;

			if (anchorPoint.inventoryRelative) {
				x = width * 0.5f + anchorPoint.offset.x;
				y = height * 0.5f + anchorPoint.offset.y;
			}

			childrenPosition.put(child, new Vector2f(x, y));
		}
	}

	@Override
	public Collection<MBGuiElement> getChildren() {
		return children.keySet();
	}
}
