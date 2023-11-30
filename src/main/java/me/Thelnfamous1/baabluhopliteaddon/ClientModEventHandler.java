package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = BaabluHopliteAddon.MODID, value = Dist.CLIENT)
public class ClientModEventHandler {
    @SubscribeEvent
    static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(BaabluHopliteAddon.HOMING_ARROW.get(), HomingArrowRenderer::new);
        // 2.8 is 0.7 / 0.25, where 0.7 is the pufferfish projectile size and 0.25 is the size of normal thrown projectiles
        event.registerEntityRenderer(BaabluHopliteAddon.PUFFERFISH.get(), ctx -> new ThrownItemRenderer<>(ctx, 2.8F, false));
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerBow();
            registerCannon();
        });
    }

    private static void registerBow(){
        ItemProperties.register(BaabluHopliteAddon.ARTEMIS_BOW.get(), new ResourceLocation("pull"), (stack, level, living, seed) -> {
            if (living == null) {
                return 0.0F;
            } else {
                return living.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - living.getUseItemRemainingTicks()) / 20.0F;
            }
        });
        ItemProperties.register(BaabluHopliteAddon.ARTEMIS_BOW.get(), new ResourceLocation("pulling"), (stack, level, living, seed) ->
                living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F);
    }

    private static void registerCannon(){
        ItemProperties.register(BaabluHopliteAddon.PUFFERFISH_CANNON.get(), new ResourceLocation("pull"), (stack, level, living, seed) -> {
            if (living == null) {
                return 0.0F;
            } else {
                return living.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - living.getUseItemRemainingTicks()) / 20.0F;
            }
        });
        ItemProperties.register(BaabluHopliteAddon.PUFFERFISH_CANNON.get(), new ResourceLocation("pulling"), (stack, level, living, seed) ->
                living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F);
    }
}
