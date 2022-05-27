package io.github.moulberry.notenoughupdates.miscgui;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.mixins.AccessorGuiContainer;
import io.github.moulberry.notenoughupdates.util.Utils;
import io.github.moulberry.notenoughupdates.util.XPInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class KatSitterOverlay {
	public KatSitterOverlay() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onGuiDrawn(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (!(event.gui instanceof GuiChest)) return;
		if (!NotEnoughUpdates.INSTANCE.config.petOverlay.showKatSitting) return;
		GuiChest gui = (GuiChest) event.gui;
		ContainerChest container = (ContainerChest) gui.inventorySlots;
		if (!"Pet Sitter".equals(container.getLowerChestInventory().getDisplayName().getUnformattedText())) return;
		Slot slot = container.getSlot(13);
		if (slot == null || !slot.getHasStack() || slot.getStack() == null) return;
		ItemStack item = slot.getStack();
		NBTTagCompound tagCompound = item.getTagCompound();
		if (tagCompound == null || !tagCompound.hasKey("ExtraAttributes", 10)) return;
		NBTTagCompound extra = tagCompound.getCompoundTag("ExtraAttributes");
		if (extra == null || !extra.hasKey("id", 8) ||
			!"PET".equals(extra.getString("id")) || !extra.hasKey("petInfo", 8))
			return;
		JsonObject petInfo = NotEnoughUpdates.INSTANCE.manager.gson.fromJson(extra.getString("petInfo"), JsonObject.class);
		if (petInfo == null || !petInfo.has("exp") || !petInfo.has("tier") || !petInfo.has("type")) return;
		String petId = petInfo.get("type").getAsString();
		double xp = petInfo.get("exp").getAsDouble();
		String rarity = petInfo.get("tier").getAsString();
		Slot katSlot = container.getSlot(22);
		String upgradedRarity = nextRarity(rarity);
		boolean nextRarityPresent = katSlot.getStack() != null && katSlot.getStack().getItem() != Item.getItemFromBlock(
			Blocks.barrier) && upgradedRarity != null;
		renderPetInformation(
			(int) XPInformation.getInstance().getPetLevel(petId, xp, rarity),
			nextRarityPresent ? (int) XPInformation.getInstance().getPetLevel(petId, xp, upgradedRarity) : null,
			gui
		);
	}

	public void renderPetInformation(int currentLevel, Integer upgradedLevel, GuiChest gui) {
		FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
		String currentText = "Current pet level: " + currentLevel;
		int currentWidth = font.getStringWidth(currentText);
		String upgradedText = "Upgraded pet level: " + upgradedLevel;
		int upgradedWidth = font.getStringWidth(upgradedText);
		int left = ((AccessorGuiContainer)gui).getGuiLeft() - 30 - (upgradedLevel == null ? Math.max(upgradedWidth, currentWidth) : currentWidth);
		GlStateManager.disableLighting();
		GlStateManager.color(1F, 1F, 1F, 1F);
		Utils.drawStringScaled(currentText, font, left, ((AccessorGuiContainer)gui).getGuiTop() + 25, false, 0xFFD700, 1F);
		if (upgradedLevel != null)
			Utils.drawStringScaled(upgradedText, font, left, ((AccessorGuiContainer)gui).getGuiTop() + 45, false, 0xFFD700, 1F);
	}

	public String nextRarity(String currentRarity) {
		switch (currentRarity.intern()) {
			case "COMMON":
				return "UNCOMMON";
			case "UNCOMMON":
				return "RARE";
			case "RARE":
				return "EPIC";
			case "EPIC":
				return "LEGENDARY";
			case "LEGENDARY":
				return "MYTHIC";
		}
		return null;
	}

}
