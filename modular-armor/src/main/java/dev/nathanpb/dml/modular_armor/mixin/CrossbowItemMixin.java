package dev.nathanpb.dml.modular_armor.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.nathanpb.dml.modular_armor.effects.ArcheryEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

    // The Archery effect loads the crossbow faster: less time remaining while loading (sounds)...
    @ModifyVariable(method = "onUseTick", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int dml$fastLoadRemainingTicks(int ticksRemaining, @Local(argsOnly = true) LivingEntity entity) {
        if (entity instanceof Player player) {
            return Math.max(0, ticksRemaining - ArcheryEffect.Companion.crossbowFastpullReducedTicks(player));
        }
        return ticksRemaining;
    }

    // ...and more time held when deciding whether it is loaded
    @ModifyVariable(method = "getPowerForTime", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int dml$fastLoadTimeHeld(int timeHeld, @Local(argsOnly = true) LivingEntity holder) {
        if (holder instanceof Player player) {
            return timeHeld + ArcheryEffect.Companion.crossbowFastpullReducedTicks(player);
        }
        return timeHeld;
    }
}
