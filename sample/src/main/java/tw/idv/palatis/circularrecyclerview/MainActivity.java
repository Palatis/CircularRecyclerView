package tw.idv.palatis.circularrecyclerview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CircularLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tw.idv.palatis.crv.CircularRecyclerView;
import tw.idv.palatis.crv.utils.RadianUtils;

public class MainActivity extends Activity {
    private CircularRecyclerView mRecyclerView;
    private ItemAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (CircularRecyclerView) findViewById(R.id.crv);
        mRecyclerView.setAdapter(mAdapter = new ItemAdapter());
    }

    private static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crv, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text1.setText(String.format("pos = %d", position));
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        static class ViewHolder extends CircularLayoutManager.ViewHolder {
            public TextView text1;
            public TextView text2;

            public ViewHolder(View itemView) {
                super(itemView);
                text1 = (TextView) itemView.findViewById(android.R.id.text1);
                text2 = (TextView) itemView.findViewById(android.R.id.text2);
            }

            @Override
            public void setLayoutAngle(float theta, float offset) {
                Log.d("CRV", "setLayoutAngle(): theta = " + theta + " offset = " + offset + " pos = " + getAdapterPosition());
                text2.setText(String.format("%.2f", RadianUtils.degreeFromRadian(theta)));
                itemView.setScaleX(offset);
                itemView.setScaleY(offset);
                itemView.setAlpha(offset);
            }
        }
    }
}
