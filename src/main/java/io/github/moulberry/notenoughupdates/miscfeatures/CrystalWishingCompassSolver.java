package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.Line;
import io.github.moulberry.notenoughupdates.core.util.Vec3Comparable;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import io.github.moulberry.notenoughupdates.util.NEUDebugLogger;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3i;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.function.LongSupplier;

public class CrystalWishingCompassSolver {
	enum SolverState {
		NOT_STARTED,
		PROCESSING_FIRST_USE,
		NEED_SECOND_COMPASS,
		PROCESSING_SECOND_USE,
		SOLVED,
		FAILED_EXCEPTION,
		FAILED_TIMEOUT_NO_REPEATING,
		FAILED_INTERSECTION_CALCULATION,
		FAILED_INVALID_SOLUTION,
	}

	private static final CrystalWishingCompassSolver INSTANCE = new CrystalWishingCompassSolver();
	public static CrystalWishingCompassSolver getInstance() {
		return INSTANCE;
	}

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static boolean isSkytilsPresent = false;

	// Crystal Nucleus unbreakable blocks, area coordinates reported by Hypixel server are slightly different
	private static final AxisAlignedBB NUCLEUS_BB = new AxisAlignedBB(463, 63, 460, 563, 181, 564);
	private static final AxisAlignedBB HOLLOWS_BB = new AxisAlignedBB(201, 30, 201, 824, 189, 824);
	private static final double MAX_COMPASS_PARTICLE_SPREAD = 16;
	private static final int PARTICLE_SPAWN_MAX_MILLIS = 5000;
	// 64.0 is an arbitrary value but seems to work well
	private static final double MINIMUM_DISATANCESQ_BETWEEN_COMPASSES = 64.0;
	public LongSupplier currentTimeMillis = System::currentTimeMillis;

	private SolverState solverState;
	private Compass firstCompass;
	private Compass secondCompass;
	private Line solutionIntersectionLine;
	private Vec3Comparable solution;

	public SolverState getSolverState() {
		return solverState;
	}

	public Vec3i getSolutionCoords() {
		return new Vec3i(solution.xCoord, solution.yCoord, solution.zCoord);
	}

	private void resetForNewTarget() {
		NEUDebugLogger.log(NEUDebugFlag.WISHING,"Resetting for new target");
		solverState = SolverState.NOT_STARTED;
		firstCompass = null;
		secondCompass = null;
		solutionIntersectionLine = null;
		solution = null;
	}

	public void initWorld() {
		resetForNewTarget();
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Unload event) {
		initWorld();
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

		BlockPos playerPos = mc.thePlayer.getPosition().getImmutable();

		try {
			HandleCompassResult result = handleCompassUse(playerPos);
			switch (result) {
				case SUCCESS:
					return;
				case USED_TOO_SOON:
				case STILL_PROCESSING_FIRST_USE:
					mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
						"[NEU] Wait a little longer before using the wishing compass again."));
					event.setCanceled(true);
					break;
				case LOCATION_TOO_CLOSE:
					mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
						"[NEU] Move a little further before using the wishing compass again."));
					event.setCanceled(true);
					break;
				default:
					throw new IllegalStateException("Unexpected wishing compass solver state: \n" + getDiagnosticMessage());
			}
		} catch (Exception e) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] Error processing wishing compass action - see log for details"));
			e.printStackTrace();
			event.setCanceled(true);
			solverState = SolverState.FAILED_EXCEPTION;
		}
	}

	public HandleCompassResult handleCompassUse(BlockPos playerPos) {
			switch (solverState) {
				case PROCESSING_FIRST_USE:
				case PROCESSING_SECOND_USE:
					return HandleCompassResult.STILL_PROCESSING_FIRST_USE;
				case SOLVED:
				case FAILED_EXCEPTION:
				case FAILED_TIMEOUT_NO_REPEATING:
				case FAILED_INTERSECTION_CALCULATION:
				case FAILED_INVALID_SOLUTION:
					resetForNewTarget();
					// falls through, NOT_STARTED is the state when resetForNewTarget returns
				case NOT_STARTED:
					firstCompass = new Compass(playerPos, currentTimeMillis.getAsLong());
					solverState = SolverState.PROCESSING_FIRST_USE;
					return HandleCompassResult.SUCCESS;
				case NEED_SECOND_COMPASS:
					if (currentTimeMillis.getAsLong() - firstCompass.whenUsedMillis < PARTICLE_SPAWN_MAX_MILLIS) {
						return HandleCompassResult.USED_TOO_SOON;
					}

					if (firstCompass.whereUsed.distanceSq(playerPos) < MINIMUM_DISATANCESQ_BETWEEN_COMPASSES) {
						return HandleCompassResult.LOCATION_TOO_CLOSE;
					}
					secondCompass = new Compass(playerPos, currentTimeMillis.getAsLong());
					solverState = SolverState.PROCESSING_SECOND_USE;
					return HandleCompassResult.SUCCESS;
			}

		throw new IllegalStateException("Unexpected compass state" );
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
			!SBInfo.getInstance().getLocation().equals("crystal_hollows")) {
			return;
		}

		try {
			SolverState originalSolverState = solverState;
			solveUsingParticle(x, y, z, currentTimeMillis.getAsLong());
			if (solverState != originalSolverState) {
				switch (solverState) {
					case SOLVED:
						showSolution();
						break;
					case FAILED_EXCEPTION:
						mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
							"[NEU] Unable to determine wishing compass target."));
						logDiagnosticData(false);
						break;
					case FAILED_TIMEOUT_NO_REPEATING:
						mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
							"[NEU] Timed out waiting for second set of wishing compass particles."));
						logDiagnosticData(false);
						break;
					case FAILED_INTERSECTION_CALCULATION:
						mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
							"[NEU] Unable to determine intersection of wishing compasses."));
						logDiagnosticData(false);
						break;
					case FAILED_INVALID_SOLUTION:
						mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
							"[NEU] Failed to find solution."));
						logDiagnosticData(false);
						break;
					case NEED_SECOND_COMPASS:
							mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
								"[NEU] Need another position to determine wishing compass target."));
							break;
				}
			}
		} catch (Exception e) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] Exception while calculating wishing compass solution - see log for details"));
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param x Particle x coordinate
	 * @param y Particle y coordinate
	 * @param z Particle z coordinate
	 */
	public void solveUsingParticle(double x, double y, double z, long currentTimeMillis) {
		Compass currentCompass;
		switch (solverState) {
			case PROCESSING_FIRST_USE:
				currentCompass = firstCompass;
				break;
			case PROCESSING_SECOND_USE:
				currentCompass = secondCompass;
				break;
			default:
				return;
		}

		currentCompass.processParticle(x, y, z, currentTimeMillis);
		switch (currentCompass.compassState) {
			case FAILED_TIMEOUT_NO_REPEATING:
				solverState = SolverState.FAILED_TIMEOUT_NO_REPEATING;
				return;
			case WAITING_FOR_FIRST_PARTICLE:
			case COMPUTING_LAST_PARTICLE:
				return;
			case COMPLETED:
				if (solverState == SolverState.PROCESSING_FIRST_USE) {
					solverState = SolverState.NEED_SECOND_COMPASS;
					return;
				}
				break;
		}

		// First and Second compasses have completed
		solutionIntersectionLine = firstCompass.line.getIntersectionLineSegment(secondCompass.line);

		if (solutionIntersectionLine == null) {
			solverState = SolverState.FAILED_INTERSECTION_CALCULATION;
			return;
		}


		solution = new Vec3Comparable(solutionIntersectionLine.getMidpoint());

		Vec3Comparable firstDirection = firstCompass.getDirection();
		Vec3Comparable firstSolutionDirection = firstCompass.getDirectionTo(solution);
		Vec3Comparable secondDirection = secondCompass.getDirection();
		Vec3Comparable secondSolutionDirection = secondCompass.getDirectionTo(solution);
		if (!firstDirection.signumEquals(firstSolutionDirection) ||
			!secondDirection.signumEquals(secondSolutionDirection) ||
			!HOLLOWS_BB.isVecInside(solution)) {
			solverState = SolverState.FAILED_INVALID_SOLUTION;
			return;
		}

		solverState = SolverState.SOLVED;
	}

	private void showSolution() {
		if (solution == null) return;

		boolean isNucleus = NUCLEUS_BB.isVecInside(solution);
		String description = EnumChatFormatting.YELLOW + "[NEU] Wishing compass target: " + EnumChatFormatting.WHITE;
		String coordsText = String.format("%.0f %.0f %.0f",
			solution.xCoord,
			solution.yCoord,
			solution.zCoord);

		if (isNucleus) {
			description += "Crystal Nucleus (" + coordsText + ")";
		} else {
			description += coordsText;
		}

		ChatComponentText message;

		if (isSkytilsPresent && !isNucleus) {
			String command = "/sthw add WishingTarget " + coordsText;
			message = new ChatComponentText(description + EnumChatFormatting.YELLOW + " [Add Skytils Waypoint]");
			ChatStyle clickEvent =
				Utils.createClickStyle(ClickEvent.Action.SUGGEST_COMMAND,
					command,
					EnumChatFormatting.YELLOW + "Set waypoint for Wishing Target\n" +
						EnumChatFormatting.GRAY + "Suggests command " +
						EnumChatFormatting.YELLOW + command +
						EnumChatFormatting.GRAY + " on click.");
			message.setChatStyle(clickEvent);
		} else {
			message = new ChatComponentText(description);
		}

		Minecraft.getMinecraft().thePlayer.addChatMessage(message);
	}

	private String getDiagnosticMessage() {
		StringBuilder diagsMessage = new StringBuilder();

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Solver State: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(solverState.name());
		diagsMessage.append("\n");

		if (firstCompass == null) {
			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append("First Compass: ");
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append("<NONE>");
			diagsMessage.append("\n");
		} else {
			firstCompass.appendCompassDiagnostics(diagsMessage, "First Compass");
		}

		if (secondCompass == null) {
			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append("Second Compass: ");
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append("<NONE>");
			diagsMessage.append("\n");
		} else {
			secondCompass.appendCompassDiagnostics(diagsMessage, "Second Compass");
		}

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Intersection Line: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((solutionIntersectionLine == null) ? "<NONE>" : solutionIntersectionLine);
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Solution: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((solution == null) ? "<NONE>" : solution.toString());
		diagsMessage.append("\n");

		return diagsMessage.toString();
	}

	public void logDiagnosticData(boolean outputAlways) {
		if (!SBInfo.getInstance().checkForSkyblockLocation()) {
			return;
		}

		if (!NotEnoughUpdates.INSTANCE.config.mining.wishingCompassSolver)
		{
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] Wishing Compass Solver is not enabled."));
			return;
		}

		boolean wishingDebugFlagSet = NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.contains(NEUDebugFlag.WISHING);
		if (outputAlways || wishingDebugFlagSet) {
			NEUDebugLogger.logAlways(getDiagnosticMessage());
		}
	}

	enum CompassState {
		WAITING_FOR_FIRST_PARTICLE,
		COMPUTING_LAST_PARTICLE,
		COMPLETED,
		FAILED_TIMEOUT_NO_REPEATING,
	}

	enum HandleCompassResult {
		SUCCESS,
		LOCATION_TOO_CLOSE,
		STILL_PROCESSING_FIRST_USE,
		USED_TOO_SOON,
	}

	static class Compass {
		public CompassState compassState;
		public Line line = null;

		private final BlockPos whereUsed;
		private final long whenUsedMillis;
		private Vec3Comparable firstParticle = null;
		private Vec3Comparable lastParticle = null;
		private ArrayList<ProcessedParticle> processedParticles;

		Compass(BlockPos whereUsed, long whenUsedMillis) {
			this.whereUsed = whereUsed;
			this.whenUsedMillis = whenUsedMillis;
			compassState = CompassState.WAITING_FOR_FIRST_PARTICLE;
			processedParticles = new ArrayList<>();
		}

		public Vec3Comparable getDirection() {
			if (firstParticle == null || lastParticle == null) {
				return null;
			}

			return new Vec3Comparable(firstParticle.subtractReverse(lastParticle).normalize());
		}

		public Vec3Comparable getDirectionTo(Vec3Comparable target) {
			if (firstParticle == null || target == null) {
				return null;
			}

			return new Vec3Comparable(firstParticle.subtractReverse(target).normalize());
		}

		public double particleSpread() {
			if (firstParticle == null || lastParticle == null) {
				return 0.0;
			}
			return firstParticle.distanceTo(lastParticle);
		}

		public void processParticle(double x, double y, double z, long particleTimeMillis) {
			processedParticles.add(new ProcessedParticle(new Vec3Comparable(x, y, z), particleTimeMillis));

			if (compassState != CompassState.WAITING_FOR_FIRST_PARTICLE &&
				compassState != CompassState.COMPUTING_LAST_PARTICLE) {
				throw new UnsupportedOperationException("processParticle should not be called in a final state");
			}

			if (particleTimeMillis - this.whenUsedMillis > PARTICLE_SPAWN_MAX_MILLIS) {
				// Assume we have failed if we're still trying to process particles
				compassState = CompassState.FAILED_TIMEOUT_NO_REPEATING;
				return;
			}

			Vec3Comparable particleVec = new Vec3Comparable(x, y, z);
			if (compassState == CompassState.WAITING_FOR_FIRST_PARTICLE) {
				firstParticle = particleVec;
				compassState = CompassState.COMPUTING_LAST_PARTICLE;
				return;
			}

			// State is COMPUTING_LAST_PARTICLE
			double distanceFromFirst = particleVec.distanceTo(firstParticle);
			// ignore particles that are too far away
			if (distanceFromFirst > MAX_COMPASS_PARTICLE_SPREAD) {
				return;
			}

			if (distanceFromFirst >= particleSpread()) {
				lastParticle = particleVec;
				return;
			}

			// We get here when the second repetition of particles begins.
			// Since the second repetition overlaps with the last few particles
			// of the first repetition, the last particle we capture isn't truly the last.
			// But that's OK since subsequent particles would not change the resulting line.
			line = new Line(firstParticle, lastParticle);
			compassState = CompassState.COMPLETED;
		}

		public void appendCompassDiagnostics(StringBuilder diagsMessage, String compassName) {
			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append("Compass State: ");
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append(compassState.name());
			diagsMessage.append("\n");

			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append(compassName);
			diagsMessage.append(" Used Millis: ");
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append(whenUsedMillis);
			diagsMessage.append("\n");

			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append(compassName);
			diagsMessage.append(" Used Position: ");
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append((whereUsed == null) ? "<NONE>" : whereUsed.toString());
			diagsMessage.append("\n");

			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append(compassName);
			diagsMessage.append(" All Seen Particles: \n");
			diagsMessage.append(EnumChatFormatting.WHITE);
			for (ProcessedParticle particle : processedParticles) {
				diagsMessage.append(particle.toString());
				diagsMessage.append("\n");
			}

			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append(compassName);
			diagsMessage.append(" Particle Spread: ");
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append(particleSpread());
			diagsMessage.append("\n");

			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append(compassName);
			diagsMessage.append(" Compass Line: ");
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append((line == null) ? "<NONE>" : line.toString());
			diagsMessage.append("\n");
		}

		static class ProcessedParticle {
			Vec3Comparable coords;
			long particleTimeMillis;

			ProcessedParticle(Vec3Comparable coords, long particleTimeMillis) {
				this.coords = coords;
				this.particleTimeMillis = particleTimeMillis;
			}

			@Override
			public String toString() {
				return coords.toString() + " " + particleTimeMillis;
			}
		}
	}
}
