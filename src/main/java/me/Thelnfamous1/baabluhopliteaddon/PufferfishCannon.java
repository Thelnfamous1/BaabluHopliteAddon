package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class PufferfishCannon extends ProjectileWeaponItem {
    public static final Predicate<ItemStack> PUFFERFISH_ONLY = (stack) -> stack.is(Items.PUFFERFISH);
    public static final int COOLDOWN_IN_SECONDS = 10;

    public PufferfishCannon(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player player) {
            boolean infinite = player.getAbilities().instabuild;
            ItemStack projectile = player.getProjectile(pStack);

            int chargeTime = this.getUseDuration(pStack) - pTimeLeft;
            chargeTime = ForgeEventFactory.onArrowLoose(pStack, pLevel, player, chargeTime, !projectile.isEmpty() || infinite);
            if (chargeTime < 0) return;

            if (!projectile.isEmpty() || infinite) {
                if (projectile.isEmpty()) {
                    projectile = new ItemStack(Items.PUFFERFISH);
                }

                float powerForTime = getPowerForTime(chargeTime);
                if (!((double)powerForTime < 0.1D)) {
                    if (!pLevel.isClientSide) {
                        ThrownPufferfish pufferfish = new ThrownPufferfish(pLevel, player);
                        if(!projectile.isEmpty()){
                            ItemStack copy = projectile.copy();
                            copy.setCount(1);
                            pufferfish.setItem(copy);
                        }
                        pufferfish.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerForTime * 3.0F, 1.0F);

                        pStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                        pLevel.addFreshEntity(pufferfish);
                        player.getCooldowns().addCooldown(pStack.getItem(), this.getCooldownTicks());
                    }

                    pLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + powerForTime * 0.5F);
                    if (!infinite) {
                        projectile.shrink(1);
                        if (projectile.isEmpty()) {
                            player.getInventory().removeItem(projectile);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    public static float getPowerForTime(int pCharge) {
        float power = (float)pCharge / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        if (power > 1.0F) {
            power = 1.0F;
        }

        return power;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        boolean hasProjectile = !pPlayer.getProjectile(itemInHand).isEmpty();

        InteractionResultHolder<ItemStack> nockEventResult = ForgeEventFactory.onArrowNock(itemInHand, pLevel, pPlayer, pHand, hasProjectile);
        if (nockEventResult != null) return nockEventResult;

        if (!pPlayer.getAbilities().instabuild && !hasProjectile) {
            return InteractionResultHolder.fail(itemInHand);
        } else {
            pPlayer.startUsingItem(pHand);
            return InteractionResultHolder.consume(itemInHand);
        }
    }

    protected int getCooldownTicks(){
        return COOLDOWN_IN_SECONDS * 20;
    }


    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return PUFFERFISH_ONLY;
    }

    @Override
    public int getDefaultProjectileRange() {
        return BowItem.DEFAULT_RANGE;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.baabluhopliteaddon.pufferfish_cannon.pufferfish").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("item.baabluhopliteaddon.pufferfish_cannon.cooldown", COOLDOWN_IN_SECONDS).withStyle(ChatFormatting.GRAY));
    }
}
