package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;

public class ChargedModifier extends EntityViewerModifier {

    @Override
    public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
        if (base instanceof EntityCreeper) {
            base.getDataWatcher().updateObject(17, (byte) 1);
            return base;
        }
        return null;
    }
}
