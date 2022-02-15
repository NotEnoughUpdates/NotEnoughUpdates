package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.mixins.AccessorEntityAgeable;
import io.github.moulberry.notenoughupdates.mixins.AccessorEntityArmorStand;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityZombie;

public class AgeModifier extends EntityViewerModifier {
    @Override
    public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
        boolean baby = info.has("baby") && info.get("baby").getAsBoolean();
        if (base instanceof EntityAgeable) {
            ((AccessorEntityAgeable) base).setGrowingAgeDirect(baby ? -1 : 1);
            return base;
        }
        if (base instanceof EntityZombie) {
            ((EntityZombie) base).setChild(baby);
            return base;
        }
        if (base instanceof EntityArmorStand) {
            ((AccessorEntityArmorStand) base).setSmallDirect(baby);
            return base;
        }
        System.out.println("Cannot apply age to a non ageable entity: " + base);
        return null;
    }
}
