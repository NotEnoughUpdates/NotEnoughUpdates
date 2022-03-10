package io.github.moulberry.notenoughupdates.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.moulberry.notenoughupdates.NEUManager;
import io.github.moulberry.notenoughupdates.miscfeatures.entityviewer.EntityViewer;
import io.github.moulberry.notenoughupdates.miscgui.GuiItemRecipe;
import io.github.moulberry.notenoughupdates.profileviewer.Panorama;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MobLootRecipe implements NeuRecipe {

    private static final int MOB_POS_X = 38, MOB_POS_Y = 100;
    private static final int SLOT_POS_X = 82, SLOT_POS_Y = 23;

    public static class MobDrop {
        public final Ingredient drop;
        public final String chance;
        public final List<String> extra;

        public MobDrop(Ingredient drop, String chance, List<String> extra) {
            this.drop = drop;
            this.chance = chance;
            this.extra = extra;
        }
    }

    public static ResourceLocation[] PANORAMAS = new ResourceLocation[6];

    static {
        for (int i = 0; i < 6; i++)
            PANORAMAS[i] = new ResourceLocation("notenoughupdates:panoramas/unknown/panorama_" + i + ".jpg");
    }

    public static ResourceLocation BACKGROUND = new ResourceLocation("notenoughupdates", "textures/gui/mob_loot_tall.png");
    private final List<MobDrop> drops;
    private final int coins;
    private final int combatXp;
    private final int xp;
    private final String name;
    private final String render;
    private final int level;
    private EntityLivingBase entityLivingBase;

    public MobLootRecipe(List<MobDrop> drops, int level, int coins, int xp, int combatXp, String name, String render) {
        this.drops = drops;
        this.level = level;
        this.coins = coins;
        this.xp = xp;
        this.combatXp = combatXp;
        this.name = name;
        this.render = render;
    }

    public String getName() {
        return name;
    }

    public List<MobDrop> getDrops() {
        return drops;
    }

    public int getCoins() {
        return coins;
    }

    public int getCombatXp() {
        return combatXp;
    }

    public int getXp() {
        return xp;
    }

    public String getRender() {
        return render;
    }

    public synchronized EntityLivingBase getRenderEntity() {
        if (entityLivingBase == null) {
            if (render == null) return null;
            if (render.startsWith("@")) {
                entityLivingBase = EntityViewer.constructEntity(new ResourceLocation(render.substring(1)));
            } else {
                entityLivingBase = EntityViewer.constructEntity(render, Collections.emptyList());
            }
        }
        return entityLivingBase;
    }

    @Override
    public Set<Ingredient> getIngredients() {
        return Collections.emptySet();
    }

    @Override
    public Set<Ingredient> getOutputs() {
        return drops.stream().map(it -> it.drop).collect(Collectors.toSet());
    }

    @Override
    public List<RecipeSlot> getSlots() {
        List<RecipeSlot> slots = new ArrayList<>();
        for (int i = 0; i < drops.size(); i++) {
            MobDrop mobDrop = drops.get(i);
            int x = i % 5;
            int y = i / 5;
            slots.add(new RecipeSlot(
                    SLOT_POS_X + x * 16,
                    SLOT_POS_Y + y * 16,
                    mobDrop.drop.getItemStack()
            ));
        }
        return slots;
    }

    @Override
    public RecipeType getType() {
        return RecipeType.MOB_LOOT;
    }

    @Override
    public boolean shouldUseForCraftCost() {
        return false;
    }

    @Override
    public boolean hasVariableCost() {
        return true;
    }

    public static final int PANORAMA_POS_X = 13;
    public static final int PANORAMA_POS_Y = 23;
    public static final int PANORAMA_WIDTH = 50;
    public static final int PANORAMA_HEIGHT = 80;

    @Override
    public void drawExtraBackground(GuiItemRecipe gui, int mouseX, int mouseY) {
        Panorama.drawPanorama(((System.nanoTime() / 20000000000F) % 1) * 360, gui.guiLeft + PANORAMA_POS_X, gui.guiTop + PANORAMA_POS_Y, PANORAMA_WIDTH, PANORAMA_HEIGHT, 0F, 0F, PANORAMAS);
        if (getRenderEntity() != null)
            EntityViewer.renderEntity(entityLivingBase, gui.guiLeft + MOB_POS_X, gui.guiTop + MOB_POS_Y, mouseX, mouseY);
    }

    @Override
    public void drawHoverInformation(GuiItemRecipe gui, int mouseX, int mouseY) {
        if (gui.isWithinRect(mouseX, mouseY, gui.guiLeft + PANORAMA_POS_X, gui.guiTop + PANORAMA_POS_Y, PANORAMA_WIDTH, PANORAMA_HEIGHT)) {
            Utils.drawHoveringText(Arrays.asList("Hehe", name), mouseX, mouseY, gui.width, gui.height, -1, Minecraft.getMinecraft().fontRendererObj);
        }

    }

    @Override
    public JsonObject serialize() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("level", level);
        recipe.addProperty("coins", coins);
        recipe.addProperty("xp", xp);
        recipe.addProperty("combat_xp", combatXp);
        recipe.addProperty("name", name);
        recipe.addProperty("render", render);
        recipe.addProperty("type", getType().getId());
        JsonArray drops = new JsonArray();
        for (MobDrop drop : this.drops) {
            JsonObject dropObject = new JsonObject();
            dropObject.addProperty("id", drop.drop.serialize());
            JsonArray extraText = new JsonArray();
            for (String extraLine : drop.extra) {
                extraText.add(new JsonPrimitive(extraLine));
            }
            dropObject.add("extra", extraText);
            dropObject.addProperty("chance", drop.chance);
            drops.add(dropObject);
        }
        recipe.add("drops", drops);
        return recipe;
    }

    @Override
    public ResourceLocation getBackground() {
        return BACKGROUND;
    }

    public static MobLootRecipe parseRecipe(NEUManager manager, JsonObject recipe, JsonObject outputItemJson) {
        List<MobDrop> drops = new ArrayList<>();
        for (JsonElement jsonElement : recipe.getAsJsonArray("drops")) {
            if (jsonElement.isJsonPrimitive()) {
                drops.add(new MobDrop(new Ingredient(manager, jsonElement.getAsString()), null, Collections.emptyList()));
            } else {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                drops.add(
                        new MobDrop(
                                new Ingredient(manager, jsonObject.get("id").getAsString()),
                                jsonObject.has("chance") ? jsonObject.get("chance").getAsString() : null,
                                jsonObject.has("extra") ?
                                        StreamSupport.stream(jsonObject.getAsJsonArray("extra").spliterator(), false)
                                                .map(JsonElement::getAsString)
                                                .collect(Collectors.toList()) : Collections.emptyList()
                        ));
            }
        }

        return new MobLootRecipe(
                drops,
                recipe.has("level") ? recipe.get("level").getAsInt() : 0,
                recipe.has("coins") ? recipe.get("coins").getAsInt() : 0,
                recipe.has("xp") ? recipe.get("xp").getAsInt() : 0,
                recipe.has("combat_xp") ? recipe.get("combat_xp").getAsInt() : 0,
                recipe.get("name").getAsString(),
                recipe.has("render") && !recipe.get("render").isJsonNull() ? recipe.get("render").getAsString() : null
        );
    }
}
