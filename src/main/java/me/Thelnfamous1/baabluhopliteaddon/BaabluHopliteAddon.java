package me.Thelnfamous1.baabluhopliteaddon;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(BaabluHopliteAddon.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BaabluHopliteAddon {
    public static final String MODID = "baabluhopliteaddon";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> ARTEMIS_BOW = ITEMS.register("artemis_bow", () -> new ArtemisBow(new Item.Properties()
            .durability(384)));

    public static final RegistryObject<Item> PUFFERFISH_CANNON = ITEMS.register("pufferfish_cannon", () -> new PufferfishCannon(new Item.Properties()
            .durability(465)));

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<HomingArrow>> HOMING_ARROW = ENTITY_TYPES.register("homing_arrow", () ->
            EntityType.Builder.<HomingArrow>of(HomingArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(new ResourceLocation(MODID, "homing_arrow").toString()));

    public static final RegistryObject<EntityType<ThrownPufferfish>> PUFFERFISH = ENTITY_TYPES.register("pufferfish", () ->
            EntityType.Builder.<ThrownPufferfish>of(ThrownPufferfish::new, MobCategory.MISC)
                    .sized(0.7F, 0.7F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation(MODID, "pufferfish").toString()));

    public BaabluHopliteAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(this::onGetProjectile);
        MinecraftForge.EVENT_BUS.addListener(this::onProjectileImpact);
    }

    @SubscribeEvent
    static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey().equals(CreativeModeTabs.COMBAT)){
            event.accept(ARTEMIS_BOW);
            event.accept(PUFFERFISH_CANNON);
        }
    }

    private void onGetProjectile(LivingGetProjectileEvent event){
        if(event.getProjectileWeaponItemStack().getItem() instanceof PufferfishCannon){
            if(!event.getProjectileWeaponItemStack().is(Items.PUFFERFISH) && event.getEntity() instanceof Player player && player.getAbilities().instabuild){
                event.setProjectileItemStack(new ItemStack(Items.PUFFERFISH));
            }
        }
    }

    private void onProjectileImpact(ProjectileImpactEvent event){
        if(!event.getProjectile().level().isClientSide && event.getProjectile() instanceof AbstractArrow arrow){
            if(arrow.getTags().contains(ArtemisBow.LIGHTNING_ARROW_TAG)){
                HitResult hitResult = event.getRayTraceResult();
                if(hitResult.getType() == HitResult.Type.MISS) return;
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(event.getProjectile().level());
                if(lightningBolt == null) return;
                lightningBolt.moveTo(hitResult.getLocation());
                Entity entity = event.getProjectile().getOwner();
                lightningBolt.setCause(entity instanceof ServerPlayer ? (ServerPlayer)entity : null);
                event.getProjectile().level().addFreshEntity(lightningBolt);
                event.getProjectile().level().playSound(null, BlockPos.containing(hitResult.getLocation()), SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
            }
        }
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(HOMING_ARROW.get(), HomingArrowRenderer::new);
        // 2.8 is 0.7 / 0.25, where 0.7 is the pufferfish projectile size and 0.25 is the size of normal thrown projectiles
        event.registerEntityRenderer(PUFFERFISH.get(), ctx -> new ThrownItemRenderer<>(ctx, 2.8F, false));
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerBow();
            registerCannon();
        });
    }

    private static void registerBow(){
        ItemProperties.register(ARTEMIS_BOW.get(), new ResourceLocation("pull"), (stack, level, living, seed) -> {
            if (living == null) {
                return 0.0F;
            } else {
                return living.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - living.getUseItemRemainingTicks()) / 20.0F;
            }
        });
        ItemProperties.register(ARTEMIS_BOW.get(), new ResourceLocation("pulling"), (stack, level, living, seed) ->
                living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F);
    }

    private static void registerCannon(){
        ItemProperties.register(PUFFERFISH_CANNON.get(), new ResourceLocation("pull"), (stack, level, living, seed) -> {
            if (living == null) {
                return 0.0F;
            } else {
                return living.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - living.getUseItemRemainingTicks()) / 20.0F;
            }
        });
        ItemProperties.register(PUFFERFISH_CANNON.get(), new ResourceLocation("pulling"), (stack, level, living, seed) ->
                living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F);
    }
}
