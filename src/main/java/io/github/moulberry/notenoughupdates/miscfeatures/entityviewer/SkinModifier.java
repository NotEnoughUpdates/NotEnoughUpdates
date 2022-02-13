package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class SkinModifier extends EntityViewerModifier {
    @Override
    public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
        if (base instanceof GUIClientPlayer) {
            GUIClientPlayer player = (GUIClientPlayer) base;
            if (info.has("cape")) {
                player.overrideCape = new ResourceLocation(info.get("cape").getAsString());
            }
            if (info.has("skin")) {
                player.overrideSkin = new ResourceLocation(info.get("skin").getAsString());
            }
            if (info.has("slim")) {
                player.overrideIsSlim = info.get("slim").getAsBoolean();
            }
        }
        return base;
    }
}
