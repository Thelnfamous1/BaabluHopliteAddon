package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public class HomingArrow extends Arrow {
    private static final EntityDataAccessor<OptionalInt> DATA_HOMING_TARGET_ID = SynchedEntityData.defineId(HomingArrow.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private Entity attachedToEntity;

    public HomingArrow(EntityType<? extends HomingArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public HomingArrow(Level pLevel, double pX, double pY, double pZ) {
        this(BaabluHopliteAddon.HOMING_ARROW.get(), pLevel);
        this.setPos(pX, pY, pZ);
    }

    public HomingArrow(Level pLevel, LivingEntity pShooter) {
        this(pLevel, pShooter.getX(), pShooter.getEyeY() - (double)0.1F, pShooter.getZ());
        this.setOwner(pShooter);
        if (pShooter instanceof Player) {
            this.pickup = AbstractArrow.Pickup.ALLOWED;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HOMING_TARGET_ID, OptionalInt.empty());
    }

    @Override
    public void tick() {
        if(this.hasHomingTarget()){
            Entity homingTarget = this.getHomingTarget();
            if (homingTarget != null && !this.inGround) {
                int loyaltyLevel = 5;
                //this.setNoPhysics(true);
                Vec3 positionDelta = homingTarget.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + positionDelta.y * 0.015 * (double)loyaltyLevel, this.getZ());
                if (this.level.isClientSide) {
                    this.yOld = this.getY();
                }

                double stepScale = 0.05 * (double)loyaltyLevel;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(positionDelta.normalize().scale(stepScale)));
                //this.setNoPhysics(false);
            }
        }

        super.tick();
    }

    public void setHomingTarget(Entity entity) {
        this.entityData.set(DATA_HOMING_TARGET_ID, OptionalInt.of(entity.getId()));
        BaabluHopliteAddon.LOGGER.info("Set homing target for {} to {}", this, entity);
    }

    @Nullable
    public Entity getHomingTarget(){
        if (this.attachedToEntity == null) {
            this.entityData.get(DATA_HOMING_TARGET_ID).ifPresent((id) -> this.attachedToEntity = this.level.getEntity(id));
        }
        return this.attachedToEntity;
    }

    private boolean hasHomingTarget() {
        return this.entityData.get(DATA_HOMING_TARGET_ID).isPresent();
    }
}
