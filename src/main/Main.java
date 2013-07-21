package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.Server.Field;

public class Main {
	
	public static AI[] player;
	
	public static void main(String[] args){
		
		Server.init();
		
		@SuppressWarnings("unchecked") ArrayList<Integer> colorCopy = (ArrayList<Integer> )COLOR.clone();
		
		player = new AI[]{	new AI().setColor(Helper.removeRandomElement(colorCopy)),
						new AI().setColor(Helper.removeRandomElement(colorCopy)),
						new AI().setColor(Helper.removeRandomElement(colorCopy)),
						Human.singleton.setColor(Helper.removeRandomElement(colorCopy))};	

		final JFrame w = new JFrame();
		JPanel p =  new JPanel(null){
			private static final long serialVersionUID = 8390218429482709636L;
			@Override public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(new Color(238,238,238));
				int width = 4;
				g.fillRoundRect(width, width, getWidth()-2*width, getHeight()-2*width, 18, 18);
			}
		};
		
		p.setBackground(Color.BLACK);
		
		Human.singleton.createBoardPanel(p);
		
		
		w.setContentPane(p);
		w.setUndecorated(true);
		w.pack();
		w.setAlwaysOnTop(true);
		 
		com.sun.awt.AWTUtilities.setWindowShape(w, new RoundRectangle2D.Float(0,0,w.getWidth(),w.getHeight(),20,20));
		JLabel l = new JLabel(new ImageIcon(Server.class.getResource("/images/close.png")));
		l.setBounds(p.getWidth()-28, -1, 30, 30);
		l.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}
		});
		p.add(l);
		w.setLocationRelativeTo(null);
		w.setVisible(true);
		
		for(Field f : Server.fields){
			f.setProbabilityVisibility(true);
		}
		
		for(int i = 1; i < 5; i++){
			try {Thread.sleep(1500L);} catch (Exception e) {}
			getAiColor(i).firstBuild();
		}
		
		for(int i = 4; i > 0; i--){
			try {Thread.sleep(1500L);} catch (Exception e) {}
			getAiColor(i).secondBuild();
		}
		
		for(Field f : Server.fields){
			f.setProbabilityVisibility(false);
		}
		
		while(true){
			for(int j = 1; j < 5; j++){
				Field[] out = Server.dice();
				for(AI a : player){
					a.addResources(out[0]);
					a.addResources(out[1]);
					a.showResource();
				}
				getAiColor(j).roundBuild();
				
				for(int k = 4; k > 0; k--){
					int points = 0;
					for(Field f : Server.fields){
						if(f.building[Main.UP].getIcon()==Main.BUILDING_IMAGES[k*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])points += 1;
						if(f.building[Main.UP].getIcon()==Main.BUILDING_IMAGES[k*Main.BUILDING_TYPE_COUNT+Main.TOWN])points += 2;
						if(f.building[Main.DOWN].getIcon()==Main.BUILDING_IMAGES[k*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])points += 1;
						if(f.building[Main.DOWN].getIcon()==Main.BUILDING_IMAGES[k*Main.BUILDING_TYPE_COUNT+Main.TOWN])points += 2;
					}
					if(points > 9){
						String color = getExternalColorName(k);
						JOptionPane.showMessageDialog(w,"With 10 points the color "+color+" just won!","End",JOptionPane.INFORMATION_MESSAGE,null);
						System.exit(0);
					}
				}
			}
			
			
			
		}
	}
	
	public static AI getAiColor(int color){
		for(int i = 0; i< 4; i++){
			if(player[i].ownColor == color)return player[i];
		}
		return null;
	}
	
	
	
	/*
	 * Constants
	 */
	
	public static final int UP = 0;
	public static final int LEFT = 0;
	public static final int RIGHT = 2;
	public static final int DOWN = 1;
	
	
	public static final int VILLAGE = 0;
	public static final int TOWN = 1;
	
	
	public static final int GREY = 0;
	public static final int BLUE = 1;
	public static final int YELLOW = 2;
	public static final int GREEN = 3;
	public static final int RED = 4;
	
	public static final ArrayList<Integer> COLOR = new ArrayList<Integer>(Arrays.asList(BLUE,YELLOW,GREEN,RED));
	public static String getExternalColorName(int color){
		switch(color){
			case BLUE:return "Blue";
			case YELLOW: return "Yellow";
			case GREEN: return "Green";
			case RED: return "Red";
			default: return null;
		}
	}
	
	public static String getInternalColorName(int color){
		switch(color){
			case GREY:return "grau";
			case BLUE:return "blau";
			case YELLOW: return "gelb";
			case GREEN: return "gruene";
			case RED: return "rot";
			default: return null;
		}
	}
	
	public static final ArrayList<Integer> RESOURCE_FIELDS = new ArrayList<Integer>(Arrays.asList(0,0,0, 1,1,1,1, 2,2,2,2, 3,3,3,3, 4,4,4,4));
	
	public static final ArrayList<Integer> PROBEBILITY_FIELDS  = new ArrayList<Integer>(Arrays.asList(1,1,1, 1,1,1,1, 1,2,2,2, 2,2,2,2, 2,2,3,3)); 
	
	/*
	 * Images
	 */
	
	public static final ImageIcon RAMEN_IMAGE = Helper.getImage("/images/discs/ramen.png");
	public static final ImageIcon COIN_IMAGE = Helper.getImage("/images/discs/coin.png");
	
	//Ore, Crop, Wood, Wool, Brick
	public static final ImageIcon[] ROHSTOFF_IMAGES = {
		Helper.getImage("/images/rohstoff/erz.png"),
		Helper.getImage("/images/rohstoff/getreide.png"),
		Helper.getImage("/images/rohstoff/holz.png"),
		Helper.getImage("/images/rohstoff/wolle.png"),
		Helper.getImage("/images/rohstoff/ziegel.png")
	};
	
	
	
	private static final int COUNT_COLOR = 4;
	private static final int COUNT_COLOR_PLUS_GREY = COUNT_COLOR+1;
	
	
	
	public static final int BUILDING_TYPE_COUNT = 2;
	public static final ImageIcon[] BUILDING_IMAGES = new ImageIcon[COUNT_COLOR_PLUS_GREY*BUILDING_TYPE_COUNT];
	static{
		int counter = 0;
		for(int i = 0; i < COUNT_COLOR_PLUS_GREY; i++){
			String color = getInternalColorName(i);
			BUILDING_IMAGES[counter++] = Helper.getImage("/images/gebaeude/dorf_"+color+".png");
			BUILDING_IMAGES[counter++] = Helper.getImage("/images/gebaeude/stadt_"+color+".png");
		}
	}
	public static ImageIcon getBuildingImage(int color, int type){
		return BUILDING_IMAGES[color*Main.BUILDING_TYPE_COUNT + type];
	}
	
	
	
	
	private static final int STREET_TYPE_COUNT = 3;
	private static final ImageIcon[] STREET_IMAGES = new ImageIcon[COUNT_COLOR_PLUS_GREY*STREET_TYPE_COUNT];
	static{
		int counter = 0;
		for(int i = 0; i < COUNT_COLOR_PLUS_GREY; i++){
			String color = getInternalColorName(i);
			STREET_IMAGES[counter++] = Helper.getImage("/images/strasse/strasse_"+color+"_left.png");
			STREET_IMAGES[counter++] = Helper.getImage("/images/strasse/strasse_"+color+"_down.png");
			STREET_IMAGES[counter++] = Helper.getImage("/images/strasse/strasse_"+color+"_right.png");
		}
	}
	public static ImageIcon getStreetImage(int color, int position){
		return STREET_IMAGES[color*Main.STREET_TYPE_COUNT + position];
	}
	
			
	
	/*
	 * Font 
	 */
	
	public static final Font font18 = new Font("Century Schoolbook L", Font.PLAIN, 20);

}
