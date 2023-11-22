package me.Thelnfamous1.baabluhopliteaddon.mixin;

import me.Thelnfamous1.baabluhopliteaddon.PhysicsCheck;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile implements PhysicsCheck {

    @Shadow public abstract boolean isNoPhysics();

    protected AbstractArrowMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    @ModifyVariable(method = "tick", at = @At(value = "LOAD", ordinal = 4))
    private boolean modifyIsNoPhysicsLocalForGravity(boolean isNoPhysics){
        if(!isNoPhysics && this.canBypassGravity()){
            return true;
        }
        return this.isNoPhysics();
    }
}
