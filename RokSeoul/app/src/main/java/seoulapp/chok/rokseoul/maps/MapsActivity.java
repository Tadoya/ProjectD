package seoulapp.chok.rokseoul.maps;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import seoulapp.chok.rokseoul.R;

public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;


    private ClusterManager<seoulapp.chok.rokseoul.maps.ClusterItem> mClusterManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //mapFragment 설정
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //이건 뭐하는건지...
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //map준비 다 되면?
        mMap = googleMap;

        //1. 내 위치gps와 내위치로 이동
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

        //커스터마이징 한 윈도우어댑터
        setCustomWindow();
        //리스트로부터 마커 뿌리기
        getSampleMarkerItems();

        //지도 클릭하며 마커 찍기-일단 주석
        //setMarkerOnClick();

        //클러스터링하기!!!!<<<진행중
        setClusterManager();

    }

    /*
    * 커스터마이징 한 윈도우어댑터시작-----------
    * */
    private void setCustomWindow() {

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            //InfoWindowOption 위의건 뭔지 모르겠다 오버라이드하라고 되어있어서 하긴 했다
            //사용자 정의 윈도우 꺼내기
            //이 윈도우는 infowindow.xml파일이다!
            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.infowindow, null);

                LatLng latLng = marker.getPosition();

                TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);

                tvLat.setText("이곳의 위도는 : " + latLng.latitude);
                tvLng.setText("이곳의 경도는 : " + latLng.longitude);

                //닫기 버튼으로 닫는것 구현하자
                //이미지는 어떻게 불러와야하나...

                return v;
            }
        });
    }
    /*
    * 커스터마이징 한 윈도우어댑터끝-----------------------------
    * */


    /*
    * 지도 클릭한 대로 마커 찍기 시작----------------------------
    * */
    private void setMarkerOnClick() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                //mMap.clear();

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_coin));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                Marker marker = mMap.addMarker(markerOptions);
                marker.showInfoWindow();

            }
        });

    }
    /*
    * 지도 클릭한 대로 마커 찍기 끝----------------------------
    * */


    /*
    * 위도경도 리스트 가져와서 마커 찍기 시작-------
    * */
    private void getSampleMarkerItems() {
        //이거 어떻게 쓸지 모르겠다...왜냐하면 클러스터를 쓸거니까! 일단 구분을 위해 아이콘만 참잘으로 바꿔놓음
        ArrayList<MarkerOptions> sampleList = new ArrayList();
        LatLng a = new LatLng(36.370881, 127.34363389999999);
        LatLng b = new LatLng(37.527523, 126.96568);
        LatLng c = new LatLng(0, 0);

        sampleList.add(new MarkerOptions().position(a));
        sampleList.add(new MarkerOptions().position(b));
        sampleList.add(new MarkerOptions().position(c));

        for (MarkerOptions mo : sampleList) {
            mMap.addMarker(new MarkerOptions().position(mo.getPosition()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_good)));
        }
    }

    /*
    * 위도경도 리스트 가져와서 마커 찍기 끝-------
    * */

    /*
    * 클러스터 매니저_지도 줌아웃하면 숫자표시되는거 시작-----------
    * */
    private void setClusterManager() {
        //--------------------
        //사용자의 위치정보 가져오는건데 실행이 안되어서 임시로 충남대 위도 넣었다
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //double lat = location.getLongitude();
        //double lng = location.getLatitude();

        double lat =  36.370881;
        double lng = 127.34363389999999;
        //--------------------


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10));

        mClusterManager = new ClusterManager<>(this, mMap);


        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        addItems(lat, lng);
    }

    private void addItems(double lat, double lng) {

        // Add ten cluster items in close proximity, for purposes of this example.
        //마커 아이콘 좀 적절한거 찾아보자..
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            //ClusterItem클래스에서 정보 가져온다. 여기에 아이콘 변경 등이 있음
            seoulapp.chok.rokseoul.maps.ClusterItem offsetItem = new seoulapp.chok.rokseoul.maps.ClusterItem(lat, lng);
            mClusterManager.setRenderer(new ClusterWithIcon(getApplicationContext(), mMap, mClusterManager));
            mClusterManager.addItem(offsetItem);

        }
    }
    /*
    * 클러스터 매니저_지도 줌아웃하면 숫자표시되는거 끝-------------
    * */




    /*
    * 권한설정 관련 메소드! 손대지 말자 시작------------
    * */

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "현재 사용자의 접속 위치를 표시합니다", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    /*
    * 권한설정 관련 메소드! 손대지 말자 끝------------
    * */


}
