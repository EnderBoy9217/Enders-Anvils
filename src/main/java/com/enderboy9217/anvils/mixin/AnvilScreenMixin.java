package com.enderboy9217.anvils.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.enderboy9217.anvils.interfaces.AnvilScreenInterface;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

    @Inject(
            method = "drawForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I",
                    shift = At.Shift.AFTER
            )
    )
    private void injectAmethystShardRendering(DrawContext context, int mouseX, int mouseY, CallbackInfo ci, @Local Text text, @Local(ordinal = 4) int k, @Local(ordinal = 5) int l) {
        if (text != null) {
            // Render Amethyst Shard item icon after the text
            ItemStack amethystShard = new ItemStack(Items.AMETHYST_SHARD);
            int iconX = k + ((AnvilScreenInterface)(Object)this).enders_Anvils$getTextRenderer().getWidth(text) - 10; // Position after text
            int iconY = l - 2 ; // Align vertically with text
            float scale = 0.75f; //

            // Save current matrix state
            context.getMatrices().push();
            // Apply scaling transformation
            context.getMatrices().scale(scale, scale, 1.0f);
            // Adjust coordinates for scaled rendering
            context.drawItem(amethystShard, (int)(iconX / scale), (int)(iconY / scale));

            context.drawItemInSlot(((AnvilScreenInterface)(Object)this).enders_Anvils$getTextRenderer(), amethystShard, iconX, iconY, null);

            context.getMatrices().pop();
        }
    }

    @ModifyArg(
            method = "drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;",
                    ordinal = 0
            ),
            index = 0
    )
    private String modifyRepairCostText(String translationKey) {
        if (translationKey.equals("container.repair.cost")) {
            // Return a custom translation key or modify directly
            return "enderboy9217.container.repair.cost"; // Assumes a custom translation key in your resource pack
        }
        return translationKey;
    }
}
