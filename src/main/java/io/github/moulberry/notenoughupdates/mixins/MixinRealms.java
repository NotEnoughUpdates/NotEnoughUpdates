package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.miscfeatures.ratprotection.RatProtection;
import net.minecraft.client.Minecraft;
import net.minecraft.realms.Realms;
import net.minecraft.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(
	value = Realms.class,
	priority = Integer.MAX_VALUE
)
public class MixinRealms {

	/**
	 * @author 2stinkysocks
	 * @reason To prevent rat protection from triggering on realms load, has the same functionality as before
	 */
	@Overwrite
	public static String sessionId() {
		Session session = Minecraft.getMinecraft().getSession();
		return session == null ? null : RatProtection.changed;
	}

}
