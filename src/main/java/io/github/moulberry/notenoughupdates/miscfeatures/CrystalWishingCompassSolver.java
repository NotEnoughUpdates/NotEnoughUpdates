package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.Line;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import io.github.moulberry.notenoughupdates.util.NEUDebugLogger;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CrystalWishingCompassSolver {
	private static final CrystalWishingCompassSolver INSTANCE = new CrystalWishingCompassSolver();
	public static CrystalWishingCompassSolver getInstance() {
		return INSTANCE;
	}

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static boolean isSkytilsPresent = false;

	// Crystal Nucleus unbreakable blocks, area coordinates reported by Hypixel server are slightly different
	private static final AxisAlignedBB NUCLEUS_BB = new AxisAlignedBB(463, 63, 460, 563, 181, 564);
	private static final double MAX_COMPASS_PARTICLE_SPREAD = 16;

	private static BlockPos prevPlayerPos;
	private long compassUsedMillis = 0;
	private Vec3 firstParticle = null;
	private Vec3 lastParticle = null;
	private double lastParticleDistanceFromFirst = 0;
	private Line firstCompassLine = null;
	private Line secondCompassLine = null;
	private Vec3 solution = null;
	private Line solutionIntersectionLine = null;

	private void resetForNewCompass() {
		compassUsedMillis = 0;
		firstParticle = null;
		lastParticle = null;
		lastParticleDistanceFromFirst = 0;
	}

	private void resetForNewTarget() {
		NEUDebugLogger.log(NEUDebugFlag.WISHING,"Resetting for new target");
		resetForNewCompass();
		firstCompassLine = null;
		secondCompassLine = null;
		solutionIntersectionLine = null;
		prevPlayerPos = null;
		solution = null;
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Unload event) {
		resetForNewTarget();
		isSkytilsPresent = Loader.isModLoaded("skytils");
	}

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!NotEnoughUpdates.INSTANCE.config.mining.wishingCompassSolver ||
			SBInfo.getInstance().getLocation() == null ||
			!SBInfo.getInstance().getLocation().equals("crystal_hollows") ||
			event.entityPlayer != mc.thePlayer ||
			(event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR &&
				event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
			) {
			return;
		}

		ItemStack heldItem = event.entityPlayer.getHeldItem();
		if (heldItem == null || heldItem.getItem() != Items.skull) {
			return;
		}

		String heldInternalName = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(heldItem);
		if (heldInternalName == null || !heldInternalName.equals("WISHING_COMPASS")) {
			return;
		}

		try {
			if (isSolved()) {
				resetForNewTarget();
			}

			// 64.0 is an arbitrary value but seems to work well
			if (prevPlayerPos != null && prevPlayerPos.distanceSq(mc.thePlayer.getPosition()) < 64.0) {
				mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
					"[NEU] Move a little further before using the wishing compass again."));
				event.setCanceled(true);
				return;
			}

			prevPlayerPos = mc.thePlayer.getPosition().getImmutable();
			compassUsedMillis = System.currentTimeMillis();
		} catch (Exception e) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] Error processing wishing compass action - see log for details"));
			e.printStackTrace();
		}
	}

	/*
	 * Processes particles if the wishing compass was used within the last 5 seconds.
	 *
	 * The first and the last particles are used to create a line for each wishing compass
	 * use that is then used to calculate the target.
	 *
	 * Once two lines have been calculated, the shortest line between the two is calculated
	 * with the midpoint on that line being the wishing compass target. The accuracy of this
	 * seems to be very high.
	 *
	 * The target location varies based on various criteria, including, but not limited to:
	 *  Topaz Crystal (Khazad-dûm)                Magma Fields
	 *  Odawa (Jungle Village)                    Jungle w/no Jungle Key in inventory
	 *  Amethyst Crystal (Jungle Temple)          Jungle w/Jungle Key in inventory
	 *  Sapphire Crystal (Lost Precursor City)    Precursor Remnants
	 *  Jade Crystal (Mines of Divan)             Mithril Deposits
	 *  King Yolkar                               Goblin Holdout without "King's Scent I" effect
	 *  Goblin Queen                              Goblin Holdout with "King's Scent I" effect
	 *  Crystal Nucleus                           All Crystals found and none placed
	 *                                            per-area structure missing, or because Hypixel.
	 *                                            Always within 1 block of X=513 Y=106 Z=551.
	 */
	public void onSpawnParticle(
		EnumParticleTypes particleType,
		double x,
		double y,
		double z
	) {
		if (!NotEnoughUpdates.INSTANCE.config.mining.wishingCompassSolver ||
			particleType != EnumParticleTypes.VILLAGER_HAPPY ||
			!SBInfo.getInstance().getLocation().equals("crystal_hollows") ||
			isSolved() ||
			System.currentTimeMillis() - compassUsedMillis > 5000) {
			return;
		}

		try {
			Vec3 particleVec = new Vec3(x, y, z);
			if (firstParticle == null) {
				firstParticle = particleVec;
				return;
			}

			double distanceFromFirst = particleVec.distanceTo(firstParticle);
			if (distanceFromFirst > MAX_COMPASS_PARTICLE_SPREAD) {
				return;
			}

			if (distanceFromFirst >= lastParticleDistanceFromFirst) {
				lastParticleDistanceFromFirst = distanceFromFirst;
				lastParticle = particleVec;
				return;
			}

			// We get here when the second repetition of particles begins.
			// Since the second repetition overlaps with the last few particles
			// of the first repetition, the last particle we capture isn't truly the last.
			// But that's OK since subsequent particles will be on the same line.
			Line line = new Line(firstParticle, lastParticle);
			if (firstCompassLine == null) {
				firstCompassLine = line;
				resetForNewCompass();
				mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
					"[NEU] Need another position to determine wishing compass target."));
				return;
			}

			secondCompassLine = line;
			solutionIntersectionLine = firstCompassLine.getIntersectionLineSegment(secondCompassLine);
			if (solutionIntersectionLine == null) {
				mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
					"[NEU] Unable to determine wishing compass target."));
				logDiagnosticData(false);
				return;
			}

			solution = solutionIntersectionLine.getMidpoint();

			if (solution.distanceTo(firstParticle) < 8) {
				mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
					"[NEU] WARNING: Solution is likely incorrect."));
				logDiagnosticData(false);
				return;
			}

			showSolution();
		} catch (Exception e) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] Exception while calculating wishing compass solution - see log for details"));
			e.printStackTrace();
		}
	}

	private boolean isSolved() {
		return solution != null;
	}

	private void showSolution() {
		if (solution == null) return;
		String description = "[NEU] Wishing compass target: ";
		String coordsText = String.format("%.0f %.0f %.0f",
			solution.xCoord,
			solution.yCoord,
			solution.zCoord);

		if (NUCLEUS_BB.isVecInside(solution)) {
			description += "Crystal Nucleus (" + coordsText + ")";
		} else {
			description += coordsText;
		}

		ChatComponentText message = new ChatComponentText(EnumChatFormatting.YELLOW + description);

		if (isSkytilsPresent) {
			ChatStyle clickEvent = new ChatStyle().setChatClickEvent(
				new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sthw add WishingTarget " + coordsText));
			clickEvent.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(
				EnumChatFormatting.YELLOW +
					"Add Skytils hollows waypoint")));
			message.setChatStyle(clickEvent);
		}

		Minecraft.getMinecraft().thePlayer.addChatMessage(message);
	}

	public void logDiagnosticData(boolean outputAlways) {
		if (SBInfo.getInstance().getLocation() == null) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] This command is not available outside SkyBlock"));
			return;
		}

		if (!NotEnoughUpdates.INSTANCE.config.mining.wishingCompassSolver)
		{
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] Wishing Compass Solver is not enabled."));
			return;
		}

		if (!outputAlways && !NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.contains(NEUDebugFlag.WISHING)) {
			return;
		}

		boolean originalDebugFlag = !NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.add(NEUDebugFlag.WISHING);

		StringBuilder diagsMessage = new StringBuilder();

		diagsMessage.append("\n");
		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Skytils Present: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(isSkytilsPresent);
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Compass Used Millis: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(compassUsedMillis);
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Compass Used Position: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((prevPlayerPos == null) ? "<NONE>" : prevPlayerPos.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("First Particle: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((firstParticle == null) ? "<NONE>" : firstParticle.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Last Particle: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((lastParticle == null) ? "<NONE>" : lastParticle.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Last Particle Distance From First: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(lastParticleDistanceFromFirst);
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("First Compass Line: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((firstCompassLine == null) ? "<NONE>" : firstCompassLine.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Second Compass Line: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((secondCompassLine == null) ? "<NONE>" : secondCompassLine.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Intersection Line: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((secondCompassLine == null) ? "<NONE>" : solutionIntersectionLine);
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Solution: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((solution == null) ? "<NONE>" : solution.toString());
		diagsMessage.append("\n");

		NEUDebugLogger.log(NEUDebugFlag.WISHING, diagsMessage.toString());

		if (!originalDebugFlag) {
			NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.remove(NEUDebugFlag.WISHING);
		}
	}
}
