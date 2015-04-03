package com.example.davis.game;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameMain extends SurfaceView implements SurfaceHolder.Callback{

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int GAMESPEED = -1;

    private MainThread thread;
    private Screen bg;
    private Player player;

    public GameMain(Context context)
    {
        super(context);

        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        while(retry)
        {
            try{thread.setRunning(false);
                thread.join();

            }catch(InterruptedException e){e.printStackTrace();}
            retry = false;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Screen(BitmapFactory.decodeResource(getResources(), R.drawable.night_bg));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.flying_cat), 100, 56, 1);

        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(!player.getPlaying()){
                player.setPlaying(true);
            }

            else{
                player.setUp(true);
            }
            return true;
        }

        if(event.getAction()== MotionEvent.ACTION_UP){
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update(){
        if(player.getPlaying()) {
            bg.update();
            player.update();
        }
    }
    @Override
    public void draw(Canvas canvas){
        final float scaleX = getWidth()/(WIDTH * 1.f);
        final float scaleY = getHeight()/(HEIGHT * 1.f);

        if(canvas!=null){
            final int savedState = canvas.save();
            canvas.scale(scaleX, scaleY);
            bg.draw(canvas);
            player.draw(canvas);
            canvas.restoreToCount(savedState);
        }
    }
}
