package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscfeatures.SlotLocking;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
	@Inject(method = "dropOneItem", at = @At("HEAD"), cancellable = true)
	public void dropOneItem(CallbackInfoReturnable<EntityItem> ci) {
		if (SBInfo.getInstance().isInDungeon) {
			return;
		}

		int slot = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
		if (SlotLocking.getInstance().isSlotIndexLocked(slot) || SlotLocking.getInstance().isSwapedSlotLocked()) {
			ci.cancel();
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"NotEnoughUpdates has prevented you from dropping that locked item!"));
		}
	}
}
