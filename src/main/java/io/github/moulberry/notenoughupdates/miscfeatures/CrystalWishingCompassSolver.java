package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.Line;
import io.github.moulberry.notenoughupdates.core.util.Vec3Comparable;
import io.github.moulberry.notenoughupdates.options.NEUConfig;
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
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3i;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.function.BooleanSupplier;
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

	enum CompassTarget {
		GOBLIN_QUEEN,
		GOBLIN_KING,
		BAL,
		JUNGLE_TEMPLE,
		ODAWA,
		PRECURSOR_CITY,
		MINES_OF_DIVAN,
		CRYSTAL_NUCLEUS,
	}

	enum Crystal {
		AMBER,
		AMETHYST,
		JADE,
		SAPPHIRE,
		TOPAZ,
	}

	private static final CrystalWishingCompassSolver INSTANCE = new CrystalWishingCompassSolver();
	public static CrystalWishingCompassSolver getInstance() {
		return INSTANCE;
	}

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static boolean isSkytilsPresent = false;

	// NOTE: There is a small set of breakable blocks above the nucleus at Y > 181. While this zone is reported
	//       as the Crystal Nucleus by Hypixel, for wishing compass purposes it is in the appropriate quadrant.
	private static final AxisAlignedBB NUCLEUS_BB = new AxisAlignedBB(462, 63, 461, 564, 181, 565);
	private static final AxisAlignedBB HOLLOWS_BB = new AxisAlignedBB(201, 30, 201, 824, 189, 824);
	private static final AxisAlignedBB PRECURSOR_REMNANTS_BB = new AxisAlignedBB(513, 64, 513, 824, 189, 824);
	private static final AxisAlignedBB MITHRIL_DEPOSITS_BB = new AxisAlignedBB(513, 64, 201, 824, 189, 512);
	private static final AxisAlignedBB GOBLIN_HOLDOUT_BB = new AxisAlignedBB(201, 64, 513, 512, 189, 824);
	private static final AxisAlignedBB JUNGLE_BB = new AxisAlignedBB(201, 64, 201, 512, 189, 512);
	private static final AxisAlignedBB MAGMA_FIELDS_BB = new AxisAlignedBB(201, 30, 201, 824, 63, 824);
	private static final double MAX_DISTANCE_BETWEEN_PARTICLES = 0.6;
	private static final double MAX_DISTANCE_FROM_USE_TO_FIRST_PARTICLE = 2.0;

	// 64.0 is an arbitrary value but seems to work well
	private static final double MINIMUM_DISTANCE_SQ_BETWEEN_COMPASSES = 64.0;

	// All particles typically arrive in < 3500, so 5000 should be enough buffer
	public static final long ALL_PARTICLES_MAX_MILLIS = 5000L;

	public LongSupplier currentTimeMillis = System::currentTimeMillis;
	public BooleanSupplier kingsScentPresent = this::isKingsScentPresent;
	public BooleanSupplier keyInInventory = this::isKeyInInventory;
	public interface CrystalEnumSetSupplier {
		EnumSet<Crystal> getAsCrystalEnumSet();
	}
	public CrystalEnumSetSupplier foundCrystals = this::getFoundCrystals;

	private SolverState solverState;
	private Compass firstCompass;
	private Compass secondCompass;
	private Line solutionIntersectionLine;
	private EnumSet<CompassTarget> possibleTargets;
	private Vec3Comparable solution;
	private Vec3Comparable originalSolution;
	private EnumSet<CompassTarget> solutionPossibleTargets;

	public SolverState getSolverState() {
		return solverState;
	}

	public Vec3i getSolutionCoords() {
		return new Vec3i(solution.xCoord, solution.yCoord, solution.zCoord);
	}

	public EnumSet<CompassTarget> getPossibleTargets() {
		return possibleTargets;
	}

	private void resetForNewTarget() {
		NEUDebugLogger.log(NEUDebugFlag.WISHING,"Resetting for new target");
		solverState = SolverState.NOT_STARTED;
		firstCompass = null;
		secondCompass = null;
		solutionIntersectionLine = null;
		possibleTargets = null;
		solution = null;
		originalSolution = null;
		solutionPossibleTargets = null;
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
				case POSSIBLE_TARGETS_CHANGED:
					mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
						"[NEU] Possible wishing compass targets have changed. Solver has been reset."));
					event.setCanceled(true);
					break;
				case PLAYER_IN_NUCLEUS:
					mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
						"[NEU] Wishing compass must be used outside the nucleus for accurate results."));
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
					if (NUCLEUS_BB.isVecInside(new Vec3Comparable(playerPos.getX(), playerPos.getY(), playerPos.getZ()))) {
						return HandleCompassResult.PLAYER_IN_NUCLEUS;
					}

					firstCompass = new Compass(playerPos, currentTimeMillis.getAsLong());
					solverState = SolverState.PROCESSING_FIRST_USE;
					possibleTargets = calculatePossibleTargets(playerPos);
					return HandleCompassResult.SUCCESS;
				case NEED_SECOND_COMPASS:
					if (firstCompass.whereUsed.distanceSq(playerPos) < MINIMUM_DISTANCE_SQ_BETWEEN_COMPASSES) {
						return HandleCompassResult.LOCATION_TOO_CLOSE;
					}

					if (!possibleTargets.equals(calculatePossibleTargets(playerPos))) {
						resetForNewTarget();
						return HandleCompassResult.POSSIBLE_TARGETS_CHANGED;
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
				if (solverState == SolverState.NEED_SECOND_COMPASS) {
					return;
				}
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

		solutionPossibleTargets = getSolutionTargets(possibleTargets, solution);

		// Adjust the Jungle Temple solution coordinates
		if (solutionPossibleTargets.size() == 1 &&
			solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE)) {
			originalSolution = solution;
			solution = solution.addVector(-57, 36, -21);
		}

		solverState = SolverState.SOLVED;
	}

	private boolean isKeyInInventory()
	{
		for (ItemStack item : mc.thePlayer.inventory.mainInventory){
			if (item != null && item.getDisplayName().contains("Jungle Key")) {
				return true;
			}
		}
		return false;
	}

	private boolean isKingsScentPresent()
	{
		return SBInfo.getInstance().footer.getUnformattedText().contains("King's Scent I");
	}

	private void filterCandidatesUsingCrystalsAndZone(
		BlockPos playerPos,
		EnumSet<CompassTarget> candidateTargets,
		EnumSet<Crystal> foundCrystals) {
		Vec3Comparable playerPosVec = new Vec3Comparable(playerPos);

		// If the current zone's crystal hasn't been found then remove
		// all non-nucleus candidates. The nucleus is kept since it will
		// be returned if the structure for the current zone is missing
		if (GOBLIN_HOLDOUT_BB.isVecInside(playerPosVec) && !foundCrystals.contains(Crystal.AMBER)) {
			candidateTargets.clear();
			candidateTargets.add(CompassTarget.CRYSTAL_NUCLEUS);
			candidateTargets.add(CompassTarget.GOBLIN_KING);
			candidateTargets.add(CompassTarget.GOBLIN_QUEEN);
			return;
		}

		if (JUNGLE_BB.isVecInside(playerPosVec) && !foundCrystals.contains(Crystal.AMETHYST)) {
			candidateTargets.clear();
			candidateTargets.add(CompassTarget.CRYSTAL_NUCLEUS);
			candidateTargets.add(CompassTarget.ODAWA);
			candidateTargets.add(CompassTarget.JUNGLE_TEMPLE);
			return;
		}

		if (MITHRIL_DEPOSITS_BB.isVecInside(playerPosVec) && !foundCrystals.contains(Crystal.JADE)) {
			candidateTargets.clear();
			candidateTargets.add(CompassTarget.CRYSTAL_NUCLEUS);
			candidateTargets.add(CompassTarget.MINES_OF_DIVAN);
			return;
		}

		if (PRECURSOR_REMNANTS_BB.isVecInside(playerPosVec) && !foundCrystals.contains(Crystal.SAPPHIRE)) {
			candidateTargets.clear();
			candidateTargets.add(CompassTarget.CRYSTAL_NUCLEUS);
			candidateTargets.add(CompassTarget.PRECURSOR_CITY);
			return;
		}

		if (MAGMA_FIELDS_BB.isVecInside(playerPosVec) && !foundCrystals.contains(Crystal.TOPAZ)) {
			candidateTargets.clear();
			candidateTargets.add(CompassTarget.CRYSTAL_NUCLEUS);
			candidateTargets.add(CompassTarget.BAL);
			return;
		}

		// Filter out crystal-based targets outside the current zone
		if (foundCrystals.contains(Crystal.AMBER)) {
			candidateTargets.remove(CompassTarget.GOBLIN_KING);
			candidateTargets.remove(CompassTarget.GOBLIN_QUEEN);
		}

		if (foundCrystals.contains(Crystal.AMETHYST)) {
			candidateTargets.remove(CompassTarget.ODAWA);
			candidateTargets.remove(CompassTarget.JUNGLE_TEMPLE);
		}

		if (foundCrystals.contains(Crystal.JADE)) {
			candidateTargets.remove(CompassTarget.MINES_OF_DIVAN);
		}

		if (foundCrystals.contains(Crystal.TOPAZ)) {
			candidateTargets.remove(CompassTarget.BAL);
		}

		if (foundCrystals.contains(Crystal.SAPPHIRE)) {
			candidateTargets.remove(CompassTarget.PRECURSOR_CITY);
		}
	}

	private	EnumSet<Crystal> getFoundCrystals() {
		EnumSet<Crystal> foundCrystals = EnumSet.noneOf(Crystal.class);
		NEUConfig.HiddenProfileSpecific perProfileConfig = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (perProfileConfig == null) return foundCrystals;
		HashMap<String, Integer> crystals = perProfileConfig.crystals;
		for (String crystalName : crystals.keySet()) {
			Integer crystalState = crystals.get(crystalName);
			if (crystalState != null && crystalState > 0) {
				foundCrystals.add(Crystal.valueOf(crystalName.toUpperCase()));
			}
		}

		return foundCrystals;
	}

	// Returns candidates based on seen Y coordinates and quadrants that
	// are not adjacent to the solution's quadrant. If the solution is
	// the nucleus then a copy of the original possible targets is
	// returned.
	//
	// NOTE: Adjacent quadrant filtering could be improved based on
	//       structure sizes in the future to only allow a certain
	//       distance into the adjacent quadrant.
	//
	// |----------|------------|
	// |  Jungle  |  Mithril   |
	// |          |  Deposits  |
	// |----------|----------- |
	// |  Goblin  |  Precursor |
	// |  Holdout |  Deposits  |
	// |----------|------------|
	static public EnumSet<CompassTarget> getSolutionTargets(
			EnumSet<CompassTarget> possibleTargets,
			Vec3Comparable solution) {
		EnumSet<CompassTarget> solutionPossibleTargets;
		solutionPossibleTargets = possibleTargets.clone();

		if (NUCLEUS_BB.isVecInside(solution)) {
			return solutionPossibleTargets;
		}

		solutionPossibleTargets.remove(CompassTarget.CRYSTAL_NUCLEUS);

		// Eliminate non-adjacent zones first
		if (MITHRIL_DEPOSITS_BB.isVecInside(solution)) {
			solutionPossibleTargets.remove(CompassTarget.GOBLIN_KING);
			solutionPossibleTargets.remove(CompassTarget.GOBLIN_QUEEN);
		} else if (PRECURSOR_REMNANTS_BB.isVecInside(solution)) {
			solutionPossibleTargets.remove(CompassTarget.ODAWA);
			solutionPossibleTargets.remove(CompassTarget.JUNGLE_TEMPLE);
		} else if (GOBLIN_HOLDOUT_BB.isVecInside(solution)) {
			solutionPossibleTargets.remove(CompassTarget.MINES_OF_DIVAN);
		} else if (JUNGLE_BB.isVecInside(solution)) {
			solutionPossibleTargets.remove(CompassTarget.PRECURSOR_CITY);
		}

		// If there's only 1 possible target then don't remove based
		// on Y coordinates since assumptions about Y coordinates could
		// be wrong.
		if (solutionPossibleTargets.size() > 1) {
			// Y coordinates are 43-70 from 11 samples
			if (solutionPossibleTargets.contains(CompassTarget.BAL) &&
					solution.yCoord > 72) {
				solutionPossibleTargets.remove(CompassTarget.BAL);
			}

			// Y coordinates are 93-157 from 10 samples, may be able to filter
			// more based on the offset of the King within the structure
			if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_KING) &&
					solution.yCoord < 64) {
				solutionPossibleTargets.remove(CompassTarget.GOBLIN_KING);
			}

			// Y coordinates are 129-139 from 10 samples
			if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_QUEEN) &&
					(solution.yCoord < 127 || solution.yCoord > 141)) {
				solutionPossibleTargets.remove(CompassTarget.GOBLIN_QUEEN);
			}

			// Y coordinates are 72-80 from 10 samples
			if (solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE) &&
					(solution.yCoord < 70 || solution.yCoord > 82)) {
				solutionPossibleTargets.remove(CompassTarget.JUNGLE_TEMPLE);
			}

			// Y coordinates are 110-128 from 3 samples, not enough data to use
			if (solutionPossibleTargets.contains(CompassTarget.ODAWA) &&
					solution.yCoord < 64) {
				solutionPossibleTargets.remove(CompassTarget.ODAWA);
			}

			// Y coordinates are 122-129 from 8 samples
			if (solutionPossibleTargets.contains(CompassTarget.PRECURSOR_CITY) &&
					(solution.yCoord < 119 || solution.yCoord > 132)) {
				solutionPossibleTargets.remove(CompassTarget.PRECURSOR_CITY);
			}

			// Y coordinates are 98-102 from 15 samples
			if (solutionPossibleTargets.contains(CompassTarget.MINES_OF_DIVAN) &&
					(solution.yCoord < 96 || solution.yCoord > 104)) {
				solutionPossibleTargets.remove(CompassTarget.MINES_OF_DIVAN);
			}
		}

		return solutionPossibleTargets;
	}

	private EnumSet<CompassTarget> calculatePossibleTargets(BlockPos playerPos) {
		EnumSet<CompassTarget> candidateTargets = EnumSet.allOf(CompassTarget.class);
		EnumSet<Crystal> foundCrystals = this.foundCrystals.getAsCrystalEnumSet();

		filterCandidatesUsingCrystalsAndZone(playerPos, candidateTargets, foundCrystals);
		candidateTargets.remove(kingsScentPresent.getAsBoolean() ? CompassTarget.GOBLIN_KING : CompassTarget.GOBLIN_QUEEN);
		candidateTargets.remove(keyInInventory.getAsBoolean() ? CompassTarget.ODAWA : CompassTarget.JUNGLE_TEMPLE);
		return candidateTargets;
	}

	private String getFriendlyNameForCompassTarget(CompassTarget compassTarget) {
		switch (compassTarget) {
			case BAL: return EnumChatFormatting.RED + "Bal";
			case ODAWA: return EnumChatFormatting.GREEN + "Odawa";
			case JUNGLE_TEMPLE: return EnumChatFormatting.AQUA + "the " +
				EnumChatFormatting.GREEN + "Jungle Temple";
			case GOBLIN_KING: return EnumChatFormatting.GOLD + "King Yolkar";
			case GOBLIN_QUEEN: return EnumChatFormatting.AQUA + "the " +
				EnumChatFormatting.YELLOW + "Goblin Queen";
			case PRECURSOR_CITY: return EnumChatFormatting.AQUA + "the " +
				EnumChatFormatting.WHITE + "Precursor City";
			case MINES_OF_DIVAN: return EnumChatFormatting.AQUA + "the " +
				EnumChatFormatting.BLUE + "Mines of Divan";
			default: return EnumChatFormatting.WHITE + "an undetermined location";
		}
	}

	private String getNameForCompassTarget(CompassTarget compassTarget) {
		boolean useSkytilsNames = (NotEnoughUpdates.INSTANCE.config.mining.wishingCompassWaypointNameType == 1);
		switch (compassTarget) {
			case BAL: return useSkytilsNames ? "internal_bal" : "Bal";
			case ODAWA: return "Odawa";
			case JUNGLE_TEMPLE: return useSkytilsNames ? "internal_temple" : "Temple";
			case GOBLIN_KING: return useSkytilsNames ? "internal_king" : "King";
			case GOBLIN_QUEEN: return useSkytilsNames ? "internal_den" : "Queen";
			case PRECURSOR_CITY: return useSkytilsNames ? "internal_city" : "City";
			case MINES_OF_DIVAN: return useSkytilsNames ? "internal_mines" : "Mines";
			default: return "WishingTarget";
		}
	}

	private String getSolutionCoordsText() {
		return solution == null ? "" :
			String.format("%.0f %.0f %.0f", solution.xCoord, solution.yCoord,	solution.zCoord);
	}

	private String getWishingCompassDestinationsMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append(EnumChatFormatting.YELLOW);
		sb.append("[NEU] ");
		sb.append(EnumChatFormatting.AQUA);
		sb.append("Wishing compass points to ");
		int index = 1;
		for (CompassTarget target : solutionPossibleTargets) {
			if (index > 1) {
				sb.append(EnumChatFormatting.AQUA);
				if (index == solutionPossibleTargets.size()) {
					sb.append(" or ");
				} else {
					sb.append(", ");
				}
			}
			sb.append(getFriendlyNameForCompassTarget(target));
			index++;
		}

		sb.append(EnumChatFormatting.AQUA);
		sb.append(" (");
		sb.append(getSolutionCoordsText());
		sb.append(")");
		return sb.toString();
	}

	private void showSolution() {
		if (solution == null) return;

		if (NUCLEUS_BB.isVecInside(solution)) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] " +
				EnumChatFormatting.AQUA + "Wishing compass target is the Crystal Nucleus"));
			return;
		}

		String destinationMessage = getWishingCompassDestinationsMessage();

		if (!isSkytilsPresent) {
			mc.thePlayer.addChatMessage(new ChatComponentText(destinationMessage));
			return;
		}

		String targetNameForSkytils = solutionPossibleTargets.size() == 1 ?
			getNameForCompassTarget(solutionPossibleTargets.iterator().next()) :
			"WishingTarget";
		String skytilsCommand = String.format("/sthw add %s %s", targetNameForSkytils, getSolutionCoordsText());
		if (NotEnoughUpdates.INSTANCE.config.mining.wishingCompassAutocreateKnownWaypoints &&
				solutionPossibleTargets.size() == 1) {
			mc.thePlayer.addChatMessage(new ChatComponentText(destinationMessage));
			int commandResult = ClientCommandHandler.instance.executeCommand(mc.thePlayer, skytilsCommand);
			if (commandResult == 1)
			{
				return;
			}
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Failed to automatically run /sthw"));
		}

		destinationMessage += EnumChatFormatting.YELLOW + " [Add Skytils Waypoint]";
		ChatComponentText chatMessage = new ChatComponentText(destinationMessage);
		chatMessage.setChatStyle(Utils.createClickStyle(ClickEvent.Action.RUN_COMMAND,
				skytilsCommand,
				EnumChatFormatting.YELLOW + "Set waypoint for wishing target\n"));
		mc.thePlayer.addChatMessage(chatMessage);
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
		diagsMessage.append("Jungle Key in Inventory: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(isKeyInInventory());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("King's Scent Present: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(isKingsScentPresent());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("First Compass Targets: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(possibleTargets == null ? "<NONE>" : possibleTargets.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Current Calculated Targets: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(calculatePossibleTargets(mc.thePlayer.getPosition()));
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Found Crystals: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(getFoundCrystals());
		diagsMessage.append("\n");

		if (originalSolution != null) {
			diagsMessage.append(EnumChatFormatting.AQUA);
			diagsMessage.append("Original Solution: ");
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append(originalSolution);
			diagsMessage.append("\n");
		}

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Solution: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((solution == null) ? "<NONE>" : solution.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Solution Targets: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((solutionPossibleTargets == null) ? "<NONE>" : solutionPossibleTargets.toString());
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
		POSSIBLE_TARGETS_CHANGED,
		PLAYER_IN_NUCLEUS
	}

	static class Compass {
		public CompassState compassState;
		public Line line = null;

		private final BlockPos whereUsed;
		private final long whenUsedMillis;
		private Vec3Comparable firstParticle = null;
		private Vec3Comparable previousParticle = null;
		private Vec3Comparable lastParticle = null;
		private final ArrayList<ProcessedParticle> processedParticles;

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
			if (compassState == CompassState.FAILED_TIMEOUT_NO_REPEATING ||
					compassState == CompassState.COMPLETED) {
				throw new UnsupportedOperationException("processParticle should not be called in a failed or completed state");
			}

			if (particleTimeMillis - this.whenUsedMillis > ALL_PARTICLES_MAX_MILLIS) {
				// Assume we have failed if we're still trying to process particles
				compassState = CompassState.FAILED_TIMEOUT_NO_REPEATING;
				return;
			}

			Vec3Comparable currentParticle = new Vec3Comparable(x, y, z);
			if (compassState == CompassState.WAITING_FOR_FIRST_PARTICLE) {
				if (currentParticle.distanceTo(new Vec3Comparable(whereUsed)) < MAX_DISTANCE_FROM_USE_TO_FIRST_PARTICLE) {
					processedParticles.add(new ProcessedParticle(currentParticle, particleTimeMillis));
					firstParticle = currentParticle;
					previousParticle = currentParticle;
					compassState = CompassState.COMPUTING_LAST_PARTICLE;
				}
				return;
			}

			// State is COMPUTING_LAST_PARTICLE, keep updating the previousParticle until
			// the first particle in the second sequence is seen.
			if (currentParticle.distanceTo(previousParticle) <= MAX_DISTANCE_BETWEEN_PARTICLES) {
				processedParticles.add(new ProcessedParticle(currentParticle, particleTimeMillis));
				previousParticle = currentParticle;
				return;
			}

			if (currentParticle.distanceTo(firstParticle) > MAX_DISTANCE_BETWEEN_PARTICLES) {
				return;
			}

			// It's a repeating particle
			processedParticles.add(new ProcessedParticle(currentParticle, particleTimeMillis));
			lastParticle = previousParticle;
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
