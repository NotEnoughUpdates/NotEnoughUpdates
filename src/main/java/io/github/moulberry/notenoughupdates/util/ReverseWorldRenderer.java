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

package io.github.moulberry.notenoughupdates.util;

import com.google.common.primitives.Floats;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.BitSet;

@SideOnly(Side.CLIENT)
public class ReverseWorldRenderer {
	private ByteBuffer byteBuffer;
	private IntBuffer rawIntBuffer;
	private ShortBuffer rawShortBuffer;
	private FloatBuffer rawFloatBuffer;
	private int vertexCount;
	private VertexFormatElement vertexFormatElement;
	private int vertexFormatIndex;
	/**
	 * None
	 */
	private boolean noColor;
	private int drawMode;
	private double xOffset;
	private double yOffset;
	private double zOffset;
	private VertexFormat vertexFormat;
	private boolean isDrawing;

	public ReverseWorldRenderer(int bufferSizeIn) {
		this.byteBuffer = GLAllocation.createDirectByteBuffer(bufferSizeIn * 4);
		this.rawIntBuffer = this.byteBuffer.asIntBuffer();
		this.rawShortBuffer = this.byteBuffer.asShortBuffer();
		this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
	}

	private void growBuffer(int p_181670_1_) {
		if (p_181670_1_ > this.rawIntBuffer.remaining()) {
			int i = this.byteBuffer.capacity();
			int j = i % 2097152;
			int k = j + (((this.rawIntBuffer.position() + p_181670_1_) * 4 - j) / 2097152 + 1) * 2097152;
			LogManager
				.getLogger()
				.warn("Needed to grow BufferBuilder buffer: Old size " + i + " bytes, new size " + k + " bytes.");
			int l = this.rawIntBuffer.position();
			ByteBuffer bytebuffer = GLAllocation.createDirectByteBuffer(k);
			this.byteBuffer.position(0);
			bytebuffer.put(this.byteBuffer);
			bytebuffer.rewind();
			this.byteBuffer = bytebuffer;
			this.rawFloatBuffer = this.byteBuffer.asFloatBuffer().asReadOnlyBuffer();
			this.rawIntBuffer = this.byteBuffer.asIntBuffer();
			this.rawIntBuffer.position(l);
			this.rawShortBuffer = this.byteBuffer.asShortBuffer();
			this.rawShortBuffer.position(l << 1);
		}
	}

	public void sortVertexData(float p_181674_1_, float p_181674_2_, float p_181674_3_) {
		int i = this.vertexCount / 4;
		final float[] afloat = new float[i];

		for (int j = 0; j < i; ++j) {
			afloat[j] = func_181665_a(
				this.rawFloatBuffer,
				(float) ((double) p_181674_1_ + this.xOffset),
				(float) ((double) p_181674_2_ + this.yOffset),
				(float) ((double) p_181674_3_ + this.zOffset),
				this.vertexFormat.getIntegerSize(),
				j * this.vertexFormat.getNextOffset()
			);
		}

		Integer[] ainteger = new Integer[i];

		for (int k = 0; k < ainteger.length; ++k) {
			ainteger[k] = k;
		}

		Arrays.sort(ainteger, (p_compare_1_, p_compare_2_) -> -Floats.compare(afloat[p_compare_2_], afloat[p_compare_1_]));
		BitSet bitset = new BitSet();
		int l = this.vertexFormat.getNextOffset();
		int[] aint = new int[l];

		for (int l1 = 0; (l1 = bitset.nextClearBit(l1)) < ainteger.length; ++l1) {
			int i1 = ainteger[l1];

			if (i1 != l1) {
				this.rawIntBuffer.limit(i1 * l + l);
				this.rawIntBuffer.position(i1 * l);
				this.rawIntBuffer.get(aint);
				int j1 = i1;

				for (int k1 = ainteger[i1]; j1 != l1; k1 = ainteger[k1]) {
					this.rawIntBuffer.limit(k1 * l + l);
					this.rawIntBuffer.position(k1 * l);
					IntBuffer intbuffer = this.rawIntBuffer.slice();
					this.rawIntBuffer.limit(j1 * l + l);
					this.rawIntBuffer.position(j1 * l);
					this.rawIntBuffer.put(intbuffer);
					bitset.set(j1);
					j1 = k1;
				}

				this.rawIntBuffer.limit(l1 * l + l);
				this.rawIntBuffer.position(l1 * l);
				this.rawIntBuffer.put(aint);
			}

			bitset.set(l1);
		}
	}

	public State getVertexState() {
		this.rawIntBuffer.rewind();
		int i = this.getBufferSize();
		this.rawIntBuffer.limit(i);
		int[] aint = new int[i];
		this.rawIntBuffer.get(aint);
		this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
		this.rawIntBuffer.position(i);
		return new State(aint, new VertexFormat(this.vertexFormat));
	}

	private int getBufferSize() {
		return this.vertexCount * this.vertexFormat.getIntegerSize();
	}

	private static float func_181665_a(
		FloatBuffer p_181665_0_,
		float p_181665_1_,
		float p_181665_2_,
		float p_181665_3_,
		int p_181665_4_,
		int p_181665_5_
	) {
		float f = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 0 + 0);
		float f1 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 0 + 1);
		float f2 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 0 + 2);
		float f3 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 1 + 0);
		float f4 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 1 + 1);
		float f5 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 1 + 2);
		float f6 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 0);
		float f7 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 1);
		float f8 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 2 + 2);
		float f9 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 0);
		float f10 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 1);
		float f11 = p_181665_0_.get(p_181665_5_ + p_181665_4_ * 3 + 2);
		float f12 = (f + f3 + f6 + f9) * 0.25F - p_181665_1_;
		float f13 = (f1 + f4 + f7 + f10) * 0.25F - p_181665_2_;
		float f14 = (f2 + f5 + f8 + f11) * 0.25F - p_181665_3_;
		return f12 * f12 + f13 * f13 + f14 * f14;
	}

	public void setVertexState(State state) {
		this.rawIntBuffer.clear();
		this.growBuffer(state.getRawBuffer().length);
		this.rawIntBuffer.put(state.getRawBuffer());
		this.vertexCount = state.getVertexCount();
		this.vertexFormat = new VertexFormat(state.getVertexFormat());
	}

	public void reset() {
		this.vertexCount = 0;
		this.vertexFormatElement = null;
		this.vertexFormatIndex = 0;
	}

	public void begin(int glMode, VertexFormat format) {
		if (this.isDrawing) {
			throw new IllegalStateException("Already building!");
		} else {
			this.isDrawing = true;
			this.reset();
			this.drawMode = glMode;
			this.vertexFormat = format;
			this.vertexFormatElement = format.getElement(this.vertexFormatIndex);
			this.noColor = false;
			this.byteBuffer.limit(this.byteBuffer.capacity());
		}
	}

	public ReverseWorldRenderer tex(double u, double v) {
		int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

		switch (this.vertexFormatElement.getType()) {
			case FLOAT:
				this.byteBuffer.putFloat(i, (float) u);
				this.byteBuffer.putFloat(i + 4, (float) v);
				break;
			case UINT:
			case INT:
				this.byteBuffer.putInt(i, (int) u);
				this.byteBuffer.putInt(i + 4, (int) v);
				break;
			case USHORT:
			case SHORT:
				this.byteBuffer.putShort(i, (short) ((int) v));
				this.byteBuffer.putShort(i + 2, (short) ((int) u));
				break;
			case UBYTE:
			case BYTE:
				this.byteBuffer.put(i, (byte) ((int) v));
				this.byteBuffer.put(i + 1, (byte) ((int) u));
		}

		this.nextVertexFormatIndex();
		return this;
	}

	public ReverseWorldRenderer lightmap(int p_181671_1_, int p_181671_2_) {
		int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

		switch (this.vertexFormatElement.getType()) {
			case FLOAT:
				this.byteBuffer.putFloat(i, (float) p_181671_1_);
				this.byteBuffer.putFloat(i + 4, (float) p_181671_2_);
				break;
			case UINT:
			case INT:
				this.byteBuffer.putInt(i, p_181671_1_);
				this.byteBuffer.putInt(i + 4, p_181671_2_);
				break;
			case USHORT:
			case SHORT:
				this.byteBuffer.putShort(i, (short) p_181671_2_);
				this.byteBuffer.putShort(i + 2, (short) p_181671_1_);
				break;
			case UBYTE:
			case BYTE:
				this.byteBuffer.put(i, (byte) p_181671_2_);
				this.byteBuffer.put(i + 1, (byte) p_181671_1_);
		}

		this.nextVertexFormatIndex();
		return this;
	}

	public void putBrightness4(int p_178962_1_, int p_178962_2_, int p_178962_3_, int p_178962_4_) {
		int i = (this.vertexCount - 4) * this.vertexFormat.getIntegerSize() + this.vertexFormat.getUvOffsetById(1) / 4;
		int j = this.vertexFormat.getNextOffset() >> 2;
		this.rawIntBuffer.put(i, p_178962_1_);
		this.rawIntBuffer.put(i + j, p_178962_2_);
		this.rawIntBuffer.put(i + j * 2, p_178962_3_);
		this.rawIntBuffer.put(i + j * 3, p_178962_4_);
	}

	public void putPosition(double x, double y, double z) {
		int i = this.vertexFormat.getIntegerSize();
		int j = (this.vertexCount - 4) * i;

		for (int k = 0; k < 4; ++k) {
			int l = j + k * i;
			int i1 = l + 1;
			int j1 = i1 + 1;
			this.rawIntBuffer.put(
				l,
				Float.floatToRawIntBits((float) (x + this.xOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(l)))
			);
			this.rawIntBuffer.put(
				i1,
				Float.floatToRawIntBits((float) (y + this.yOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(i1)))
			);
			this.rawIntBuffer.put(
				j1,
				Float.floatToRawIntBits((float) (z + this.zOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(j1)))
			);
		}
	}

	/**
	 * Takes in the pass the call list is being requested for. Args: renderPass
	 */
	public int getColorIndex(int p_78909_1_) {
		return ((this.vertexCount - p_78909_1_) * this.vertexFormat.getNextOffset() + this.vertexFormat.getColorOffset()) /
			4;
	}

	public void putColorMultiplier(float red, float green, float blue, int p_178978_4_) {
		int i = this.getColorIndex(p_178978_4_);
		int j = -1;

		if (!this.noColor) {
			j = this.rawIntBuffer.get(i);

			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				int k = (int) ((float) (j & 255) * red);
				int l = (int) ((float) (j >> 8 & 255) * green);
				int i1 = (int) ((float) (j >> 16 & 255) * blue);
				j = j & -16777216;
				j = j | i1 << 16 | l << 8 | k;
			} else {
				int j1 = (int) ((float) (j >> 24 & 255) * red);
				int k1 = (int) ((float) (j >> 16 & 255) * green);
				int l1 = (int) ((float) (j >> 8 & 255) * blue);
				j = j & 255;
				j = j | j1 << 24 | k1 << 16 | l1 << 8;
			}
		}

		this.rawIntBuffer.put(i, j);
	}

	private void putColor(int argb, int p_178988_2_) {
		int i = this.getColorIndex(p_178988_2_);
		int j = argb >> 16 & 255;
		int k = argb >> 8 & 255;
		int l = argb & 255;
		int i1 = argb >> 24 & 255;
		this.putColorRGBA(i, j, k, l, i1);
	}

	public void putColorRGB_F(float red, float green, float blue, int p_178994_4_) {
		int i = this.getColorIndex(p_178994_4_);
		int j = MathHelper.clamp_int((int) (red * 255.0F), 0, 255);
		int k = MathHelper.clamp_int((int) (green * 255.0F), 0, 255);
		int l = MathHelper.clamp_int((int) (blue * 255.0F), 0, 255);
		this.putColorRGBA(i, j, k, l, 255);
	}

	public void putColorRGBA(int index, int red, int p_178972_3_, int p_178972_4_, int p_178972_5_) {
		if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
			this.rawIntBuffer.put(index, p_178972_5_ << 24 | p_178972_4_ << 16 | p_178972_3_ << 8 | red);
		} else {
			this.rawIntBuffer.put(index, red << 24 | p_178972_3_ << 16 | p_178972_4_ << 8 | p_178972_5_);
		}
	}

	/**
	 * Disabels color processing.
	 */
	public void noColor() {
		this.noColor = true;
	}

	public ReverseWorldRenderer color(float red, float green, float blue, float alpha) {
		return this.color((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), (int) (alpha * 255.0F));
	}

	public ReverseWorldRenderer color(int red, int green, int blue, int alpha) {
		if (!this.noColor) {
			int i =
				this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

			switch (this.vertexFormatElement.getType()) {
				case FLOAT:
					this.byteBuffer.putFloat(i, (float) red / 255.0F);
					this.byteBuffer.putFloat(i + 4, (float) green / 255.0F);
					this.byteBuffer.putFloat(i + 8, (float) blue / 255.0F);
					this.byteBuffer.putFloat(i + 12, (float) alpha / 255.0F);
					break;
				case UINT:
				case INT:
					this.byteBuffer.putFloat(i, (float) red);
					this.byteBuffer.putFloat(i + 4, (float) green);
					this.byteBuffer.putFloat(i + 8, (float) blue);
					this.byteBuffer.putFloat(i + 12, (float) alpha);
					break;
				case USHORT:
				case SHORT:
					this.byteBuffer.putShort(i, (short) red);
					this.byteBuffer.putShort(i + 2, (short) green);
					this.byteBuffer.putShort(i + 4, (short) blue);
					this.byteBuffer.putShort(i + 6, (short) alpha);
					break;
				case UBYTE:
				case BYTE:
					if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
						this.byteBuffer.put(i, (byte) red);
						this.byteBuffer.put(i + 1, (byte) green);
						this.byteBuffer.put(i + 2, (byte) blue);
						this.byteBuffer.put(i + 3, (byte) alpha);
					} else {
						this.byteBuffer.put(i, (byte) alpha);
						this.byteBuffer.put(i + 1, (byte) blue);
						this.byteBuffer.put(i + 2, (byte) green);
						this.byteBuffer.put(i + 3, (byte) red);
					}
			}

			this.nextVertexFormatIndex();
		}
		return this;
	}

	public void addVertexData(int[] vertexData) {
		this.growBuffer(vertexData.length);
		this.rawIntBuffer.position(this.getBufferSize());
		this.rawIntBuffer.put(vertexData);
		this.vertexCount += vertexData.length / this.vertexFormat.getIntegerSize();
	}

	public void endVertex() {
		++this.vertexCount;
		this.growBuffer(this.vertexFormat.getIntegerSize());
	}

	public ReverseWorldRenderer pos(double x, double y, double z) {
		int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

		switch (this.vertexFormatElement.getType()) {
			case FLOAT:
				this.byteBuffer.putFloat(i, (float) (x + this.xOffset));
				this.byteBuffer.putFloat(i + 4, (float) (y + this.yOffset));
				this.byteBuffer.putFloat(i + 8, (float) (z + this.zOffset));
				break;
			case UINT:
			case INT:
				this.byteBuffer.putInt(i, Float.floatToRawIntBits((float) (x + this.xOffset)));
				this.byteBuffer.putInt(i + 4, Float.floatToRawIntBits((float) (y + this.yOffset)));
				this.byteBuffer.putInt(i + 8, Float.floatToRawIntBits((float) (z + this.zOffset)));
				break;
			case USHORT:
			case SHORT:
				this.byteBuffer.putShort(i, (short) ((int) (x + this.xOffset)));
				this.byteBuffer.putShort(i + 2, (short) ((int) (y + this.yOffset)));
				this.byteBuffer.putShort(i + 4, (short) ((int) (z + this.zOffset)));
				break;
			case UBYTE:
			case BYTE:
				this.byteBuffer.put(i, (byte) ((int) (x + this.xOffset)));
				this.byteBuffer.put(i + 1, (byte) ((int) (y + this.yOffset)));
				this.byteBuffer.put(i + 2, (byte) ((int) (z + this.zOffset)));
		}

		this.nextVertexFormatIndex();
		return this;
	}

	public void putNormal(float x, float y, float z) {
		int i = (byte) ((int) (x * 127.0F)) & 255;
		int j = (byte) ((int) (y * 127.0F)) & 255;
		int k = (byte) ((int) (z * 127.0F)) & 255;
		int l = i | j << 8 | k << 16;
		int i1 = this.vertexFormat.getNextOffset() >> 2;
		int j1 = (this.vertexCount - 4) * i1 + this.vertexFormat.getNormalOffset() / 4;
		this.rawIntBuffer.put(j1, l);
		this.rawIntBuffer.put(j1 + i1, l);
		this.rawIntBuffer.put(j1 + i1 * 2, l);
		this.rawIntBuffer.put(j1 + i1 * 3, l);
	}

	private void nextVertexFormatIndex() {
		++this.vertexFormatIndex;
		this.vertexFormatIndex %= this.vertexFormat.getElementCount();
		this.vertexFormatElement = this.vertexFormat.getElement(this.vertexFormatIndex);

		if (this.vertexFormatElement.getUsage() == VertexFormatElement.EnumUsage.PADDING) {
			this.nextVertexFormatIndex();
		}
	}

	public ReverseWorldRenderer normal(float p_181663_1_, float p_181663_2_, float p_181663_3_) {
		int i = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);

		switch (this.vertexFormatElement.getType()) {
			case FLOAT:
				this.byteBuffer.putFloat(i, p_181663_1_);
				this.byteBuffer.putFloat(i + 4, p_181663_2_);
				this.byteBuffer.putFloat(i + 8, p_181663_3_);
				break;
			case UINT:
			case INT:
				this.byteBuffer.putInt(i, (int) p_181663_1_);
				this.byteBuffer.putInt(i + 4, (int) p_181663_2_);
				this.byteBuffer.putInt(i + 8, (int) p_181663_3_);
				break;
			case USHORT:
			case SHORT:
				this.byteBuffer.putShort(i, (short) ((int) (p_181663_1_ * 32767) & 65535));
				this.byteBuffer.putShort(i + 2, (short) ((int) (p_181663_2_ * 32767) & 65535));
				this.byteBuffer.putShort(i + 4, (short) ((int) (p_181663_3_ * 32767) & 65535));
				break;
			case UBYTE:
			case BYTE:
				this.byteBuffer.put(i, (byte) ((int) (p_181663_1_ * 127) & 255));
				this.byteBuffer.put(i + 1, (byte) ((int) (p_181663_2_ * 127) & 255));
				this.byteBuffer.put(i + 2, (byte) ((int) (p_181663_3_ * 127) & 255));
		}

		this.nextVertexFormatIndex();
		return this;
	}

	public void setTranslation(double x, double y, double z) {
		this.xOffset = x;
		this.yOffset = y;
		this.zOffset = z;
	}

	public void finishDrawing() {
		if (!this.isDrawing) {
			throw new IllegalStateException("Not building!");
		} else {
			this.isDrawing = false;
			this.byteBuffer.position(0);
			this.byteBuffer.limit(this.getBufferSize() * 4);
		}
	}

	public ByteBuffer getByteBuffer() {
		return this.byteBuffer;
	}

	public VertexFormat getVertexFormat() {
		return this.vertexFormat;
	}

	public int getVertexCount() {
		return this.vertexCount;
	}

	public int getDrawMode() {
		return this.drawMode;
	}

	public void putColor4(int argb) {
		for (int i = 0; i < 4; ++i) {
			this.putColor(argb, i + 1);
		}
	}

	public void putColorRGB_F4(float red, float green, float blue) {
		for (int i = 0; i < 4; ++i) {
			this.putColorRGB_F(red, green, blue, i + 1);
		}
	}

	public void checkAndGrow() {
		this.growBuffer(vertexFormat.getNextOffset()/* / 4 * 4 */);
	}

	public boolean isColorDisabled() {
		/* None */
		return noColor;
	}

	@SideOnly(Side.CLIENT)
	public static class State {
		private final int[] stateRawBuffer;
		private final VertexFormat stateVertexFormat;

		public State(int[] buffer, VertexFormat format) {
			this.stateRawBuffer = buffer;
			this.stateVertexFormat = format;
		}

		public int[] getRawBuffer() {
			return this.stateRawBuffer;
		}

		public int getVertexCount() {
			return this.stateRawBuffer.length / this.stateVertexFormat.getIntegerSize();
		}

		public VertexFormat getVertexFormat() {
			return this.stateVertexFormat;
		}
	}
}
