package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.entity.EntityLivingBase;

public class EquipmentModifier extends EntityViewerModifier {
    @Override
    public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
        NEUManager manager = NotEnoughUpdates.INSTANCE.manager;
        if (info.has("hand"))
            base.setCurrentItemOrArmor(0, manager.createItem(info.get("hand").getAsString()));
        if (info.has("helmet"))
            base.setCurrentItemOrArmor(4, manager.createItem(info.get("helmet").getAsString()));
        if (info.has("chestplate"))
            base.setCurrentItemOrArmor(3, manager.createItem(info.get("chestplate").getAsString()));
        if (info.has("leggings"))
            base.setCurrentItemOrArmor(2, manager.createItem(info.get("leggings").getAsString()));
        if (info.has("feet"))
            base.setCurrentItemOrArmor(1, manager.createItem(info.get("feet").getAsString()));
        return base;
    }
}
