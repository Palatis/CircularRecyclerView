package android.support.v4.widget;

import android.content.Context;
import android.view.animation.Interpolator;

/**
 * Created by Palatis on 2017/4/17.
 */

public class CircularScroller {
    CircularOverScroller mScroller;

    public CircularScroller(Context context, Interpolator interpolator) {
        mScroller = interpolator != null ? new CircularOverScroller(context, interpolator) : new CircularOverScroller(context);
    }

    /**
     * Returns whether the scroller has finished scrolling.
     *
     * @return True if the scroller has finished scrolling, false otherwise.
     */
    public boolean isFinished() {
        return mScroller.isFinished();
    }

    /**
     * Returns the current angular offset in the scroll.
     *
     * @return The new angular offset as an absolute distance from the origin.
     */
    public float getCurrTheta() {
        return mScroller.getCurrTheta();
    }

    /**
     * @return The final angular position for the scroll in progress, if known.
     */
    public float getFinalTheta() {
        return mScroller.getFinalTheta();
    }

    /**
     * Returns the current velocity on platform versions that support it.
     * <p>
     * <p>The device must support at least API level 14 (Ice Cream Sandwich).
     * On older platform versions this method will return 0. This method should
     * only be used as input for nonessential visual effects such as {@link EdgeEffectCompat}.</p>
     *
     * @return The original velocity less the deceleration. Result may be
     * negative.
     */
    public float getCurrVelocity() {
        return mScroller.getCurrVelocity();
    }

    /**
     * Call this when you want to know the new location.  If it returns true,
     * the animation is not yet finished.  loc will be altered to provide the
     * new location.
     */
    public boolean computeScrollOffset() {
        return mScroller.computeScrollOffset();
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     * The scroll will use the default value of 250 milliseconds for the
     * duration.
     *
     * @param startTheta Starting angular scroll offset in theta. Positive
     *                   numbers will scroll the content counterclockwise.
     * @param dTheta     angle to travel. Positive numbers will scroll the
     *                   content to counterclockwise.
     */
    public void startScroll(float startTheta, float dTheta) {
        mScroller.startScroll(startTheta, dTheta);
    }

    /**
     * Start scrolling by providing a starting angle and the angle to travel.
     *
     * @param startTheta Starting angular scroll offset in theta. Positive
     *                   numbers will scroll the content counterclockwise.
     * @param dTheta     angle to travel. Positive numbers will scroll the
     *                   content to counterclockwise.
     * @param duration   Duration of the scroll in milliseconds.
     */
    public void startScroll(float startTheta, float dTheta, int duration) {
        mScroller.startScroll(startTheta, dTheta, duration);
    }

    /**
     * Start scrolling based on a fling gesture. The distance travelled will
     * depend on the initial velocity of the fling.
     *
     * @param startTheta    Starting angle of the scroll
     * @param velocityTheta Initial velocity of the fling measured in theta per second.
     * @param minTheta      Minimum theta value. The scroller will not scroll past this point.
     * @param maxTheta      Maximum theta value. The scroller will not scroll past this point.
     */
    public void fling(float startTheta, float velocityTheta, float minTheta, float maxTheta) {
        mScroller.fling(startTheta, velocityTheta, minTheta, maxTheta);
    }

    /**
     * Start scrolling based on a fling gesture. The distance travelled will
     * depend on the initial velocity of the fling.
     *
     * @param startTheta    Starting angle of the scroll
     * @param velocityTheta Initial velocity of the fling measured in theta per second.
     * @param minTheta      Minimum theta value. The scroller will not scroll past this point.
     * @param maxTheta      Maximum theta value. The scroller will not scroll past this point.
     * @param overTheta     Overfling range. If > 0, overfling in either direction will be possible.
     */
    public void fling(float startTheta, float velocityTheta, float minTheta, float maxTheta, float overTheta) {
        mScroller.fling(startTheta, velocityTheta, minTheta, maxTheta, overTheta);
    }

    /**
     * Call this when you want to 'spring back' into a valid coordinate range.
     *
     * @param startTheta Starting angle
     * @param minTheta   Minimum valid angle
     * @param maxTheta   Maximum valid angle
     * @return {@code true} if a springback was initiated, {@code false} if startTheta was already within the valid range.
     */
    public boolean springBack(float startTheta, float minTheta, float maxTheta) {
        return mScroller.springBack(startTheta, minTheta, maxTheta);
    }

    /**
     * Notify the scroller that we've reached a horizontal boundary.
     * Normally the information to handle this will already be known
     * when the animation is started, such as in a call to one of the
     * fling functions. However there are cases where this cannot be known
     * in advance. This function will transition the current motion and
     * animate from startX to finalX as appropriate.
     *
     * @param startTheta Starting/current angle
     * @param finalTheta Desired final angle
     * @param overTheta  Magnitude of overscroll allowed. This should be the maximum
     *                   desired distance from finalTheta. Absolute value - must be positive.
     */
    public void notifyEdgeReached(float startTheta, float finalTheta, float overTheta) {
        mScroller.notifyEdgeReached(startTheta, finalTheta, overTheta);
    }

    /**
     * Returns whether the current Scroller is currently returning to a valid position.
     * Valid bounds were provided by the
     * {@link #fling(float, float, float, float, float)} method.
     * <p>
     * One should check this value before calling
     * {@link #startScroll(float, float)} as the interpolation currently in progress
     * to restore a valid position will then be stopped. The caller has to take into account
     * the fact that the started scroll will start from an overscrolled position.
     *
     * @return true when the current position is overscrolled and in the process of
     * interpolating back to a valid value.
     */
    public boolean isOverScrolled() {
        return mScroller.isOverScrolled();
    }

    /**
     * Stops the animation. Contrary to {@link #forceFinished(boolean)},
     * aborting the animating causes the scroller to move to the final x and y
     * positions.
     *
     * @see #forceFinished(boolean)
     */
    public void abortAnimation() {
        mScroller.abortAnimation();
    }

    /**
     * Force the finished field to a particular value. Contrary to
     * {@link #abortAnimation()}, forcing the animation to finished
     * does NOT cause the scroller to move to the final x and y
     * position.
     *
     * @param finished The new finished value.
     */
    public final void forceFinished(boolean finished) {
        mScroller.forceFinished(finished);
    }
}
