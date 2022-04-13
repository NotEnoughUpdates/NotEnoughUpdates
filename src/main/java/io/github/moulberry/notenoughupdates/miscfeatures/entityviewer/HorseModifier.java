package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HorseModifier extends EntityViewerModifier {
	@Override
	public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
		if (!(base instanceof EntityHorse))
			return null;
		EntityHorse horse = (EntityHorse) base;
		if (info.has("type")) {
			String type = info.get("type").getAsString().intern();
			switch (type) {
				case "skeleton":
					horse.setHorseType(4);
					break;
				case "zombie":
					horse.setHorseType(3);
					break;
				case "mule":
					horse.setHorseType(2);
					break;
				case "donkey":
					horse.setHorseType(1);
					break;
				case "horse":
					horse.setHorseType(0);
					break;
				default:
					throw new IllegalArgumentException("Unknown horse type: " + type);
			}
		}
		if (info.has("armor")) {
			JsonElement el = info.get("type");
			if (el.isJsonNull()) {
				horse.setHorseArmorStack(null);
			} else {
				Item item;
				switch (el.getAsString().intern()) {
					case "iron":
						item = Items.iron_horse_armor;
						break;
					case "golden":
						item = Items.golden_horse_armor;
						break;
					case "diamond":
						item = Items.diamond_horse_armor;
						break;
					default:
						throw new IllegalArgumentException("Unknown horse armor: " + el.getAsString());
				}
				horse.setHorseArmorStack(new ItemStack(item));
			}
		}
		if (info.has("saddled")) {
			horse.setHorseSaddled(info.get("saddled").getAsBoolean());
		}
		return horse;
	}
}
