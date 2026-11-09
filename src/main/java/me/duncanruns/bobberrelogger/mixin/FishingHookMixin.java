package me.duncanruns.bobberrelogger.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {
    @SuppressWarnings("all")
    protected FishingHookMixin() {
        super(null, null);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveOwner(ValueOutput output, CallbackInfo info) {
        super.addAdditionalSaveData(output);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void loadOwner(ValueInput input, CallbackInfo info) {
        super.readAdditionalSaveData(input);
    }
}
