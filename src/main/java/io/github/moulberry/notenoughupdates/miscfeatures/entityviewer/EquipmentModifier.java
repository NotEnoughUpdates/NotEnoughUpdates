package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class EquipmentModifier extends EntityViewerModifier {
    @Override
    public EntityLivingBase applyModifier(EntityLivingBase base, JsonObject info) {
        NEUManager manager = NotEnoughUpdates.INSTANCE.manager;
        if (info.has("hand"))
            setCurrentItemOrArmor(base, 0, manager.createItem(info.get("hand").getAsString()));
        if (info.has("helmet"))
            setCurrentItemOrArmor(base, 4, manager.createItem(info.get("helmet").getAsString()));
        if (info.has("chestplate"))
            setCurrentItemOrArmor(base, 3, manager.createItem(info.get("chestplate").getAsString()));
        if (info.has("leggings"))
            setCurrentItemOrArmor(base, 2, manager.createItem(info.get("leggings").getAsString()));
        if (info.has("feet"))
            setCurrentItemOrArmor(base, 1, manager.createItem(info.get("feet").getAsString()));
        return base;
    }

    public void setCurrentItemOrArmor(EntityLivingBase entity, int slot, ItemStack itemStack) {
        if (entity instanceof EntityPlayer) {
            setPlayerCurrentItemOrArmor((EntityPlayer) entity, slot, itemStack);
        } else {
            entity.setCurrentItemOrArmor(slot, itemStack);
        }
    }

    // Biscuit person needs to learn how to code and not fuck up valid vanilla behaviour
    public static void setPlayerCurrentItemOrArmor(EntityPlayer player, int slot, ItemStack itemStack) {
        if (slot == 0) {
            player.inventory.mainInventory[player.inventory.currentItem] = itemStack;
        } else {
            player.inventory.armorInventory[slot - 1] = itemStack;
        }
    }

}
