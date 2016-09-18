/**
 * 2016.9.10
 * Tadoya
 */
package com.example.choiseongsik.drawingfun;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * Created by choiseongsik on 2016. 9. 10..
 */

public class ImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public ImageSurfaceView(Context context){
        super(context);
        getHolder().addCallback(this);  //생성자에 콜백등록
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
