package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = BaabluHopliteAddon.MODID)
public class ThrownPufferfish extends ThrowableItemProjectile{
   private int explosionPower = 1;

   public ThrownPufferfish(EntityType<? extends ThrownPufferfish> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public ThrownPufferfish(Level pLevel, LivingEntity pShooter) {
      super(BaabluHopliteAddon.PUFFERFISH.get(), pShooter, pLevel);
   }

   public ThrownPufferfish(Level pLevel, double pX, double pY, double pZ) {
      super(BaabluHopliteAddon.PUFFERFISH.get(), pX, pY, pZ, pLevel);
   }

   @SubscribeEvent
   static void onExplosionDetonate(ExplosionEvent.Detonate event){
      List<Entity> affectedEntities = event.getAffectedEntities();
      if(!event.getLevel().isClientSide && event.getExplosion().getExploder() instanceof ThrownPufferfish pufferfish){
         for(Entity affectedEntity : affectedEntities){
            if(affectedEntity instanceof LivingEntity living){
               pufferfish.postHit(living);
            }
         }
      }
   }

   public void setExplosionPower(int explosionPower){
      this.explosionPower = explosionPower;
   }

   protected void postHit(LivingEntity hitEntity) {
      if (hitEntity.isAlive()) {
         if(hitEntity instanceof ServerPlayer serverPlayer) this.stingPlayer(serverPlayer);
         else this.sting(hitEntity);
      }
   }

   private void stingPlayer(ServerPlayer player){
       if (!this.isSilent()) {
         player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
      }

      player.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 0), this);
   }

   private void sting(LivingEntity pMob) {
      pMob.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 0), this);
      this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putByte("ExplosionPower", (byte)this.explosionPower);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("ExplosionPower", 99)) {
         this.explosionPower = pCompound.getByte("ExplosionPower");
      }

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
   protected void onHit(HitResult pResult) {
      super.onHit(pResult);
      if (!this.level.isClientSide) {
         boolean grief = ForgeEventFactory.getMobGriefingEvent(this.level, this.getOwner());
         this.level.explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, grief, grief ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE);
         this.discard();
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult pResult) {
      super.onHitEntity(pResult);
      if (!this.level.isClientSide) {
         Entity hitEntity = pResult.getEntity();
         Entity owner = this.getOwner();
         hitEntity.hurt(DamageSource.thrown(this, this.getOwner()), 6.0F);
         if (owner instanceof LivingEntity livingOwner) {
            this.doEnchantDamageEffects(livingOwner, hitEntity);
         }
         if(hitEntity instanceof LivingEntity victim){
            this.postHit(victim);
         }

      }
   }

}