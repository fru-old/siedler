package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import main.Server.Field;





public class Human extends AI{
	
	//Singleton there can only every be one player at a given instance!
	public static final Human singleton = new Human();
	
	private Human(){
		
	}
	
	JLabel[] rohstoffLabel = {null,null,null,null,null};
	JButton[] buildingButton = {null,null,null};
	JPanel greyOverPanel = null;
	boolean nextRoundNow = false;
	boolean greyedOut = true;
	boolean hasBoughtNow = false;
	
	{
		resourceAccount = new int[]{0,2,4,2,4};
	}
	
	public JPanel createBoardPanel(final JPanel panel){
		panel.setBorder(null);
		
		for(Field field : Server.fields){
			panel.add(field.resource);
			for(int j = 0; j < 3; j++){
				panel.add(field.street[j]); 
				field.street[j].addMouseListener(Human.singleton.getStreetListener(field.street[j]));
				panel.setComponentZOrder(field.street[j], 0);
			}
		}
		for(Field field : Server.fields){
			for(int j = 0; j < 2; j++){
				panel.add(field.building[j]); 
				field.building[j].addMouseListener(Human.singleton.getBuildingListener(field.building[j]));
				panel.setComponentZOrder(field.building[j], 0);
			}
		}
		for(Field field : Server.fields){
			panel.add(field.probabilityIcon);
			panel.setComponentZOrder(field.probabilityIcon, 0);
			panel.add(field.probabilityText);
			panel.setComponentZOrder(field.probabilityText, 0);
			
			if(field.probability == 1){
				field.probabilityText.setFont(Main.font18);
				field.probabilityText.setForeground(new Color(75,75,75));
			}else if(field.probability == 2){
				field.probabilityText.setFont(Main.font18);
				field.probabilityText.setForeground(new Color(30,30,30));
			}else{
				field.probabilityText.setFont(Main.font18);
				field.probabilityText.setForeground(new Color(80,10,10));
			}
		}

		fillFieldBounds( 61*3, 106*0, Server.up3);
		fillFieldBounds( 61*2, 106*1, Server.up2);
		fillFieldBounds( 61*1, 106*2, Server.up1);
		fillFieldBounds( 61*0, 106*3, Server.middle);
		fillFieldBounds( 61*1, 106*4, Server.down1);
		fillFieldBounds( 61*2, 106*5, Server.down2);
		fillFieldBounds( 61*3, 106*6, Server.down3);
		panel.setPreferredSize(new Dimension(5*122 + 2* BORDER , 4*106 + 139 + 2* BORDER + 150));
		
		buildingButton[0] = new JButton(Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT + Main.VILLAGE]);
		buildingButton[0].setBackground(Color.LIGHT_GRAY);
		buildingButton[0].setEnabled(false);
		buildingButton[0].setBounds(20, 4*106 + 139 + 2* BORDER, 50, 50);
		System.out.println(4*106 + 139 + 2* BORDER);
		buildingButton[0].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				unGreyAll();
				getBuildableVillages(false).setIcon(Main.GREY*Main.BUILDING_TYPE_COUNT+Main.VILLAGE);
            }
        }); 
		panel.add(buildingButton[0]);
		panel.setComponentZOrder(buildingButton[0], 0);
		
		buildingButton[1] = new JButton(Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT + Main.TOWN]);
		buildingButton[1].setBackground(Color.LIGHT_GRAY);
		buildingButton[1].setEnabled(false);
		buildingButton[1].setBounds(75, 4*106 + 139 + 2* BORDER, 50, 50);
		buildingButton[1].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				unGreyAll();
        		getBuildableTowns().setIcon(Main.GREY*Main.BUILDING_TYPE_COUNT+Main.TOWN);
            }
        }); 
		panel.add(buildingButton[1]);
		panel.setComponentZOrder(buildingButton[1], 0);
		
		buildingButton[2] = new JButton(Main.getStreetImage(ownColor,Main.LEFT));
		buildingButton[2].setBackground(Color.LIGHT_GRAY);
		buildingButton[2].setEnabled(false);
		buildingButton[2].setBounds(130, 4*106 + 139 + 2* BORDER, 50, 50);
		buildingButton[2].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				unGreyAll();
        		getBuildableStreets().setIcon(Main.GREY);
            }
        });
		panel.add(buildingButton[2]);
		panel.setComponentZOrder(buildingButton[2], 0);
		
		JLabel l;
		//l = new JLabel(Main.STREET_IMAGES[ownColor*Main.STREET_TYPE_COUNT + Main.LEFT]);
		
		
		l = new JLabel(Helper.getImage("/images/pfeil.png"));
		l.setBounds(5*122 + 2* BORDER - 150, 4*106 + 139 + 2* BORDER, 130, 50);
		panel.add(l);
		
		l = new JLabel("Next");
		l.setBounds(5*122 + 2* BORDER - 150 + 15, 4*106 + 139 + 2* BORDER, 130, 50);
		l.setFont(Main.font18.deriveFont(20f));
		l.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(!greyedOut){
					nextRoundNow = true;
				}
			}
		});
		panel.add(l);
		panel.setComponentZOrder(l, 0);
		
		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),"nextRoundNow");
		panel.getActionMap().put("nextRoundNow",
         new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!greyedOut) {
			    	nextRoundNow = true;
			    }
			}
		});
		
		
		int factor = 125;
		
		rohstoffLabel[0] = new JLabel("0 x"); //33x
		rohstoffLabel[0].setBorder(null);
		rohstoffLabel[0].setFont(Main.font18.deriveFont(30f));
		rohstoffLabel[0].setBounds(15 , 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(rohstoffLabel[0]);
		
		l = new JLabel(Helper.getImage("/images/rohstoff/erz_small.png"));
		l.setBorder(null);
		l.setBounds(65, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(l);
		
		
		rohstoffLabel[1] = new JLabel("0 x"); //33x
		rohstoffLabel[1].setBorder(null);
		rohstoffLabel[1].setFont(Main.font18.deriveFont(30f));
		rohstoffLabel[1].setBounds(15+factor, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(rohstoffLabel[1]);
		
		l = new JLabel(Helper.getImage("/images/rohstoff/getreide_small.png"));
		l.setBorder(null);
		l.setBounds(65+factor, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(l);
		
		
		rohstoffLabel[2] = new JLabel("0 x"); //33x
		rohstoffLabel[2].setBorder(null);
		rohstoffLabel[2].setFont(Main.font18.deriveFont(30f));
		rohstoffLabel[2].setBounds(15+2*factor, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(rohstoffLabel[2]);
		
		l = new JLabel(Helper.getImage("/images/rohstoff/holz_small.png"));
		l.setBorder(null);
		l.setBounds(65+2*factor, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(l);
		
		
		
		
		rohstoffLabel[3] = new JLabel("0 x"); //33x
		rohstoffLabel[3].setBorder(null);
		rohstoffLabel[3].setFont(Main.font18.deriveFont(30f));
		rohstoffLabel[3].setBounds(15+3*factor, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(rohstoffLabel[3]);
		
		l = new JLabel(Helper.getImage("/images/rohstoff/wolle_small.png"));
		l.setBorder(null);
		l.setBounds(65+3*factor, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(l);
		
		
		
		rohstoffLabel[4] = new JLabel("0 x"); //33x
		rohstoffLabel[4].setBorder(null);
		rohstoffLabel[4].setFont(Main.font18.deriveFont(30f));
		rohstoffLabel[4].setBounds(15+4*factor, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(rohstoffLabel[4]);
		
		l = new JLabel(Helper.getImage("/images/rohstoff/ziegel_small.png"));
		l.setBorder(null);
		l.setBounds(65+4*factor, 4*106 + 139 + 2* BORDER + 70, 60, 70);
		panel.add(l);
		
		
		greyOverPanel = new JPanel(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				Color ppColor = new Color(238,238,238, 150); //r,g,b,alpha
				g.setColor(ppColor);
				g.fillRect(0,0,getWidth(),getHeight()); //x,y,width,height
			}
		};
		greyOverPanel.setOpaque(false);
		greyOverPanel.setLayout(null);
		//p.setBounds(0, 0, 200, 200);
		greyOverPanel.setBounds(10, 4*106 + 139 + 2* BORDER, 5*122 + 2* BORDER-20, 140);
		panel.add(greyOverPanel);
		panel.setComponentZOrder(greyOverPanel, 0);
		
		
		/*
		l = new JLabel();
		AnimatedIcon icon1 = new AnimatedIcon(l, 50, 1);
		ImageIcon duke = Helper.getImage("/images/smiley.png") ;
		icon1.addIcon( duke );

		for (int angle = 0; angle <= 180; angle += 12){
		    icon1.addIcon( new RotatedIcon(duke, angle) );
		}

		l.setIcon( icon1 );
		l.setBounds(160, 120, 317, 317);
		panel.add(l);
		panel.setComponentZOrder(l, 0);
		icon1.start();
		*/
		
		return panel;
	}
	
	public void firstBuild(){
		getBuildableVillages(true).setIcon(Main.GREY*Main.BUILDING_TYPE_COUNT+Main.VILLAGE);
		while(!hasBoughtNow){
			try {Thread.sleep(15L);} catch (Exception e) {}
		}
		hasBoughtNow = false;
		unGreyAll();
		getBuildableStreets().setIcon(Main.GREY);
		while(!hasBoughtNow){
			try {Thread.sleep(15L);} catch (Exception e) {}
		}
		hasBoughtNow = false;
		unGreyAll();
	}
	
	public void secondBuild(){
		firstBuild();
	}
	
	public void setControllesActive(boolean active){
		for(int i = 0; i<3;i++)buildingButton[i].setEnabled(active);
		greyOverPanel.setVisible(!active);
		greyedOut = !active;
	}
	
	public void addResources(Icon icon, int amount, int[] account){
		super.addResources(icon, amount, account);
	}
	
	public void showResource(){
		for(int i = 0; i < 5; i++){
			int c = resourceAccount[i];
			if(c > 9)rohstoffLabel[i].setText(c+"x");
			else rohstoffLabel[i].setText(c+" x");
		}
	}
	
	public void roundBuild(){
			setControllesActive(true);
			activateButtons();
			while(!nextRoundNow){
				try {Thread.sleep(15L);} catch (Exception e) {}
				if(hasBoughtNow){
					unGreyAll();
					hasBoughtNow = false;
					activateButtons();
				}
			}
			unGreyAll();
			nextRoundNow = false;
			setControllesActive(false);
	}
	
	public void activateButtons(){
		if(!canBuyVillage())buildingButton[0].setEnabled(false);
		if(!canBuyTown())buildingButton[1].setEnabled(false);	
		if(!canBuyStreet())buildingButton[2].setEnabled(false);
	}
	
	public void buy(int i){
		if(i == 0)buyVillage();
		if(i == 1)buyTown();
		if(i == 2)buyStreet();
	}
	
	public void buyVillage(){
		super.buyVillage();
		showResource();
	}
	
	public void buyTown(){
		super.buyTown();
		showResource();
	}
	
	public void buyStreet(){
		super.buyStreet();
		showResource();
	}
	
	public final int BORDER = 20;
	
	public void fillFieldBounds(int x, int y, ArrayList<Field> list){
		x += -122 + BORDER;
		y += -106 + BORDER;
		for(Field field : list){
			
			field.resource.setBounds(x, y, 120, 139);
			field.probabilityIcon.setBounds(x+39, y+50, 42, 42);//42
			field.probabilityText.setBounds(x+39+15, y+50+2, 42, 42);
			
			field.building[Main.UP].setBounds(x+46, y-28, 33, 38);
			field.building[Main.DOWN].setBounds(x+46, y+113, 35, 38);
			
			field.street[Main.LEFT].setBounds(x+4, y-9, 50, 55);
			field.street[Main.DOWN].setBounds(x+36, y+151, 50, 55);
			field.street[Main.RIGHT].setBounds(x+66, y-9, 50, 55);
			
			x+=122;
		}
	}
	
	
	public MouseAdapter getListener(final JLabel label, final Icon[][] transition, final Integer[] buyNumber){
		return new MouseAdapter(){
			Icon returnExitIcon = null;
			int buyNum = -1;
			@Override
			public void mouseExited(MouseEvent arg0) {
				if(returnExitIcon != null){
					label.setIcon(returnExitIcon);
					returnExitIcon = null;
				}
			}
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if(returnExitIcon != null){
					returnExitIcon = null;
					buy(buyNum);
					hasBoughtNow = true;
				}
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {
				for(int i = 0; i < transition.length; i++){
					Helper.assertion(transition[i].length==2);
					if(label.getIcon() == transition[i][0]){
						returnExitIcon = transition[i][0];
						label.setIcon(transition[i][1]);
						buyNum = buyNumber[i];
					}
				}
			}
		};
	}
	
	public MouseAdapter getBuildingListener(final JLabel street){
		return getListener(street, new Icon[][]{{Main.BUILDING_IMAGES[Main.GREY*Main.BUILDING_TYPE_COUNT+Main.VILLAGE],
													Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE]},
												{Main.BUILDING_IMAGES[Main.GREY*Main.BUILDING_TYPE_COUNT+Main.TOWN],
													Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.TOWN]}},new Integer[]{0,1});
	}
	
	//TODO horrible
	public MouseAdapter getStreetListener(final JLabel street){
		return getListener(street, new Icon[][]{{Main.getStreetImage(Main.GREY,Main.DOWN),
														Main.getStreetImage(ownColor,Main.DOWN)},
												{Main.getStreetImage(Main.GREY,Main.LEFT),
														Main.getStreetImage(ownColor,Main.LEFT)},
												{Main.getStreetImage(Main.GREY,Main.RIGHT),
														Main.getStreetImage(ownColor,Main.RIGHT)}},new Integer[]{2,2,2});	
	}
	
	public void unGreyAll(){
		for(Field field : Server.fields){
			for(int i = 0; i<3; i++){
				if(field.street[i].getIcon() == Main.getStreetImage(Main.GREY,i))
					field.street[i].setIcon(null);
			}
			for(int i = 0; i<2; i++){
				if(field.building[i].getIcon() == Main.BUILDING_IMAGES[Main.GREY*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])
					field.building[i].setIcon(null);
				if(field.building[i].getIcon() == Main.BUILDING_IMAGES[Main.GREY*Main.BUILDING_TYPE_COUNT+Main.TOWN])
					field.building[i].setIcon(Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE]);
			}
		}
	}

}
