package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownPufferfish extends ThrowableItemProjectile{

   public ThrownPufferfish(EntityType<? extends ThrownPufferfish> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ThrownPufferfish(Level pLevel, LivingEntity pShooter) {
      super(BaabluHopliteAddon.PUFFERFISH.get(), pShooter, pLevel);
   }

   public ThrownPufferfish(Level pLevel, double pX, double pY, double pZ) {
      super(BaabluHopliteAddon.PUFFERFISH.get(), pX, pY, pZ, pLevel);
   }

   @Override
   protected Item getDefaultItem() {
      return Items.PUFFERFISH;
   }

   @Override
   protected float getGravity() {
      return 0.05F;
   }

   @Override
   protected void onHitEntity(EntityHitResult pResult) {
      Entity entity = pResult.getEntity();
      if(!this.level.isClientSide && entity instanceof LivingEntity living){
         this.handleHitEntity(living);
      }
   }

   @Override
   protected void onHit(HitResult pResult) {
      super.onHit(pResult);
      if (!this.level.isClientSide) {
         /*
         List<LivingEntity> hitEntities = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D));
         if(hitEntities.isEmpty()){
            BaabluHopliteAddon.LOGGER.info("{} hit no entities!", this);
         }
         for(LivingEntity hitEntity : hitEntities){
            this.handleHitEntity(hitEntity);
         }
          */
         this.discard();
      }
   }

   private void handleHitEntity(LivingEntity hitEntity) {
      if (hitEntity.isAlive()) {
         //BaabluHopliteAddon.LOGGER.info("{} hit {}!", this, hitEntity);
         if(hitEntity instanceof ServerPlayer serverPlayer) this.touchPlayer(serverPlayer);
         else this.touch(hitEntity);
      }
   }

   private void touchPlayer(ServerPlayer player){
      int puffState = 2; // Pufferfish max puff state is 2
      if (player.hurt(DamageSource.thrown(this, this.getOwner()), (float)(1 + puffState))) {
         if (!this.isSilent()) {
            player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
         }

         player.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * puffState, 0), this);
      }
   }

   private void touch(LivingEntity pMob) {
      int puffState = 2; // Pufferfish max puff state is 2
      if (pMob.hurt(DamageSource.thrown(this, this.getOwner()), (float)(1 + puffState))) {
         pMob.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * puffState, 0), this);
         this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
      }

   }

}