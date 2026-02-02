package com.entropy.tacz_mtga.client.renderer;

import com.entropy.tacz_mtga.TACZMtga;
import com.entropy.tacz_mtga.TurretItem;
import com.entropy.tacz_mtga.common.entity.TurretEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TurretItemModel extends GeoModel<TurretItem> {
    @Override
    public ResourceLocation getAnimationResource(TurretItem animatable) {
        return new ResourceLocation(TACZMtga.MODID, "animations/entity/turret.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(TurretItem animatable) {
        return new ResourceLocation(TACZMtga.MODID, "geo/entity/turret.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TurretItem animatable) {
        return new ResourceLocation(TACZMtga.MODID, "textures/entity/turret_active.png");
    }
}
