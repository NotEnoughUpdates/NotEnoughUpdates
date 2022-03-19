package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.core.util.Vec3Comparable;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver.HandleCompassResult;
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver.SolverState;
import io.github.moulberry.notenoughupdates.util.NEUDebugLogger;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

class CrystalWishingCompassSolverTest {
	private static final CrystalWishingCompassSolver solver = CrystalWishingCompassSolver.getInstance();
	long systemTimeMillis;

	private final CompassUse compassUse1Set1 = new CompassUse(
		1647528732979L,
		new BlockPos(754, 137, 239),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(754.358459, 138.536407, 239.200928), 137),
			new ParticleSpawn(new Vec3Comparable(754.315735, 138.444351, 239.690521), 45),
			new ParticleSpawn(new Vec3Comparable(754.272278, 138.352051, 240.180008), 51),
			new ParticleSpawn(new Vec3Comparable(754.228760, 138.259750, 240.669479), 49),
			new ParticleSpawn(new Vec3Comparable(754.185303, 138.167435, 241.158966), 57),
			new ParticleSpawn(new Vec3Comparable(754.141846, 138.075134, 241.648438), 50),
			new ParticleSpawn(new Vec3Comparable(754.098328, 137.982819, 242.137909), 51),
			new ParticleSpawn(new Vec3Comparable(754.054871, 137.890518, 242.627396), 57),
			new ParticleSpawn(new Vec3Comparable(754.011353, 137.798203, 243.116867), 44),
			new ParticleSpawn(new Vec3Comparable(753.967896, 137.705887, 243.606354), 59),
			new ParticleSpawn(new Vec3Comparable(753.924438, 137.613586, 244.095825), 35),
			new ParticleSpawn(new Vec3Comparable(753.880920, 137.521271, 244.585297), 48),
			new ParticleSpawn(new Vec3Comparable(753.837463, 137.428970, 245.074783), 70),
			new ParticleSpawn(new Vec3Comparable(753.793945, 137.336655, 245.564255), 33),
			new ParticleSpawn(new Vec3Comparable(753.750488, 137.244354, 246.053741), 55),
			new ParticleSpawn(new Vec3Comparable(753.707031, 137.152039, 246.543213), 42),
			new ParticleSpawn(new Vec3Comparable(753.663513, 137.059738, 247.032700), 56),
			new ParticleSpawn(new Vec3Comparable(753.620056, 136.967422, 247.522171), 48),
			new ParticleSpawn(new Vec3Comparable(753.576538, 136.875122, 248.011642), 56),
			new ParticleSpawn(new Vec3Comparable(754.333618, 138.527710, 239.197800), 55)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.NEED_SECOND_COMPASS);

	private final CompassUse compassUse2Set1 = new CompassUse(
		1647528737531L,
		new BlockPos(760, 134, 266),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(759.686951, 135.524994, 266.190704), 129),
			new ParticleSpawn(new Vec3Comparable(759.625183, 135.427887, 266.677277), 69),
			new ParticleSpawn(new Vec3Comparable(759.561707, 135.330704, 267.163635), 31),
			new ParticleSpawn(new Vec3Comparable(759.498230, 135.233536, 267.649963), 115),
			new ParticleSpawn(new Vec3Comparable(759.434753, 135.136368, 268.136322), 0),
			new ParticleSpawn(new Vec3Comparable(759.371277, 135.039200, 268.622650), 46),
			new ParticleSpawn(new Vec3Comparable(759.307800, 134.942017, 269.109009), 49),
			new ParticleSpawn(new Vec3Comparable(759.244324, 134.844849, 269.595337), 59),
			new ParticleSpawn(new Vec3Comparable(759.180847, 134.747681, 270.081696), 45),
			new ParticleSpawn(new Vec3Comparable(759.117371, 134.650513, 270.568024), 39),
			new ParticleSpawn(new Vec3Comparable(759.053894, 134.553329, 271.054352), 67),
			new ParticleSpawn(new Vec3Comparable(758.990356, 134.456161, 271.540710), 49),
			new ParticleSpawn(new Vec3Comparable(758.926880, 134.358994, 272.027039), 32),
			new ParticleSpawn(new Vec3Comparable(758.863403, 134.261826, 272.513397), 61),
			new ParticleSpawn(new Vec3Comparable(758.799927, 134.164642, 272.999725), 44),
			new ParticleSpawn(new Vec3Comparable(758.736450, 134.067474, 273.486084), 48),
			new ParticleSpawn(new Vec3Comparable(758.672974, 133.970306, 273.972412), 57),
			new ParticleSpawn(new Vec3Comparable(758.609497, 133.873138, 274.458740), 55),
			new ParticleSpawn(new Vec3Comparable(758.546021, 133.775955, 274.945099), 59),
			new ParticleSpawn(new Vec3Comparable(758.482544, 133.678787, 275.431427), 38),
			new ParticleSpawn(new Vec3Comparable(759.636658, 135.522827, 266.186371), 0)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.SOLVED);

	Vec3i set1Solution = new Vec3i(735, 98, 451);

	private final CompassUse compassUse1Set2 = new CompassUse(
		1647657213031L,
		new BlockPos(292, 122, 304),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(292.419891, 123.624809, 304.129456), 180),
			new ParticleSpawn(new Vec3Comparable(292.754242, 123.628769, 304.501190), 64),
			new ParticleSpawn(new Vec3Comparable(293.088562, 123.632713, 304.872986), 33),
			new ParticleSpawn(new Vec3Comparable(293.422882, 123.636658, 305.244751), 272),
			new ParticleSpawn(new Vec3Comparable(293.757172, 123.640602, 305.616547), 0),
			new ParticleSpawn(new Vec3Comparable(294.091492, 123.644547, 305.988312), 0),
			new ParticleSpawn(new Vec3Comparable(294.425812, 123.648491, 306.360077), 1),
			new ParticleSpawn(new Vec3Comparable(294.760132, 123.652435, 306.731873), 0),
			new ParticleSpawn(new Vec3Comparable(295.094452, 123.656387, 307.103638), 41),
			new ParticleSpawn(new Vec3Comparable(295.428741, 123.660332, 307.475433), 36),
			new ParticleSpawn(new Vec3Comparable(295.763062, 123.664276, 307.847198), 79),
			new ParticleSpawn(new Vec3Comparable(296.097382, 123.668221, 308.218994), 22),
			new ParticleSpawn(new Vec3Comparable(296.431702, 123.672165, 308.590759), 43),
			new ParticleSpawn(new Vec3Comparable(296.766022, 123.676109, 308.962524), 91),
			new ParticleSpawn(new Vec3Comparable(297.100311, 123.680061, 309.334320), 16),
			new ParticleSpawn(new Vec3Comparable(297.434631, 123.684006, 309.706085), 55),
			new ParticleSpawn(new Vec3Comparable(297.768951, 123.687950, 310.077881), 74),
			new ParticleSpawn(new Vec3Comparable(298.103271, 123.691895, 310.449646), 15),
			new ParticleSpawn(new Vec3Comparable(298.437592, 123.695839, 310.821411), 241),
			new ParticleSpawn(new Vec3Comparable(298.771881, 123.699783, 311.193207), 0),
			new ParticleSpawn(new Vec3Comparable(292.417267, 123.623947, 304.131836), 0)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.NEED_SECOND_COMPASS);

	private final CompassUse compassUse2Set2 = new CompassUse(
		1647657224034L,
		new BlockPos(286, 122, 315),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(286.052460, 123.623955, 315.075287), 209),
			new ParticleSpawn(new Vec3Comparable(286.393890, 123.627922, 315.440521), 16),
			new ParticleSpawn(new Vec3Comparable(286.735413, 123.631897, 315.805695), 79),
			new ParticleSpawn(new Vec3Comparable(287.076965, 123.635864, 316.170837), 24),
			new ParticleSpawn(new Vec3Comparable(287.418518, 123.639839, 316.535980), 54),
			new ParticleSpawn(new Vec3Comparable(287.760071, 123.643806, 316.901123), 47),
			new ParticleSpawn(new Vec3Comparable(288.101624, 123.647774, 317.266266), 42),
			new ParticleSpawn(new Vec3Comparable(288.443146, 123.651749, 317.631409), 56),
			new ParticleSpawn(new Vec3Comparable(288.784698, 123.655716, 317.996552), 66),
			new ParticleSpawn(new Vec3Comparable(289.126251, 123.659691, 318.361694), 41),
			new ParticleSpawn(new Vec3Comparable(289.467804, 123.663658, 318.726837), 47),
			new ParticleSpawn(new Vec3Comparable(289.809357, 123.667633, 319.091980), 40),
			new ParticleSpawn(new Vec3Comparable(290.150909, 123.671600, 319.457123), 65),
			new ParticleSpawn(new Vec3Comparable(290.492432, 123.675568, 319.822266), 37),
			new ParticleSpawn(new Vec3Comparable(290.833984, 123.679543, 320.187408), 69),
			new ParticleSpawn(new Vec3Comparable(291.175537, 123.683510, 320.552551), 66),
			new ParticleSpawn(new Vec3Comparable(291.517090, 123.687485, 320.917694), 16),
			new ParticleSpawn(new Vec3Comparable(291.858643, 123.691452, 321.282837), 80),
			new ParticleSpawn(new Vec3Comparable(292.200195, 123.695419, 321.647980), 32),
			new ParticleSpawn(new Vec3Comparable(286.064362, 123.623970, 315.064484), 51)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.SOLVED);

	Vec3i set2Solution = new Vec3i(705, 128, 764);

	@BeforeEach
	void setUp() {
		NEUDebugLogger.logMethod = 	CrystalWishingCompassSolverTest::neuDebugLog;
		NEUDebugLogger.allFlagsEnabled = true;
		solver.initWorld();
		systemTimeMillis = 0;
		solver.currentTimeMillis = () -> (systemTimeMillis);
	}

	private void checkSolution(Solution solution) {
		int index = 0;
		for (CompassUse compassUse : solution.compassUses) {
			systemTimeMillis += compassUse.timeIncrementMillis;
			HandleCompassResult handleCompassResult = solver.handleCompassUse(compassUse.playerPos);
			Assertions.assertEquals(compassUse.expectedHandleCompassUseResult,
				handleCompassResult,
				"CompassUse index " + index);

			for (ParticleSpawn particle : compassUse.particles) {
				systemTimeMillis += particle.timeIncrementMillis;
				solver.solveUsingParticle(
					particle.spawnLocation.xCoord,
					particle.spawnLocation.yCoord,
					particle.spawnLocation.zCoord,
					systemTimeMillis);
			}

			Assertions.assertEquals(compassUse.expectedSolverState,
				solver.getSolverState(),
				"CompassUse index " + index);
			if (compassUse.expectedSolverState == SolverState.SOLVED) {
				Assertions.assertEquals(solution.expectedSolutionCoords,
					solver.getSolutionCoords());
			}

			index++;
		}
	}

	@Test
	void first_compass_without_particles_sets_solver_state_to_processing_first_use() {
		CompassUse compassUse = new CompassUse(compassUse1Set1);
		compassUse.particles.clear();
		compassUse.expectedSolverState = SolverState.PROCESSING_FIRST_USE;

		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse)),
			Vec3i.NULL_VECTOR);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void first_compass_with_repeating_particles_sets_state_to_need_second_compass() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set1)),
			Vec3i.NULL_VECTOR);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void use_while_handling_previous_returns_still_processing_first_use() {
		// Arrange
		CompassUse compassUse1 = new CompassUse(compassUse1Set1);
		compassUse1.particles.clear();
		compassUse1.expectedSolverState = SolverState.PROCESSING_FIRST_USE;

		// STILL_PROCESSING_FIRST_USE is expected instead of LOCATION_TOO_CLOSE since the solver
		// isn't ready for the second compass use, which includes the location check
		CompassUse compassUse2 = new CompassUse(compassUse1);
		compassUse2.expectedHandleCompassUseResult = HandleCompassResult.STILL_PROCESSING_FIRST_USE;

		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1, compassUse2)),
			Vec3i.NULL_VECTOR);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void missing_repeating_particles_sets_state_to_failed_timeout_no_repeating() {
		CompassUse compassUse = new CompassUse(compassUse1Set1);
		compassUse.particles.remove(compassUse.particles.size()-1);
		compassUse.particles.get(compassUse.particles.size()-1).timeIncrementMillis += 5000;
		compassUse.expectedSolverState = SolverState.FAILED_TIMEOUT_NO_REPEATING;

		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse)),
			Vec3i.NULL_VECTOR);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void compasses_too_close_returns_location_too_close_and_solver_state_is_still_need_second_compass() {
		// Arrange
		CompassUse secondCompassUse = new CompassUse(
			5000,
			compassUse1Set1.playerPos = compassUse1Set1.playerPos.add(2, 2, 2),
			null,
			HandleCompassResult.LOCATION_TOO_CLOSE,
			SolverState.NEED_SECOND_COMPASS
			);

		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set1, secondCompassUse)),
			Vec3i.NULL_VECTOR);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void second_compass_sets_solver_state_to_processing_second_use() {
		// Arrange
		CompassUse secondCompassUse = new CompassUse(compassUse2Set1);
		secondCompassUse.expectedSolverState = SolverState.PROCESSING_SECOND_USE;
		secondCompassUse.particles.clear();

		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set1, compassUse2Set1)),
			set1Solution
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void second_compass_with_repeating_particles_sets_state_to_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set1, compassUse2Set1)),
			set1Solution
		);

		// Act & Assert
		checkSolution(solution);
	}

	private void execInvalidParticlesInvalidSolution() {
		// Arrange
		CompassUse compassUse2 = new CompassUse(compassUse2Set1);
		// trim the repeat particle off
		compassUse2.particles.remove(compassUse2.particles.size()-1);
		Collections.reverse(compassUse2.particles);
		// add a new repeat particle
		compassUse2.particles.add(new ParticleSpawn(compassUse2.particles.get(0)));
		compassUse2.expectedSolverState = SolverState.FAILED_INVALID_SOLUTION;
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set1, compassUse2)),
			Vec3i.NULL_VECTOR);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void second_compass_with_inverted_particles_sets_state_to_invalid_solution() {
		// Arrange, Act, and Assert
		execInvalidParticlesInvalidSolution();
	}

	@Test
	void solution_outside_hollows_sets_state_to_invalid_solution() {
		// Arrange
		CompassUse compassUse1 = new CompassUse(compassUse1Set1);
		CompassUse compassUse2 = new CompassUse(compassUse2Set1);
		Vec3 offset = new Vec3(0.0, 200.0, 0.0);
		compassUse1.playerPos.add(offset.xCoord, offset.yCoord, offset.zCoord);
		for (ParticleSpawn particle : compassUse1.particles) {
			particle.spawnLocation = particle.spawnLocation.add(offset);
		}
		compassUse2.playerPos.add(offset.xCoord, offset.yCoord, offset.zCoord);
		for (ParticleSpawn particle : compassUse2.particles) {
			particle.spawnLocation = particle.spawnLocation.add(offset);
		}
		compassUse2.expectedSolverState = SolverState.FAILED_INVALID_SOLUTION;
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1, compassUse2)),
			Vec3i.NULL_VECTOR);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void second_solution_can_be_solved_after_state_is_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set1, compassUse2Set1)),
			set1Solution
		);
		checkSolution(solution);

		Solution solution2 = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set2, compassUse2Set2)),
			set2Solution
		);

		// Act & Assert
		checkSolution(solution2);
	}

	@Test
	void second_solution_can_be_solved_after_state_is_failed() {
		// Arrange
		execInvalidParticlesInvalidSolution();
		Assertions.assertEquals(solver.getSolverState(), SolverState.FAILED_INVALID_SOLUTION);

		Solution solution2 = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set2, compassUse2Set2)),
			set2Solution
		);

		// Act & Assert
		checkSolution(solution2);
	}

	@Test
	void distant_particles_are_ignored() {
		// Arrange
		CompassUse compassUse = new CompassUse(compassUse1Set1);
		compassUse.particles.get(2).spawnLocation.addVector(100.0, 100.0, 100.0);
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1Set1)),
			Vec3i.NULL_VECTOR);

		// Act & Assert
		checkSolution(solution);
	}

	// Represents a particle spawn, including:
	// - Milliseconds to increment the "system time" prior to spawn.
	// - The particle spawn location.
	static class ParticleSpawn {
		long timeIncrementMillis;
		Vec3Comparable spawnLocation;

		ParticleSpawn(Vec3Comparable spawnLocation, long timeIncrementMillis) {
			this.timeIncrementMillis = timeIncrementMillis;
			this.spawnLocation = spawnLocation;
		}

		ParticleSpawn(ParticleSpawn source) {
			timeIncrementMillis = source.timeIncrementMillis;
			spawnLocation = new Vec3Comparable(source.spawnLocation);
		}
	}

	// Represents a use of the wishing compass, including:
	// - Milliseconds to increment the "system time" prior to use.
	// - The player's position when the compass is used.
	// - The resulting set of particles
	// - The expected state of the wishing compass solver after this compass is used
	static class CompassUse {
		long timeIncrementMillis;
		BlockPos playerPos;
		ArrayList<ParticleSpawn> particles;
		HandleCompassResult expectedHandleCompassUseResult;
		SolverState expectedSolverState;

		CompassUse(long timeIncrementMillis,
							 BlockPos playerPos,
							 ArrayList<ParticleSpawn> particles,
							 HandleCompassResult expectedHandleCompassUseResult,
							 SolverState expectedState) {
			this.timeIncrementMillis = timeIncrementMillis;
			this.playerPos = playerPos;
			this.particles = particles != null ? particles : new ArrayList<>();
			this.expectedHandleCompassUseResult = expectedHandleCompassUseResult;
			this.expectedSolverState = expectedState;
		}

		CompassUse(CompassUse source) {
			this.timeIncrementMillis = source.timeIncrementMillis;
			this.playerPos = new BlockPos(source.playerPos);
			this.particles = new ArrayList<>(source.particles);
			this.expectedHandleCompassUseResult = source.expectedHandleCompassUseResult;
			this.expectedSolverState = source.expectedSolverState;
		}
	}

	static class Solution {
		ArrayList<CompassUse> compassUses;
		Vec3i expectedSolutionCoords;

		Solution(ArrayList<CompassUse> compassUses, Vec3i expectedSolutionCoords) {
			this.compassUses = compassUses;
			this.expectedSolutionCoords = new Vec3i(expectedSolutionCoords.getX(), expectedSolutionCoords.getY(), expectedSolutionCoords.getZ());
		}

		Solution(Solution source, Vec3i expectedSolutionCoords) {
			this.compassUses = new ArrayList<>(source.compassUses);
			this.expectedSolutionCoords = new Vec3i(expectedSolutionCoords.getX(), expectedSolutionCoords.getY(), expectedSolutionCoords.getZ());
		}
	}

	private static void neuDebugLog(String message) {
		System.out.println(message);
	}
}
