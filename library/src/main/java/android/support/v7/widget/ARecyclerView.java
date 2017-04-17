package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This class is here to expose private methods and fields to package access
 */
public abstract class ARecyclerView extends RecyclerView {
    private static final String TAG = "ARecyclerView";

    public ARecyclerView(Context context) {
        this(context, null);
    }

    public ARecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ARecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void _setScrollPointerId(int id) {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mScrollPointerId");
            field.setAccessible(true);
            field.set(this, id);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_setScrollPointerId(): problem getting mScrollPointerId", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_setScrollPointerId(): problem getting mScrollPointerId", ex);
        }
    }

    int _getScrollPointerId() {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mScrollPointerId");
            field.setAccessible(true);
            return field.getInt(this);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_setScrollPointerId(): problem getting mScrollPointerId", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_setScrollPointerId(): problem getting mScrollPointerId", ex);
        }
        return -1;
    }

    void _setViewFlinger(ViewFlinger flinger) {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mViewFlinger");
            field.setAccessible(true);
            field.set(this, flinger);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_setViewFlinger(): problem getting mViewFlinger", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_setViewFlinger(): problem getting mViewFlinger", ex);
        }
    }

    VelocityTracker _getVelocityTracker() {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mVelocityTracker");
            field.setAccessible(true);
            return (VelocityTracker) field.get(this);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_getVelocityTracker(): problem getting mVelocityTracker", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_getVelocityTracker(): problem getting mVelocityTracker", ex);
        }
        return null;
    }

    void _setVelocityTracker(VelocityTracker tracker) {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mVelocityTracker");
            field.setAccessible(true);
            field.set(this, tracker);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_setVelocityTracker(): problem getting mVelocityTracker", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_setVelocityTracker(): problem getting mVelocityTracker", ex);
        }
    }

    void _setDispatchScrollCounter(int count) {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mDispatchScrollCounter");
            field.setAccessible(true);
            field.setInt(this, count);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_setDispatchScrollCounter(): problem getting mDispatchScrollCounter", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_setDispatchScrollCounter(): problem getting mDispatchScrollCounter", ex);
        }
    }

    int _getDispatchScrollCounter() {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mDispatchScrollCounter");
            field.setAccessible(true);
            return field.getInt(this);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_getDispatchScrollCounter(): problem getting mDispatchScrollCounter", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_getDispatchScrollCounter(): problem getting mDispatchScrollCounter", ex);
        }
        return 0;
    }

    RecyclerView.OnScrollListener _getOnScrollListener() {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mScrollListener");
            field.setAccessible(true);
            return (OnScrollListener) field.get(this);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_getOnScrollListener(): problem getting mScrollListener", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_getOnScrollListener(): problem getting mScrollListener", ex);
        }
        return null;
    }

    List<OnScrollListener> _getOnScrollListeners() {
        try {
            final Field field = RecyclerView.class.getDeclaredField("mScrollListeners");
            field.setAccessible(true);
            return (List<OnScrollListener>) field.get(this);
        } catch (NoSuchFieldException ex) {
            Log.d(TAG, "_getOnScrollListeners(): problem getting mScrollListeners", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_getOnScrollListeners(): problem getting mScrollListeners", ex);
        }
        return null;
    }

    boolean _dispatchOnItemTouch(MotionEvent e) {
        try {
            final Method method = RecyclerView.class.getDeclaredMethod("dispatchOnItemTouch", MotionEvent.class);
            method.setAccessible(true);
            return (boolean) method.invoke(this, e);
        } catch (NoSuchMethodException ex) {
            Log.d(TAG, "_dispatchOnItemTouch(): problem getting dispatchOnItemTouch()", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_dispatchOnItemTouch(): problem getting dispatchOnItemTouch()", ex);
        } catch (InvocationTargetException ex) {
            Log.d(TAG, "_dispatchOnItemTouch(): problem getting dispatchOnItemTouch()", ex);
        }
        return false;
    }

    void _resetTouch() {
        try {
            final Method method = RecyclerView.class.getDeclaredMethod("resetTouch");
            method.setAccessible(true);
            method.invoke(this);
        } catch (NoSuchMethodException ex) {
            Log.d(TAG, "_resetTouch(): problem getting resetTouch()", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_resetTouch(): problem getting resetTouch()", ex);
        } catch (InvocationTargetException ex) {
            Log.d(TAG, "_resetTouch(): problem getting resetTouch()", ex);
        }
    }

    void _cancelTouch() {
        try {
            final Method method = RecyclerView.class.getDeclaredMethod("cancelTouch");
            method.setAccessible(true);
            method.invoke(this);
        } catch (NoSuchMethodException ex) {
            Log.d(TAG, "_cancelTouch(): problem getting cancelTouch()", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_cancelTouch(): problem getting cancelTouch()", ex);
        } catch (InvocationTargetException ex) {
            Log.d(TAG, "_cancelTouch(): problem getting cancelTouch()", ex);
        }
    }

    void _consumePendingUpdateOperations() {
        try {
            final Method method = RecyclerView.class.getDeclaredMethod("consumePendingUpdateOperations");
            method.setAccessible(true);
            method.invoke(this);
        } catch (NoSuchMethodException ex) {
            Log.d(TAG, "_consumePendingUpdateOperations(): problem getting consumePendingUpdateOperations()", ex);
        } catch (IllegalAccessException ex) {
            Log.d(TAG, "_consumePendingUpdateOperations(): problem getting consumePendingUpdateOperations()", ex);
        } catch (InvocationTargetException ex) {
            Log.d(TAG, "_consumePendingUpdateOperations(): problem getting consumePendingUpdateOperations()", ex);
        }
    }
}
