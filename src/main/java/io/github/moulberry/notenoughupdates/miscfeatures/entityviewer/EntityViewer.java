package io.github.moulberry.notenoughupdates.miscfeatures.entityviewer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EntityViewer extends GuiScreen {

    public static Map<String, Supplier<? extends EntityLivingBase>> validEntities = new HashMap<String, Supplier<? extends EntityLivingBase>>() {{
        put("Zombie", () -> new EntityZombie(null));
        put("Chicken", () -> new EntityChicken(null));
        put("Slime", () -> new EntitySlime(null));
        put("Wolf", () -> new EntityWolf(null));
        put("Skeleton", () -> new EntitySkeleton(null));
        put("Creeper", () -> new EntityCreeper(null));
        put("Ocelot", () -> new EntityOcelot(null));
        put("Blaze", () -> new EntityBlaze(null));
        put("Rabbit", () -> new EntityRabbit(null));
        put("Sheep", () -> new EntitySheep(null));
        put("Horse", () -> new EntityHorse(null));
        put("Eisengolem", () -> new EntityIronGolem(null));
        put("Silverfish", () -> new EntitySilverfish(null));
        put("Witch", () -> new EntityWitch(null));
        put("Endermite", () -> new EntityEndermite(null));
        put("Snowman", () -> new EntitySnowman(null));
        put("Villager", () -> new EntityVillager(null));
        put("Guardian", () -> new EntityGuardian(null));
        put("ArmorStand", () -> new EntityArmorStand(null));
        put("Squid", () -> new EntitySquid(null));
        put("Bat", () -> new EntityBat(null));
        put("Spider", () -> new EntitySpider(null));
        put("CaveSpider", () -> new EntityCaveSpider(null));
        put("Pigman", () -> new EntityPigZombie(null));
        put("Ghast", () -> new EntityGhast(null));
        put("MagmaCube", () -> new EntityMagmaCube(null));
        put("Wither", () -> new EntityWither(null));
        put("Enderman", () -> new EntityEnderman(null));
        put("WitherSkeleton", () -> {
            EntitySkeleton skeleton = new EntitySkeleton(null);
            skeleton.setSkeletonType(1);
            return skeleton;
        });
        put("Cow", () -> new EntityCow(null));
        put("Dragon", ()-> new EntityDragon(null));
        put("Player", () -> new GUIClientPlayer());
    }};

    public static Map<String, EntityViewerModifier> validModifiers = new HashMap<String, EntityViewerModifier>() {{
        put("playerdata", new SkinModifier());
        put("equipment", new EquipmentModifier());
        put("riding", new RidingModifier());
        put("charged", new ChargedModifier());
        put("witherdata", new WitherModifier());
        put("invisible", new InvisibleModifier());
        put("age", new AgeModifier());
    }};

    public int guiLeft = 0;
    public int guiTop = 0;
    public int xSize = 176;
    public int ySize = 166;

    private final String label;
    private final EntityLivingBase entity;
    private static final ResourceLocation BACKGROUND = new ResourceLocation("notenoughupdates", "textures/gui/entity_viewer.png");

    public EntityViewer(String label, EntityLivingBase entity) {
        this.label = label;
        this.entity = entity;
    }

    public static EntityLivingBase constructEntity(ResourceLocation resourceLocation) {
        Gson gson = NotEnoughUpdates.INSTANCE.manager.gson;
        try (Reader is = new InputStreamReader(
                Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream(), StandardCharsets.UTF_8)) {
            return constructEntity(gson.fromJson(is, JsonObject.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EntityLivingBase constructEntity(JsonObject info) {
        List<JsonObject> modifiers = info.has("modifiers") ?
                StreamSupport.stream(info.get("modifiers").getAsJsonArray().spliterator(), false)
                        .map(JsonElement::getAsJsonObject).collect(Collectors.toList())
                : Collections.emptyList();
        return EntityViewer.constructEntity(info.get("entity").getAsString(), modifiers);
    }

    public static EntityLivingBase constructEntity(String string, String[] modifiers) {
        Gson gson = NotEnoughUpdates.INSTANCE.manager.gson;
        return constructEntity(string, Arrays.stream(modifiers).map(it -> gson.fromJson(it, JsonObject.class)).collect(Collectors.toList()));
    }

    public static EntityLivingBase constructEntity(String string, List<JsonObject> modifiers) {
        Supplier<? extends EntityLivingBase> aClass = validEntities.get(string);
        if (aClass == null) {
            System.err.println("Could not find entity of type: " + string);
            return null;
        }
        try {
            EntityLivingBase entity = aClass.get();
            for (JsonObject modifier : modifiers) {
                String type = modifier.get("type").getAsString();
                EntityViewerModifier entityViewerModifier = validModifiers.get(type);
                entity = entityViewerModifier.applyModifier(entity, modifier);
                if (entity == null) break;
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        this.guiLeft = (width - this.xSize) / 2;
        this.guiTop = (height - this.ySize) / 2;


        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);


        Utils.drawStringScaledMaxWidth(label, fontRenderer, guiLeft + 10, guiTop + 10, false, 100, 0xFF00FF);
        renderEntity(entity, guiLeft + 90, guiTop + 75, mouseX, mouseY);
    }

    public static void renderEntity(EntityLivingBase entity, int posX, int posY, int mouseX, int mouseY) {
        GlStateManager.color(1F, 1F, 1F, 1F);

        int scale = 30;
        float bottomOffset = 0F;
        EntityLivingBase stack = entity;
        while (true) {

            GuiInventory.drawEntityOnScreen(posX, (int) (posY - bottomOffset * scale), scale, posX - mouseX, (int) (posY - stack.getEyeHeight() * scale - mouseY), stack);
            bottomOffset += stack.getMountedYOffset();
            if (!(stack.riddenByEntity instanceof EntityLivingBase)) {
                break;
            }
            stack = (EntityLivingBase) stack.riddenByEntity;
        }

    }
}
