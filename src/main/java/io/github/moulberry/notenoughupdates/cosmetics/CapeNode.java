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

package io.github.moulberry.notenoughupdates.cosmetics;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CapeNode {
	private static final NEUCape.Direction[] cardinals = new NEUCape.Direction[]{
		NEUCape.Direction.UP,
		NEUCape.Direction.RIGHT,
		NEUCape.Direction.DOWN,
		NEUCape.Direction.LEFT
	};

	public Vector3f position;
	public Vector3f lastPosition = new Vector3f();
	public Vector3f renderPosition = new Vector3f();
	public final Vector3f[] oldRenderPosition = new Vector3f[5];
	public final Vector3f velocity = new Vector3f();
	public Vector3f normal = null;
	public Vector3f sideNormal = null;
	public boolean fixed = false;

	public static final int DRAW_MASK_FRONT = 0b1;
	public static final int DRAW_MASK_BACK = 0b10;
	public static final int DRAW_MASK_SIDES = 0b100;

	public HashMap<NEUCape.Offset, CapeNode> neighbors = new HashMap<>();

	public float texU = 0;
	public float texV = 0;

	public float horzDistMult = 2f;
	public float vertDistMult = 0.5f;

	public float horzSideTexU = 0;
	public float horzSideTexVTop = 0;

	public float vertSideTexU = 0;
	public float vertSideTexVTop = 0;

	public final float gravity = 0.1f;
	public final float resistance = 0.5f;

	public static final int FLOAT_NUM = 20;

	public CapeNode(float x, float y, float z) {
		this.position = new Vector3f(x, y, z);
	}

	private List<Vector2f> getConstaints() {
		List<Vector2f> constaints = new ArrayList<>();
		for (NEUCape.Direction cardinal : cardinals) {
			for (int i = 1; i <= 2; i++) {
				NEUCape.Offset offset = new NEUCape.Offset(cardinal, i);
				CapeNode other = neighbors.get(offset);
				if (other != null) {
					int iOffset = offset.getXOffset() + NEUCape.HORZ_NODES * offset.getYOffset();
					constaints.add(new Vector2f(
						iOffset,
						i * NEUCape.targetDist * (cardinal.yOff == 0 ? horzDistMult : vertDistMult)
					));
				}
			}

		}
		return constaints;
	}

	public void loadIntoBuffer(FloatBuffer buffer) {
		loadVec3IntoBuffer(buffer, position);
		List<Vector2f> containts = getConstaints();
		buffer.put(containts.size());
		for (int i = 0; i < 8; i++) {
			if (i < containts.size()) {
				loadVec2IntoBuffer(buffer, containts.get(i));
			} else {
				loadVec2IntoBuffer(buffer, new Vector2f());
			}
		}
	}

	public void readFromBuffer(FloatBuffer buffer) {
		readVec3FromBuffer(buffer, position);
		buffer.position(buffer.position() + 17);
	}

	private void readVec3FromBuffer(FloatBuffer buffer, Vector3f vec) {
		vec.x = buffer.get();
		vec.y = buffer.get();
		vec.z = buffer.get();
	}

	private void loadVec2IntoBuffer(FloatBuffer buffer, Vector2f vec) {
		buffer.put(vec.x);
		buffer.put(vec.y);
	}

	private void loadVec3IntoBuffer(FloatBuffer buffer, Vector3f vec) {
		buffer.put(vec.x);
		buffer.put(vec.y);
		buffer.put(vec.z);
	}

	public void update() {
		if (!fixed) {
			velocity.y -= gravity * (resistance) / (1 - resistance);

			float actualResistance = resistance;
            /*BlockPos pos = new BlockPos(
                    MathHelper.floor_double(position.x),
                    MathHelper.floor_double(position.y),
                    MathHelper.floor_double(position.z));
            Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
            if(block.getMaterial().isLiquid()) {
                actualResistance = 0.8f;
            }*/

			velocity.scale(1 - actualResistance);

			Vector3f.add(position, velocity, position);
		}
	}

	public final CapeNode getNeighbor(NEUCape.Offset offset) {
		return neighbors.get(offset);
	}

	public final void setNeighbor(NEUCape.Offset offset, CapeNode node) {
		neighbors.put(offset, node);
	}

	public void applyForce(float dX, float dY, float dZ) {
		velocity.x += dX;
		velocity.y += dY;
		velocity.z += dZ;
	}

	public void move(float dX, float dY, float dZ) {
		position.x += dX;
		position.y += dY;
		position.z += dZ;
		lastPosition.x = position.x;
		lastPosition.y = position.y;
		lastPosition.z = position.z;
	}

	public void resetNormal() {
		normal = null;
		sideNormal = null;
	}

	public void resolveAll(float horzDistMult, boolean opt) {
		resolveBend(horzDistMult, opt);
		//resolveShear();
		resolveStruct(horzDistMult, opt);
	}

	public void resolve(CapeNode other, float targetDist, float strength, boolean opt) {
		double dX = position.x - other.position.x;
		double dY = position.y - other.position.y;
		double dZ = position.z - other.position.z;

		double distSq = dX * dX + dY * dY + dZ * dZ;

		double factor = (distSq - targetDist * targetDist) / (40 * distSq) * strength;

		factor = Math.max(-0.5f, factor);
		dX *= factor;
		dY *= factor;
		dZ *= factor;

		if (fixed || other.fixed) {
			dX *= 2;
			dY *= 2;
			dZ *= 2;
		}

		if (!fixed) {
			position.x -= dX;
			position.y -= dY;
			position.z -= dZ;
		}

		if (!other.fixed) {
			other.position.x += dX;
			other.position.y += dY;
			other.position.z += dZ;
		}
	}

	public void resolveStruct(float horzDistMult, boolean opt) {
		for (NEUCape.Direction cardinal : cardinals) {
			NEUCape.Offset offset = new NEUCape.Offset(cardinal, 1);
			CapeNode other = neighbors.get(offset);
			if (other != null) {
				resolve(other, NEUCape.targetDist * (cardinal.yOff == 0 ? horzDistMult : 1), 2f * 7.5f, opt);
			}
		}
	}

	public void resolveShear(float horzDistMult, boolean opt) {
		for (NEUCape.Direction d : new NEUCape.Direction[]{
			NEUCape.Direction.DOWNLEFT,
			NEUCape.Direction.UPLEFT,
			NEUCape.Direction.DOWNRIGHT,
			NEUCape.Direction.DOWNLEFT
		}) {
			NEUCape.Offset o = new NEUCape.Offset(d, 1);
			CapeNode neighbor = getNeighbor(o);
			if (neighbor != null) {
				resolve(neighbor, 1f * NEUCape.targetDist * (d.yOff == 0 ? horzDistMult : 1f), 0.5f * 7.5f, opt);
			}
		}
	}

	public void resolveBend(float horzDistMult, boolean opt) {
		for (NEUCape.Direction cardinal : cardinals) {
			NEUCape.Offset offset = new NEUCape.Offset(cardinal, 2);
			CapeNode other = neighbors.get(offset);
			if (other != null) {
				resolve(other, 2f * NEUCape.targetDist * (cardinal.yOff == 0 ? horzDistMult : 1), 1f * 7.5f, opt);
			}
		}
	}

	public Vector3f normal() {
		if (normal != null) return normal;

		normal = new Vector3f();
		for (int i = 0; i < cardinals.length; i++) {
			NEUCape.Direction dir1 = cardinals[i];
			NEUCape.Direction dir2 = cardinals[(i + 1) % cardinals.length];
			CapeNode node1 = getNeighbor(new NEUCape.Offset(dir1, 1));
			CapeNode node2 = getNeighbor(new NEUCape.Offset(dir2, 1));

			if (node1 == null || node2 == null) continue;

			Vector3f toCapeNode1 = Vector3f.sub(node1.renderPosition, renderPosition, null);
			Vector3f toCapeNode2 = Vector3f.sub(node2.renderPosition, renderPosition, null);
			Vector3f cross = Vector3f.cross(toCapeNode1, toCapeNode2, null);
			Vector3f.add(normal, cross.normalise(null), normal);
		}
		float l = normal.length();
		if (l != 0) {
			normal.scale(1f / l);
		}
		return normal;
	}

	public Vector3f sideNormal() {
		if (sideNormal != null) return sideNormal;

		sideNormal = new Vector3f();
		NEUCape.Direction[] cardinals = new NEUCape.Direction[]{
			NEUCape.Direction.UP,
			NEUCape.Direction.RIGHT,
			NEUCape.Direction.DOWN,
			NEUCape.Direction.LEFT
		};
		for (NEUCape.Direction cardinal : cardinals) {
			CapeNode nodeCardinal = getNeighbor(new NEUCape.Offset(cardinal, 1));
			if (nodeCardinal == null) {
				NEUCape.Direction dirLeft = cardinal.rotateLeft90();
				NEUCape.Direction dirRight = cardinal.rotateRight90();
				CapeNode nodeLeft = getNeighbor(new NEUCape.Offset(dirLeft, 1));
				CapeNode nodeRight = getNeighbor(new NEUCape.Offset(dirRight, 1));

				if (nodeRight != null) {
					Vector3f toOther = Vector3f.sub(nodeRight.renderPosition, renderPosition, null);
					Vector3f cross = Vector3f.cross(normal(), toOther, null); //Inverted
					Vector3f.add(sideNormal, cross.normalise(null), sideNormal);
				}
				if (nodeLeft != null) {
					Vector3f toOther = Vector3f.sub(nodeLeft.renderPosition, renderPosition, null);
					Vector3f cross = Vector3f.cross(toOther, normal(), null);
					Vector3f.add(sideNormal, cross.normalise(null), sideNormal);
				}
			}
		}
		float l = sideNormal.length();
		if (l != 0) {
			sideNormal.scale(0.05f / l);
		}
		return sideNormal;
	}

	public void renderNode() {
		renderNode(DRAW_MASK_FRONT | DRAW_MASK_BACK | DRAW_MASK_SIDES);
	}

	public void renderNode(int mask) {
		CapeNode nodeLeft = getNeighbor(new NEUCape.Offset(NEUCape.Direction.LEFT, 1));
		CapeNode nodeUp = getNeighbor(new NEUCape.Offset(NEUCape.Direction.UP, 1));
		CapeNode nodeDown = getNeighbor(new NEUCape.Offset(NEUCape.Direction.DOWN, 1));
		CapeNode nodeRight = getNeighbor(new NEUCape.Offset(NEUCape.Direction.RIGHT, 1));
		CapeNode nodeDownRight = getNeighbor(new NEUCape.Offset(NEUCape.Direction.DOWNRIGHT, 1));

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		if (nodeDown != null && nodeRight != null && nodeDownRight != null) {
			//Back
			if ((mask & DRAW_MASK_BACK) != 0) {
				worldrenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
				for (CapeNode node : new CapeNode[]{this, nodeDown, nodeRight, nodeDownRight}) {
					Vector3f nodeNorm = node.normal();
					worldrenderer.pos(node.renderPosition.x, node.renderPosition.y, node.renderPosition.z)
											 .tex(1 - node.texU, node.texV)
											 .normal(-nodeNorm.x, -nodeNorm.y, -nodeNorm.z).endVertex();
				}
				tessellator.draw();
			}

			//Front (Offset by normal)
			if ((mask & DRAW_MASK_FRONT) != 0) {
				worldrenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
				for (CapeNode node : new CapeNode[]{nodeDownRight, nodeDown, nodeRight, this}) {
					Vector3f nodeNorm = node.normal();
					worldrenderer.pos(
												 node.renderPosition.x + nodeNorm.x * 0.05f,
												 node.renderPosition.y + nodeNorm.y * 0.05f,
												 node.renderPosition.z + nodeNorm.z * 0.05f
											 )
											 .tex(node.texU, node.texV)
											 .normal(nodeNorm.x, nodeNorm.y, nodeNorm.z).endVertex();
				}
				tessellator.draw();
			}
		}

		if ((mask & DRAW_MASK_SIDES) != 0) {
			if (nodeLeft == null || nodeRight == null) {
				//Render left/right edge
				if (nodeDown != null) {
					renderEdge(nodeDown, true);
				}
			}
			if (nodeUp == null || nodeDown == null) {
				//Render up/down edge
				if (nodeRight != null) {
					renderEdge(nodeRight, false);
				}
			}
		}
	}

	public void renderEdge(CapeNode other, boolean lr) {
		float thisTexU = lr ? this.horzSideTexU : this.vertSideTexU;
		float thisTexV = lr ? this.horzSideTexVTop : this.vertSideTexVTop;
		float otherTexU = lr ? other.horzSideTexU : other.vertSideTexU;
		float otherTexV = lr ? other.horzSideTexVTop : other.vertSideTexVTop;

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		Vector3f thisNorm = normal();
		Vector3f otherNorm = other.normal();

		Vector3f thisSideNorm = sideNormal();
		Vector3f otherSideNorm = other.sideNormal();

		worldrenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_NORMAL);
		worldrenderer.pos(this.renderPosition.x, this.renderPosition.y, this.renderPosition.z)
								 .tex(thisTexU, thisTexV + 20 / 1024f)
								 .normal(thisSideNorm.x, thisSideNorm.y, thisSideNorm.z).endVertex();
		worldrenderer.pos(other.renderPosition.x, other.renderPosition.y, other.renderPosition.z)
								 .tex(otherTexU, otherTexV + 20 / 1024f)
								 .normal(otherSideNorm.x, otherSideNorm.y, otherSideNorm.z).endVertex();
		worldrenderer.pos(
									 this.renderPosition.x + thisNorm.x * 0.05f,
									 this.renderPosition.y + thisNorm.y * 0.05f,
									 this.renderPosition.z + thisNorm.z * 0.05f
								 )
								 .tex(thisTexU, thisTexV)
								 .normal(thisSideNorm.x, thisSideNorm.y, thisSideNorm.z).endVertex();
		worldrenderer.pos(
									 other.renderPosition.x + otherNorm.x * 0.05f,
									 other.renderPosition.y + otherNorm.y * 0.05f,
									 other.renderPosition.z + otherNorm.z * 0.05f
								 )
								 .tex(otherTexU, otherTexV)
								 .normal(otherSideNorm.x, otherSideNorm.y, otherSideNorm.z).endVertex();
		tessellator.draw();
	}
}
