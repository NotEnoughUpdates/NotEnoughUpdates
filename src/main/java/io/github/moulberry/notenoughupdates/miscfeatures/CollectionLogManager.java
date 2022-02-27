package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.collectionlog.CollectionConstant;
import io.github.moulberry.notenoughupdates.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;

import java.util.regex.Matcher;

public class CollectionLogManager {
	private static final CollectionLogManager INSTANCE = new CollectionLogManager();

	public static CollectionLogManager getInstance() {
		return INSTANCE;
	}

	public void onEntityMetadataUpdated(int entityId) {
		System.out.println("entity created:" + entityId);
		WorldClient world = Minecraft.getMinecraft().theWorld;
		if (world != null) {
			Entity entity = world.getEntityByID(entityId);

			if (entity instanceof EntityArmorStand && entity.hasCustomName()) {
				String customName = entity.getName();
				System.out.println("got name:" + customName);
				for (CollectionConstant.DropEntry entry : Constants.COLLECTIONLOG.dropdata) {
					System.out.println("iter entry");
					if (entry.type.equalsIgnoreCase("itemdrop")) {
						Matcher matcher = entry.regex.matcher(customName);
						if (matcher.matches()) {
							System.out.println("Match found!");
							System.out.println("Count: " + matcher.group("count"));
							System.out.println("Name: " + matcher.group("itemname"));
						} else {
							System.out.println("Doesn't match: " + customName);
						}
					}
				}
			}
		}
	}
}
