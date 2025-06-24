package com.enderboy9217.anvils.mixin;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow
    private int repairItemUsage;

    @Final
    @Shadow
    private Property levelCost;

    public AnvilScreenHandlerMixin(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(null,syncId, inventory, context);
    }

    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    private void modifyCanTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        int levelCost = ((AnvilScreenHandler)(Object)this).getLevelCost();
        int shardCount = countAmethystShards(player);
        cir.setReturnValue((player.getAbilities().creativeMode || shardCount >= levelCost) && levelCost > 0);
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"), cancellable = true)
    private void modifyOnTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!player.getAbilities().creativeMode) {
            int levelCost = ((AnvilScreenHandler)(Object)this).getLevelCost();
            consumeAmethystShards(player, levelCost);
        }

        this.input.setStack(0, ItemStack.EMPTY);
        if (this.repairItemUsage > 0) {
            ItemStack itemStack = this.input.getStack(1);
            if (!itemStack.isEmpty() && itemStack.getCount() > this.repairItemUsage) {
                itemStack.decrement(this.repairItemUsage);
                this.input.setStack(1, itemStack);
            } else {
                this.input.setStack(1, ItemStack.EMPTY);
            }
        } else {
            this.input.setStack(1, ItemStack.EMPTY);
        }

        this.levelCost.set(0);
        this.context.run((world, pos) -> {
            BlockState blockState = world.getBlockState(pos);
            if (!player.getAbilities().creativeMode && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
                BlockState blockState2 = AnvilBlock.getLandingState(blockState);
                if (blockState2 == null) {
                    world.removeBlock(pos, false);
                    world.syncWorldEvent(WorldEvents.ANVIL_DESTROYED, pos, 0);
                } else {
                    world.setBlockState(pos, blockState2, Block.NOTIFY_LISTENERS);
                    world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
                }
            } else {
                world.syncWorldEvent(WorldEvents.ANVIL_USED, pos, 0);
            }
        });

        ci.cancel();
    }

    @Inject(method="updateResult",at=@At("TAIL"))
    private void overrideCost(CallbackInfo ci) {
        ItemStack itemStack = this.input.getStack(0);
        int levelcost = this.levelCost.get();
        this.levelCost.set(
                Math.max(Math.min(levelcost-1,20),1) // All costs must be greater than 1, Cannot be 40 or greater
        );
    }


    @Unique
    private int countAmethystShards(PlayerEntity player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() == Items.AMETHYST_SHARD) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Unique
    private void consumeAmethystShards(PlayerEntity player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().main.size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().main.get(i);
            if (stack.getItem() == Items.AMETHYST_SHARD) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.decrement(toRemove);
                remaining -= toRemove;
                if (stack.isEmpty()) {
                    player.getInventory().main.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
