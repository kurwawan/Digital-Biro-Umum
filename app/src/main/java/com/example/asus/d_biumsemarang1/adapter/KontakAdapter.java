package com.example.asus.d_biumsemarang1.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.data.Kontak;
import com.example.asus.d_biumsemarang1.listener.ViewHolderListener;

import java.util.List;

/**
 * Created by ASUS on 12/11/2017.
 */

public class KontakAdapter extends RecyclerView.Adapter<KontakAdapter.KontakViewHolder> {

    private List<Kontak> mData;
    private ViewHolderListener mListener;
    private Context mContext;
    public KontakAdapter(List<Kontak> data, ViewHolderListener listener){
        mData = data;
        mListener = listener;
    }


    @Override
    public KontakViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View content = LayoutInflater.from(mContext).inflate(R.layout.item_kontak, parent, false);
        return new KontakViewHolder(content);
    }

    @Override
    public void onBindViewHolder(KontakViewHolder holder, int position) {
        holder.nama.setText(mData.get(position).getNama());
        holder.noHp.setText(mData.get(position).getNoHp());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class KontakViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView nama, noHp;
        private ImageView telepon;

        public KontakViewHolder(View itemView) {
            super(itemView);
            nama = itemView.findViewById(R.id.nama);
            noHp = itemView.findViewById(R.id.noHp);
            telepon = itemView.findViewById(R.id.telepon);
            telepon.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(getAdapterPosition());
        }
    }
}
