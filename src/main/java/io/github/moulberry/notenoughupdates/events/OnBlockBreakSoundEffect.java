package io.github.moulberry.notenoughupdates.events;

import net.minecraft.block.state.BlockState;
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
