package seoulapp.chok.rokseoul.maps;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.*;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import seoulapp.chok.rokseoul.R;

/**
 * Created by JIEUN on 2016-09-17.
 * 클러스터링과 마커는 다른개념이라고 한다...클러스터에 이미지를 입히기 위한 클래스
 */

public class ClusterWithIcon extends DefaultClusterRenderer<seoulapp.chok.rokseoul.maps.ClusterItem>{

    public ClusterWithIcon(Context context, GoogleMap map, ClusterManager<seoulapp.chok.rokseoul.maps.ClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }
/*
*
* mMap
* .addMarker(
* new MarkerOptions()
* .position(mo.getPosition())
* .icon(BitmapDescriptorFactory
* .fromResource(R.mipmap.ic_coin)));
*
*
* markerOptions.icon(item.getIcon());
        markerOptions.snippet(item.getSnippet());
        markerOptions.title(item.getTitle());
        super.onBeforeClusterItemRendered(item, markerOptions);
        }
*
* */
    public void onBeforeClusterItemRendered(seoulapp.chok.rokseoul.maps.ClusterItem item, MarkerOptions markerOptions){
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_coin));
        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
