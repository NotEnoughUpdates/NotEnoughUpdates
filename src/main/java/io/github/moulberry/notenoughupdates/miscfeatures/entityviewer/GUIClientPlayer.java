package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class GUIClientPlayer extends AbstractClientPlayer {
    public GUIClientPlayer() {
        super(null, new GameProfile(UUID.randomUUID(), "GuiPlayer"));
    }

    ResourceLocation overrideSkin = DefaultPlayerSkin.getDefaultSkinLegacy();
    ResourceLocation overrideCape = null;
    boolean overrideIsSlim = false;
    NetworkPlayerInfo playerInfo = new NetworkPlayerInfo(this.getGameProfile()) {
        @Override
        public String getSkinType() {
            return overrideIsSlim ? "slim" : "default";
        }

        @Override
        public ResourceLocation getLocationSkin() {
            return overrideSkin;
        }

        @Override
        public ResourceLocation getLocationCape() {
            return overrideCape;
        }
    };

    @Override
    protected NetworkPlayerInfo getPlayerInfo() {
        return playerInfo;
    }
}
