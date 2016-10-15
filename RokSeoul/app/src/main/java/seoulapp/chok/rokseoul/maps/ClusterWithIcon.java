package seoulapp.chok.rokseoul.maps;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by JIEUN on 2016-09-17.
 * 클러스터링과 마커는 다른개념이라고 한다...클러스터에 이미지를 입히기 위한 클래스
 */

public class ClusterWithIcon extends DefaultClusterRenderer<ClusterItem> {

    public ClusterWithIcon(Context context, GoogleMap map, ClusterManager<ClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }

    public void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions){
        super.onBeforeClusterItemRendered(item, markerOptions);
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.brush));
        markerOptions.title(item.getSightN());
        markerOptions.position(item.getPosition());
        markerOptions.snippet(item.getContents());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }

}
