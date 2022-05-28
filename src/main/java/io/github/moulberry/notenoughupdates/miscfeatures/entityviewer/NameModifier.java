package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;

public class NameModifier extends EntityViewerModifier {
	@Override
	public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
		if (base instanceof GUIClientPlayer) {
			((GUIClientPlayer) base).setName(info.get("name").getAsString());
		}
		base.setCustomNameTag(info.get("name").getAsString());
		return base;
	}
}
