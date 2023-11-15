package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class HomingArrow extends Arrow {
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
    public void tick() {
        super.tick();
    }
}
