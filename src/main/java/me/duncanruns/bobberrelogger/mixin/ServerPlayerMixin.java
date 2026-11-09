package me.duncanruns.bobberrelogger.mixin;

import me.duncanruns.bobberrelogger.BobberRelogger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Unique
    private static final String BOBBER_KEY = "bobber";
    @Unique
    private static final String DIM_KEY = "bobdim";
    @Unique
    private static final String FISHING_BOBBER_ID = "minecraft:fishing_bobber";

    @SuppressWarnings("all")
    public ServerPlayerMixin() {
        super(null, null);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveFishingBobbers(ValueOutput output, CallbackInfo info) {
        if (this.fishing == null || this.fishing.isRemoved()) return;

        ValueOutput bobberOutput = output.child(BOBBER_KEY);
        bobberOutput.putString("id", FISHING_BOBBER_ID);
        this.fishing.saveWithoutId(bobberOutput);
        bobberOutput.store(DIM_KEY, Level.RESOURCE_KEY_CODEC, this.fishing.level().dimension());
    }

    @Inject(method = "loadAndSpawnEnderPearls", at = @At("TAIL"))
    private void loadFishingBobbers(ValueInput playerInput, CallbackInfo info) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        playerInput.child(BOBBER_KEY).ifPresent(bobberInput -> {
            Optional<ResourceKey<Level>> bobberLevelKey = bobberInput.read(DIM_KEY, Level.RESOURCE_KEY_CODEC);
            if (bobberLevelKey.isEmpty()) {
                BobberRelogger.LOGGER.warn("Bobber has no dimension, cannot load!");
                return;
            }

            ServerLevel bobberLevel = player.level().getServer().getLevel(bobberLevelKey.get());
            if (bobberLevel == null) {
                BobberRelogger.LOGGER.warn("Bobber has dimension {}, which does not exist, cannot load!", bobberLevelKey.get());
                return;
            }

            Entity entity = EntityType.loadEntityRecursive(
                    bobberInput,
                    bobberLevel,
                    EntitySpawnReason.LOAD,
                    loadedEntity -> !bobberLevel.addWithUUID(loadedEntity) ? null : loadedEntity
            );
            if (entity instanceof FishingHook bobber) {
                bobber.setOwner(player);
            } else if (entity == null) {
                BobberRelogger.LOGGER.warn("Failed to load stored bobber!");
            } else {
                entity.discard();
                BobberRelogger.LOGGER.warn("Non-bobber entity stored in player bobber data, discarding!");
            }
        });
    }
}
