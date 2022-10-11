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

package io.github.moulberry.notenoughupdates.commands;

import io.github.moulberry.notenoughupdates.commands.dev.DevTestCommand;
import io.github.moulberry.notenoughupdates.commands.dev.DiagCommand;
import io.github.moulberry.notenoughupdates.commands.dev.DungeonWinTestCommand;
import io.github.moulberry.notenoughupdates.commands.dev.EnableStorageCommand;
import io.github.moulberry.notenoughupdates.commands.dev.NullzeeSphereCommand;
import io.github.moulberry.notenoughupdates.commands.dev.PackDevCommand;
import io.github.moulberry.notenoughupdates.commands.dev.ReloadRepoCommand;
import io.github.moulberry.notenoughupdates.commands.dev.ResetRepoCommand;
import io.github.moulberry.notenoughupdates.commands.dev.StatsCommand;
import io.github.moulberry.notenoughupdates.commands.dungeon.DhCommand;
import io.github.moulberry.notenoughupdates.commands.dungeon.DnCommand;
import io.github.moulberry.notenoughupdates.commands.dungeon.JoinDungeonCommand;
import io.github.moulberry.notenoughupdates.commands.dungeon.MapCommand;
import io.github.moulberry.notenoughupdates.commands.help.FeaturesCommand;
import io.github.moulberry.notenoughupdates.commands.help.HelpCommand;
import io.github.moulberry.notenoughupdates.commands.help.LinksCommand;
import io.github.moulberry.notenoughupdates.commands.help.SettingsCommand;
import io.github.moulberry.notenoughupdates.commands.help.StorageViewerWhyCommand;
import io.github.moulberry.notenoughupdates.commands.misc.AhCommand;
import io.github.moulberry.notenoughupdates.commands.misc.CalculatorCommand;
import io.github.moulberry.notenoughupdates.commands.misc.CalendarCommand;
import io.github.moulberry.notenoughupdates.commands.misc.CosmeticsCommand;
import io.github.moulberry.notenoughupdates.commands.misc.CustomizeCommand;
import io.github.moulberry.notenoughupdates.commands.misc.PronounsCommand;
import io.github.moulberry.notenoughupdates.commands.profile.CataCommand;
import io.github.moulberry.notenoughupdates.commands.profile.PeekCommand;
import io.github.moulberry.notenoughupdates.commands.profile.PvCommand;
import io.github.moulberry.notenoughupdates.commands.profile.ViewProfileCommand;
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
		ClientCommandHandler.instance.registerCommand(new DiagCommand());
		ClientCommandHandler.instance.registerCommand(new ReloadRepoCommand());
		ClientCommandHandler.instance.registerCommand(new ResetRepoCommand());
		ClientCommandHandler.instance.registerCommand(new EnableStorageCommand());

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
		ClientCommandHandler.instance.registerCommand(new CalculatorCommand());
		ClientCommandHandler.instance.registerCommand(new CalendarCommand());
		//ClientCommandHandler.instance.registerCommand(new UpdateCommand(NotEnoughUpdates.INSTANCE));
		ClientCommandHandler.instance.registerCommand(new PronounsCommand());

		// Fairy Soul Commands
		ClientCommandHandler.instance.registerCommand(new FairySouls.FairySoulsCommand());
	}
}
