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
import java.util.TreeSet;
import java.util.Iterator;

public class ChartView extends View {

    public static final int TYPE_WINPROB = 1;
    public static final int TYPE_IMPORTANCE = 2;
    public static final int TYPE_IMPORTANCE_WIN = 3;

    private static final int IMPORTANCE_WIN_PARTS = 10;
   
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
    
    public ChartView(Context c, AttributeSet attr) {
        super(c, attr);
        Resources res = getContext().getResources();
        DisplayMetrics met = res.getDisplayMetrics();
        float dpr = met.densityDpi / DisplayMetrics.DENSITY_DEFAULT;

        bluePaint = new Paint();
        bluePaint.setColor(0xff4285f4);
        bluePaint.setStrokeWidth(2*dpr);
        bluePaint.setAntiAlias(true);

        redPaint = new Paint();
        redPaint.setColor(0xffdb4437);
        redPaint.setStrokeWidth(2*dpr);
        redPaint.setAntiAlias(true);
        
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
        
        scalePaint = new Paint();
        scalePaint.setColor(0xff888888);
        scalePaint.setAntiAlias(true);
        scalePaint.setTextAlign(Paint.Align.CENTER);
        scalePaint.setTextSize(14*dpr);
        
        labelA = "";
        labelB = "";
        type = TYPE_WINPROB;
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
                    canvas.drawText((50*i/n+50)+"% ", ls, ys+h/2+y+halfText, textPaint);
                    canvas.drawLine(ls, ys+h/2-y, ls+w, ys+h/2-y, indicatorPaint);
                    canvas.drawText((50*i/n+50)+"% ", ls, ys+h/2-y+halfText, textPaint);
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
                    canvas.drawText((20*i/n)+"% ", ls, ys+h-y+halfText, textPaint);
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
                                    linePaint = bluePaint;
                                    break;
                                case TYPE_IMPORTANCE:
                                    val = point.importance * 5;
                                    linePaint = redPaint;
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
                TreeSet<Float> imps = new TreeSet<Float>();
                for(Match.Set set : match.sets) {
                    for(Match.Game game : set.games) {
                        for(Match.Point point : game.points) {
                            if(point.winner != 0) imps.add(point.importance);
                        }
                    }
                }
                int size = imps.size();
                int n = Math.min(size, IMPORTANCE_WIN_PARTS);
                if(n > 0) {
                    float minImp = imps.first();
                    float maxImp = imps.last();
                    Log.i("ChartView", n + ", " + minImp + ", " + maxImp);
                    float[] imps0 = new float[size];
                    int index = 0;
                    for(Iterator<Float> it = imps.iterator(); it.hasNext();) {
                        imps0[index++] = it.next();
                    }
                    float[] scale = new float[n];
                    int j = 0;
                    for(int i = 0; i < n; i++) {
                        float val = minImp + i * (maxImp - minImp);
                        boolean b = false;
                        while(size - j > n - i && imps0[j] < val) {
                            j++;
                            b = true;
                        }
                        if(b) {
                            if(Math.abs(imps0[j-1] - val) < Math.abs(imps0[j] - val)) {
                                scale[i] = imps0[j-1];
                            }else {
                                scale[i] = imps0[j];
                                j++;
                            }
                        }else {
                            scale[i] = imps0[j];
                            j++;
                        }
                    }
                    int[] data = new int[n];
                    int[] ns = new int[n];
                    int maxN = 0;
                    for(Match.Set set : match.sets) {
                        for(Match.Game game : set.games) {
                            for(Match.Point point : game.points) {
                                for(int i = 0; i < n; i++) {
                                    if(i == n - 1 || Math.abs(scale[i+1] - point.importance) >= Math.abs(scale[i] - point.importance)) {
                                        if(point.winner == 1) data[i]++;
                                        if(point.winner != 0) ns[i]++;
                                        maxN = Math.max(Math.max(maxN, data[i]), ns[i] - data[i]);
                                        break;
                                    }
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
                        canvas.drawText(val+"", ls, ys+h-y+halfText, textPaint);
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
                        canvas.drawText((int)(scale[i]*1000)/10f+"%", ls + xt, ys + h + 2 * halfText, scalePaint);
                    }
                }else {
                    int nlines = 4;
                    canvas.drawLine(ls, ys+h, ls+w, ys+h, axisPaint);
                    canvas.drawText("0", ls, ys+h+halfText, textPaint);
                    for(int i = 1; i <= nlines; i++) {
                        int val = i;
                        int y = val*h/nlines;
                        canvas.drawLine(ls, ys+h-y, ls+w, ys+h-y, indicatorPaint);
                        canvas.drawText(i+"", ls, ys+h-y+halfText, textPaint);
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
