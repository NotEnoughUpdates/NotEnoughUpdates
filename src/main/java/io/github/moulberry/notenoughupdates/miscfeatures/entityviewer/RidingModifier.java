package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;

public class RidingModifier extends EntityViewerModifier {
    @Override
    public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
        EntityLivingBase newEntity = EntityViewer.constructEntity(info);
        if (newEntity == null) return null;
        newEntity.mountEntity(base);
        return base;
    }
}
