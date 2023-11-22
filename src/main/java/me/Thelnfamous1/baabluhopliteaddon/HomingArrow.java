package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public class HomingArrow extends Arrow implements PhysicsCheck{
    private static final EntityDataAccessor<OptionalInt> DATA_HOMING_TARGET_ID = SynchedEntityData.defineId(HomingArrow.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Float> DATA_SHOT_VELOCITY = SynchedEntityData.defineId(HomingArrow.class, EntityDataSerializers.FLOAT);
    private Entity homingTarget;

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
        this.entityData.define(DATA_SHOT_VELOCITY, 0.0F);
    }

    @Override
    public void tick() {
        if(this.hasHomingTarget()){
            Entity homingTarget = this.getHomingTarget();
            if (homingTarget != null && !this.inGround) {
                float shotVelocity = this.getShotVelocity();
                //this.setNoPhysics(true);
                Vec3 positionDelta = homingTarget.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + positionDelta.y * 0.015D * (double)shotVelocity, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                double stepScale = 0.05D * (double)shotVelocity;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(positionDelta.normalize().scale(stepScale)));
                //this.setNoPhysics(false);


                Vec3 deltaMovement = this.getDeltaMovement();
                double xNew = this.getX() + deltaMovement.x;
                double yNew = this.getY() + deltaMovement.y;
                double zNew = this.getZ() + deltaMovement.z;
                this.level().addParticle(ParticleTypes.PORTAL, xNew - deltaMovement.x * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, yNew - deltaMovement.y * 0.25D - 0.5D, zNew - deltaMovement.z * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, deltaMovement.x, deltaMovement.y, deltaMovement.z);
            }
        }

        super.tick();
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        if(!this.level().isClientSide()) this.setHomingTarget(null);
    }

    public void setHomingTarget(@Nullable Entity entity) {
        this.entityData.set(DATA_HOMING_TARGET_ID, entity == null ? OptionalInt.empty() : OptionalInt.of(entity.getId()));
        //BaabluHopliteAddon.LOGGER.info("Set homing target for {} to {}", this, entity);
    }

    @Nullable
    public Entity getHomingTarget(){
        if (this.homingTarget == null) {
            this.entityData.get(DATA_HOMING_TARGET_ID).ifPresent((id) -> this.homingTarget = this.level().getEntity(id));
        }
        return this.homingTarget;
    }

    private boolean hasHomingTarget() {
        return this.entityData.get(DATA_HOMING_TARGET_ID).isPresent();
    }

    @Override
    public void playerTouch(Player pEntity) {
        if (!this.hasHomingTarget() || this.inGround) {
            super.playerTouch(pEntity);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if(this.hasHomingTarget() && entity == this.getOwner()){
            return false;
        }

        return super.canHitEntity(entity);
    }

    @Override
    public boolean canBypassGravity() {
        return this.hasHomingTarget();
    }

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        super.shoot(pX, pY, pZ, pVelocity, pInaccuracy);
        if(!this.level().isClientSide) this.setShotVelocity(pVelocity);
    }

    private float getShotVelocity(){
        return this.entityData.get(DATA_SHOT_VELOCITY);
    }

    private void setShotVelocity(float pVelocity) {
        this.entityData.set(DATA_SHOT_VELOCITY, pVelocity);
    }
}
