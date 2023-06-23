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
import io.github.moulberry.notenoughupdates.util.SBInfo;
import io.github.moulberry.notenoughupdates.util.SpecialColour;
import io.github.moulberry.notenoughupdates.util.Utils;
import lombok.var;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NEUAutoSubscribe
public class CustomItemEffects {
	public static final CustomItemEffects INSTANCE = new CustomItemEffects();

	private static final int MAX_BUILDERS_BLOCKS = 241;

	private final HashSet<BlockPos> zapperBlocks = new HashSet<>();
	private boolean zapperDirty = false;

	public long aoteUseMillis = 0;

	public long lastUsedHyperion = 0;

	private final Pattern etherwarpDistancePattern = Pattern.compile("up to (?<distance>\\d{2}) blocks away\\.");

	private final List<String> aoteNames = Arrays.asList(
		"ASPECT_OF_THE_END",
		"ASPECT_OF_THE_VOID",
		"ASPECT_OF_THE_LEECH_1",
		"ASPECT_OF_THE_LEECH_2"
	);

	public int aoteTeleportationMillis = 0;
	public Vector3f aoteTeleportationCurr = null;

	public int tpTime = NotEnoughUpdates.INSTANCE.config.itemOverlays.smoothTpMillis;

	private int tick;

	public long lastMillis = 0;

	public Vector3f getCurrentPosition() {
		if (aoteTeleportationMillis <= 0) return null;
		return aoteTeleportationCurr;
	}

	@SubscribeEvent
	public void onTick(TickEvent.RenderTickEvent event) {
		if (Minecraft.getMinecraft().thePlayer == null) return;

		zapperDirty = true;

		long currentTime = System.currentTimeMillis();
		int delta = (int) (currentTime - lastMillis);
		lastMillis = currentTime;

		if (delta <= 0) return;

		if (aoteTeleportationMillis > tpTime * 2) {
			aoteTeleportationMillis = tpTime * 2;
		}
		if (aoteTeleportationMillis < 0) aoteTeleportationMillis = 0;

		if (currentTime - aoteUseMillis > 1000 && aoteTeleportationMillis <= 0) {
			aoteTeleportationCurr = null;
		}

		if (aoteTeleportationCurr != null) {
			if (aoteTeleportationMillis > 0) {
				int deltaMin = Math.min(delta, aoteTeleportationMillis);

				float factor = deltaMin / (float) aoteTeleportationMillis;

				float dX = aoteTeleportationCurr.x - (float) Minecraft.getMinecraft().thePlayer.posX;
				float dY = aoteTeleportationCurr.y - (float) Minecraft.getMinecraft().thePlayer.posY;
				float dZ = aoteTeleportationCurr.z - (float) Minecraft.getMinecraft().thePlayer.posZ;

				aoteTeleportationCurr.x -= dX * factor;
				aoteTeleportationCurr.y -= dY * factor;
				aoteTeleportationCurr.z -= dZ * factor;

				if (Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(aoteTeleportationCurr.x,
					aoteTeleportationCurr.y, aoteTeleportationCurr.z
				)).getBlock().getMaterial() != Material.air) {
					aoteTeleportationCurr.y = (float) Math.ceil(aoteTeleportationCurr.y);
				}

				aoteTeleportationMillis -= deltaMin;
			} else {
				aoteTeleportationCurr.x = (float) Minecraft.getMinecraft().thePlayer.posX;
				aoteTeleportationCurr.y = (float) Minecraft.getMinecraft().thePlayer.posY;
				aoteTeleportationCurr.z = (float) Minecraft.getMinecraft().thePlayer.posZ;
			}
		} else {
			aoteUseMillis = 0;
			aoteTeleportationMillis = 0;
		}
	}

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR ||
			event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
			ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
			String internal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);
			if (internal != null) {
				boolean shadowWarp = false;
				if (internal.equals("HYPERION") || internal.equals("VALKYRIE") || internal.equals("SCYLLA") || internal.equals(
					"ASTRAEA")) {
					NBTTagCompound tag = held.getTagCompound();
					if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
						NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
						if (ea != null && ea.hasKey("ability_scroll", 9)) {
							NBTTagList list = ea.getTagList("ability_scroll", 8);
							for (int i = 0; i < list.tagCount(); i++) {
								if (list.getStringTagAt(i).equals("IMPLOSION_SCROLL")) {
									lastUsedHyperion = System.currentTimeMillis();
								} else if (list.getStringTagAt(i).equals("SHADOW_WARP_SCROLL")) {
									shadowWarp = true;
								}
							}
						}
					}
				}

				if (usingEtherwarp) {
					lastEtherwarpUse = tick;
				}

				if (tpTime <= 0 || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0) return;

				boolean aote = NotEnoughUpdates.INSTANCE.config.itemOverlays.enableSmoothAOTE &&
					aoteNames.contains(internal);
				boolean hyp = NotEnoughUpdates.INSTANCE.config.itemOverlays.enableSmoothHyperion && shadowWarp;
				if (usingEtherwarp) {
					tpTime = NotEnoughUpdates.INSTANCE.config.itemOverlays.smoothTpMillisEtherwarp;
				} else {
					tpTime = NotEnoughUpdates.INSTANCE.config.itemOverlays.smoothTpMillis;
				}
				if (usingEtherwarp || aote || hyp) {
					aoteUseMillis = System.currentTimeMillis();
					if (aoteTeleportationCurr == null) {
						aoteTeleportationCurr = new Vector3f();
						aoteTeleportationCurr.x = (float) Minecraft.getMinecraft().thePlayer.posX;
						aoteTeleportationCurr.y = (float) Minecraft.getMinecraft().thePlayer.posY;
						aoteTeleportationCurr.z = (float) Minecraft.getMinecraft().thePlayer.posZ;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onGameTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (player == null) return;

		if (!usingEtherwarp && wasUsingEtherwarp) {
			if (player.rotationYaw > 0) {
				player.rotationYaw -= 0.000001;
			} else {
				player.rotationYaw += 0.000001;
			}
		}
		wasUsingEtherwarp = usingEtherwarp;

		tick++;
		if (tick > Integer.MAX_VALUE / 2) tick = 0;

	}

	private float lastPartialTicks = 0;
	private float currentFOVMult = 1;
	private float targetFOVMult = 1;

	private float lastPartialDelta = 0;

	private float currentSensMult = 1;
	private float targetSensMult = 1;

	public float getSensMultiplier() {
		if (targetSensMult < 0) {
			currentSensMult = 1;
		} else {
			float deltaSens = targetSensMult - currentSensMult;

			currentSensMult += deltaSens * lastPartialDelta * 0.1;// (0.05 * );
			if (currentSensMult < 0.25f) currentSensMult = 0.25f;
			if (currentSensMult > 1) currentSensMult = 1;
		}
		return currentSensMult;
	}

	public float getFovMultiplier(float partialTicks) {
		float partialDelta = partialTicks + tick - lastPartialTicks;
		if (partialDelta < 0) partialDelta++;

		if (partialDelta > 0) lastPartialDelta = partialDelta;

		if (targetFOVMult < 0) {
			currentFOVMult = 1;
		} else {
			float deltaFOV = targetFOVMult - currentFOVMult;

			currentFOVMult += deltaFOV * lastPartialDelta * 0.2;
			if (currentFOVMult < 0.15f) currentFOVMult = 0.15f;
			if (currentFOVMult > 1) currentFOVMult = 1;
		}
		lastPartialTicks = partialTicks + tick;
		return currentFOVMult;
	}

	private boolean wasUsingEtherwarp = false;
	private boolean usingEtherwarp = false;
	private RaycastResult etherwarpRaycast = null;
	private int lastEtherwarpUse = 0;
	private String denyTpReason = null;

	@SubscribeEvent
	public void onOverlayDrawn(RenderGameOverlayEvent.Post event) {
		if (((event.type == null && Loader.isModLoaded("labymod")) ||
			event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS)) {
			ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
			String heldInternal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);

			WorldClient world = Minecraft.getMinecraft().theWorld;
			if (usingEtherwarp) {
				denyTpReason = null;
				if (etherwarpRaycast == null) {
					denyTpReason = "Too far!";
				} else {
					BlockPos pos = etherwarpRaycast.pos;

					Block block = etherwarpRaycast.state.getBlock();
					if (!block.isCollidable() ||
						//Don't allow teleport at this block
						block == Blocks.carpet || block == Blocks.skull ||
						block.getCollisionBoundingBox(world, etherwarpRaycast.pos, etherwarpRaycast.state) == null &&
							//Allow teleport at this block
							block != Blocks.wall_sign && block != Blocks.standing_sign) {
						denyTpReason = "Not solid!";
					} else {
						BlockPos blockPosAbove = pos.add(0, 1, 0);
						Block blockAbove = world.getBlockState(blockPosAbove).getBlock();

						Block twoBlockAbove = world.getBlockState(pos.add(0, 2, 0)).getBlock();
						if (blockAbove != Blocks.air &&
							//Allow teleport to the block below this block
							blockAbove != Blocks.carpet && blockAbove != Blocks.skull && blockAbove.isCollidable() &&
							blockAbove.getCollisionBoundingBox(world, blockPosAbove, world.getBlockState(blockPosAbove)) != null ||
							//Don't allow teleport to the block below this block
							blockAbove == Blocks.wall_sign || block == Blocks.standing_sign ||
							//Allow teleport to the block 2 blocks below this block
							twoBlockAbove != Blocks.air && twoBlockAbove != Blocks.double_plant && twoBlockAbove != Blocks.carpet &&
								blockAbove != Blocks.skull) {
							denyTpReason = "No air above!";
						}
					}
				}

				if (NotEnoughUpdates.INSTANCE.config.itemOverlays.enableEtherwarpHelperOverlay) {
					if (denyTpReason != null) {
						ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
						Utils.drawStringCentered(EnumChatFormatting.RED + "Can't TP: " + denyTpReason,
							Minecraft.getMinecraft().fontRendererObj,
							scaledResolution.getScaledWidth() / 2f, scaledResolution.getScaledHeight() / 2f + 10, true, 0
						);
						GlStateManager.color(1, 1, 1, 1);
					}
				}
			}

			boolean onPrivateIsland = Arrays.asList("dynamic","garden").contains(SBInfo.getInstance().getLocation());

			if (NotEnoughUpdates.INSTANCE.config.itemOverlays.enableWandOverlay &&
				Minecraft.getMinecraft().objectMouseOver != null &&
				Minecraft.getMinecraft().objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
				onPrivateIsland) {

				IBlockState hover = world.getBlockState(
					Minecraft.getMinecraft().objectMouseOver.getBlockPos().offset(
						Minecraft.getMinecraft().objectMouseOver.sideHit, 1));
				if (hover.getBlock() == Blocks.air) {

					if (heldInternal != null && (heldInternal.equals("BUILDERS_WAND"))) buildersWandText(event, world);
					else if (heldInternal != null && (heldInternal.equals("BUILDERS_RULER"))) buildersRulerText(event, world);

				}
			}
		}
	}

	public void buildersWandText(RenderGameOverlayEvent.Post event, WorldClient world) {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

		HashSet<BlockPos> candidatesOld = new HashSet<>();
		TreeMap<Float, Set<BlockPos>> candidatesOldSorted = new TreeMap<>();

		IBlockState match =
			world.getBlockState(Minecraft.getMinecraft().objectMouseOver.getBlockPos());
		Item matchItem = Item.getItemFromBlock(match.getBlock());
		if (matchItem == null) return;
		ItemStack matchStack;

		matchStack = new ItemStack(matchItem, 1,
			match
				.getBlock()
				.getDamageValue(
					world,
					Minecraft.getMinecraft().objectMouseOver.getBlockPos()
				)
		);

		getBuildersWandCandidates(
			Minecraft.getMinecraft().thePlayer,
			Minecraft.getMinecraft().objectMouseOver,
			event.partialTicks,
			candidatesOld,
			candidatesOldSorted,
			999 - MAX_BUILDERS_BLOCKS
		);

		boolean usingDirtWand = false;
		int itemCount;
		if (match.getBlock() == Blocks.dirt && matchStack.getItemDamage() == 0 && hasDirtWand()) {
			itemCount = candidatesOld.size();
			usingDirtWand = true;
		} else {
			itemCount = countItemsInInventoryAndStorage(matchStack);
		}
		if (candidatesOld.size() == 0) return;

		if (candidatesOld.size() > MAX_BUILDERS_BLOCKS) {
			Utils.drawStringCentered(
				EnumChatFormatting.RED.toString() + candidatesOld.size() + "/" + MAX_BUILDERS_BLOCKS,
				Minecraft.getMinecraft().fontRendererObj,
				scaledResolution.getScaledWidth() / 2f,
				scaledResolution.getScaledHeight() / 2f + 10,
				true,
				0
			);
		} else {
			String pre = EnumChatFormatting.GREEN.toString();
			if (itemCount < candidatesOld.size()) {
				pre = EnumChatFormatting.RED.toString();
			}
			Utils.drawStringCentered(pre + Math.min(candidatesOld.size(), itemCount) + "/" +
					Math.min(candidatesOld.size(), MAX_BUILDERS_BLOCKS),
				Minecraft.getMinecraft().fontRendererObj,
				scaledResolution.getScaledWidth() / 2f, scaledResolution.getScaledHeight() / 2f + 10, true, 0
			);
		}

		String itemCountS = EnumChatFormatting.DARK_GRAY + "x" + EnumChatFormatting.RESET + itemCount;
		int itemCountLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(itemCountS);

		if (NotEnoughUpdates.INSTANCE.config.itemOverlays.wandBlockCount) {
			if (usingDirtWand) {
				Utils.drawItemStack(
					new ItemStack(Items.gold_nugget),
					scaledResolution.getScaledWidth() / 2 - (itemCountLen + 16) / 2,
					scaledResolution.getScaledHeight() / 2 + 10 + 4
				);
				Minecraft.getMinecraft().fontRendererObj.drawString(
					itemCountS,
					scaledResolution.getScaledWidth() / 2f - (itemCountLen + 16) / 2f + 11,
					scaledResolution.getScaledHeight() / 2f + 10 + 8,
					-1,
					true
				);
			} else {
				Utils.drawItemStack(matchStack, scaledResolution.getScaledWidth() / 2 - (itemCountLen + 16) / 2,
					scaledResolution.getScaledHeight() / 2 + 10 + 4
				);
				Minecraft.getMinecraft().fontRendererObj.drawString(
					itemCountS,
					scaledResolution.getScaledWidth() / 2f - (itemCountLen + 16) / 2f + 16,
					scaledResolution.getScaledHeight() / 2f + 10 + 8,
					-1,
					true
				);
			}

		}

		GlStateManager.color(1, 1, 1, 1);
	}

	public void buildersRulerText(RenderGameOverlayEvent.Post event, WorldClient world) {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

		HashSet<BlockPos> candidatesOld = new HashSet<>();
		TreeMap<Float, Set<BlockPos>> candidatesOldSorted = new TreeMap<>();

		IBlockState match =
			world.getBlockState(Minecraft.getMinecraft().objectMouseOver.getBlockPos());
		Item matchItem = Item.getItemFromBlock(match.getBlock());
		if (matchItem == null) return;
		ItemStack matchStack;

		matchStack = new ItemStack(matchItem, 1,
			match
				.getBlock()
				.getDamageValue(
					world,
					Minecraft.getMinecraft().objectMouseOver.getBlockPos()
				)
		);
		if (!Minecraft.getMinecraft().thePlayer.isSneaking()) matchStack = getFirstItemInRuler();
		match = Blocks.dirt.getDefaultState();
		if (matchStack == null) return;

		getBuildersRulerCandidates(
			Minecraft.getMinecraft().thePlayer,
			Minecraft.getMinecraft().objectMouseOver,
			event.partialTicks,
			candidatesOld,
			candidatesOldSorted,
			499 - MAX_BUILDERS_BLOCKS
		);

		boolean usingDirtWand = false;
		int itemCount;
		if (Minecraft.getMinecraft().thePlayer.isSneaking()) {
			itemCount = candidatesOld.size();
		} else {
			if (match.getBlock() == Blocks.dirt && matchStack.getItemDamage() == 0 && hasDirtWand()) {
				itemCount = candidatesOld.size();
				usingDirtWand = true;
			} else {
				itemCount = countItemsInInventoryAndStorage(matchStack);
			}
		}
		if (candidatesOld.size() == 0) return;

		if (!Minecraft.getMinecraft().thePlayer.isSneaking()) {
			if (candidatesOld.size() > MAX_BUILDERS_BLOCKS) {
				Utils.drawStringCentered(
					EnumChatFormatting.RED.toString() + candidatesOld.size() + "/" + MAX_BUILDERS_BLOCKS,
					Minecraft.getMinecraft().fontRendererObj,
					scaledResolution.getScaledWidth() / 2f,
					scaledResolution.getScaledHeight() / 2f + 10,
					true,
					0
				);
			} else {
				String pre = EnumChatFormatting.GREEN.toString();
				Utils.drawStringCentered(pre + Math.min(candidatesOld.size(), itemCount) + "/" +
						Math.min(candidatesOld.size(), MAX_BUILDERS_BLOCKS),
					Minecraft.getMinecraft().fontRendererObj,
					scaledResolution.getScaledWidth() / 2f, scaledResolution.getScaledHeight() / 2f + 10, true, 0
				);
			}
		}

		String itemCountS = EnumChatFormatting.DARK_GRAY + "x" + EnumChatFormatting.RESET + itemCount;
		int itemCountLen = Minecraft.getMinecraft().fontRendererObj.getStringWidth(itemCountS);

		if (NotEnoughUpdates.INSTANCE.config.itemOverlays.wandBlockCount) {
			if (usingDirtWand) {
				Utils.drawItemStack(
					new ItemStack(Items.gold_nugget),
					scaledResolution.getScaledWidth() / 2 - (itemCountLen + 16) / 2,
					scaledResolution.getScaledHeight() / 2 + 10 + 4
				);
				Minecraft.getMinecraft().fontRendererObj.drawString(
					itemCountS,
					scaledResolution.getScaledWidth() / 2f - (itemCountLen + 16) / 2f + 11,
					scaledResolution.getScaledHeight() / 2f + 10 + 8,
					-1,
					true
				);
			} else {
				Utils.drawItemStack(matchStack, scaledResolution.getScaledWidth() / 2 - (itemCountLen + 16) / 2,
					scaledResolution.getScaledHeight() / 2 + 10 + 4
				);
				Minecraft.getMinecraft().fontRendererObj.drawString(
					itemCountS,
					scaledResolution.getScaledWidth() / 2f - (itemCountLen + 16) / 2f + 16,
					scaledResolution.getScaledHeight() / 2f + 10 + 8,
					-1,
					true
				);
			}

		}

		GlStateManager.color(1, 1, 1, 1);
	}
	//ethermerge

	private static class RaycastResult {
		IBlockState state;
		BlockPos pos;

		public RaycastResult(IBlockState state, BlockPos pos) {
			this.state = state;
			this.pos = pos;
		}
	}

	private RaycastResult raycast(EntityPlayerSP player, float partialTicks, float dist, float step) {
		Vector3f pos = new Vector3f((float) player.posX, (float) player.posY + player.getEyeHeight(), (float) player.posZ);

		Vec3 lookVec3 = player.getLook(partialTicks);

		Vector3f look = new Vector3f((float) lookVec3.xCoord, (float) lookVec3.yCoord, (float) lookVec3.zCoord);
		look.scale(step / look.length());

		int stepCount = (int) Math.ceil(dist / step);

		for (int i = 0; i < stepCount; i++) {
			Vector3f.add(pos, look, pos);

			WorldClient world = Minecraft.getMinecraft().theWorld;
			BlockPos position = new BlockPos(pos.x, pos.y, pos.z);
			IBlockState state = world.getBlockState(position);

			if (state.getBlock() != Blocks.air) {
				//Back-step
				Vector3f.sub(pos, look, pos);
				look.scale(0.1f);

				for (int j = 0; j < 10; j++) {
					Vector3f.add(pos, look, pos);

					BlockPos position2 = new BlockPos(pos.x, pos.y, pos.z);
					IBlockState state2 = world.getBlockState(position2);

					if (state2.getBlock() != Blocks.air) {
						return new RaycastResult(state2, position2);
					}
				}

				return new RaycastResult(state, position);
			}
		}

		return null;
	}

	public NBTTagCompound getBuildersNbt(boolean isWand) {
		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (held == null) return null;

		if (held.hasTagCompound() && held.getTagCompound().hasKey("ExtraAttributes", 10) &&
			held.getTagCompound().getCompoundTag("ExtraAttributes").hasKey(isWand
				? "builder's_wand_data"
				: "builder's_ruler_data", 7)) {
			byte[] bytes = held.getTagCompound().getCompoundTag("ExtraAttributes").getByteArray(isWand
				? "builder's_wand_data"
				: "builder's_ruler_data");
			try {
				return CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public int countItemsInInventoryAndStorage(ItemStack match) {
		if (match == null) return 0;
		int count = 0;

		for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
			if (match.isItemEqual(stack)) {
				count += stack.stackSize;
			}
		}

		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
		String heldInternal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);

		boolean isWand = heldInternal != null && heldInternal.equals("BUILDERS_WAND");
		if (heldInternal == null || !heldInternal.equals(isWand ? "BUILDERS_WAND" : "BUILDERS_RULER")) return count;

		NBTTagCompound contents_nbt = getBuildersNbt(isWand);
		if (contents_nbt == null) return count;

		NBTTagList items = contents_nbt.getTagList("i", 10);
		for (int j = 0; j < items.tagCount(); j++) {
			NBTTagCompound buildersItem = items.getCompoundTagAt(j);
			if (buildersItem.getKeySet().size() > 0) {
				if (buildersItem.getInteger("id") == Item.getIdFromItem(match.getItem()) &&
					buildersItem.getInteger("Damage") == match.getItemDamage()) {
					count += items.getCompoundTagAt(j).getByte("Count");
				}
			}
		}

		return count;
	}

	public ItemStack getFirstItemInRuler() {

		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
		String heldInternal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);

		if (heldInternal == null || !heldInternal.equals("BUILDERS_RULER")) return null;

		NBTTagCompound contents_nbt = getBuildersNbt(false);
		if (contents_nbt == null) return null;

		NBTTagList items = contents_nbt.getTagList("i", 10);
		for (int j = 0; j < items.tagCount(); j++) {
			NBTTagCompound buildersItem = items.getCompoundTagAt(j);
			if (buildersItem.getKeySet().size() > 0) {
				ItemStack newStack = new ItemStack(
					Item.getItemById(buildersItem.getInteger("id")),
					1,
					buildersItem.getInteger("Damage")
				);
				return newStack;
			}
		}

		//the ruler says it uses ur inv but it doesnt
		/*for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
			if (stack != null && stack.getItem() instanceof ItemBlock) return stack;
		}*/
		return null;
	}

	public boolean hasDirtWand() {
		for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
			String internalname = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(stack);
			if (internalname != null && internalname.equals("INFINIDIRT_WAND")) {
				return true;
			}
		}

		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
		String heldInternal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);
		boolean isWand = heldInternal != null && heldInternal.equals("BUILDERS_WAND");
		if (heldInternal != null && heldInternal.equals(isWand ? "BUILDERS_WAND" : "BUILDERS_RULER")) {
			if (held.hasTagCompound() && held.getTagCompound().hasKey("ExtraAttributes", 10) &&
				held.getTagCompound().getCompoundTag("ExtraAttributes").hasKey(isWand
					? "builder's_wand_data"
					: "builder's_ruler_data", 7)) {
				byte[] bytes = held.getTagCompound().getCompoundTag("ExtraAttributes").getByteArray(isWand
					? "builder's_wand_data"
					: "builder's_ruler_data");
				try {
					NBTTagCompound contents_nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
					NBTTagList items = contents_nbt.getTagList("i", 10);
					for (int j = 0; j < items.tagCount(); j++) {
						NBTTagCompound buildersItem = items.getCompoundTagAt(j);
						if (buildersItem.getKeySet().size() > 0) {
							String internalname =
								NotEnoughUpdates.INSTANCE.manager.createItemResolutionQuery()
																								 .withItemNBT(buildersItem.getCompoundTag("tag"))
																								 .resolveInternalName();
							if (internalname != null && internalname.equals("INFINIDIRT_WAND")) {
								return true;
							}
						}
					}
				} catch (Exception ignored) {
				}
			}
		}

		return false;
	}

	private static final List<BlockPos> zapperOffsets = new ArrayList<BlockPos>() {{
		add(new BlockPos(0, 0, -1));
		add(new BlockPos(0, 0, 1));
		add(new BlockPos(-1, 0, 0));
		add(new BlockPos(1, 0, 0));
		add(new BlockPos(0, 1, 0));
		add(new BlockPos(0, -1, 0));
	}};

	private static final HashSet<Block> cropBlocksZapper = new HashSet<Block>() {{
		add(Blocks.wheat);
		add(Blocks.carrots);
		add(Blocks.potatoes);
		add(Blocks.pumpkin);
		add(Blocks.pumpkin_stem);
		add(Blocks.melon_block);
		add(Blocks.melon_stem);
		add(Blocks.cactus);
		add(Blocks.reeds);
		add(Blocks.nether_wart);
		add(Blocks.tallgrass);
		add(Blocks.double_plant);
	}};

	private static final HashSet<Block> otherBannedBlocksZapper = new HashSet<Block>() {{
		add(Blocks.farmland);
	}};

	List<Block> scytheBlocks = Arrays.asList(
		Blocks.leaves, Blocks.leaves2, Blocks.red_flower, Blocks.yellow_flower, Blocks.tallgrass, Blocks.double_plant);

	@SubscribeEvent
	public void renderBlockOverlay(DrawBlockHighlightEvent event) {
		if (aoteTeleportationCurr != null && aoteTeleportationMillis > 0) {
			event.setCanceled(true);
		}
		usingEtherwarp = false;
		etherwarpRaycast = null;
		float lastFOVMult = this.targetFOVMult;
		this.targetFOVMult = 1;
		this.targetSensMult = 1;

		ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
		String heldInternal = NotEnoughUpdates.INSTANCE.manager.getInternalNameForItem(held);
		if (heldInternal != null) {
			boolean onPrivateIsland = Arrays.asList("dynamic","garden").contains(SBInfo.getInstance().getLocation());
			EntityPlayer player = event.player;
			double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.partialTicks;
			double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.partialTicks;
			double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.partialTicks;

			//Don't need to wait 10 ticks when zoom is disabled
			if (tick - lastEtherwarpUse > 10 || !NotEnoughUpdates.INSTANCE.config.itemOverlays.etherwarpZoom) {
				boolean aotv = Minecraft.getMinecraft().thePlayer.isSneaking() &&
					(heldInternal.equals("ASPECT_OF_THE_VOID") || heldInternal.equals("ASPECT_OF_THE_END"));
				if (aotv || heldInternal.equals("ETHERWARP_CONDUIT")) {
					usingEtherwarp = !aotv;

					if (aotv) {
						NBTTagCompound tag = held.getTagCompound();
						if (tag != null && tag.hasKey("ExtraAttributes", 10)) {
							NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");
							usingEtherwarp = ea.hasKey("ethermerge");
						}
					}

					if (usingEtherwarp) {
						int dist = 0;
						for (String line : NotEnoughUpdates.INSTANCE.manager.getLoreFromNBT(held.getTagCompound())) {
							String cleaned = Utils.cleanColour(line);
							Matcher matcher = etherwarpDistancePattern.matcher(cleaned);
							if (matcher.matches()) {
								dist = Integer.parseInt(matcher.group("distance"));
								break;
							}
						}
						if (dist != 0) {
							etherwarpRaycast = raycast(Minecraft.getMinecraft().thePlayer, 1f, dist, 0.1f);

							if (etherwarpRaycast != null &&
								NotEnoughUpdates.INSTANCE.config.itemOverlays.enableEtherwarpBlockOverlay) {
								if (denyTpReason == null || !NotEnoughUpdates.INSTANCE.config.itemOverlays.disableOverlayWhenFailed) {
									AxisAlignedBB box = etherwarpRaycast.state.getBlock().getSelectedBoundingBox(
										Minecraft.getMinecraft().theWorld,
										etherwarpRaycast.pos
									);
									AxisAlignedBB bb = box.expand(0.01D, 0.01D, 0.01D).offset(-d0, -d1, -d2);
									drawFilledBoundingBox(
										bb,
										1f,
										NotEnoughUpdates.INSTANCE.config.itemOverlays.etherwarpHighlightColour
									);

									GlStateManager.disableDepth();
									drawOutlineBoundingBox(
										bb,
										2f,
										NotEnoughUpdates.INSTANCE.config.itemOverlays.etherwarpHighlightColour
									);
									GlStateManager.enableDepth();

									GlStateManager.depthMask(true);
									GlStateManager.enableTexture2D();
									GlStateManager.disableBlend();
								}

								if (NotEnoughUpdates.INSTANCE.config.itemOverlays.etherwarpZoom) {
									float distFactor = 1 -
										(float) Math.sqrt(etherwarpRaycast.pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition())) /
											60;

									targetFOVMult = distFactor * distFactor * distFactor * 0.75f + 0.25f;
									if (targetFOVMult < 0.25f) targetFOVMult = 0.25f;

									targetSensMult = distFactor * 0.76f + 0.25f;
								}
							} else if (NotEnoughUpdates.INSTANCE.config.itemOverlays.etherwarpZoom) {
								targetFOVMult = lastFOVMult;
							}

							return;
						}
					}
				}
			}

			if (heldInternal.equals("BLOCK_ZAPPER")) {

				if (!onPrivateIsland || !NotEnoughUpdates.INSTANCE.config.itemOverlays.enableZapperOverlay ||
					event.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
					zapperBlocks.clear();
					return;
				}
				if (zapperDirty) {
					zapperDirty = false;

					zapperBlocks.clear();

					LinkedList<BlockPos> returnablePositions = new LinkedList<>();

					BlockPos pos = event.target.getBlockPos();
					IBlockState firstBlockState = Minecraft.getMinecraft().theWorld.getBlockState(pos);
					Block block = firstBlockState.getBlock();

					BlockPos above = pos.add(0, 1, 0);
					Block aboveBlock = Minecraft.getMinecraft().theWorld.getBlockState(above).getBlock();

					if (!cropBlocksZapper.contains(aboveBlock) && !cropBlocksZapper.contains(block) &&
						!otherBannedBlocksZapper.contains(block) && !block.hasTileEntity(firstBlockState) &&
						block.getBlockHardness(Minecraft.getMinecraft().theWorld, pos) >= 0) {
						for (int i = 0; i < 164; i++) {
							zapperBlocks.add(pos);
							returnablePositions.remove(pos);

							List<BlockPos> availableNeighbors = new ArrayList<>();

							for (BlockPos offset : zapperOffsets) {
								BlockPos newPos = pos.add(offset);

								if (zapperBlocks.contains(newPos)) continue;

								IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(newPos);
								if (state != null && state.getBlock() == block) {
									above = newPos.add(0, 1, 0);
									aboveBlock = Minecraft.getMinecraft().theWorld.getBlockState(above).getBlock();
									if (!cropBlocksZapper.contains(aboveBlock)) {
										availableNeighbors.add(newPos);
									}
								}
							}

							if (availableNeighbors.size() >= 2) {
								returnablePositions.add(pos);
								pos = availableNeighbors.get(0);
							} else if (availableNeighbors.size() == 1) {
								pos = availableNeighbors.get(0);
							} else if (returnablePositions.isEmpty()) {
								break;
							} else {
								i--;
								pos = returnablePositions.getLast();
							}
						}
					}
				}
				for (BlockPos pos : zapperBlocks) {
					Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
					drawFilledBoundingBox(block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, pos)
																		 .expand(0.001D, 0.001D, 0.001D).offset(-d0, -d1, -d2),
						1f, NotEnoughUpdates.INSTANCE.config.itemOverlays.zapperOverlayColour
					);
				}
				GlStateManager.depthMask(true);
				GlStateManager.enableTexture2D();
				GlStateManager.disableBlend();
			} else if (NotEnoughUpdates.INSTANCE.config.itemOverlays.enableTreecapOverlay &&
				(heldInternal.equals("JUNGLE_AXE") || heldInternal.equals("TREECAPITATOR_AXE"))) {
				int maxWood = 10;
				if (heldInternal.equals("TREECAPITATOR_AXE")) maxWood = 35;

				if (event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
					GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
					GlStateManager.disableTexture2D();
					GlStateManager.depthMask(false);

					if (Minecraft.getMinecraft().theWorld.getBlockState(event.target.getBlockPos()).getBlock() == Blocks.log ||
						Minecraft.getMinecraft().theWorld.getBlockState(event.target.getBlockPos()).getBlock() == Blocks.log2) {

						int woods = 0;

						HashSet<BlockPos> candidatesOld = new HashSet<>();
						LinkedList<BlockPos> candidates = new LinkedList<>();
						LinkedList<BlockPos> candidatesNew = new LinkedList<>();

						candidatesNew.add(event.target.getBlockPos());

						while (woods < maxWood) {
							if (candidatesNew.isEmpty()) {
								break;
							}

							candidates.addAll(candidatesNew);
							candidatesNew.clear();

							woods += candidates.size();
							boolean random = woods > maxWood;

							while (!candidates.isEmpty()) {
								BlockPos candidate = candidates.pop();
								Block block = Minecraft.getMinecraft().theWorld.getBlockState(candidate).getBlock();

								candidatesOld.add(candidate);

								for (int x = -1; x <= 1; x++) {
									for (int y = -1; y <= 1; y++) {
										for (int z = -1; z <= 1; z++) {
											if (x != 0 || y != 0 || z != 0) {
												BlockPos posNew = candidate.add(x, y, z);
												if (!candidatesOld.contains(posNew) && !candidates.contains(posNew) && !candidatesNew.contains(
													posNew)) {
													Block blockNew = Minecraft.getMinecraft().theWorld.getBlockState(posNew).getBlock();
													if (blockNew == Blocks.log || blockNew == Blocks.log2) {
														candidatesNew.add(posNew);
													}
												}
											}
										}
									}
								}

								block.setBlockBoundsBasedOnState(Minecraft.getMinecraft().theWorld, candidate);

								drawFilledBoundingBox(block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, candidate)
																					 .expand(0.001D, 0.001D, 0.001D).offset(-d0, -d1, -d2),
									random ? 0.5f : 1f, NotEnoughUpdates.INSTANCE.config.itemOverlays.treecapOverlayColour
								);
							}
						}
					}

					GlStateManager.depthMask(true);
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
				}
			} else if (NotEnoughUpdates.INSTANCE.config.itemOverlays.enableWandOverlay) {
				if (heldInternal.equals("BUILDERS_WAND") && onPrivateIsland) {
					buildersWandOverlay(event, d0, d1, d2);
				} else if (heldInternal.equals("BUILDERS_RULER") && onPrivateIsland) {
					buildersRulerOverlay(event, d0, d1, d2);
				}else if (heldInternal.equals("INFINIDIRT_WAND") &&
					event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
					NotEnoughUpdates.INSTANCE.config.itemOverlays.enableDirtWandOverlay) {
					BlockPos hover = event.target.getBlockPos().offset(event.target.sideHit, 1);
					if ("garden".equals(SBInfo.getInstance().getLocation()) &&
						(hover.getX() > 239 || hover.getX() < -240 || hover.getZ() > 239 || hover.getZ() < -240 ||
							hover.getY() <= 66 || hover.getY() > 76)) {
						return;
					}
					IBlockState hoverState = Minecraft.getMinecraft().theWorld.getBlockState(event.target
						.getBlockPos()
						.offset(event.target.sideHit, 1));
					if (hoverState.getBlock() == Blocks.air) {
						GlStateManager.enableBlend();
						GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
						GlStateManager.disableTexture2D();
						GlStateManager.depthMask(false);

						String special = NotEnoughUpdates.INSTANCE.config.itemOverlays.wandOverlayColour;

						AxisAlignedBB bb = Blocks.dirt.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, hover);
						drawBlock((int) bb.minX, (int) bb.minY, (int) bb.minZ + 1, Blocks.dirt.getDefaultState(),
							event.partialTicks, 0.75f
						);

						AxisAlignedBB bbExpanded = Blocks.dirt.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld, hover)
																									.expand(0.001D, 0.001D, 0.001D).offset(-d0, -d1, -d2);
						drawOutlineBoundingBox(bbExpanded, 1f, special);

						GlStateManager.depthMask(true);
						GlStateManager.enableTexture2D();
						GlStateManager.disableBlend();
					}
				} else if ((heldInternal.equals("WATER_BUCKET") || heldInternal.equals("MAGICAL_WATER_BUCKET") &&
					NotEnoughUpdates.INSTANCE.config.itemOverlays.enablePrismapumpOverlay && onPrivateIsland) &&
					event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
					BlockPos hover = event.target.getBlockPos().offset(event.target.sideHit, 1);

					HashSet<BlockPos> verticalSources = new HashSet<>();
					TreeMap<Float, HashMap<BlockPos, EnumFacing>> watersSorted = new TreeMap<>();

					for (int xOff = -1; xOff <= 1; xOff++) {
						for (int yOff = 0; yOff <= 1; yOff++) {
							for (int zOff = -1; zOff <= 1; zOff++) {
								if ((xOff == 0 && yOff == 0) ||
									(xOff == 0 && zOff == 0) ||
									(zOff == 0 && yOff == 0)) {

									BlockPos checkPos = hover.add(-xOff, -yOff, -zOff);
									IBlockState check = Minecraft.getMinecraft().theWorld.getBlockState(checkPos);
									if (check.getBlock() == Blocks.prismarine && check.getBlock().getMetaFromState(check) == 2) {
										for (int i = 0; i < 300; i++) {
											BlockPos renderPos = hover.add(xOff * i, yOff * i, zOff * i);

											if (Math.abs(renderPos.getX()) > 128) {
												break;
											}
											if (Math.abs(renderPos.getY()) > 255) {
												break;
											}
											if (Math.abs(renderPos.getZ()) > 128) {
												break;
											}

											IBlockState renderState = Minecraft.getMinecraft().theWorld.getBlockState(renderPos);

											if (renderState.getBlock() != Blocks.air && renderState.getBlock() != Blocks.water &&
												renderState.getBlock() != Blocks.flowing_water) {
												break;
											}

											if (yOff != 0) {
												verticalSources.add(renderPos);
											} else {
												IBlockState belowState = Minecraft.getMinecraft().theWorld.getBlockState(renderPos.add(
													0,
													-1,
													0
												));
												if (belowState.getBlock() == Blocks.air) {
													break;
												}
											}

											for (EnumFacing facing : EnumFacing.values()) {
												float xDist = (float) (renderPos.getX() + 0.5f + 0.5f * facing.getFrontOffsetX() - d0);
												float yDist = (float) (renderPos.getY() + 0.5f + 0.5f * facing.getFrontOffsetY() - d1 -
													player.getEyeHeight());
												float zDist = (float) (renderPos.getZ() + 0.5f + 0.5f * facing.getFrontOffsetZ() - d2);

												float distSq = xDist * xDist + yDist * yDist + zDist * zDist;

												watersSorted.computeIfAbsent(distSq, k -> new HashMap<>()).put(renderPos, facing);
											}

										}
									}
								}
							}
						}
					}

					GlStateManager.enableDepth();
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
					GlStateManager.disableTexture2D();
					GlStateManager.depthMask(true);

					for (HashMap<BlockPos, EnumFacing> blockPoses : watersSorted.values()) {
						for (Map.Entry<BlockPos, EnumFacing> entry : blockPoses.entrySet()) {
							boolean vertical = verticalSources.contains(entry.getKey());
							AxisAlignedBB bbExpanded = Blocks.water.getSelectedBoundingBox(
																								 Minecraft.getMinecraft().theWorld,
																								 entry.getKey()
																							 )
																										 .expand(-0.001D, -0.001D - (vertical ? 0 : 0.0625D), -0.001D)
																										 .offset(-d0, -d1 - (vertical ? 0 : 0.0625), -d2);
							drawFilledBoundingBoxSide(bbExpanded, entry.getValue(), 1f, "0:100:20:50:160");
						}
					}

					GlStateManager.depthMask(true);
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
				} else if ((heldInternal.equals("HOE_OF_GREAT_TILLING") || heldInternal.equals("HOE_OF_GREATER_TILLING") &&
					NotEnoughUpdates.INSTANCE.config.itemOverlays.enableHoeOverlay && onPrivateIsland) &&
					event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
					BlockPos target = event.target.getBlockPos();
					IBlockState targetState = Minecraft.getMinecraft().theWorld.getBlockState(target);

					int radius = heldInternal.equals("HOE_OF_GREAT_TILLING") ? 1 : 2;

					if (targetState.getBlock() == Blocks.dirt || targetState.getBlock() == Blocks.grass) {
						GlStateManager.enableDepth();
						GlStateManager.enableBlend();
						GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
						GlStateManager.disableTexture2D();
						GlStateManager.depthMask(true);

						for (int xOff = -radius; xOff <= radius; xOff++) {
							for (int zOff = -radius; zOff <= radius; zOff++) {
								BlockPos renderPos = target.add(xOff, 0, zOff);
								BlockPos airPos = renderPos.add(0, 1, 0);
								IBlockState renderState = Minecraft.getMinecraft().theWorld.getBlockState(renderPos);
								IBlockState airState = Minecraft.getMinecraft().theWorld.getBlockState(airPos);
								if (renderState.getBlock() == Blocks.dirt || renderState.getBlock() == Blocks.grass && airState.getBlock() == Blocks.air) {
									AxisAlignedBB bbExpanded = Blocks.dirt.getSelectedBoundingBox(
																										 Minecraft.getMinecraft().theWorld,
																										 renderPos
																									 )
																												.expand(0.001D, 0.001D, 0.001D)
																												.offset(-d0, -d1, -d2);
									drawFilledBoundingBox(bbExpanded, 1f, "0:100:178:34:34");
								}
							}
						}

						GlStateManager.depthMask(true);
						GlStateManager.enableTexture2D();
						GlStateManager.disableBlend();
					}
				} else if ((heldInternal.equals("HOE_OF_GREATEST_TILLING") &&
					NotEnoughUpdates.INSTANCE.config.itemOverlays.enableHoeOverlay && onPrivateIsland) &&
					event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
					BlockPos target = event.target.getBlockPos();
					IBlockState targetState = Minecraft.getMinecraft().theWorld.getBlockState(target);

					if (targetState.getBlock() == Blocks.dirt || targetState.getBlock() == Blocks.grass) {
						GlStateManager.enableDepth();
						GlStateManager.enableBlend();
						GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
						GlStateManager.disableTexture2D();
						GlStateManager.depthMask(true);

						LinkedList<BlockPos> candidates = new LinkedList<>();
						LinkedList<BlockPos> candidatesOld = new LinkedList<>();
						LinkedList<BlockPos> candidatesNew = new LinkedList<>();
						candidatesNew.add(target);

						while (candidatesOld.size() <= 100) {
							if (candidatesNew.isEmpty()) {
								break;
							}

							candidates.addAll(candidatesNew);
							candidatesNew.clear();

							while (!candidates.isEmpty()) {

								BlockPos candidate = candidates.pop();
								candidatesOld.add(candidate);

								float yaw = Minecraft.getMinecraft().thePlayer.getRotationYawHead();
								Facing facing = Facing.forDirection(yaw);
								int xOff = facing == Facing.WEST ? -1 : facing == Facing.EAST ? 1 : 0;
								int zOff = facing == Facing.NORTH ? -1 : facing == Facing.SOUTH ? 1 : 0;

								BlockPos renderPos = candidate.add(xOff, 0, zOff);
								BlockPos airPos = renderPos.add(0, 1, 0);
								IBlockState renderState = Minecraft.getMinecraft().theWorld.getBlockState(renderPos);
								IBlockState airState = Minecraft.getMinecraft().theWorld.getBlockState(airPos);
								if (!candidatesOld.contains(renderPos) && !candidates.contains(renderPos) && !candidatesNew.contains(
									renderPos)) {
									if (renderState.getBlock() == Blocks.dirt || renderState.getBlock() == Blocks.grass && airState.getBlock() == Blocks.air) {
										candidatesNew.add(renderPos);
									} else {
										break;
									}
								}
							}

							for (BlockPos renderPos : candidatesNew) {
								AxisAlignedBB bbExpanded = Blocks.dirt.getSelectedBoundingBox(
																									 Minecraft.getMinecraft().theWorld,
																									 renderPos
																								 )
																											.expand(0.001D, 0.001D, 0.001D)
																											.offset(-d0, -d1, -d2);
								drawFilledBoundingBox(bbExpanded, 1f, "0:100:178:34:34");
							}
						}

						AxisAlignedBB bbExpanded = Blocks.dirt.getSelectedBoundingBox(
																							 Minecraft.getMinecraft().theWorld,
																							 target
																						 )
																									.expand(0.001D, 0.001D, 0.001D)
																									.offset(-d0, -d1, -d2);
						drawFilledBoundingBox(bbExpanded, 1f, "0:100:178:34:34");

						GlStateManager.depthMask(true);
						GlStateManager.enableTexture2D();
						GlStateManager.disableBlend();
					}
				} else if ((heldInternal.equals("SAM_SCYTHE") || heldInternal.equals("GARDEN_SCYTHE") &&
					NotEnoughUpdates.INSTANCE.config.itemOverlays.enableScytheOverlay && onPrivateIsland) &&
					event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
					BlockPos target = event.target.getBlockPos();
					IBlockState targetState = Minecraft.getMinecraft().theWorld.getBlockState(target);

					int radius = heldInternal.equals("SAM_SCYTHE") ? 1 : 2;

					if (scytheBlocks.contains(targetState.getBlock())) {
						GlStateManager.enableDepth();
						GlStateManager.enableBlend();
						GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
						GlStateManager.disableTexture2D();
						GlStateManager.depthMask(true);

						for (int xOff = -radius; xOff <= radius; xOff++) {
							for (int yOff = -radius; yOff <= radius; yOff++) {
								for (int zOff = -radius; zOff <= radius; zOff++) {
									BlockPos renderPos = target.add(xOff, yOff, zOff);
									IBlockState renderState = Minecraft.getMinecraft().theWorld.getBlockState(renderPos);
									if (scytheBlocks.contains(renderState.getBlock())) {
										AxisAlignedBB bbExpanded = renderState.getBlock().getSelectedBoundingBox(
																											 Minecraft.getMinecraft().theWorld,
																											 renderPos
																										 )
																													.expand(0.001D, 0.001D, 0.001D)
																													.offset(-d0, -d1, -d2);
										drawFilledBoundingBox(bbExpanded, 1f, "0:100:178:34:34");
									}
								}
							}
						}

						GlStateManager.depthMask(true);
						GlStateManager.enableTexture2D();
						GlStateManager.disableBlend();
					}
				}
			}
		}
	}

	public void buildersWandOverlay(DrawBlockHighlightEvent event, double d0, double d1, double d2) {
		if (event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			IBlockState hover = Minecraft.getMinecraft().theWorld.getBlockState(event.target
				.getBlockPos()
				.offset(event.target.sideHit, 1));
			if (hover.getBlock() != Blocks.air) return;
			IBlockState match = Minecraft.getMinecraft().theWorld.getBlockState(event.target.getBlockPos());
			Item matchItem = Item.getItemFromBlock(match.getBlock());
			if (matchItem == null) return;
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);

			HashSet<BlockPos> candidatesOld = new HashSet<>();
			TreeMap<Float, Set<BlockPos>> candidatesOldSorted = new TreeMap<>();

			getBuildersWandCandidates(
				Minecraft.getMinecraft().thePlayer,
				event.target,
				event.partialTicks,
				candidatesOld,
				candidatesOldSorted,
				10
			);

			ItemStack matchStack;

			matchStack = new ItemStack(matchItem,
				1,
				match.getBlock().getDamageValue(Minecraft.getMinecraft().theWorld, event.target.getBlockPos())
			);

			int itemCount;
			if (match.getBlock() == Blocks.dirt && matchStack.getItemDamage() == 0 && hasDirtWand()) {
				itemCount = candidatesOld.size();
			} else {
				itemCount = countItemsInInventoryAndStorage(matchStack);
			}

			String special = (candidatesOld.size() <= itemCount)
				? NotEnoughUpdates.INSTANCE.config.itemOverlays.wandOverlayColour
				: "0:255:255:0:0";

			if (candidatesOld.size() <= MAX_BUILDERS_BLOCKS) {
				for (Set<BlockPos> candidatesSorted : candidatesOldSorted.values()) {
					for (BlockPos candidate : candidatesSorted) {
						match.getBlock().setBlockBoundsBasedOnState(Minecraft.getMinecraft().theWorld, candidate);
						Minecraft.getMinecraft().thePlayer.isSneaking();
						AxisAlignedBB bb = match.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld,
							candidate.add(0, 0, 0)
						).offset(event.target.sideHit.getFrontOffsetX(),
							event.target.sideHit.getFrontOffsetY(),
							event.target.sideHit.getFrontOffsetZ()
						);

						drawBlock((int) bb.minX, (int) bb.minY, (int) bb.minZ + 1, match, event.partialTicks, 0.75f);
					}
				}

				for (BlockPos candidate : candidatesOld) {
					match.getBlock().setBlockBoundsBasedOnState(Minecraft.getMinecraft().theWorld, candidate);
					Minecraft.getMinecraft().thePlayer.isSneaking();
					AxisAlignedBB bb = match.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld,
						candidate.add(0, 0, 0)
					).expand(0.001D, 0.001D, 0.001D).offset(-d0, -d1, -d2).offset(event.target.sideHit.getFrontOffsetX(),
						event.target.sideHit.getFrontOffsetY(),
						event.target.sideHit.getFrontOffsetZ()
					);

					drawOutlineBoundingBox(bb, 1f, special);
				}
			}

			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}
	}

	public void buildersRulerOverlay(DrawBlockHighlightEvent event, double d0, double d1, double d2) {
		if (event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			IBlockState hover = Minecraft.getMinecraft().theWorld.getBlockState(event.target
				.getBlockPos()
				.offset(event.target.sideHit, 1));

			if (hover.getBlock() != Blocks.air) return;

			IBlockState match = Minecraft.getMinecraft().theWorld.getBlockState(event.target.getBlockPos());
			Item matchItem = Item.getItemFromBlock(match.getBlock());
			if (matchItem == null) return;
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);

			HashSet<BlockPos> candidatesOld = new HashSet<>();
			TreeMap<Float, Set<BlockPos>> candidatesOldSorted = new TreeMap<>();

			getBuildersRulerCandidates(Minecraft.getMinecraft().thePlayer,
				event.target,
				event.partialTicks,
				candidatesOld,
				candidatesOldSorted,
				10
			);
			ItemStack firstItemInRuler = getFirstItemInRuler();
			if (!Minecraft.getMinecraft().thePlayer.isSneaking()) {
				Item item = firstItemInRuler == null ? null : firstItemInRuler.getItem();
				if (item != null) {
					if (item instanceof ItemBlock)
						match = ((ItemBlock) item).getBlock().getStateFromMeta(firstItemInRuler.getItemDamage());
					else match = Blocks.dirt.getDefaultState();
				}
			}

			ItemStack matchStack;
			if (Minecraft.getMinecraft().thePlayer.isSneaking()) {
				matchStack = new ItemStack(Minecraft.getMinecraft().theWorld
					.getBlockState(event.target.getBlockPos())
					.getBlock());
			} else {
				matchStack = firstItemInRuler;
			}
			int itemCount;
			if (matchStack != null) {
				if (match.getBlock() == Blocks.dirt && matchStack.getItemDamage() == 0 && hasDirtWand()) {
					itemCount = candidatesOld.size();
				} else {
					itemCount = countItemsInInventoryAndStorage(matchStack);
				}
			} else {
				return;
			}

			String special = (candidatesOld.size() <= itemCount)
				? NotEnoughUpdates.INSTANCE.config.itemOverlays.wandOverlayColour
				: "0:255:255:0:0";

			for (Set<BlockPos> candidatesSorted : candidatesOldSorted.values()) {
				for (BlockPos candidate : candidatesSorted) {
					AxisAlignedBB bb = match.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld,
						candidate.add(0, Minecraft.getMinecraft().thePlayer.isSneaking() ? -1 : 0, 0)
					).offset(event.target.sideHit.getFrontOffsetX(),
						event.target.sideHit.getFrontOffsetY(),
						event.target.sideHit.getFrontOffsetZ()
					);

					drawBlock((int) bb.minX, (int) bb.minY, (int) bb.minZ + 1, match, event.partialTicks, 0.75f);
				}
			}

			for (BlockPos candidate : candidatesOld) {
				AxisAlignedBB bb = match.getBlock().getSelectedBoundingBox(Minecraft.getMinecraft().theWorld,
					candidate.add(0, Minecraft.getMinecraft().thePlayer.isSneaking() ? -1 : 0, 0)
				).expand(0.001D, 0.001D, 0.001D).offset(-d0, -d1, -d2).offset(event.target.sideHit.getFrontOffsetX(),
					event.target.sideHit.getFrontOffsetY(),
					event.target.sideHit.getFrontOffsetZ()
				);

				drawOutlineBoundingBox(bb,
					1f,
					(Minecraft.getMinecraft().thePlayer.isSneaking())
						? NotEnoughUpdates.INSTANCE.config.itemOverlays.wandOverlayColour
						: special
				);
			}

			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
		}
	}

	public void getBuildersWandCandidates(
		EntityPlayer player, MovingObjectPosition target, float partialTicks,
		HashSet<BlockPos> candidatesOld, TreeMap<Float, Set<BlockPos>> candidatesOldSorted, int extraMax
	) {
		IBlockState match = Minecraft.getMinecraft().theWorld.getBlockState(target.getBlockPos());

		candidatesOld.clear();
		candidatesOldSorted.clear();
		LinkedList<BlockPos> candidates = new LinkedList<>();
		LinkedList<BlockPos> candidatesNew = new LinkedList<>();

		candidatesNew.add(target.getBlockPos());

		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

		while (candidatesOld.size() <= MAX_BUILDERS_BLOCKS + extraMax) {
			if (candidatesNew.isEmpty()) {
				break;
			}

			candidates.addAll(candidatesNew);
			candidatesNew.clear();

			while (!candidates.isEmpty()) {
				if (candidatesOld.size() > MAX_BUILDERS_BLOCKS + extraMax) break;

				BlockPos candidate = candidates.pop();

				float distSq = (float) ((candidate.getX() + 0.5f - d0) * (candidate.getX() + 0.5f - d0) +
					(candidate.getY() + 0.5f - d1 - player.getEyeHeight()) *
						(candidate.getY() + 0.5f - d1 - player.getEyeHeight()) +
					(candidate.getZ() + 0.5f - d2) * (candidate.getZ() + 0.5f - d2));
				candidatesOldSorted.computeIfAbsent(distSq, k -> new HashSet<>()).add(candidate);

				candidatesOld.add(candidate);

				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 1; y++) {
						for (int z = -1; z <= 1; z++) {
							if (x * x + y * y + z * z == 1) {
								if (((x == 0) && (target.sideHit.getAxis() == EnumFacing.Axis.X)) ||
									((y == 0) && (target.sideHit.getAxis() == EnumFacing.Axis.Y)) ||
									((z == 0) && (target.sideHit.getAxis() == EnumFacing.Axis.Z))) {
									if (Minecraft.getMinecraft().theWorld.getBlockState(candidate.add(
										x + target.sideHit.getFrontOffsetX(),
										y + target.sideHit.getFrontOffsetY(),
										z + target.sideHit.getFrontOffsetZ()
									)).getBlock() == Blocks.air) {
										BlockPos posNew = candidate.add(x, y, z);
										if (!candidatesOld.contains(posNew) && !candidates.contains(posNew) && !candidatesNew.contains(
											posNew)) {
											IBlockState blockNew = Minecraft.getMinecraft().theWorld.getBlockState(posNew);
											if (blockNew == match) {
												candidatesNew.add(posNew);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public enum Facing {
		NORTH,
		EAST,
		SOUTH,
		WEST;

		public static Facing forDirection(float direction) {
			double rotation = (direction - 90) % 360;
			if (rotation < 0) {
				rotation += 360.0;
			}

			if ((0.0D <= rotation) && (rotation < 45.0D))
				return WEST;
			if ((45.0D <= rotation) && (rotation < 135.0D))
				return NORTH;
			if ((135.0D <= rotation) && (rotation < 225.0D))
				return EAST;
			if ((225.0D <= rotation) && (rotation < 315.0D))
				return SOUTH;
			if ((315.0D <= rotation) && (rotation < 360.0D)) {
				return WEST;
			} else {
				return NORTH; // it uhhh shouldnt do this tho right
			}
		}
	}

	public void getBuildersRulerCandidates(
		EntityPlayer player,
		MovingObjectPosition target,
		float partialTicks,
		HashSet<BlockPos> candidatesOld,
		TreeMap<Float, Set<BlockPos>> candidatesOldSorted,
		int extraMax
	) {
		if (target.sideHit != EnumFacing.UP) return;
		boolean sneaking = Minecraft.getMinecraft().thePlayer.isSneaking();
		candidatesOld.clear();
		candidatesOldSorted.clear();
		LinkedList<BlockPos> candidates = new LinkedList<>();
		LinkedList<BlockPos> candidatesNew = new LinkedList<>();

		candidatesNew.add(target.getBlockPos());

		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

		while (candidatesOld.size() <= MAX_BUILDERS_BLOCKS + extraMax) {
			if (candidatesNew.isEmpty()) {
				break;
			}

			candidates.addAll(candidatesNew);
			candidatesNew.clear();

			while (!candidates.isEmpty()) {
				if (candidatesOld.size() > MAX_BUILDERS_BLOCKS + extraMax) break;

				BlockPos candidate = candidates.pop();

				float distSq = (float) ((candidate.getX() + 0.5f - d0) * (candidate.getX() + 0.5f - d0) +
					(candidate.getY() + 0.5f - d1 - player.getEyeHeight()) *
						(candidate.getY() + 0.5f - d1 - player.getEyeHeight()) +
					(candidate.getZ() + 0.5f - d2) * (candidate.getZ() + 0.5f - d2));
				if (candidate.getY() < 66 || candidate.getY() >= 76) {
					continue;
				}
				candidatesOldSorted.computeIfAbsent(distSq, k -> new HashSet<>()).add(candidate);

				candidatesOld.add(candidate);

				float yaw = Minecraft.getMinecraft().thePlayer.getRotationYawHead();
				Facing facing = Facing.forDirection(yaw);
				int xOff = facing == Facing.WEST ? -1 : facing == Facing.EAST ? 1 : 0;
				int zOff = facing == Facing.NORTH ? -1 : facing == Facing.SOUTH ? 1 : 0;
				BlockPos posNew = candidate.add(xOff, 0, zOff);
				if (
					"garden".equals(SBInfo.getInstance().getLocation())
				) {
					var plotX = MathHelper.floor_float((candidate.getX() + 48) / 96F);
					var plotXNew = MathHelper.floor_float((posNew.getX() + 48) / 96F);
					var plotZ = MathHelper.floor_float((candidate.getZ() + 48) / 96F);
					var plotZNew = MathHelper.floor_float((posNew.getZ() + 48) / 96F);
					if (plotX != plotXNew || plotZ != plotZNew) {
						continue;
					}
					if (2 < plotXNew || plotXNew < -2 || 2 < plotZNew || plotZNew < -2) {
						continue;
					}
				}
				if (!sneaking) {
					if (Minecraft.getMinecraft().theWorld.getBlockState(candidate.add(xOff, 1, zOff)).getBlock() == Blocks.air) {

						if (!candidatesOld.contains(posNew) && !candidates.contains(posNew) && !candidatesNew.contains(posNew)) {
							IBlockState blockNew = Minecraft.getMinecraft().theWorld.getBlockState(posNew.add(0, 1, 0));
							if (blockNew.getBlock() == Blocks.air) {
								candidatesNew.add(posNew);
							}
						}
					} else {
						break;
					}
				} else {
					if (Minecraft.getMinecraft().theWorld.getBlockState(candidate.add(xOff, 0, zOff)).getBlock()
						== Minecraft.getMinecraft().theWorld.getBlockState(target.getBlockPos()).getBlock()) {
						if (!candidatesOld.contains(posNew) && !candidates.contains(posNew) && !candidatesNew.contains(posNew)) {
							IBlockState blockNew = Minecraft.getMinecraft().theWorld.getBlockState(posNew.add(0, 0, 0));
							if (blockNew.getBlock() ==
								Minecraft.getMinecraft().theWorld.getBlockState(target.getBlockPos()).getBlock()) {
								candidatesNew.add(posNew);
							}
						}
					} else {
						break;
					}
				}

			}
		}
	}

	public static void drawBlock(int x, int y, int z, IBlockState state, float partialTicks, float brightness) {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

		BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

		GlStateManager.enableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);

		GlStateManager.enableDepth();
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.cullFace(GL11.GL_BACK);

		GlStateManager.pushMatrix();
		GlStateManager.translate(x - d0, y - d1, z - d2);

		int i = state.getBlock().getRenderType();
		if (i == 3) {
			IBakedModel ibakedmodel = blockrendererdispatcher.getModelFromBlockState(
				state,
				Minecraft.getMinecraft().theWorld,
				null
			);

			Block block = state.getBlock();
			block.setBlockBoundsForItemRender();
			GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
			int colour = block.getRenderColor(block.getStateForEntityRender(state));

			if (EntityRenderer.anaglyphEnable) {
				colour = TextureUtil.anaglyphColor(i);
			}

			colour = (colour & 0x00FFFFFF) | (100 << 24); //Set alpha to 100

			for (EnumFacing enumfacing : EnumFacing.values()) {
				renderModelBrightnessColorQuads(colour, ibakedmodel.getFaceQuads(enumfacing));
			}

			renderModelBrightnessColorQuads(colour, ibakedmodel.getGeneralQuads());
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.translate(-x + d0, -y + d1, -z + d2);
		GlStateManager.popMatrix();
	}

	private static void renderModelBrightnessColorQuads(int c, List<BakedQuad> listQuads) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		for (BakedQuad bakedquad : listQuads) {
			worldrenderer.begin(7, DefaultVertexFormats.ITEM);
			worldrenderer.addVertexData(bakedquad.getVertexData());

			worldrenderer.putColor4(c);

			Vec3i vec3i = bakedquad.getFace().getDirectionVec();
			worldrenderer.putNormal((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
			tessellator.draw();
		}
	}

	public static void drawFilledBoundingBox(AxisAlignedBB p_181561_0_, float alpha, String special) {
		Color c = new Color(SpecialColour.specialToChromaRGB(special), true);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableTexture2D();

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha);

		//vertical
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		tessellator.draw();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		tessellator.draw();

		GlStateManager.color(
			c.getRed() / 255f * 0.8f,
			c.getGreen() / 255f * 0.8f,
			c.getBlue() / 255f * 0.8f,
			c.getAlpha() / 255f * alpha
		);

		//x
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		tessellator.draw();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		tessellator.draw();

		GlStateManager.color(
			c.getRed() / 255f * 0.9f,
			c.getGreen() / 255f * 0.9f,
			c.getBlue() / 255f * 0.9f,
			c.getAlpha() / 255f * alpha
		);
		//z
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		tessellator.draw();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		tessellator.draw();
	}

	public static void drawFilledBoundingBoxSide(
		AxisAlignedBB p_181561_0_,
		EnumFacing facing,
		float alpha,
		String special
	) {
		Color c = new Color(SpecialColour.specialToChromaRGB(special), true);
		GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f * alpha);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableTexture2D();

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		switch (facing) {
			case UP:
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
				break;
			case DOWN:
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
				break;
			case EAST:
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
				break;
			case WEST:
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
				break;
			case SOUTH:
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
				break;
			case NORTH:
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
				worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
				break;
		}
		tessellator.draw();
	}

	public static void drawOutlineBoundingBox(AxisAlignedBB p_181561_0_, float alpha, String special) {
		Color c = new Color(SpecialColour.specialToChromaRGB(special), true);
		float newAlpha = c.getAlpha() / 255f * alpha;
		if (newAlpha > 1) newAlpha = 1;
		GlStateManager.color(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, newAlpha);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		GL11.glLineWidth(3);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(3, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		tessellator.draw();
		worldrenderer.begin(3, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		tessellator.draw();
		worldrenderer.begin(1, DefaultVertexFormats.POSITION);
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
		worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
		tessellator.draw();

		GL11.glLineWidth(1);
	}
}
