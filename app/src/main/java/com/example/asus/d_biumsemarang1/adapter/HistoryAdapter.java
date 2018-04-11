package com.example.asus.d_biumsemarang1.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.data.Sewa;
import com.example.asus.d_biumsemarang1.listener.ViewHolderListener;
import com.example.asus.d_biumsemarang1.utils.TimeUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ASUS on 11/18/2017.
 */

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String NOTIFIY_ADAPTER = "notify2";
    private static final String TAG = GedungVenueAdapter.class.getSimpleName();
    private static final int ITEM_TYPE = 0;
    private static final int LOADING_TYPE = 1;
    public static final  int NOT_YET = 0;
    public static final  int VERIFIED = 1;
    public static final  int REJECTED = 2;

    private List<Sewa> mData;
    private ViewHolderListener mListener;
    private Context mContext;
    private boolean needLoading;

    public HistoryAdapter(List<Sewa> data, ViewHolderListener listener){
        mData = data;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (viewType == ITEM_TYPE) {
            View container = LayoutInflater.from(mContext).inflate(R.layout.item_gedungvenuehistory, parent, false);
            return new HistoryViewHolder(container);
        } else {
            View container = LayoutInflater.from(mContext).inflate(R.layout.item_loadingv2, parent, false);
            return new LoadingViewHolder(container);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HistoryViewHolder){
            setHistoryViewHolder((HistoryViewHolder) holder, position);
        }
        else {
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

    private void setHistoryViewHolder(final HistoryViewHolder holder, int position){
        int color = 0;
        String status = null;
        switch ((int) mData.get(position).getStatus()){
            case NOT_YET :
                color = ContextCompat.getColor(mContext, R.color.color_notYet);
                status = " Belum diverfikasi";
                break;
            case VERIFIED:
                color = ContextCompat.getColor(mContext, R.color.color_verified);
                status = " Terverifikasi";
                break;
            case REJECTED:
                color = ContextCompat.getColor(mContext, R.color.color_rejected);
                status = " Ditolak";
                break;
        }
        holder.mStatusView.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        holder.nama.setText(mData.get(position).getStatus_inf().substring(2));
        holder.alamat.setText(mData.get(position).getAlamat());
        holder.judulStatus.setText(mContext.getString(R.string.status, status != null ? status.toUpperCase() : " -"));
        long wktpinjam = mData.get(position).getWaktu_pinjam();
        long wktkembali = mData.get(position).getWaktu_kembali();
        long hourpinjam = TimeUnit.MILLISECONDS.toHours(wktpinjam);
        long hourkembali = TimeUnit.MILLISECONDS.toHours(wktkembali);
        StringBuilder pinjam = new StringBuilder();
        pinjam.append(TimeUtils.getStringDate(wktpinjam)).append(" - ")
                .append(TimeUtils.getStringJam((int) hourpinjam % 24));
        holder.mWaktuPinjam.setText(pinjam.toString());
        StringBuilder kembali = new StringBuilder();
        kembali.append(TimeUtils.getStringDate(wktkembali)).append(" - ")
                .append(TimeUtils.getStringJam((int) hourkembali % 24));
        holder.mWaktuKembali.setText(kembali.toString());

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) == null ? LOADING_TYPE : ITEM_TYPE;
    }
    public void setNeedLoading(boolean needLoading) {
        this.needLoading = needLoading;
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView mWaktuPinjam;
        private TextView mWaktuKembali;
        private TextView nama;
        private TextView alamat;
        private TextView judulStatus;
        private Button mBtnDetail;
        private View mStatusView;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            mWaktuPinjam = itemView.findViewById(R.id.mWaktuPinjam);
            mWaktuKembali = itemView.findViewById(R.id.mWaktuKembali);
            nama = itemView.findViewById(R.id.nama);
            alamat = itemView.findViewById(R.id.alamat);
            judulStatus = itemView.findViewById(R.id.judulStatus);
            mBtnDetail = itemView.findViewById(R.id.mBtnDetail);
            mStatusView = itemView.findViewById(R.id.mStatusView);
            mBtnDetail.setOnClickListener(this);
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
