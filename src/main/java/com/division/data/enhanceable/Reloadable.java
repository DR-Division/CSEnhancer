package com.division.data.enhanceable;

import com.division.data.wrapper.WrappedWeaponReloadEvent;

public interface Reloadable extends Enhanceable{

    void reload(WrappedWeaponReloadEvent event);
}
