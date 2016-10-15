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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import seoulapp.chok.rokseoul.R;
import seoulapp.chok.rokseoul.maps.getdata.GetSightInform;

public class MapsActivity extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    public GoogleMap mMap;
    private ClusterManager<ClusterItem> mClusterManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    String allData = null;

    private DatabaseReference mPlaceDatabase;
    private ValueEventListener mPlaceValueEventListener;

    private String[] places;
    private int[ ] placeDoodles;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_maps, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mPlaceDatabase = FirebaseDatabase.getInstance().getReference().child("QRPlace");
        mPlaceValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("mapsac" , "onDataChange");
                try {
                    int i = (int)dataSnapshot.getChildrenCount();
                    places = new String[i];
                    placeDoodles = new int[i];
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        places[--i] = snapshot.getKey();
                        placeDoodles[i] = Integer.parseInt(snapshot.child("doodles").getValue().toString());
                        Log.d("MapsActivity", "placename(realtime-maps): " + places[i]+" / placeDoodles : " + placeDoodles[i]);
                    }
                }catch (Exception e){
                    places = null;
                    placeDoodles = null;
                }
                setClusterManager_sight();
                setClusterManager_event();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        MapFragment mapFragment = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            } else {
                mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            }
            Log.d("SDS", "SDK version : " + Build.VERSION.SDK_INT + "\nmapFragment : " + mapFragment);
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }



        RadioButton sRadio = (RadioButton) view.findViewById(R.id.radio_sight);
        RadioButton fRadio = (RadioButton) view.findViewById(R.id.radio_festival);

        sRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "sRadio click", Toast.LENGTH_SHORT).show();
                mMap.clear();
                mClusterManager.clearItems();
                setClusterManager_sight();
            }
        });
        fRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "fCheck click", Toast.LENGTH_SHORT).show();
                mMap.clear();
                mClusterManager.clearItems();
                setClusterManager_event();
            }
        });

    }


    //FragmentActivity는 다른 액티비티에서 포함할 수 없대
    //따라서 fragment로만 extend해야함

    @Override
    public void onResume() {
        super.onResume();
        mPlaceDatabase.addListenerForSingleValueEvent(mPlaceValueEventListener);
        Log.d("mapsac" , "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mPlaceDatabase != null){
            mPlaceDatabase.removeEventListener(mPlaceValueEventListener);
            Log.d("mapsac" , "onPause");
        }
        mMap.clear();
        try {
            mClusterManager.clearItems();
        }catch(Exception e){

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 200, 0, 220);
        LatLng seoul = new LatLng(37.5666805, 126.9784147);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 10));


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

        latlngList.add(3, "37.5516394747");
        latlngList.add(4, "126.9876206116");
        latlngList.add(5, "N서울타워");

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
 /*
        * 1. input되는 행사의 시작일, 종료일은 sysdate로!
        * */
        String curTime = new SimpleDateFormat("yyyyMMdd").format(new Date());
        ArrayList<String> eventList = new ArrayList<>();

        try {
            StringBuilder urlBuilder = new StringBuilder("http://api.visitkorea.or.kr/openapi/service/rest/KorService/searchFestival");
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=pAfNXzuf%2B9ZoTgQ9ckBQOlCUzLNozWj6Am72wZ%2B57zK3c%2FjStotNWWUd2Na4PPsq1Ugcq18kbUWE%2F6QfkuloIQ%3D%3D"); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("contentTypeId", "UTF-8") + "=" + URLEncoder.encode("15", "UTF-8")); /*타입 ID*/
            urlBuilder.append("&" + URLEncoder.encode("eventStartDate", "UTF-8") + "=" + URLEncoder.encode(curTime, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("eventEndDate", "UTF-8") + "=" + URLEncoder.encode(curTime, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("arrange", "UTF-8") + "=" + URLEncoder.encode("A", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("areaCode", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("listYN", "UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("100", "UTF-8"));//보통 이벤트가 60개 넘는 일이 없어서..100개로  끊어놓음..
            urlBuilder.append("&" + URLEncoder.encode("MobileOS", "UTF-8") + "=" + URLEncoder.encode("ETC", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("MobileApp", "UTF-8") + "=" + URLEncoder.encode("RokSEOUL", "UTF-8")); /*어플이름*/

            Document output = new GetSightInform().execute(urlBuilder.toString()).get();

            // 2. x좌표, y좌표, 이벤트명, 행사시작일자, 행사종료일자를 가져온다.

            NodeList nodeList = output.getElementsByTagName("item");
            System.out.println("몇개나 있나 : "+nodeList.getLength());

            for(int i=0; i<nodeList.getLength(); i++) {
                Node nameNode = nodeList.item(i);
                Element nameElmnt = (Element) nameNode;

                eventList.add(i,nameElmnt.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue());            //축제명
                eventList.add(i+1,nameElmnt.getElementsByTagName("mapy").item(0).getChildNodes().item(0).getNodeValue());           //X좌표
                eventList.add(i+2,nameElmnt.getElementsByTagName("mapx").item(0).getChildNodes().item(0).getNodeValue());           //Y좌표
                eventList.add(i+3,nameElmnt.getElementsByTagName("eventstartdate").item(0).getChildNodes().item(0).getNodeValue()); //행사시작일자
                eventList.add(i+4,nameElmnt.getElementsByTagName("eventenddate").item(0).getChildNodes().item(0).getNodeValue());   //행사종료일자

                //받아온 것들을 5파라미터로 addFestivalItem해준다
                String festName = eventList.get(i);
                Double festMapX = Double.parseDouble(eventList.get(i+1));
                Double festMapY = Double.parseDouble(eventList.get(i+2));
                String festStartDate = eventList.get(i+3);
                String festEndDate = eventList.get(i+4);
                System.out.println("*********축제, 좌표, 일정출력 "+i+". : "+festName+", "+festMapX+", "+festMapY+", "+festStartDate+", "+festEndDate);


                String allData = festName+"\n"+festStartDate+"~"+festEndDate;
                makeMarker(festMapX, festMapY, festName, allData);

                /*if(visible){
                    addFestItems(festName, festMapX, festMapY, festStartDate, festEndDate);
                }else{

                }*/

            }
        }catch (Exception e){
            System.out.println("에러 : "+e.toString());
        }

    }

    private void addItems(final double lat, final double lng, String sightN) {

        //GetSightInform class로부터 공공데이터 받아온 후 여기서 xml파싱한다

        try {
            StringBuilder urlBuilder = new StringBuilder("http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailIntro"); /*URL*/

            /*필요 파라미터*/
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=pAfNXzuf%2B9ZoTgQ9ckBQOlCUzLNozWj6Am72wZ%2B57zK3c%2FjStotNWWUd2Na4PPsq1Ugcq18kbUWE%2F6QfkuloIQ%3D%3D"); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("contentTypeId", "UTF-8") + "=" + URLEncoder.encode("12", "UTF-8")); /*타입 ID*/

            if(sightN.equals("경복궁")){
                urlBuilder.append("&" + URLEncoder.encode("contentId", "UTF-8") + "=" + URLEncoder.encode("126508", "UTF-8"));
            }else if(sightN.equals("N서울타워")){
                urlBuilder.append("&" + URLEncoder.encode("contentId", "UTF-8") + "=" + URLEncoder.encode("126535", "UTF-8"));
            }else{
                urlBuilder.append("&" + URLEncoder.encode("contentId", "UTF-8") + "=" + URLEncoder.encode("126535", "UTF-8"));
            }
             /*콘텐츠ID  경복궁 : 126508, N서울타워 :126535*/

            urlBuilder.append("&" + URLEncoder.encode("MobileOS", "UTF-8") + "=" + URLEncoder.encode("ETC", "UTF-8")); /*OS 구분*/
            urlBuilder.append("&" + URLEncoder.encode("MobileApp", "UTF-8") + "=" + URLEncoder.encode("RokSEOUL", "UTF-8")); /*어플이름*/
            urlBuilder.append("&" + URLEncoder.encode("introYN", "UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); /*뭔지 모르겠는데 인트로*/

            Document output = new GetSightInform().execute(urlBuilder.toString()).get();
            /*
            * XML 파싱
            * */

            allData = "";
            NodeList nodeList = output.getElementsByTagName("item");
            Node nameNode = nodeList.item(0);
            Element nameElmnt = (Element) nameNode;
            String sightsID = nameElmnt.getElementsByTagName("contentid").item(0).getChildNodes().item(0).getNodeValue();
            if(sightsID.equals("126508")){
                allData+= "\n관광지명 : 경복궁 \n";
            }else if(sightsID.equals("126535")){
                allData+= "관광지명 : N서울타워(남산타워) \n";
            }
            allData+= "이용시간 : "+
                    nameElmnt.getElementsByTagName("usetime").item(0).getChildNodes().item(0).getNodeValue()+"\n";
            allData+= "쉬는날 : "+
                    nameElmnt.getElementsByTagName("restdate").item(0).getChildNodes().item(0).getNodeValue()+"\n";
            allData+= "주차시설 : "+
                    nameElmnt.getElementsByTagName("parking").item(0).getChildNodes().item(0).getNodeValue()+"\n";

            allData = allData.replaceAll("<br (/)>","");

            makeMarker(lat, lng, sightN, allData);
        }catch (Exception e){

        }

    }

    private void makeMarker(double lat, double lng, String name, String allData){
        ClusterItem offsetItem = new ClusterItem(lat, lng, name, allData);
        mClusterManager.setRenderer(new ClusterWithIcon(getActivity().getApplicationContext(), mMap, mClusterManager));

        //클러스터 눌렀을 때 확대하기
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<ClusterItem> cluster) {
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
        setCustomWindow(name);
        mClusterManager.addItem(offsetItem);
        mClusterManager.cluster();
    }



    /*
    * 클러스터 매니저_지도 줌아웃하면 숫자표시되는거 끝-----------
    * */


    /*
    * 커스터마이징 한 윈도우어댑터시작-----------
    * */
    private void setCustomWindow(String sightN) {

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            View v = getActivity().getLayoutInflater().inflate(R.layout.infowindow, null);
            TextView tvContent = (TextView) v.findViewById(R.id.tv_content);


            @Override
            public View getInfoWindow(Marker marker) {
                //LatLng latLng = marker.getPosition();
                //tvLat.setText("이곳의 위도는 : " + latLng.latitude);
                //tvLng.setText("이곳의 경도는 : " + latLng.longitude);
                tvContent.setText(marker.getSnippet());

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
