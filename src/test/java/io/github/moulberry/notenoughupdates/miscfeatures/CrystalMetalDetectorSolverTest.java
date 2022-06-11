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

import io.github.moulberry.notenoughupdates.core.util.Vec3Comparable;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalMetalDetectorSolver.SolutionState;
import io.github.moulberry.notenoughupdates.util.NEUDebugLogger;
import net.minecraft.util.BlockPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class CrystalMetalDetectorSolverTest {
	class Location {
		double distance;
		Vec3Comparable playerPosition;
		SolutionState expectedState;
		boolean centerKnown;

		public Location(
			double distance,
			Vec3Comparable playerPosition,
			SolutionState expectedState,
			boolean centerKnown
		) {
			this.distance = distance;
			this.playerPosition = playerPosition;
			this.expectedState = expectedState;
			this.centerKnown = centerKnown;
		}
	}

	class Solution {
		ArrayList<Location> locations = new ArrayList<>();
		BlockPos center = BlockPos.ORIGIN;
		BlockPos expectedSolution = BlockPos.ORIGIN;
	}

	@BeforeEach
	void setUp() {
		CrystalMetalDetectorSolver.initWorld();
		CrystalMetalDetectorSolver.treasureAllowedPredicate = blockPos -> true;
		NEUDebugLogger.logMethod = CrystalMetalDetectorSolverTest::neuDebugLog;
		NEUDebugLogger.allFlagsEnabled = true;
	}

	private void findPossibleSolutionsTwice(Location loc, boolean centerNewlyDiscovered) {
		// Each location has to be received twice to be valid
		CrystalMetalDetectorSolver.findPossibleSolutions(loc.distance, loc.playerPosition, centerNewlyDiscovered);
		CrystalMetalDetectorSolver.findPossibleSolutions(loc.distance, loc.playerPosition, false);
	}

	private void checkSolution(Solution solution) {
		boolean centerSet = false;
		int index = 0;
		for (Location loc : solution.locations) {
			if (loc.centerKnown && !centerSet && !solution.center.equals(BlockPos.ORIGIN)) {
				CrystalMetalDetectorSolver.setMinesCenter(solution.center);
				centerSet = true;
				findPossibleSolutionsTwice(loc, true);
			} else {
				findPossibleSolutionsTwice(loc, false);
			}
			Assertions.assertEquals(
				loc.expectedState,
				CrystalMetalDetectorSolver.currentState,
				"Location index " + index
			);
			index++;
		}

		Assertions.assertEquals(solution.expectedSolution, CrystalMetalDetectorSolver.getSolution());
	}

	@Test
	void findPossibleSolutions_single_location_sample_is_ignored() {
		Location location = new Location(
			37.3,
			new Vec3Comparable(779.1057116115207, 70.5, 502.2997937667801),
			SolutionState.MULTIPLE,
			false
		);

		CrystalMetalDetectorSolver.findPossibleSolutions(location.distance, location.playerPosition, false);
		Assertions.assertEquals(SolutionState.NOT_STARTED, CrystalMetalDetectorSolver.previousState,
			"Previous state"
		);
		Assertions.assertEquals(SolutionState.NOT_STARTED, CrystalMetalDetectorSolver.currentState,
			"Current state"
		);
	}

	@Test
	void findPossibleSolutions_currentState_becomes_previousState() {
		Location location = new Location(
			37.3,
			new Vec3Comparable(779.1057116115207, 70.5, 502.2997937667801),
			SolutionState.MULTIPLE,
			false
		);

		findPossibleSolutionsTwice(location, false);
		Assertions.assertEquals(SolutionState.NOT_STARTED, CrystalMetalDetectorSolver.previousState,
			"Previous state"
		);
		Assertions.assertEquals(location.expectedState, CrystalMetalDetectorSolver.currentState,
			"Current state"
		);
	}

	@Test
	void findPossibleSolutions_state_is_invalid_when_solution_and_distance_mismatch() {
		Solution solution = new Solution();
		solution.center = new BlockPos(736, 88, 547);
		solution.expectedSolution = new BlockPos(722, 67, 590);
		solution.locations.add(new Location(
			67.5,
			new Vec3Comparable(757.8235166144441, 68.0, 532.8037800566217),
			SolutionState.FOUND_KNOWN,
			true
		));
		// slightly different player position with invalid distance
		solution.locations.add(new Location(
			4.0,
			new Vec3Comparable(757.8235166144441, 69.0, 532.8037800566217),
			SolutionState.INVALID,
			true
		));
		checkSolution(solution);
	}

	@Test
	void findPossibleSolutions_state_is_failed_when_second_location_eliminates_all_blocks() {
		Solution solution = new Solution();
		solution.center = new BlockPos(736, 88, 547);
		solution.locations.add(new Location(
			29.4,
			new Vec3Comparable(721.5979761606153, 68.0, 590.9056839507032),
			SolutionState.MULTIPLE_KNOWN,
			true
		));
		solution.locations.add(new Location(
			4.0, // actual distance should be 38.2
			new Vec3Comparable(711.858759313838, 67.0, 590.3583935310772),
			SolutionState.FAILED,
			true
		));
		checkSolution(solution);
	}

	@Test
	void findPossibleSolutions_state_is_found_known_when_center_found_after_location() {
		Solution solution = new Solution();
		solution.center = new BlockPos(736, 88, 547);
		solution.locations.add(new Location(
			67.5,
			new Vec3Comparable(757.8235166144441, 68.0, 532.8037800566217),
			SolutionState.MULTIPLE,
			false
		));
		checkSolution(solution);

		solution.locations.get(0).centerKnown = true;
		solution.locations.get(0).expectedState = SolutionState.FOUND_KNOWN;
		solution.expectedSolution = new BlockPos(722, 67, 590);
		checkSolution(solution);
	}

	@Test
	void findPossibleSolutions_state_is_found_when_single_known_location() {
		Solution solution = new Solution();
		solution.center = new BlockPos(736, 88, 547);
		solution.expectedSolution = new BlockPos(722, 67, 590);
		solution.locations.add(new Location(
			67.5,
			new Vec3Comparable(757.8235166144441, 68.0, 532.8037800566217),
			SolutionState.FOUND_KNOWN,
			true
		));
		checkSolution(solution);
	}

	@Test
	void findPossibleSolutions_states_are_correct_using_multiple_locations_with_unknown_center() {
		Solution solution = new Solution();
		solution.locations.add(new Location(
			37.3,
			new Vec3Comparable(779.1057116115207, 70.5, 502.2997937667801),
			SolutionState.MULTIPLE,
			false
		));
		solution.locations.add(new Location(
			34.8,
			new Vec3Comparable(782.6999999880791, 71.0, 508.69999998807907),
			SolutionState.FOUND,
			false
		));
		solution.expectedSolution = new BlockPos(758, 67, 533);

		checkSolution(solution);
	}

	@Test
	void findPossibleSolutions_states_are_correct_when_multiple_known_locations_found() {
		Solution solution = new Solution();

		// First, validate that the solution doesn't work without the center
		solution.locations.add(new Location(
			29.4,
			new Vec3Comparable(721.5979761606153, 68.0, 590.9056839507032),
			SolutionState.MULTIPLE,
			false
		));
		solution.locations.add(new Location(
			38.2,
			new Vec3Comparable(711.858759313838, 67.0, 590.3583935310772),
			SolutionState.MULTIPLE,
			false
		));
		checkSolution(solution);

		// Now validate that the solution works with the center
		CrystalMetalDetectorSolver.resetSolution(false);
		solution.locations.get(0).expectedState = SolutionState.MULTIPLE_KNOWN;
		solution.locations.get(0).centerKnown = true;
		solution.locations.get(1).expectedState = SolutionState.FOUND_KNOWN;
		solution.locations.get(1).centerKnown = true;
		solution.expectedSolution = new BlockPos(748, 66, 578);
		solution.center = new BlockPos(736, 88, 547);
		checkSolution(solution);
	}

	private static void neuDebugLog(String message) {
		System.out.println(message);
	}
}
