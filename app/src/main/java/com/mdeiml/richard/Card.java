package com.mdeiml.richard;
import android.widget.FrameLayout;
import android.widget.Checkable;
import android.content.Context;
import android.util.AttributeSet;
import android.support.v7.widget.CardView;

public class Card extends CardView implements Checkable {
    
    boolean checked;
    
    public Card(Context c, AttributeSet attr) {
        super(c, attr);
        setCardBackgroundColor(R.drawable.card_background);
        checked = false;
    }
    
    @Override
    public void setChecked(boolean p1) {
        setCardBackgroundColor(getContext().getColor(p1 ? R.color.card_background_selected : R.color.card_background));
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
