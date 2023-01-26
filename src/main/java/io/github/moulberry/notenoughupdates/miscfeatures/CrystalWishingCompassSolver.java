/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.autosubscribe.NEUAutoSubscribe;
import io.github.moulberry.notenoughupdates.core.util.Line;
import io.github.moulberry.notenoughupdates.core.util.Vec3Comparable;
import io.github.moulberry.notenoughupdates.events.SpawnParticleEvent;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

@NEUAutoSubscribe
public class CrystalWishingCompassSolver {
	enum SolverState {
		NOT_STARTED,
		PROCESSING_FIRST_USE,
		NEED_SECOND_COMPASS,
		PROCESSING_SECOND_USE,
		SOLVED,
		FAILED_EXCEPTION,
		FAILED_TIMEOUT_NO_REPEATING,
		FAILED_TIMEOUT_NO_PARTICLES,
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

	enum HollowsZone {
		CRYSTAL_NUCLEUS,
		JUNGLE,
		MITHRIL_DEPOSITS,
		GOBLIN_HOLDOUT,
		PRECURSOR_REMNANTS,
		MAGMA_FIELDS,
	}

	private static final CrystalWishingCompassSolver INSTANCE = new CrystalWishingCompassSolver();

	public static CrystalWishingCompassSolver getInstance() {
		return INSTANCE;
	}

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static boolean isSkytilsPresent = false;
	private static final ArrayDeque<ParticleData> seenParticles = new ArrayDeque<>();

	// There is a small set of breakable blocks above the nucleus at Y > 181. While this zone is reported
	// as the Crystal Nucleus by Hypixel, for wishing compass purposes it is in the appropriate quadrant.
	private static final AxisAlignedBB NUCLEUS_BB = new AxisAlignedBB(462, 63, 461, 564, 181, 565);
	// Bounding box around all breakable blocks in the crystal hollows, appears as bedrock in-game
	private static final AxisAlignedBB HOLLOWS_BB = new AxisAlignedBB(201, 30, 201, 824, 189, 824);

	// Zone bounding boxes
	private static final AxisAlignedBB PRECURSOR_REMNANTS_BB = new AxisAlignedBB(512, 63, 512, 824, 189, 824);
	private static final AxisAlignedBB MITHRIL_DEPOSITS_BB = new AxisAlignedBB(512, 63, 201, 824, 189, 513);
	private static final AxisAlignedBB GOBLIN_HOLDOUT_BB = new AxisAlignedBB(201, 63, 512, 513, 189, 824);
	private static final AxisAlignedBB JUNGLE_BB = new AxisAlignedBB(201, 63, 201, 513, 189, 513);
	private static final AxisAlignedBB MAGMA_FIELDS_BB = new AxisAlignedBB(201, 30, 201, 824, 64, 824);

	// Structure bounding boxes (size + 2 in each dimension to make it an actual bounding box)
	private static final AxisAlignedBB PRECURSOR_CITY_BB = new AxisAlignedBB(0, 0, 0, 107, 122, 107);
	private static final AxisAlignedBB GOBLIN_KING_BB = new AxisAlignedBB(0, 0, 0, 59, 53, 56);
	private static final AxisAlignedBB GOBLIN_QUEEN_BB = new AxisAlignedBB(0, 0, 0, 108, 114, 108);
	private static final AxisAlignedBB JUNGLE_TEMPLE_BB = new AxisAlignedBB(0, 0, 0, 108, 120, 108);
	private static final AxisAlignedBB ODAWA_BB = new AxisAlignedBB(0, 0, 0, 53, 46, 54);
	private static final AxisAlignedBB MINES_OF_DIVAN_BB = new AxisAlignedBB(0, 0, 0, 108, 125, 108);
	private static final AxisAlignedBB KHAZAD_DUM_BB = new AxisAlignedBB(0, 0, 0, 110, 46, 108);

	private static final Vec3Comparable JUNGLE_DOOR_OFFSET_FROM_CRYSTAL = new Vec3Comparable(-57, 36, -21);

	private static final double MAX_DISTANCE_BETWEEN_PARTICLES = 0.6;
	private static final double MAX_DISTANCE_FROM_USE_TO_FIRST_PARTICLE = 9.0;

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

	public static HollowsZone getZoneForCoords(BlockPos blockPos) {
		return getZoneForCoords(new Vec3Comparable(blockPos));
	}

	public static HollowsZone getZoneForCoords(Vec3Comparable coords) {
		if (NUCLEUS_BB.isVecInside(coords)) return HollowsZone.CRYSTAL_NUCLEUS;
		if (JUNGLE_BB.isVecInside(coords)) return HollowsZone.JUNGLE;
		if (MITHRIL_DEPOSITS_BB.isVecInside(coords)) return HollowsZone.MITHRIL_DEPOSITS;
		if (GOBLIN_HOLDOUT_BB.isVecInside(coords)) return HollowsZone.GOBLIN_HOLDOUT;
		if (PRECURSOR_REMNANTS_BB.isVecInside(coords)) return HollowsZone.PRECURSOR_REMNANTS;
		if (MAGMA_FIELDS_BB.isVecInside(coords)) return HollowsZone.MAGMA_FIELDS;
		throw new IllegalArgumentException("Coordinates do not fall in known zone: " + coords.toString());
	}

	private void resetForNewTarget() {
		NEUDebugLogger.log(NEUDebugFlag.WISHING, "Resetting for new target");
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
				case STILL_PROCESSING_PRIOR_USE:
					Utils.addChatMessage(
						EnumChatFormatting.YELLOW + "[NEU] Wait a little longer before using the wishing compass again.");
					event.setCanceled(true);
					break;
				case LOCATION_TOO_CLOSE:
					Utils.addChatMessage(
						EnumChatFormatting.YELLOW + "[NEU] Move a little further before using the wishing compass again.");
					event.setCanceled(true);
					break;
				case POSSIBLE_TARGETS_CHANGED:
					Utils.addChatMessage(
						EnumChatFormatting.YELLOW + "[NEU] Possible wishing compass targets have changed. Solver has been reset.");
					event.setCanceled(true);
					break;
				case NO_PARTICLES_FOR_PREVIOUS_COMPASS:
					Utils.addChatMessage(EnumChatFormatting.YELLOW +
						"[NEU] No particles detected for prior compass use. Need another position to solve.");
					break;
				case PLAYER_IN_NUCLEUS:
					Utils.addChatMessage(
						EnumChatFormatting.YELLOW + "[NEU] Wishing compass must be used outside the nucleus for accurate results.");
					event.setCanceled(true);
					break;
				default:
					throw new IllegalStateException("Unexpected wishing compass solver state: \n" + getDiagnosticMessage());
			}
		} catch (Exception e) {
			Utils.addChatMessage(EnumChatFormatting.RED +
				"[NEU] Error processing wishing compass action - see log for details");
			e.printStackTrace();
			event.setCanceled(true);
			solverState = SolverState.FAILED_EXCEPTION;
		}
	}

	public HandleCompassResult handleCompassUse(BlockPos playerPos) {
		long lastCompassUsedMillis = 0;
		switch (solverState) {
			case PROCESSING_SECOND_USE:
				if (secondCompass != null) {
					lastCompassUsedMillis = secondCompass.whenUsedMillis;
				}
			case PROCESSING_FIRST_USE:
				if (lastCompassUsedMillis == 0 && firstCompass != null) {
					lastCompassUsedMillis = firstCompass.whenUsedMillis;
				}
				if (lastCompassUsedMillis != 0 &&
					(currentTimeMillis.getAsLong() > lastCompassUsedMillis + ALL_PARTICLES_MAX_MILLIS)) {
					return HandleCompassResult.NO_PARTICLES_FOR_PREVIOUS_COMPASS;
				}

				return HandleCompassResult.STILL_PROCESSING_PRIOR_USE;
			case SOLVED:
			case FAILED_EXCEPTION:
			case FAILED_TIMEOUT_NO_REPEATING:
			case FAILED_TIMEOUT_NO_PARTICLES:
			case FAILED_INTERSECTION_CALCULATION:
			case FAILED_INVALID_SOLUTION:
				resetForNewTarget();
				// falls through, NOT_STARTED is the state when resetForNewTarget returns
			case NOT_STARTED:
				if (NUCLEUS_BB.isVecInside(new Vec3Comparable(playerPos.getX(), playerPos.getY(), playerPos.getZ()))) {
					return HandleCompassResult.PLAYER_IN_NUCLEUS;
				}

				firstCompass = new Compass(playerPos, currentTimeMillis.getAsLong());
				seenParticles.clear();
				solverState = SolverState.PROCESSING_FIRST_USE;
				possibleTargets = calculatePossibleTargets(playerPos);
				return HandleCompassResult.SUCCESS;
			case NEED_SECOND_COMPASS:
				if (firstCompass.whereUsed.distanceSq(playerPos) < MINIMUM_DISTANCE_SQ_BETWEEN_COMPASSES) {
					return HandleCompassResult.LOCATION_TOO_CLOSE;
				}

				HollowsZone firstCompassZone = getZoneForCoords(firstCompass.whereUsed);
				HollowsZone playerZone = getZoneForCoords(playerPos);
				if (!possibleTargets.equals(calculatePossibleTargets(playerPos)) ||
					firstCompassZone != playerZone) {
					resetForNewTarget();
					return HandleCompassResult.POSSIBLE_TARGETS_CHANGED;
				}

				secondCompass = new Compass(playerPos, currentTimeMillis.getAsLong());
				solverState = SolverState.PROCESSING_SECOND_USE;
				return HandleCompassResult.SUCCESS;
		}

		throw new IllegalStateException("Unexpected compass state");
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
	@SubscribeEvent
	public void onSpawnParticle(SpawnParticleEvent event) {
		EnumParticleTypes particleType = event.getParticleTypes();
		double x = event.getXCoord();
		double y = event.getYCoord();
		double z = event.getZCoord();
		if (!NotEnoughUpdates.INSTANCE.config.mining.wishingCompassSolver ||
			particleType != EnumParticleTypes.VILLAGER_HAPPY ||
			!"crystal_hollows".equals(SBInfo.getInstance().getLocation())) {
			return;
		}

		// Capture particle troubleshooting info for two minutes starting when the first compass is used.
		// This list is reset each time the first compass is used from a NOT_STARTED state.
		if (firstCompass != null && !solverState.equals(SolverState.SOLVED) &&
			System.currentTimeMillis() < firstCompass.whenUsedMillis + 2 * 60 * 1000) {
			seenParticles.add(new ParticleData(new Vec3Comparable(x, y, z), System.currentTimeMillis()));
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
						Utils.addChatMessage(EnumChatFormatting.RED + "[NEU] Unable to determine wishing compass target.");
						logDiagnosticData(false);
						break;
					case FAILED_TIMEOUT_NO_REPEATING:
						Utils.addChatMessage(
							EnumChatFormatting.RED + "[NEU] Timed out waiting for repeat set of compass particles.");
						logDiagnosticData(false);
						break;
					case FAILED_TIMEOUT_NO_PARTICLES:
						Utils.addChatMessage(EnumChatFormatting.RED + "[NEU] Timed out waiting for compass particles.");
						logDiagnosticData(false);
						break;
					case FAILED_INTERSECTION_CALCULATION:
						Utils.addChatMessage(
							EnumChatFormatting.RED + "[NEU] Unable to determine intersection of wishing compasses.");
						logDiagnosticData(false);
						break;
					case FAILED_INVALID_SOLUTION:
						Utils.addChatMessage(EnumChatFormatting.RED + "[NEU] Failed to find solution.");
						logDiagnosticData(false);
						break;
					case NEED_SECOND_COMPASS:
						Utils.addChatMessage(
							EnumChatFormatting.YELLOW + "[NEU] Need another position to determine wishing compass target.");
						break;
				}
			}
		} catch (Exception e) {
			Utils.addChatMessage(
				EnumChatFormatting.RED + "[NEU] Exception while calculating wishing compass solution - see log for details");
			e.printStackTrace();
		}
	}

	/**
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
			case FAILED_TIMEOUT_NO_PARTICLES:
				solverState = SolverState.FAILED_TIMEOUT_NO_PARTICLES;
				return;
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

		solutionPossibleTargets = getSolutionTargets(
			getZoneForCoords(firstCompass.whereUsed),
			foundCrystals.getAsCrystalEnumSet(),
			possibleTargets,
			solution
		);

		// Adjust the Jungle Temple solution coordinates
		if (solutionPossibleTargets.size() == 1 &&
			solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE)) {
			originalSolution = solution;
			solution = solution.add(JUNGLE_DOOR_OFFSET_FROM_CRYSTAL);
		}

		solverState = SolverState.SOLVED;
	}

	private boolean isKeyInInventory() {
		for (ItemStack item : mc.thePlayer.inventory.mainInventory) {
			if (item != null && item.getDisplayName().contains("Jungle Key")) {
				return true;
			}
		}
		return false;
	}

	private boolean isKingsScentPresent() {
		return SBInfo.getInstance().footer.getUnformattedText().contains("King's Scent I");
	}

	private EnumSet<Crystal> getFoundCrystals() {
		EnumSet<Crystal> foundCrystals = EnumSet.noneOf(Crystal.class);
		NEUConfig.HiddenProfileSpecific perProfileConfig = NotEnoughUpdates.INSTANCE.config.getProfileSpecific();
		if (perProfileConfig == null) return foundCrystals;
		HashMap<String, Integer> crystals = perProfileConfig.crystals;
		for (String crystalName : crystals.keySet()) {
			Integer crystalState = crystals.get(crystalName);
			if (crystalState != null && crystalState > 0) {
				foundCrystals.add(Crystal.valueOf(crystalName.toUpperCase(Locale.US).replace("İ", "I")));
			}
		}

		return foundCrystals;
	}

	// Returns candidates based on:
	//  - Structure Y levels observed in various lobbies. It is assumed
	//    that structures other than Khazad Dum cannot have any portion
	//    in the Magma Fields.
	//
	//  - Structure sizes & offsets into other zones that assume at least
	//    one block must be in the correct zone.
	//
	//  - An assumption that any structure could be missing with a
	//    special exception for the Jungle Temple since it often conflicts
	//    with Bal and a lobby with a missing Jungle Temple has not been
	//    observed. This exception will remove Bal as a target if:
	//      - Target candidates include both Bal & the Jungle Temple.
	//      - The Amethyst crystal has not been acquired.
	//      - The zone that the compass was used in is the Jungle.
	//
	// 	- If the solution is the Crystal Nucleus then a copy of the
	// 	  passed in possible targets is returned.
	//
	// |----------|------------|
	// |  Jungle  |  Mithril   |
	// |          |  Deposits  |
	// |----------|----------- |
	// |  Goblin  |  Precursor |
	// |  Holdout |  Deposits  |
	// |----------|------------|
	static public EnumSet<CompassTarget> getSolutionTargets(
		HollowsZone compassUsedZone,
		EnumSet<Crystal> foundCrystals,
		EnumSet<CompassTarget> possibleTargets,
		Vec3Comparable solution
	) {
		EnumSet<CompassTarget> solutionPossibleTargets;
		solutionPossibleTargets = possibleTargets.clone();

		HollowsZone solutionZone = getZoneForCoords(solution);
		if (solutionZone == HollowsZone.CRYSTAL_NUCLEUS) {
			return solutionPossibleTargets;
		}

		solutionPossibleTargets.remove(CompassTarget.CRYSTAL_NUCLEUS);

		// Y coordinates are 43-71 from 13 samples
		// Y=41/74 is the absolute min/max based on structure size if
		// the center of the topaz crystal has to be in magma fields.
		if (solutionPossibleTargets.contains(CompassTarget.BAL) &&
			solution.yCoord > 75) {
			solutionPossibleTargets.remove(CompassTarget.BAL);
		}

		// Y coordinates are 93-157 from 15 samples.
		// Y=83/167 is the absolute min/max based on structure size
		if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_KING) &&
			solution.yCoord < 82 || solution.yCoord > 168) {
			solutionPossibleTargets.remove(CompassTarget.GOBLIN_KING);
		}

		// Y coordinates are 129-139 from 10 samples
		// Y=126/139 is the absolute min/max based on structure size
		if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_QUEEN) &&
			(solution.yCoord < 125 || solution.yCoord > 140)) {
			solutionPossibleTargets.remove(CompassTarget.GOBLIN_QUEEN);
		}

		// Y coordinates are 72-80 from 10 samples
		// Y=73/80 is the absolute min/max based on structure size
		if (solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE) &&
			(solution.yCoord < 72 || solution.yCoord > 81)) {
			solutionPossibleTargets.remove(CompassTarget.JUNGLE_TEMPLE);
		}

		// Y coordinates are 87-155 from 7 samples
		// Y=74/155 is the absolute min/max solution based on structure size
		if (solutionPossibleTargets.contains(CompassTarget.ODAWA) &&
			(solution.yCoord < 73 || solution.yCoord > 155)) {
			solutionPossibleTargets.remove(CompassTarget.ODAWA);
		}

		// Y coordinates are 122-129 from 8 samples
		// Y=122/129 is the absolute min/max based on structure size
		if (solutionPossibleTargets.contains(CompassTarget.PRECURSOR_CITY) &&
			(solution.yCoord < 121 || solution.yCoord > 130)) {
			solutionPossibleTargets.remove(CompassTarget.PRECURSOR_CITY);
		}

		// Y coordinates are 98-102 from 15 samples
		// Y=98/100 is the absolute min/max based on structure size,
		// but 102 has been seen - possibly with earlier code that rounded up
		if (solutionPossibleTargets.contains(CompassTarget.MINES_OF_DIVAN) &&
			(solution.yCoord < 97 || solution.yCoord > 102)) {
			solutionPossibleTargets.remove(CompassTarget.MINES_OF_DIVAN);
		}

		// Now filter by structure offset
		if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_KING) &&
			(solution.xCoord > GOBLIN_HOLDOUT_BB.maxX + GOBLIN_KING_BB.maxX ||
				solution.zCoord < GOBLIN_HOLDOUT_BB.minZ - GOBLIN_KING_BB.maxZ)) {
			solutionPossibleTargets.remove(CompassTarget.GOBLIN_KING);
		}

		if (solutionPossibleTargets.contains(CompassTarget.GOBLIN_QUEEN) &&
			(solution.xCoord > GOBLIN_HOLDOUT_BB.maxX + GOBLIN_QUEEN_BB.maxX ||
				solution.zCoord < GOBLIN_HOLDOUT_BB.minZ - GOBLIN_QUEEN_BB.maxZ)) {
			solutionPossibleTargets.remove(CompassTarget.GOBLIN_QUEEN);
		}

		if (solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE) &&
			(solution.xCoord > JUNGLE_BB.maxX + JUNGLE_TEMPLE_BB.maxX ||
				solution.zCoord > JUNGLE_BB.maxZ + JUNGLE_TEMPLE_BB.maxZ)) {
			solutionPossibleTargets.remove(CompassTarget.JUNGLE_TEMPLE);
		}

		if (solutionPossibleTargets.contains(CompassTarget.ODAWA) &&
			(solution.xCoord > JUNGLE_BB.maxX + ODAWA_BB.maxX ||
				solution.zCoord > JUNGLE_BB.maxZ + ODAWA_BB.maxZ)) {
			solutionPossibleTargets.remove(CompassTarget.ODAWA);
		}

		if (solutionPossibleTargets.contains(CompassTarget.PRECURSOR_CITY) &&
			(solution.xCoord < PRECURSOR_REMNANTS_BB.minX - PRECURSOR_CITY_BB.maxX ||
				solution.zCoord < PRECURSOR_REMNANTS_BB.minZ - PRECURSOR_CITY_BB.maxZ)) {
			solutionPossibleTargets.remove(CompassTarget.PRECURSOR_CITY);
		}

		if (solutionPossibleTargets.contains(CompassTarget.MINES_OF_DIVAN) &&
			(solution.xCoord < MITHRIL_DEPOSITS_BB.minX - MINES_OF_DIVAN_BB.maxX ||
				solution.zCoord > MITHRIL_DEPOSITS_BB.maxZ + MINES_OF_DIVAN_BB.maxZ)) {
			solutionPossibleTargets.remove(CompassTarget.MINES_OF_DIVAN);
		}

		// Special case the Jungle Temple
		if (solutionPossibleTargets.contains(CompassTarget.JUNGLE_TEMPLE) &&
			solutionPossibleTargets.contains(CompassTarget.BAL) &&
			!foundCrystals.contains(Crystal.AMETHYST) &&
			compassUsedZone == HollowsZone.JUNGLE) {
			solutionPossibleTargets.remove(CompassTarget.BAL);
		}

		return solutionPossibleTargets;
	}

	private EnumSet<CompassTarget> calculatePossibleTargets(BlockPos playerPos) {
		EnumSet<CompassTarget> candidateTargets = EnumSet.of(CompassTarget.CRYSTAL_NUCLEUS);
		EnumSet<Crystal> foundCrystals = this.foundCrystals.getAsCrystalEnumSet();

		// Add targets based on missing crystals.
		// NOTE:
		//   We used to assume that only the adjacent zone's targets could be returned. That turned
		//   out to be incorrect (e.g. a compass in the jungle pointed to the Precursor City when
		//   the king would have been a valid target). Now we assume that any structure could be
		//   missing (because Hypixel) and  depend on the solution coordinates to filter the list.
		for (Crystal crystal : Crystal.values()) {
			if (foundCrystals.contains(crystal)) {
				continue;
			}

			switch (crystal) {
				case JADE:
					candidateTargets.add(CompassTarget.MINES_OF_DIVAN);
					break;
				case AMBER:
					candidateTargets.add(
						kingsScentPresent.getAsBoolean() ? CompassTarget.GOBLIN_QUEEN : CompassTarget.GOBLIN_KING);
					break;
				case TOPAZ:
					candidateTargets.add(CompassTarget.BAL);
					break;
				case AMETHYST:
					candidateTargets.add(
						keyInInventory.getAsBoolean() ? CompassTarget.JUNGLE_TEMPLE : CompassTarget.ODAWA);
					break;
				case SAPPHIRE:
					candidateTargets.add(CompassTarget.PRECURSOR_CITY);
					break;
			}
		}

		return candidateTargets;
	}

	private String getFriendlyNameForCompassTarget(CompassTarget compassTarget) {
		switch (compassTarget) {
			case BAL:
				return EnumChatFormatting.RED + "Bal";
			case ODAWA:
				return EnumChatFormatting.GREEN + "Odawa";
			case JUNGLE_TEMPLE:
				return EnumChatFormatting.AQUA + "the " +
					EnumChatFormatting.GREEN + "Jungle Temple";
			case GOBLIN_KING:
				return EnumChatFormatting.GOLD + "King Yolkar";
			case GOBLIN_QUEEN:
				return EnumChatFormatting.AQUA + "the " +
					EnumChatFormatting.YELLOW + "Goblin Queen";
			case PRECURSOR_CITY:
				return EnumChatFormatting.AQUA + "the " +
					EnumChatFormatting.WHITE + "Precursor City";
			case MINES_OF_DIVAN:
				return EnumChatFormatting.AQUA + "the " +
					EnumChatFormatting.BLUE + "Mines of Divan";
			default:
				return EnumChatFormatting.WHITE + "an undetermined location";
		}
	}

	private String getNameForCompassTarget(CompassTarget compassTarget) {
		boolean useSkytilsNames = (NotEnoughUpdates.INSTANCE.config.mining.wishingCompassWaypointNames == 1);
		switch (compassTarget) {
			case BAL:
				return useSkytilsNames ? "internal_bal" : "Bal";
			case ODAWA:
				return "Odawa";
			case JUNGLE_TEMPLE:
				return useSkytilsNames ? "internal_temple" : "Temple";
			case GOBLIN_KING:
				return useSkytilsNames ? "internal_king" : "King";
			case GOBLIN_QUEEN:
				return useSkytilsNames ? "internal_den" : "Queen";
			case PRECURSOR_CITY:
				return useSkytilsNames ? "internal_city" : "City";
			case MINES_OF_DIVAN:
				return useSkytilsNames ? "internal_mines" : "Mines";
			default:
				return "WishingTarget";
		}
	}

	private String getSolutionCoordsText() {
		return solution == null ? "" :
			String.format("%.0f %.0f %.0f", solution.xCoord, solution.yCoord, solution.zCoord);
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
			Utils.addChatMessage(EnumChatFormatting.YELLOW + "[NEU] " + EnumChatFormatting.AQUA + "Wishing compass target is the Crystal Nucleus");
			return;
		}

		String destinationMessage = getWishingCompassDestinationsMessage();

		if (!isSkytilsPresent) {
			Utils.addChatMessage(destinationMessage);
			return;
		}

		String targetNameForSkytils = solutionPossibleTargets.size() == 1 ?
			getNameForCompassTarget(solutionPossibleTargets.iterator().next()) :
			"WishingTarget";
		String skytilsCommand = String.format("/sthw add %s %s", getSolutionCoordsText(), targetNameForSkytils);
		if (NotEnoughUpdates.INSTANCE.config.mining.wishingCompassAutocreateKnownWaypoints &&
			solutionPossibleTargets.size() == 1) {
			Utils.addChatMessage(destinationMessage);
			int commandResult = ClientCommandHandler.instance.executeCommand(mc.thePlayer, skytilsCommand);
			if (commandResult == 1) {
				return;
			}
			Utils.addChatMessage(
				EnumChatFormatting.RED + "[NEU] Failed to automatically run /sthw");
		}

		destinationMessage += EnumChatFormatting.YELLOW + " [Add Skytils Waypoint]";
		ChatComponentText chatMessage = new ChatComponentText(destinationMessage);
		chatMessage.setChatStyle(Utils.createClickStyle(
			ClickEvent.Action.RUN_COMMAND,
			skytilsCommand,
			EnumChatFormatting.YELLOW + "Set waypoint for wishing target"
		));
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

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Seen particles:\n");
		for (ParticleData particleData : seenParticles) {
			diagsMessage.append(EnumChatFormatting.WHITE);
			diagsMessage.append(particleData);
			diagsMessage.append("\n");
		}

		return diagsMessage.toString();
	}

	public void logDiagnosticData(boolean outputAlways) {
		if (!SBInfo.getInstance().checkForSkyblockLocation()) {
			return;
		}

		if (!NotEnoughUpdates.INSTANCE.config.mining.wishingCompassSolver) {
			Utils.addChatMessage(EnumChatFormatting.RED +
				"[NEU] Wishing Compass Solver is not enabled.");
			return;
		}

		boolean wishingDebugFlagSet = NEUDebugFlag.WISHING.isSet();
		if (outputAlways || wishingDebugFlagSet) {
			NEUDebugLogger.logAlways(getDiagnosticMessage());
		}
	}

	enum CompassState {
		WAITING_FOR_FIRST_PARTICLE,
		COMPUTING_LAST_PARTICLE,
		COMPLETED,
		FAILED_TIMEOUT_NO_REPEATING,
		FAILED_TIMEOUT_NO_PARTICLES,
	}

	enum HandleCompassResult {
		SUCCESS,
		LOCATION_TOO_CLOSE,
		STILL_PROCESSING_PRIOR_USE,
		POSSIBLE_TARGETS_CHANGED,
		NO_PARTICLES_FOR_PREVIOUS_COMPASS,
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
				compassState == CompassState.FAILED_TIMEOUT_NO_PARTICLES ||
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

	private static class ParticleData {
		Vec3Comparable particleLocation;
		long systemTime;

		public ParticleData(Vec3Comparable particleLocation, long systemTime) {
			this.particleLocation = particleLocation;
			this.systemTime = systemTime;
		}

		public String toString() {
			return "Location: " + particleLocation.toString() + ", systemTime: " + systemTime;
		}
	}
}
