/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.instance.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.model.AmmoModelObject;
import dev.xcolorful.customgun.client.resource.assets.display.AmmoDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._ModelTransform;
import dev.xcolorful.customgun.client.resource.assets.display.ammo._AmmoEntityDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.ammo._AmmoParticle;
import dev.xcolorful.customgun.client.resource.assets.display.ammo._ShellDisplay;
import dev.xcolorful.customgun.client.resource.assets.model.BedrockModel;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public final class ClientAmmoIndexInstance extends PojoInstance<AmmoIndex> {

    private @Nullable AmmoModelObject ammoModel;
    private @Nullable AmmoModelObject ammoEntityModel;
    private @Nullable AmmoModelObject ammoShellModel;

    private AmmoDisplay ammoDisplayCache;
    private @Nullable ParticleOptions ammoParticleOptionsCache;

    private ClientAmmoIndexInstance(@NotNull AmmoIndex pojo) {
        super(pojo);
    }

    public static @Nullable ClientAmmoIndexInstance fromPojo(AmmoIndex pojo) {
        if (pojo == null) return null;
        ClientAmmoIndexInstance instance = new ClientAmmoIndexInstance(pojo);
        if (!instance.isPojoValid()) return null;
        else return instance;
    }

    @Override public boolean resetCache() {
        this.ammoDisplayCache = ClientResourceApi.getAmmoDisplay(this.getPojo().getDisplayIndexLocation());
        if (this.ammoDisplayCache == null) {
            CustomGun.LOGGER.debug("ClientAmmoIndexInstance: AmmoDisplay {} not found", this.getPojo().getDisplayIndexLocation());
            return false;
        } else if (!this.ammoDisplayCache.isValid()) {
            CustomGun.LOGGER.debug("ClientAmmoIndexInstance: AmmoDisplay {} not valid", this.getPojo().getDisplayIndexLocation());
            return false;
        }
        {
            _AmmoParticle ammoParticle = this.ammoDisplayCache.getAmmoParticle();
            var particleRl = ammoParticle.getParticleLocation();
            try {
                this.ammoParticleOptionsCache = ParticleArgument.readParticle(new StringReader(particleRl.toString()), BuiltInRegistries.PARTICLE_TYPE.asLookup());
            } catch (CommandSyntaxException e) {
                CustomGun.LOGGER.debug("ClientAmmoIndexInstance: ParticleArgument.readParticle({}) failed", particleRl, e);
            }
            if (this.ammoParticleOptionsCache == null) {
                CustomGun.LOGGER.debug("ClientAmmoIndexInstance: AmmoParticle {} not valid", particleRl);
            }
        }

        {
            BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(this.ammoDisplayCache.getModelLocation());
            if (bedrockModel != null) {
                this.ammoModel = AmmoModelObject.fromPojo(bedrockModel);
                if (this.ammoModel == null) CustomGun.LOGGER.debug("ClientAmmoIndexInstance: Failed to create AmmoModelObject {}", this.ammoDisplayCache.getModelLocation());
            } else {
                CustomGun.LOGGER.debug("ClientAmmoIndexInstance: BedrockModel {} not found", this.ammoDisplayCache.getModelLocation());
            }
        }
        _AmmoEntityDisplay ammoEntityDisplay = this.ammoDisplayCache.getAmmoEntityDisplay();
        if (ammoEntityDisplay != null) {
            BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(ammoEntityDisplay.getModelLocation());
            if (bedrockModel != null) {
                this.ammoEntityModel = AmmoModelObject.fromPojo(bedrockModel);
                if (this.ammoEntityModel == null) CustomGun.LOGGER.debug("ClientAmmoIndexInstance: Failed to create AmmoModelObject (for entity) {}", ammoEntityDisplay.getModelLocation());
            }
        }
        _ShellDisplay shellDisplay = this.ammoDisplayCache.getShellDisplay();
        if (shellDisplay != null) {
            BedrockModel bedrockModel = ClientResourceApi.getBedrockModel(shellDisplay.getModelLocation());
            if (bedrockModel != null) {
                this.ammoShellModel = AmmoModelObject.fromPojo(bedrockModel);
                if (this.ammoShellModel == null) CustomGun.LOGGER.debug("ClientAmmoIndexInstance: Failed to create AmmoModelObject (for shell) {}", shellDisplay.getModelLocation());
            }
        }

        return true;
    }
    @Override protected boolean isPojoValid() {
        var pojo = this.getPojo();
        if (!pojo.isValid()) return false;
        if (!resetCache()) return false;

        return true;
    }

    // --------Getter--------

    public AmmoDisplay getAmmoDisplay() {
        return this.ammoDisplayCache;
    }
    public @Nullable ParticleOptions getParticleOptions() {
        return this.ammoParticleOptionsCache;
    }
    public @Nullable AmmoModelObject getAmmoModel() {
        return this.ammoModel;
    }
    public @Nullable AmmoModelObject getAmmoEntityModel() {
        return this.ammoEntityModel;
    }
    public @Nullable AmmoModelObject getAmmoShellModel() {
        return this.ammoShellModel;
    }

    // --------Deprecated--------

    @Deprecated public String getName() {
        return ComponentUtils.toTranslatableKey(this.getPojo().getNameLang());
    }
    @Deprecated public String getTooltipKey() {
        return ComponentUtils.toTranslatableKey(this.getPojo().getTooltipLang());
    }
    @Deprecated public int getStackSize() {
        return this.getPojo().getMaxStackSize();
    }
    @Deprecated public _AmmoParticle getParticle() {
        return this.ammoDisplayCache.getAmmoParticle();
    }
    @Deprecated public Color getTracerColor() {
        return this.ammoDisplayCache.getTracerColor();
    }
    @Deprecated public _ModelTransform getAmmoTransform() {
        return this.ammoDisplayCache.getModelTransform();
    }
}
