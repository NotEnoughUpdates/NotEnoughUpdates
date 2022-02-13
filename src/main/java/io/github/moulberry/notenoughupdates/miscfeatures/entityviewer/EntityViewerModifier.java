package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;

public abstract class EntityViewerModifier {
    public abstract EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info);
}
