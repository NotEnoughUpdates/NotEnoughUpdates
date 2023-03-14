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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.ChromaColour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

@NEUAutoSubscribe
public class ItemCustomizeManager {
	public static class ReloadListener implements IResourceManagerReloadListener {
		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
			ItemCustomizeManager.loadedCustomGlintTexture = false;
		}
	}

	public static boolean disableTextureBinding = false;

	private static ResourceLocation CUSTOM_GLINT_TEXTURE = new ResourceLocation(
		"notenoughupdates:dynamic/custom_glint_texture");
	private static boolean loadedCustomGlintTexture = false;

	public static final String DEFAULT_GLINT_COLOR = ChromaColour.special(0, 0xcc, 0x6419FF);
	//A050FF 0x8040cc 100,25,255 64,19

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ItemDataMap itemDataMap = new ItemDataMap();
	public static class ItemDataMap {
		public HashMap<String, ItemData> itemData = new HashMap<>();
	}

	public static class ItemData {
		public String customName = null;
		public String customNamePrefix = "";
		public boolean overrideEnchantGlint = false;
		public boolean enchantGlintValue;

		public String customGlintColour = DEFAULT_GLINT_COLOR;

		public String customLeatherColour = null;

		public String defaultItem = null;
		public String customItem = null;
	}

	public static void putItemData(String uuid, ItemData data) {
		itemDataMap.itemData.put(uuid, data);
	}

	public static void setCustomBlendFunc(String colour) {

        /*int argb = ChromaColour.specialToChromaRGB(colour);
        float[] hsv = Color.RGBtoHSB((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, null);
        GL14.glBlendColor(1, 1, 1, hsv[2]);*/

		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		//GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
	}

	private static void renderEffect(Consumer<Integer> renderModelCallback, int color) {
		GL11.glPushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		GlStateManager.depthFunc(GL11.GL_EQUAL);
		GlStateManager.disableLighting();
		Minecraft.getMinecraft().getTextureManager().bindTexture(getCustomGlintTexture());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		GlStateManager.matrixMode(5890);
		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
		GlStateManager.translate(f, 0.0F, 0.0F);
		GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
		renderModelCallback.accept(color);
		GlStateManager.matrixMode(5890);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.scale(8.0F, 8.0F, 8.0F);
		float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
		GlStateManager.translate(-f1, 0.0F, 0.0F);
		GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
		renderModelCallback.accept(color);
		GlStateManager.matrixMode(5890);
		GlStateManager.popMatrix();

		GlStateManager.matrixMode(5888);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableLighting();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

		GL11.glPopMatrix();
	}

	private static void renderArmorGlint(Runnable renderModelCallback, float existed, int color) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(getCustomGlintTexture());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GlStateManager.enableBlend();
		GlStateManager.depthFunc(514);
		GlStateManager.depthMask(false);
		float f1 = 0.5F;
		GlStateManager.color(f1, f1, f1, 1.0F);

		for (int i = 0; i < 2; ++i) {
			GlStateManager.disableLighting();

			float red = ((color >> 16) & 0xFF) / 255f;
			float green = ((color >> 8) & 0xFF) / 255f;
			float blue = (color & 0xFF) / 255f;
			float alpha = ((color >> 24) & 0xFF) / 255f;

			GlStateManager.color(red, green, blue, alpha);
			GlStateManager.matrixMode(5890);
			GlStateManager.loadIdentity();
			float f3 = 0.33333334F;
			GlStateManager.scale(f3, f3, f3);
			GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, existed * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
			GlStateManager.matrixMode(5888);
			renderModelCallback.run();
		}

		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
		GlStateManager.disableBlend();
	}

	public static void pre() {
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
	}

	public static boolean render3DGlint(String customEnchantGlint, float existed, Runnable renderModelCallback) {
		if (customEnchantGlint != null) {
			int colour = ChromaColour.specialToChromaRGB(customEnchantGlint);

			float[] hsv = Color.RGBtoHSB((colour >> 16) & 0xff, (colour >> 8) & 0xff, colour & 0xff, null);
			GL14.glBlendColor(1, 1, 1, hsv[2]);

			GlStateManager.tryBlendFuncSeparate(
				GL11.GL_ZERO,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ZERO,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);
			int alphaValue = (int) ((1 - hsv[2] * hsv[2]) * 0xff) * ((colour >> 24) & 0xff) / 0xff;
			renderArmorGlint(renderModelCallback, existed, alphaValue << 24);
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
			renderArmorGlint(renderModelCallback, existed, colour);

			return true;
		}
		return false;
	}

	public static boolean renderEffectHook(String customEnchantGlint, Consumer<Integer> renderModelCallback) {
		if (customEnchantGlint != null) {
			int colour = ChromaColour.specialToChromaRGB(customEnchantGlint);

			float[] hsv = Color.RGBtoHSB((colour >> 16) & 0xff, (colour >> 8) & 0xff, colour & 0xff, null);
			GL14.glBlendColor(1, 1, 1, hsv[2]);

			GL11.glPushMatrix();

			GlStateManager.tryBlendFuncSeparate(
				GL11.GL_ZERO,
				GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ZERO,
				GL11.GL_ONE_MINUS_SRC_ALPHA
			);
			int alphaValue = (int) ((1 - hsv[2] * hsv[2]) * 0xff) * ((colour >> 24) & 0xff) / 0xff;
			renderEffect(renderModelCallback, alphaValue << 24);
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
			renderEffect(renderModelCallback, colour);

			GL11.glPopMatrix();

			return true;
		}
		return false;
	}

	public static ResourceLocation getCustomGlintTexture() {
		if (!loadedCustomGlintTexture) {
			loadedCustomGlintTexture = true;

			final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

			try {
				BufferedImage originalGlint = ImageIO.read(Minecraft
					.getMinecraft()
					.getResourceManager()
					.getResource(RES_ITEM_GLINT)
					.getInputStream());
				BufferedImage newGlint = new BufferedImage(
					originalGlint.getWidth(),
					originalGlint.getHeight(),
					BufferedImage.TYPE_INT_ARGB
				);

				for (int x = 0; x < originalGlint.getWidth(); x++) {
					for (int y = 0; y < originalGlint.getHeight(); y++) {
						int argb = originalGlint.getRGB(x, y);

						int avgRGB = ((((argb >> 16) & 0xff) + ((argb >> 8) & 0xff) + (argb & 0xff)) / 3) & 0xff;

						int newArgb = (avgRGB << 24) | (avgRGB << 16) | (avgRGB << 8) | avgRGB;

						newGlint.setRGB(x, y, newArgb);
					}
				}

				Minecraft.getMinecraft().getTextureManager().loadTexture(CUSTOM_GLINT_TEXTURE, new DynamicTexture(newGlint));
			} catch (Exception e) {
				e.printStackTrace();
				CUSTOM_GLINT_TEXTURE = RES_ITEM_GLINT;
			}
		}
		return CUSTOM_GLINT_TEXTURE;
	}

	public static ItemData getDataForItem(ItemStack stack) {
		if (stack == null) return null;

		String uuid = NEUManager.getUUIDForItem(stack);

		if (uuid == null) {
			return null;
		} else {
			return itemDataMap.itemData.get(uuid);
		}
	}

	public static void tick() {
		disableTextureBinding = false;
	}

	public static void loadCustomization(File file) {
		try (
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file),
				StandardCharsets.UTF_8
			))
		) {
			itemDataMap = GSON.fromJson(reader, ItemDataMap.class);
		} catch (Exception ignored) {
		}
		if (itemDataMap == null) {
			itemDataMap = new ItemDataMap();
		}
	}

	public static void saveCustomization(File file) {
		try {
			file.createNewFile();
			try (
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file),
					StandardCharsets.UTF_8
				))
			) {
				writer.write(GSON.toJson(itemDataMap));
			}
		} catch (Exception ignored) {
		}
	}

	public static Item getCustomItem(ItemStack stack) {
		ItemData data = getDataForItem(stack);
		if (data == null || data.customItem == null || data.customItem.length() == 0 || data.customItem.split(":").length == 0) return stack.getItem();
		Item newItem = Item.getByNameOrId(data.customItem.split(":")[0]);
		if (newItem == null) return stack.getItem();
		return newItem;
	}

	public static Item getCustomItem(ItemStack stack, String newItemString) {
		if (newItemString.split(":").length == 0) return stack.getItem();
		Item newItem = Item.getByNameOrId(newItemString.split(":")[0]);
		if (newItem == null) return stack.getItem();
		return newItem;
	}

	static Random random = new Random();
	static HashMap<Integer, Long> lastUpdate = new HashMap<>();
	static HashMap<Integer, Integer> damageMap = new HashMap<>();

	public static int getCustomItemDamage(ItemStack stack) {
		ItemData data = getDataForItem(stack);
		if (data == null || data.customItem == null || data.customItem.length() == 0) return stack.getMetadata();
		try {
			String damageString = data.customItem.split(":")[1];
			if (damageString.equals("?")) {
				ArrayList<ItemStack> list = new ArrayList<>();
				getCustomItem(stack).getSubItems(getCustomItem(stack), null, list);
				if (damageMap.get(stack.getTagCompound().hashCode()) == null || System.currentTimeMillis() - lastUpdate.get(stack.getTagCompound().hashCode()) > 250) {
					damageMap.put(stack.getTagCompound().hashCode(), random.nextInt(list.size()));

					lastUpdate.put(stack.getTagCompound().hashCode(), System.currentTimeMillis());
				}
				return damageMap.get(stack.getTagCompound().hashCode());
			}
			return Integer.parseInt(data.customItem.split(":")[1]);
		} catch (Exception e) {
			if (Item.getByNameOrId(data.defaultItem) == Items.skull && getCustomItem(stack) != Items.skull) return 0;
			return stack.getMetadata();
		}
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		damageMap.clear();
		lastUpdate.clear();
	}

	public static boolean shouldRenderLeatherColour(ItemStack stack) {
		ItemData data = getDataForItem(stack);
		if (data == null || data.customItem == null || data.customItem.length() == 0) return stack.getItem() instanceof ItemArmor &&
			((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;
		Item item = Item.getByNameOrId(data.customItem);
		if (item == null) return stack.getItem() instanceof ItemArmor &&
			((ItemArmor) stack.getItem()).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;
		return item instanceof ItemArmor &&
			((ItemArmor) item).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;
	}

	public static boolean hasCustomItem(ItemStack stack) {
		ItemData data = getDataForItem(stack);
		if (data == null || data.customItem == null || data.customItem.length() == 0 || data.defaultItem == null || data.customItem.equals(data.defaultItem) || data.customItem.split(":").length == 0) return false;
		Item item = Item.getByNameOrId(data.customItem.split(":")[0]);
		Item defaultItem = Item.getByNameOrId(data.defaultItem);
		if (item == null) {
			data.customItem = null;
			return false;
		}
		return defaultItem != item;
	}

	public static ItemStack useCustomArmour(LayerArmorBase<?> instance, EntityLivingBase entitylivingbaseIn, int armorSlot) {
		ItemStack stack = instance.getCurrentArmor(entitylivingbaseIn, armorSlot);
		if (stack == null) return stack;
		ItemStack newStack = stack.copy();
		newStack.setItem(ItemCustomizeManager.getCustomItem(newStack));
		newStack.setItemDamage(ItemCustomizeManager.getCustomItemDamage(newStack));
		if (armorSlot != 4) {
			if (newStack.getItem() instanceof ItemArmor) return newStack;
			else return stack;
		}
		return newStack;
	}

	public static ItemStack useCustomItem(ItemStack stack) {
		if (stack == null) return stack;
		if (!ItemCustomizeManager.hasCustomItem(stack)) return stack;
		ItemStack newStack = stack.copy();
		newStack.setItem(ItemCustomizeManager.getCustomItem(newStack));
		newStack.setItemDamage(ItemCustomizeManager.getCustomItemDamage(newStack));
		return newStack;
	}

	public static ItemStack setHeadArmour(EntityLivingBase instance, int i) {
		if (instance.getCurrentArmor(3) == null) return null;
		ItemStack stack = instance.getCurrentArmor(3).copy();
		stack.setItem(ItemCustomizeManager.getCustomItem(stack));
		stack.setItemDamage(ItemCustomizeManager.getCustomItemDamage(stack));
		return stack;
	}

}
