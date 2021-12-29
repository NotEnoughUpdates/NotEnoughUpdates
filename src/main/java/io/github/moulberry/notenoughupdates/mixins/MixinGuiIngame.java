package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.StreamerMode;
import io.github.moulberry.notenoughupdates.miscgui.InventoryStorageSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiIngame.class})
public class MixinGuiIngame {
    private static final String TARGET = "Lnet/minecraft/scoreboard/ScorePlayerTeam;" +
            "formatPlayerName(Lnet/minecraft/scoreboard/Team;Ljava/lang/String;)Ljava/lang/String;";

    @Redirect(method = "renderScoreboard", at = @At(value = "INVOKE", target = TARGET))
    public String renderScoreboard_formatPlayerName(Team team, String name) {
        if (NotEnoughUpdates.INSTANCE.isOnSkyblock() && NotEnoughUpdates.INSTANCE.config.misc.streamerMode) {
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
    public void renderTooltooltip_drawTexturedModelRect(GuiIngame guiIngame, int x, int y, int textureX, int textureY, int width, int height) {
        if (!InventoryStorageSelector.getInstance().isSlotSelected() || textureX != 0 || textureY != 22 || width != 24 || height != 22) {
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
}
