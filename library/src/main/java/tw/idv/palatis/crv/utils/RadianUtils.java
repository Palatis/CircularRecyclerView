package tw.idv.palatis.crv.utils;

/**
 * Created by Palatis on 2017/4/15.
 */

public final class RadianUtils {
    public static float radianFromDegree(float degree) {
        return (float) (degree / 180.0 * Math.PI);
    }

    public static float degreeFromRadian(float radian) {
        return (float) (radian / Math.PI * 180.0);
    }

    private RadianUtils() {
    }
}
