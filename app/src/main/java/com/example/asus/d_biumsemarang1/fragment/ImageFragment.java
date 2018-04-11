package com.example.asus.d_biumsemarang1.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.asus.d_biumsemarang1.R;
import com.example.asus.d_biumsemarang1.utils.GlideApp;

/**
 * Created by ASUS on 11/7/2017.
 */

public class ImageFragment extends Fragment {
    private static final String URL_GAMBAR = "url_gambar";

    public static ImageFragment newInstance(String gambar) {
        Bundle args = new Bundle();
        args.putString(URL_GAMBAR, gambar);
        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_image, container, false);
        ImageView mImgView = content.findViewById(R.id.mImgView);
        String gambar = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            gambar = bundle.getString(URL_GAMBAR, "");
        }
        GlideApp.with(getContext()).load(gambar).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().into(mImgView);
        return content;
    }
}
