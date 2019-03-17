package com.sphereruler.panoclicks.Model;

import java.util.List;

public class RootObject {
    List<Angles> angles;

    public RootObject(List<Angles> angles) {
        this.angles = angles;
    }

    public List<Angles> getAngles() {
        return angles;
    }
}
