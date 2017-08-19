package com.mdeiml.richard;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import java.util.ArrayList;

public class ChartView extends View {
   
    private Paint linePaint;
    private Paint axisPaint;
    private Paint indicatorPaint;
    private Paint textPaint;
    private String labelA;
    private String labelB;
    private Match match;
    
    public ChartView(Context c, AttributeSet attr) {
        super(c, attr);
        Resources res = getContext().getResources();
        DisplayMetrics met = res.getDisplayMetrics();
        float dpr = met.densityDpi / DisplayMetrics.DENSITY_DEFAULT;

        linePaint = new Paint();
        linePaint.setColor(0xff4285f4);
        linePaint.setStrokeWidth(2*dpr);
        linePaint.setAntiAlias(true);
        
        axisPaint = new Paint();
        axisPaint.setColor(0xff888888);
        axisPaint.setStrokeWidth(0);
        axisPaint.setStyle(Paint.Style.STROKE);

        indicatorPaint = new Paint();
        indicatorPaint.setColor(0xffdddddd);
        indicatorPaint.setStrokeWidth(0);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        
        textPaint = new Paint();
        textPaint.setColor(0xff888888);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(14*dpr);
        
        labelA = "";
        labelB = "";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        Resources res = getContext().getResources();
        DisplayMetrics met = res.getDisplayMetrics();
        float dpr = met.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        
        int w = canvas.getWidth() - getPaddingLeft() - getPaddingRight() - (int)(dpr*15);
        int h = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
        int xs = getPaddingLeft();
        int ys = getPaddingTop();

        int ls = xs + (int)(dpr*20);
        int halfText = (int)(dpr*7);
        canvas.drawLine(ls, ys+h/2, ls+w, ys+h/2, axisPaint);
        canvas.drawText("50% ", ls, ys+h/2+halfText, textPaint);
        int n = 2;
        for(int i = 1; i <= n; i++) {
            int y = i*h/n/2;
            canvas.drawLine(ls, ys+h/2+y+1, ls+w, ys+h/2+y+1, indicatorPaint);
            canvas.drawText((50*i/n+50)+"% ", ls, ys+h/2+y+1+halfText, textPaint);
            canvas.drawLine(ls, ys+h/2-y, ls+w, ys+h/2-y, indicatorPaint);
            canvas.drawText((50*i/n+50)+"% ", ls, ys+h/2-y+halfText, textPaint);
        }

        if(match != null) {
            int numEntries = 0;
            for(Match.Set set : match.sets) {
                for(Match.Game game : set.games) {
                    numEntries += game.points.size();
                }
            }
            numEntries = (numEntries/50+1)*50;
            float entryWidth = w/numEntries;
            float lastVal = 0;
            int index = 0;
            for(Match.Set set : match.sets) {
                for(Match.Game game : set.games) {
                    for(Match.Point point : game.points) {
                        float val = point.winProb;
                        if(index != 0) {
                            int x0 = (int)((index-1)*entryWidth);
                            int y0 = (int)((1-lastVal)*h);
                            int x1 = (int)(index*entryWidth);
                            int y1 = (int)((1-val)*h);
                            canvas.drawLine(ls+x0, ys+y0, ls+x1, ys+y1, linePaint);
                        }
                        lastVal = val;
                        index++;
                    }
                }
            }
        }
        
        canvas.drawText(labelA, xs+5*dpr, ys+15*dpr, textPaint);
        canvas.drawText(labelB, xs+5*dpr, ys+h-5*dpr, textPaint);
    }
    
    public void drawMatch(Match match) {
        this.match = match;
        invalidate();
    }
    
    public void setLabels(String a, String b) {
        labelA = a;
        labelB = b;
    }

}
