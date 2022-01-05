package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class CustomBlockSounds {
    public static final ResourceLocation mithrilBreak = new ResourceLocation("notenoughupdates", "mithril.break");
    public static final ResourceLocation mithrilStep = new ResourceLocation("notenoughupdates", "mithril.step");
    public static final ResourceLocation gemstoneBreakRuby = new ResourceLocation("notenoughupdates", "gemstoneRuby.break");
    public static final ResourceLocation gemstoneBreakAmber = new ResourceLocation("notenoughupdates", "gemstoneAmber.break");
    public static final ResourceLocation gemstoneBreakAmethyst = new ResourceLocation("notenoughupdates", "gemstoneAmethyst.break");
    public static final ResourceLocation gemstoneBreakSapphire = new ResourceLocation("notenoughupdates", "gemstoneSapphire.break");
    public static final ResourceLocation gemstoneBreakJade = new ResourceLocation("notenoughupdates", "gemstoneJade.break");
    public static final ResourceLocation gemstoneBreakTopaz = new ResourceLocation("notenoughupdates", "gemstoneTopaz.break");
    public static final ResourceLocation gemstoneBreakJasper = new ResourceLocation("notenoughupdates", "gemstoneJasper.break");
    public static final ResourceLocation gemstoneStep = new ResourceLocation("notenoughupdates", "gemstone.step");
    public static final ResourceLocation titaniumBreak = new ResourceLocation("notenoughupdates", "titanium.break");
    public static final ResourceLocation titaniumStep = new ResourceLocation("notenoughupdates", "titanium.step");


    public static ISound replaceSoundEvent(ISound sound, ResourceLocation newEvent) {
        return new PositionedSoundRecord(
                newEvent,
                sound.getPitch(), sound.getVolume(),
                sound.getXPosF(), sound.getYPosF(), sound.getZPosF()
        );
    }
}
