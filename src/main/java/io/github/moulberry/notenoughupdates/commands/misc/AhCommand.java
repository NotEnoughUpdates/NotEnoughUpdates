package io.github.moulberry.notenoughupdates.commands.misc;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.auction.CustomAHGui;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;

public class AhCommand extends ClientCommandBase {

	public AhCommand() {
		super("neuah");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (!NotEnoughUpdates.INSTANCE.hasSkyblockScoreboard()) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"You must be on Skyblock to use this feature."));
		} else if (NotEnoughUpdates.INSTANCE.config.apiKey.apiKey == null ||
			NotEnoughUpdates.INSTANCE.config.apiKey.apiKey.trim().isEmpty()) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				"Can't open NeuAH, apikey is not set. Run /api new and put the result in settings."));
		} else {
			NotEnoughUpdates.INSTANCE.openGui = new CustomAHGui();
			NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.lastOpen = System.currentTimeMillis();
			NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.clearSearch();
			NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.updateSearch();

			if (args.length > 0)
				NotEnoughUpdates.INSTANCE.manager.auctionManager.customAH.setSearch(StringUtils.join(args, " "));
		}
	}
}
