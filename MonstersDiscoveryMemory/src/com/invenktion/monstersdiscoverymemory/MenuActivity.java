package com.invenktion.monstersdiscoverymemory;

import com.invenktion.monstersdiscoverymemory.core.ActivityHelper;
import com.invenktion.monstersdiscoverymemory.core.AnimationFactory;
import com.invenktion.monstersdiscoverymemory.core.ApplicationManager;
import com.invenktion.monstersdiscoverymemory.core.FontFactory;
import com.invenktion.monstersdiscoverymemory.core.ScreenReceiver;
import com.invenktion.monstersdiscoverymemory.core.SoundManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuActivity extends Activity {
	//Typeface font; 
	float DENSITY = 1.0f;

	BroadcastReceiver mReceiver;
	
	static final int DIALOG_EXIT_APPLICATION = 0;
	private boolean waiting = false;
	private boolean waitingAudio = false;
	
	private ImageView soundImage;
	
	@Override
	protected void onDestroy() {
		//Rilascio l'animazione sulla faccia di Jhonny
		if(findViewById(R.id.facejhonny) != null) {
			ImageView faceJhonny = ((ImageView)findViewById(R.id.facejhonny));
			if(faceJhonny != null) {
				faceJhonny.clearAnimation();
				faceJhonny.setAnimation(null);
			}
		}
		
		//Rilascio tutte le risorse audio del SoundPool
		SoundManager.finalizeSounds();
		//AnimationFactory.releaseAllAnimation();
		//Log.d("Sound finalized!","### Sound finalized! ###");
		if(mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		//Log.e("MenuActivity","DESTROY MenuActivity ####################");
		super.onDestroy();
	}
	
	//Crea il particolare dialog una volta sola
    //Per riconfigurarlo usare onPrepareDialog
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;
        switch(id) {
        case DIALOG_EXIT_APPLICATION:
        	// prepare the custom dialog
			dialog = new Dialog(this);//con l'app context non si aprono
			dialog.setCancelable(false);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.exit_application_dialog);
			//dialog.setTitle("Custom Dialog");
			dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialogbg);
			
			TextView textExit = (TextView)dialog.findViewById(R.id.textexit);
			textExit.setTypeface(FontFactory.getFont1(getApplicationContext()));
			
			final ImageView yesButton = (ImageView) dialog.findViewById(R.id.yesButton);
			yesButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
				        case MotionEvent.ACTION_UP:
				        	yesButton.setEnabled(false);
				        	dialog.dismiss();
							finish();
							overridePendingTransition(0,0);
				            break;
					}
					return true;
				}
			});
			
			final ImageView noButton = (ImageView) dialog.findViewById(R.id.noButton);
			noButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
				        case MotionEvent.ACTION_UP:
				        	noButton.setEnabled(false);
				        	waiting = false;
							noButton.setEnabled(true);
							dialog.dismiss();
				            break;
					}
					return true;
				}
			});
            break;
        default:
            dialog = null;
        }
        return dialog;
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	//Log.e("KEY BACK PRESSED","KEY BACK PRESSED");
	    	try {
	    		if(waiting) return false;
		    	else {
		    		waiting = true;
		    		showDialog(DIALOG_EXIT_APPLICATION);
					return true;
		    	}
			}catch (Exception e) {
				e.printStackTrace();
			}
	        return false;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		
		//Rilancio la musica se e solo se non è già attiva
		//Questo ci permette di utilizzare la stessa traccia musicale tra Activity differenti, oltre
		//al metodo presente nel onPause che controlla se siamo o no in background
		KeyguardManager keyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Activity.KEYGUARD_SERVICE);  
    	boolean bloccoSchermoAttivo = keyguardManager.inKeyguardRestrictedInputMode();
		if(!bloccoSchermoAttivo && !SoundManager.isBackgroundMusicPlaying()) {
			SoundManager.playBackgroundMusic(getApplicationContext());
			//Update SOUND UI ICON di conseguenza
			if(SoundManager.SOUND_ON) {
				if(soundImage != null) {
					soundImage.setImageResource(R.drawable.soundon);
				}
			}else {
				if(soundImage != null) {
					soundImage.setImageResource(R.drawable.soundoff);
				}
			}
		}

		waiting = false;
		waitingAudio = false;
		//LogUtils.logHeap();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//Spengo la musica solo se un'altra applicazione è davanti alla nostra (VOICE CALL, HOME Button, etc..)
		if(ActivityHelper.isApplicationBroughtToBackground(this)) {
			SoundManager.pauseBackgroundMusic();
		}
	}

	
	private boolean checkApplicationKill() {
		if(ApplicationManager.APPLICATION_KILLED == null) {
			Intent myIntent = new Intent(MenuActivity.this, SplashScreenActivity.class);
    		MenuActivity.this.startActivity(myIntent);
    		overridePendingTransition(0,0);
			finish();
			return true;
		}
		return false;
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean finish = checkApplicationKill();
        if(finish) return;
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
        
        setContentView(R.layout.home);
        
        this.DENSITY = getApplicationContext().getResources().getDisplayMetrics().density;
        
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.homelayout);

        ImageView facileBtn = (ImageView)findViewById(R.id.textuno);
        //TextView nuovaPartita = (TextView)findViewById(R.id.textdue);
        ImageView medioBtn = (ImageView)findViewById(R.id.textcinque);
        //TextView tutorial = (TextView)findViewById(R.id.textquattro);
        ImageView difficileBtn = (ImageView)findViewById(R.id.texttre);
        //ImageView impossibileBtn = (ImageView)findViewById(R.id.impossibile);
       
        facileBtn.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
			        case MotionEvent.ACTION_UP:
			        	if(waiting) return false;
			        	waiting = true;
			        	Intent myIntent = new Intent(MenuActivity.this, GameBoardActivity.class);
		        		myIntent.putExtra("gamemode", GameBoardActivity.FACILE);
		        		MenuActivity.this.startActivity(myIntent);
		        		//Set the transition -> method available from Android 2.0 and beyond  
		        		overridePendingTransition(0,0); 
			            break;
				}
				return true;
			}
		});
        medioBtn.setOnTouchListener(new OnTouchListener() {
			
			
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
			        case MotionEvent.ACTION_UP:
			        	if(waiting) return false;
			        	waiting = true;
			        	Intent myIntent = new Intent(MenuActivity.this, GameBoardActivity.class);
		        		myIntent.putExtra("gamemode", GameBoardActivity.MEDIO);
		        		MenuActivity.this.startActivity(myIntent);
		        		overridePendingTransition(0,0);
			            break;
				}
				return true;
			}
		});
        difficileBtn.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
			        case MotionEvent.ACTION_UP:
			        	if(waiting) return false;
			        	waiting = true;
			        	Intent myIntent = new Intent(MenuActivity.this, GameBoardActivity.class);
		        		myIntent.putExtra("gamemode", GameBoardActivity.DIFFICILE);
		        		MenuActivity.this.startActivity(myIntent);
		        		overridePendingTransition(0,0);
			            break;
				}
				return true;
			}
		});
        /*
        impossibileBtn.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
			        case MotionEvent.ACTION_UP:
			        	if(waiting) return false;
			        	waiting = true;
			        	Intent myIntent = new Intent(MenuActivity.this, GameBoardActivity.class);
		        		myIntent.putExtra("gamemode", GameBoardActivity.IMPOSSIBILE);
		        		MenuActivity.this.startActivity(myIntent);
		        		overridePendingTransition(0,0);
			            break;
				}
				return true;
			}
		});
		*/
        
        
        //FISSO LA DIMENSIONE DEI TRE BOTTONI IN MODO PERCENTUALE RISPETTO ALLO SCHERMO
        //COSI VANNO BENE IN OGNI SCERMO, GRANDE, MEDIO O PICCOLO E OGNI DENSITA'
        double proporzioniPlay = 3.85;
        double proporzioniAltriBtn = 5.5;
        
        int W_PLAY = (int)((double)ApplicationManager.SCREEN_W / (double)2.5);
        int H_PLAY = (int)(W_PLAY / proporzioniPlay);
        
        int W_BTN = (int)((double)ApplicationManager.SCREEN_W / (double)2.5);
        int H_BTN = (int)(W_BTN / proporzioniAltriBtn);
        
        facileBtn.setLayoutParams(new LinearLayout.LayoutParams(W_PLAY,H_PLAY));
        medioBtn.setLayoutParams(new LinearLayout.LayoutParams(W_BTN,H_BTN));
        difficileBtn.setLayoutParams(new LinearLayout.LayoutParams(W_BTN,H_BTN));
        //impossibileBtn.setLayoutParams(new LinearLayout.LayoutParams(W_BTN,H_BTN));
        
        final ImageView creditsImage = (ImageView)findViewById(R.id.creditsimage);
        creditsImage.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
			        case MotionEvent.ACTION_UP:
			        	if(waiting) return false;
			        	waiting = true;
			        	Intent myIntent = new Intent(MenuActivity.this, CreditsActivity.class);
		        		MenuActivity.this.startActivity(myIntent);
		        		overridePendingTransition(0,0);
			            break;
				}
				return true;
			}
		});
        
        soundImage = (ImageView)findViewById(R.id.soundimage);
        
        //Imposto l'immagine sulla base della preferenza dell'utente (sound on/off)
        String soundState = SoundManager.getSoundPreference(getApplicationContext());
        if(SoundManager.SOUND_ENABLED.equalsIgnoreCase(soundState)) {
        	soundImage.setImageResource(R.drawable.soundon);
        }else {
        	soundImage.setImageResource(R.drawable.soundoff);
        }
        
        soundImage.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
			        case MotionEvent.ACTION_UP:
			        	if(waitingAudio) return false;
			        	waitingAudio = true;
			        	new Thread() {
							public void run() {
								if(SoundManager.SOUND_ON) {
									SoundManager.SOUND_ON = false;
									SoundManager.pauseBackgroundMusic();
									SoundManager.saveSoundPreference(SoundManager.SOUND_DISABLED, getApplicationContext());
									runOnUiThread(new Runnable() {
										public void run() {
											soundImage.setImageResource(R.drawable.soundoff);
										}
									});
								}else {
									SoundManager.SOUND_ON = true;
									SoundManager.playBackgroundMusic(getApplicationContext());
									SoundManager.saveSoundPreference(SoundManager.SOUND_ENABLED, getApplicationContext());
									runOnUiThread(new Runnable() {
										public void run() {
											soundImage.setImageResource(R.drawable.soundon);
										}
									});
								}
								waitingAudio = false;
							};
						}.start();
			            break;
				}
				return true;
			}
		});
        
        
       
        //Imposto il volume all'inizio, cosi l'utente poi lo controlla solo con i tasti del device
        SoundManager.initVolume(getApplicationContext());
        
        ImageView faceJhonny = (ImageView)findViewById(R.id.facejhonny);
        
        Animation rotAnim = AnimationFactory.getJhonnyFaceAnimation(getApplicationContext());
        rotAnim.setFillAfter(true);rotAnim.setFillBefore(true);
        faceJhonny.setAnimation(rotAnim);
    }
}
