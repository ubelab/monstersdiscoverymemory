package com.invenktion.monstersdiscoverymemory;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class DisegnoButton extends ImageButton{

	private int immagineGabbia = R.drawable.coperto;
	private int immagineMostro;
	
	public DisegnoButton(Context context) {
		super(context);
		setImageResource(immagineGabbia);
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				DisegnoButton.this.setImageResource(getImmagineMostro());
				return false;
			}
		});
	}

	public int getImmagineGabbia() {
		return immagineGabbia;
	}



	public void setImmagineGabbia(int immagineGabbia) {
		this.immagineGabbia = immagineGabbia;
	}



	public int getImmagineMostro() {
		return immagineMostro;
	}



	public void setImmagineMostro(int immagineMostro) {
		this.immagineMostro = immagineMostro;
	}

}
