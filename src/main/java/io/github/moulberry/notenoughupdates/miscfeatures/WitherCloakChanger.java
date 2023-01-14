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

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

@NEUAutoSubscribe
public class WitherCloakChanger {
	public static boolean isCloakActive = false;
	/**
	 * When was the last charged Creeper that is a member of the group rendered?
	 * Used to determine if the cloak was deactivated by Hypixel without sending a message
	 *
	 * @see io.github.moulberry.notenoughupdates.mixins.MixinEntityChargedCreeper#cancelChargedCreeperLayer(net.minecraft.entity.monster.EntityCreeper , float, float, float, float, float, float, float, org.spongepowered.asm.mixin.injection.callback.CallbackInfo)
	 */
	public static long lastCreeperRender = 0;
	public static long lastDeactivate = System.currentTimeMillis();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChatMessage(ClientChatReceivedEvent event) {
		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock()) return;
		if (event.message.getUnformattedText().startsWith("Creeper Veil ")) {
			if (isCloakActive && !event.message.getUnformattedText().equals("Creeper Veil Activated!")) {
				isCloakActive = false;
				lastDeactivate = System.currentTimeMillis();
			} else {
				isCloakActive = true;
			}
		} else if (event.message.getUnformattedText().startsWith("Not enough mana! Creeper Veil De-activated!")) {
			isCloakActive = false;
			lastDeactivate = System.currentTimeMillis();
		}
	}

	@SubscribeEvent
	public void onWorldChange(WorldEvent.Unload event) {
		isCloakActive = false;
	}

	private static final ResourceLocation witherCloakShield = new ResourceLocation(
		"notenoughupdates:wither_cloak_shield.png");

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {
		if (isCloakActive) {
			//last creeper rendered over 2 seconds ago -> Creeper Veil de activated without a message. Happens for example when picking up the item in the inventory
			if (System.currentTimeMillis() - lastCreeperRender >= 2000) {
				isCloakActive = false;
				lastDeactivate = System.currentTimeMillis();
				lastCreeperRender = 0;
				return;
			}
		}

		if (!NotEnoughUpdates.INSTANCE.isOnSkyblock() || !isCloakActive ||
			!NotEnoughUpdates.INSTANCE.config.itemOverlays.customWitherCloakToggle) return;
		Minecraft mc = Minecraft.getMinecraft();

		//CONSTANTS (Other contribs, mess with these as you wish, but you should know I chose these for a reason)
		final double shieldWidth = 0.8d; //How wide they are
		final double shieldHeight = 2.0d; //How tall they are
		final double accuracy =
			4.0d; //Will be accurate to 1/accuracy of a degree (so updates every 0.25 degrees with an accuracy of 4)

		for (int i = 0; i < NotEnoughUpdates.INSTANCE.config.itemOverlays.customWitherCloakCount; i++) {
			double angle = (int) (
				((System.currentTimeMillis() / 30 * NotEnoughUpdates.INSTANCE.config.itemOverlays.customWitherCloakSpeed *
					-0.5 * accuracy)) % (360 * accuracy)) / accuracy;
			angle += (360d / NotEnoughUpdates.INSTANCE.config.itemOverlays.customWitherCloakCount) * i;
			angle %= 360;
			double posX = mc.thePlayer.posX - (shieldWidth / 2);
			double posY = mc.thePlayer.posY;
			double posZ = mc.thePlayer.posZ + NotEnoughUpdates.INSTANCE.config.itemOverlays.customWitherCloakDistance;

			Vec3 topLeft = rotateAboutOrigin(
				mc.thePlayer.posX,
				mc.thePlayer.posZ,
				angle,
				new Vec3(posX, posY + shieldHeight, posZ)
			);
			Vec3 topRight = rotateAboutOrigin(
				mc.thePlayer.posX,
				mc.thePlayer.posZ,
				angle,
				new Vec3(posX + shieldWidth, posY + shieldHeight, posZ)
			);
			Vec3 bottomRight = rotateAboutOrigin(
				mc.thePlayer.posX,
				mc.thePlayer.posZ,
				angle,
				new Vec3(posX + shieldWidth, posY, posZ)
			);
			Vec3 bottomLeft = rotateAboutOrigin(mc.thePlayer.posX, mc.thePlayer.posZ, angle, new Vec3(posX, posY, posZ));
			RenderUtils.drawFilledQuadWithTexture(
				topLeft,
				topRight,
				bottomRight,
				bottomLeft, /*NotEnoughUpdates.INSTANCE.config.misc.customWitherCloakTransparency*/
				1.0f,
				witherCloakShield
			);
		}
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.tryBlendFuncSeparate(
			GL11.GL_SRC_ALPHA,
			GL11.GL_ONE_MINUS_SRC_ALPHA,
			GL11.GL_ONE,
			GL11.GL_ONE_MINUS_SRC_ALPHA
		);
	}

	private static Vec3 rotateAboutOrigin(double originX, double originZ, double angle, Vec3 point) {
		double a = angle * Math.PI / 180;
		double newX = originX + (Math.cos(a) * (point.xCoord - originX) + Math.sin(a) * (point.zCoord - originZ));
		double newZ = originZ + (-Math.sin(a) * (point.xCoord - originX) + Math.cos(a) * (point.zCoord - originZ));
		return new Vec3(newX, point.yCoord, newZ);
	}
}
