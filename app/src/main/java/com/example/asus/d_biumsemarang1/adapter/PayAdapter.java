package com.example.asus.d_biumsemarang1.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.data.Bank;
import com.example.asus.d_biumsemarang1.listener.PayHolderListener;
import com.example.asus.d_biumsemarang1.utils.GlideApp;

import java.util.List;

/**
 * Created by ASUS on 12/3/2017.
 */

public class PayAdapter extends RecyclerView.Adapter<PayAdapter.PayViewHolder> {
    public static final String TAG = PayAdapter.class.getSimpleName();

    private List<Bank> mData;
    private Context mContext;
    private PayHolderListener mListener;
    private int mHeight;

    public PayAdapter(List<Bank> data, PayHolderListener listener) {
        mData = data;
        mListener = listener;
    }

    @Override
    public PayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View content = LayoutInflater.from(mContext).inflate(R.layout.item_pay, parent, false);
        return new PayViewHolder(content);
    }

    @Override
    public void onBindViewHolder(PayViewHolder holder, int position) {
        String urlGambar = mData.get(position).getGambar();
        GlideApp.with(mContext).load(urlGambar).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.mPhoto);
        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams)holder.mPhoto.getLayoutParams();
        param.width = mHeight;
        Log.v(TAG,"BIND " + position);
        holder.mNama.setText(mData.get(position).getNama());
        holder.mAtasNama.setText(mContext.getString(R.string.atas_bank, mData.get(position).getAtas_nama()));
        holder.mNoRekening.setText(mContext.getString(R.string.rek_bank, mData.get(position).getNo_rekening()));
        if(position == mData.size() - 1){
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.itemView.getLayoutParams();
            params.bottomMargin = params.topMargin * 6;
        }
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class PayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mPhoto;
        private TextView mNama, mAtasNama, mNoRekening;
        private Button mBtnLink, mBtnDetail;

        public PayViewHolder(final View itemView) {
            super(itemView);
            mPhoto = itemView.findViewById(R.id.mPhoto);
            if(mHeight == 0){
                itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            itemView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        else {
                            itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams)mPhoto.getLayoutParams();
                        mHeight = mPhoto.getHeight();
                        param.width = mHeight;
                        Log.v(TAG, "GLOBAL");
                    }

                });
            }
            mNama = itemView.findViewById(R.id.mNama);
            mAtasNama = itemView.findViewById(R.id.mAtasNama);
            mNoRekening = itemView.findViewById(R.id.mNoRekening);
            mBtnLink = itemView.findViewById(R.id.mBtnLink);
            mBtnLink.setOnClickListener(this);
            mBtnDetail = itemView.findViewById(R.id.mBtnDetail);
            mBtnDetail.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id){
                case R.id.mBtnDetail:
                    mListener.onClickDetail(getAdapterPosition());
                    break;
                case R.id.mBtnLink:
                    mListener.onClickLink(getAdapterPosition());
                    break;
            }

        }
    }
}
