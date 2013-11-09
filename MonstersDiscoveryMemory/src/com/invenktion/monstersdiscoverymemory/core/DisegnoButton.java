package com.invenktion.monstersdiscoverymemory.core;

import com.invenktion.monstersdiscoverymemory.R;
import com.invenktion.monstersdiscoverymemory.R.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;

public class DisegnoButton extends ImageButton{
	
	private int immagineCoperto = R.drawable.coperto;
	//private int immagineEnigma = R.drawable.coperto_enigma;
	private int immagineMostro;
	public boolean bloccata = false;
	
	public DisegnoButton(final Context context) {
		super(context);
		setSoundEffectsEnabled(false);
		setImageResource(immagineCoperto);
		setBackgroundResource(R.drawable.white_btn);
	}

	public int getImmagineCoperto() {
		return immagineCoperto;
	}



	public void setImmagineGabbia(int immagineGabbia) {
		this.immagineCoperto = immagineGabbia;
	}



	public int getImmagineMostro() {
		return immagineMostro;
	}



	public void setImmagineMostro(int immagineMostro) {
		this.immagineMostro = immagineMostro;
	}

}
