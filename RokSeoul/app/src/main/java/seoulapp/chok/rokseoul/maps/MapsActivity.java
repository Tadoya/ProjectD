package seoulapp.chok.rokseoul.maps;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.*;

import java.util.ArrayList;

import seoulapp.chok.rokseoul.R;

public class MapsActivity extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public GoogleMap mMap;
    private ClusterManager<ClusterItem> mClusterManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment mapFragment = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            } else {
                mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            }
            Log.d("SDS", "SDK version : " + android.os.Build.VERSION.SDK_INT + "\nmapFragment : " + mapFragment);
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }


    //FragmentActivity는 다른 액티비티에서 포함할 수 없대
    //따라서 fragment로만 extend해야함


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setClusterManager_sight();
        //setCustomWindow();
        enableMyLocation();

    }

    /*
    * 클러스터 매니저_지도 줌아웃하면 숫자표시되는거 시작-----------
    * 유적지마커
    * */
    private void setClusterManager_sight() {
        //double lat = 36.370881;
        //double lng = 127.34363389999999; 충남대

        ArrayList<String> latlngList = new ArrayList<>();
        latlngList.add(0, "37.5801859611");
        latlngList.add(1, "126.9767235747");
        latlngList.add(2, "경복궁");
        latlngList.add(3, "36.370881");
        latlngList.add(4, "127.34363389999999");
        latlngList.add(5, "충남대");
        latlngList.add(6, "37.5516394747");
        latlngList.add(7, "126.9876206116");
        latlngList.add(8, "N서울타워");

        //위도, 경도, 관광지 명을 파라미터로보낸다 setCustomWindow

        mClusterManager = new ClusterManager<ClusterItem>(getActivity(), mMap);

        mMap.setOnCameraChangeListener(mClusterManager);
        //mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        //자세히보기를 하려면 다음 리스너필요
        mMap.setOnInfoWindowClickListener(mClusterManager);


        for (int i = 0; i < latlngList.size(); i += 3) {
            double lat = Double.parseDouble(latlngList.get(i));
            double lng = Double.parseDouble(latlngList.get(i + 1));
            String sightName = latlngList.get(i + 2);
            addItems(lat, lng, sightName);
        }
        //mMap.setOnMarkerClickListener(mClusterManager);
    }

    private void setClusterManager_event() {

    }

    private void addItems(final double lat, final double lng, final String sightN) {


        ClusterItem offsetItem = new ClusterItem(lat, lng, sightN, "위도 : "+ lat + "\n경도 : "+lng+"\n장소 :" + sightN);
        mClusterManager.setRenderer(new ClusterWithIcon(getActivity().getApplicationContext(), mMap, mClusterManager));
        //setCustomWindow();

        //클러스터 눌렀을 때 확대하기
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<ClusterItem> cluster) {
                // Show a toast with some info when the cluster is clicked.
                //String firstName = cluster.getItems().iterator().next().getSightN();
                //Toast.makeText(getActivity(), cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

                // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
                // inside of bounds, then animate to center of the bounds.

                // Create the builder to collect all essential cluster items for the bounds.
                LatLngBounds.Builder builder = LatLngBounds.builder();
                for (ClusterItem item : cluster.getItems()) {
                    builder.include(item.getPosition());
                }

                // Get the LatLngBounds
                final LatLngBounds bounds = builder.build();

                // Animate camera to the bounds
                try {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;

            }
        });
        setCustomWindow();
        mClusterManager.addItem(offsetItem);
        mClusterManager.cluster();

    }




    /*
    * 클러스터 매니저_지도 줌아웃하면 숫자표시되는거 끝-----------
    * */


    /*
    * 커스터마이징 한 윈도우어댑터시작-----------
    * */
    private void setCustomWindow() {

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            View v = getActivity().getLayoutInflater().inflate(R.layout.infowindow, null);
            TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
            TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
            TextView tvSightN = (TextView) v.findViewById(R.id.tv_sightName);


            @Override
            public View getInfoWindow(Marker marker) {
                LatLng latLng = marker.getPosition();
                tvLat.setText("이곳의 위도는 : " + latLng.latitude);
                tvLng.setText("이곳의 경도는 : " + latLng.longitude);
                tvSightN.setText("이곳의 이름은 : " + marker.getTitle());

                return v;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
    }

    /*
    * 커스터마이징 한 윈도우어댑터끝-----------
    * */

    /*내 위치 버튼 클릭 시작-----------------*/

    /*내 위치 버튼 클릭 끝-----------------*/


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getActivity(), "현재 사용자의 위치를 표시합니다", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else {
            mPermissionDenied = true;
        }
    }

}
