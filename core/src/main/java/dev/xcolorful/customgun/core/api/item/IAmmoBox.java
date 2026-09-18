package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.ammobox.IAmmoBoxDataAccess;
import dev.xcolorful.customgun.core.api.item.ammobox.IAmmoBoxGetter;

public interface IAmmoBox extends IAmmo, IAmmoBoxDataAccess, IAmmoBoxGetter,
        IPojoItem {
}
