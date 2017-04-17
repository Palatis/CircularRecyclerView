package android.support.v4.widget;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * Created by Palatis on 2017/4/17.
 */

public class CircularOverScroller {
    private int mMode;

    private final SplineOverScroller mScrollerTheta;
    private Interpolator mInterpolator;

    private final boolean mFlywheel;

    private static final int DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final int FLING_MODE = 1;

    /**
     * Creates an OverScroller with a viscous fluid scroll interpolator and flywheel.
     *
     * @param context
     */
    public CircularOverScroller(Context context) {
        this(context, null);
    }

    /**
     * Creates an OverScroller with flywheel enabled.
     *
     * @param context      The context of this application.
     * @param interpolator The scroll interpolator. If null, a default (viscous) interpolator will
     *                     be used.
     */
    public CircularOverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, true);
    }

    /**
     * Creates an OverScroller.
     *
     * @param context      The context of this application.
     * @param interpolator The scroll interpolator. If null, a default (viscous) interpolator will
     *                     be used.
     * @param flywheel     If true, successive fling motions will keep on increasing scroll speed.
     * @hide
     */
    public CircularOverScroller(Context context, Interpolator interpolator, boolean flywheel) {
        if (interpolator == null) {
            mInterpolator = new ViscousFluidInterpolator();
        } else {
            mInterpolator = interpolator;
        }
        mFlywheel = flywheel;
        mScrollerTheta = new SplineOverScroller(context);
    }

    void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            mInterpolator = new ViscousFluidInterpolator();
        } else {
            mInterpolator = interpolator;
        }
    }

    /**
     * The amount of friction applied to flings. The default value
     * is {@link ViewConfiguration#getScrollFriction}.
     *
     * @param friction A scalar dimension-less value representing the coefficient of
     *                 friction.
     */
    public final void setFriction(float friction) {
        mScrollerTheta.setFriction(friction);
    }

    /**
     * Returns whether the scroller has finished scrolling.
     *
     * @return True if the scroller has finished scrolling, false otherwise.
     */
    public final boolean isFinished() {
        return mScrollerTheta.mFinished;
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
        mScrollerTheta.mFinished = finished;
    }

    /**
     * Returns the current Y offset in the scroll.
     *
     * @return The new Y offset as an absolute distance from the origin.
     */
    public final float getCurrTheta() {
        return mScrollerTheta.mCurrentPosition;
    }

    /**
     * Returns the absolute value of the current velocity.
     *
     * @return The original velocity less the deceleration, norm of the X and Y velocity vector.
     */
    public float getCurrVelocity() {
        return mScrollerTheta.mCurrVelocity;
    }

    /**
     * Returns the start Y offset in the scroll.
     *
     * @return The start Y offset as an absolute distance from the origin.
     */
    public final float getStartTheta() {
        return mScrollerTheta.mStart;
    }

    /**
     * Returns where the scroll will end. Valid only for "fling" scrolls.
     *
     * @return The final X offset as an absolute distance from the origin.
     */
    public final float getFinalTheta() {
        return mScrollerTheta.mFinal;
    }

    /**
     * Returns how long the scroll event will take, in milliseconds.
     *
     * @return The duration of the scroll in milliseconds.
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScrollers don't necessarily have a fixed duration.
     * This function will lie to the best of its ability.
     */
    @Deprecated
    public final int getDuration() {
        return mScrollerTheta.mDuration;
    }

    /**
     * Extend the scroll animation. This allows a running animation to scroll
     * further and longer, when used with {@link #setFinalTheta(float)}.
     *
     * @param extend Additional time to scroll in milliseconds.
     * @hide Pending removal once nothing depends on it
     * @see #setFinalTheta(float)
     * @deprecated OverScrollers don't necessarily have a fixed duration.
     * Instead of setting a new final position and extending
     * the duration of an existing scroll, use startScroll
     * to begin a new animation.
     */
    @Deprecated
    public void extendDuration(int extend) {
        mScrollerTheta.extendDuration(extend);
    }

    /**
     * Sets the final angular position for this scroller.
     *
     * @param newTheta The new X offset as an absolute distance from the origin.
     * @hide Pending removal once nothing depends on it
     * @see #extendDuration(int)
     * @deprecated OverScroller's final position may change during an animation.
     * Instead of setting a new final position and extending
     * the duration of an existing scroll, use startScroll
     * to begin a new animation.
     */
    @Deprecated
    public void setFinalTheta(float newTheta) {
        mScrollerTheta.setFinalPosition(newTheta);
    }

    /**
     * Call this when you want to know the new location. If it returns true, the
     * animation is not yet finished.
     */
    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }

        switch (mMode) {
            case SCROLL_MODE:
                long time = AnimationUtils.currentAnimationTimeMillis();
                // Any scroller can be used for time, since they were started
                // together in scroll mode. We use X here.
                final long elapsedTime = time - mScrollerTheta.mStartTime;

                final int duration = mScrollerTheta.mDuration;
                if (elapsedTime < duration) {
                    final float q = mInterpolator.getInterpolation(elapsedTime / (float) duration);
                    mScrollerTheta.updateScroll(q);
                } else {
                    abortAnimation();
                }
                break;

            case FLING_MODE:
                if (!mScrollerTheta.mFinished) {
                    if (!mScrollerTheta.update()) {
                        if (!mScrollerTheta.continueWhenFinished()) {
                            mScrollerTheta.finish();
                        }
                    }
                }
                break;
        }

        return true;
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     * The scroll will use the default value of 250 milliseconds for the
     * duration.
     *
     * @param startTheta Starting angular scroll offset in pixels. Positive numbers will scroll the content counterclockwise.
     * @param dTheta     Angular distance to travel. Positive numbers will scroll the content counterclockwise.
     */
    public void startScroll(float startTheta, float dTheta) {
        startScroll(startTheta, dTheta, DEFAULT_DURATION);
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     *
     * @param startTheta Starting angular scroll offset in pixels. Positive numbers will scroll the content counterclockwise.
     * @param dTheta     Angular distance to travel. Positive numbers will scroll the content counterclockwise.
     * @param duration   Duration of the scroll in milliseconds.
     */
    public void startScroll(float startTheta, float dTheta, int duration) {
        mMode = SCROLL_MODE;
        mScrollerTheta.startScroll(startTheta, dTheta, duration);
    }

    /**
     * Call this when you want to 'spring back' into a valid coordinate range.
     *
     * @param startTheta Starting angle
     * @param minTheta   Minimum valid angle
     * @param maxTheta   Minimum valid angle
     * @return true if a springback was initiated, false if startX and startY were
     * already within the valid range.
     */
    public boolean springBack(float startTheta, float minTheta, float maxTheta) {
        mMode = FLING_MODE;
        return mScrollerTheta.springback(startTheta, minTheta, maxTheta);
    }

    public void fling(float startTheta, float velocityTheta, float minTheta, float maxTheta) {
        fling(startTheta, velocityTheta, minTheta, maxTheta, 0);
    }

    /**
     * Start scrolling based on a fling gesture. The distance traveled will
     * depend on the initial velocity of the fling.
     *
     * @param startTheta    Starting point of the scroll (X)
     * @param velocityTheta Initial velocity of the fling (X) measured in pixels per
     *                      second.
     * @param minTheta      Minimum X value. The scroller will not scroll past this point
     *                      unless overX > 0. If overfling is allowed, it will use minX as
     *                      a springback boundary.
     * @param maxTheta      Maximum X value. The scroller will not scroll past this point
     *                      unless overX > 0. If overfling is allowed, it will use maxX as
     *                      a springback boundary.
     * @param overTheta     Overfling range. If > 0, horizontal overfling in either
     *                      direction will be possible.
     */
    public void fling(float startTheta, float velocityTheta, float minTheta, float maxTheta, float overTheta) {
        // Continue a scroll or fling in progress
        if (mFlywheel && !isFinished()) {
            float oldVelocityTheta = mScrollerTheta.mCurrVelocity;
            if (Math.signum(velocityTheta) == Math.signum(oldVelocityTheta)) {
                velocityTheta += oldVelocityTheta;
            }
        }

        mMode = FLING_MODE;
        mScrollerTheta.fling(startTheta, velocityTheta, minTheta, maxTheta, overTheta);
    }

    /**
     * Notify the scroller that we've reached a horizontal boundary.
     * Normally the information to handle this will already be known
     * when the animation is started, such as in a call to one of the
     * fling functions. However there are cases where this cannot be known
     * in advance. This function will transition the current motion and
     * animate from startX to finalX as appropriate.
     *
     * @param startTheta Starting/current X position
     * @param finalTheta Desired final X position
     * @param overTheta  Magnitude of overscroll allowed. This should be the maximum
     *                   desired distance from finalX. Absolute value - must be positive.
     */
    public void notifyEdgeReached(float startTheta, float finalTheta, float overTheta) {
        mScrollerTheta.notifyEdgeReached(startTheta, finalTheta, overTheta);
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
        return !mScrollerTheta.mFinished && mScrollerTheta.mState != SplineOverScroller.SPLINE;
    }

    /**
     * Stops the animation. Contrary to {@link #forceFinished(boolean)},
     * aborting the animating causes the scroller to move to the final x and y
     * positions.
     *
     * @see #forceFinished(boolean)
     */
    public void abortAnimation() {
        mScrollerTheta.finish();
    }

    static class SplineOverScroller {
        // Initial position
        private float mStart;

        // Current position
        private float mCurrentPosition;

        // Final position
        private float mFinal;

        // Initial velocity
        private float mVelocity;

        // Current velocity
        private float mCurrVelocity;

        // Constant current deceleration
        private float mDeceleration;

        // Animation starting time, in system milliseconds
        private long mStartTime;

        // Animation duration, in milliseconds
        private int mDuration;

        // Duration to complete spline component of animation
        private int mSplineDuration;

        // Distance to travel along spline animation
        private float mSplineDistance;

        // Whether the animation is currently in progress
        private boolean mFinished;

        // The allowed overshot distance before boundary is reached.
        private float mOver;

        // Fling friction
        private float mFlingFriction = ViewConfiguration.getScrollFriction();

        // Current state of the animation.
        private int mState = SPLINE;

        // Constant gravity value, used in the deceleration phase.
        private static final float GRAVITY = 2000.0f;

        // A context-specific coefficient adjusted to physical values.
        private float mPhysicalCoeff;

        private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
        private static final float START_TENSION = 0.5f;
        private static final float END_TENSION = 1.0f;
        private static final float P1 = START_TENSION * INFLEXION;
        private static final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

        private static final int NB_SAMPLES = 100;
        private static final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];
        private static final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];

        private static final int SPLINE = 0;
        private static final int CUBIC = 1;
        private static final int BALLISTIC = 2;

        static {
            float x_min = 0.0f;
            float y_min = 0.0f;
            for (int i = 0; i < NB_SAMPLES; i++) {
                final float alpha = (float) i / NB_SAMPLES;

                float x_max = 1.0f;
                float x, tx, coef;
                while (true) {
                    x = x_min + (x_max - x_min) / 2.0f;
                    coef = 3.0f * x * (1.0f - x);
                    tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x;
                    if (Math.abs(tx - alpha) < 1E-5) break;
                    if (tx > alpha) x_max = x;
                    else x_min = x;
                }
                SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x;

                float y_max = 1.0f;
                float y, dy;
                while (true) {
                    y = y_min + (y_max - y_min) / 2.0f;
                    coef = 3.0f * y * (1.0f - y);
                    dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y;
                    if (Math.abs(dy - alpha) < 1E-5) break;
                    if (dy > alpha) y_max = y;
                    else y_min = y;
                }
                SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y;
            }
            SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES] = 1.0f;
        }

        void setFriction(float friction) {
            mFlingFriction = friction;
        }

        SplineOverScroller(Context context) {
            mFinished = true;
            final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
            mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
                    * 39.37f // inch/meter
                    * ppi
                    * 0.84f; // look and feel tuning
        }

        void updateScroll(float q) {
            mCurrentPosition = mStart + Math.round(q * (mFinal - mStart));
        }

        /*
         * Get a signed deceleration that will reduce the velocity.
         */
        static private float getDeceleration(float velocity) {
            return velocity > 0 ? -GRAVITY : GRAVITY;
        }

        /*
         * Modifies mDuration to the duration it takes to get from start to newFinal using the
         * spline interpolation. The previous duration was needed to get to oldFinal.
         */
        private void adjustDuration(float start, float oldFinal, float newFinal) {
            final float oldDistance = oldFinal - start;
            final float newDistance = newFinal - start;
            final float x = Math.abs(newDistance / oldDistance);
            final int index = (int) (NB_SAMPLES * x);
            if (index < NB_SAMPLES) {
                final float x_inf = (float) index / NB_SAMPLES;
                final float x_sup = (float) (index + 1) / NB_SAMPLES;
                final float t_inf = SPLINE_TIME[index];
                final float t_sup = SPLINE_TIME[index + 1];
                final float timeCoef = t_inf + (x - x_inf) / (x_sup - x_inf) * (t_sup - t_inf);
                mDuration *= timeCoef;
            }
        }

        void startScroll(float start, float distance, int duration) {
            mFinished = false;

            mCurrentPosition = mStart = start;
            mFinal = start + distance;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = duration;

            // Unused
            mDeceleration = 0.0f;
            mVelocity = 0;
        }

        void finish() {
            mCurrentPosition = mFinal;
            // Not reset since WebView relies on this value for fast fling.
            // TODO: restore when WebView uses the fast fling implemented in this class.
            // mCurrVelocity = 0.0f;
            mFinished = true;
        }

        void setFinalPosition(float position) {
            mFinal = position;
            mFinished = false;
        }

        void extendDuration(int extend) {
            final long time = AnimationUtils.currentAnimationTimeMillis();
            final int elapsedTime = (int) (time - mStartTime);
            mDuration = elapsedTime + extend;
            mFinished = false;
        }

        boolean springback(float start, float min, float max) {
            mFinished = true;

            mCurrentPosition = mStart = mFinal = start;
            mVelocity = 0;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = 0;

            if (start < min) {
                startSpringback(start, min, 0);
            } else if (start > max) {
                startSpringback(start, max, 0);
            }

            return !mFinished;
        }

        private void startSpringback(float start, float end, float velocity) {
            // mStartTime has been set
            mFinished = false;
            mState = CUBIC;
            mCurrentPosition = mStart = start;
            mFinal = end;
            final float delta = start - end;
            mDeceleration = getDeceleration(delta);
            // TODO take velocity into account
            mVelocity = -delta; // only sign is used
            mOver = Math.abs(delta);
            mDuration = (int) (1000.0 * Math.sqrt(-2.0 * delta / mDeceleration));
        }

        void fling(float start, float velocity, float min, float max, float over) {
            mOver = over;
            mFinished = false;
            mCurrVelocity = mVelocity = velocity;
            mDuration = mSplineDuration = 0;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mCurrentPosition = mStart = start;

            if (start > max || start < min) {
                startAfterEdge(start, min, max, velocity);
                return;
            }

            mState = SPLINE;
            double totalDistance = 0.0;

            if (velocity != 0) {
                mDuration = mSplineDuration = getSplineFlingDuration(velocity);
                totalDistance = getSplineFlingDistance(velocity);
            }

            mSplineDistance = (int) (totalDistance * Math.signum(velocity));
            mFinal = start + mSplineDistance;

            // Clamp to a valid final position
            if (mFinal < min) {
                adjustDuration(mStart, mFinal, min);
                mFinal = min;
            }

            if (mFinal > max) {
                adjustDuration(mStart, mFinal, max);
                mFinal = max;
            }
        }

        private double getSplineDeceleration(float velocity) {
            return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
        }

        private double getSplineFlingDistance(float velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
        }

        /* Returns the duration, expressed in milliseconds */
        private int getSplineFlingDuration(float velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return (int) (1000.0 * Math.exp(l / decelMinusOne));
        }

        private void fitOnBounceCurve(float start, float end, float velocity) {
            // Simulate a bounce that started from edge
            final float durationToApex = - velocity / mDeceleration;
            // The float cast below is necessary to avoid integer overflow.
            final float velocitySquared = velocity * velocity;
            final float distanceToApex = velocitySquared / 2.0f / Math.abs(mDeceleration);
            final float distanceToEdge = Math.abs(end - start);
            final float totalDuration = (float) Math.sqrt(
                    2.0 * (distanceToApex + distanceToEdge) / Math.abs(mDeceleration));
            mStartTime -= (int) (1000.0f * (totalDuration - durationToApex));
            mCurrentPosition = mStart = end;
            mVelocity = (int) (- mDeceleration * totalDuration);
        }

        private void startBounceAfterEdge(float start, float end, float velocity) {
            mDeceleration = getDeceleration(velocity == 0 ? start - end : velocity);
            fitOnBounceCurve(start, end, velocity);
            onEdgeReached();
        }

        private void startAfterEdge(float start, float min, float max, float velocity) {
            if (start > min && start < max) {
                Log.e("OverScroller", "startAfterEdge called from a valid position");
                mFinished = true;
                return;
            }
            final boolean positive = start > max;
            final float edge = positive ? max : min;
            final float overDistance = start - edge;
            boolean keepIncreasing = overDistance * velocity >= 0;
            if (keepIncreasing) {
                // Will result in a bounce or a to_boundary depending on velocity.
                startBounceAfterEdge(start, edge, velocity);
            } else {
                final double totalDistance = getSplineFlingDistance(velocity);
                if (totalDistance > Math.abs(overDistance)) {
                    fling(start, velocity, positive ? min : start, positive ? start : max, mOver);
                } else {
                    startSpringback(start, edge, velocity);
                }
            }
        }

        void notifyEdgeReached(float start, float end, float over) {
            // mState is used to detect successive notifications
            if (mState == SPLINE) {
                mOver = over;
                mStartTime = AnimationUtils.currentAnimationTimeMillis();
                // We were in fling/scroll mode before: current velocity is such that distance to
                // edge is increasing. This ensures that startAfterEdge will not start a new fling.
                startAfterEdge(start, end, end, (int) mCurrVelocity);
            }
        }

        private void onEdgeReached() {
            // mStart, mVelocity and mStartTime were adjusted to their values when edge was reached.
            // The float cast below is necessary to avoid integer overflow.
            final float velocitySquared = (float) mVelocity * mVelocity;
            float distance = velocitySquared / (2.0f * Math.abs(mDeceleration));
            final float sign = Math.signum(mVelocity);

            if (distance > mOver) {
                // Default deceleration is not sufficient to slow us down before boundary
                mDeceleration = - sign * velocitySquared / (2.0f * mOver);
                distance = mOver;
            }

            mOver = (int) distance;
            mState = BALLISTIC;
            mFinal = mStart + (int) (mVelocity > 0 ? distance : -distance);
            mDuration = - (int) (1000.0f * mVelocity / mDeceleration);
        }

        boolean continueWhenFinished() {
            switch (mState) {
                case SPLINE:
                    // Duration from start to null velocity
                    if (mDuration < mSplineDuration) {
                        // If the animation was clamped, we reached the edge
                        mCurrentPosition = mStart = mFinal;
                        // TODO Better compute speed when edge was reached
                        mVelocity = (int) mCurrVelocity;
                        mDeceleration = getDeceleration(mVelocity);
                        mStartTime += mDuration;
                        onEdgeReached();
                    } else {
                        // Normal stop, no need to continue
                        return false;
                    }
                    break;
                case BALLISTIC:
                    mStartTime += mDuration;
                    startSpringback(mFinal, mStart, 0);
                    break;
                case CUBIC:
                    return false;
            }

            update();
            return true;
        }

        /*
         * Update the current position and velocity for current time. Returns
         * true if update has been done and false if animation duration has been
         * reached.
         */
        boolean update() {
            final long time = AnimationUtils.currentAnimationTimeMillis();
            final long currentTime = time - mStartTime;

            if (currentTime == 0) {
                // Skip work but report that we're still going if we have a nonzero duration.
                return mDuration > 0;
            }
            if (currentTime > mDuration) {
                return false;
            }

            double distance = 0.0;
            switch (mState) {
                case SPLINE: {
                    final float t = (float) currentTime / mSplineDuration;
                    final int index = (int) (NB_SAMPLES * t);
                    float distanceCoef = 1.f;
                    float velocityCoef = 0.f;
                    if (index < NB_SAMPLES) {
                        final float t_inf = (float) index / NB_SAMPLES;
                        final float t_sup = (float) (index + 1) / NB_SAMPLES;
                        final float d_inf = SPLINE_POSITION[index];
                        final float d_sup = SPLINE_POSITION[index + 1];
                        velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                        distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                    }

                    distance = distanceCoef * mSplineDistance;
                    mCurrVelocity = velocityCoef * mSplineDistance / mSplineDuration * 1000.0f;
                    break;
                }

                case BALLISTIC: {
                    final float t = currentTime / 1000.0f;
                    mCurrVelocity = mVelocity + mDeceleration * t;
                    distance = mVelocity * t + mDeceleration * t * t / 2.0f;
                    break;
                }

                case CUBIC: {
                    final float t = (float) (currentTime) / mDuration;
                    final float t2 = t * t;
                    final float sign = Math.signum(mVelocity);
                    distance = sign * mOver * (3.0f * t2 - 2.0f * t * t2);
                    mCurrVelocity = sign * mOver * 6.0f * (- t + t2);
                    break;
                }
            }

            mCurrentPosition = mStart + (int) Math.round(distance);

            return true;
        }
    }

    // FIXME: find a way to instantiate android.widget.Scroller.ViscousFluidInterpolator maybe?
    static class ViscousFluidInterpolator implements Interpolator {
        /**
         * Controls the viscous fluid effect (how much of it).
         */
        private static final float VISCOUS_FLUID_SCALE = 8.0f;

        private static final float VISCOUS_FLUID_NORMALIZE;
        private static final float VISCOUS_FLUID_OFFSET;

        static {
            // must be set to 1.0 (used in viscousFluid())
            VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f);
            // account for very small floating-point error
            VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f);
        }

        private static float viscousFluid(float x) {
            x *= VISCOUS_FLUID_SCALE;
            if (x < 1.0f) {
                x -= (1.0f - (float) Math.exp(-x));
            } else {
                float start = 0.36787944117f;   // 1/e == exp(-1)
                x = 1.0f - (float) Math.exp(1.0f - x);
                x = start + x * (1.0f - start);
            }
            return x;
        }

        @Override
        public float getInterpolation(float input) {
            final float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
            if (interpolated > 0) {
                return interpolated + VISCOUS_FLUID_OFFSET;
            }
            return interpolated;
        }
    }
}
