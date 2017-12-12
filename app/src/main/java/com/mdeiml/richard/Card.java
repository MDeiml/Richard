package com.mdeiml.richard;
import android.widget.FrameLayout;
import android.widget.Checkable;
import android.content.Context;
import android.util.AttributeSet;

public class Card extends FrameLayout implements Checkable {
    
    boolean checked;
    
    public Card(Context c, AttributeSet attr) {
        super(c, attr);
        setBackgroundResource(R.drawable.card_background);
        checked = false;
    }
    
    @Override
    public void setChecked(boolean p1) {
        setBackgroundResource(p1 ? R.drawable.card_background_selected : R.drawable.card_background);
        checked = p1;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        setChecked(!checked);
    }
    
}
