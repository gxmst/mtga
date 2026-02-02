package com.entropy.tacz_mtga.client.renderer;

import com.entropy.tacz_mtga.common.entity.TurretEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TurretRenderer extends GeoEntityRenderer<TurretEntity> {
    public TurretRenderer(EntityRendererProvider.Context context) {
        super(context, new TurretModel());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TurretEntity animatable) {
        return model.getTextureResource(animatable);
    }

    @Override
    protected void applyRotations(TurretEntity turret, PoseStack poseStack, float ageInTicks, float rotationYaw,
            float partialTick) {
        if (turret != null && turret.deathTime > 0) {
            float deathRotation = ((float) turret.deathTime + partialTick - 1.0F) / 20.0F * 1.6F;
            poseStack.mulPose(Axis.ZP
                    .rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1.0F) * this.getDeathMaxRotation(animatable)));
        } else if (animatable.hasCustomName()) {
            String name = animatable.getName().getString();
            name = ChatFormatting.stripFormatting(name);

            if (name != null && (name.equals("Dinnerbone") || name.equalsIgnoreCase("Grumm"))) {
                poseStack.translate(0.0F, animatable.getBbHeight() + 0.1F, 0.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
        }
    }

    @Override
    public void renderFinal(PoseStack poseStack, TurretEntity turret, BakedGeoModel model,
            MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight,
            int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderFinal(poseStack, turret, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red,
                green, blue, alpha);

        poseStack.pushPose();

        model.getBone("gun").ifPresent(gun -> poseStack.translate(gun.getModelPosition().x / 16f,
                gun.getModelPosition().y / 16f, gun.getModelPosition().z / 16f));
        poseStack.mulPose(Axis.YN.rotationDegrees(turret.getYHeadRot() + 180));
        poseStack.mulPose(Axis.XN.rotationDegrees(turret.getXRot()));

        ItemStack stack = turret.getMainHandItem();
        if (!stack.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    packedLight, packedOverlay, poseStack, bufferSource, turret.level(), 0);
        }

        poseStack.popPose();
    }
}
