package dev.nathanpb.dml.event

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.monster.Enderman
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.ExplosionDamageCalculator

object VanillaEvents {
    data class LivingEntityDamageContext(val entity: LivingEntity, val source: DamageSource, val damage: Float)

    val LivingEntityDamageEvent = event<(LivingEntityDamageContext)->LivingEntityDamageContext?> { listeners ->
        { context: LivingEntityDamageContext ->
            listeners.fold(context) { acc, listener ->
                listener(acc) ?: acc
            }
        }
    }

    // Doing it just with Player to prevent server overload
    val PlayerEntityTickEvent = event<(Player)->Unit> { listeners ->
        { entity ->
            listeners.forEach {
                it(entity)
            }
        }
    }

    val PlayerTakeHungerEvent = event<(Player, Int)->Int> { listeners ->
        { player, amount ->
            listeners.toList().fold(amount) { acc, function ->
                if (acc > 0) {
                    function(player, acc)
                } else acc
            }
        }
    }

    val LivingEntityEatEvent = event<(LivingEntity, ItemStack)->Unit> { listeners ->
        { entity, stack ->
            listeners.forEach {
                it(entity, stack)
            }
        }
    }

    val BowShotEvent = event<(LivingEntity, ItemStack)->Unit> { listeners ->
        { entity, stack ->
            listeners.forEach {
                it(entity, stack)
            }
        }
    }

    val CrossbowReloadedEvent = event<(LivingEntity, ItemStack)->Unit> { listeners ->
        { entity, stack ->
            listeners.forEach {
                it(entity, stack)
            }
        }
    }

    val EndermanTeleportEvent = event<(Enderman, Vec3)-> InteractionResult> { listeners ->
        { entity, pos ->
            val shouldFail = listeners.any { it.invoke(entity, pos) == InteractionResult.FAIL }
            if (shouldFail) InteractionResult.FAIL else InteractionResult.PASS
        }
    }

    val WorldExplosionEvent = event<(Level, Entity?, DamageSource?, behavior: ExplosionDamageCalculator?, pos: BlockPos, power: Float, createFire: Boolean, Level.ExplosionInteraction)-> InteractionResult> { listeners ->
        { world, entity, damageSource, behavior, pos, power, createFire, destructionType ->
            val shouldFail = listeners.any {
                it.invoke(world, entity, damageSource, behavior, pos, power, createFire, destructionType) == InteractionResult.FAIL
            }

            if (shouldFail) InteractionResult.FAIL else InteractionResult.PASS
        }
    }
}