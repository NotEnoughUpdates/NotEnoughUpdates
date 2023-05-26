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

package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NEUOverlay;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.events.DrawSlotReturnEvent;
import io.github.moulberry.notenoughupdates.events.GuiContainerBackgroundDrawnEvent;
import io.github.moulberry.notenoughupdates.events.SlotClickEvent;
import io.github.moulberry.notenoughupdates.listener.RenderListener;
import io.github.moulberry.notenoughupdates.miscfeatures.AbiphoneFavourites;
import io.github.moulberry.notenoughupdates.miscfeatures.AbiphoneWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.AuctionBINWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.AuctionSortModeWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.BetterContainers;
import io.github.moulberry.notenoughupdates.miscfeatures.DungeonNpcProfitOverlay;
import io.github.moulberry.notenoughupdates.miscfeatures.EnchantingSolvers;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.miscfeatures.PresetWarning;
import io.github.moulberry.notenoughupdates.miscfeatures.SlotLocking;
import io.github.moulberry.notenoughupdates.miscgui.GuiCustomEnchant;
import io.github.moulberry.notenoughupdates.miscgui.StorageOverlay;
import io.github.moulberry.notenoughupdates.miscgui.hex.GuiCustomHex;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(value = GuiContainer.class, priority = 500)
public abstract class MixinGuiContainer extends GuiScreen {
	private static boolean hasProfileViewerStack = false;
	private static final ItemStack profileViewerStack = Utils.createItemStack(
		Item.getItemFromBlock(Blocks.command_block),
		EnumChatFormatting.GREEN + "Profile Viewer",
		EnumChatFormatting.YELLOW + "Click to open NEU profile viewer!"
	);

	@Inject(method = "drawSlot", at = @At("RETURN"))
	public void drawSlotRet(Slot slotIn, CallbackInfo ci) {
		SlotLocking.getInstance().drawSlot(slotIn);
		new DrawSlotReturnEvent(slotIn).post();
	}

	@Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
	public void drawSlot(Slot slot, CallbackInfo ci) {
		if (slot == null) return;

		GuiContainer $this = (GuiContainer) (Object) this;

		if (!hasProfileViewerStack && $this instanceof GuiChest && slot.getSlotIndex() > 9 &&
			(slot.getSlotIndex() % 9 == 6 || slot.getSlotIndex() % 9 == 7) &&
			BetterContainers.isBlankStack(-1, slot.getStack())) {
			BetterContainers.profileViewerStackIndex = -1;
			hasProfileViewerStack = true;

			GuiChest eventGui = (GuiChest) $this;
			ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
			String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
			if (containerName.contains(" Profile") && cc.inventorySlots.size() >= 54) {
				if (cc.inventorySlots.get(22).getStack() != null &&
					cc.inventorySlots.get(22).getStack().getTagCompound() != null) {
					NBTTagCompound tag = eventGui.inventorySlots.inventorySlots.get(22).getStack().getTagCompound();
					if (tag.hasKey("SkullOwner") && tag.getCompoundTag("SkullOwner").hasKey("Name")) {
						String tagName = tag.getCompoundTag("SkullOwner").getString("Name");
						String displayName = Utils.cleanColour(cc.inventorySlots.get(22).getStack().getDisplayName());
						if (displayName.length() - tagName.length() >= 0 && tagName.equals(displayName.substring(
							displayName.length() - tagName.length()))) {
							ci.cancel();

							this.zLevel = 100.0F;
							this.itemRender.zLevel = 100.0F;

							GlStateManager.enableDepth();
							this.itemRender.renderItemAndEffectIntoGUI(
								profileViewerStack,
								slot.xDisplayPosition,
								slot.yDisplayPosition
							);
							this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, profileViewerStack,
								slot.xDisplayPosition, slot.yDisplayPosition, ""
							);

							this.itemRender.zLevel = 0.0F;
							this.zLevel = 0.0F;

							BetterContainers.profileViewerStackIndex = slot.getSlotIndex();
						}
					}
				}
			}
		} else if (slot.getSlotIndex() == 0)
			hasProfileViewerStack = false;
		else if (!($this instanceof GuiChest))
			BetterContainers.profileViewerStackIndex = -1;

		if (slot.getStack() == null && NotEnoughUpdates.INSTANCE.overlay.searchMode && RenderListener.drawingGuiScreen &&
			NotEnoughUpdates.INSTANCE.isOnSkyblock()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 100 + Minecraft.getMinecraft().getRenderItem().zLevel);
			GlStateManager.depthMask(false);
			Gui.drawRect(slot.xDisplayPosition, slot.yDisplayPosition,
				slot.xDisplayPosition + 16, slot.yDisplayPosition + 16, NEUOverlay.overlayColourDark
			);
			GlStateManager.depthMask(true);
			GlStateManager.popMatrix();
		}

		ItemStack stack = slot.getStack();

		if (stack != null) {
			if (EnchantingSolvers.onStackRender(
				stack,
				slot.inventory,
				slot.getSlotIndex(),
				slot.xDisplayPosition,
				slot.yDisplayPosition
			)) {
				ci.cancel();
				return;
			}
			if (AbiphoneFavourites.getInstance().onRenderStack(stack)) {
				ci.cancel();
				return;
			}
		}

		RenderHelper.enableGUIStandardItemLighting();

		if (BetterContainers.isOverriding() && !BetterContainers.shouldRenderStack(slot.slotNumber, stack)) {
			ci.cancel();
		}
	}

	@ModifyArg(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;renderToolTip(Lnet/minecraft/item/ItemStack;II)V"), index = 0)
	public ItemStack adjustItemStack(ItemStack itemStack) {
		if (theSlot.slotNumber == BetterContainers.profileViewerStackIndex) {
			return profileViewerStack;
		} else {
			return itemStack;
		}
	}

	@Inject(method = "drawScreen",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V",
			shift = At.Shift.AFTER
		)
	)
	public void drawScreen_after(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		AuctionSortModeWarning.getInstance().onPostGuiRender();
	}

	@Redirect(method = "mouseReleased", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"))
	public boolean mouseReleased_isEmpty(Set<?> set) {
		return set.size() <= 1;
	}

	@Inject(method = "isMouseOverSlot", at = @At("HEAD"), cancellable = true)
	public void isMouseOverSlot(Slot slotIn, int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
		StorageOverlay.getInstance().overrideIsMouseOverSlot(slotIn, mouseX, mouseY, cir);
		GuiCustomEnchant.getInstance().overrideIsMouseOverSlot(slotIn, mouseX, mouseY, cir);
		GuiCustomHex.getInstance().overrideIsMouseOverSlot(slotIn, mouseX, mouseY, cir);
		AuctionBINWarning.getInstance().overrideIsMouseOverSlot(slotIn, mouseX, mouseY, cir);
		AbiphoneWarning.getInstance().overrideIsMouseOverSlot(slotIn, mouseX, mouseY, cir);
		PresetWarning.getInstance().overrideIsMouseOverSlot(slotIn, mouseX, mouseY, cir);
	}

	@Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V"))
	public void drawScreen_drawGradientRect(
		GuiContainer container,
		int left,
		int top,
		int right,
		int bottom,
		int startColor,
		int endColor
	) {
		if (startColor == 0x80ffffff && endColor == 0x80ffffff &&
			theSlot != null && SlotLocking.getInstance().isSlotLocked(theSlot)) {
			int col = 0x80ff8080;
			drawGradientRect(left, top, right, bottom, col, col);
		} else {
			drawGradientRect(left, top, right, bottom, startColor, endColor);
		}
	}

	@Shadow
	private Slot theSlot;

	@Inject(method = "drawScreen", at = @At("RETURN"))
	public void drawScreen(CallbackInfo ci) {
		if (theSlot != null && SlotLocking.getInstance().isSlotLocked(theSlot)) {
			SlotLocking.getInstance().setRealSlot(theSlot);
			theSlot = null;
		} else if (theSlot == null) {
			SlotLocking.getInstance().setRealSlot(null);
		}
	}

	private static final String TARGET_GETSTACK =
		"Lnet/minecraft/inventory/Slot;getStack()Lnet/minecraft/item/ItemStack;";

	@Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = TARGET_GETSTACK))
	public ItemStack drawScreen_getStack(Slot slot) {
		if (theSlot != null && theSlot == slot && theSlot.getStack() != null) {
			ItemStack newStack = EnchantingSolvers.overrideStack(
				theSlot.inventory,
				theSlot.getSlotIndex(),
				theSlot.getStack()
			);
			if (newStack != null) {
				return newStack;
			}
		}
		return slot.getStack();
	}

	@Redirect(method = "drawSlot", at = @At(value = "INVOKE", target = TARGET_GETSTACK))
	public ItemStack drawSlot_getStack(Slot slot) {
		GuiContainer $this = (GuiContainer) (Object) this;

		ItemStack stack = slot.getStack();

		if (stack != null) {
			ItemStack newStack = EnchantingSolvers.overrideStack(slot.inventory, slot.getSlotIndex(), stack);
			if (newStack != null) {
				stack = newStack;
			}
		}

		if ($this instanceof GuiChest) {
			Container container = $this.inventorySlots;
			if (container instanceof ContainerChest) {
				IInventory lower = ((ContainerChest) container).getLowerChestInventory();
				int size = lower.getSizeInventory();
				if (slot.slotNumber >= size) {
					return stack;
				}
				if (System.currentTimeMillis() - BetterContainers.lastRenderMillis < 300 && stack == null) {
					for (int index = 0; index < size; index++) {
						if (lower.getStackInSlot(index) != null) {
							BetterContainers.itemCache.put(slot.slotNumber, null);
							return null;
						}
					}
					return BetterContainers.itemCache.get(slot.slotNumber);
				} else {
					BetterContainers.itemCache.put(slot.slotNumber, stack);
				}
			}
		}
		return stack;
	}

	private static final String TARGET_CANBEHOVERED = "Lnet/minecraft/inventory/Slot;canBeHovered()Z";

	@Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = TARGET_CANBEHOVERED))
	public boolean drawScreen_canBeHovered(Slot slot) {
		if ((NotEnoughUpdates.INSTANCE.config.improvedSBMenu.hideEmptyPanes &&
			BetterContainers.isOverriding() && BetterContainers.isBlankStack(slot.slotNumber, slot.getStack())) ||
			slot.getStack() != null &&
				slot.getStack().hasTagCompound() && slot.getStack().getTagCompound().getBoolean("NEUHIDETOOLIP")) {
			return false;
		}
		return slot.canBeHovered();
	}

	@Inject(method = "checkHotbarKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;handleMouseClick(Lnet/minecraft/inventory/Slot;III)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	public void checkHotbarKeys_Slotlock(int keyCode, CallbackInfoReturnable<Boolean> cir, int i) {
		if (SlotLocking.getInstance().isSlotIndexLocked(i)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "handleMouseClick", at = @At(value = "HEAD"), cancellable = true)
	public void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
		if (slotIn == null) return;
		GuiContainer $this = (GuiContainer) (Object) this;
		SlotClickEvent event = new SlotClickEvent($this, slotIn, slotId, clickedButton, clickType);
		event.post();
		if (event.isCanceled()) {
			ci.cancel();
			return;
		}
		if (event.usePickblockInstead) {
			$this.mc.playerController.windowClick(
				$this.inventorySlots.windowId,
				slotId, 2, 3, $this.mc.thePlayer
			);
			ci.cancel();
		}
	}

	@Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 1))
	private void drawBackground(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		new GuiContainerBackgroundDrawnEvent(((GuiContainer) (Object) this), partialTicks).post();
	}

	@ModifyArg(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V", ordinal = 0))
	public ItemStack drawSlot_renderItemAndEffectIntoGUI(ItemStack stack) {
		return ItemCustomizeManager.useCustomItem(stack);
	}

	@ModifyArg(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"))
	public ItemStack drawSlot_renderItemOverlays(ItemStack stack) {
		return ItemCustomizeManager.useCustomItem(stack);
	}
}
