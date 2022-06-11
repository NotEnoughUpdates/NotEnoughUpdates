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
import io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver.CompassTarget;
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
import java.util.EnumSet;

import static io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver.ALL_PARTICLES_MAX_MILLIS;
import static io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver.Crystal;
import static io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver.HollowsZone;
import static io.github.moulberry.notenoughupdates.miscfeatures.CrystalWishingCompassSolver.getInstance;

class CrystalWishingCompassSolverTest {
	private static final CrystalWishingCompassSolver solver = getInstance();
	long systemTimeMillis;
	private final long DELAY_AFTER_FIRST_COMPASS_LAST_PARTICLE = 500L;
	private final int CH_LOWEST_VALID_Y = 30;

	private final CompassUse minesOfDivanCompassUse1 = new CompassUse(
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
		SolverState.NEED_SECOND_COMPASS
	);

	private final CompassUse minesOfDivanCompassUse2 = new CompassUse(
		DELAY_AFTER_FIRST_COMPASS_LAST_PARTICLE,
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
		SolverState.SOLVED
	);

	Vec3i minesOfDivanSolution = new Vec3i(735, 98, 451);

	private final CompassUse goblinHoldoutCompassUse1 = new CompassUse(
		1647776326763L,
		new BlockPos(454, 87, 776),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(454.171722, 88.616852, 775.807190), 188),
			new ParticleSpawn(new Vec3Comparable(454.010315, 88.613464, 775.333984), 44),
			new ParticleSpawn(new Vec3Comparable(453.849243, 88.610069, 774.860657), 61),
			new ParticleSpawn(new Vec3Comparable(453.688141, 88.606674, 774.387329), 51),
			new ParticleSpawn(new Vec3Comparable(453.527069, 88.603271, 773.914001), 40),
			new ParticleSpawn(new Vec3Comparable(453.365997, 88.599876, 773.440674), 57),
			new ParticleSpawn(new Vec3Comparable(453.204926, 88.596481, 772.967346), 45),
			new ParticleSpawn(new Vec3Comparable(453.043854, 88.593086, 772.494019), 49),
			new ParticleSpawn(new Vec3Comparable(452.882782, 88.589691, 772.020691), 46),
			new ParticleSpawn(new Vec3Comparable(452.721710, 88.586288, 771.547302), 65),
			new ParticleSpawn(new Vec3Comparable(452.560638, 88.582893, 771.073975), 43),
			new ParticleSpawn(new Vec3Comparable(452.399567, 88.579498, 770.600647), 50),
			new ParticleSpawn(new Vec3Comparable(452.238495, 88.576103, 770.127319), 48),
			new ParticleSpawn(new Vec3Comparable(452.077423, 88.572701, 769.653992), 47),
			new ParticleSpawn(new Vec3Comparable(451.916351, 88.569305, 769.180664), 60),
			new ParticleSpawn(new Vec3Comparable(451.755280, 88.565910, 768.707336), 40),
			new ParticleSpawn(new Vec3Comparable(451.594208, 88.562515, 768.234009), 69),
			new ParticleSpawn(new Vec3Comparable(451.433136, 88.559120, 767.760681), 40),
			new ParticleSpawn(new Vec3Comparable(451.272064, 88.555717, 767.287354), 42),
			new ParticleSpawn(new Vec3Comparable(454.183441, 88.616600, 775.803040), 54)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.NEED_SECOND_COMPASS
	);

	private final CompassUse goblinHoldoutCompassUse2 = new CompassUse(
		DELAY_AFTER_FIRST_COMPASS_LAST_PARTICLE,
		new BlockPos(439, 85, 777),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(439.068848, 86.624870, 776.043701), 136),
			new ParticleSpawn(new Vec3Comparable(438.936066, 86.625786, 775.561646), 46),
			new ParticleSpawn(new Vec3Comparable(438.804352, 86.626595, 775.079346), 65),
			new ParticleSpawn(new Vec3Comparable(438.672699, 86.627396, 774.596985), 40),
			new ParticleSpawn(new Vec3Comparable(438.541016, 86.628197, 774.114624), 51),
			new ParticleSpawn(new Vec3Comparable(438.409332, 86.628998, 773.632263), 50),
			new ParticleSpawn(new Vec3Comparable(438.277679, 86.629799, 773.149902), 50),
			new ParticleSpawn(new Vec3Comparable(438.145996, 86.630608, 772.667603), 56),
			new ParticleSpawn(new Vec3Comparable(438.014343, 86.631409, 772.185242), 40),
			new ParticleSpawn(new Vec3Comparable(437.882660, 86.632210, 771.702881), 65),
			new ParticleSpawn(new Vec3Comparable(437.751007, 86.633011, 771.220520), 45),
			new ParticleSpawn(new Vec3Comparable(437.619324, 86.633812, 770.738159), 42),
			new ParticleSpawn(new Vec3Comparable(437.487671, 86.634613, 770.255798), 60),
			new ParticleSpawn(new Vec3Comparable(437.355988, 86.635414, 769.773499), 51),
			new ParticleSpawn(new Vec3Comparable(437.224335, 86.636215, 769.291138), 44),
			new ParticleSpawn(new Vec3Comparable(437.092651, 86.637024, 768.808777), 56),
			new ParticleSpawn(new Vec3Comparable(436.960999, 86.637825, 768.326416), 56),
			new ParticleSpawn(new Vec3Comparable(436.829315, 86.638626, 767.844055), 40),
			new ParticleSpawn(new Vec3Comparable(436.697632, 86.639427, 767.361694), 50),
			new ParticleSpawn(new Vec3Comparable(436.565979, 86.640228, 766.879395), 46),
			new ParticleSpawn(new Vec3Comparable(439.108551, 86.620811, 776.031067), 0)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.SOLVED
	);

	Vec3i goblinHoldoutKingSolution = new Vec3i(377, 87, 550);
	Vec3i goblinHoldoutQueenSolution = new Vec3i(322, 139, 769);

	private final CompassUse precursorCityCompassUse1 = new CompassUse(
		1647744920365L,
		new BlockPos(570, 120, 565),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(570.428955, 121.630745, 565.674500), 192),
			new ParticleSpawn(new Vec3Comparable(570.572998, 121.642563, 566.153137), 52),
			new ParticleSpawn(new Vec3Comparable(570.714233, 121.654442, 566.632629), 45),
			new ParticleSpawn(new Vec3Comparable(570.855286, 121.666321, 567.112183), 51),
			new ParticleSpawn(new Vec3Comparable(570.996338, 121.678200, 567.591736), 0),
			new ParticleSpawn(new Vec3Comparable(571.137390, 121.690079, 568.071289), 111),
			new ParticleSpawn(new Vec3Comparable(571.278442, 121.701958, 568.550781), 38),
			new ParticleSpawn(new Vec3Comparable(571.419495, 121.713844, 569.030334), 51),
			new ParticleSpawn(new Vec3Comparable(571.560547, 121.725723, 569.509888), 49),
			new ParticleSpawn(new Vec3Comparable(571.701599, 121.737602, 569.989441), 0),
			new ParticleSpawn(new Vec3Comparable(571.842651, 121.749481, 570.468994), 101),
			new ParticleSpawn(new Vec3Comparable(571.983704, 121.761360, 570.948547), 53),
			new ParticleSpawn(new Vec3Comparable(572.124756, 121.773239, 571.428101), 47),
			new ParticleSpawn(new Vec3Comparable(572.265747, 121.785118, 571.907654), 49),
			new ParticleSpawn(new Vec3Comparable(572.406799, 121.796997, 572.387207), 49),
			new ParticleSpawn(new Vec3Comparable(572.547852, 121.808876, 572.866699), 51),
			new ParticleSpawn(new Vec3Comparable(572.688904, 121.820755, 573.346252), 57),
			new ParticleSpawn(new Vec3Comparable(572.829956, 121.832634, 573.825806), 42),
			new ParticleSpawn(new Vec3Comparable(572.971008, 121.844513, 574.305359), 50),
			new ParticleSpawn(new Vec3Comparable(573.112061, 121.856392, 574.784912), 52),
			new ParticleSpawn(new Vec3Comparable(570.372192, 121.631874, 565.694946), 0)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.NEED_SECOND_COMPASS
	);

	private final CompassUse precursorCityCompassUse2 = new CompassUse(
		DELAY_AFTER_FIRST_COMPASS_LAST_PARTICLE,
		new BlockPos(591, 136, 579),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(590.847961, 137.589584, 579.776672), 192),
			new ParticleSpawn(new Vec3Comparable(590.918945, 137.528259, 580.267761), 50),
			new ParticleSpawn(new Vec3Comparable(590.985229, 137.465118, 580.759338), 56),
			new ParticleSpawn(new Vec3Comparable(591.051147, 137.401855, 581.250916), 47),
			new ParticleSpawn(new Vec3Comparable(591.117126, 137.338593, 581.742493), 47),
			new ParticleSpawn(new Vec3Comparable(591.183044, 137.275330, 582.234070), 49),
			new ParticleSpawn(new Vec3Comparable(591.249023, 137.212067, 582.725647), 60),
			new ParticleSpawn(new Vec3Comparable(591.314941, 137.148804, 583.217224), 55),
			new ParticleSpawn(new Vec3Comparable(591.380920, 137.085541, 583.708801), 47),
			new ParticleSpawn(new Vec3Comparable(591.446838, 137.022263, 584.200378), 50),
			new ParticleSpawn(new Vec3Comparable(591.512817, 136.959000, 584.691956), 39),
			new ParticleSpawn(new Vec3Comparable(591.578735, 136.895737, 585.183533), 53),
			new ParticleSpawn(new Vec3Comparable(591.644714, 136.832474, 585.675110), 53),
			new ParticleSpawn(new Vec3Comparable(591.710632, 136.769211, 586.166687), 45),
			new ParticleSpawn(new Vec3Comparable(591.776611, 136.705948, 586.658264), 79),
			new ParticleSpawn(new Vec3Comparable(591.842529, 136.642685, 587.149841), 20),
			new ParticleSpawn(new Vec3Comparable(591.908508, 136.579407, 587.641418), 62),
			new ParticleSpawn(new Vec3Comparable(591.974426, 136.516144, 588.132996), 48),
			new ParticleSpawn(new Vec3Comparable(592.040344, 136.452881, 588.624573), 40),
			new ParticleSpawn(new Vec3Comparable(592.106323, 136.389618, 589.116150), 51),
			new ParticleSpawn(new Vec3Comparable(590.766357, 137.556885, 579.791565), 0)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.SOLVED
	);

	Vec3i precursorCitySolution = new Vec3i(604, 124, 681);

	private final CompassUse jungleCompassUse1 = new CompassUse(
		1647744980313L,
		new BlockPos(454, 122, 459),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(453.954895, 122.958122, 458.687866), 141),
			new ParticleSpawn(new Vec3Comparable(453.515991, 122.760010, 458.553314), 59),
			new ParticleSpawn(new Vec3Comparable(453.078156, 122.560112, 458.417877), 41),
			new ParticleSpawn(new Vec3Comparable(452.640381, 122.360123, 458.282349), 50),
			new ParticleSpawn(new Vec3Comparable(452.202606, 122.160133, 458.146851), 66),
			new ParticleSpawn(new Vec3Comparable(451.764832, 121.960136, 458.011353), 35),
			new ParticleSpawn(new Vec3Comparable(451.327057, 121.760147, 457.875854), 49),
			new ParticleSpawn(new Vec3Comparable(450.889313, 121.560150, 457.740356), 50),
			new ParticleSpawn(new Vec3Comparable(450.451538, 121.360161, 457.604858), 49),
			new ParticleSpawn(new Vec3Comparable(450.013763, 121.160172, 457.469330), 51),
			new ParticleSpawn(new Vec3Comparable(449.575989, 120.960175, 457.333832), 59),
			new ParticleSpawn(new Vec3Comparable(449.138214, 120.760185, 457.198334), 41),
			new ParticleSpawn(new Vec3Comparable(448.700439, 120.560196, 457.062836), 55),
			new ParticleSpawn(new Vec3Comparable(448.262695, 120.360199, 456.927338), 50),
			new ParticleSpawn(new Vec3Comparable(447.824921, 120.160210, 456.791840), 49),
			new ParticleSpawn(new Vec3Comparable(447.387146, 119.960213, 456.656311), 53),
			new ParticleSpawn(new Vec3Comparable(446.949371, 119.760223, 456.520813), 43),
			new ParticleSpawn(new Vec3Comparable(446.511597, 119.560234, 456.385315), 51),
			new ParticleSpawn(new Vec3Comparable(446.073853, 119.360237, 456.249817), 49),
			new ParticleSpawn(new Vec3Comparable(445.636078, 119.160248, 456.114319), 56),
			new ParticleSpawn(new Vec3Comparable(453.975647, 122.920158, 458.668488), 0)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.NEED_SECOND_COMPASS
	);

	private final CompassUse jungleCompassUse2 = new CompassUse(
		DELAY_AFTER_FIRST_COMPASS_LAST_PARTICLE,
		new BlockPos(438, 126, 468),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(437.701721, 127.395279, 467.455048), 139),
			new ParticleSpawn(new Vec3Comparable(437.297852, 127.161415, 467.275604), 35),
			new ParticleSpawn(new Vec3Comparable(436.895813, 126.927208, 467.092529), 68),
			new ParticleSpawn(new Vec3Comparable(436.493896, 126.692986, 466.909241), 41),
			new ParticleSpawn(new Vec3Comparable(436.091980, 126.458763, 466.725952), 54),
			new ParticleSpawn(new Vec3Comparable(435.690033, 126.224533, 466.542664), 39),
			new ParticleSpawn(new Vec3Comparable(435.288116, 125.990311, 466.359375), 52),
			new ParticleSpawn(new Vec3Comparable(434.886200, 125.756088, 466.176086), 66),
			new ParticleSpawn(new Vec3Comparable(434.484283, 125.521866, 465.992767), 40),
			new ParticleSpawn(new Vec3Comparable(434.082367, 125.287636, 465.809479), 41),
			new ParticleSpawn(new Vec3Comparable(433.680420, 125.053413, 465.626190), 50),
			new ParticleSpawn(new Vec3Comparable(433.278503, 124.819191, 465.442902), 59),
			new ParticleSpawn(new Vec3Comparable(432.876587, 124.584969, 465.259613), 54),
			new ParticleSpawn(new Vec3Comparable(432.474670, 124.350746, 465.076294), 38),
			new ParticleSpawn(new Vec3Comparable(432.072723, 124.116516, 464.893005), 63),
			new ParticleSpawn(new Vec3Comparable(431.670807, 123.882294, 464.709717), 36),
			new ParticleSpawn(new Vec3Comparable(431.268890, 123.648071, 464.526428), 64),
			new ParticleSpawn(new Vec3Comparable(430.866974, 123.413849, 464.343140), 48),
			new ParticleSpawn(new Vec3Comparable(430.465057, 123.179619, 464.159821), 53),
			new ParticleSpawn(new Vec3Comparable(430.063110, 122.945396, 463.976532), 46),
			new ParticleSpawn(new Vec3Comparable(437.732666, 127.385803, 467.381592), 1)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.SOLVED
	);

	Vec3i jungleSolution = new Vec3i(343, 72, 424);
	Vec3i jungleSolutionTempleDoor = new Vec3i(
		jungleSolution.getX() - 57,
		jungleSolution.getY() + 36,
		jungleSolution.getZ() - 21
	);

	private final CompassUse magmaCompassUse1 = new CompassUse(
		1647745029814L,
		new BlockPos(462, 58, 550),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(462.226898, 59.614380, 550.032654), 160),
			new ParticleSpawn(new Vec3Comparable(462.693848, 59.609089, 549.853943), 47),
			new ParticleSpawn(new Vec3Comparable(463.160706, 59.603809, 549.674988), 48),
			new ParticleSpawn(new Vec3Comparable(463.627533, 59.598526, 549.496033), 136),
			new ParticleSpawn(new Vec3Comparable(464.094391, 59.593246, 549.317017), 0),
			new ParticleSpawn(new Vec3Comparable(464.561218, 59.587963, 549.138062), 0),
			new ParticleSpawn(new Vec3Comparable(465.028076, 59.582684, 548.959106), 53),
			new ParticleSpawn(new Vec3Comparable(465.494904, 59.577400, 548.780090), 48),
			new ParticleSpawn(new Vec3Comparable(465.961761, 59.572117, 548.601135), 55),
			new ParticleSpawn(new Vec3Comparable(466.428589, 59.566837, 548.422180), 47),
			new ParticleSpawn(new Vec3Comparable(466.895416, 59.561554, 548.243164), 46),
			new ParticleSpawn(new Vec3Comparable(467.362274, 59.556274, 548.064209), 53),
			new ParticleSpawn(new Vec3Comparable(467.829102, 59.550991, 547.885254), 50),
			new ParticleSpawn(new Vec3Comparable(468.295959, 59.545712, 547.706238), 54),
			new ParticleSpawn(new Vec3Comparable(468.762787, 59.540428, 547.527283), 52),
			new ParticleSpawn(new Vec3Comparable(469.229645, 59.535145, 547.348328), 105),
			new ParticleSpawn(new Vec3Comparable(469.696472, 59.529865, 547.169312), 1),
			new ParticleSpawn(new Vec3Comparable(470.163300, 59.524582, 546.990356), 51),
			new ParticleSpawn(new Vec3Comparable(470.630157, 59.519302, 546.811401), 40),
			new ParticleSpawn(new Vec3Comparable(471.096985, 59.514019, 546.632385), 49),
			new ParticleSpawn(new Vec3Comparable(462.221954, 59.614719, 550.019165), 0)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.NEED_SECOND_COMPASS
	);

	private final CompassUse magmaCompassUse2 = new CompassUse(
		DELAY_AFTER_FIRST_COMPASS_LAST_PARTICLE,
		new BlockPos(449, 53, 556),
		new ArrayList<>(Arrays.asList(
			new ParticleSpawn(new Vec3Comparable(449.120911, 54.624340, 556.108948), 204),
			new ParticleSpawn(new Vec3Comparable(449.587433, 54.627399, 555.929138), 102),
			new ParticleSpawn(new Vec3Comparable(450.053741, 54.630432, 555.748657), 0),
			new ParticleSpawn(new Vec3Comparable(450.520020, 54.633465, 555.568237), 62),
			new ParticleSpawn(new Vec3Comparable(450.986298, 54.636497, 555.387756), 38),
			new ParticleSpawn(new Vec3Comparable(451.452606, 54.639530, 555.207275), 48),
			new ParticleSpawn(new Vec3Comparable(451.918884, 54.642563, 555.026794), 63),
			new ParticleSpawn(new Vec3Comparable(452.385162, 54.645596, 554.846375), 52),
			new ParticleSpawn(new Vec3Comparable(452.851471, 54.648628, 554.665894), 35),
			new ParticleSpawn(new Vec3Comparable(453.317749, 54.651661, 554.485413), 53),
			new ParticleSpawn(new Vec3Comparable(453.784027, 54.654694, 554.304993), 54),
			new ParticleSpawn(new Vec3Comparable(454.250305, 54.657726, 554.124512), 50),
			new ParticleSpawn(new Vec3Comparable(454.716614, 54.660759, 553.944031), 55),
			new ParticleSpawn(new Vec3Comparable(455.182892, 54.663792, 553.763550), 49),
			new ParticleSpawn(new Vec3Comparable(455.649170, 54.666824, 553.583130), 41),
			new ParticleSpawn(new Vec3Comparable(456.115479, 54.669857, 553.402649), 48),
			new ParticleSpawn(new Vec3Comparable(456.581757, 54.672890, 553.222168), 54),
			new ParticleSpawn(new Vec3Comparable(457.048035, 54.675922, 553.041687), 45),
			new ParticleSpawn(new Vec3Comparable(457.514313, 54.678959, 552.861267), 55),
			new ParticleSpawn(new Vec3Comparable(449.110443, 54.623035, 556.079163), 49)
		)),
		HandleCompassResult.SUCCESS,
		SolverState.SOLVED
	);

	Vec3i magmaSolution = new Vec3i(737, 56, 444);

	Vec3Comparable kingOdawaMinesOrNucleusCoordsInRemnants = new Vec3Comparable(566, 100, 566);
	Vec3Comparable queenKingOdawaOrCityNucleusCoordsInMithrilDeposits = new Vec3Comparable(566, 130, 466);
	Vec3Comparable odawaSolution = new Vec3Comparable(349, 110, 390);

	private final CompassUse nucleusCompass = new CompassUse(
		1647745029814L,
		new BlockPos(512, 106, 512),
		null,
		HandleCompassResult.PLAYER_IN_NUCLEUS,
		SolverState.NOT_STARTED
	);

	private void resetSolverState() {
		solver.initWorld();
		systemTimeMillis = 0;
		solver.currentTimeMillis = () -> (systemTimeMillis);
		// These must be overridden for all test cases or an exception will be thrown when
		// data that is only present when running in the context of Minecraft is accessed.
		solver.keyInInventory = () -> false;
		solver.kingsScentPresent = () -> false;
		solver.foundCrystals = () -> EnumSet.noneOf(Crystal.class);
	}

	@BeforeEach
	void setUp() {
		NEUDebugLogger.logMethod = CrystalWishingCompassSolverTest::neuDebugLog;
		NEUDebugLogger.allFlagsEnabled = true;
		resetSolverState();
	}

	private void checkSolution(Solution solution) {
		int index = 0;
		for (CompassUse compassUse : solution.compassUses) {
			systemTimeMillis += compassUse.timeIncrementMillis;
			HandleCompassResult handleCompassResult = solver.handleCompassUse(compassUse.playerPos);
			Assertions.assertEquals(
				compassUse.expectedHandleCompassUseResult,
				handleCompassResult,
				"CompassUse index " + index
			);

			for (ParticleSpawn particle : compassUse.particles) {
				systemTimeMillis += particle.timeIncrementMillis;
				solver.solveUsingParticle(
					particle.spawnLocation.xCoord,
					particle.spawnLocation.yCoord,
					particle.spawnLocation.zCoord,
					systemTimeMillis
				);
			}

			Assertions.assertEquals(
				compassUse.expectedSolverState,
				solver.getSolverState(),
				"CompassUse index " + index
			);
			if (compassUse.expectedSolverState == SolverState.SOLVED) {
				Assertions.assertEquals(
					solution.expectedSolutionCoords,
					solver.getSolutionCoords()
				);
			}

			index++;
		}
	}

	@Test
	void first_compass_without_particles_sets_solver_state_to_processing_first_use() {
		// Arrange
		CompassUse compassUse = new CompassUse(minesOfDivanCompassUse1);
		compassUse.particles.clear();
		compassUse.expectedSolverState = SolverState.PROCESSING_FIRST_USE;

		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(compassUse)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void new_compass_resets_processing_first_use_state_after_timeout() {
		// Arrange
		CompassUse processingFirstUseCompassUse = new CompassUse(minesOfDivanCompassUse1);
		processingFirstUseCompassUse.particles.clear();
		processingFirstUseCompassUse.expectedSolverState = SolverState.PROCESSING_FIRST_USE;
		Solution processingFirstUseSolution = new Solution(
			new ArrayList<>(Collections.singletonList(processingFirstUseCompassUse)),
			Vec3i.NULL_VECTOR
		);
		checkSolution(processingFirstUseSolution);
		Assertions.assertEquals(SolverState.PROCESSING_FIRST_USE, solver.getSolverState());

		CompassUse resetStateCompassUse = new CompassUse(jungleCompassUse1);
		resetStateCompassUse.timeIncrementMillis = ALL_PARTICLES_MAX_MILLIS + 1;
		resetStateCompassUse.expectedHandleCompassUseResult = HandleCompassResult.NO_PARTICLES_FOR_PREVIOUS_COMPASS;
		resetStateCompassUse.expectedSolverState = SolverState.FAILED_TIMEOUT_NO_REPEATING;
		Solution goodSolution = new Solution(
			new ArrayList<>(Collections.singletonList(resetStateCompassUse)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(goodSolution);
	}

	@Test
	void first_compass_with_repeating_particles_sets_state_to_need_second_compass() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(minesOfDivanCompassUse1)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void first_compass_in_nucleus_sets_state_to_player_in_nucleus() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(nucleusCompass)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void use_while_handling_previous_returns_still_processing_first_use() {
		// Arrange
		CompassUse compassUse1 = new CompassUse(
			1647528732979L,
			new BlockPos(754, 137, 239),
			new ArrayList<>(Collections.singletonList(
				new ParticleSpawn(new Vec3Comparable(754.358459, 138.536407, 239.200928), 137)
			)),
			HandleCompassResult.SUCCESS,
			SolverState.PROCESSING_FIRST_USE
		);

		// STILL_PROCESSING_FIRST_USE is expected instead of LOCATION_TOO_CLOSE since the solver
		// isn't ready for the second compass use, which includes the location check
		CompassUse compassUse2 = new CompassUse(compassUse1);
		compassUse2.expectedHandleCompassUseResult = HandleCompassResult.STILL_PROCESSING_PRIOR_USE;
		compassUse2.timeIncrementMillis = 500;

		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1, compassUse2)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void missing_repeating_particles_sets_state_to_failed_timeout_no_repeating() {
		// Arrange
		CompassUse compassUse = new CompassUse(minesOfDivanCompassUse1);
		compassUse.particles.remove(compassUse.particles.size() - 1);
		compassUse.particles.get(compassUse.particles.size() - 1).timeIncrementMillis += ALL_PARTICLES_MAX_MILLIS;
		compassUse.expectedSolverState = SolverState.FAILED_TIMEOUT_NO_REPEATING;

		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(compassUse)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void compasses_too_close_returns_location_too_close_and_solver_state_is_still_need_second_compass() {
		// Arrange
		CompassUse secondCompassUse = new CompassUse(
			DELAY_AFTER_FIRST_COMPASS_LAST_PARTICLE,
			minesOfDivanCompassUse1.playerPos.add(2, 2, 2),
			null,
			HandleCompassResult.LOCATION_TOO_CLOSE,
			SolverState.NEED_SECOND_COMPASS
		);

		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(minesOfDivanCompassUse1, secondCompassUse)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void second_compass_sets_solver_state_to_processing_second_use() {
		// Arrange
		CompassUse secondCompassUse = new CompassUse(minesOfDivanCompassUse2);
		secondCompassUse.expectedSolverState = SolverState.PROCESSING_SECOND_USE;
		secondCompassUse.particles.clear();

		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(minesOfDivanCompassUse1, minesOfDivanCompassUse2)),
			minesOfDivanSolution
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void second_compass_with_repeating_particles_sets_state_to_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(minesOfDivanCompassUse1, minesOfDivanCompassUse2)),
			minesOfDivanSolution
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void particles_from_first_compass_are_ignored_by_second_compass() {
		// Arrange
		CompassUse compassUse2 = new CompassUse(minesOfDivanCompassUse2);
		compassUse2.particles.add(0, minesOfDivanCompassUse1.particles.get(0));
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(minesOfDivanCompassUse1, compassUse2)),
			minesOfDivanSolution
		);

		// Act & Assert
		checkSolution(solution);
	}

	private void execInvalidParticlesInvalidSolution() {
		// Arrange
		CompassUse compassUse2 = new CompassUse(minesOfDivanCompassUse2);

		// reverse the direction of the particles, moving the repeat particle
		// to "new" end
		compassUse2.particles.remove(compassUse2.particles.size() - 1);
		Collections.reverse(compassUse2.particles);
		// add a new repeat particle
		compassUse2.particles.add(new ParticleSpawn(compassUse2.particles.get(0)));

		// Adjust the player position
		compassUse2.playerPos = new BlockPos(compassUse2.particles.get(0).spawnLocation);
		compassUse2.expectedSolverState = SolverState.FAILED_INVALID_SOLUTION;
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(minesOfDivanCompassUse1, compassUse2)),
			Vec3i.NULL_VECTOR
		);

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
		CompassUse compassUse1 = new CompassUse(minesOfDivanCompassUse1);
		CompassUse compassUse2 = new CompassUse(minesOfDivanCompassUse2);
		double invalidYOffset = -(minesOfDivanSolution.getY() - (CH_LOWEST_VALID_Y - 1));
		Vec3 offset = new Vec3(0.0, invalidYOffset, 0.0);

		compassUse1.playerPos = compassUse1.playerPos.add(offset.xCoord, offset.yCoord, offset.zCoord);
		for (ParticleSpawn particle : compassUse1.particles) {
			particle.spawnLocation = particle.spawnLocation.add(offset);
		}

		compassUse2.playerPos = compassUse2.playerPos.add(offset.xCoord, offset.yCoord, offset.zCoord);
		for (ParticleSpawn particle : compassUse2.particles) {
			particle.spawnLocation = particle.spawnLocation.add(offset);
		}
		compassUse2.expectedSolverState = SolverState.FAILED_INVALID_SOLUTION;

		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(compassUse1, compassUse2)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void second_solution_can_be_solved_after_state_is_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(minesOfDivanCompassUse1, minesOfDivanCompassUse2)),
			minesOfDivanSolution
		);
		checkSolution(solution);

		Solution solution2 = new Solution(
			new ArrayList<>(Arrays.asList(precursorCityCompassUse1, precursorCityCompassUse2)),
			precursorCitySolution
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
			new ArrayList<>(Arrays.asList(precursorCityCompassUse1, precursorCityCompassUse2)),
			precursorCitySolution
		);

		// Act & Assert
		checkSolution(solution2);
	}

	@Test
	void distant_particles_are_ignored() {
		// Arrange
		CompassUse compassUse = new CompassUse(minesOfDivanCompassUse1);
		compassUse.particles.get(2).spawnLocation.addVector(100.0, 100.0, 100.0);
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(minesOfDivanCompassUse1)),
			Vec3i.NULL_VECTOR
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void possible_targets_includes_queen_and_excludes_king_when_kings_scent_is_present() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(goblinHoldoutCompassUse1)),
			Vec3i.NULL_VECTOR
		);
		solver.kingsScentPresent = () -> true;

		// Act
		checkSolution(solution);
		EnumSet<CompassTarget> targets = solver.getPossibleTargets();

		// Assert
		Assertions.assertTrue(targets.contains(CompassTarget.GOBLIN_QUEEN));
		Assertions.assertFalse(targets.contains(CompassTarget.GOBLIN_KING));
	}

	@Test
	void possible_targets_excludes_king_and_includes_queen_when_kings_scent_is_not_present() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(goblinHoldoutCompassUse1)),
			Vec3i.NULL_VECTOR
		);
		solver.kingsScentPresent = () -> false;

		// Act
		checkSolution(solution);
		EnumSet<CompassTarget> targets = solver.getPossibleTargets();

		// Assert
		Assertions.assertFalse(targets.contains(CompassTarget.GOBLIN_QUEEN));
		Assertions.assertTrue(targets.contains(CompassTarget.GOBLIN_KING));
	}

	@Test
	void possible_targets_excludes_odawa_and_includes_temple_when_key_in_inventory() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(jungleCompassUse1)),
			Vec3i.NULL_VECTOR
		);
		solver.keyInInventory = () -> true;

		// Act
		checkSolution(solution);
		EnumSet<CompassTarget> targets = solver.getPossibleTargets();

		// Assert
		Assertions.assertFalse(targets.contains(CompassTarget.ODAWA));
		Assertions.assertTrue(targets.contains(CompassTarget.JUNGLE_TEMPLE));
	}

	@Test
	void possible_targets_includes_odawa_and_excludes_temple_when_key_not_in_inventory() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(jungleCompassUse1)),
			Vec3i.NULL_VECTOR
		);
		solver.keyInInventory = () -> false;

		// Act
		checkSolution(solution);
		EnumSet<CompassTarget> targets = solver.getPossibleTargets();

		// Assert
		Assertions.assertTrue(targets.contains(CompassTarget.ODAWA));
		Assertions.assertFalse(targets.contains(CompassTarget.JUNGLE_TEMPLE));
	}

	@Test
	void possible_targets_contains_all_valid_targets_when_all_crystals_missing() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(precursorCityCompassUse1)),
			Vec3i.NULL_VECTOR
		);
		solver.foundCrystals = () -> EnumSet.noneOf(Crystal.class);
		solver.keyInInventory = () -> false;
		solver.kingsScentPresent = () -> false;

		// Act
		checkSolution(solution);
		EnumSet<CompassTarget> targets = solver.getPossibleTargets();

		// Assert
		Assertions.assertTrue(targets.contains(CompassTarget.CRYSTAL_NUCLEUS));
		Assertions.assertTrue(targets.contains(CompassTarget.ODAWA));
		Assertions.assertTrue(targets.contains(CompassTarget.MINES_OF_DIVAN));
		Assertions.assertTrue(targets.contains(CompassTarget.GOBLIN_KING));
		Assertions.assertTrue(targets.contains(CompassTarget.PRECURSOR_CITY));
		Assertions.assertTrue(targets.contains(CompassTarget.BAL));
		// No key or king's scent, so these should be false
		Assertions.assertFalse(targets.contains(CompassTarget.JUNGLE_TEMPLE));
		Assertions.assertFalse(targets.contains(CompassTarget.GOBLIN_QUEEN));
	}

	private void CheckExcludedTargetsForCrystals(
		CompassUse compassUseToExecute,
		ArrayList<CompassTarget> excludedTargets,
		EnumSet<Crystal> foundCrystals
	) {
		// Arrange
		EnumSet<CompassTarget> targets;
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(compassUseToExecute)),
			Vec3i.NULL_VECTOR
		);

		// Act
		checkSolution(solution);
		targets = solver.getPossibleTargets();
		boolean targetFound = false;
		for (CompassTarget target : excludedTargets) {
			if (targets.contains(target)) {
				targetFound = true;
				break;
			}
		}
		Assertions.assertTrue(targetFound);

		resetSolverState();
		solver.foundCrystals = () -> foundCrystals;
		checkSolution(solution);
		targets = solver.getPossibleTargets();

		// Assert
		for (CompassTarget target : excludedTargets) {
			Assertions.assertFalse(targets.contains(target));
		}
	}

	@Test
	void possible_targets_excludes_king_and_queen_when_amber_crystal_found() {
		// Arrange
		ArrayList<CompassTarget> excludedTargets = new ArrayList<>(Arrays.asList(
			CompassTarget.GOBLIN_KING,
			CompassTarget.GOBLIN_QUEEN
		));

		// Act & Assert
		CheckExcludedTargetsForCrystals(goblinHoldoutCompassUse1, excludedTargets, EnumSet.of(Crystal.AMBER));
	}

	@Test
	void possible_targets_excludes_odawa_and_temple_when_amethyst_crystal_found() {
		// Arrange
		ArrayList<CompassTarget> excludedTargets = new ArrayList<>(Arrays.asList(
			CompassTarget.ODAWA,
			CompassTarget.JUNGLE_TEMPLE
		));

		// Act & Assert
		CheckExcludedTargetsForCrystals(jungleCompassUse1, excludedTargets, EnumSet.of(Crystal.AMETHYST));
	}

	@Test
	void possible_targets_excludes_mines_when_jade_crystal_found() {
		// Arrange
		ArrayList<CompassTarget> excludedTargets = new ArrayList<>(Collections.singletonList(
			CompassTarget.MINES_OF_DIVAN));

		// Act & Assert
		CheckExcludedTargetsForCrystals(minesOfDivanCompassUse1, excludedTargets, EnumSet.of(Crystal.JADE));
	}

	@Test
	void possible_targets_excludes_city_when_sapphire_crystal_found() {
		// Arrange
		ArrayList<CompassTarget> excludedTargets = new ArrayList<>(Collections.singletonList(
			CompassTarget.PRECURSOR_CITY));

		// Act & Assert
		CheckExcludedTargetsForCrystals(precursorCityCompassUse1, excludedTargets, EnumSet.of(Crystal.SAPPHIRE));
	}

	@Test
	void possible_targets_excludes_bal_when_topaz_crystal_found() {
		// Arrange
		ArrayList<CompassTarget> excludedTargets = new ArrayList<>(Collections.singletonList(
			CompassTarget.BAL));

		// Act & Assert
		CheckExcludedTargetsForCrystals(magmaCompassUse1, excludedTargets, EnumSet.of(Crystal.TOPAZ));
	}

	@Test
	void solver_resets_when_possible_targets_change_based_on_found_crystals() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(minesOfDivanCompassUse1)),
			Vec3i.NULL_VECTOR
		);

		// Act
		checkSolution(solution);
		systemTimeMillis += minesOfDivanCompassUse2.timeIncrementMillis;
		solver.foundCrystals = () -> EnumSet.of(Crystal.JADE);
		HandleCompassResult handleCompassResult = solver.handleCompassUse(minesOfDivanCompassUse2.playerPos);

		// Assert
		Assertions.assertEquals(HandleCompassResult.POSSIBLE_TARGETS_CHANGED, handleCompassResult);
		Assertions.assertEquals(SolverState.NOT_STARTED, solver.getSolverState());
	}

	@Test
	void solver_resets_when_player_location_changes_zones() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(minesOfDivanCompassUse1)),
			Vec3i.NULL_VECTOR
		);

		// Act
		checkSolution(solution);
		systemTimeMillis += minesOfDivanCompassUse2.timeIncrementMillis;
		BlockPos newLocation = minesOfDivanCompassUse2.playerPos.add(-400, 0, 0);
		HandleCompassResult handleCompassResult = solver.handleCompassUse(newLocation);

		// Assert
		Assertions.assertEquals(HandleCompassResult.POSSIBLE_TARGETS_CHANGED, handleCompassResult);
		Assertions.assertEquals(SolverState.NOT_STARTED, solver.getSolverState());
	}

	@Test
	void solver_resets_based_on_jungle_key_presence() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(jungleCompassUse1)),
			Vec3i.NULL_VECTOR
		);

		// Act
		solver.keyInInventory = () -> false;
		checkSolution(solution);
		systemTimeMillis += jungleCompassUse2.timeIncrementMillis;
		solver.keyInInventory = () -> true;
		HandleCompassResult handleCompassResult = solver.handleCompassUse(jungleCompassUse2.playerPos);

		// Assert
		Assertions.assertEquals(HandleCompassResult.POSSIBLE_TARGETS_CHANGED, handleCompassResult);
		Assertions.assertEquals(SolverState.NOT_STARTED, solver.getSolverState());
	}

	@Test
	void solver_resets_based_on_kings_scent_presence() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Collections.singletonList(goblinHoldoutCompassUse1)),
			Vec3i.NULL_VECTOR
		);

		// Act
		solver.kingsScentPresent = () -> false;
		checkSolution(solution);
		systemTimeMillis += goblinHoldoutCompassUse2.timeIncrementMillis;
		solver.kingsScentPresent = () -> true;
		HandleCompassResult handleCompassResult = solver.handleCompassUse(goblinHoldoutCompassUse2.playerPos);

		// Assert
		Assertions.assertEquals(HandleCompassResult.POSSIBLE_TARGETS_CHANGED, handleCompassResult);
		Assertions.assertEquals(SolverState.NOT_STARTED, solver.getSolverState());
	}

	@Test
	void mines_of_divan_solution_is_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(minesOfDivanCompassUse1, minesOfDivanCompassUse2)),
			minesOfDivanSolution
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void jungle_temple_solution_with_key_in_inventory_is_solved_successfully_excluding_bal() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(jungleCompassUse1, jungleCompassUse2)),
			jungleSolutionTempleDoor
		);
		solver.keyInInventory = () -> true;

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void jungle_temple_solution_is_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(jungleCompassUse1, jungleCompassUse2)),
			jungleSolution
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void precursor_city_solution_is_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(precursorCityCompassUse1, precursorCityCompassUse2)),
			precursorCitySolution
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void goblin_king_solution_is_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(goblinHoldoutCompassUse1, goblinHoldoutCompassUse2)),
			goblinHoldoutKingSolution
		);

		// Act & Assert
		checkSolution(solution);
	}

	@Test
	void bal_solution_is_solved() {
		// Arrange
		Solution solution = new Solution(
			new ArrayList<>(Arrays.asList(magmaCompassUse1, magmaCompassUse2)),
			magmaSolution
		);

		// Act & Assert
		checkSolution(solution);
	}

	EnumSet<CompassTarget> GetSolutionTargetsHelper(
		HollowsZone compassUsedZone,
		EnumSet<Crystal> foundCrystals,
		EnumSet<CompassTarget> possibleTargets,
		Vec3Comparable solutionCoords,
		int expectedSolutionCount
	) {
		EnumSet<CompassTarget> solutionTargets =
			CrystalWishingCompassSolver.getSolutionTargets(
				compassUsedZone,
				foundCrystals,
				possibleTargets,
				solutionCoords
			);
		Assertions.assertEquals(expectedSolutionCount, solutionTargets.size());
		return solutionTargets;
	}

	@Test
	void solutionPossibleTargets_removes_nucleus_when_coords_not_in_nucleus() {
		// Arrange & Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.MITHRIL_DEPOSITS,
			EnumSet.noneOf(Crystal.class),
			EnumSet.allOf(CompassTarget.class),
			new Vec3Comparable(minesOfDivanSolution),
			1
		);

		// Assert
		Assertions.assertFalse(solutionTargets.contains(CompassTarget.CRYSTAL_NUCLEUS));
	}

	@Test
	void solutionPossibleTargets_includes_jungle_temple_and_bal_from_other_zones_when_overlapping() {
		// Arrange
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.ODAWA);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.GOBLIN_HOLDOUT,
			EnumSet.of(Crystal.AMBER),
			possibleTargets,
			new Vec3Comparable(202, 72, 513), // upper left of Goblin Holdout
			2
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.JUNGLE_TEMPLE));
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.BAL));
	}

	@Test
	void solutionPossibleTargets_includes_king_odawa_and_mines_of_divan_from_other_zones_when_overlapping() {
		// Arrange & Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.PRECURSOR_REMNANTS,
			EnumSet.noneOf(Crystal.class),
			EnumSet.allOf(CompassTarget.class),
			kingOdawaMinesOrNucleusCoordsInRemnants,
			3
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.GOBLIN_KING));
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.MINES_OF_DIVAN));
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.ODAWA));
	}

	@Test
	void solutionPossibleTargets_includes_city_and_queen_from_other_zones_when_overlapping() {
		// Arrange
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.GOBLIN_KING);
		possibleTargets.remove(CompassTarget.ODAWA);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.MITHRIL_DEPOSITS,
			EnumSet.noneOf(Crystal.class),
			possibleTargets,
			queenKingOdawaOrCityNucleusCoordsInMithrilDeposits,
			2
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.GOBLIN_QUEEN));
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.PRECURSOR_CITY));
	}

	@Test
	void solutionPossibleTargets_excludes_jungle_temple_from_other_zone_when_not_overlapping() {
		// Arrange
		Vec3Comparable notOverlapping = new Vec3Comparable(202, 72, 513 + 110); // upper left of Goblin Holdout
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.ODAWA);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.GOBLIN_HOLDOUT,
			EnumSet.of(Crystal.AMBER),
			possibleTargets,
			notOverlapping,
			1
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.BAL));
	}

	@Test
	void solutionPossibleTargets_excludes_king_odawa_and_mines_of_divan_from_other_zones_when_not_overlapping() {
		// Arrange
		Vec3Comparable notOverlapping = kingOdawaMinesOrNucleusCoordsInRemnants.addVector(100, 0, 100);

		// Act & Assert
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.PRECURSOR_REMNANTS,
			EnumSet.noneOf(Crystal.class),
			EnumSet.allOf(CompassTarget.class),
			notOverlapping,
			0
		);
	}

	@Test
	void solutionPossibleTargets_excludes_city_and_queen_from_other_zones_when_not_overlapping() {
		// Arrange
		Vec3Comparable notOverlapping = queenKingOdawaOrCityNucleusCoordsInMithrilDeposits.addVector(100, 0, -100);
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.GOBLIN_KING);
		possibleTargets.remove(CompassTarget.ODAWA);

		// Act & Assert
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.MITHRIL_DEPOSITS,
			EnumSet.noneOf(Crystal.class),
			possibleTargets,
			notOverlapping,
			0
		);
	}

	@Test
	void solutionPossibleTargets_includes_king_based_on_y_coordinate() {
		// Arrange
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.ODAWA);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.GOBLIN_HOLDOUT,
			EnumSet.noneOf(Crystal.class),
			possibleTargets,
			new Vec3Comparable(goblinHoldoutKingSolution),
			1
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.GOBLIN_KING));
	}

	@Test
	void solutionPossibleTargets_includes_odawa_based_on_y_coordinate() {
		// Arrange
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.GOBLIN_KING);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.JUNGLE,
			EnumSet.noneOf(Crystal.class),
			possibleTargets,
			new Vec3Comparable(odawaSolution),
			1
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.ODAWA));
	}

	@Test
	void solutionPossibleTargets_includes_mines_based_on_y_coordinate() {
		// Arrange
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.ODAWA);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.MITHRIL_DEPOSITS,
			EnumSet.noneOf(Crystal.class),
			possibleTargets,
			new Vec3Comparable(minesOfDivanSolution),
			1
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.MINES_OF_DIVAN));
	}

	@Test
	void solutionPossibleTargets_includes_temple_based_on_y_coordinate() {
		// Arrange
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.BAL);
		possibleTargets.remove(CompassTarget.ODAWA);
		possibleTargets.remove(CompassTarget.GOBLIN_KING);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.JUNGLE,
			EnumSet.noneOf(Crystal.class),
			possibleTargets,
			new Vec3Comparable(jungleSolution),
			1
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.JUNGLE_TEMPLE));
	}

	@Test
	void solutionPossibleTargets_includes_queen_based_on_y_coordinate() {
		// Arrange
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.GOBLIN_KING);
		possibleTargets.remove(CompassTarget.ODAWA);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.GOBLIN_HOLDOUT,
			EnumSet.noneOf(Crystal.class),
			possibleTargets,
			new Vec3Comparable(goblinHoldoutQueenSolution),
			1
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.GOBLIN_QUEEN));
	}

	@Test
	void solutionPossibleTargets_includes_city_based_on_y_coordinate() {
		// Arrange
		EnumSet<CompassTarget> possibleTargets = EnumSet.allOf(CompassTarget.class);
		possibleTargets.remove(CompassTarget.GOBLIN_KING);

		// Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.PRECURSOR_REMNANTS,
			EnumSet.noneOf(Crystal.class),
			possibleTargets,
			new Vec3Comparable(precursorCitySolution),
			1
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.PRECURSOR_CITY));
	}

	@Test
	void solutionPossibleTargets_includes_bal_based_on_y_coordinate() {
		// Arrange & Act
		EnumSet<CompassTarget> solutionTargets = GetSolutionTargetsHelper(
			HollowsZone.MAGMA_FIELDS,
			EnumSet.noneOf(Crystal.class),
			EnumSet.allOf(CompassTarget.class),
			new Vec3Comparable(magmaSolution),
			1
		);

		// Assert
		Assertions.assertTrue(solutionTargets.contains(CompassTarget.BAL));
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

		CompassUse(
			long timeIncrementMillis,
			BlockPos playerPos,
			ArrayList<ParticleSpawn> particles,
			HandleCompassResult expectedHandleCompassUseResult,
			SolverState expectedState
		) {
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
			this.expectedSolutionCoords = new Vec3i(
				expectedSolutionCoords.getX(),
				expectedSolutionCoords.getY(),
				expectedSolutionCoords.getZ()
			);
		}
	}

	private static void neuDebugLog(String message) {
		System.out.println(message);
	}
}
