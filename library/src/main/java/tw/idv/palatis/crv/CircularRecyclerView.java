package tw.idv.palatis.crv;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.ACircularRecyclerView;
import android.util.AttributeSet;

public class CircularRecyclerView extends ACircularRecyclerView {
    public CircularRecyclerView(Context context) {
        this(context, null);
    }

    public CircularRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
