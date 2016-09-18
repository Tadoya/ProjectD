package seoulapp.chok.rokseoul.maps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by JIEUN on 2016-09-17.
 * 구글맵 클러스터링을 위한 클래스! 과연 쓸데가 있을까?
 */

public class ClusterItem implements com.google.maps.android.clustering.ClusterItem{

    private final LatLng mPosition;

    public ClusterItem(double lat, double lng){
        mPosition = new LatLng(lat, lng);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
