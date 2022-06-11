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

package io.github.moulberry.notenoughupdates.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.BlockPos;

public class OnBlockBreakSoundEffect extends NEUEvent {

	private ISound sound;
	private final BlockPos position;
	private final IBlockState block;

	public OnBlockBreakSoundEffect(ISound sound, BlockPos position, IBlockState block) {
		this.sound = sound;
		this.position = position;
		this.block = block;
	}

	@Override
	public boolean isCancelable() {
		return true;
	}

	public BlockPos getPosition() {
		return position;
	}

	public IBlockState getBlock() {
		return block;
	}

	public ISound getSound() {
		return sound;
	}

	public void setSound(ISound sound) {
		this.sound = sound;
	}
}
