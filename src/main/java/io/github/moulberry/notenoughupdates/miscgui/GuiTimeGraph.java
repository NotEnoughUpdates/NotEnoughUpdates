package io.github.moulberry.notenoughupdates.miscgui;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class GuiTimeGraph extends GuiScreen {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
    private final ResourceLocation TEXTURE;
    private static final int X_SIZE = 364;
    private static final int Y_SIZE = 215;
    private boolean itemNotFound = false;
    private TreeMap<Long, Long> dataPoints;
    private long highestValue;
    private long firstTime;
    private long lastTime;
    private Long lowestValue = null;
    private String itemName;
    private final String itemId;
    private int guiLeft;
    private int guiTop;
    private ItemStack itemStack = null;
    /**
     * 0 = hour
     * 1 = day
     * 2 = week
     * 3 = all
     **/
    private int mode = 1;

    public GuiTimeGraph(String itemId) {
        switch (NotEnoughUpdates.INSTANCE.config.ahGraph.graphStyle) {
            case 1:
                TEXTURE = new ResourceLocation("notenoughupdates:price_information_gui_dark.png");
                break;
            case 2:
                TEXTURE = new ResourceLocation("notenoughupdates:price_information_gui_phqdark.png");
                break;
            case 3:
                TEXTURE = new ResourceLocation("notenoughupdates:price_information_gui_fsr.png");
                break;
            default:
                TEXTURE = new ResourceLocation("notenoughupdates:price_information_gui.png");
                break;
        }
        this.itemId = itemId;
        if (NotEnoughUpdates.INSTANCE.manager.getItemInformation().containsKey(itemId)) {
            JsonObject itemInfo = NotEnoughUpdates.INSTANCE.manager.getItemInformation().get(itemId);
            itemName = itemInfo.get("displayname").getAsString();
            itemStack = NotEnoughUpdates.INSTANCE.manager.jsonToStack(itemInfo);
        }
        loadData();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        guiLeft = (width - X_SIZE) / 2;
        guiTop = (height - Y_SIZE) / 2;

        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        Utils.drawTexturedRect(guiLeft, guiTop, X_SIZE, Y_SIZE,
                0, X_SIZE / 512f, 0, Y_SIZE / 512f, GL11.GL_NEAREST);
        Utils.drawTexturedRect(guiLeft + 245, guiTop + 17, 16, 16,
                0, 16 / 512f, (mode == 0 ? 215 : 231) / 512f, (mode == 0 ? 231 : 247) / 512f, GL11.GL_NEAREST);
        Utils.drawTexturedRect(guiLeft + 263, guiTop + 17, 16, 16,
                16 / 512f, 32 / 512f, (mode == 1 ? 215 : 231) / 512f, (mode == 1 ? 231 : 247) / 512f, GL11.GL_NEAREST);
        Utils.drawTexturedRect(guiLeft + 281, guiTop + 17, 16, 16,
                32 / 512f, 48 / 512f, (mode == 2 ? 215 : 231) / 512f, (mode == 2 ? 231 : 247) / 512f, GL11.GL_NEAREST);
        Utils.drawTexturedRect(guiLeft + 299, guiTop + 17, 16, 16,
                48 / 512f, 64 / 512f, (mode == 3 ? 215 : 231) / 512f, (mode == 3 ? 231 : 247) / 512f, GL11.GL_NEAREST);

        if (itemName != null && itemStack != null) {
            Utils.drawItemStack(itemStack, guiLeft + 16, guiTop + 11);
            Utils.drawStringScaled(itemName, Minecraft.getMinecraft().fontRendererObj, guiLeft + 35, guiTop + 13, false,
                    new Color(255, 255, 255).getRGB(), 1.77f);
        }

        if (itemNotFound) return;
        Utils.drawGradientRect(0, guiLeft + 17, guiTop + 35, guiLeft + 315, guiTop + 198,
                new Color(30, 255, 30, 120).getRGB(), new Color(30, 255, 30, 10).getRGB());
        Integer prevX = null;
        Integer prevY = null;
        Integer lowestDist = null;
        Long lowestDistTime = null;
        for (Long time : dataPoints.keySet()) {
            Long price = dataPoints.get(time);
            int xPos = (int) map(time, firstTime, lastTime, guiLeft + 17, guiLeft + 315);
            int yPos = (int) map(price, highestValue + 10, lowestValue - 10, guiTop + 35, guiTop + 198);
            if (prevX != null) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
                GlStateManager.color(1, 1, 1, 1);
                drawTexturedQuad(prevX, prevY, xPos, yPos, xPos, guiTop + 35, prevX, guiTop + 35, 18 / 512f, 19 / 512f,
                        36 / 512f, 37 / 512f, GL11.GL_NEAREST);
                drawLine(prevX, prevY + 0.5f, xPos, yPos + 0.5f, 2, new Color(0, 255, 0).getRGB());
            }
            if (mouseX >= guiLeft + 17 && mouseX <= guiLeft + 315 && mouseY >= guiTop + 35 && mouseY <= guiTop + 198) {
                int dist = Math.abs(mouseX - xPos);
                if (lowestDist == null || dist < lowestDist) {
                    lowestDist = dist;
                    lowestDistTime = time;
                }
            }
            prevX = xPos;
            prevY = yPos;
        }
        Date firstDate = Date.from(Instant.ofEpochSecond(firstTime));
        Date lastDate = Date.from(Instant.ofEpochSecond(lastTime));
        float firstHour = firstDate.getHours() + firstDate.getMinutes() / 60f;
        float lastHour = lastDate.getHours() + lastDate.getMinutes() / 60f;
        for (int h = (int) Math.ceil(firstHour); h <= lastHour; h++) {
            Utils.drawStringCentered(String.valueOf(h), Minecraft.getMinecraft().fontRendererObj,
                    (float) map(h, firstHour, lastHour, guiLeft + 17, guiLeft + 315), guiTop + 206,
                    false, new Color(139, 139, 139).getRGB());
        }
        for (int i = 0; i <= 6; i++) {
            long price = (long) map(i, 0, 6, highestValue, lowestValue);
            String formattedPrice = formatPrice(price);
            Utils.drawStringF(formattedPrice, Minecraft.getMinecraft().fontRendererObj, guiLeft + 320,
                    (float) map(i, 0, 6, guiTop + 35, guiTop + 198)
                            - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2f,
                    false, new Color(139, 139, 139).getRGB());
        }
        if (lowestDist != null) {
            Long price = dataPoints.get(lowestDistTime);
            int xPos = (int) map(lowestDistTime, firstTime, lastTime, guiLeft + 17, guiLeft + 315);
            int yPos = (int) map(price, highestValue + 10, lowestValue - 10, guiTop + 35, guiTop + 198);

            Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1, 1, 1, 1);
            Utils.drawTexturedRect(xPos - 2.5f, yPos - 2.5f, 5, 5,
                    0, 5 / 512f, 247 / 512f, 252 / 512f, GL11.GL_NEAREST);

            drawLine(xPos, guiTop + 35, xPos, guiTop + 198, 2, new Color(16, 117, 255, 100).getRGB());
            Date date = Date.from(Instant.ofEpochSecond(lowestDistTime));
            SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMMM yyyy 'at' HH:mm");
            NumberFormat nf = NumberFormat.getInstance();
            String text = displayFormat.format(date) + ": " + nf.format(price);
            int length = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1, 1, 1, 1);
            Utils.drawTexturedRect(xPos - length / 2f - 3, guiTop + 30, 3, 16,
                    8 / 512f, 11 / 512f, 247 / 512f, 263 / 512f, GL11.GL_NEAREST);
            Utils.drawTexturedRect(xPos - length / 2f, guiTop + 30, length, 16,
                    11 / 512f, 13 / 512f, 247 / 512f, 263 / 512f, GL11.GL_NEAREST);
            Utils.drawTexturedRect(xPos + length / 2f, guiTop + 30, 3, 16,
                    13 / 512f, 16 / 512f, 247 / 512f, 263 / 512f, GL11.GL_NEAREST);
            Minecraft.getMinecraft().fontRendererObj.drawString(text, xPos - length / 2, guiTop + 35, new Color(255, 255, 255).getRGB());
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        if (mouseY >= guiTop + 17 && mouseY <= guiTop + 35 && mouseX >= guiLeft + 244 && mouseX <= guiLeft + 316) {
            int index = (mouseX - guiLeft - 245) / 18;
            switch (index) {
                case 0:
                    Gui.drawRect(guiLeft + 245, guiTop + 17, guiLeft + 261, guiTop + 33, 0x80ffffff);
                    drawHoveringText(Collections.singletonList("Show 1 Hour"), mouseX, mouseY);
                    break;
                case 1:
                    Gui.drawRect(guiLeft + 263, guiTop + 17, guiLeft + 279, guiTop + 33, 0x80ffffff);
                    drawHoveringText(Collections.singletonList("Show 1 Day"), mouseX, mouseY);
                    break;
                case 2:
                    Gui.drawRect(guiLeft + 281, guiTop + 17, guiLeft + 297, guiTop + 33, 0x80ffffff);
                    drawHoveringText(Collections.singletonList("Show 1 Week"), mouseX, mouseY);
                    break;
                case 3:
                    Gui.drawRect(guiLeft + 299, guiTop + 17, guiLeft + 315, guiTop + 33, 0x80ffffff);
                    drawHoveringText(Collections.singletonList("Show All"), mouseX, mouseY);
                    break;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseY >= guiTop + 17 && mouseY <= guiTop + 35 && mouseX >= guiLeft + 244 && mouseX <= guiLeft + 316) {
            mode = (mouseX - guiLeft - 245) / 18;
            loadData();
        }
    }

    private void loadData() {
        File dir = new File("config/notenoughupdates/prices");
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        HashMap<Long, Long> data = new HashMap<>();
        assert files != null;
        for (File file : files) {
            HashMap<String, HashMap<Long, Long>> data2 = load(file);
            if (data2 == null || !data2.containsKey(itemId)) continue;
            data.putAll(data2.get(itemId));
        }
        if (data.isEmpty()) itemNotFound = true;
        else {
            if (mode != 3)
                data = new HashMap<>(data.entrySet().stream()
                        .filter(e -> e.getKey() > (System.currentTimeMillis() - (mode == 0 ? 3600000 : mode == 1 ? 86400000 : 604800000)) / 1000)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            if (data.isEmpty()) return;
            dataPoints = trimData(new TreeMap<>(data));
            firstTime = dataPoints.firstKey();
            lastTime = dataPoints.lastKey();
            highestValue = 0;
            lowestValue = null;
            for (long value : dataPoints.values()) {
                if (value > highestValue) {
                    highestValue = value;
                }
                if (lowestValue == null || value < lowestValue) {
                    lowestValue = value;
                }
            }
        }
    }

    public static void addToCache(JsonObject items) {
        try {
            File dir = new File("config/notenoughupdates/prices");
            if (!dir.exists() && !dir.mkdir()) return;
            Date date = new Date();
            Long epochSecond = date.toInstant().getEpochSecond();
            File file = new File(dir, "prices_" + format.format(date) + ".json");
            HashMap<String, HashMap<Long, Long>> prices = load(file);
            if (prices == null) {
                prices = new HashMap<>();
            }
            for (Map.Entry<String, JsonElement> item : items.entrySet()) {
                if (prices.containsKey(item.getKey())) {
                    prices.get(item.getKey()).put(epochSecond, item.getValue().getAsLong());
                } else {
                    HashMap<Long, Long> mapData = new HashMap<>();
                    mapData.put(epochSecond, item.getValue().getAsLong());
                    prices.put(item.getKey(), mapData);
                }
            }
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(GSON.toJson(prices));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TreeMap<Long, Long> trimData(TreeMap<Long, Long> data) {
        long first = data.firstKey();
        long last = data.lastKey();
        TreeMap<Long, Long> trimmed = new TreeMap<>();
        for (int i = 0; i < 200; i++) {
            long lowest = (long) map(i, 0, 200, first, last);
            long highest = (long) map(i + 1, 0, 200, first, last);
            int amount = 0;
            long sum = 0;
            for (long key : data.keySet()) {
                if (key >= lowest && key <= highest) {
                    amount++;
                    sum += data.get(key);
                } else if (key > highest)
                    break;
            }
            if (amount > 0)
                trimmed.put((lowest + highest) / 2, sum / amount);
        }
        return trimmed;
    }

    private static void drawLine(float sx, float sy, float ex, float ey, int width, int color) {
        float f = (float) (color >> 24 & 255) / 255.0F;
        float f1 = (float) (color >> 16 & 255) / 255.0F;
        float f2 = (float) (color >> 8 & 255) / 255.0F;
        float f3 = (float) (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        GL11.glLineWidth(width);
        GL11.glBegin(2);
        GL11.glVertex2d(sx, sy);
        GL11.glVertex2d(ex, ey);
        GL11.glEnd();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void drawTexturedQuad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4,
                                             float uMin, float uMax, float vMin, float vMax, int filter) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer
                .pos(x1, y1, 0.0D)
                .tex(uMin, vMax).endVertex();
        worldrenderer
                .pos(x2, y2, 0.0D)
                .tex(uMax, vMax).endVertex();
        worldrenderer
                .pos(x3, y3, 0.0D)
                .tex(uMax, vMin).endVertex();
        worldrenderer
                .pos(x4, y4, 0.0D)
                .tex(uMin, vMin).endVertex();
        tessellator.draw();

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GlStateManager.disableBlend();
    }

    private static HashMap<String, HashMap<Long, Long>> load(File file) {
        Type type = new TypeToken<HashMap<String, HashMap<Long, Long>>>() {
        }.getType();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                return GSON.fromJson(reader, type);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private static String formatPrice(long price) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (price >= 1000000000) {
            return df.format(price / 1000000000f) + "B";
        } else if (price >= 1000000) {
            return df.format(price / 1000000f) + "M";
        } else if (price >= 1000) {
            return df.format(price / 1000f) + "K";
        }
        return String.valueOf(price);
    }
}
