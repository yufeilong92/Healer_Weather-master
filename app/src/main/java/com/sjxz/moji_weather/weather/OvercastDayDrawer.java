package com.sjxz.moji_weather.weather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.sjxz.moji_weather.R;

import java.util.ArrayList;

/**
 * @author WYH_Healer
 * @email 3425934925@qq.com
 * Created by xz on 2017/2/13.
 * Role:
 */
public class OvercastDayDrawer extends BaseDrawer {
    private ArrayList<OvercastDayHolder> holders = new ArrayList<OvercastDayHolder>();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Context context;
    Bitmap bg;


    public OvercastDayDrawer(Context context, boolean isNight) {
        super(context, false);
        this.context = context;
        if (bg == null) {
            bg = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg_overcast_day);
        }
    }

    @Override
    public boolean drawWeather(Canvas canvas, float alpha) {
        if(alpha!=1){
            paint.setAlpha((int)(alpha*255));
        }else{
            paint.setAlpha(255);
        }
        canvas.drawBitmap(bg, new Rect(0, 0, bg.getWidth(), bg.getHeight()), new Rect(0, 0, width, height), paint);
        for (OvercastDayHolder holder : holders) {
            holder.updateRandom(canvas, holder.matrix, paint,alpha);
        }
        return true;
    }

    @Override
    protected void setSize(int width, int height) {
        super.setSize(width, height);
        if (this.holders.size() == 0) {
            for (int i = 0; i < 0; i++) {
                OvercastDayHolder holder = new OvercastDayHolder(context, width, height, new Matrix(), i);
                holders.add(holder);
            }
        }
    }

    public static class OvercastDayHolder{
        float initPositionX;
        float initPositionY;
        Bitmap frame;
        RectF box;
        RectF targetBox;
        int width;
        int position = 0;
        protected Matrix matrix ;

        public OvercastDayHolder(Context context, int width, int height, Matrix matrix, int i) {
            super();
            this.position = i;
            this.width = width;
            this.matrix=matrix;
            box = new RectF();
            targetBox = new RectF();


        }

        public void updateRandom(Canvas canvas, Matrix matrix, Paint paint, float alpha) {

        }
    }
}
