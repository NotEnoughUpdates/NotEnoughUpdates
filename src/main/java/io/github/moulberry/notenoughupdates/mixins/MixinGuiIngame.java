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

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.ItemCustomizeManager;
import io.github.moulberry.notenoughupdates.miscfeatures.StreamerMode;
import io.github.moulberry.notenoughupdates.miscgui.InventoryStorageSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiIngame.class})
public class MixinGuiIngame {
	@Shadow
	@Final
	protected RenderItem itemRenderer;
	@Shadow
	@Final
	protected Minecraft mc;
	private static final String TARGET = "Lnet/minecraft/scoreboard/ScorePlayerTeam;" +
		"formatPlayerName(Lnet/minecraft/scoreboard/Team;Ljava/lang/String;)Ljava/lang/String;";

	@Redirect(method = "renderScoreboard", at = @At(value = "INVOKE", target = TARGET))
	public String renderScoreboard_formatPlayerName(Team team, String name) {
		if (NotEnoughUpdates.INSTANCE.config.misc.streamerMode) {
			return StreamerMode.filterScoreboard(ScorePlayerTeam.formatPlayerName(team, name));
		}
		return ScorePlayerTeam.formatPlayerName(team, name);
	}

	@Inject(method = "renderTooltip", at = @At("HEAD"))
	protected void renderTooltip(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
		if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
			InventoryStorageSelector.getInstance().render(sr, partialTicks);
		}
	}

	@Redirect(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;drawTexturedModalRect(IIIIII)V"))
	public void renderTooltooltip_drawTexturedModelRect(
		GuiIngame guiIngame,
		int x,
		int y,
		int textureX,
		int textureY,
		int width,
		int height
	) {
		if (!InventoryStorageSelector.getInstance().isSlotSelected() || textureX != 0 || textureY != 22 || width != 24 ||
			height != 22) {
			guiIngame.drawTexturedModalRect(x, y, textureX, textureY, width, height);
		}
	}

	@Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
	public ItemStack updateTick_getCurrentItem(InventoryPlayer inventory) {
		if (!NotEnoughUpdates.INSTANCE.config.storageGUI.showInvBackpackPreview &&
			InventoryStorageSelector.getInstance().isSlotSelected()) {
			return InventoryStorageSelector.getInstance().getNamedHeldItemOverride();
		}
		return inventory.getCurrentItem();
	}

	@Redirect(method = "renderHotbarItem", at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack;animationsToGo:I", opcode = Opcodes.GETFIELD))
	public int renderHotbarItem_animationsToGo(ItemStack stack) {
		return ItemCustomizeManager.useCustomItem(stack).animationsToGo;
	}

	@ModifyArg(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V", ordinal = 0))
	public ItemStack renderHotbarItem_renderItemAndEffectIntoGUI(ItemStack stack) {
		return ItemCustomizeManager.useCustomItem(stack);
	}

	@ModifyArg(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemOverlays(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;II)V", ordinal = 0))
	public ItemStack renderHotbarItem_renderItemOverlays(ItemStack stack) {
		return ItemCustomizeManager.useCustomItem(stack);
	}
}
