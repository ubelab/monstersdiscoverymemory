package com.invenktion.monstersdiscoverymemory.core;

import com.invenktion.monstersdiscoverymemory.R;
import com.invenktion.monstersdiscoverymemory.R.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class DisegnoButton extends ImageButton{
	public static int NASCOSTO = 0;
	public static int VISIBILE = 1;
	
	private int immagineGabbia = R.drawable.coperto;
	private int immagineMostro;
	private int stato = NASCOSTO;
	
	public DisegnoButton(Context context) {
		super(context);
		setImageResource(immagineGabbia);
		setBackgroundResource(R.drawable.white_btn);

		setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if(stato == NASCOSTO) {
					DisegnoButton.this.setImageResource(getImmagineMostro());
					stato = VISIBILE;
				}else {
					DisegnoButton.this.setImageResource(getImmagineGabbia());
					stato = NASCOSTO;
				}
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
