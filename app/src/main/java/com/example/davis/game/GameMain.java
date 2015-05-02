package com.example.davis.game;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

public class GameMain extends SurfaceView implements SurfaceHolder.Callback{

    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int GAMESPEED = -2;

    private MainThread thread;
    private Random rand = new Random();
    private AudioPlayer mPlayer = new AudioPlayer();
    private Screen bg;
    private Player player;

    private ArrayList<Bird> bird;
    private ArrayList<TopBorder> topBorder;
    private ArrayList<BottomBorder> bottomBorder;

    private long birdStartTime;
    private long startReset;

    private int maxBorderHeight;
    private int minBorderHeight;
    private int progressDenom = 20;
    private int highscore;

    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;
    private boolean reset;
    private boolean disappear;
    private boolean started;

    public GameMain(Context context){
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
        int counter = 0;

        while(retry && counter < 1000){
            counter++;

            try{
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        bg = new Screen(BitmapFactory.decodeResource(getResources(), R.drawable.night_bg));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.flying_cat), 85, 38, 1);

        bird = new ArrayList<>();
        topBorder = new ArrayList<>();
        bottomBorder = new ArrayList<>();

        birdStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();

        //mPlayer.play(getActivity());
        //mPlayer.loop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(!player.getPlaying()){
                player.setPlaying(true);
                player.setUp(true);
            }

            if(player.getPlaying()){
                if(!started){
                    started = true;
                }
                reset = false;
                player.setUp(true);
            }

            return true;
        }

        if(event.getAction() == MotionEvent.ACTION_UP){
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update(){
        if(player.getPlaying()){

            if(bottomBorder.isEmpty()){
                player.setPlaying(false);
                return;
            }

            if(topBorder.isEmpty()){
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            maxBorderHeight = 30 + player.getScore() / progressDenom;

            if(maxBorderHeight > HEIGHT / 4){
                maxBorderHeight = HEIGHT / 4;
            }

            minBorderHeight = 5 + player.getScore() / progressDenom;

           for(int i = 0; i < bottomBorder.size(); i++){
                if(collision(bottomBorder.get(i), player)){
                    player.setPlaying(false);
                }
            }

            for(int i = 0; i <topBorder.size(); i++){
                if(collision(topBorder.get(i),player)){
                    player.setPlaying(false);
                }
            }

            this.updateTopBorder();
            this.updateBottomBorder();

            long birdElapsed = (System.nanoTime() - birdStartTime) / 1000000;

            if(birdElapsed >(2000 - player.getScore() / 4)){
                if(bird.size() == 0){
                    bird.add(new Bird(BitmapFactory.decodeResource(getResources(), R.drawable.bird),
                            WIDTH + 10, HEIGHT / 2, 48, 34, player.getScore(), 1));
                }

                else{
                    bird.add(new Bird(BitmapFactory.decodeResource(getResources(), R.drawable.bird),
                            WIDTH + 10, (int)(rand.nextDouble() * (HEIGHT)), 48, 34, player.getScore(), 1));
                }

                birdStartTime = System.nanoTime();
            }

            for(int i = 0; i < bird.size(); i++){
                bird.get(i).update();

                if(collision(bird.get(i), player)){
                    bird.remove(i);
                    player.setPlaying(false);
                    break;
                }

                if(bird.get(i).getX() <- 100){
                    bird.remove(i);
                    break;
                }
            }
        }

        else{
            player.resetDY();

            if(!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
            }

            long resetElapsed = (System.nanoTime()-startReset) / 1000000;

            if(resetElapsed > 2500 && !newGameCreated){
                newGame();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b){
        if(Rect.intersects(a.getRect(), b.getRect())){
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas){
        final float scaleX = getWidth() / (WIDTH * 1.f);
        final float scaleY = getHeight() / (HEIGHT * 1.f);

        if(canvas!=null){
            final int savedState = canvas.save();

            canvas.scale(scaleX, scaleY);
            bg.draw(canvas);
            player.draw(canvas);

            for(Bird b: bird){
                b.draw(canvas);
            }

            for(TopBorder tb: topBorder){
                tb.draw(canvas);
            }

            for(BottomBorder bb: bottomBorder){
                bb.draw(canvas);
            }

            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public void updateTopBorder(){
        if(player.getScore() % 50 == 0){
            topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                    topBorder.get(topBorder.size() - 1).getX() + 20, 0, (int)((rand.nextDouble() * (maxBorderHeight)) + 1)));
        }

        for(int i = 0; i < topBorder.size(); i++){
            topBorder.get(i).update();

            if(topBorder.get(i).getX() < -20){
                topBorder.remove(i);

                if(topBorder.get(topBorder.size() - 1).getHeight() >= maxBorderHeight){
                    topDown = false;
                }

                if(topBorder.get(topBorder.size()-1).getHeight() <= minBorderHeight){
                    topDown = true;
                }

                if(topDown){
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.border),topBorder.get(topBorder.size()-1).getX()+20,
                            0, topBorder.get(topBorder.size()-1).getHeight()+1));
                }

                else{
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.border),topBorder.get(topBorder.size() - 1).getX() + 20,
                            0, topBorder.get(topBorder.size() - 1).getHeight() - 1));
                }
            }
        }
    }
    public void updateBottomBorder(){
        if(player.getScore() % 40 == 0){
            bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                    bottomBorder.get(bottomBorder.size() - 1).getX() + 20,
                    (int)((rand.nextDouble() * maxBorderHeight) + (HEIGHT-maxBorderHeight))));
        }

        for(int i = 0; i < bottomBorder.size(); i++){
            bottomBorder.get(i).update();

            if(bottomBorder.get(i).getX() < -20) {
                bottomBorder.remove(i);

                if(bottomBorder.get(bottomBorder.size() - 1).getY() <= HEIGHT-maxBorderHeight){
                    botDown = true;
                }
                if(bottomBorder.get(bottomBorder.size() - 1).getY() >= HEIGHT - minBorderHeight){
                    botDown = false;
                }

                if (botDown) {
                    bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                            bottomBorder.get(bottomBorder.size() - 1).getX() + 20, bottomBorder.get(bottomBorder.size() - 1).getY() + 1));
                } else {
                    bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                            bottomBorder.get(bottomBorder.size() - 1).getX() + 20, bottomBorder.get(bottomBorder.size() - 1).getY() - 1));
                }
            }
        }
    }

    public void newGame(){
        disappear = false;

        bottomBorder.clear();
        topBorder.clear();
        bird.clear();

        minBorderHeight = 10;
        maxBorderHeight = 40;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT / 2);

        if(player.getScore() > highscore){
            highscore = player.getScore();
        }

        for(int i = 0; i * 20 < WIDTH + 40; i++){
            if(i == 0){
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                        i * 20, 0, 10));
            }
            else{
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.border),
                        i * 20, 0, topBorder.get(i-1).getHeight()+1));
            }
        }

        for(int i = 0; i * 20 < WIDTH + 40; i++){
            if(i == 0){
                bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                        i * 20, HEIGHT - minBorderHeight));
            }
            else{
                bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.border),
                        i * 20, bottomBorder.get(i - 1).getY() - 1));
            }
        }

        newGameCreated = true;
    }

    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore() * 3), 10, HEIGHT - 10, paint);
        canvas.drawText("HIGHSCORE: " + highscore, WIDTH - 250, HEIGHT - 10, paint);

        if(!player.getPlaying() && newGameCreated && reset){
            Paint paint1 = new Paint();

            paint1.setTextSize(50);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("SUPER GATTO", WIDTH/2 - 50, HEIGHT/2 - 75, paint1);

            paint1.setTextSize(25);
            canvas.drawText("TAP TO START", WIDTH/2 - 52, HEIGHT/2 - 50, paint1);

            paint1.setTextSize(50);
            paint1.setColor(Color.WHITE);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("SUPER GATTO", WIDTH/2 - 55, HEIGHT/2 - 75, paint1);

            paint1.setTextSize(25);
            paint1.setColor(Color.WHITE);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("TAP TO START", WIDTH/2 - 55, HEIGHT/2 - 50, paint1);
        }
    }
}