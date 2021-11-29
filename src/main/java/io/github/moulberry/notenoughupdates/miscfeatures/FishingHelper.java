package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class FishingHelper {

    private static final FishingHelper INSTANCE = new FishingHelper();

    public static FishingHelper getInstance() {
        return INSTANCE;
    }

    public static class WakeChain {
        public int particleNum = 0;
        public long lastUpdate;
        public double currentAngle;
        public double currentX;
        public double currentZ;

        public final HashMap<Integer, Double> distances = new HashMap<>();

        public WakeChain(long lastUpdate, double currentAngle, double currentX, double currentZ) {
            this.lastUpdate = lastUpdate;
            this.currentAngle = currentAngle;
            this.currentX = currentX;
            this.currentZ = currentZ;
        }
    }

    public enum PlayerWarningState {
        NOTHING,
        FISH_INCOMING,
        FISH_HOOKED
    }

    public PlayerWarningState warningState = PlayerWarningState.NOTHING;
    private int hookedWarningStateTicks = 0;

    public final HashMap<Integer, EntityFishHook> hookEntities = new HashMap<>();
    public final HashMap<WakeChain, List<Integer>> chains = new HashMap<>();

    private long lastCastRodMillis = 0;
    private int pingDelayTicks = 0;
    private final List<Integer> pingDelayList = new ArrayList<>();
    private int buildupSoundDelay = 0;

    private static final ResourceLocation FISHING_WARNING_EXCLAM = new ResourceLocation("notenoughupdates:fishing_warning_exclam.png");

    public void onRenderBobber(EntityFishHook hook) {
        if (Minecraft.getMinecraft().thePlayer.fishEntity == hook && warningState != PlayerWarningState.NOTHING) {

            if (!NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarning && warningState == PlayerWarningState.FISH_INCOMING)
                return;
            if (!NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarningR && warningState == PlayerWarningState.FISH_HOOKED)
                return;

            GlStateManager.disableCull();
            GlStateManager.disableLighting();
            GL11.glDepthFunc(GL11.GL_ALWAYS);
            GlStateManager.scale(1, -1, 1);

            float offset = warningState == PlayerWarningState.FISH_HOOKED ? 0.5f : 0f;

            float centerOffset = 0.5f / 8f;
            Minecraft.getMinecraft().getTextureManager().bindTexture(FISHING_WARNING_EXCLAM);
            Utils.drawTexturedRect(centerOffset - 4f / 8f, -20 / 8f, 1f, 2f, 0 + offset, 0.5f + offset, 0, 1, GL11.GL_NEAREST);

            GlStateManager.scale(1, -1, 1);
            GL11.glDepthFunc(GL11.GL_LEQUAL);
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
        }
    }

    public void addEntity(int entityId, Entity entity) {
        if (entity instanceof EntityFishHook) {
            hookEntities.put(entityId, (EntityFishHook) entity);

            if (((EntityFishHook) entity).angler == Minecraft.getMinecraft().thePlayer) {
                long currentTime = System.currentTimeMillis();
                long delay = currentTime - lastCastRodMillis;
                if (delay > 0 && delay < 500) {
                    if (delay > 300) delay = 300;
                    pingDelayList.add(0, (int) delay);
                }
            }
        }
    }

    public void removeEntity(int entityId) {
        hookEntities.remove(entityId);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        hookEntities.clear();
        chains.clear();
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR &&
                event.entityPlayer == Minecraft.getMinecraft().thePlayer) {

            ItemStack heldItem = event.entityPlayer.getHeldItem();

            if (heldItem != null && heldItem.getItem() == Items.fishing_rod) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastCastRodMillis > 500) {
                    lastCastRodMillis = currentTime;
                }
            }

        }
    }

    private int tickCounter = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().thePlayer != null && event.phase == TickEvent.Phase.END) {
            if (buildupSoundDelay > 0) buildupSoundDelay--;

            if (NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarning || NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarningR) {
                if (Minecraft.getMinecraft().thePlayer.fishEntity != null) {
                    if (!pingDelayList.isEmpty()) {
                        while (pingDelayList.size() > 5) pingDelayList.remove(pingDelayList.size() - 1);

                        int totalMS = 0;
                        for (int delay : pingDelayList) {
                            totalMS += delay;
                        }

                        int averageMS = totalMS / pingDelayList.size();
                        pingDelayTicks = (int) Math.floor(averageMS / 50f);
                    }
                }

                if (hookedWarningStateTicks > 0) {
                    hookedWarningStateTicks--;
                    warningState = PlayerWarningState.FISH_HOOKED;
                } else {
                    warningState = PlayerWarningState.NOTHING;
                    if (Minecraft.getMinecraft().thePlayer.fishEntity != null) {
                        int fishEntityId = Minecraft.getMinecraft().thePlayer.fishEntity.getEntityId();
                        for (Map.Entry<WakeChain, List<Integer>> entry : chains.entrySet()) {
                            if (entry.getKey().particleNum >= 3 && entry.getValue().contains(fishEntityId)) {
                                warningState = PlayerWarningState.FISH_INCOMING;
                                break;
                            }
                        }
                    }
                }
            }

            if (tickCounter++ >= 20) {
                long currentTime = System.currentTimeMillis();
                tickCounter = 0;

                Set<Integer> toRemoveEnt = new HashSet<>();
                for (Map.Entry<Integer, EntityFishHook> entry : hookEntities.entrySet()) {
                    if (entry.getValue().isDead) {
                        toRemoveEnt.add(entry.getKey());
                    }
                }
                hookEntities.keySet().removeAll(toRemoveEnt);

                Set<WakeChain> toRemoveChain = new HashSet<>();
                for (Map.Entry<WakeChain, List<Integer>> entry : chains.entrySet()) {
                    if (currentTime - entry.getKey().lastUpdate > 200 ||
                            entry.getValue().isEmpty() ||
                            Collections.disjoint(entry.getValue(), hookEntities.keySet())) {
                        toRemoveChain.add(entry.getKey());
                    }
                }
                chains.keySet().removeAll(toRemoveChain);
            }
        }
    }

    private double calculateAngleFromOffsets(double xOffset, double zOffset) {
        double angleX = Math.toDegrees(Math.acos(xOffset / 0.04f));
        double angleZ = Math.toDegrees(Math.asin(zOffset / 0.04f));

        if (xOffset < 0) {
            angleZ = 180 - angleZ;
        }
        if (zOffset < 0) {
            angleX = 360 - angleX;
        }

        angleX %= 360;
        angleZ %= 360;
        if (angleX < 0) angleX += 360;
        if (angleZ < 0) angleZ += 360;

        double dist = angleX - angleZ;
        if (dist < -180) dist += 360;
        if (dist > 180) dist -= 360;

        return angleZ + dist / 2;
    }

    private boolean checkAngleWithinRange(double angle1, double angle2, double range) {
        double dist = Math.abs(angle1 - angle2);
        if (dist > 180) dist = 360 - dist;

        return dist <= range;
    }

    private enum HookPossibleRet {
        NOT_POSSIBLE,
        EITHER,
        ANGLE1,
        ANGLE2
    }

    private HookPossibleRet isHookPossible(EntityFishHook hook, double particleX, double particleY, double particleZ, double angle1, double angle2) {
        double dY = particleY - hook.posY;
        if (Math.abs(dY) > 0.5f) {
            return HookPossibleRet.NOT_POSSIBLE;
        }

        double dX = particleX - hook.posX;
        double dZ = particleZ - hook.posZ;
        double dist = Math.sqrt(dX * dX + dZ * dZ);

        if (dist < 0.2) {
            return HookPossibleRet.EITHER;
        } else {
            float angleAllowance = (float) Math.toDegrees(Math.atan2(0.03125f, dist)) * 1.5f;
            float angleHook = (float) Math.toDegrees(Math.atan2(dX, dZ));
            angleHook %= 360;
            if (angleHook < 0) angleHook += 360;

            if (checkAngleWithinRange(angle1, angleHook, angleAllowance)) {
                return HookPossibleRet.ANGLE1;
            } else if (checkAngleWithinRange(angle2, angleHook, angleAllowance)) {
                return HookPossibleRet.ANGLE2;
            }
        }
        return HookPossibleRet.NOT_POSSIBLE;
    }

    public static EnumParticleTypes type = EnumParticleTypes.BARRIER;

    private static final float ZERO_PITCH = 1.0f;
    private static final float MAX_PITCH = 0.1f;
    private static final float MAX_DISTANCE = 5f;

    private float calculatePitchFromDistance(float d) {
        if (d < 0.1f) d = 0.1f;
        if (d > MAX_DISTANCE) d = MAX_DISTANCE;

        return 1 / (d + (1 / (ZERO_PITCH - MAX_PITCH))) * (1 - d / MAX_DISTANCE) + MAX_PITCH;
    }

    public boolean onSpawnParticle(EnumParticleTypes particleType, double x, double y, double z, double xOffset, double yOffset, double zOffset) {

        if (!NotEnoughUpdates.INSTANCE.config.fishing.hideOtherPlayerAll &&
                !NotEnoughUpdates.INSTANCE.config.fishing.enableCustomParticles &&
                !NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarning &&
                !NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarningR) {
            return false;
        }
        if (hookEntities.isEmpty()) {
            return false;
        }

        if ((particleType == EnumParticleTypes.WATER_WAKE || particleType == EnumParticleTypes.SMOKE_NORMAL) && Math.abs(yOffset - 0.01f) < 0.001f) {
            double angle1 = calculateAngleFromOffsets(xOffset, -zOffset);
            double angle2 = calculateAngleFromOffsets(-xOffset, zOffset);

            final List<Integer> possibleHooks1 = new ArrayList<>();
            final List<Integer> possibleHooks2 = new ArrayList<>();

            for (EntityFishHook hook : hookEntities.values()) {
                if (hook.isDead) continue;
                if (possibleHooks1.contains(hook.getEntityId())) continue;
                if (possibleHooks2.contains(hook.getEntityId())) continue;

                HookPossibleRet ret = isHookPossible(hook, x, y, z, angle1, angle2);
                if (ret == HookPossibleRet.ANGLE1) {
                    possibleHooks1.add(hook.getEntityId());
                } else if (ret == HookPossibleRet.ANGLE2) {
                    possibleHooks2.add(hook.getEntityId());
                } else if (ret == HookPossibleRet.EITHER) {
                    possibleHooks1.add(hook.getEntityId());
                    possibleHooks2.add(hook.getEntityId());
                }
            }

            if (!possibleHooks1.isEmpty() || !possibleHooks2.isEmpty()) {
                long currentTime = System.currentTimeMillis();

                boolean isMainPlayer = false;

                boolean foundChain = false;
                for (Map.Entry<WakeChain, List<Integer>> entry : chains.entrySet()) {
                    WakeChain chain = entry.getKey();

                    if (currentTime - chain.lastUpdate > 200) continue;

                    double updateAngle;
                    List<Integer> possibleHooks;
                    if (checkAngleWithinRange(chain.currentAngle, angle1, 16)) {
                        possibleHooks = possibleHooks1;
                        updateAngle = angle1;
                    } else if (checkAngleWithinRange(chain.currentAngle, angle2, 16)) {
                        possibleHooks = possibleHooks2;
                        updateAngle = angle2;
                    } else {
                        continue;
                    }

                    if (!Collections.disjoint(entry.getValue(), possibleHooks)) {
                        HashSet<Integer> newHooks = new HashSet<>();

                        for (int hookEntityId : possibleHooks) {
                            if (entry.getValue().contains(hookEntityId) && chain.distances.containsKey(hookEntityId)) {
                                EntityFishHook entity = hookEntities.get(hookEntityId);

                                if (entity != null && !entity.isDead) {
                                    double oldDistance = chain.distances.get(hookEntityId);

                                    double dX = entity.posX - x;
                                    double dZ = entity.posZ - z;
                                    double newDistance = Math.sqrt(dX * dX + dZ * dZ);

                                    double delta = oldDistance - newDistance;

                                    if (newDistance < 0.2 || (delta > -0.1 && delta < 0.3)) {
                                        if ((NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarning || NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarningR) &&
                                                Minecraft.getMinecraft().thePlayer.fishEntity != null &&
                                                Minecraft.getMinecraft().thePlayer.fishEntity.getEntityId() == hookEntityId &&
                                                chain.particleNum > 3) {
                                            float lavaOffset = 0.1f;
                                            if (particleType == EnumParticleTypes.SMOKE_NORMAL) {
                                                lavaOffset = 0.03f;
                                            } else if (particleType == EnumParticleTypes.WATER_WAKE) {
                                                lavaOffset = 0.1f;
                                            }
                                            if (newDistance <= 0.2f + lavaOffset * pingDelayTicks && NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarningR) {
                                                if (NotEnoughUpdates.INSTANCE.config.fishing.incomingFishHookedSounds &&
                                                        hookedWarningStateTicks <= 0) {
                                                    float vol = NotEnoughUpdates.INSTANCE.config.fishing.incomingFishHookedSoundsVol / 100f;
                                                    if (vol > 0) {
                                                        if (vol > 1) vol = 1;
                                                        final float volF = vol;

                                                        ISound sound = new PositionedSound(new ResourceLocation("note.pling")) {{
                                                            volume = volF;
                                                            pitch = 2f;
                                                            repeat = false;
                                                            repeatDelay = 0;
                                                            attenuationType = ISound.AttenuationType.NONE;
                                                        }};

                                                        float oldLevel = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
                                                        Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.RECORDS, 1);
                                                        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                                                        Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.RECORDS, oldLevel);
                                                    }
                                                }

                                                hookedWarningStateTicks = 12;
                                            } else if (newDistance >= 0.4f + 0.1f * pingDelayTicks && NotEnoughUpdates.INSTANCE.config.fishing.incomingFishWarning) {
                                                if (NotEnoughUpdates.INSTANCE.config.fishing.incomingFishIncSounds &&
                                                        buildupSoundDelay <= 0) {
                                                    float vol = NotEnoughUpdates.INSTANCE.config.fishing.incomingFishIncSoundsVol / 100f;
                                                    if (vol > 0) {
                                                        if (vol > 1) vol = 1;
                                                        final float volF = vol;

                                                        ISound sound = new PositionedSound(new ResourceLocation("note.pling")) {{
                                                            volume = volF;
                                                            pitch = calculatePitchFromDistance((float) newDistance - (0.3f + 0.1f * pingDelayTicks));
                                                            repeat = false;
                                                            repeatDelay = 0;
                                                            attenuationType = ISound.AttenuationType.NONE;
                                                        }};

                                                        float oldLevel = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS);
                                                        Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.RECORDS, 1);
                                                        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
                                                        Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.RECORDS, oldLevel);
                                                        buildupSoundDelay = 4;
                                                    }
                                                }
                                            }
                                        }

                                        chain.distances.put(hookEntityId, newDistance);
                                        newHooks.add(hookEntityId);
                                    }
                                }

                            }
                        }
                        if (newHooks.isEmpty()) {
                            continue;
                        }

                        entry.getValue().retainAll(newHooks);
                        chain.distances.keySet().retainAll(newHooks);

                        for (int i : entry.getValue()) {
                            EntityFishHook hook = hookEntities.get(i);
                            if (hook != null && hook.angler == Minecraft.getMinecraft().thePlayer) {
                                isMainPlayer = true;
                                break;
                            }
                        }

                        chain.lastUpdate = currentTime;
                        chain.particleNum++;
                        chain.currentAngle = updateAngle;

                        foundChain = true;
                    }
                }

                if (!foundChain) {
                    possibleHooks1.removeAll(possibleHooks2);
                    if (!possibleHooks1.isEmpty()) {
                        for (int i : possibleHooks1) {
                            EntityFishHook hook = hookEntities.get(i);
                            if (hook != null && hook.angler == Minecraft.getMinecraft().thePlayer) {
                                isMainPlayer = true;
                                break;
                            }
                        }

                        WakeChain chain = new WakeChain(currentTime, angle1, x, z);
                        for (int hookEntityId : possibleHooks1) {
                            EntityFishHook entity = hookEntities.get(hookEntityId);

                            if (entity != null && !entity.isDead) {
                                double dX = entity.posX - x;
                                double dZ = entity.posZ - z;
                                double newDistance = Math.sqrt(dX * dX + dZ * dZ);
                                chain.distances.put(hookEntityId, newDistance);
                            }
                        }
                        chains.put(chain, possibleHooks1);
                    } else if (!possibleHooks2.isEmpty()) {
                        for (int i : possibleHooks2) {
                            EntityFishHook hook = hookEntities.get(i);
                            if (hook != null && hook.angler == Minecraft.getMinecraft().thePlayer) {
                                isMainPlayer = true;
                                break;
                            }
                        }

                        WakeChain chain = new WakeChain(currentTime, angle2, x, z);
                        for (int hookEntityId : possibleHooks2) {
                            EntityFishHook entity = hookEntities.get(hookEntityId);

                            if (entity != null && !entity.isDead) {
                                double dX = entity.posX - x;
                                double dZ = entity.posZ - z;
                                double newDistance = Math.sqrt(dX * dX + dZ * dZ);
                                chain.distances.put(hookEntityId, newDistance);
                            }
                        }
                        chains.put(chain, possibleHooks2);
                    }
                }

                int particleTypeI;
                String particleCustomColour;
                if (isMainPlayer) {
                    particleTypeI = NotEnoughUpdates.INSTANCE.config.fishing.yourParticleType;
                    particleCustomColour = NotEnoughUpdates.INSTANCE.config.fishing.yourParticleColour;
                } else if (NotEnoughUpdates.INSTANCE.config.fishing.hideOtherPlayerAll) {
                    return true;
                } else {
                    particleTypeI = NotEnoughUpdates.INSTANCE.config.fishing.otherParticleType;
                    particleCustomColour = NotEnoughUpdates.INSTANCE.config.fishing.otherParticleColour;
                }

                if (!NotEnoughUpdates.INSTANCE.config.fishing.enableCustomParticles) {
                    return false;
                }

                int argb = SpecialColour.specialToChromaRGB(particleCustomColour);

                if (particleTypeI == 0) {
                    return false;
                } else if (particleTypeI == 1) {
                    return true;
                }

                if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getRenderViewEntity() != null && Minecraft.getMinecraft().effectRenderer != null) {
                    int i = Minecraft.getMinecraft().gameSettings.particleSetting;

                    if (i == 1 && Minecraft.getMinecraft().theWorld.rand.nextInt(3) == 0) {
                        return true;
                    }

                    if (i >= 2) {
                        return true;
                    }

                    double xDist = Minecraft.getMinecraft().getRenderViewEntity().posX - x;
                    double yDist = Minecraft.getMinecraft().getRenderViewEntity().posY - y;
                    double zDist = Minecraft.getMinecraft().getRenderViewEntity().posZ - z;
                    double distSq = xDist * xDist + yDist * yDist + zDist * zDist;

                    if (distSq < 32 * 32) {
                        boolean customColour = false;
                        double yVel = 0;

                        switch (particleTypeI) {
                            case 2:
                                particleType = EnumParticleTypes.FIREWORKS_SPARK;
                                customColour = true;
                                yVel = 0.05;
                                break;
                            case 3:
                                particleType = EnumParticleTypes.SPELL_MOB;
                                customColour = true;
                                break;
                            case 4:
                                particleType = EnumParticleTypes.REDSTONE;
                                customColour = true;
                                break;
                            case 5:
                                particleType = EnumParticleTypes.FLAME;
                                yVel = 0.015;
                                break;
                            case 6:
                                particleType = EnumParticleTypes.CRIT;
                                yVel = 0.05;
                                break;
                            case 7:
                                particleType = EnumParticleTypes.CRIT_MAGIC;
                                yVel = 0.05;
                                break;
                        }

                        if (customColour && (((argb >> 24) & 0xFF) < 10)) {
                            return true;
                        }

                        EntityFX fx = Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(particleType.getParticleID(), x, y, z, 0, 0, 0);

                        fx.motionX = Math.random() * 0.02 - 0.01;
                        fx.motionY = yVel;
                        fx.motionZ = Math.random() * 0.02 - 0.01;

                        if (customColour) {
                            float red = ((argb >> 16) & 0xFF) / 255f;
                            float green = ((argb >> 8) & 0xFF) / 255f;
                            float blue = (argb & 0xFF) / 255f;
                            float alpha = ((argb >> 24) & 0xFF) / 255f;
                            fx.setRBGColorF(red, green, blue);
                            fx.setAlphaF(alpha);
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

}
