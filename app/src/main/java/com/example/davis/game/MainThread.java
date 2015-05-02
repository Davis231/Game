package com.example.davis.game;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainThread extends Thread{

    private int FPS = 30;
    private double avgFPS;
    private SurfaceHolder surfaceHolder;
    private GameMain gameMain;
    private boolean running;
    private static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder, GameMain gameMain){
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameMain = gameMain;
    }

    @Override
    public void run(){
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        long frameCount = 0;
        long targetTime = 1000 / FPS;

        while(running) {
            startTime = System.nanoTime();
            canvas = null;

            try{
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    this.gameMain.update();
                    this.gameMain.draw(canvas);
                }
            } catch(Exception e){}

            finally{
                if(canvas != null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis;

            try{
                this.sleep(waitTime);
            } catch(Exception e){}

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if(frameCount == FPS){
                avgFPS = 1000 / ((totalTime/frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
                System.out.println(avgFPS);
            }
        }
    }

    public void setRunning(boolean b){
        running = b;
    }
}