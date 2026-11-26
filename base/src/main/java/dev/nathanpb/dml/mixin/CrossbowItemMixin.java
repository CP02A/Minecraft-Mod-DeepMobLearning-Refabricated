package dev.nathanpb.dml.mixin;

import dev.nathanpb.dml.event.VanillaEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(at = @At("RETURN"), method = "tryLoadProjectiles")
    private static void dml$onLoadProjectiles(LivingEntity shooter, ItemStack crossbow, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            VanillaEvents.INSTANCE.getCrossbowReloadedEvent()
                .invoker()
                .invoke(shooter, crossbow);
        }
    }
}
