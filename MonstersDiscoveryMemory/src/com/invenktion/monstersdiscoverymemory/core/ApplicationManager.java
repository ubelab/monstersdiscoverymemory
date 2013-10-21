package com.invenktion.monstersdiscoverymemory.core;

public class ApplicationManager {

	public static String APPLICATION_KILLED = null;
	public static int CURRENT_SELECTION_NUMBER = -1;
	public static DisegnoButton CURRENT_SELECTION_TESSETA = null;
	public static int TENTATIVO_NUMERO = 0;
	public static boolean block = false;
	
	public static int SCREEN_H, SCREEN_W;
	public static String PREFS_NAME = "MONSTERS_D_MEMORY";
	
	public static void resetStatus() {
		CURRENT_SELECTION_NUMBER = -1;
		CURRENT_SELECTION_TESSETA = null;
		TENTATIVO_NUMERO = 0;
		block = false;
	}
}
