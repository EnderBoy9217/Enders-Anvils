package com.enderboy9217.anvils.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.enderboy9217.anvils.interfaces.AnvilScreenInterface;

@Mixin(Screen.class)
public class ScreenMixin implements AnvilScreenInterface {
    @Shadow
    protected TextRenderer textRenderer;

    public TextRenderer enders_Anvils$getTextRenderer() {
        return textRenderer;
    }
}
