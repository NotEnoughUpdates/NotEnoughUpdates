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

import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WitherCloakChanger {
	public static boolean isCloakActive = false;

	@SubscribeEvent
	public void onChatMessage(ClientChatReceivedEvent event) {
		if(event.message.getUnformattedText().startsWith("Creeper Veil ")) { isCloakActive = event.message.getUnformattedText().equals("Creeper Veil Activated!"); }
	}

	@SubscribeEvent
	public void onWorldChange(WorldEvent.Unload event) {
		isCloakActive = false;
	}

	private static final ResourceLocation witherCloakShield = new ResourceLocation("notenoughupdates:wither_cloak_shield.png");

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event) {

		//CONSTANTS (Other contribs, mess with these as you wish, but you should know I chose these for a reason)
		double shieldWidth = 0.8d; //How wide they are
		double shieldHeight = 2.0d; //How tall they are
		double accuracy = 4.0d; //Will be accurate to 1/accuracy of a degree (so updates every 0.25 degrees with accuracy of 4)

		//SETTINGS (The ones players can change)
		boolean witherCloakChangerToggle = true;
		double distanceFromPlayer = 1.2d; //How many blocks will the shields be from the player
		int shieldCount = 6; //How many shields
		float transparency = 1.0f; //How transparent it is
		double speed = 12.0; //Will be how many times each shield goes in a full circle each second

		if(!isCloakActive || !witherCloakChangerToggle) return;
		Minecraft mc = Minecraft.getMinecraft();



		for(int i=0;i<shieldCount;i++) {
			double angle = (int) (((System.currentTimeMillis()/speed*accuracy))%(360*accuracy))/accuracy; angle+=(360d/shieldCount)*i; angle%=360;

			double posX = mc.thePlayer.posX-(shieldWidth/2);
			double posY = mc.thePlayer.posY;
			double posZ = mc.thePlayer.posZ+distanceFromPlayer;

			Vec3 topLeft = rotateAboutOrigin(mc.thePlayer.posX, mc.thePlayer.posZ, angle, new Vec3(posX, posY+shieldHeight, posZ));
			Vec3 topRight = rotateAboutOrigin(mc.thePlayer.posX, mc.thePlayer.posZ, angle, new Vec3(posX+shieldWidth, posY+shieldHeight, posZ));
			Vec3 bottomRight = rotateAboutOrigin(mc.thePlayer.posX, mc.thePlayer.posZ, angle, new Vec3(posX+shieldWidth, posY, posZ));
			Vec3 bottomLeft = rotateAboutOrigin(mc.thePlayer.posX, mc.thePlayer.posZ, angle, new Vec3(posX, posY, posZ));
			RenderUtils.drawFilledQuadWithTexture(topLeft, topRight, bottomRight, bottomLeft, transparency, witherCloakShield);
		}


	}

	private static Vec3 rotateAboutOrigin(double originX, double originZ, double angle, Vec3 point) {
		double a = angle * Math.PI / 180;
		double newX = originX + ( Math.cos(a) * (point.xCoord-originX) + Math.sin(a) * (point.zCoord - originZ));
		double newZ = originZ + ( -Math.sin(a) * (point.xCoord-originX) + Math.cos(a) * (point.zCoord - originZ));
		return new Vec3(newX, point.yCoord, newZ);
	}

}
/*


//
//System.out.println(posX+", "+posZ+" : "+angle);
			//RenderUtils.renderBeaconBeamOrBoundingBox(new BlockPos(posX, mc.thePlayer.posY, posZ), 0x00FFFF, 1.0f, event.partialTicks);
 */
