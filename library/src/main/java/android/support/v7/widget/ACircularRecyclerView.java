package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.os.TraceCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.widget.CircularScroller;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ACircularRecyclerView extends ARecyclerView {
    private static final String TAG = "ACircularRecyclerView";

    public ACircularRecyclerView(Context context) {
        this(context, null);
    }

    public ACircularRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ACircularRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        _setViewFlinger(new AngularViewFlinger());
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
    }

    @Override
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
    }

    public CircularLayoutManager getCircularLayoutManager() {
        final LayoutManager layout = getLayoutManager();
        if (layout != null && layout instanceof CircularLayoutManager)
            return (CircularLayoutManager) layout;
        return null;
    }

    public void scrollBy(float dTheta) {
        final CircularLayoutManager layout = getCircularLayoutManager();
        if (layout == null) {
            Log.e(TAG, "Cannot scroll without a CircularLayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        if (isLayoutFrozen()) {
            return;
        }
        if (layout.canScrollCircularly())
            layout.scrollCircularlyBy(dTheta, mRecycler, mState);
    }

    private float mLastTouchTheta;
    private MotionEvent mVtev;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLayoutFrozen())
            return false;

        if (_dispatchOnItemTouch(event)) {
            _cancelTouch();
            return true;
        }

        final CircularLayoutManager layout = getCircularLayoutManager();
        if (layout == null || !layout.canScrollCircularly())
            return super.onTouchEvent(event);

        final float layoutCenterX = layout.getLayoutCenterX();
        final float layoutCenterY = layout.getLayoutCenterY();

        VelocityTracker tracker = _getVelocityTracker();
        if (tracker == null)
            _setVelocityTracker(tracker = VelocityTracker.obtain());

        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                _setScrollPointerId(event.getPointerId(0));
                mLastTouchTheta = (float) Math.atan2(-event.getY() + layoutCenterY, event.getX() - layoutCenterX);
                mVtev = MotionEvent.obtain(event);
                mVtev.setLocation(0, 0);
                tracker.addMovement(mVtev);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float theta = (float) Math.atan2(-event.getY() + layoutCenterY, event.getX() - layoutCenterX);
                float dTheta = mLastTouchTheta - theta;
                if (dTheta > Math.PI)
                    dTheta -= Math.PI * 2;
                if (dTheta < -Math.PI)
                    dTheta += Math.PI * 2;

                if (layout.scrollCircularlyBy(dTheta, mRecycler, mState) != 0)
                    getParent().requestDisallowInterceptTouchEvent(true);

                // forge linear motion into angular motion
                final MotionEvent vtev = MotionEvent.obtain(event);
                if (mVtev != null) {
                    vtev.setLocation(0, mVtev.getY() + layout.getLayoutRadius() * dTheta);
                    mVtev.recycle();
                } else {
                    vtev.setLocation(0, event.getY() + layout.getLayoutRadius() * dTheta);
                }
                tracker.addMovement(vtev);
                mVtev = vtev;

                mLastTouchTheta = theta;
                setScrollState(SCROLL_STATE_DRAGGING);
                break;
            }
            case MotionEvent.ACTION_UP: {
                tracker.computeCurrentVelocity(1000, getMaxFlingVelocity());
                final float yvel = -VelocityTrackerCompat.getYVelocity(tracker, _getScrollPointerId());
                if (!(yvel != 0 && fling(yvel)))
                    setScrollState(SCROLL_STATE_IDLE);
                _resetTouch();
                mVtev.recycle();
                mVtev = null;
                tracker.recycle();
                _setVelocityTracker(null);
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                _cancelTouch();
                tracker.recycle();
                _setVelocityTracker(null);
            }
            break;
        }

        return true;
    }

    public boolean fling(float velocityAngular) {
        final CircularLayoutManager layout = getCircularLayoutManager();
        if (layout == null) {
            Log.e(TAG, "Cannot fling angular without a CircularLayoutManager set.");
            return false;
        }

        if (isLayoutFrozen())
            return false;
        if (!layout.canScrollCircularly())
            return false;

        final float radius = layout.getLayoutRadius();
        velocityAngular /= radius;
        final float minAngularVelocity = getMinFlingVelocity() / radius;
        final float maxAngularVelocity = getMaxFlingVelocity() / radius;

        if (Math.abs(velocityAngular) < minAngularVelocity)
            return false;

        final RecyclerView.OnFlingListener listener = getOnFlingListener();
        if (listener != null && listener instanceof OnFlingListener && ((OnFlingListener) listener).onFling(velocityAngular))
            return true;

        if (mViewFlinger instanceof AngularViewFlinger)
            ((AngularViewFlinger) mViewFlinger).fling(Math.signum(velocityAngular) * Math.max(minAngularVelocity, Math.min(Math.abs(velocityAngular), maxAngularVelocity)));
        return true;
    }

    public abstract static class OnFlingListener extends RecyclerView.OnFlingListener {
        /**
         * Override this to handle a fling given the angular velocity.
         * Note that this method will only be called if the associated {@link LayoutManager}
         * supports scrolling and the fling is not handled by nested scrolls first.
         *
         * @param velocityAngular the fling velocity counterclockwise
         * @return true if the fling was handled, false otherwise.
         */
        abstract boolean onFling(float velocityAngular);
    }

    private class AngularViewFlinger extends ViewFlinger {
        private final CircularScroller mCircularScroller = new CircularScroller(getContext(), sQuinticInterpolator);
        private float mLastFlingTheta = Float.NaN;

        @Override
        public void run() {
            if (Float.isNaN(mLastFlingTheta)) {
                super.run();
                return;
            }

            if (mLayout == null || !(mLayout instanceof CircularLayoutManager)) {
                stop();
                return; // no layout, cannot scroll.
            }
            Log.d(TAG, "AngularViewFlinger.run()");

            final CircularLayoutManager layout = (CircularLayoutManager) mLayout;
            _disableRunOnAnimationRequests();
            consumePendingUpdateOperations();
            // keep a local reference so that if it is changed during onAnimation method, it won't
            // cause unexpected behaviors
            final CircularScroller scroller = mCircularScroller;
            final SmoothScroller smoothScroller = mLayout.mSmoothScroller;
            if (scroller.computeScrollOffset()) {
                final float theta = scroller.getCurrTheta();
                float dTheta = theta - mLastFlingTheta;
                float thetaResult = 0;
                mLastFlingTheta = theta;
                float overscrollTheta = 0;

                if (mAdapter != null) {
                    eatRequestLayout();
                    onEnterLayoutOrScroll();
                    TraceCompat.beginSection(TRACE_SCROLL_TAG);
                    if (dTheta != 0) {
                        thetaResult = layout.scrollCircularlyBy(dTheta / layout.getLayoutRadius(), mRecycler, mState) * layout.getLayoutRadius();
                        overscrollTheta = dTheta - thetaResult;
                    }
                    TraceCompat.endSection();
                    repositionShadowingViews();

                    onExitLayoutOrScroll();
                    resumeRequestLayout(false);

//                    if (smoothScroller != null && !smoothScroller.isPendingInitialRun() &&
//                            smoothScroller.isRunning()) {
//                        final int adapterSize = mState.getItemCount();
//                        if (adapterSize == 0) {
//                            smoothScroller.stop();
//                        } else if (smoothScroller.getTargetPosition() >= adapterSize) {
//                            smoothScroller.setTargetPosition(adapterSize - 1);
//                            smoothScroller.onAnimation(dx - overscrollX, dy - overscrollY);
//                        } else {
//                            smoothScroller.onAnimation(dx - overscrollX, dy - overscrollY);
//                        }
//                    }
                }
                if (!mItemDecorations.isEmpty()) {
                    invalidate();
                }
//                if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
//                    considerReleasingGlowsOnScroll(dx, dy);
//                }
                if (overscrollTheta != 0) {
                    final float vel = scroller.getCurrVelocity();
                    float velTheta = 0;
                    if (overscrollTheta != theta) {
                        velTheta = overscrollTheta < 0 ? -vel : overscrollTheta > 0 ? vel : 0;
                    }
//                    if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
//                        absorbGlows(velX, velY);
//                    }
                    if (velTheta != 0 || overscrollTheta == theta || scroller.getFinalTheta() == 0)
                        scroller.abortAnimation();
                }
//                if (thetaResult != 0)
//                    dispatchOnScrolled(thetaResult);

                if (!awakenScrollBars()) {
                    invalidate();
                }

                final boolean fullyConsumed = dTheta != 0 && layout.canScrollCircularly() && thetaResult == dTheta;

                if (scroller.isFinished() || !fullyConsumed) {
                    mLastFlingTheta = Float.NaN;
                    setScrollState(SCROLL_STATE_IDLE); // setting state to idle will stop this.
//                    if (ALLOW_THREAD_GAP_WORK) {
//                        mPrefetchRegistry.clearPrefetchPositions();
//                    }
                } else {
                    postOnAnimation();
//                    if (mGapWorker != null) {
//                        mGapWorker.postFromTraversal(RecyclerView.this, dx, dy);
//                    }
                }
            }
//            // call this after the onAnimation is complete not to have inconsistent callbacks etc.
//            if (smoothScroller != null) {
//                if (smoothScroller.isPendingInitialRun()) {
//                    smoothScroller.onAnimation(0, 0);
//                }
//                if (!mReSchedulePostAnimationCallback) {
//                    smoothScroller.stop(); //stop if it does not trigger any scroll
//                }
//                }
//            }
            _enableRunOnAnimationRequests();
        }

        private void _enableRunOnAnimationRequests() {
            try {
                final Method method = ViewFlinger.class.getDeclaredMethod("enableRunOnAnimationRequests");
                method.setAccessible(true);
                method.invoke(this);
            } catch (NoSuchMethodException ex) {
                Log.d(TAG, "_enableRunOnAnimationRequests(): problem getting enableRunOnAnimationRequests()", ex);
            } catch (InvocationTargetException ex) {
                Log.d(TAG, "_enableRunOnAnimationRequests(): problem getting enableRunOnAnimationRequests()", ex);
            } catch (IllegalAccessException ex) {
                Log.d(TAG, "_enableRunOnAnimationRequests(): problem getting enableRunOnAnimationRequests()", ex);
            }
        }

        private void _disableRunOnAnimationRequests() {
            try {
                final Method method = ViewFlinger.class.getDeclaredMethod("disableRunOnAnimationRequests");
                method.setAccessible(true);
                method.invoke(this);
            } catch (NoSuchMethodException ex) {
                Log.d(TAG, "_disableRunOnAnimationRequests(): problem getting disableRunOnAnimationRequests()", ex);
            } catch (InvocationTargetException ex) {
                Log.d(TAG, "_disableRunOnAnimationRequests(): problem getting disableRunOnAnimationRequests()", ex);
            } catch (IllegalAccessException ex) {
                Log.d(TAG, "_disableRunOnAnimationRequests(): problem getting disableRunOnAnimationRequests()", ex);
            }
        }

        @Override
        public void stop() {
            mLastFlingTheta = Float.NaN;
            super.stop();
        }

        void fling(float velocityAngular) {
            if (mLayout instanceof CircularLayoutManager) {
                Log.d(TAG, "fling!!! velocity = " + velocityAngular);
                setScrollState(SCROLL_STATE_SETTLING);
                mLastFlingTheta = 0;
                mCircularScroller.fling(0, velocityAngular * ((CircularLayoutManager) mLayout).getLayoutRadius(), Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
                postOnAnimation();
            }
        }
    }
}
