package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class PufferfishCannon extends Item implements Vanishable {
    public static final int COOLDOWN_IN_SECONDS = 10;

    public PufferfishCannon(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return !entityLiving.swinging ? HumanoidModel.ArmPose.CROSSBOW_HOLD : null;
            }
        });
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CROSSBOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        if (!pLevel.isClientSide) {
            ThrownPufferfish pufferfish = new ThrownPufferfish(pLevel, pPlayer);
            pufferfish.setExplosionPower(2);
            pufferfish.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 3.0F, 1.0F);

            itemInHand.hurtAndBreak(1, pPlayer, (p) -> p.broadcastBreakEvent(pPlayer.getUsedItemHand()));
            pLevel.addFreshEntity(pufferfish);
            pPlayer.getCooldowns().addCooldown(itemInHand.getItem(), this.getCooldownTicks());
        }

        pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FIREWORK_ROCKET_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);

        pPlayer.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.consume(itemInHand);
    }

    protected int getCooldownTicks(){
        return COOLDOWN_IN_SECONDS * 20;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.baabluhopliteaddon.pufferfish_cannon.pufferfish").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("item.baabluhopliteaddon.pufferfish_cannon.cooldown", COOLDOWN_IN_SECONDS).withStyle(ChatFormatting.GRAY));
    }
}
