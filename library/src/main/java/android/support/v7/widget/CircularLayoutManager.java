package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import tw.idv.palatis.crv.R;

public class CircularLayoutManager extends RecyclerView.LayoutManager implements
        RecyclerView.SmoothScroller.ScrollVectorProvider {
    private static final String TAG = "CircularLayoutManager";

    /**
     * the range for all children in radius, that's
     * - the full range of {@code mThetaSweep - mThetaStart}, if {@code getItemCount() < mNumDisplayChildren}
     * - {@code mItemTheta * getItemCount()}, otherwise.
     */
    private float mCircularRange;
    /**
     * the offset of the first visible child, in radius. start from 0.
     */
    private float mCircularOffset;

    private float mItemTheta;
    private float mThetaStart;
    private float mThetaSweep;

    private float mLayoutCenterX = 0;
    private float mLayoutCenterY = 0;
    private float mLayoutRadius;

    private int mNumDisplayChildren;
    private int mNumSkipChildren;

    public CircularLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularLayoutManager, defStyleAttr, defStyleRes);
        try {
            mThetaStart = (float) Math.toRadians(a.getFloat(R.styleable.CircularLayoutManager_clm_startAngle, 0f));
            mThetaSweep = (float) Math.toRadians(a.getFloat(R.styleable.CircularLayoutManager_clm_sweepAngle, 360f));
            mNumSkipChildren = a.getInteger(R.styleable.CircularLayoutManager_clm_numSkipChildren, 0);
            mNumDisplayChildren = a.getInteger(R.styleable.CircularLayoutManager_clm_numDisplayChildren, 8);
        } finally {
            a.recycle();
        }

        updateRanges();
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);

        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        final float cx = widthSize / 2;
        final float cy = heightSize / 2;
        final float r = Math.min(cx, cy) - Math.max(Math.max(getPaddingLeft(), getPaddingRight()), Math.max(getPaddingTop(), getPaddingBottom()));

        if (cx != mLayoutCenterX || cy != mLayoutCenterY || r != mLayoutRadius) {
            mLayoutCenterX = cx;
            mLayoutCenterY = cy;
            mLayoutRadius = r;
            requestLayout();
        }
    }

    public void setNumDisplayChildren(int children) {
        if (children < 2)
            Log.d(TAG, "setNumDisplayChildren(): children < 2, you'll probably get strange behavior.");

        if (children != mNumDisplayChildren) {
            mNumDisplayChildren = children;
            updateRanges();
        }
    }

    public int getNumDisplayChildren() {
        return mNumDisplayChildren;
    }

    public void setNumSkipChildren(int children) {
        if (children != mNumSkipChildren) {
            mNumSkipChildren = children;
            updateRanges();
        }
    }

    public int getNumSkipChildren() {
        return mNumSkipChildren;
    }

    /**
     * @param thetaStart the starting angle to layout the first child
     * @param thetaSweep the sweeping angle of the arc
     */
    public void setLayoutRange(float thetaStart, float thetaSweep) {
        if (mThetaStart != thetaStart || mThetaSweep != thetaSweep) {
            mThetaStart = thetaStart;
            mThetaSweep = thetaSweep;
            updateRanges();
        }
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        if (newAdapter != null)
            updateRanges();
    }

    @Override
    public void onItemsChanged(RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        updateRanges();
    }

    @Override
    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);
        updateRanges();
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount);
        updateRanges();
    }

    void updateRanges() {
        float oldTheta = mItemTheta;

        float ratio = mCircularOffset / Math.abs(mItemTheta);
        if (Float.isNaN(ratio))
            ratio = 0;
        mItemTheta = mThetaSweep;

        final int itemCount = getItemCount() + mNumSkipChildren;
        if (itemCount == 0)
            return;

        if (mNumDisplayChildren > 2)
            mItemTheta /= Math.min(itemCount, mNumDisplayChildren);

        float absItemTheta = Math.abs(mItemTheta);
        mCircularRange = absItemTheta * itemCount;
        mCircularOffset = ratio * absItemTheta;
        if (mCircularOffset > mCircularRange - absItemTheta)
            mCircularOffset = mCircularRange - absItemTheta;
        if (mCircularOffset < 0)
            mCircularOffset = 0;

        if (oldTheta != mItemTheta) {
            Log.d(TAG, "updateRanges(): child = " + mNumDisplayChildren + ", items = " + getItemCount() + ", theta = " + mItemTheta);
            this.requestLayout();
        }
    }

    public float getStartAngle() {
        return mThetaStart;
    }

    public float getSweepAngle() {
        return mThetaSweep;
    }

    public float getLayoutCenterX() {
        return mLayoutCenterX;
    }

    public float getLayoutCenterY() {
        return mLayoutCenterY;
    }

    public float getLayoutRadius() {
        return mLayoutRadius;
    }

    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private RecyclerView mRecyclerView;

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        mRecyclerView = view;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        removeAndRecycleAllViews(recycler);
        mRecyclerView = null;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        if (getWidth() == 0 || getHeight() == 0)
            return;
        if (state.getItemCount() == 0)
            return;

        if (mThetaSweep < 0) {
            throw new UnsupportedOperationException("sweep < 0 not implemented.");
        } else if (mThetaSweep > 0) {
            final int startIndex = (int) (mCircularOffset / mItemTheta);
            final float compensation = mCircularOffset - startIndex * mItemTheta;
            // lay one more child if we have to compensate
            final int count = mNumDisplayChildren + (compensation != 0 ? 1 : 0);
            for (int i = mNumSkipChildren; i < count; ++i) {
                final int childIndex = startIndex + i - mNumSkipChildren;
                if (childIndex >= state.getItemCount())
                    break;

                final float theta = mThetaStart - compensation + i * mItemTheta;
                final float childStartAngle = theta - mItemTheta / 2.0f;
                final float childEndAngle = theta + mItemTheta / 2.0f;

                float childOffset = 1.0f;
                if (childStartAngle < mThetaStart)
                    childOffset = (childEndAngle - mThetaStart) / mItemTheta;
                else if (childEndAngle > mThetaStart + mThetaSweep)
                    childOffset = (mThetaStart + mThetaSweep - childStartAngle) / mItemTheta;

                if (childOffset < 0.0f)
                    childOffset = 0.0f;
                else if (childOffset > 1.0f)
                    childOffset = 1.0f;

                final View child = recycler.getViewForPosition(childIndex);
                addView(child);
                final RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(child);
                if (holder instanceof ViewHolder)
                    ((ViewHolder) holder).setLayoutAngle(theta, childOffset);

                child.measure(View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(getHeight(), View.MeasureSpec.AT_MOST));
                layoutCircular(child, theta);
            }
        } else {
            throw new IllegalStateException("sweeping angle == 0!!!");
        }
    }

    public void layoutCircular(View child, final float theta) {
        final int childWidth = child.getMeasuredWidth();
        final int childHeight = child.getMeasuredHeight();
        final float childRadius = Math.min(childWidth, childHeight) / 2.0f;
        final float childCenterX = (float) (mLayoutCenterX + Math.cos(theta) * (mLayoutRadius - childRadius));
        final float childCenterY = (float) (mLayoutCenterY - Math.sin(theta) * (mLayoutRadius - childRadius));
        final int left = (int) (childCenterX - childWidth / 2);
        final int top = (int) (childCenterY - childHeight / 2);
        child.layout(left, top, left + childWidth, top + childHeight);
    }

    public static abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        /**
         * @param theta  the angle which current item is being laid-out
         * @param offset the offset for the child which it's before or after the sweep area
         */
        public abstract void setLayoutAngle(float theta, float offset);
    }

    public float scrollCircularlyBy(float dTheta, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dTheta == 0)
            return dTheta;

        float offset = mCircularOffset + dTheta;
        if (offset > mCircularRange - Math.abs(mThetaSweep)) {
            dTheta = offset - mCircularRange - Math.abs(mThetaSweep);
            offset = mCircularRange - Math.abs(mThetaSweep);
        }
        if (offset < 0) {
            dTheta = offset;
            offset = 0;
        }

        if (mCircularOffset != offset) {
            mCircularOffset = offset;
            Log.d(TAG, "scrollCircularlyBy(): dTheta = " + dTheta);
            requestLayout();
        }
        return dTheta;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return dx;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return dy;
    }

    public boolean canScrollCircularly() {
        return getItemCount() > mNumDisplayChildren - mNumSkipChildren;
    }

    /**
     * relayed to {@link #canScrollCircularly()}
     *
     * @return true if {@link #canScrollCircularly()}, false otherwise.
     */
    @Override
    public boolean canScrollHorizontally() {
        return canScrollCircularly();
    }

    /**
     * relayed to {@link #canScrollCircularly()}
     *
     * @return true if {@link #canScrollCircularly()}, false otherwise.
     */
    @Override
    public boolean canScrollVertically() {
        return canScrollCircularly();
    }

    public float computeCircularScrollExtent(RecyclerView.State state) {
        return getChildCount() == 0 ?
                0 :
                (mNumDisplayChildren - mNumSkipChildren) * mItemTheta;
    }

    public float computeCircularScrollOffset(RecyclerView.State state) {
        return getChildCount() == 0 ?
                0 :
                mCircularOffset;
    }

    public float computeCircularScrollRange(RecyclerView.State state) {
        return getChildCount() == 0 ?
                0 :
                mCircularRange;
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return 1;
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return 0;
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return 1;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return (int) computeCircularScrollExtent(state);
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return (int) computeCircularScrollOffset(state);
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return (int) computeCircularScrollRange(state);
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        return new PointF(0, 0);
    }
}
