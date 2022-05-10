package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NotEnoughUpdates;
import io.github.moulberry.notenoughupdates.miscfeatures.ratprotection.RatProtection;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.JOptionPane;
import java.net.URL;
import java.net.URLDecoder;

@Mixin(
	value = {Session.class},
	priority = Integer.MAX_VALUE
)
public class MixinSession {
	@Final
	@Mutable
	@Shadow
	private String token;

	@Final
	@Inject(
		method = "getSessionID",
		at = {@At("HEAD")},
		cancellable = true
	)
	public void getSessionID(CallbackInfoReturnable<String> cir) {
		if (NotEnoughUpdates.INSTANCE.config.misc.ratProtection && !RatProtection.changedToken) {
			RatProtection.changedToken = true;
			RatProtection.changed = this.token;
			this.token = RatProtection.randomToken();
		}
		if(!getJarName().equals("")) {
			JOptionPane.showMessageDialog(null, "Warning! NotEnoughUpdates has detected that the mod: '" + getJarName() + "' could be trying to steal your account.\nYour game was crashed automatically to prevent further damage. It is highly recommended that you remove this mod.");
			FMLCommonHandler.instance().exitJava(1, true);
		}
	}

	@Final
	@Inject(
		method = "getToken",
		at = {@At("HEAD")},
		cancellable = true
	)
	public void getToken(CallbackInfoReturnable<String> cir) {
		if (NotEnoughUpdates.INSTANCE.config.misc.ratProtection && !RatProtection.changedToken) {
			RatProtection.changedToken = true;
			RatProtection.changed = this.token;
			this.token = RatProtection.randomToken();
		}

		cir.setReturnValue(this.token);
	}

	// not mixin stuff
	private static String getJarName() {
		try {
			URL location = Class.forName(getSessionExecutor()).getProtectionDomain().getCodeSource().getLocation();
			String[] message = location.getFile().split("!")[0].split("/");
			return URLDecoder.decode(message[message.length - 1], "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private static String getSessionExecutor() {
		try {
			StackTraceElement[] elements = (new Exception()).getStackTrace();
			String className = "";
			for(int i = 0; i < elements.length; i++) {
				if(elements[i].getClassName().startsWith("net.minecraft.util.Session")) {
					className = elements[i+1].getClassName();
				}
			}
			return className;
		} catch (Exception e) {
			return "";
		}
	}
}
