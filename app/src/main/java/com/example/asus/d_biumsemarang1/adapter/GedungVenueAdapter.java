package com.example.asus.d_biumsemarang1.adapter;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.data.GedungVenue;
import com.example.asus.d_biumsemarang1.listener.ViewHolderListener;
import com.example.asus.d_biumsemarang1.utils.GlideApp;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by ASUS on 11/2/2017.
 */

public class GedungVenueAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String NOTIFIY_ADAPTER = "notify";
    private static final int ITEM_TYPE = 0;
    private static final int LOADING_TYPE = 1;
    private static final String TAG = GedungVenueAdapter.class.getSimpleName();
    private boolean needLoading;
    private List<GedungVenue> mData;
    private ViewHolderListener mListener;
    private Context mContext;

    public GedungVenueAdapter(List<GedungVenue> data, ViewHolderListener listener) {
        mData = data;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (viewType == ITEM_TYPE) {
            View container = LayoutInflater.from(mContext).inflate(R.layout.item_gedung_venue, parent, false);
            return new GedungVenueViewHolder(container);
        } else {
            View container = LayoutInflater.from(mContext).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(container);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GedungVenueViewHolder) {
            GedungVenueViewHolder temp = (GedungVenueViewHolder) holder;
            temp.nama.setText(mData.get(position).getNama());
            temp.alamat.setText(mData.get(position).getAlamat());
            if (mData.get(position).getBiaya() == 0) {
                temp.biaya.setText(mContext.getString(R.string.free));
            } else {
                temp.biaya.setText(mContext.getString(R.string.hour,
                        NumberFormat.getInstance().format(mData.get(position).getBiaya())));
            }
            String urlGambar = null;
            if (mData.get(position).getGambar() != null) {
                urlGambar = (String) mData.get(position).getGambar().get("P0");
            }
            temp.photo.getLayoutParams().height = getWidth() * 9 / 16;
            GlideApp.with(mContext).load(urlGambar).diskCacheStrategy(DiskCacheStrategy.RESOURCE).centerCrop().into(temp.photo);
        } else {
            showLoading((LoadingViewHolder) holder);
        }
    }

    private void showLoading(final LoadingViewHolder holder) {
        if (needLoading) {
            holder.mConnectionLinear2.setVisibility(View.GONE);
            holder.mProgress2.setVisibility(View.VISIBLE);
        } else {
            holder.mProgress2.setVisibility(View.GONE);
            holder.mConnectionLinear2.setVisibility(View.VISIBLE);
        }
    }

    private int getWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) == null ? LOADING_TYPE : ITEM_TYPE;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setNeedLoading(boolean needLoading) {
        this.needLoading = needLoading;
    }

    public class GedungVenueViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nama;
        private TextView alamat;
        private ImageView photo;
        private TextView biaya;

        public GedungVenueViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nama = itemView.findViewById(R.id.nama);
            alamat = itemView.findViewById(R.id.alamat);
            photo = itemView.findViewById(R.id.photo);
            biaya = itemView.findViewById(R.id.biaya);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(getAdapterPosition());
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgress2;
        private LinearLayout mConnectionLinear2;
        private Button mBtnCoba2;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            mBtnCoba2 = itemView.findViewById(R.id.mBtnCoba2);
            mProgress2 = itemView.findViewById(R.id.mProgress2);
            mProgress2.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.colorProgress), PorterDuff.Mode.SRC_ATOP);
            mConnectionLinear2 = itemView.findViewById(R.id.mConnectionLinear2);
            mBtnCoba2.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mConnectionLinear2.setVisibility(View.GONE);
            mProgress2.setVisibility(View.VISIBLE);
            mListener.fetchMoreData();
        }
    }

}
