package pers.zhc.tools.views;

import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;

/**
 * @author bczhc
 */
public interface OnColorPickedInterface {
    void onColorPicked(@FloatRange(from = 0, to = 1) float h,
                       @FloatRange(from = 0, to = 1) float s,
                       @FloatRange(from = 0, to = 1) float v,
                       @IntRange(from = 0, to = 255) int alpha);
}
