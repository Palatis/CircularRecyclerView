package tw.idv.palatis.crv;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.ACircularRecyclerView;
import android.util.AttributeSet;

/**
 * Created by Palatis on 2017/4/18.
 */

public class CircularRecyclerView extends ACircularRecyclerView {
    public CircularRecyclerView(Context context) {
        super(context);
    }

    public CircularRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
