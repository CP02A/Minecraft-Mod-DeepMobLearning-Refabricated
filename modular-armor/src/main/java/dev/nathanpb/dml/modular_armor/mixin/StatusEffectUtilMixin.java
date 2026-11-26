package dev.nathanpb.dml.modular_armor.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.nathanpb.dml.modular_armor.EntityStatusEffectsKt;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** Underwater Haste works like Haste while the eyes are in water. */
@Mixin(MobEffectUtil.class)
public class StatusEffectUtilMixin {

    @Unique
    private static boolean dml$hasUnderwaterHaste(LivingEntity entity) {
        return entity.hasEffect(EntityStatusEffectsKt.getUNDERWATER_HASTE_EFFECT()) && entity.isEyeInFluid(FluidTags.WATER);
    }

    @ModifyReturnValue(at = @At("RETURN"), method = "hasDigSpeed")
    private static boolean dml$underwaterHasteHasDigSpeed(boolean original, LivingEntity entity) {
        return original || dml$hasUnderwaterHaste(entity);
    }

    // max(haste, conduit) + underwater haste
    @ModifyReturnValue(at = @At("RETURN"), method = "getDigSpeedAmplification")
    private static int dml$underwaterHasteAmplification(int original, LivingEntity entity) {
        if (dml$hasUnderwaterHaste(entity)) {
            MobEffectInstance effect = entity.getEffect(EntityStatusEffectsKt.getUNDERWATER_HASTE_EFFECT());
            return original + (effect != null ? effect.getAmplifier() : 0);
        }
        return original;
    }
}
