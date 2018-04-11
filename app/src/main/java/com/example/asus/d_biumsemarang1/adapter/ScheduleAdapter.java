package com.example.asus.d_biumsemarang1.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.data.JadwalSewa;
import com.example.asus.d_biumsemarang1.utils.TimeUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ASUS on 11/15/2017.
 */

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<JadwalSewa> mData;
    private Context mContext;

    public ScheduleAdapter(List<JadwalSewa> data){
        mData = data;
    }
    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View container = LayoutInflater.from(mContext).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(container);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        holder.mNoJadwal.setText(mContext.getString(R.string.schedule_tipe, String.valueOf(position+1)));
        long wktpinjam = mData.get(position).getWaktu_pinjam();
        long wktkembali = mData.get(position).getWaktu_kembali();
        long hourpinjam = TimeUnit.MILLISECONDS.toHours(wktpinjam);
        long hourkembali = TimeUnit.MILLISECONDS.toHours(wktkembali);
        StringBuilder pinjam = new StringBuilder();
        pinjam.append(TimeUtils.getStringDate(wktpinjam)).append(" - ").append(TimeUtils.getStringJam((int) hourpinjam % 24));
        holder.mWaktuPinjam.setText(pinjam.toString());
        StringBuilder kembali = new StringBuilder();
        kembali.append(TimeUtils.getStringDate(wktkembali)).append(" - ").append(TimeUtils.getStringJam((int) hourkembali % 24));
        holder.mWaktuKembali.setText(kembali.toString());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ScheduleViewHolder extends RecyclerView.ViewHolder{
        private TextView mNoJadwal;
        private TextView mWaktuPinjam;
        private TextView mWaktuKembali;
        public ScheduleViewHolder(View itemView) {
            super(itemView);
            mNoJadwal = itemView.findViewById(R.id.mNoJadwal);
            mWaktuPinjam = itemView.findViewById(R.id.mWaktuPinjam);
            mWaktuKembali = itemView.findViewById(R.id.mWaktuKembali);
        }
    }
}
