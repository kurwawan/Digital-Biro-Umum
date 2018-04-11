package com.example.asus.d_biumsemarang1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asus.d_biumsemarang1.fragment.SearchGV;
import com.example.asus.d_biumsemarang1.fragment.SearchGVH;
import com.example.asus.d_biumsemarang1.utils.DialogUtils;

import java.lang.reflect.Field;

public class SearchActivity extends AppCompatActivity {

    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String SEARCH_TYPE = "search_type";
    public static final String RIWAYAT_POSITION = "riwayat_position";
    public static final String SEARCH_QUERY = "search_query";

    private Toolbar mToolbar;
    private MenuItem mSearch;
    private FragmentManager mFragmentManager;
    private AlertDialog mDialog;

    private String mHint;
    private String mSearchType;
    private String mQuery;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mFragmentManager = getSupportFragmentManager();
        initView();
        initHintSearch();
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        String fullTitle;
        if(mSearchType.equals(MainActivity.KODE_RIWAYAT)){
            fullTitle = getString(R.string.history_v2, mTitle);

        }
        else{
            fullTitle = mTitle;
        }
        getSupportActionBar().setTitle(getString(R.string.search_title,fullTitle));

        putSearchFragment();
    }

    private void initView() {
        mToolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        mSearch = menu.findItem(R.id.search);
        initSearchView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.info:
                if(mDialog != null){
                    mDialog.dismiss();
                }
                mDialog = DialogUtils.buildMessageDialog(this, R.string.information, R.string.info_search);
                mDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initSearchView() {
        final SearchView searchView =
                (SearchView) mSearch.getActionView();

        //Expand the searchView
        mSearch.expandActionView();
        //searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);

        // Enable/Disable Submit button in the keyboard
        searchView.setSubmitButtonEnabled(false);

        //remove the hint icon
        ImageView searchViewIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        searchViewIcon.setImageDrawable(null);
        searchViewIcon.setVisibility(View.GONE);


        // set hint and the text colors
        EditText txtSearch = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        txtSearch.setHint(mHint);
        txtSearch.setHintTextColor(ContextCompat.getColor(this, R.color.colorHint));
        txtSearch.setTextColor(getResources().getColor(android.R.color.black));


        // set the cursor
        AutoCompleteTextView searchTextView = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, 0); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.trim().length() > 0){
                    mQuery = query;
                    callSearch();
                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            private void callSearch() {
                //Do searching
                putSearchFragment();
                Log.i("query", "" + mQuery);

            }

        });
    }

    private void initHintSearch() {
        Intent intent = getIntent();
        mSearchType = intent.getStringExtra(SEARCH_TYPE);
        if (mSearchType.equals(MainActivity.KODE_RIWAYAT)) {
            int pos = getIntent().getIntExtra(RIWAYAT_POSITION, 0);
            if (pos == 0) {
                mTitle = getString(R.string.gedung);
            } else {
                mTitle = getString(R.string.venue);
            }
            mHint = getString(R.string.history_v3, mTitle);
        } else {
            if (mSearchType.equals(MainActivity.KODE_GEDUNG)) {
                mTitle = getString(R.string.gedung);
            } else {
                mTitle = getString(R.string.venue);
            }
            mHint = getString(R.string.name, mTitle);
        }
    }

    private void putSearchFragment() {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        if (mSearchType.equals(MainActivity.KODE_RIWAYAT)) {
            fragment = new SearchGVH();
            int pos = getIntent().getIntExtra(RIWAYAT_POSITION, 0);
            bundle.putInt(RIWAYAT_POSITION, pos);
        } else{
            fragment = new SearchGV();
            if (mSearchType.equals(MainActivity.KODE_GEDUNG)) {
                bundle.putString(SEARCH_TYPE, MainActivity.KODE_GEDUNG);

            } else {
                bundle.putString(SEARCH_TYPE, MainActivity.KODE_VENUE);
            }
        }
        bundle.putString(SEARCH_QUERY, mQuery);
        fragment.setArguments(bundle);
        mFragmentManager.beginTransaction().replace(R.id.mContainer,fragment).commit();


    }


}
