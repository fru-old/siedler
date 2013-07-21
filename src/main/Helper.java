package main;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;

import main.Server.Field;

public class Helper {

	public static Random random = new Random(System.currentTimeMillis());
	
	public static <U> U removeRandomElement(ArrayList<U> list){
		return list.remove(random.nextInt(list.size()));
	}
	
	public static <U> ArrayList<U> selectSubList(ArrayList<U> in, int startOffset, int count){
		ArrayList<U> out = new ArrayList<U>();
		for(int i = 0; i < count; i++)out.add(in.get(i+startOffset));
		return out;
	}
	
	public static void assertion(boolean t){
		if(!t){
			System.err.println("Assertion failed!");
			new Exception().printStackTrace();
			System.exit(1);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static class BuildingList{
		private ArrayList<Field> list = new ArrayList<Field>();
		private ArrayList<Integer> position = new ArrayList<Integer>();
	
		public void setIcon(int value){
			for(int i = 0; i<list.size();i++)list.get(i).building[position.get(i)].setIcon(Main.BUILDING_IMAGES[value]);
		}
		
		public void add(Field field, int p){
			list.add(field);
			position.add(p);
		}
		
		public int size(){
			return list.size();
		}
		
		public Field getField(int i){
			return list.get(i);
		}
		
		public int getPosition(int i){
			return position.get(i);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static class StreetList{
		private ArrayList<Field> list = new ArrayList<Field>();
		private ArrayList<Integer> position = new ArrayList<Integer>();
	
		public void setIcon(int color){
			for(int i = 0; i<list.size();i++)list.get(i).street[position.get(i)].setIcon(Main.getStreetImage(color, position.get(i)));
		}

		public void add(Field field, int p){
			list.add(field);
			position.add(p);
		}
		
		public int size(){
			return list.size();
		}
		
		public Field getField(int i){
			return list.get(i);
		}
		
		public int getPosition(int i){
			return position.get(i);
		}
	}
	
	public static ArrayList<Field> createFieldList(int count){
		ArrayList<Field> out = new ArrayList<Field>();
		for(int i = 0; i < count; i++)out.add(new Field());
		return out;
	}

	public static ImageIcon getImage(String s){
		return new ImageIcon(Server.class.getResource(s));
	}
}
