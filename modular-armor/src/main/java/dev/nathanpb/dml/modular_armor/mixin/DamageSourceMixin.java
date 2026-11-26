package dev.nathanpb.dml.modular_armor.mixin;

import dev.nathanpb.dml.modular_armor.core.ModularEffect;
import dev.nathanpb.dml.modular_armor.core.ModularEffectContext;
import dev.nathanpb.dml.modular_armor.core.ModularEffectRegistry;
import dev.nathanpb.dml.modular_armor.effects.PlentyEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DamageSource.class)
public class DamageSourceMixin {

    @Inject(at = @At("HEAD"), method = "getLocalizedDeathMessage", cancellable = true)
    private void dml$starvedWithPlenty(LivingEntity entity, CallbackInfoReturnable<Component> cir) {
        DamageSource damageSource = ((DamageSource) (Object) this);
        if (entity instanceof Player player && damageSource.is(DamageTypeTags.BYPASSES_EFFECTS)) { // this is the best way to check for starving atm, but could break on a later version
            Optional<ModularEffect<?>> effectOpt = ModularEffectRegistry.Companion
                .getINSTANCE()
                .getAll()
                .stream()
                .filter((it) -> it instanceof PlentyEffect)
                .findFirst();

            if (effectOpt.isPresent()) {
                ModularEffect<?> effect = effectOpt.get();
                boolean any = ModularEffectContext.Companion.from(player)
                    .stream()
                    .anyMatch((context) -> effect.getCategory() == context.getDataModel().getCategory() && effect.acceptTier(context.getTier()));
                if (any) {
                    cir.setReturnValue(Component.translatable(
                        "death.dml-refabricated.starvedWithPlenty",
                        entity.getDisplayName()
                    ));
                }
            }
        }
    }
}
