package io.github.moulberry.notenoughupdates.commands;

import io.github.moulberry.notenoughupdates.commands.dev.*;
import io.github.moulberry.notenoughupdates.commands.dungeon.DhCommand;
import io.github.moulberry.notenoughupdates.commands.dungeon.DnCommand;
import io.github.moulberry.notenoughupdates.commands.dungeon.JoinDungeonCommand;
import io.github.moulberry.notenoughupdates.commands.dungeon.MapCommand;
import io.github.moulberry.notenoughupdates.commands.help.*;
import io.github.moulberry.notenoughupdates.commands.misc.AhCommand;
import io.github.moulberry.notenoughupdates.commands.misc.CalendarCommand;
import io.github.moulberry.notenoughupdates.commands.misc.CosmeticsCommand;
import io.github.moulberry.notenoughupdates.commands.misc.CustomizeCommand;
import io.github.moulberry.notenoughupdates.commands.profile.CataCommand;
import io.github.moulberry.notenoughupdates.commands.profile.PeekCommand;
import io.github.moulberry.notenoughupdates.commands.profile.PvCommand;
import io.github.moulberry.notenoughupdates.commands.profile.ViewProfileCommand;
import io.github.moulberry.notenoughupdates.commands.repo.ReloadRepoCommand;
import io.github.moulberry.notenoughupdates.commands.repo.RepoModeCommand;
import io.github.moulberry.notenoughupdates.commands.repo.ResetRepoCommand;
import io.github.moulberry.notenoughupdates.miscfeatures.FairySouls;
import io.github.moulberry.notenoughupdates.miscgui.GuiEnchantColour;
import io.github.moulberry.notenoughupdates.miscgui.GuiInvButtonEditor;
import io.github.moulberry.notenoughupdates.miscgui.NEUOverlayPlacements;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Loader;

public class Commands {
    public Commands() {

        // Help Commands
        ClientCommandHandler.instance.registerCommand(new LinksCommand());
        ClientCommandHandler.instance.registerCommand(new HelpCommand());
        ClientCommandHandler.instance.registerCommand(new StorageViewerWhyCommand());
        ClientCommandHandler.instance.registerCommand(new FeaturesCommand());
        ClientCommandHandler.instance.registerCommand(new SettingsCommand());

        // Dev Commands
        ClientCommandHandler.instance.registerCommand(new PackDevCommand());
        ClientCommandHandler.instance.registerCommand(new DungeonWinTestCommand());
        ClientCommandHandler.instance.registerCommand(new StatsCommand());
        ClientCommandHandler.instance.registerCommand(new DevTestCommand());
        ClientCommandHandler.instance.registerCommand(new NullzeeSphereCommand());

        // Repo Commands
        ClientCommandHandler.instance.registerCommand(new ResetRepoCommand());
        ClientCommandHandler.instance.registerCommand(new RepoModeCommand());
        ClientCommandHandler.instance.registerCommand(new ReloadRepoCommand());

        // Profile Commands
        ClientCommandHandler.instance.registerCommand(new PeekCommand());
        ClientCommandHandler.instance.registerCommand(new ViewProfileCommand());
        ClientCommandHandler.instance.registerCommand(new PvCommand());
        if (!Loader.isModLoaded("skyblockextras")) ClientCommandHandler.instance.registerCommand(new CataCommand());

        // Dungeon Commands
        ClientCommandHandler.instance.registerCommand(new MapCommand());
        ClientCommandHandler.instance.registerCommand(new JoinDungeonCommand());
        ClientCommandHandler.instance.registerCommand(new DnCommand());
        ClientCommandHandler.instance.registerCommand(new DhCommand());

        // Misc Commands
        ClientCommandHandler.instance.registerCommand(new CosmeticsCommand());
        ClientCommandHandler.instance.registerCommand(new CustomizeCommand());
        ClientCommandHandler.instance.registerCommand(new ScreenCommand("neubuttons", GuiInvButtonEditor::new));
        ClientCommandHandler.instance.registerCommand(new ScreenCommand("neuec", GuiEnchantColour::new));
        ClientCommandHandler.instance.registerCommand(new ScreenCommand("neuoverlay", NEUOverlayPlacements::new));
        //ClientCommandHandler.instance.registerCommand(new ScreenCommand("neututorial", NeuTutorial::new));
        ClientCommandHandler.instance.registerCommand(new AhCommand());
        ClientCommandHandler.instance.registerCommand(new CalendarCommand());

        // Fairy Soul Commands
        ClientCommandHandler.instance.registerCommand(new FairySouls.FairySoulsCommand());
    }
}
