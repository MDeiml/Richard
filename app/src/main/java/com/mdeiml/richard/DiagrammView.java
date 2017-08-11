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

public class DiagrammView extends View {
   
    private Paint linePaint;
    private Paint boxPaint;
    private Paint textPaint;
    private ArrayList<Float> vals;
    private String labelA;
    private String labelB;
    
    public DiagrammView(Context c, AttributeSet attr) {
        super(c, attr);
        linePaint = new Paint();
        linePaint.setColor(0xff0000ff);
        linePaint.setStrokeWidth(2);
        linePaint.setAntiAlias(true);
        
        boxPaint = new Paint();
        boxPaint.setColor(0xff000000);
        boxPaint.setStrokeWidth(2);
        boxPaint.setStyle(Paint.Style.STROKE);
        
        textPaint = new Paint();
        textPaint.setColor(0xff000000);
        textPaint.setAntiAlias(true);
        Resources res = getContext().getResources();
        DisplayMetrics met = res.getDisplayMetrics();
        float dpr = met.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        textPaint.setTextSize(10*dpr);
        
        vals = new ArrayList<>();
        
        labelA = "";
        labelB = "";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        Resources res = getContext().getResources();
        DisplayMetrics met = res.getDisplayMetrics();
        float dpr = met.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        
        int w = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        int h = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
        int xs = getPaddingLeft();
        int ys = getPaddingTop();
        canvas.drawRect(xs, ys, xs+w, ys+h, boxPaint);
        canvas.drawLine(xs, ys+h/2, xs+w, ys+h/2, boxPaint);
        int numEntries = (vals.size()/50+1)*50;
        float entryWidth = w/numEntries;
        for(int i = 1; i < vals.size(); i++) {
            int x0 = (int)((i-1)*entryWidth);
            int y0 = (int)((1-vals.get(i-1))*h);
            int x1 = (int)(i*entryWidth);
            int y1 = (int)((1-vals.get(i))*h);
            canvas.drawLine(xs+x0, ys+y0, xs+x1, ys+y1, linePaint);
        }
        
        canvas.drawText(labelA, xs+5*dpr, ys+15*dpr, textPaint);
        canvas.drawText(labelB, xs+5*dpr, ys+h-5*dpr, textPaint);
    }
    
    public void addValue(float val) {
        vals.add(val);
        invalidate();
    }
    
    public void clear() {
        vals.clear();
    }
    
    public void setLabels(String a, String b) {
        labelA = a;
        labelB = b;
    }

        @Override
        public Parcelable onSaveInstanceState() {
            //begin boilerplate code that allows parent classes to save state
            Parcelable superState = super.onSaveInstanceState();

            SavedState ss = new SavedState(superState);
            //end

            ss.vals = new float[vals.size()];
            for(int i = 0; i < vals.size(); i++) {
                ss.vals[i] = vals.get(i);
            }
            ss.labelA = labelA;
            ss.labelB = labelB;

            return ss;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            //begin boilerplate code so parent classes can restore state
            if(!(state instanceof SavedState)) {
                super.onRestoreInstanceState(state);
                return;
            }

            SavedState ss = (SavedState)state;
            super.onRestoreInstanceState(ss.getSuperState());
            //end

            vals.clear();
            for(int i = 0; i < ss.vals.length; i++) {
                vals.add(ss.vals[i]);
            }
            labelA = ss.labelA;
            labelB = ss.labelB;
        }

        static class SavedState extends BaseSavedState {
            float[] vals;
            String labelA, labelB;

            SavedState(Parcelable superState) {
                super(superState);
            }

            private SavedState(Parcel in) {
                super(in);
                int l = in.readInt();
                this.vals = new float[l];
                in.readFloatArray(vals);
                this.labelA = in.readString();
                this.labelB = in.readString();
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);
                out.writeInt(this.vals.length);
                out.writeFloatArray(vals);
                out.writeString(labelA);
                out.writeString(labelB);
            }

            //required field that makes Parcelables from a Parcel
            public static final Parcelable.Creator<SavedState> CREATOR =
            new Parcelable.Creator<SavedState>() {
                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in);
                }
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
        }
    
}
