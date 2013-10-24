package com.invenktion.monstersdiscoverymemory;

import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusOneButton;
import com.invenktion.monstersdiscoverymemory.core.ActivityHelper;
import com.invenktion.monstersdiscoverymemory.core.AnimationFactory;
import com.invenktion.monstersdiscoverymemory.core.ApplicationManager;
import com.invenktion.monstersdiscoverymemory.core.FontFactory;
import com.invenktion.monstersdiscoverymemory.core.ScreenReceiver;
import com.invenktion.monstersdiscoverymemory.core.SoundManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;


public class MenuActivity extends Activity implements
ConnectionCallbacks, OnConnectionFailedListener{
	//Typeface font; 
	float DENSITY = 1.0f;
	
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private ProgressDialog mConnectionProgressDialog;
    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;

	PlusOneButton mPlusOneButton;
	
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
	// The request code must be 0 or greater.
	private static final int PLUS_ONE_REQUEST_CODE = 0;

	@Override
	protected void onResume() {
		super.onResume();
		// Aggiorna lo stato del pulsante +1 ogni volta che l'attività riceve lo stato attivo.
		mPlusOneButton.initialize("https://play.google.com/store/apps/details?id=com.invenktion.monstersdiscovery&hl=it", PLUS_ONE_REQUEST_CODE);


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
	
	@Override
    protected void onStart() {
        super.onStart();
        mPlusClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPlusClient.disconnect();
    }

    public void onConnected() {
        String accountName = mPlusClient.getAccountName();
        Toast.makeText(this, accountName + " is connected.", Toast.LENGTH_LONG).show();
    }

    public void onDisconnected() {
        Log.d("", "disconnected");
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
        
        mPlusClient = new PlusClient.Builder(this, this, this)
		        .setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
		        .build();
		// Barra di avanzamento da visualizzare se l'errore di connessione non viene risolto.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");


        mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);

        
        this.DENSITY = getApplicationContext().getResources().getDisplayMetrics().density;
        
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.homelayout);

        ImageView facileBtn = (ImageView)findViewById(R.id.textuno);
        //TextView nuovaPartita = (TextView)findViewById(R.id.textdue);
        ImageView medioBtn = (ImageView)findViewById(R.id.textcinque);
        //TextView tutorial = (TextView)findViewById(R.id.textquattro);
        ImageView difficileBtn = (ImageView)findViewById(R.id.texttre);
        ImageView impossibileBtn = (ImageView)findViewById(R.id.impossibile);
       
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
        
        
        //FISSO LA DIMENSIONE DEI TRE BOTTONI IN MODO PERCENTUALE RISPETTO ALLO SCHERMO
        //COSI VANNO BENE IN OGNI SCERMO, GRANDE, MEDIO O PICCOLO E OGNI DENSITA'
        double proporzioniPlay = 3.85;
        double proporzioniAltriBtn = 5.5;
        
        int W_PLAY = (int)((double)ApplicationManager.SCREEN_W / (double)2.5);
        int H_PLAY = (int)(W_PLAY / proporzioniPlay);
        
        int W_BTN = (int)((double)ApplicationManager.SCREEN_W / (double)3);
        int H_BTN = (int)(W_BTN / proporzioniAltriBtn);
        
        facileBtn.setLayoutParams(new LinearLayout.LayoutParams(W_PLAY,H_PLAY));
        medioBtn.setLayoutParams(new LinearLayout.LayoutParams(W_BTN,H_BTN));
        difficileBtn.setLayoutParams(new LinearLayout.LayoutParams(W_BTN,H_BTN));
        impossibileBtn.setLayoutParams(new LinearLayout.LayoutParams(W_BTN,H_BTN));
        
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

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (mConnectionProgressDialog.isShowing()) {
            // L'utente ha già fatto clic sul pulsante di accesso. Inizia a risolvere
            // gli errori di connessione. Attendi fino a onConnected() per eliminare la
            // finestra di dialogo di connessione.
            if (result.hasResolution()) {
                    try {
                            result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                    } catch (SendIntentException e) {
                            mPlusClient.connect();
                    }
            }
    }

    // Salva l'intent in modo che sia possibile avviare un'attività quando l'utente fa clic
    // sul pulsante di accesso.
    mConnectionResult = result;

	}

	@Override
	public void onConnected(Bundle arg0) {
		// Abbiamo risolto ogni errore di connessione.
		  mConnectionProgressDialog.dismiss();

	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
	    if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
	        mConnectionResult = null;
	        mPlusClient.connect();
	    }
	}

}
