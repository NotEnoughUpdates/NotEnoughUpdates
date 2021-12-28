package io.github.moulberry.notenoughupdates.options.seperateSections;

import com.google.gson.annotations.Expose;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigEditorBoolean;
import io.github.moulberry.notenoughupdates.core.config.annotations.ConfigOption;

public class Notifications {
    @Expose
    @ConfigOption(
            name = "Update Messages",
            desc = "Give a notification in chat whenever a new version of NEU is released"
    )
    @ConfigEditorBoolean
    public boolean showUpdateMsg = true;

    @Expose
    @ConfigOption(
            name = "RAM Warning",
            desc = "Warning when game starts with lots of RAM allocated\n" +
                    "\u00a7cBefore disabling this, please seriously read the message. If you complain about FPS issues without listening to the warning, that's your fault."
    )
    @ConfigEditorBoolean
    public boolean doRamNotif = true;

    /*@Expose
    @ConfigOption(
            name = "Wrong Pet",
            desc = "Gives a notification in chat whenever you're using a pet that doesnt match the same xp you're gathering."
    )
    @ConfigEditorBoolean
    public boolean showWrongPetMsg = false;*/
}
