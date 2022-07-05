package com.division.data.enhanceable;

import com.division.enums.Priority;

public interface Enhanceable {

    Priority getPriority();

    void setPriority(Priority value);

    boolean isEnabled();

}
