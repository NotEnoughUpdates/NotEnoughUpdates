package io.github.moulberry.notenoughupdates.miscfeatures.CustomBiomeTextures;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ChangeLocationEvent extends Event {
    public final String newLocation;
    public final String oldLocation;
    public ChangeLocationEvent(String newLocation, String oldLocation)
    {
        this.newLocation = newLocation;
        this.oldLocation = oldLocation;
    }
}
