package com.example.davis.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Screen {
    private Bitmap image;
    private int x;
    private int y;
    private int dx;

    public Screen(Bitmap res){
        image = res;
        dx = GameMain.GAMESPEED;
    }

    public void update(){
        x += dx;

        if(x < -GameMain.WIDTH){
            x = 0;
        }
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(image, x, y, null);

        if(x < 0){
            canvas.drawBitmap(image, x + GameMain.WIDTH, y, null);
        }
    }
}