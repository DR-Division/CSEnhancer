package com.division.data.enhanceable;

import com.division.data.wrapper.WrappedWeaponShootEvent;

public interface Shootable extends Enhanceable{

    void shoot(WrappedWeaponShootEvent event);
}
