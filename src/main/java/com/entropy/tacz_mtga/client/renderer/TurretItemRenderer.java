package com.entropy.tacz_mtga.client.renderer;

import com.entropy.tacz_mtga.TurretItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TurretItemRenderer extends GeoItemRenderer<TurretItem> {
    public TurretItemRenderer() {
        super(new TurretItemModel());
    }
}
