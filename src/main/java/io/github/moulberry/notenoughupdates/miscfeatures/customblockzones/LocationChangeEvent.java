package io.github.moulberry.notenoughupdates.miscfeatures.customblockzones;

import net.minecraftforge.fml.common.eventhandler.Event;

public class LocationChangeEvent extends Event {
    public final String newLocation;
    public final String oldLocation;
    public LocationChangeEvent(String newLocation, String oldLocation)
    {
        this.newLocation = newLocation;
        this.oldLocation = oldLocation;
    }
}
