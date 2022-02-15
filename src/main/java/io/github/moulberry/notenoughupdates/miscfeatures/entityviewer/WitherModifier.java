package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;

public class WitherModifier extends EntityViewerModifier {
    @Override
    public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
        if (!(base instanceof EntityWither))
            return null;
        EntityWither wither = (EntityWither) base;
        if (info.has("tiny")) {
            if (info.get("tiny").getAsBoolean()) {
                wither.setInvulTime(800);
            } else {
                wither.setInvulTime(0);
            }
        }
        if (info.has("armored")) {
            if (info.get("armored").getAsBoolean()) {
                wither.setHealth(1);
            } else {
                wither.setHealth(wither.getMaxHealth());
            }
        }
        return base;
    }
}
