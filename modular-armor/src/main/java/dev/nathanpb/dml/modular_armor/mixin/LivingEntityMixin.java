package dev.nathanpb.dml.modular_armor.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.nathanpb.dml.modular_armor.ItemModularGlitchArmor;
import dev.nathanpb.dml.modular_armor.effects.TargetCancellationEffect;
import dev.nathanpb.dml.modular_armor.effects.UndyingEffect;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    // Undying: works like a totem of undying when the player has none
    @Inject(at = @At("RETURN"), method = "checkTotemDeathProtection", cancellable = true)
    private void dml$undyingEffect(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && (Object) this instanceof Player player) {
            if (UndyingEffect.Companion.trigger(player)) {
                cir.setReturnValue(true);
            }
        }
    }

    // Zombie/Skeleton/Shulker Friendly: those mobs stop targeting the player
    @Inject(at = @At("RETURN"), method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", cancellable = true)
    private void dml$targetCancellation(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Mob mob && cir.getReturnValue()) {
            InteractionResult result = TargetCancellationEffect.Companion.attemptToCancel(mob, target);
            if (result.equals(InteractionResult.FAIL)) {
                cir.setReturnValue(false);
            }
        }
    }

    // Glitch armor is not capped at 80% damage reduction like vanilla armor
    @WrapOperation(
            method = "getDamageAfterArmorAbsorb",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F")
    )
    private float dml$glitchArmorUncapProtection(LivingEntity entity, float damage, DamageSource source, float armor, float armorToughness, Operation<Float> original) {
        boolean shouldUncap = EquipmentSlotGroup.ARMOR.slots().stream()
            .anyMatch(slot -> entity.getItemBySlot(slot).getItem() instanceof ItemModularGlitchArmor);

        if (shouldUncap) {
            float f = 2.0F + armorToughness / 4.0F;
            float g = Math.max(armor - damage / f, armor * 0.2F);
            return damage * (1.0F - g / 25.0F);
        } else {
            return original.call(entity, damage, source, armor, armorToughness);
        }
    }
}
