package dev.mpthlee.allthetweaks.tweak.broom.mixin;

import dev.mpthlee.allthetweaks.tweak.broom.BroomFlightLease;
import dev.mpthlee.allthetweaks.tweak.broom.BroomFlightTweak;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.cyclops.evilcraft.entity.item.EntityBroom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EntityBroom.class)
public abstract class BroomFlightMixin implements BroomFlightLease.Owner {
    @Unique private BroomFlightLease att$flight;

    @Override
    public boolean hasFlightLease(ServerPlayer player) {
        return att$flight != null && att$flight.contains(player);
    }

    @Inject(method = "startAllowFlying", at = @At("HEAD"), remap = false)
    private void att$rememberFlight(LivingEntity entity, CallbackInfo ci) {
        if (BroomFlightTweak.enabled() && entity instanceof ServerPlayer player) {
            if (att$flight == null) att$flight = new BroomFlightLease();
            att$flight.acquire(player);
        }
    }

    @Inject(method = "stopAllowFlying", at = @At("HEAD"), cancellable = true, remap = false)
    private void att$restoreInsteadOfUnconditionallyRevoking(LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player && att$flight != null && att$flight.release(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "removePassenger", at = @At("TAIL"))
    private void att$cleanupActualPassenger(Entity passenger, CallbackInfo ci) {
        // EvilCraft's lastMounted is populated by tick(), so it can be null or refer to another passenger.
        // Release an existing lease even if the config was disabled after the player mounted.
        if (passenger instanceof ServerPlayer player && att$flight != null) att$flight.release(player);
    }
}
