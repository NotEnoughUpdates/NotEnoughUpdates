package io.github.moulberry.notenoughupdates.commands.dev;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.commands.ClientCommandBase;
import io.github.moulberry.notenoughupdates.core.config.GuiPositionEditor;
import io.github.moulberry.notenoughupdates.miscfeatures.FancyPortals;
import io.github.moulberry.notenoughupdates.miscfeatures.FishingHelper;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.CustomBiomes;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.LocationChangeEvent;
import io.github.moulberry.notenoughupdates.miscfeatures.customblockzones.SpecialBlockZone;
import io.github.moulberry.notenoughupdates.miscgui.GuiPriceGraph;
import io.github.moulberry.notenoughupdates.util.SBInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DevTestCommand extends ClientCommandBase {

	private static final List<String> DEV_TESTERS =
		Arrays.asList("moulberry", "lucycoconut", "ironm00n", "ariyio", "throwpo", "lrg89", "dediamondpro", "craftyoldminer");

	private static final String[] DEV_FAIL_STRINGS = {
		"No.",
		"I said no.",
		"You aren't allowed to use this.",
		"Are you sure you want to use this? Type 'Yes' in chat.",
		"Are you sure you want to use this? Type 'Yes' in chat.",
		"Lmao you thought",
		"Ok please stop",
		"What do you want from me?",
		"This command almost certainly does nothing useful for you",
		"Ok, this is the last message, after this it will repeat",
		"No.",
		"I said no.",
		"Dammit. I thought that would work. Uhh...",
		"\u00a7dFrom \u00a7c[ADMIN] Minikloon\u00a77: If you use that command again, I'll have to ban you",
		"",
		"Ok, this is actually the last message, use the command again and you'll crash I promise"
	};

	private int devFailIndex = 0;
	private final ScheduledExecutorService devES = Executors.newSingleThreadScheduledExecutor();

	public DevTestCommand() {
		super("neudevtest");
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (!DEV_TESTERS.contains(Minecraft.getMinecraft().thePlayer.getName().toLowerCase())) {
			if (devFailIndex >= DEV_FAIL_STRINGS.length) {
				throw new Error("L") {
					@Override
					public void printStackTrace() {
						throw new Error("L");
					}
				};
			}
			if (devFailIndex == DEV_FAIL_STRINGS.length - 2) {
				devFailIndex++;

				ChatComponentText component = new ChatComponentText("\u00a7cYou are permanently banned from this server!");
				component.appendText("\n");
				component.appendText("\n\u00a77Reason: \u00a7rI told you not to run the command - Moulberry");
				component.appendText("\n\u00a77Find out more: \u00a7b\u00a7nhttps://www.hypixel.net/appeal");
				component.appendText("\n");
				component.appendText("\n\u00a77Ban ID: \u00a7r#49871982");
				component.appendText("\n\u00a77Sharing your Ban ID may affect the processing of your appeal!");
				Minecraft.getMinecraft().getNetHandler().getNetworkManager().closeChannel(component);
				return;
			}
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED +
				DEV_FAIL_STRINGS[devFailIndex++]));
			return;
		}
            /*if(args.length == 1) {
                DupePOC.doDupe(args[0]);
                return;
            }*/
		if (args.length >= 1 && args[0].equalsIgnoreCase("profileinfo")) {
			String currentProfile = SBInfo.getInstance().currentProfile;
			SBInfo.Gamemode gamemode = SBInfo.getInstance().getGamemodeForProfile(currentProfile);
			sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD +
				"You are on Profile " +
				currentProfile +
				" with the mode " +
				gamemode));
		}
		if (args.length >= 1 && args[0].equalsIgnoreCase("pricetest")) {
			if (args.length == 1) {
				NotEnoughUpdates.INSTANCE.manager.auctionManager.updateBazaar();
			} else {
				NotEnoughUpdates.INSTANCE.openGui = new GuiPriceGraph(args[1]);
			}
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("zone")) {
			BlockPos target = Minecraft.getMinecraft().objectMouseOver.getBlockPos();
			if (target == null) target = Minecraft.getMinecraft().thePlayer.getPosition();
			SpecialBlockZone zone = CustomBiomes.INSTANCE.getSpecialZone(target);
			Arrays.asList(
				new ChatComponentText("Showing Zone Info for: " + target),
				new ChatComponentText("Zone: " + (zone != null ? zone.name() : "null")),
				new ChatComponentText("Location: " + SBInfo.getInstance().getLocation()),
				new ChatComponentText("Biome: " + CustomBiomes.INSTANCE.getCustomBiome(target))
			).forEach(Minecraft.getMinecraft().thePlayer::addChatMessage);
			MinecraftForge.EVENT_BUS.post(new LocationChangeEvent(SBInfo.getInstance().getLocation(), SBInfo
				.getInstance()
				.getLocation()));
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("positiontest")) {
			NotEnoughUpdates.INSTANCE.openGui = new GuiPositionEditor();
			return;
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("pt")) {
			EnumParticleTypes t = EnumParticleTypes.valueOf(args[1]);
			FishingHelper.type = t;
			return;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("dev")) {
			NotEnoughUpdates.INSTANCE.config.hidden.dev = true;
			return;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("saveconfig")) {
			NotEnoughUpdates.INSTANCE.saveConfig();
			return;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("searchmode")) {
			NotEnoughUpdates.INSTANCE.config.hidden.firstTimeSearchFocus = true;
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA +
				"I would never search"));
			return;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("center")) {
			double x = Math.floor(Minecraft.getMinecraft().thePlayer.posX) + 0.5f;
			double z = Math.floor(Minecraft.getMinecraft().thePlayer.posZ) + 0.5f;
			Minecraft.getMinecraft().thePlayer.setPosition(x, Minecraft.getMinecraft().thePlayer.posY, z);
			return;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("pansc")) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN +
				"Taking panorama screenshot"));

			AtomicInteger perspective = new AtomicInteger(0);
			FancyPortals.perspectiveId = 0;

			EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
			p.prevRotationYaw = p.rotationYaw = 0;
			p.prevRotationPitch = p.rotationPitch = 90;
			devES.schedule(new Runnable() {
				@Override
				public void run() {
					Minecraft.getMinecraft().addScheduledTask(() -> {
						ScreenShotHelper.saveScreenshot(new File("C:/Users/James/Desktop/"), "pansc-" + perspective.get() + ".png",
							Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight,
							Minecraft.getMinecraft().getFramebuffer()
						);
					});
					if (perspective.incrementAndGet() >= 6) {
						FancyPortals.perspectiveId = -1;
						return;
					}
					devES.schedule(() -> {
						FancyPortals.perspectiveId = perspective.get();
						if (FancyPortals.perspectiveId == 5) {
							p.prevRotationYaw = p.rotationYaw = 0;
							p.prevRotationPitch = p.rotationPitch = -90;
						} else if (FancyPortals.perspectiveId >= 1 && FancyPortals.perspectiveId <= 4) {
							float yaw = 90 * FancyPortals.perspectiveId - 180;
							if (yaw > 180) yaw -= 360;
							p.prevRotationYaw = p.rotationYaw = yaw;
							p.prevRotationPitch = p.rotationPitch = 0;
						}
						devES.schedule(this, 3000L, TimeUnit.MILLISECONDS);
					}, 100L, TimeUnit.MILLISECONDS);
				}
			}, 3000L, TimeUnit.MILLISECONDS);

			return;
		}

            /* if(args.length == 1 && args[0].equalsIgnoreCase("update")) {
                NEUEventListener.displayUpdateMessageIfOutOfDate();
            } */

		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN +
			"Executing dubious code"));
            /*Minecraft.getMinecraft().thePlayer.rotationYaw = 0;
            Minecraft.getMinecraft().thePlayer.rotationPitch = 0;
            Minecraft.getMinecraft().thePlayer.setPosition(
                    Math.floor(Minecraft.getMinecraft().thePlayer.posX) + Float.parseFloat(args[0]),
                    Minecraft.getMinecraft().thePlayer.posY,
                    Minecraft.getMinecraft().thePlayer.posZ);*/
		//Hot reload me yay!
	}
}
