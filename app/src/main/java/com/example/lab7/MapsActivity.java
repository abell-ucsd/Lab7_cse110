package com.example.lab7;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lab7.databinding.ActivityMapsBinding;

import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private ActivityMapsBinding binding;

    private Location lastVisitedLocation;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), perms -> {
                perms.forEach((perm,isGranted) -> {
                    Log.i("LAB7",String.format("Permission %s granted: %s", perm, isGranted));
                });
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        var mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //map setup
        {
        map = googleMap;
        var uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        var ucsdPosition = LatLngs.UCSD_LATLNG;
        var zooPosition = LatLngs.ZOO_LATLNG;

        var cameraPosition = new LatLng(
                (ucsdPosition.latitude + zooPosition.latitude) / 2,
                (ucsdPosition.longitude + zooPosition.longitude) / 2
        );
        map.addMarker(new MarkerOptions()
                .position(ucsdPosition)
                .title("UCSD"));

        map.moveCamera(CameraUpdateFactory
                .newLatLng(cameraPosition));
        map.moveCamera(CameraUpdateFactory.zoomTo(11.5f));
        // Add a marker in Sydney and move the camera
    }

        //permissions setup
        {
            var requiredPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

            var hasNoLocationPerms = Arrays.stream(requiredPermissions)
                    .map(perm -> ContextCompat.checkSelfPermission(this,perm))
                    .allMatch(status -> status == PackageManager.PERMISSION_DENIED);
            if (hasNoLocationPerms){
                requestPermissionLauncher.launch(requiredPermissions);
                return;
            }
        }

        //Listen for Location Updates
        {
            var provider = LocationManager.GPS_PROVIDER;
            var locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            var locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location){
                    Log.d("LAB&",String.format("Location changed: %s", location));

                    var marker = new MarkerOptions()
                            .position(new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            ))
                            .title("Navigation Step");

                    map.addMarker(marker);
                }
            };

            locationManager.requestLocationUpdates(provider,0,0f,locationListener);
        }

    }
}