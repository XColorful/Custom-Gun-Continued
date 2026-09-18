package dev.xcolorful.customgun.client.api.animation.interpolator;

@Deprecated(forRemoval=true)
public class InterpolatorUtil {

    public static IInterpolator<?> fromInterpolation(InterpolatorType interpolation) {
        return interpolation.create();
    }
}
