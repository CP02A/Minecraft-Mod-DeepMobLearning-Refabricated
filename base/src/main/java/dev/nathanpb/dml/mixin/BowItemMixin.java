package dev.nathanpb.dml.mixin;

import dev.nathanpb.dml.event.VanillaEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowItemMixin {

    // The bow fires: the arrows are about to be shot (server side only)
    @Inject(
        method = "releaseUsing",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V")
    )
    private void dml$onShoot(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfoReturnable<Boolean> cir) {
        VanillaEvents.INSTANCE.getBowShotEvent()
            .invoker()
            .invoke(user, stack);
    }

}
