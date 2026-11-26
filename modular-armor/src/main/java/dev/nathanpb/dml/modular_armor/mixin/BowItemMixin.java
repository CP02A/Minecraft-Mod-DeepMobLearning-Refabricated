package dev.nathanpb.dml.modular_armor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.nathanpb.dml.modular_armor.effects.ArcheryEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    // The Archery effect pulls the bow faster: the power of the shot for the time it was held
    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"))
    private float dml$fastPull(int timeHeld, Operation<Float> original, @Local(argsOnly = true) LivingEntity user) {
        if (user instanceof Player player) {
            float modifier = ArcheryEffect.Companion.bowFastpullLevels(player) + 1;
            if (modifier > 1) {
                float f = (float) timeHeld / (20F / modifier);
                f = (f * f + f * 2.0F) / 3.0F;
                return Math.min(f, 1.0F);
            }
        }

        return original.call(timeHeld);
    }
}
