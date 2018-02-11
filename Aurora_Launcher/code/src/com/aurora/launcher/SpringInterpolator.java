package com.aurora.launcher;

import android.view.animation.Interpolator;

public class SpringInterpolator implements Interpolator {
    private float factor;

    public SpringInterpolator() {
        this.factor = 4f;
    }
    
    public SpringInterpolator(float fac) {
        this.factor = fac;
    }

    @Override
    public float getInterpolation(float input) {
        return (float)(Math.pow(3, -5 * input) * Math.sin((10*input - factor / 4) * (2 * Math.PI) / factor) + 1);
    }
}