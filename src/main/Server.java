package main;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JLabel;



public class Server {

	


	
	public static Random random = new Random(System.currentTimeMillis());
	
	
	
	
	

 	
	public static class Field{
		

		
		int probability = 0;
		
		JLabel probabilityText = new JLabel();
		JLabel probabilityIcon = new JLabel();
		
		JLabel resource = new JLabel();
		
		JLabel[] street = {new JLabel(),new JLabel(),new JLabel()}; 
		JLabel[] building = {new JLabel(),new JLabel()}; 
		
		Field[] neighbour = {null, null, null, null};
		
		public Field ur(){return neighbour[Main.UP+Main.RIGHT];}
		public Field ul(){return neighbour[Main.UP+Main.LEFT];}
		public Field dr(){return neighbour[Main.DOWN+Main.RIGHT];}
		public Field dl(){return neighbour[Main.DOWN+Main.LEFT];}
		public Field upup(){return  ur() != null?ur().neighbour[Main.UP+Main.LEFT]:null;}
		public Field dodo(){return  dr() != null?dr().neighbour[Main.DOWN+Main.LEFT]:null;}
		public Field ml(){return ul() != null?ul().neighbour[Main.DOWN+Main.LEFT]:null;}
		public Field mr(){return ur() != null?ur().neighbour[Main.DOWN+Main.RIGHT]:null;}
		
		public void setProbabilityVisibility(boolean visible){
			if(probability != 0f){
				if(visible){
					probabilityIcon.setIcon(Main.RAMEN_IMAGE);
					probabilityText.setText(""+probability);
					if(probability == 1f || probability == 2f || probability == 3f)
						probabilityText.setText(""+probabilityText.getText().charAt(0));
				}else{
					probabilityIcon.setIcon(null);
					probabilityText.setText("");
				}
			}	
		}
		
		public void init(ArrayList<Integer> resourceCopy, ArrayList<Integer> probabilityCopy){
			if(ur() != null && ul() != null && dr() != null && dl() != null){
				resource.setIcon(Main.ROHSTOFF_IMAGES[Helper.removeRandomElement(resourceCopy)]);
				probability = Helper.removeRandomElement(probabilityCopy);
			}
		}
	}
	
	public static final long standardRecognitionDelay = 800L;

	public static Field[] dice(){
		final Field[] out = {null,null};
		new Thread() {
			public void run() {
				
				int traceLength = random.nextInt(7)+1;
				Field[] firstTrace = new Field[traceLength];
				Field[] secondTrace = new Field[traceLength];
				
				//random = new Random(System.currentTimeMillis());
				int countProbabilities = 0;
				for(int i = 0; i < Main.PROBEBILITY_FIELDS.size(); i++)countProbabilities += Main.PROBEBILITY_FIELDS.get(i);
				
				int firstResult = random.nextInt(countProbabilities);
				int secondResult = random.nextInt(countProbabilities);
				int accumulativeProbabilities = 0;
				for(Field f : Server.fields){
					if(f.probability != 0){
						accumulativeProbabilities += f.probability;
						if(firstResult<accumulativeProbabilities){firstTrace[0] = f; firstResult = Integer.MAX_VALUE;}
						if(secondResult<accumulativeProbabilities){secondTrace[0] = f; secondResult = Integer.MAX_VALUE;}
					}
				}
				
				out[0] = firstTrace[0];
				out[1] = secondTrace[0];
				
				for(int i = 1; i < traceLength; i++){
					do{firstTrace[i] = firstTrace[i-1].neighbour[random.nextInt(4)];}while(firstTrace[i] == null || firstTrace[i].probability == 0);
					do{secondTrace[i] = secondTrace[i-1].neighbour[random.nextInt(4)];}while(secondTrace[i] == null || secondTrace[i].probability == 0 || secondTrace[i] == firstTrace[i]);
				}
				
				for(int i = traceLength -1; i > 0; i--){
					firstTrace[i].probabilityIcon.setIcon(Main.COIN_IMAGE);
					secondTrace[i].probabilityIcon.setIcon(Main.COIN_IMAGE);
					try {Thread.sleep(100L);} catch (Exception e) {}
					firstTrace[i].probabilityIcon.setIcon(null);
					secondTrace[i].probabilityIcon.setIcon(null);
				}
				firstTrace[0].probabilityIcon.setIcon(Main.COIN_IMAGE);
				secondTrace[0].probabilityIcon.setIcon(Main.COIN_IMAGE);
				try {Thread.sleep(250L);} catch (Exception e) {}
				firstTrace[0].probabilityIcon.setIcon(null);
				secondTrace[0].probabilityIcon.setIcon(null);
				try {Thread.sleep(100L);} catch (Exception e) {}
				firstTrace[0].probabilityIcon.setIcon(Main.COIN_IMAGE);
				secondTrace[0].probabilityIcon.setIcon(Main.COIN_IMAGE);
				//TODO results add
				try {Thread.sleep(standardRecognitionDelay);} catch (Exception e) {}
				firstTrace[0].probabilityIcon.setIcon(null);
				secondTrace[0].probabilityIcon.setIcon(null);
			}
		  }.run();
		  return out;
	}
	
	/*
	 * Board Elements and initialisation
	 */

	public static ArrayList<Field> fields = Helper.createFieldList(37);
	
	public static ArrayList<Field> middle = Helper.selectSubList(fields, 0, 7);
	public static ArrayList<Field> down1 = Helper.selectSubList(fields, 7, 6);
	public static ArrayList<Field> down2 = Helper.selectSubList(fields, 13, 5);
	public static ArrayList<Field> down3 = Helper.selectSubList(fields, 18, 4);
	public static ArrayList<Field> up1 = Helper.selectSubList(fields, 22, 6);
	public static ArrayList<Field> up2 = Helper.selectSubList(fields, 28, 5);
	public static ArrayList<Field> up3 = Helper.selectSubList(fields, 33, 4);
	
	public static void init(){
		connect(connect(connect(middle,down1,false),down2,false),down3,false);
		connect(connect(connect(middle,up1,true),up2,true),up3,true);
		
		@SuppressWarnings("unchecked")ArrayList<Integer> resourceCopy = (ArrayList<Integer>)Main.RESOURCE_FIELDS.clone();
		@SuppressWarnings("unchecked")ArrayList<Integer> probabilityCopy = (ArrayList<Integer>)Main.PROBEBILITY_FIELDS.clone();
		
		for(Field f : Server.fields){
			f.init(resourceCopy, probabilityCopy);
		}
	}
	
	public static ArrayList<Field> connect(ArrayList<Field> leftRow, ArrayList<Field> rightRow, boolean leftBelowRight){
		Field lastRight = null;
		for(int i = 0; i<leftRow.size(); i++){
			Field left = leftRow.get(i);
			Field right = rightRow.size()>i?rightRow.get(i):null;
			connect(left,right,leftBelowRight);
			connect(lastRight,left,!leftBelowRight);
			lastRight = right;
		}
		return rightRow;
		
	}
	
	public static void connect(Field left,Field right,boolean leftBelowRight){
		if(left != null && right != null){
			if(leftBelowRight){
				left.neighbour[Main.UP+Main.RIGHT] = right;
				right.neighbour[Main.DOWN+Main.LEFT] = left;
			}else{
				left.neighbour[Main.DOWN+Main.RIGHT] = right;
				right.neighbour[Main.UP+Main.LEFT] = left;
			}
		}
	}

	
	
	
	
	


}
