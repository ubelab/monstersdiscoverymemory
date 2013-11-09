package com.invenktion.monstersdiscoverymemory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.invenktion.monstersdiscoverymemory.core.ActivityHelper;
import com.invenktion.monstersdiscoverymemory.core.AnimationFactory;
import com.invenktion.monstersdiscoverymemory.core.ApplicationManager;
import com.invenktion.monstersdiscoverymemory.core.DisegnoButton;
import com.invenktion.monstersdiscoverymemory.core.FontFactory;
import com.invenktion.monstersdiscoverymemory.core.LevelManager;
import com.invenktion.monstersdiscoverymemory.core.SoundManager;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;

public class GameBoardActivity extends FragmentActivity {

	private static final String TAG = "GameBoardActivity";
	
	public static final String FACILE = "NEWBIES";
	public static final String MEDIO = "NATURAL";
	public static final String DIFFICILE = "MONSTROUS";
	public static final String IMPOSSIBILE = "IMPOSSIBLE";
	
	private String gamemode;
	LinearLayout rowContainer;
	
	//FACEBOOK
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = 
	    new Session.StatusCallback() {
	    @Override
	    public void call(Session session, 
	            SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};

	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permissions)) {
	            Session.NewPermissionsRequest newPermissionsRequest = new Session
	                    .NewPermissionsRequest(this, PERMISSIONS);
	        session.requestNewPublishPermissions(newPermissionsRequest);
	            return;
	        }
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	    }
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    try{
	    	uiHelper.onSaveInstanceState(outState);
	    }catch (Exception e) {
			e.printStackTrace();//altrimenti crashava
		}
	}
	
	@Override
	protected void onDestroy() {
		//fb
		try{
			uiHelper.onDestroy();
		}catch (Exception e) {
			e.printStackTrace();//altrimenti crashava
		}
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    try{
	    	uiHelper.onActivityResult(requestCode, resultCode, data);
	    }catch (Exception e) {
			e.printStackTrace();//altrimenti crashava
		}
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	//fb
		try {
			uiHelper.onResume();
		}catch (Exception e) {
			e.printStackTrace();//altrimenti crashava
		}
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		showExitDialog();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
	
    @Override
	protected void onPause() {
		super.onPause();
		try{
			uiHelper.onPause();
		}catch (Exception e) {
			e.printStackTrace();//altrimenti crashava
		}
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
        
        //FACEBOOK
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        //loginButton = (LoginButton)findViewById(R.id.authButton);
        
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
        ApplicationManager.TENTATIVO_NUMERO  =0;
        
        ArrayList<Integer> lista = LevelManager.getRandomNx2DifferentImageResources(gamemode, (N_DISEGNI_COLONNA*N_DISEGNI_RIGA)/2);
        int current = 0;
        for(int r=0; r < N_DISEGNI_COLONNA; r++) {
        	LinearLayout rowLayout = new LinearLayout(getApplicationContext());
        	//rowLayout.setClipChildren(false);
        		rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        		rowContainer.addView(rowLayout);
        	for(int c=0; c<N_DISEGNI_RIGA; c++) {
        		final DisegnoButton but = new DisegnoButton(getApplicationContext());
        			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(disegno_SIZE,disegno_SIZE);
        			but.setLayoutParams(params);
        			but.setScaleType(scaleType);
        			but.setImmagineMostro(lista.get(current));
        			current++;
        			rowLayout.addView(but);
        			but.setOnClickListener(new OnClickListener() {		
        				@Override
        				public void onClick(View v) {
        					
        					if(but.bloccata || ApplicationManager.block) return;
        					ApplicationManager.block = true;
        					//Dalla prima volta che la si vede l'immagine con il punto di domanda non deve più apparire
        					//immagineEnigma = immagineCoperto;
        					Animation anim = AnimationFactory.getTesseraAnimation(getApplicationContext());
        					anim.setAnimationListener(new AnimationListener() {
        						
        						@Override
        						public void onAnimationStart(Animation animation) {
        							but.setImageResource(but.getImmagineMostro());
        						}
        						
        						@Override
        						public void onAnimationRepeat(Animation animation) {
        							// TODO Auto-generated method stub
        							
        						}
        						
        						@Override
        						public void onAnimationEnd(Animation animation) {
        							if(ApplicationManager.CURRENT_SELECTION_NUMBER != -1) {
        								//Siamo alla seconda tessera scoperchiata, controllo se è uguale all'altra
        								if(but.getImmagineMostro() == ApplicationManager.CURRENT_SELECTION_TESSETA.getImmagineMostro()) {
        									//VITTORIA
        									but.bloccata = true;
        									ApplicationManager.CURR_CORRECT++;
        									if(ApplicationManager.CURR_CORRECT == ApplicationManager.NUM_TO_WIN) {//VITTORIA
        										SoundManager.playSound(SoundManager.SOUND_POSITIVE, getApplicationContext(), false);
        										showWinnerDialog();
        										publishToFacebook(gamemode,(ApplicationManager.TENTATIVO_NUMERO+1)+"");
        									}else {
        										SoundManager.playSound(SoundManager.SOUND_POSITIVE, getApplicationContext(), false);
        									}
        								}else {
        									//FALLIMENTO
        									but.setImageResource(but.getImmagineCoperto());
        									ApplicationManager.CURRENT_SELECTION_TESSETA.bloccata = false;
        									ApplicationManager.CURRENT_SELECTION_TESSETA.setImageResource(ApplicationManager.CURRENT_SELECTION_TESSETA.getImmagineCoperto());
        									SoundManager.playSound(SoundManager.SOUND_NEGATIVE, getApplicationContext(), false);
        								}
        								ApplicationManager.TENTATIVO_NUMERO ++;
        								ApplicationManager.CURRENT_SELECTION_NUMBER = -1;
        								
        							}else {
        								but.bloccata = true;
        								ApplicationManager.CURRENT_SELECTION_NUMBER = 1;
        								ApplicationManager.CURRENT_SELECTION_TESSETA = but;
        							}
        							ApplicationManager.block = false;
        						}
        					});
        					but.startAnimation(anim);
        				}
        			});
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

    public static class WinnerDialogFragment extends DialogFragment {
	    
	    /**
	     * Create a new instance of WinnerDialogFragment
	     */
	    static WinnerDialogFragment newInstance() {
	    	WinnerDialogFragment f = new WinnerDialogFragment();

	        // Supply num input as an argument.
	        Bundle args = new Bundle();
	        //args.putInt("num", num);
	        f.setArguments(args);

	        return f;
	    }

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	       
	        // Pick a style
	        int style = DialogFragment.STYLE_NORMAL;
	        setStyle(style, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
	    }

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.winner_dialog, container, false);
	        //Font
	        TextView textView = (TextView)v.findViewById(R.id.winnertext);
	        textView.setTypeface(FontFactory.getFont1(getActivity().getApplicationContext()));
	        
	        ((TextView)v.findViewById(R.id.tentativilabel)).setTypeface(FontFactory.getFont1(getActivity().getApplicationContext()));
	        
	        TextView tentativiTextView = (TextView)v.findViewById(R.id.tentativitext);
	        tentativiTextView.setTypeface(FontFactory.getFont1(getActivity().getApplicationContext()));
	        tentativiTextView.setText(ApplicationManager.TENTATIVO_NUMERO+"");

	        //sul click chiudo la partita, altrimenti il doppio dialog sta male
	        RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.winner_fragment_dialog_root);
	        rl.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
	            	((GameBoardActivity)getActivity()).finish();
				}
			});
	        
	        return v;
	    }
	}
	
    public static class ExitDialogFragment extends DialogFragment {
	    
	    /**
	     * Create a new instance of ExitDialogFragment
	     */
	    static ExitDialogFragment newInstance() {
	    	ExitDialogFragment f = new ExitDialogFragment();

	        // Supply num input as an argument.
	        Bundle args = new Bundle();
	        //args.putInt("num", num);
	        f.setArguments(args);

	        return f;
	    }

	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	       
	        // Pick a style
	        int style = DialogFragment.STYLE_NORMAL;
	        setStyle(style, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
	    }

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View v = inflater.inflate(R.layout.exit_dialog, container, false);
	        //Font
	        TextView textView = (TextView)v.findViewById(R.id.exittext);
	        textView.setTypeface(FontFactory.getFont1(getActivity().getApplicationContext()));

	        // Watch for button clicks.
	        Button yesButton = (Button)v.findViewById(R.id.yes);
	        Button noButton = (Button)v.findViewById(R.id.no);
	        yesButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	dismiss();
	            	((GameBoardActivity)getActivity()).finish();
	            }
	        });
	        noButton.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            	dismiss();
	            }
	        });

	        return v;
	    }
	}
	
	public void showExitDialog() {
       
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("exitdialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = ExitDialogFragment.newInstance();
        newFragment.show(ft, "exitdialog");
    }
    
	public void showWinnerDialog() {
       
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("winnerdialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = WinnerDialogFragment.newInstance();
        newFragment.setCancelable(false);
        newFragment.show(ft, "winnerdialog");
    }
	
	private void publishToFacebook(String level, String attempts){
		//FACEBOOK pubblico su bacheca utente i risultati di gioco
		try{
			Session session = Session.getActiveSession();
			//pubblico solo ai nuovi record (ossia almeno una volta per quadro nuovo durante il gioco arcade)
		    if (session != null && !session.isClosed()){

		        // Check for publish permissions    
		        List<String> permissions = session.getPermissions();
		        if (!isSubsetOf(PERMISSIONS, permissions)) {
		            pendingPublishReauthorization = true;
		            Session.NewPermissionsRequest newPermissionsRequest = new Session
		                    .NewPermissionsRequest(this, PERMISSIONS);
		        session.requestNewPublishPermissions(newPermissionsRequest);
		            return;
		        }
		        
		        Bundle postParams = new Bundle();
		        postParams.putString("name", "Memory Monsters Discovery");
		        postParams.putString("caption", "Level "+level+" completed!");
		        postParams.putString("description", "Level completed with "+attempts+" attempts!");
		        postParams.putString("link", "https://play.google.com/store/apps/details?id=com.invenktion.monstersdiscoverymemory");
		        postParams.putString("picture", "www.invenktion.com/images/mdm.png");

		        Request.Callback callback= new Request.Callback() {
		            public void onCompleted(Response response) {
		            	try {
    		                JSONObject graphResponse = response
    		                                           .getGraphObject()
    		                                           .getInnerJSONObject();
    		                String postId = null;
    		                try {
    		                    postId = graphResponse.getString("id");
    		                } catch (JSONException e) {
    		                    Log.i(TAG,
    		                        "JSON error "+ e.getMessage());
    		                }
    		                FacebookRequestError error = response.getError();
    		                if (error != null) {
    		                    Toast.makeText(GameBoardActivity.this.getApplicationContext(),
    		                         error.getErrorMessage(),
    		                         Toast.LENGTH_SHORT).show();
    		                    } else {
    		                    	/*
    		                        Toast.makeText(DrawChallengeActivity.this
    		                             .getApplicationContext(), 
    		                             "Facebook post send with success",
    		                             Toast.LENGTH_SHORT).show();
    		                             */
		                }
		            	}catch(Exception e){e.printStackTrace();}
		            }
		        };

		        Request request = new Request(session, "me/feed", postParams, 
		                              HttpMethod.POST, callback);

		        RequestAsyncTask task = new RequestAsyncTask(request);
		        task.execute();
		    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}
}
