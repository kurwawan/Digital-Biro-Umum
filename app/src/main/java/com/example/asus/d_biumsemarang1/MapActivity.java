package com.example.asus.d_biumsemarang1;


import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.d_biumsemarang1.utils.ConnectionUtils;
import com.example.asus.d_biumsemarang1.utils.GlideApp;
import com.example.asus.d_biumsemarang1.utils.GoogleMapLoader;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import es.dmoral.toasty.Toasty;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String>, OnStreetViewPanoramaReadyCallback {

    private static final String TAG = MapActivity.class.getSimpleName();
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE_STREET = "longitude_street";
    private static final String LATITUDE_STREET = "latitude_street";
    private static final String LONGITUDE_LOCATION = "longitude_location";
    private static final String LATITUDE_LOCATION = "latitude_location";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String ZOOM_STREET = "zoom_street";
    private static final String BEARING_STREET = "bearing_street";
    private static final String TILT_STREET = "tilt_street";
    public static final String NAMA_MAP = "nama_map";
    public static final String ALAMAT_MAP = "alamat_map";
    public static final String FOTO_MAP = "foto_map";
    private static final String API_KEY = "AIzaSyBRSPVzX8iFmj3JoBG7HqkuHNsK-C7r-4U";
    private static final String BASE_URL_GEOLOCATION = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final int ID_LOADER = 7;

    private Toolbar mToolbar;
    private FragmentManager mFragmentManager;
    private boolean isConnected = false;
    private GoogleMap mMap;
    private StreetViewPanorama mPanorama;
    private FloatingActionButton mButtonLocation;
    private Toast mToast;
    private ImageView mImageView;
    private TextView nama, alamat;
    private View mContentMap;

    private String mAlamat;
    private String mNama;
    private String mFoto;
    private int mIdGambar;
    private boolean hasMarkered = false;
    private LatLng udinus = new LatLng(-6.982813, 110.409222);
    private LatLng mLocation;
    private CameraPosition mPosition;
    private StreetViewPanoramaCamera mCamera;
    private LatLng mStreetLocation;

    private boolean isMapReady = false;
    private boolean isPanoramaReady = false;
    private String mTitle;
    private Marker mMarker;
    private int mRadius = 200;
    private boolean isLocationReady = false;
    private boolean isSaved = false;
    private boolean isMap = true;
    private boolean isFinished = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        handleIntent();
        isConnected = ConnectionUtils.isOnline(this);
        if (savedInstanceState != null) {
            isFinished = savedInstanceState.getBoolean("isFinished");
            isLocationReady = savedInstanceState.getBoolean("isLocationReady");
            isMap = savedInstanceState.getBoolean("isMap");
            isSaved = savedInstanceState.getBoolean("isSaved");
            if(isSaved){
                if(isMap){
                    mPosition = getSavedCameraPosition(savedInstanceState);
                }
                else {
                    getSavedStreetPosition(savedInstanceState);
                }
            }

            mLocation = new LatLng(savedInstanceState.getDouble(LATITUDE_LOCATION), savedInstanceState.getDouble(LONGITUDE_LOCATION));
            if (!isFinished) {
                getSupportLoaderManager().initLoader(ID_LOADER, null, this);
            }
        } else {
            if (isConnected) {
                getSupportLoaderManager().initLoader(ID_LOADER, null, this);
            }
        }
        initView();
        mFragmentManager = getFragmentManager();
        if (isMap) {
            addMap();
        } else {
            addStreetView();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_google, menu);
        if (isMap) {
            Log.d(TAG, "this");
            menu.findItem(R.id.google).setTitle(R.string.street_google);
        } else {
            menu.findItem(R.id.google).setTitle(R.string.map_google);

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.google:
                if (!isMap) {
                    isMap = true;
                    isMapReady = false;
                    invalidateOptionsMenu();
                    addMap();
                } else if (isLocationReady) {
                    isMap = false;
                    isPanoramaReady = false;
                    invalidateOptionsMenu();
                    addStreetView();

                } else {
                    maybeNeedReload(true);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        mToolbar = findViewById(R.id.mToolbar);
        mButtonLocation = findViewById(R.id.mLocation);
        mButtonLocation.setImageResource(mIdGambar);
        mButtonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMap) {
                    checkAllReady(true);
                } else if (isPanoramaReady) {
                    mPanorama.setPosition(mLocation, mRadius);
                } else {
                    checkToast();
                    showToast(R.string.try_again, false);
                }

            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mTitle);
        mContentMap = getLayoutInflater().inflate(R.layout.item_mapcontent, null);
        mImageView = mContentMap.findViewById(R.id.mImage);
        GlideApp.with(this).load(mFoto).into(mImageView);
        nama = mContentMap.findViewById(R.id.nama);
        alamat = mContentMap.findViewById(R.id.alamat);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.getAction().equals(MainActivity.KODE_GEDUNG)) {
            mIdGambar = R.drawable.gedung_v2;
            mTitle = getString(R.string.map_title, getString(R.string.gedung));
        } else {
            mIdGambar = R.drawable.venue_v2;
            mTitle = getString(R.string.map_title, getString(R.string.venue));
        }
        mNama = intent.getStringExtra(NAMA_MAP);
        mAlamat = intent.getStringExtra(ALAMAT_MAP);
        mFoto = intent.getStringExtra(FOTO_MAP);

    }

    private void addMap() {
        mPanorama = null;
        mStreetLocation = null;
        mCamera = null;
        MapFragment mapFragment = new MapFragment();
        mFragmentManager.beginTransaction().replace(R.id.mContainer, mapFragment).commit();
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                nama.setText(marker.getTitle());
                alamat.setText(marker.getSnippet());
                return mContentMap;
            }
        });
        isMapReady = true;
        if (mPosition != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(mPosition);
            mMap.moveCamera(update);
            mPosition = null;
            if (!hasMarkered && isLocationReady) {
                hasMarkered = true;
                mMarker = mMap.addMarker(new MarkerOptions().position(mLocation).title(mNama).snippet(mAlamat));

            }
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(udinus, 18.0f));
            checkAllReady(false);
        }

    }

    private void addStreetView() {
        hasMarkered = false;
        mMap = null;
        StreetViewPanoramaFragment panoramaFragment;
        if(mStreetLocation != null){
             panoramaFragment = StreetViewPanoramaFragment.newInstance(new StreetViewPanoramaOptions().panoramaCamera(mCamera).position(mStreetLocation));
             mStreetLocation = null;
             mCamera = null;
        }
        else{
            panoramaFragment = StreetViewPanoramaFragment.newInstance(new StreetViewPanoramaOptions().position(mLocation, 200));
        }

        mFragmentManager.beginTransaction().replace(R.id.mContainer, panoramaFragment).commit();
        panoramaFragment.getStreetViewPanoramaAsync(this
        );
    }


    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        isFinished = false;
        isLocationReady = false;
        Uri base_uri = Uri.parse(BASE_URL_GEOLOCATION);
        Uri.Builder builder = null;
        try {
            builder = base_uri.buildUpon()
                    .appendQueryParameter("address", URLEncoder.encode(mAlamat, "utf-8"))
                    .appendQueryParameter("region", "id")
                    .appendQueryParameter("key", API_KEY);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "URI " + base_uri == null ? "YA" : builder.build().toString());
        return new GoogleMapLoader(this, builder.build().toString());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFinished", isFinished);
        outState.putBoolean("isLocationReady", isLocationReady);
        outState.putBoolean("isMap", isMap);
        if (mLocation != null) {
            outState.putDouble(LATITUDE_LOCATION, mLocation.latitude);
            outState.putDouble(LONGITUDE_LOCATION, mLocation.longitude);
        }
        if (mMap != null && isMapReady) {
            outState.putBoolean("isSaved", true);
            mPosition = mMap.getCameraPosition();
            outState.putDouble(LATITUDE, mPosition.target.latitude);
            outState.putDouble(LONGITUDE, mPosition.target.longitude);
            outState.putFloat(ZOOM, mPosition.zoom);
            outState.putFloat(TILT, mPosition.tilt);
            outState.putFloat(BEARING, mPosition.bearing);
        }
        if (mPanorama != null && isLocationReady) {
            Log.d(TAG,"mPanorama ");

            mCamera = mPanorama.getPanoramaCamera();
            outState.putFloat(ZOOM_STREET, mCamera.zoom);
            outState.putFloat(TILT_STREET, mCamera.tilt);
            outState.putFloat(BEARING_STREET, mCamera.bearing);
            StreetViewPanoramaLocation location = mPanorama.getLocation();
            if(mPanorama.getLocation() != null){
                outState.putDouble(LATITUDE_STREET, mPanorama.getLocation().position.latitude);
                outState.putDouble(LONGITUDE_STREET, mPanorama.getLocation().position.longitude);
                outState.putBoolean("isSaved", true);
            }
        }

    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        getJSONLocation(data);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    private void getJSONLocation(String data) {
        if (data != null) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONArray jsonArray;
                JSONObject jsonDataLocation;
                String status = jsonObject.getString("status");
                switch (status) {
                    case "OK":
                        jsonArray = jsonObject.getJSONArray("results");
                        if (jsonArray.length() != 0) {
                            jsonDataLocation = jsonArray.getJSONObject(0);
                            JSONObject jsonActualLocation = jsonDataLocation.getJSONObject("geometry").getJSONObject("location");
                            mLocation = new LatLng(jsonActualLocation.getDouble("lat"), jsonActualLocation.getDouble("lng"));
                            isLocationReady = true;
                            if (isMapReady && !hasMarkered && mMap != null) {
                                hasMarkered = true;
                                mMarker = mMap.addMarker(new MarkerOptions().position(mLocation).title(mNama).snippet(mAlamat));
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
        isFinished = true;

    }

    private void checkAllReady(boolean fromButton) {
        if (isMapReady && isLocationReady) {
            if (!hasMarkered) {
                hasMarkered = true;
                mMarker = mMap.addMarker(new MarkerOptions().position(mLocation).title(mNama).snippet(mAlamat));
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mLocation));

        } else if (isMapReady) {
            checkToast();
            maybeNeedReload(fromButton);
        }

    }

    private CameraPosition getSavedCameraPosition(Bundle bundle) {
        double latitude = bundle.getDouble(LATITUDE, 0);
        if (latitude == 0) {
            return null;
        }
        double longitude = bundle.getDouble(LONGITUDE, 0);
        LatLng target = new LatLng(latitude, longitude);

        float zoom = bundle.getFloat(ZOOM, 0);
        float bearing = bundle.getFloat(BEARING, 0);
        float tilt = bundle.getFloat(TILT, 0);

        return new CameraPosition(target, zoom, tilt, bearing);
    }

    private void getSavedStreetPosition(Bundle bundle) {
        mStreetLocation = new LatLng(bundle.getDouble(LATITUDE_STREET), bundle.getDouble(LONGITUDE_STREET));
        Log.d(TAG, "panoID " + mStreetLocation);
        mCamera = StreetViewPanoramaCamera.builder().bearing(bundle.getFloat(BEARING_STREET))
                .tilt(bundle.getFloat(TILT_STREET)).zoom(bundle.getFloat(ZOOM_STREET)).build();
    }

    private void checkToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private void showToast(int message, boolean error) {
        if (error) {
            mToast = Toasty.error(this, getString(message), Toast.LENGTH_SHORT);
        } else {
            mToast = Toast.makeText(this, getString(message), Toast.LENGTH_SHORT);
        }
        mToast.setGravity(Gravity.TOP | Gravity.CENTER, 0, getResources().getDimensionPixelSize(R.dimen.toolbar));
        mToast.show();
    }

    private void maybeNeedReload(boolean fromButton) {
        isConnected = ConnectionUtils.isOnline(this);
        if (isConnected) {
            if (isFinished) {
                getSupportLoaderManager().restartLoader(ID_LOADER, null, this);

            }
            if (fromButton) {
                showToast(R.string.try_again, false);
            }

        } else {
            if (fromButton) {
                showToast(R.string.check_connection, true);
            }
        }
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        mPanorama = streetViewPanorama;
        isPanoramaReady = true;
    }
}
