package dev.nathanpb.dml.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.nathanpb.dml.event.VanillaEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
public class HungerManagerMixin {

    @Shadow
    private int foodLevel;

    // foodLevel = Math.max(foodLevel - 1, 0): the hunger lost from exhaustion, which the event can change
    @WrapOperation(
        method = "tick",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0)
    )
    private int dml$modifyHungerLoss(int a, int b, Operation<Integer> original, @Local(argsOnly = true) ServerPlayer player) {
        int eventResult = VanillaEvents.INSTANCE.getPlayerTakeHungerEvent()
            .invoker()
            .invoke(player, foodLevel - a);

        return original.call(foodLevel - eventResult, b);
    }
}
