package com.hanssen.lab3;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;



public class Layout extends SurfaceView implements Runnable {

    Thread          thread        = null;
    boolean         canDraw       = false;
    List<Ball>      balls         = new ArrayList<>();

    Vibrator        vibrator      = null;
    MediaPlayer     music         = null;

    Bitmap          background    = null;
    Canvas          canvas        = null;
    SurfaceHolder   surfaceholder = null;

    SensorManager   sensorManager = null;
    Sensor          sensor        = null;
    float           ax, ay        = 0;



    public Layout(Context context) {
        super(context);
        setup(context);
    }


    private void setup(Context context) {
        vibrator        = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        music           = MediaPlayer.create(context, R.raw.ding);
        surfaceholder   = getHolder();
        background      = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        sensorManager   = (SensorManager) context.getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        } else {
            Log.e("SENSOR MANAGER", "cannot get method '.getDefaultSensor' from null");
        }

        setSensorListener();


        // Add balls
        for (int i = 1; i < 8; ++i) {
            for (int j = 1; j < 8; ++j) {
                balls.add(new Ball(120*i, 120*j));
            }
        }
    }


    private void setSensorListener() {
        SensorEventListener sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                ax = event.values[0] / 10;
                ay = event.values[1] / 10;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };

        sensorManager.registerListener(sensorListener,
                sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void run() {
        while(canDraw) {
            if (!surfaceholder.getSurface().isValid())  continue;

            canvas = surfaceholder.lockCanvas();
            canvas.drawBitmap(background, 0, 0, null);

            draw();

            surfaceholder.unlockCanvasAndPost(canvas);
        }
    }
    public void resume() {
        canDraw = true;

        thread = new Thread(this);
        thread.start();
    }
    public void pause() {
        canDraw = false;

        for(;;) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                Log.e("CANVAS", e.getMessage());
            }
        }

        thread = null;
    }



    private void draw() {
        for (int i = 0; i < balls.size(); ++i) {
            balls.get(i).draw();
        }

        handleCollisions();
    }





    private void handleCollisions() {
        float tvx, tvy;
        Ball a, b;
        for (int i = 0; i < balls.size() - 1; ++i) {
            for (int j = i + 1; j < balls.size(); ++j) {
                a = balls.get(i); b = balls.get(j);
                if (checkBallCollisions(a, b)) {
                    tvx = a.vx; tvy = a.vy;

                    a.vx = b.vx; a.vy = b.vy;
                    b.vx = tvx; b.vy = tvy;
                }
            }
        }
    }


    private boolean checkBallCollisions(Ball a, Ball b) {
        float dx  = Math.abs((a.x - a.vx) - (b.x - b.vx)),
              dy  = Math.abs((a.y + a.vy) - (b.y + b.vy)),
              min = a.radius + b.radius;

        return (dx < min && dy < min);
    }










    public class Ball {
        private float x, y, vx = 0, vy = 0, radius = 20;
        Paint paint = new Paint();

        private Ball(float X, float Y) {
            this.x = X; this.y = Y;

            this.paint.setColor(Color.MAGENTA);
            this.paint.setStyle(Paint.Style.FILL);
        }


        private void draw() {
            vx += ax; vy += ay;

            x -= vx; y += vy;

            checkCollision();

            canvas.drawCircle(this.x, this.y, this.radius, this.paint);
        }

        private void checkCollision() {
            if (this.x - this.radius < 0) {
                this.x = this.radius;
                this.vx *= -0.7f;
                notifyCollision();
            } else if (this.x + this.radius > canvas.getWidth()){
                this.x = canvas.getWidth() - this.radius;
                this.vx *= -0.7f;
                notifyCollision();
            }
            if (this.y - this.radius < 0) {
                this.y = this.radius;
                this.vy *= -0.7f;
                notifyCollision();
            } else if (this.y + this.radius > canvas.getHeight()){
                this.y = canvas.getHeight() - this.radius;
                this.vy *= -0.7f;
                notifyCollision();
            }
        }

        private void notifyCollision() {
            if ( !music.isPlaying() )     music.start();

            music.seekTo(100);
            vibrator.vibrate(10);
        };
    }

}
