package io.github.moulberry.notenoughupdates.miscfeatures;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.core.util.render.RenderUtils;
import io.github.moulberry.notenoughupdates.options.customtypes.NEUDebugFlag;
import io.github.moulberry.notenoughupdates.util.NEUDebugLogger;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CrystalMetalDetectorSolver {
	private static final Minecraft mc = Minecraft.getMinecraft();

	private static BlockPos prevPlayerPos;
	private static double prevDistToTreasure = 0;
	private static HashSet<BlockPos> possibleBlocks = new HashSet<>();
	private static final HashMap<BlockPos, Double> evaluatedPlayerPositions = new HashMap<>();
	private static BlockPos blockPosIfLastSolutionInvalid;
	private static Boolean chestRecentlyFound = false;
	private static long chestLastFoundMillis = 0;
	private static final HashSet<BlockPos> openedChestPositions = new HashSet<>();

	// Keeper and Mines of Divan center location info
	private static Vec3i minesCenter;
	private static boolean visitKeeperMessagePrinted = false;
	private static String KEEPER_OF_STRING = "Keeper of ";
	private static String DIAMOND_STRING = "diamond";
	private static String LAPIS_STRING = "lapis";
	private static String EMERALD_STRING = "emerald";
	private static String GOLD_STRING = "gold";
	private static final HashMap<String, Vec3i> keeperOffsets = new HashMap<String, Vec3i>() {{
		put(DIAMOND_STRING, new Vec3i(33,0,3));
		put(LAPIS_STRING, new Vec3i(-33,0,-3));
		put(EMERALD_STRING, new Vec3i(-3,0,33));
		put(GOLD_STRING, new Vec3i(3,0,-33));
	}};

	// Chest offsets from center
	private static final HashSet<Long> knownChestOffsets = new HashSet<>(Arrays.asList(
		-10171958951910L,	// x=-38, y=-22, z=26
		10718829084646L,	// x=38, y=-22, z=-26
		-10721714765806L,	// x=-40, y=-22, z=18
		-10996458455018L,	// x=-41, y=-20, z=22
		-1100920913904L,	// x=-5, y=-21, z=16
		11268584898530L,	// x=40, y=-22, z=-30
		-11271269253148L,	// x=-42, y=-20, z=-28
		-11546281377832L,	// x=-43, y=-22, z=-40
		11818542038999L,	// x=42, y=-19, z=-41
		12093285728240L,	// x=43, y=-21, z=-16
		-1409286164L,			// x=-1, y=-22, z=-20
		1922736062492L,		// x=6, y=-21, z=28
		2197613969419L,		// x=7, y=-21, z=11
		2197613969430L,		// x=7, y=-21, z=22
		-3024999153708L,	// x=-12, y=-21, z=-44
		3571936395295L,		// x=12, y=-22, z=31
		3572003504106L,		// x=12, y=-22, z=-22
		3572003504135L,		// x=12, y=-21, z=7
		3572070612949L,		// x=12, y=-21, z=-43
		-3574822076373L,	// x=-14, y=-21, z=43
		-3574822076394L,	// x=-14, y=-21, z=22
		-4399455797228L,	// x=-17, y=-21, z=20
		-5224156626944L,	// x=-20, y=-22, z=0
		548346527764L,		// x=1, y=-21, z=20
		5496081743901L,		// x=19, y=-22, z=29
		5770959650816L,		// x=20, y=-22, z=0
		5771093868518L,		// x=20, y=-21, z=-26
		-6048790347736L,	// x=-23, y=-22, z=40
		6320849682418L,		// x=22, y=-21, z=-14
		-6323668254708L,	// x=-24, y=-22, z=12
		6595593371674L,		// x=23, y=-22, z=26
		6595660480473L,		// x=23, y=-22, z=-39
		6870471278619L,		// x=24, y=-22, z=27
		7145349185553L,		// x=25, y=-22, z=17
		8244995030996L,		// x=29, y=-21, z=-44
		-8247679385612L,	// x=-31, y=-21, z=-12
		-8247679385640L,	// x=-31, y=-21, z=-40
		8519872937959L,		// x=30, y=-21, z=-25
		-8522557292584L,	// x=-32, y=-21, z=-40
		-9622068920278L,	// x=-36, y=-20, z=42
		-9896946827278L,	// x=-37, y=-21, z=-14
		-9896946827286L		// x=-37, y=-21, z=-22
	));

	public static void process(IChatComponent message) {
		if (SBInfo.getInstance().getLocation() == null ||
			!NotEnoughUpdates.INSTANCE.config.mining.metalDetectorEnabled ||
			!SBInfo.getInstance().getLocation().equals("crystal_hollows") ||
			!message.getUnformattedText().contains("TREASURE: ")) {
			return;
		}

		locateMinesCenterIfNeeded();

		double distToTreasure = Double.parseDouble(message
			.getUnformattedText()
			.split("TREASURE: ")[1].split("m")[0].replaceAll("(?!\\.)\\D", ""));

		// Delay to keep old chest location from being treated as the new chest location
		if (chestRecentlyFound) {
			long currentTimeMillis = System.currentTimeMillis();
			if (chestLastFoundMillis == 0) {
				chestLastFoundMillis = currentTimeMillis;
				return;
			} else if (currentTimeMillis - chestLastFoundMillis < 1000 && distToTreasure < 5.0) {
				return;
			}

			chestLastFoundMillis = 0;
			chestRecentlyFound = false;
		}

		if (prevDistToTreasure == distToTreasure &&
				prevPlayerPos.equals(mc.thePlayer.getPosition()) &&
				!evaluatedPlayerPositions.keySet().contains(mc.thePlayer.getPosition())) {
			if (possibleBlocks.size() == 0) {
				evaluatedPlayerPositions.put(mc.thePlayer.getPosition(), distToTreasure);
				for (int zOffset = (int) Math.floor(-distToTreasure); zOffset <= Math.ceil(distToTreasure); zOffset++) {
					for (int y = 65; y <= 75; y++) {
						double calculatedDist = 0;
						int xOffset = 0;
						while (calculatedDist < distToTreasure) {
							BlockPos pos = new BlockPos(Math.floor(mc.thePlayer.posX) + xOffset,
								y, Math.floor(mc.thePlayer.posZ) + zOffset);
							calculatedDist = getPlayerPos().distanceTo(new Vec3(pos).addVector(0D, 1D, 0D));
							if (round(calculatedDist, 1) == distToTreasure && treasureAllowed(pos)) {
								possibleBlocks.add(pos);
							}
							xOffset++;
						}
						xOffset = 0;
						calculatedDist = 0;
						while (calculatedDist < distToTreasure) {
							BlockPos pos = new BlockPos(Math.floor(mc.thePlayer.posX) - xOffset,
								y, Math.floor(mc.thePlayer.posZ) + zOffset);
							calculatedDist = getPlayerPos().distanceTo(new Vec3(pos).addVector(0D, 1D, 0D));
							if (round(calculatedDist, 1) == distToTreasure && treasureAllowed(pos)) {
								possibleBlocks.add(pos);
							}
							xOffset++;
						}
					}
				}

				checkAndDisplaySolutionState();
			} else if (possibleBlocks.size() != 1) {
				evaluatedPlayerPositions.put(mc.thePlayer.getPosition().getImmutable(), distToTreasure);
				HashSet<BlockPos> temp = new HashSet<>();
				for (BlockPos pos : possibleBlocks) {
					if (round(getPlayerPos().distanceTo(new Vec3(pos).addVector(0D, 1D, 0D)), 1) == distToTreasure) {
						temp.add(pos);
					}
				}

				possibleBlocks = temp;
				checkAndDisplaySolutionState();
			} else {
				BlockPos pos = possibleBlocks.iterator().next();
				if (Math.abs(distToTreasure - (getPlayerPos().distanceTo(new Vec3(pos)))) > 5) {
					mc.thePlayer.addChatMessage(new ChatComponentText(
						EnumChatFormatting.RED + "[NEU] Previous solution is invalid."));
					blockPosIfLastSolutionInvalid = pos.getImmutable();
					logDiagnosticData(false);
					resetSolution(false);
				}
			}
		}

		prevPlayerPos = mc.thePlayer.getPosition().getImmutable();
		prevDistToTreasure = distToTreasure;
	}

	private static void checkForSingleKnownLocationMatch() {
		if (minesCenter == BlockPos.NULL_VECTOR || possibleBlocks.size() < 2) {
			return;
		}

		HashSet<BlockPos> temp = possibleBlocks.stream()
			.filter(block -> knownChestOffsets.contains(block.subtract(minesCenter).toLong()))
			.collect(Collectors.toCollection(HashSet::new));
		if (temp.size() == 1) {
			possibleBlocks = temp;
			NEUDebugLogger.log(NEUDebugFlag.METAL, "Known location identified.");
		} else if (temp.size() > 1) {
			NEUDebugLogger.log(NEUDebugFlag.METAL, temp.size() + " known locations identified:");
		}
	}

	private static String getFriendlyBlockPositions(Collection<BlockPos> positions) {
		if (!NEUDebugLogger.isFlagEnabled(NEUDebugFlag.METAL) || positions.size() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (BlockPos blockPos : positions) {
			sb.append("Absolute: " + blockPos.toString());
			if (minesCenter != Vec3i.NULL_VECTOR) {
				BlockPos relativeOffset = blockPos.subtract(minesCenter);
				sb.append(", Relative: " + relativeOffset.toString() + " (" + relativeOffset.toLong() + ")");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	private static String getFriendlyEvaluatedPositions(HashMap<BlockPos, Double> positions) {
		if (!NEUDebugLogger.isFlagEnabled(NEUDebugFlag.METAL) || positions.size() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (BlockPos blockPos : positions.keySet()) {
			sb.append("Absolute: " + blockPos.toString());
			if (minesCenter != Vec3i.NULL_VECTOR) {
				BlockPos relativeOffset = blockPos.subtract(minesCenter);
				sb.append(", Relative: " + relativeOffset.toString() + " (" + relativeOffset.toLong() + ")");
			}

			sb.append(" Distance: ");
			sb.append(positions.get(blockPos));

			sb.append("\n");
		}

		return sb.toString();
	}

	public static void logDiagnosticData(boolean outputAlways) {
		if (SBInfo.getInstance().getLocation() == null) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] This command is not available outside SkyBlock"));
			return;
		}

		if (!NotEnoughUpdates.INSTANCE.config.mining.metalDetectorEnabled)
		{
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"[NEU] Metal Detector Solver is not enabled."));
			return;
		}

		if (!outputAlways && !NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.contains(NEUDebugFlag.METAL)) {
			return;
		}

		boolean originalDebugFlag = !NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.add(NEUDebugFlag.METAL);

		StringBuilder diagsMessage = new StringBuilder();

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Mines Center: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((minesCenter == Vec3i.NULL_VECTOR) ? "<NOT DISCOVERED>" : minesCenter.toString());
		diagsMessage.append("\n");

		diagsMessage.append("\n");
		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Previous Player Position: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((prevPlayerPos == null) ? "<NONE>" : prevPlayerPos.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Previous Distance To Treasure: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((prevDistToTreasure == 0) ? "<NONE>" : prevDistToTreasure);
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Last Solution Invalid Position: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append((blockPosIfLastSolutionInvalid == null) ? "<NONE>" : blockPosIfLastSolutionInvalid.toString());
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Current Possible Blocks: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(possibleBlocks.size());
		diagsMessage.append(getFriendlyBlockPositions(possibleBlocks));
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Evaluated player positions: ");
		diagsMessage.append(EnumChatFormatting.WHITE);
		diagsMessage.append(evaluatedPlayerPositions.size());
		diagsMessage.append(getFriendlyEvaluatedPositions(evaluatedPlayerPositions));
		diagsMessage.append("\n");

		diagsMessage.append(EnumChatFormatting.AQUA);
		diagsMessage.append("Chest locations not on known list:\n");
		diagsMessage.append(EnumChatFormatting.WHITE);
		if (minesCenter != Vec3i.NULL_VECTOR) {
			HashSet<BlockPos> locationsNotOnKnownList = openedChestPositions
				.stream()
				.filter(block -> !knownChestOffsets.contains(block.subtract(minesCenter).toLong()))
				.map(block -> block.subtract(minesCenter))
				.collect(Collectors.toCollection(HashSet::new));
			if (locationsNotOnKnownList.size() > 0) {
				for (BlockPos blockPos : locationsNotOnKnownList) {
					diagsMessage.append(String.format(
						"%dL,\t\t// x=%d, y=%d, z=%d",
						blockPos.toLong(),
						blockPos.getX(),
						blockPos.getY(),
						blockPos.getZ()
					));
				}
			}
		} else {
			diagsMessage.append("<REQUIRES MINES CENTER>");
		}

		NEUDebugLogger.log(NEUDebugFlag.METAL, diagsMessage.toString());

		if (!originalDebugFlag) {
			NotEnoughUpdates.INSTANCE.config.hidden.debugFlags.remove(NEUDebugFlag.METAL);
		}
	}

	public static void resetSolution(Boolean chestFound) {
		if (chestFound) {
			blockPosIfLastSolutionInvalid = null;
			prevPlayerPos = null;
			prevDistToTreasure = 0;
		  if (possibleBlocks.size() == 1) {
				openedChestPositions.add(possibleBlocks.iterator().next().getImmutable());
			}
		}

		chestRecentlyFound = chestFound;
		possibleBlocks.clear();
		evaluatedPlayerPositions.clear();
	}

	public static void initWorld() {
		minesCenter = Vec3i.NULL_VECTOR;
		visitKeeperMessagePrinted = false;
		blockPosIfLastSolutionInvalid = null;
		openedChestPositions.clear();
		prevDistToTreasure = 0;
		prevPlayerPos = null;
		resetSolution(false);
	}

	public static void render(float partialTicks) {
		int beaconRGB = 0x1fd8f1;

		if (SBInfo.getInstance().getLocation() != null && SBInfo.getInstance().getLocation().equals("crystal_hollows") &&
			SBInfo.getInstance().location.equals("Mines of Divan")) {

			if (possibleBlocks.size() == 1) {
				BlockPos block = possibleBlocks.iterator().next();

				RenderUtils.renderBeaconBeam(block.add(0, 1, 0), beaconRGB, 1.0f, partialTicks);
				RenderUtils.renderWayPoint("Treasure", possibleBlocks.iterator().next().add(0, 2.5, 0), partialTicks);
			} else if (possibleBlocks.size() > 1 && NotEnoughUpdates.INSTANCE.config.mining.metalDetectorShowPossible) {
				for (BlockPos block : possibleBlocks) {
					RenderUtils.renderBeaconBeam(block.add(0, 1, 0), beaconRGB, 1.0f, partialTicks);
					RenderUtils.renderWayPoint("Possible Treasure Location", block.add(0, 2.5, 0), partialTicks);
				}
			}
		}
	}

	private static void locateMinesCenterIfNeeded() {
		if (minesCenter != Vec3i.NULL_VECTOR) {
			return;
		}

		List<EntityArmorStand> keeperEntities = mc.theWorld.getEntities(EntityArmorStand.class, (entity) -> {
			if (!entity.hasCustomName()) return false;
			if (entity.getCustomNameTag().contains(KEEPER_OF_STRING)) return true;
			return false;
		});

		if (keeperEntities.size() == 0) {
			if (!visitKeeperMessagePrinted) {
				mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
					"[NEU] Approach a Keeper while holding the metal detector to enable faster treasure hunting."));
				visitKeeperMessagePrinted = true;
			}
			return;
		}

		EntityArmorStand keeperEntity = keeperEntities.get(0);
		String keeperName = keeperEntity.getCustomNameTag();
		NEUDebugLogger.log(NEUDebugFlag.METAL,"Locating center using Keeper: " +
			EnumChatFormatting.WHITE + keeperEntity);
		String keeperType = keeperName.substring(keeperName.indexOf(KEEPER_OF_STRING) + KEEPER_OF_STRING.length());
		minesCenter =  keeperEntity.getPosition().add(keeperOffsets.get(keeperType.toLowerCase()));
		NEUDebugLogger.log(NEUDebugFlag.METAL,"Mines center: " +
			EnumChatFormatting.WHITE + minesCenter.toString());
		mc.thePlayer.addChatMessage(new ChatComponentText(
			EnumChatFormatting.YELLOW + "[NEU] Faster treasure hunting is now enabled based on Keeper location."));
	}

	private static double round(double value, int precision) {
		int scale = (int) Math.pow(10, precision);
		return (double) Math.round(value * scale) / scale;
	}

	private static void checkAndDisplaySolutionState() {
		if (possibleBlocks.size() == 0) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[NEU] Failed to find a solution."));
			logDiagnosticData(false);
			resetSolution(false);
			return;
		}

		checkForSingleKnownLocationMatch();
		if (possibleBlocks.size() > 1) {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW +
				"[NEU] Need another position to find solution. Possible blocks: " + possibleBlocks.size()));
		} else {
			mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[NEU] Found solution."));
		}
	}

	private static Vec3 getPlayerPos() {
		return new Vec3(
			mc.thePlayer.posX,
			mc.thePlayer.posY + (mc.thePlayer.getEyeHeight() - mc.thePlayer.getDefaultEyeHeight()),
			mc.thePlayer.posZ
		);
	}

	private static boolean treasureAllowed(BlockPos pos) {
		boolean airAbove = mc.theWorld.
			getBlockState(pos.add(0, 1, 0)).getBlock().getRegistryName().equals("minecraft:air");
		boolean allowedBlockType = mc.theWorld.getBlockState(pos).getBlock().getRegistryName().equals("minecraft:gold_block") ||
			mc.theWorld.getBlockState(pos).getBlock().getRegistryName().equals("minecraft:prismarine") ||
			mc.theWorld.getBlockState(pos).getBlock().getRegistryName().equals("minecraft:chest") ||
			mc.theWorld.getBlockState(pos).getBlock().getRegistryName().equals("minecraft:stained_glass") ||
			mc.theWorld.getBlockState(pos).getBlock().getRegistryName().equals("minecraft:stained_glass_pane") ||
			mc.theWorld.getBlockState(pos).getBlock().getRegistryName().equals("minecraft:wool") ||
			mc.theWorld.getBlockState(pos).getBlock().getRegistryName().equals("minecraft:stained_hardened_clay");
		boolean knownOffset = knownChestOffsets.contains(pos.subtract(minesCenter).toLong());
		return airAbove & (knownOffset | allowedBlockType);
	}
}
