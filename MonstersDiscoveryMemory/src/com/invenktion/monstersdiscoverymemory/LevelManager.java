package com.invenktion.monstersdiscoverymemory;

public class LevelManager {
	private static int[] disegni = new int[]{
		R.drawable.w1_monster1,
		R.drawable.w1_monster2,
		R.drawable.w1_monster3,
		R.drawable.w1_monster4,
		R.drawable.w1_monster5,
		R.drawable.w1_monster6,
		R.drawable.w1_monster7,
		R.drawable.w1_monster8,
		R.drawable.w1_monster9,
		R.drawable.w1_monster10,
		
		R.drawable.w2_monster1,
		R.drawable.w2_monster2,
		R.drawable.w2_monster3,
		R.drawable.w2_monster4,
		R.drawable.w2_monster5,
		R.drawable.w2_monster6,
		R.drawable.w2_monster7,
		R.drawable.w2_monster8,
		
		R.drawable.w3_monster1,
		R.drawable.w3_monster2,
		R.drawable.w3_monster3,
		R.drawable.w3_monster4,
		R.drawable.w3_monster5,
		R.drawable.w3_monster6,
		R.drawable.w3_monster7,
		R.drawable.w3_monster8,
		R.drawable.w3_monster9,
		R.drawable.w3_monster10,
		
		R.drawable.w4_monster1,
		R.drawable.w4_monster2,
		R.drawable.w4_monster3,
		R.drawable.w4_monster4,
		R.drawable.w4_monster5,
		R.drawable.w4_monster6,
		R.drawable.w4_monster7,
		R.drawable.w4_monster8,
		R.drawable.w4_monster9,
		R.drawable.w4_monster10,
		
		R.drawable.w5_monster1,
		R.drawable.w5_monster2,
		R.drawable.w5_monster3,
		R.drawable.w5_monster4,
		R.drawable.w5_monster5,
		R.drawable.w5_monster6,
		R.drawable.w5_monster7,
		R.drawable.w5_monster8,
		R.drawable.w5_monster9,
		R.drawable.w5_monster10,
		
		R.drawable.wb_monster1,
		R.drawable.wb_monster2,
		R.drawable.wb_monster3,
		R.drawable.wb_monster4,
		R.drawable.wb_monster5
	};
	
	public static int getRandomImageResource(){
		int n= (int)(Math.random() * disegni.length);
		if(n < 0) n = 0;
		else if(n >= disegni.length) n = disegni.length -1;
		return disegni[n];
	}
}
