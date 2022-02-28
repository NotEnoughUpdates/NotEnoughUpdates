package io.github.moulberry.notenoughupdates.options.customtypes;

import org.apache.commons.lang3.BitField;

public class DebugFlags {
	private int holder = 0;
	private BitField allBits = new BitField(0xFFFFFFFF);

	public DebugFlags(int value) { holder = value; }
	public boolean isSet(BitField bitField) { return bitField.isSet(holder); }
	public int getFlags() { return holder; }
	public void setFlags(int value) { holder = value; }
}
