package com.invenktion.monstersdiscoverymemory;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;
import android.app.Activity;

public class MainActivity extends Activity {

	LinearLayout rowContainer;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        rowContainer = (LinearLayout)findViewById(R.id.rowContainer);
        
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int H = metrics.heightPixels;
        int W = metrics.widthPixels;
        int N_DISEGNI_RIGA = 6;
        int N_DISEGNI_COLONNA = 3;
        int disegno_SIZE = (int)((double)H/(double)N_DISEGNI_COLONNA);
        
        //SE nella direzione dove non è stato eseguito il calcolo si sfora dallo schermo, ricalcolo
        if(disegno_SIZE*N_DISEGNI_RIGA > W) {
        	disegno_SIZE = (int)((double)W/(double)N_DISEGNI_RIGA);
        }
        
        for(int r=0; r < N_DISEGNI_COLONNA; r++) {
        	LinearLayout rowLayout = new LinearLayout(getApplicationContext());
        		rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        		rowContainer.addView(rowLayout);
        	for(int c=0; c<N_DISEGNI_RIGA; c++) {
        		DisegnoButton but = new DisegnoButton(getApplicationContext());
        			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(disegno_SIZE,disegno_SIZE);
        			but.setLayoutParams(params);
        			but.setScaleType(ScaleType.FIT_CENTER);
        			but.setImmagineMostro(LevelManager.getRandomImageResource());
        			
        			rowLayout.addView(but);
        	}
        }
    }

}
