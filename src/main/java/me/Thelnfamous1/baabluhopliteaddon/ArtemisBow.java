package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class ArtemisBow extends BowItem {

    public static final String LIGHTNING_ARROW_TAG = new ResourceLocation(BaabluHopliteAddon.MODID, "lightning_arrow").toString();
    public static final float HOMING_ARROW_CHANCE = 0.25F;
    public static final float LIGHTNING_ARROW_CHANCE = 0.25F;
    public static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#.##"), (decimalFormat) -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));

    public ArtemisBow(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        /*
        25% chance to launch homing arrows that seek out targets
        75% chance to launch a normal arrow that has a 25% chance to strike lightning on the enemy that was hit
         */
        AbstractArrow custom = super.customArrow(arrow);
        boolean homing = arrow.level.random.nextFloat() < HOMING_ARROW_CHANCE;
        if(homing){
            Entity owner = custom.getOwner();
            if(owner instanceof LivingEntity shooter){
                custom = new HomingArrow(shooter.level, shooter);
                ((HomingArrow)custom).setEffectsFromItem(shooter.getUseItem());
                this.lockOn(shooter, (HomingArrow) custom);
            }
        } else{
            boolean lightning = custom.level.random.nextFloat() < LIGHTNING_ARROW_CHANCE;
            if(lightning){
                custom.addTag(LIGHTNING_ARROW_TAG);
            }
        }

        return custom;
    }

    private void lockOn(LivingEntity shooter, HomingArrow homingArrow) {
        double maxLockOnDist = this.getMaxLockOnDist();
        HitResult hitResult = shooter.pick(maxLockOnDist, 0.0F, false);
        Vec3 eyePosition = shooter.getEyePosition(0.0F);
        double effectiveMaxLockOnDist = maxLockOnDist;

        if (hitResult.getType() != HitResult.Type.MISS) {
            effectiveMaxLockOnDist = hitResult.getLocation().distanceToSqr(eyePosition);
        }

        Vec3 startVec = shooter.getViewVector(1.0F);
        Vec3 endVec = eyePosition.add(startVec.x * maxLockOnDist, startVec.y * maxLockOnDist, startVec.z * maxLockOnDist);
        AABB searchBox = shooter.getBoundingBox().expandTowards(startVec.scale(maxLockOnDist)).inflate(1.0D, 1.0D, 1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(shooter, eyePosition, endVec, searchBox, (e) -> !e.isSpectator() && e.isPickable(), effectiveMaxLockOnDist);
        if(entityHitResult != null){
            homingArrow.setHomingTarget(entityHitResult.getEntity());
        }
    }

    private double getMaxLockOnDist() {
        return 64;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.baabluhopliteaddon.artemis_bow.homing_arrow", DECIMAL_FORMAT.format(HOMING_ARROW_CHANCE * 100.0D)).withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("item.baabluhopliteaddon.artemis_bow.lightning_arrow",  DECIMAL_FORMAT.format(100.0D - (HOMING_ARROW_CHANCE * 100.0D)), DECIMAL_FORMAT.format(LIGHTNING_ARROW_CHANCE * 100.0D)).withStyle(ChatFormatting.GRAY));
    }
}
