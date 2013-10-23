package com.invenktion.monstersdiscoverymemory;

import java.util.ArrayList;

import com.invenktion.monstersdiscoverymemory.core.ActivityHelper;
import com.invenktion.monstersdiscoverymemory.core.ApplicationManager;
import com.invenktion.monstersdiscoverymemory.core.DisegnoButton;
import com.invenktion.monstersdiscoverymemory.core.LevelManager;
import com.invenktion.monstersdiscoverymemory.core.SoundManager;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.app.Activity;
import android.app.KeyguardManager;

public class GameBoardActivity extends Activity {

	public static final String FACILE = "FACILE";
	public static final String MEDIO = "MEDIO";
	public static final String DIFFICILE = "DIFFICILE";
	public static final String IMPOSSIBILE = "IMPOSSIBILE";
	
	private String gamemode;
	LinearLayout rowContainer;
	
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
		}
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
			finish();
			return true;
		}
		return false;
	}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean finish = checkApplicationKill();
        if(finish) return;
        setContentView(R.layout.gameboard);
        
        //Salvo la modalità di gioco che mi è stata passata
        Bundle extras = getIntent().getExtras();
        if(extras !=null){
        	gamemode = extras.getString("gamemode");
        }
        
        rowContainer = (LinearLayout)findViewById(R.id.rowContainer);
        
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int H = metrics.heightPixels;
        int W = metrics.widthPixels;
        int N_DISEGNI_RIGA;
        int N_DISEGNI_COLONNA;
        
        if(FACILE.equals(gamemode)) {
        	N_DISEGNI_RIGA = 3;
        	N_DISEGNI_COLONNA = 2;
        }else if(MEDIO.equals(gamemode)){
        	N_DISEGNI_RIGA = 6;
        	N_DISEGNI_COLONNA = 3;
        }else if(DIFFICILE.equals(gamemode)) {
        	N_DISEGNI_RIGA = 8;
        	N_DISEGNI_COLONNA = 5;
        }else {
        	N_DISEGNI_RIGA = 14;
        	N_DISEGNI_COLONNA = 7;
        }
        
        int disegno_SIZE = (int)((double)H/(double)N_DISEGNI_COLONNA);
        ScaleType scaleType = ScaleType.FIT_XY;
        //SE nella direzione dove non è stato eseguito il calcolo si sfora dallo schermo, ricalcolo
        if(disegno_SIZE*N_DISEGNI_RIGA > W) {
        	disegno_SIZE = (int)((double)W/(double)N_DISEGNI_RIGA);
        }
        
        ApplicationManager.NUM_TO_WIN = (N_DISEGNI_COLONNA*N_DISEGNI_RIGA)/2;
        ApplicationManager.CURR_CORRECT = 0;
        
        ArrayList<Integer> lista = LevelManager.getRandomNx2DifferentImageResources(gamemode, (N_DISEGNI_COLONNA*N_DISEGNI_RIGA)/2);
        int current = 0;
        for(int r=0; r < N_DISEGNI_COLONNA; r++) {
        	LinearLayout rowLayout = new LinearLayout(getApplicationContext());
        	//rowLayout.setClipChildren(false);
        		rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        		rowContainer.addView(rowLayout);
        	for(int c=0; c<N_DISEGNI_RIGA; c++) {
        		DisegnoButton but = new DisegnoButton(getApplicationContext());
        			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(disegno_SIZE,disegno_SIZE);
        			but.setLayoutParams(params);
        			but.setScaleType(scaleType);
        			but.setImmagineMostro(lista.get(current));
        			current++;
        			rowLayout.addView(but);
        			/*
        			RotateAnimation anim = new RotateAnimation(0, 30,
        		            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);

        		    anim.setInterpolator(new LinearInterpolator());
        		    anim.setDuration(1000);
        		    anim.setFillEnabled(true);

        		    anim.setFillAfter(true);
        		    but.startAnimation(anim);
        		    */
        	}
        }
    }

}
