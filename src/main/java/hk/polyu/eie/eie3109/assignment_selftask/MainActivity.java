package hk.polyu.eie.eie3109.assignment_selftask;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    private AnimationView AnimationView;
    MediaPlayer song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnimationView = new AnimationView(this);
        setContentView(AnimationView);
        song = MediaPlayer.create(MainActivity.this,R.raw.mytune);
        song.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnimationView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AnimationView.onPause();
        song.release();
    }

    class AnimationView extends SurfaceView implements Runnable {

        private Thread gameThread;
        private SurfaceHolder ourHolder;
        private volatile boolean playing;
        private Canvas canvas;
        private Bitmap man;
        private boolean isMoving;
        private float runSpeedPerSecond = 300;
        private float manXPosition = 10, manYPosition = 10;
        private int width = 300, height = 400;
        private int count = 13;
        private int currentFrame = 0;
        private long fps;
        private long timeThisFrame;
        private long lastFrameChangeTime = 0;
        private int frameLengthInMillisecond = 200;
        private Rect frameToDraw = new Rect(1, 4, width, height);
        private RectF whereToDraw = new RectF(manXPosition, manYPosition, manXPosition + width, height);

        public AnimationView(Context context) {
            super(context);
            ourHolder = getHolder();
            man = BitmapFactory.decodeResource(getResources(), R.drawable.goku);
            man = Bitmap.createScaledBitmap(man, width * count, height, false);
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();
                update();
                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;

                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update() {
            if (isMoving) {
                manXPosition = manXPosition + runSpeedPerSecond / fps;

                if (manXPosition > getWidth()) {
                    manYPosition += height;
                    manXPosition = 10;
                }

                if (manYPosition + height > getHeight()) {
                    manYPosition = 10;
                }
            }
        }

        public void manageCurrentFrame() {
            long time = System.currentTimeMillis();

            if (isMoving) {
                if (time > lastFrameChangeTime + frameLengthInMillisecond) {
                    lastFrameChangeTime = time;
                    currentFrame++;

                    if (currentFrame >= count) {
                        currentFrame = 0;
                    }
                }
            }

            frameToDraw.left = currentFrame * width;
            frameToDraw.right = frameToDraw.left + width;
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                whereToDraw.set( manXPosition,manYPosition, manXPosition + width, manYPosition + height);
                manageCurrentFrame();
                canvas.drawBitmap(man, frameToDraw, whereToDraw, null);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void onPause() {
            playing = false;
            try {
                gameThread.join();
            } catch(InterruptedException e) {
                Log.e("ERR", "Joining Thread");
            }
        }

        public void onResume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN :
                    isMoving = !isMoving;
                    break;
            }

            return true;
        }
    }
}
