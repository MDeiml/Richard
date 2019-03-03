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
import android.util.Log;
import java.util.Locale;

public class ChartView extends View {

    public static final int TYPE_WINPROB = 1;
    public static final int TYPE_IMPORTANCE = 2;
    public static final int TYPE_IMPORTANCE_WIN = 3;

    private static final int IMPORTANCE_WIN_PARTS = 6;
   
    private Paint bluePaint;
    private Paint redPaint;
    private Paint axisPaint;
    private Paint indicatorPaint;
    private Paint textPaint;
    private Paint scalePaint;
    private String labelA;
    private String labelB;
    private Match match;
    private int type;

    private int[] data;
    private int[] ns;
    
    public ChartView(Context c, AttributeSet attr) {
        super(c, attr);
        Resources res = getContext().getResources();
        DisplayMetrics met = res.getDisplayMetrics();
        float dpr = met.densityDpi / DisplayMetrics.DENSITY_DEFAULT;

        bluePaint = new Paint();
        bluePaint.setColor(c.getResources().getColor(R.color.primaryLight));
        bluePaint.setStrokeWidth(2*dpr);
        bluePaint.setAntiAlias(true);
        bluePaint.setStrokeCap(Paint.Cap.ROUND);

        redPaint = new Paint();
        redPaint.setColor(c.getResources().getColor(R.color.primaryRedLight));
        redPaint.setStrokeWidth(2*dpr);
        redPaint.setAntiAlias(true);
        redPaint.setStrokeCap(Paint.Cap.ROUND);
        
        axisPaint = new Paint();
        axisPaint.setColor(c.getResources().getColor(R.color.textContent));
        axisPaint.setStrokeWidth(0);
        axisPaint.setStyle(Paint.Style.STROKE);

        indicatorPaint = new Paint();
        indicatorPaint.setColor(0xffdddddd);
        indicatorPaint.setStrokeWidth(0);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        
        textPaint = new Paint();
        textPaint.setColor(c.getResources().getColor(R.color.textContent));
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTextSize(14*dpr);
        
        scalePaint = new Paint();
        scalePaint.setColor(c.getResources().getColor(R.color.textContent));
        scalePaint.setAntiAlias(true);
        scalePaint.setTextAlign(Paint.Align.CENTER);
        scalePaint.setTextSize(14*dpr);
        
        labelA = "";
        labelB = "";
        type = TYPE_WINPROB;

        data = new int[IMPORTANCE_WIN_PARTS];
        ns = new int[IMPORTANCE_WIN_PARTS];
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

        switch(type) {
            case TYPE_WINPROB: {
                canvas.drawLine(ls, ys+h/2, ls+w, ys+h/2, axisPaint);
                canvas.drawText("50% ", ls, ys+h/2+halfText, textPaint);
                int n = 2;
                for(int i = 1; i <= n; i++) {
                    int y = i*h/n/2;
                    canvas.drawLine(ls, ys+h/2+y, ls+w, ys+h/2+y, indicatorPaint);
                    canvas.drawText(String.format(Locale.getDefault(), "%d%% ", 50*i/n+50), ls, ys+h/2+y+halfText, textPaint);
                    canvas.drawLine(ls, ys+h/2-y, ls+w, ys+h/2-y, indicatorPaint);
                    canvas.drawText(String.format(Locale.getDefault(), "%d%% ", 50*i/n+50), ls, ys+h/2-y+halfText, textPaint);
                }
                break;
            }
            case TYPE_IMPORTANCE: {
                canvas.drawLine(ls, ys+h, ls+w, ys+h, axisPaint);
                canvas.drawText("0% ", ls, ys+h+halfText, textPaint);
                int n = 4;
                for(int i = 1; i <= n; i++) {
                    int y = i*h/n;
                    canvas.drawLine(ls, ys+h-y, ls+w, ys+h-y, indicatorPaint);
                    canvas.drawText(String.format(Locale.getDefault(), "%d%% ", 20*i/n), ls, ys+h-y+halfText, textPaint);
                }
                break;
            }
        }

        if(match != null) {
            if(type == TYPE_WINPROB || type == TYPE_IMPORTANCE) {
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
                            float val = 0;
                            Paint linePaint = bluePaint;
                            switch(type) {
                                case TYPE_WINPROB:
                                    val = point.winProb;
                                    break;
                                case TYPE_IMPORTANCE:
                                    val = point.importance * 5;
                                    break;
                            }
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
            }else if(type == TYPE_IMPORTANCE_WIN) {
                float minImp = 10000;
                float maxImp = 0;
                int n = 0;
                for(Match.Set set : match.sets) {
                    for(Match.Game game : set.games) {
                        for(Match.Point point : game.points) {
                            if(point.winner != 0) {
                                minImp = Math.min(minImp, point.importance);
                                maxImp = Math.max(maxImp, point.importance);
                                n++;
                            }
                        }
                    }
                }
                n = Math.min(n, IMPORTANCE_WIN_PARTS);
                if(n > 0) {
                    float scale = n == 1 ? 1 : (maxImp - minImp) / (n - 1);
                    int maxN = 0;
                    for (int i = 0; i < n; i++) {
                        ns[i] = 0;
                        data[i] = 0;
                    }
                    for(Match.Set set : match.sets) {
                        for(Match.Game game : set.games) {
                            for(Match.Point point : game.points) {
                                if(point.winner != 0) {
                                    int i = Math.round((point.importance - minImp) / scale);
                                    if(point.winner == 1) data[i]++;
                                    ns[i]++;
                                    maxN = Math.max(Math.max(maxN, data[i]), ns[i] - data[i]);
                                }
                            }
                        }
                    }
                    int nlines = 4;
                    canvas.drawLine(ls, ys+h, ls+w, ys+h, axisPaint);
                    canvas.drawText("0", ls, ys+h+halfText, textPaint);
                    for(int i = 1; i <= nlines; i++) {
                        int val = maxN*i/nlines;
                        int y = val*h/maxN;
                        canvas.drawLine(ls, ys+h-y, ls+w, ys+h-y, indicatorPaint);
                        canvas.drawText(String.format(Locale.getDefault(), "%d", val), ls, ys+h-y+halfText, textPaint);
                    }
                    float entryWidth = w / n;
                    for(int i = 0; i < n; i++) {
                        float val1 = (float)data[i] / maxN;
                        float val2 = (float)(ns[i] - data[i]) / maxN;
                        int x0 = (int)(entryWidth * (i + 0.15));
                        int x1 = (int)(entryWidth * (i + 0.45));
                        int xt = (int)(entryWidth * (i + 0.5));
                        int x2 = (int)(entryWidth * (i + 0.55));
                        int x3 = (int)(entryWidth * (i + 0.85));
                        int y1 = Math.max(2, (int)(val1 * h));
                        int y2 = Math.max(2, (int)(val2 * h));
                        canvas.drawRect(ls + x0, ys + h, ls + x1, ys + h - y1, redPaint);
                        canvas.drawRect(ls + x2, ys + h, ls + x3, ys + h - y2, bluePaint);
                        canvas.drawText(String.format(Locale.getDefault(), "%.1f%%", (minImp + scale * i) * 100), ls + xt, ys + h + 2 * halfText, scalePaint);
                    }
                }else {
                    int nlines = 4;
                    canvas.drawLine(ls, ys+h, ls+w, ys+h, axisPaint);
                    canvas.drawText("0", ls, ys+h+halfText, textPaint);
                    for(int i = 1; i <= nlines; i++) {
                        int val = i;
                        int y = val*h/nlines;
                        canvas.drawLine(ls, ys+h-y, ls+w, ys+h-y, indicatorPaint);
                        canvas.drawText(String.format(Locale.getDefault(), "%d ", i), ls, ys+h-y+halfText, textPaint);
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

    public void setType(int type) {
        this.type = type;
    }

}
