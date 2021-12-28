package io.github.moulberry.notenoughupdates.mixins;

import io.github.moulberry.notenoughupdates.NEUEventListener;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {
    @Inject(method = {"ungrabMouseCursor"}, at = {@At("HEAD")}, cancellable = true)
    public void ungrabMouseCursor(final CallbackInfo ci) {
        if (System.currentTimeMillis() - NEUEventListener.lastGuiClosed < 150L) {
            ci.cancel();
            Mouse.setGrabbed(false);
        }
    }
}
