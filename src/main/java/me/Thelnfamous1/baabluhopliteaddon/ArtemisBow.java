package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
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
import java.util.function.Predicate;

public class ArtemisBow extends BowItem {

    public static final String LIGHTNING_ARROW_TAG = new ResourceLocation(BaabluHopliteAddon.MODID, "lightning_arrow").toString();
    public static final float HOMING_ARROW_CHANCE = 0.25F;
    public static final float LIGHTNING_ARROW_CHANCE = 0.25F;
    public static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#.##"), (decimalFormat) -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
    public static final String HOMING_CHANCE_TAG = "HomingChance";
    public static final String LIGHTNING_CHANCE_TAG = "LightningChance";

    public ArtemisBow(Properties pProperties) {
        super(pProperties);
    }

    public static float getHomingArrowChance(ItemStack bowStack) {
        CompoundTag tag = bowStack.getTag();
        return tag == null || !tag.contains(HOMING_CHANCE_TAG)? HOMING_ARROW_CHANCE : tag.getFloat(HOMING_CHANCE_TAG);
    }

    public static float getLightningArrowChance(ItemStack bowStack) {
        CompoundTag tag = bowStack.getTag();
        return tag == null || !tag.contains(LIGHTNING_CHANCE_TAG) ? LIGHTNING_ARROW_CHANCE : tag.getFloat(LIGHTNING_CHANCE_TAG);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        /*
        25% chance to launch homing arrows that seek out targets
        75% chance to launch a normal arrow that has a 25% chance to strike lightning on the enemy that was hit
         */
        AbstractArrow custom = super.customArrow(arrow);
        if(custom.getOwner() instanceof LivingEntity shooter){
            ItemStack bowStack = shooter.getUseItem();
            if(custom.level().random.nextFloat() < getHomingArrowChance(bowStack)){
                custom = new HomingArrow(shooter.level(), shooter);
                ((HomingArrow)custom).setEffectsFromItem(bowStack);
                this.lockOn(shooter, (HomingArrow) custom);
            } else if(custom.level().random.nextFloat() < getLightningArrowChance(bowStack)){
                custom.addTag(LIGHTNING_ARROW_TAG);
            }
        }

        return custom;
    }

    private void lockOn(LivingEntity shooter, HomingArrow homingArrow) {
        double maxLockOnDist = this.getMaxLockOnDist();
        HitResult hitResult = shooter.pick(maxLockOnDist, 0.0F, false);
        Vec3 startVec = shooter.getEyePosition();
        double maxLockOnDistSqr = maxLockOnDist * maxLockOnDist;
        if (hitResult.getType() != HitResult.Type.MISS) {
            maxLockOnDistSqr = hitResult.getLocation().distanceToSqr(startVec);
        }
        Vec3 viewVector = shooter.getViewVector(1.0F).scale(maxLockOnDist);
        Vec3 endVec = startVec.add(viewVector);
        AABB searchBox = shooter.getBoundingBox().expandTowards(viewVector).inflate(1.0D);
        Predicate<Entity> filter = (e) -> !e.isSpectator() && e.isPickable();
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(shooter, startVec, endVec, searchBox, filter, maxLockOnDistSqr);
        if(entityHitResult != null){
            homingArrow.setHomingTarget(entityHitResult.getEntity());
        }
    }

    private double getMaxLockOnDist() {
        return 512;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        double homingArrowChance = getHomingArrowChance(pStack) * 100.0D;
        pTooltipComponents.add(Component.translatable("item.baabluhopliteaddon.artemis_bow.homing_arrow", DECIMAL_FORMAT.format(homingArrowChance)).withStyle(ChatFormatting.GRAY));
        double regularArrowChance = 100.0D - homingArrowChance;
        double lightningArrowChance = getLightningArrowChance(pStack) * 100.0D;
        pTooltipComponents.add(Component.translatable("item.baabluhopliteaddon.artemis_bow.lightning_arrow",  DECIMAL_FORMAT.format(regularArrowChance), DECIMAL_FORMAT.format(lightningArrowChance)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void onCraftedBy(ItemStack pStack, Level pLevel, Player pPlayer) {
        pStack.enchant(Enchantments.POWER_ARROWS, 3);
    }
}
