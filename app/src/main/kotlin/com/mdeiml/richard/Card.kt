package com.mdeiml.richard

import android.widget.Checkable
import android.content.Context
import android.util.AttributeSet
import android.support.v7.widget.CardView
import android.support.v4.content.ContextCompat

class Card(c: Context, attr: AttributeSet) : CardView(c, attr), Checkable {
    
    var checked1 = false
    
    init {
        setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundLight))
    }

    override fun setChecked(c: Boolean) { 
        this.setCardBackgroundColor(ContextCompat.getColor(this.context, if (c) R.color.card_background_selected else R.color.backgroundLight))
        checked1 = c
    }

    override fun isChecked() = checked1

    override fun toggle() {
        checked1 = !checked1
    }
    
}
