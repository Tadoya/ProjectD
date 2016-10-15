package seoulapp.chok.rokseoul.firebase;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.storage.StorageReference;

/**
 * Created by choiseongsik on 2016. 10. 7..
 */

public class DownloadService extends Service {

    StorageSet mStorageSet;
    private StorageReference mStorage;

    public DownloadService(StorageSet storageSet){
        super();
        mStorageSet = storageSet;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


}
