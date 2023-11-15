package me.Thelnfamous1.baabluhopliteaddon;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HomingArrowRenderer extends ArrowRenderer<HomingArrow> {
   public static final ResourceLocation NORMAL_ARROW_LOCATION = new ResourceLocation(BaabluHopliteAddon.MODID, "textures/entity/projectiles/homing_arrow.png");
   public static final ResourceLocation TIPPED_ARROW_LOCATION = new ResourceLocation(BaabluHopliteAddon.MODID,"textures/entity/projectiles/tipped_homing_arrow.png");

   public HomingArrowRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
   }

   @Override
   public ResourceLocation getTextureLocation(HomingArrow pEntity) {
      return pEntity.getColor() > 0 ? TIPPED_ARROW_LOCATION : NORMAL_ARROW_LOCATION;
   }
}