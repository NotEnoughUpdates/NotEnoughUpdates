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
import io.github.moulberry.notenoughupdates.miscfeatures.FishingHelper;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderFish;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(RenderFish.class)
public abstract class MixinRenderFish extends Render<EntityFishHook> {
	protected MixinRenderFish(RenderManager renderManager) {
		super(renderManager);
	}

	@Inject(method = "doRender(Lnet/minecraft/entity/projectile/EntityFishHook;DDDFF)V", at = @At(value = "HEAD"), cancellable = true)
	public void render(
		EntityFishHook entity,
		double x,
		double y,
		double z,
		float entityYaw,
		float partialTicks,
		CallbackInfo ci
	) {
		if (NotEnoughUpdates.INSTANCE.config.fishing.hideOtherPlayerAll &&
			entity != null && entity.angler != Minecraft.getMinecraft().thePlayer) {
			ci.cancel();
			return;
		}

		if ((!NotEnoughUpdates.INSTANCE.config.fishing.enableRodColours &&
			FishingHelper.getInstance().warningState == FishingHelper.PlayerWarningState.NOTHING) || entity == null)
			return;

		String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(entity.angler.getHeldItem());
		if (NotEnoughUpdates.INSTANCE.isOnSkyblock() && internalname != null && entity.angler != null &&
			entity.angler.getHeldItem().getItem().equals(Items.fishing_rod)) {
			if (!internalname.equals("GRAPPLING_HOOK") && !internalname.endsWith("_WHIP")) {
				ci.cancel();

				GlStateManager.pushMatrix();
				GlStateManager.translate((float) x, (float) y, (float) z);
				GlStateManager.enableRescaleNormal();
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
				this.bindEntityTexture(entity);

				Tessellator tessellator = Tessellator.getInstance();
				WorldRenderer worldrenderer = tessellator.getWorldRenderer();
				worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
				worldrenderer.pos(-0.5D, -0.5D, 0.0D).tex(0.0625D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
				worldrenderer.pos(0.5D, -0.5D, 0.0D).tex(0.125D, 0.1875D).normal(0.0F, 1.0F, 0.0F).endVertex();
				worldrenderer.pos(0.5D, 0.5D, 0.0D).tex(0.125D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
				worldrenderer.pos(-0.5D, 0.5D, 0.0D).tex(0.0625D, 0.125D).normal(0.0F, 1.0F, 0.0F).endVertex();
				tessellator.draw();

				FishingHelper.getInstance().onRenderBobber(entity);

				GlStateManager.disableRescaleNormal();
				GlStateManager.popMatrix();

				double playerVecX;
				double playerVecY;
				double playerVecZ;
				double startY;
				if (this.renderManager.options.thirdPersonView == 0 && entity.angler == Minecraft.getMinecraft().thePlayer) {
					float f7 = entity.angler.getSwingProgress(partialTicks);
					float sqrtSinSwing = MathHelper.sin(MathHelper.sqrt_float(f7) * (float) Math.PI);

					double decimalFov = (this.renderManager.options.fovSetting / 110.0D);
					Vec3 fppOffset = new Vec3(
						(-decimalFov + (decimalFov / 2.5) - (decimalFov / 8)) + 0.025,
						-0.045D * (this.renderManager.options.fovSetting / 100.0D),
						0.4D
					);
					fppOffset = fppOffset.rotatePitch(
						-mathLerp(partialTicks, entity.angler.prevRotationPitch, entity.angler.rotationPitch) *
							((float) Math.PI / 180.0F));
					fppOffset = fppOffset.rotateYaw(
						-mathLerp(partialTicks, entity.angler.prevRotationYaw, entity.angler.rotationYaw) *
							((float) Math.PI / 180.0F));
					fppOffset = fppOffset.rotateYaw(sqrtSinSwing * 0.5F);
					fppOffset = fppOffset.rotatePitch(-sqrtSinSwing * 0.7F);

					playerVecX = entity.angler.prevPosX + (entity.angler.posX - entity.angler.prevPosX) * (double) partialTicks +
						fppOffset.xCoord;
					playerVecY = entity.angler.prevPosY + (entity.angler.posY - entity.angler.prevPosY) * (double) partialTicks +
						fppOffset.yCoord;
					playerVecZ = entity.angler.prevPosZ + (entity.angler.posZ - entity.angler.prevPosZ) * (double) partialTicks +
						fppOffset.zCoord;
					startY = entity.angler.getEyeHeight();
				} else {
					float angle = (entity.angler.prevRenderYawOffset +
						(entity.angler.renderYawOffset - entity.angler.prevRenderYawOffset) * partialTicks) * (float) Math.PI /
						180.0F;
					double d4 = MathHelper.sin(angle);
					double d6 = MathHelper.cos(angle);
					playerVecX = entity.angler.prevPosX + (entity.angler.posX - entity.angler.prevPosX) * (double) partialTicks -
						d6 * 0.35D - d4 * 0.8D;
					playerVecY = entity.angler.prevPosY + entity.angler.getEyeHeight() +
						(entity.angler.posY - entity.angler.prevPosY) * (double) partialTicks - 0.45D;
					playerVecZ = entity.angler.prevPosZ + (entity.angler.posZ - entity.angler.prevPosZ) * (double) partialTicks -
						d4 * 0.35D + d6 * 0.8D;
					startY = entity.angler.isSneaking() ? -0.1875D : 0.0D;
				}

				double d13 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
				double d5 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + 0.25D;
				double d7 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
				double d9 = (float) (playerVecX - d13);
				double d11 = (double) ((float) (playerVecY - d5)) + startY;
				double d12 = (float) (playerVecZ - d7);
				GlStateManager.disableTexture2D();
				GlStateManager.disableLighting();
				GlStateManager.enableBlend();
				GL14.glBlendFuncSeparate(
					GL11.GL_SRC_ALPHA,
					GL11.GL_ONE_MINUS_SRC_ALPHA,
					GL11.GL_ONE,
					GL11.GL_ONE_MINUS_SRC_ALPHA
				);
				worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);

				String specialColour;
				if (entity.angler.getUniqueID().equals(Minecraft.getMinecraft().thePlayer.getUniqueID())) {
					specialColour = NotEnoughUpdates.INSTANCE.config.fishing.ownRodColour;
				} else {
					specialColour = NotEnoughUpdates.INSTANCE.config.fishing.otherRodColour;
				}
				int colourI = SpecialColour.specialToChromaRGB(specialColour);

				for (int l = 0; l <= 16; ++l) {
					if (SpecialColour.getSpeed(specialColour) > 0) { //has chroma
						colourI = SpecialColour.rotateHue(colourI, 10);
					}
					Color colour = new Color(colourI, true);

					float f10 = (float) l / 16.0F;
					worldrenderer.pos(
												 x + d9 * (double) f10,
												 y + d11 * (double) (f10 * f10 + f10) * 0.5D + 0.25D,
												 z + d12 * (double) f10
											 )
											 .color(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha()).endVertex();
				}

				tessellator.draw();
				GlStateManager.disableBlend();
				GlStateManager.enableLighting();
				GlStateManager.enableTexture2D();
			}
		}
	}

	private static float mathLerp(float var1, float var2, float var3) {
		return var2 + var1 * (var3 - var2);
	}

	private static double mathLerp(double var1, double var2, double var3) {
		return var2 + var1 * (var3 - var2);
	}
}
