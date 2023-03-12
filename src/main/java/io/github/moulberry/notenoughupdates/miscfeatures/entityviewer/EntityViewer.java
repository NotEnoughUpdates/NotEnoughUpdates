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

package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EntityViewer extends GuiScreen {

	public static Map<String, Supplier<? extends EntityLivingBase>> validEntities =
		new HashMap<String, Supplier<? extends EntityLivingBase>>() {{
			put("Zombie", () -> new EntityZombie(null));
			put("Chicken", () -> new EntityChicken(null));
			put("Slime", () -> new EntitySlime(null));
			put("Wolf", () -> new EntityWolf(null));
			put("Skeleton", () -> new EntitySkeleton(null));
			put("Creeper", () -> new EntityCreeper(null));
			put("Ocelot", () -> new EntityOcelot(null));
			put("Blaze", () -> new EntityBlaze(null));
			put("Rabbit", () -> new EntityRabbit(null));
			put("Sheep", () -> new EntitySheep(null));
			put("Horse", () -> new EntityHorse(null));
			put("Eisengolem", () -> new EntityIronGolem(null));
			put("Silverfish", () -> new EntitySilverfish(null));
			put("Witch", () -> new EntityWitch(null));
			put("Endermite", () -> new EntityEndermite(null));
			put("Snowman", () -> new EntitySnowman(null));
			put("Villager", () -> new EntityVillager(null));
			put("Guardian", () -> new EntityGuardian(null));
			put("ArmorStand", () -> new EntityArmorStand(null));
			put("Squid", () -> new EntitySquid(null));
			put("Bat", () -> new EntityBat(null));
			put("Spider", () -> new EntitySpider(null));
			put("CaveSpider", () -> new EntityCaveSpider(null));
			put("Pigman", () -> new EntityPigZombie(null));
			put("Ghast", () -> new EntityGhast(null));
			put("MagmaCube", () -> new EntityMagmaCube(null));
			put("Wither", () -> new EntityWither(null));
			put("Enderman", () -> new EntityEnderman(null));
			put("Mooshroom", () -> new EntityMooshroom(null));
			put("WitherSkeleton", () -> {
				EntitySkeleton skeleton = new EntitySkeleton(null);
				skeleton.setSkeletonType(1);
				return skeleton;
			});
			put("Cow", () -> new EntityCow(null));
			put("Dragon", () -> new EntityDragon(null));
			put("Player", () -> new GUIClientPlayer());
			put("Pig", () -> new EntityPig(null));
		}};

	public static Map<String, EntityViewerModifier> validModifiers = new HashMap<String, EntityViewerModifier>() {{
		put("playerdata", new SkinModifier());
		put("equipment", new EquipmentModifier());
		put("riding", new RidingModifier());
		put("charged", new ChargedModifier());
		put("witherdata", new WitherModifier());
		put("invisible", new InvisibleModifier());
		put("age", new AgeModifier());
		put("horse", new HorseModifier());
		put("name", new NameModifier());
	}};

	public int guiLeft = 0;
	public int guiTop = 0;
	public int xSize = 176;
	public int ySize = 166;

	private final String label;
	private final EntityLivingBase entity;
	private static final ResourceLocation BACKGROUND = new ResourceLocation(
		"notenoughupdates",
		"textures/gui/entity_viewer.png"
	);

	public EntityViewer(String label, EntityLivingBase entity) {
		this.label = label;
		this.entity = entity;
	}

	public static EntityLivingBase constructEntity(ResourceLocation resourceLocation) {
		Gson gson = NotEnoughUpdates.INSTANCE.manager.gson;
		try (
			Reader is = new InputStreamReader(
				Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream(),
				StandardCharsets.UTF_8
			)
		) {
			return constructEntity(gson.fromJson(is, JsonObject.class));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static EntityLivingBase constructEntity(JsonObject info) {
		List<JsonObject> modifiers = info.has("modifiers") ?
			StreamSupport.stream(info.get("modifiers").getAsJsonArray().spliterator(), false)
									 .map(JsonElement::getAsJsonObject).collect(Collectors.toList())
			: Collections.emptyList();
		return EntityViewer.constructEntity(info.get("entity").getAsString(), modifiers);
	}

	public static EntityLivingBase constructEntity(String string, String[] modifiers) {
		Gson gson = NotEnoughUpdates.INSTANCE.manager.gson;
		return constructEntity(
			string,
			Arrays.stream(modifiers).map(it -> gson.fromJson(it, JsonObject.class)).collect(Collectors.toList())
		);
	}

	public static EntityLivingBase constructEntity(String string, List<JsonObject> modifiers) {
		Supplier<? extends EntityLivingBase> aClass = validEntities.get(string);
		if (aClass == null) {
			System.err.println("Could not find entity of type: " + string);
			return null;
		}
		try {
			EntityLivingBase entity = aClass.get();
			for (JsonObject modifier : modifiers) {
				String type = modifier.get("type").getAsString();
				EntityViewerModifier entityViewerModifier = validModifiers.get(type);
				entity = entityViewerModifier.applyModifier(entity, modifier);
				if (entity == null) break;
			}
			return entity;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		this.guiLeft = (width - this.xSize) / 2;
		this.guiTop = (height - this.ySize) / 2;

		Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

		Utils.drawStringScaledMaxWidth(label, guiLeft + 10, guiTop + 10, false, 100, 0xFF00FF);
		renderEntity(entity, guiLeft + 90, guiTop + 75, mouseX, mouseY);
	}

	public static void renderEntity(EntityLivingBase entity, int posX, int posY, int mouseX, int mouseY) {
		GlStateManager.color(1F, 1F, 1F, 1F);

		int scale = 30;
		float bottomOffset = 0F;
		EntityLivingBase stack = entity;
		while (true) {
			if (stack instanceof EntityDragon) {
				if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
					scale = 35;
					bottomOffset = 0F;
				}
				else {
					scale = 10;
					bottomOffset = 2F;
				}
			} else if (stack instanceof EntityWither) {
				scale = 20;
			} else if (stack instanceof EntityGhast) {
				scale = 8;
				bottomOffset = 4F;
			}
			stack.ticksExisted = Minecraft.getMinecraft().thePlayer.ticksExisted;
			drawEntityOnScreen(
				posX,
				(int) (posY - bottomOffset * scale),
				scale,
				posX - mouseX,
				(int) (posY - stack.getEyeHeight() * scale - mouseY),
				stack
			);
			bottomOffset += stack.getMountedYOffset();
			if (!(stack.riddenByEntity instanceof EntityLivingBase)) {
				break;
			}
			stack = (EntityLivingBase) stack.riddenByEntity;
		}

	}

	// Need this to flip the ender dragon and make it follow mouse correctly
	public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)posX, (float)posY, 50.0F);
		GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		float f = ent.renderYawOffset;
		float g = ent.rotationYaw;
		float h = ent.rotationPitch;
		float i = ent.prevRotationYawHead;
		float j = ent.rotationYawHead;
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate((ent instanceof EntityDragon) ? 45.0F : -135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((ent instanceof EntityDragon) ? ((float)Math.atan(mouseY / 40.0F)) * 20.0F : -((float)Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
		ent.renderYawOffset = (float)Math.atan(mouseX / 40.0F) * 20.0F;
		ent.rotationYaw = (float)Math.atan(mouseX / 40.0F) * 40.0F;
		ent.rotationPitch = -((float)Math.atan(mouseY / 40.0F)) * 20.0F;
		ent.rotationYawHead = ent.rotationYaw;
		ent.prevRotationYawHead = ent.rotationYaw;
		GlStateManager.translate(0.0F, 0.0F, 0.0F);
		RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		renderManager.setPlayerViewY(180.0F);
		renderManager.setRenderShadow(false);
		renderManager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
		renderManager.setRenderShadow(true);
		ent.renderYawOffset = f;
		ent.rotationYaw = g;
		ent.rotationPitch = h;
		ent.prevRotationYawHead = i;
		ent.rotationYawHead = j;
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
}
