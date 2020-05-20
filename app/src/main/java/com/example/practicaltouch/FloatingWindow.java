package com.example.practicaltouch;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class FloatingWindow extends Service {
    WindowManager windowManager;
    LinearLayout frontLayer;
    LinearLayout backLayer;
    Point screenSize = new Point();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        assert windowManager != null;
        windowManager.getDefaultDisplay().getSize(screenSize);
        frontLayer = new LinearLayout(this);
        frontLayer.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        frontLayer.setLayoutParams(layoutParams);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.END | Gravity.TOP;
        params.x = 16;
        params.y = 16;

        final ImageView openapp = new ImageView(this);
        openapp.setImageResource(R.mipmap.ic_launcher_round);
        ViewGroup.LayoutParams butnparams = new ViewGroup.LayoutParams(
                150,150);
        openapp.setLayoutParams(butnparams);

        backLayer = new LinearLayout(this);
        backLayer.setBackgroundColor(Color.TRANSPARENT);
        backLayer.setLayoutParams(layoutParams);


        final WindowManager.LayoutParams crossParam = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        final ImageView crossIcon = new ImageView(this);
        crossIcon.setImageResource(R.mipmap.floating_cross_foreground);
        crossIcon.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        crossParam.gravity = Gravity.BOTTOM;
        crossParam.x = 16;
        crossParam.y = 16;

        frontLayer.addView(openapp);
        windowManager.addView(backLayer,crossParam);
        windowManager.addView(frontLayer,params);

        openapp.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatepar = params;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = updatepar.x;
                        y = updatepar.y;
                        px = motionEvent.getRawX();
                        py = motionEvent.getRawY();
                        backLayer.addView(crossIcon);
                        break;
                    case MotionEvent.ACTION_UP:
                        backLayer.removeView(crossIcon);
                        windowManager.updateViewLayout(frontLayer,updatepar);
                        if(screenSize.y - updatepar.y <= 400 && Math.abs(updatepar.x - screenSize.x/2) <= 150) {
                            MainActivity.switchBubbleService(false);
                            onDestroy();
                        } else {
                            if (updatepar.x >= screenSize.x / 2) {
                                updatepar.x = screenSize.x - 170;
                            } else {
                                updatepar.x = 16;
                            }
                            windowManager.updateViewLayout(frontLayer,updatepar);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updatepar.x = (int) (x-(motionEvent.getRawX()-px));
                        updatepar.y = (int) (y+(motionEvent.getRawY()-py));
                        windowManager.updateViewLayout(frontLayer,updatepar);
                        default:
                        break;
                }
                return false;
            }
        });


        openapp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view){
                return true;
            }
        });

        openapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                params.x = 16;
                params.y = 16;
                windowManager.updateViewLayout(frontLayer,params);
                Intent home = new Intent(FloatingWindow.this,MainActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(home);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        try {
            windowManager.removeView(backLayer);
        } catch (Exception e) {

        }
        windowManager.removeView(frontLayer);
    }
}
