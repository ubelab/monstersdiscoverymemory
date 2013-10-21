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
	private int immagineEnigma = R.drawable.coperto_enigma;
	private int immagineMostro;
	private boolean bloccata = false;
	
	public DisegnoButton(final Context context) {
		super(context);
		setSoundEffectsEnabled(false);
		setImageResource(immagineEnigma);
		setBackgroundResource(R.drawable.white_btn);

		setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				
				if(bloccata || ApplicationManager.block) return;
				ApplicationManager.block = true;
				//Dalla prima volta che la si vede l'immagine con il punto di domanda non deve più apparire
				immagineEnigma = immagineCoperto;
				Animation anim = AnimationFactory.getTesseraAnimation(context);
				anim.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						DisegnoButton.this.setImageResource(getImmagineMostro());
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						if(ApplicationManager.CURRENT_SELECTION_NUMBER != -1) {
							//Siamo alla seconda tessera scoperchiata, controllo se è uguale all'altra
							if(DisegnoButton.this.getImmagineMostro() == ApplicationManager.CURRENT_SELECTION_TESSETA.getImmagineMostro()) {
								//VITTORIA
								bloccata = true;
								//SoundManager.playSound(R.raw.positive, context, false);
							}else {
								//FALLIMENTO
								DisegnoButton.this.setImageResource(DisegnoButton.this.getImmagineCoperto());
								ApplicationManager.CURRENT_SELECTION_TESSETA.setImageResource(ApplicationManager.CURRENT_SELECTION_TESSETA.getImmagineCoperto());
								//SoundManager.playSound(R.raw.negative, context, false);
							}
							ApplicationManager.TENTATIVO_NUMERO ++;
							ApplicationManager.CURRENT_SELECTION_NUMBER = -1;
							
						}else {
							ApplicationManager.CURRENT_SELECTION_NUMBER = 1;
							ApplicationManager.CURRENT_SELECTION_TESSETA = DisegnoButton.this;
						}
						ApplicationManager.block = false;
					}
				});
				DisegnoButton.this.startAnimation(anim);
			}
		});
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
