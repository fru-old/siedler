package main;

import java.util.ArrayList;

import javax.swing.Icon;

import main.Server.Field;

public class AI {
	
	Helper.BuildingList buildVillages = new Helper.BuildingList();
	public int[] buildExpectedAccount = {0,0,0,0,0};
	
	public void buildVillage(Field f, int position){
		f.building[position].setIcon(Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT + Main.VILLAGE]);
		addResources(f.resource.getIcon(), f.probability, buildExpectedAccount);
		if(position == Main.UP){
			addResources(f.ul().resource.getIcon(), f.ul().probability, buildExpectedAccount);
			addResources(f.ur().resource.getIcon(), f.ur().probability, buildExpectedAccount);
		}
		if(position == Main.DOWN){
			addResources(f.dl().resource.getIcon(), f.dl().probability, buildExpectedAccount);
			addResources(f.dr().resource.getIcon(), f.dr().probability, buildExpectedAccount);
		}
	}
	
	public void buildStreet(Field f, int position){
		f.street[position].setIcon(Main.getStreetImage(ownColor,position));
	}
	
	public void buildTown(Field f, int position){
		f.building[position].setIcon(Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT + Main.TOWN]);
	}
	
	
	public boolean hasBuilt = false;
	public void roundBuild(){
		if(hasBuilt)try {Thread.sleep(Server.standardRecognitionDelay);} catch (Exception e) {}
		hasBuilt = false;
		if(canBuyTown()){
			if(buildWantedTown(getBuildableTowns())){buyTown();hasBuilt = true;roundBuild();};
		}
		if(canBuyStreet()){
			boolean buildStreet = buildWantedStreet();
			if(buildStreet){buyStreet();hasBuilt = true;roundBuild();}
			else if(!buildStreet && canBuyVillage()){buildWantedVillage(); buyVillage();hasBuilt = true;roundBuild();}
		}	
	}
	
	public void firstBuild(){
		secondBuild();
	}
	
	public void secondBuild(){
		buildWantedVillage(getBuildableVillages(true),false);
		buildWantedStreet(getBuildableVillages(true));
		
	}
	
	public boolean buildWantedVillage(){
		return buildWantedVillage(getBuildableVillages(false),true);
	}
	
	public boolean buildWantedStreet(){
		return buildWantedStreet(getBuildableVillages(true));
	}
	
	public boolean buildWantedVillage(Helper.BuildingList buildableVillages,boolean onlyNear){
		if(buildableVillages.size()==0)return false;
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(int i = 0; i<buildableVillages.size(); i++){
			values.add(evaluateVillage(buildableVillages.getField(i), buildableVillages.getPosition(i),onlyNear));
		}
		int biggest = getBiggest(values);
		buildVillage(buildableVillages.getField(biggest), buildableVillages.getPosition(biggest));
		return true;
	}
	
	public boolean buildWantedTown(Helper.BuildingList buildableTowns){
		if(buildableTowns.size()==0)return false;
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(int i = 0; i<buildableTowns.size(); i++){
			values.add(evaluateVillage(buildableTowns.getField(i), buildableTowns.getPosition(i),false));
		}
		int biggest = getBiggest(values);
		buildTown(buildableTowns.getField(biggest), buildableTowns.getPosition(biggest));
		return true;
	}
	
	public boolean buildWantedStreet(Helper.BuildingList buildableVillages){
		if(buildableVillages.size()==0)return false;
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(int i = 0; i<buildableVillages.size(); i++){
			values.add(evaluateVillage(buildableVillages.getField(i), buildableVillages.getPosition(i),true));
		}
		while(true){
			int biggest = getBiggest(values);
			if(values.get(biggest)==0)return false;
			getStreetsToBuilt(buildableVillages.getField(biggest), buildableVillages.getPosition(biggest));
			if(streetToBuilt.size()==0){
				return false;
			}else{
				for(int i = 0; i<streetToBuilt.size(); i++ ){
					if(isEmptyStreet(streetToBuilt.getField(i), streetToBuilt.getPosition(i))){
						buildStreet(streetToBuilt.getField(i), streetToBuilt.getPosition(i));
						return true;
					}
				}
				values.set(biggest,0);
			}	
		}
		
	}
	
	public int getBiggest(ArrayList<Integer> list){
		int biggest = 0; 
		for(int i = 1; i<list.size(); i++){
			if(list.get(biggest)<list.get(i))biggest = i;
		}
		return biggest;
	}
	
	public int evaluateVillage(Field f, int p, boolean onlyNear){
		if(p == Main.UP)return evaluateVillage(f, f.ul(), f.ur(), onlyNear?getStreetsToBuilt(f, p):-1);
		if(p == Main.DOWN)return evaluateVillage(f, f.dl(), f.dr(), onlyNear?getStreetsToBuilt(f, p):-1);
		return 0;
	}
	
	public int evaluateVillage(Field f1, Field f2, Field f3, int streetsToBuilt){
		int out = 1;
		int rc1 = getIdResources(f1.resource.getIcon());
		int rc2 = getIdResources(f2.resource.getIcon());
		int rc3 = getIdResources(f3.resource.getIcon());
		
		int pCount = 1;
		if(rc1 != -1)pCount += f1.probability;
		if(rc2 != -1)pCount += f2.probability;
		if(rc3 != -1)pCount += f3.probability;
		out *= pCount;
		
		
		int expectationCount = 0;
		for(int i = 0; i<5;i++)expectationCount += buildExpectedAccount[i];
			
		
		//Evaluate Need
		if(rc1 != -1 && buildExpectedAccount[rc1] != 0){
			if(buildExpectedAccount[rc1] < (expectationCount / 5))out *= 3;
		}
		if(rc2 != -1 && buildExpectedAccount[rc2] != 0){
			if(buildExpectedAccount[rc2] < (expectationCount / 5))out *= 3;
		}
		if(rc3 != -1 && buildExpectedAccount[rc3] != 0){
			if(buildExpectedAccount[rc3] < (expectationCount / 5))out *= 3;
		}
		
		//Diversity Bonus on initial building
		if(rc1 == rc2 && rc2 == rc3){
			out = filterBonusFirstTimeCoverage(rc1, out);
		}else if(rc1 == rc2){
			out = filterBonusFirstTimeCoverage(rc1, out);
			out = filterBonusFirstTimeCoverage(rc3, out);
		}else if(rc1 == rc3){
			out = filterBonusFirstTimeCoverage(rc1, out);
			out = filterBonusFirstTimeCoverage(rc2, out);
		}else if(rc2 == rc3){
			out = filterBonusFirstTimeCoverage(rc1, out);
			out = filterBonusFirstTimeCoverage(rc3, out);
		}else{
			out = filterBonusFirstTimeCoverage(rc1, out);
			out = filterBonusFirstTimeCoverage(rc2, out);
			out = filterBonusFirstTimeCoverage(rc3, out);
		}
		
		if(streetsToBuilt != -1){
			if(streetsToBuilt != 0)out -= streetsToBuilt;
		}
			
		return out;
	}
	
	public int filterBonusFirstTimeCoverage(int resource, int factor){
		if(resource == -1 || buildExpectedAccount[resource] != 0)return factor;
		return factor*9;
	}
	
	
	
	
	int ownColor;
	
	public AI setColor(int ownColor){
		this.ownColor = ownColor;
		return this;
	}
	
	
	//redundent
	public int getIdResources(Icon icon){
		for(int i = 0; i < Main.ROHSTOFF_IMAGES.length; i++)if(icon == Main.ROHSTOFF_IMAGES[i]){
			return i;
		}
		return -1;
	}
	
	public int[] resourceAccount = {0,0,0,0,0};
	public void addResources(Icon icon, int amount, int[] account){
		int i = getIdResources(icon);
		if(i != -1)account[i] += amount;
	}
	public void addResources(Field field){
		//redundant
		Field ur = field.neighbour[Main.UP+Main.RIGHT];
		Field ul = field.neighbour[Main.UP+Main.LEFT];
		Field dr = field.neighbour[Main.DOWN+Main.RIGHT];
		Field dl = field.neighbour[Main.DOWN+Main.LEFT];
		
		if(ul != null && ul.building[Main.DOWN].getIcon() == Main.getBuildingImage(ownColor,Main.VILLAGE))addResources(field.resource.getIcon(),1,resourceAccount);
		if(ur != null && ur.building[Main.DOWN].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])addResources(field.resource.getIcon(),1,resourceAccount);
		if(dl != null && dl.building[Main.UP].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])addResources(field.resource.getIcon(),1,resourceAccount);
		if(dr != null && dr.building[Main.UP].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])addResources(field.resource.getIcon(),1,resourceAccount);
		if(field != null && field.building[Main.DOWN].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])addResources(field.resource.getIcon(),1,resourceAccount);
		if(field != null && field.building[Main.UP].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])addResources(field.resource.getIcon(),1,resourceAccount);
				
		if(ul != null && ul.building[Main.DOWN].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.TOWN])addResources(field.resource.getIcon(),2,resourceAccount);
		if(ul != null && ur.building[Main.DOWN].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.TOWN])addResources(field.resource.getIcon(),2,resourceAccount);
		if(ul != null && dl.building[Main.UP].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.TOWN])addResources(field.resource.getIcon(),2,resourceAccount);
		if(ul != null && dr.building[Main.UP].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.TOWN])addResources(field.resource.getIcon(),2,resourceAccount);
		if(ul != null && field.building[Main.DOWN].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.TOWN])addResources(field.resource.getIcon(),2,resourceAccount);
		if(ul != null && field.building[Main.UP].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.TOWN])addResources(field.resource.getIcon(),2,resourceAccount);
	}
	
	public void showResource(){
		
	}
	
	public boolean canBuyVillage(){
		if(resourceAccount.length != 5)return false;
		if(resourceAccount[1]<1 || resourceAccount[2]<1 || resourceAccount[3]<1 || resourceAccount[4]<1 )return false;
		return true;
	}
	
	public boolean canBuyStreet(){
		if(resourceAccount.length != 5)return false;
		if(resourceAccount[2]<1 || resourceAccount[4]<1 )return false;
		return true;
	}
	
	public boolean canBuyTown(){
		if(resourceAccount.length != 5)return false;
		if(resourceAccount[0]<3 || resourceAccount[1]<2 )return false;
		return true;
	}
	
	public void buyVillage(){
		resourceAccount[1] -= 1; resourceAccount[2] -= 1; resourceAccount[3] -= 1; resourceAccount[4] -= 1; 
		
	}
	
	public void buyTown(){
		resourceAccount[0] -= 3; resourceAccount[1] -= 2;
	}
	
	public void buyStreet(){
		resourceAccount[2] -= 1; resourceAccount[4] -= 1; 
	}
	

	
	public Helper.BuildingList getAllVillages(){
		Helper.BuildingList out = new Helper.BuildingList();
		for(Field field : Server.fields){
			
		}
		return out;
	}
	
	
	public boolean isEmptyBuilding(Field building, int i){return building == null || building.building[i].getIcon() == null || building.building[i].getIcon() == Main.BUILDING_IMAGES[Main.GREY*Main.BUILDING_TYPE_COUNT+Main.VILLAGE];}
	public boolean isEmptyStreet(Field street, int i){return street == null || street.street[i].getIcon() == null || street.street[i].getIcon() == Main.getStreetImage(Main.GREY, i);}
	public boolean isOwnStreet(Field street, int i){return street != null && (street.street[i].getIcon() == Main.getStreetImage(ownColor, i));}
	public boolean isOwnBuilding(Field building, int i){return building != null && (building.building[i].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE] || building.building[i].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.TOWN]);}
	
	
	public Helper.StreetList streetToBuilt = null; 
	public int getStreetsToBuilt(Field f, int p){
		Field dlml = f.dl() != null?f.dl().ml():null;
		Field drmr = f.dr() != null?f.dr().mr():null;
		Field dldl = f.dl() != null?f.dl().dl():null;
		Field drdr = f.dr() != null?f.dr().dr():null;
		Field dldldr = dldl != null?dldl.dr():null;
		Field drdrdl = drdr != null?drdr.dl():null;
		
		Field ulml = f.ul() != null?f.ul().ml():null;
		Field urmr = f.ur() != null?f.ur().mr():null;
		Field ulul = f.ul() != null?f.ul().ul():null;
		Field urur = f.ur() != null?f.ur().ur():null;
		Field ululur = ulul != null?ulul.ur():null;
		Field ururul = urur != null?urur.ul():null;
		
		streetToBuilt = new Helper.StreetList();
		//Promblem cant see if there are obsticles to building a village like foreign streets!
		//Es fängt also an zu bauen und merkt dann ups...
		if(p == Main.DOWN){
			if(isOwnStreet(f.dr(), Main.LEFT))return 0;
			if(isOwnStreet(f.dl(), Main.RIGHT))return 0;
			if(isOwnStreet(f, Main.DOWN))return 0;
			
			if(isOwnStreet(f.dl(), Main.LEFT)){streetToBuilt.add(f.dl(), Main.RIGHT); return 1;}
			if(isOwnStreet(f.dr(), Main.RIGHT)){streetToBuilt.add(f.dr(), Main.LEFT); return 1;}
			if(isOwnStreet(f.ul(), Main.DOWN)){streetToBuilt.add(f.dl(), Main.RIGHT); return 1;}
			if(isOwnStreet(f.ur(), Main.DOWN)){streetToBuilt.add(f.dr(), Main.LEFT); return 1;}
			if(isOwnStreet(f.dodo(), Main.LEFT)){streetToBuilt.add(f, Main.DOWN); return 1;}
			if(isOwnStreet(f.dodo(), Main.RIGHT)){streetToBuilt.add(f, Main.DOWN); return 1;}
			
			if(isOwnBuilding(f.ul(), Main.DOWN)){streetToBuilt.add(f.ul(), Main.DOWN);return 2;}
			if(isOwnBuilding(f.ur(), Main.DOWN)){streetToBuilt.add(f.ur(), Main.DOWN);return 2;}
			if(isOwnBuilding(f.ml(), Main.DOWN)){streetToBuilt.add(f.dl(), Main.LEFT);return 2;}
			if(isOwnBuilding(f.mr(), Main.DOWN)){streetToBuilt.add(f.dr(), Main.RIGHT);return 2;}
			if(isOwnBuilding(f.dl(), Main.DOWN)){streetToBuilt.add(f.dodo(), Main.LEFT);return 2;}
			if(isOwnBuilding(f.dr(), Main.DOWN)){streetToBuilt.add(f.dodo(), Main.RIGHT);return 2;}
			
			if(isOwnStreet(f, Main.LEFT)){streetToBuilt.add(f.ul(), Main.DOWN);return 2;}
			if(isOwnStreet(f, Main.RIGHT)){streetToBuilt.add(f.ur(), Main.DOWN);return 2;}
			if(isOwnStreet(f.mr(), Main.LEFT)){streetToBuilt.add(f.ur(), Main.DOWN);return 2;}
			if(isOwnStreet(f.ml(), Main.RIGHT)){streetToBuilt.add(f.ul(), Main.DOWN);return 2;}
			if(isOwnStreet(f.mr(), Main.DOWN)){streetToBuilt.add(f.dr(), Main.RIGHT);return 2;}
			if(isOwnStreet(f.ml(), Main.DOWN)){streetToBuilt.add(f.dl(), Main.LEFT);return 2;}
			if(isOwnStreet(f.dl(), Main.DOWN)){streetToBuilt.add(f.dodo(), Main.LEFT);return 2;}
			if(isOwnStreet(f.dr(), Main.DOWN)){streetToBuilt.add(f.dodo(), Main.RIGHT);return 2;}
			if(isOwnStreet(dldl, Main.RIGHT)){streetToBuilt.add(f.dodo(), Main.LEFT);return 2;}
			if(isOwnStreet(drdr, Main.LEFT)){streetToBuilt.add(f.dodo(), Main.RIGHT);return 2;}
			if(isOwnStreet(dlml, Main.RIGHT)){streetToBuilt.add(f.dl(), Main.LEFT);return 2;}
			if(isOwnStreet(drmr, Main.LEFT)){streetToBuilt.add(f.dr(), Main.RIGHT);return 2;}
			
			if(isOwnBuilding(f, Main.UP)){streetToBuilt.add(f, Main.LEFT);streetToBuilt.add(f, Main.RIGHT);return 3;}
			if(isOwnBuilding(f.ml(), Main.UP)){streetToBuilt.add(f.ml(), Main.RIGHT);return 3;}
			if(isOwnBuilding(f.mr(), Main.UP)){streetToBuilt.add(f.mr(), Main.LEFT);return 3;}
			if(isOwnBuilding(dlml, Main.UP)){streetToBuilt.add(dlml, Main.RIGHT);return 3;}
			if(isOwnBuilding(drmr, Main.UP)){streetToBuilt.add(drmr, Main.LEFT);return 3;}
			if(isOwnBuilding(dldl, Main.UP)){streetToBuilt.add(dldl, Main.RIGHT);streetToBuilt.add(f.ml(), Main.DOWN);return 3;}
			if(isOwnBuilding(drdr, Main.UP)){streetToBuilt.add(drdr, Main.LEFT);streetToBuilt.add(f.mr(), Main.DOWN);return 3;}
			if(isOwnBuilding(dldldr, Main.UP)){streetToBuilt.add(f.dl(), Main.DOWN);return 3;}
			if(isOwnBuilding(drdrdl, Main.UP)){streetToBuilt.add(f.dr(), Main.DOWN);return 3;}
		}else if(p == Main.UP){
			if(isOwnStreet(f, Main.LEFT))return 0;
			if(isOwnStreet(f, Main.RIGHT))return 0;
			if(isOwnStreet(f.upup(), Main.DOWN))return 0;
			
			if(isOwnStreet(f.ul(), Main.RIGHT)){streetToBuilt.add(f.upup(), Main.DOWN); return 1;}
			if(isOwnStreet(f.ur(), Main.LEFT)){streetToBuilt.add(f.upup(), Main.DOWN); return 1;}
			if(isOwnStreet(f.ul(), Main.DOWN)){streetToBuilt.add(f, Main.LEFT); return 1;}
			if(isOwnStreet(f.ur(), Main.DOWN)){streetToBuilt.add(f, Main.RIGHT); return 1;}
			if(isOwnStreet(f.ml(), Main.RIGHT)){streetToBuilt.add(f, Main.LEFT); return 1;}
			if(isOwnStreet(f.mr(), Main.LEFT)){streetToBuilt.add(f, Main.RIGHT); return 1;}
			
			if(isOwnBuilding(f.ul(), Main.UP)){streetToBuilt.add(f.ul(), Main.RIGHT);return 2;}
			if(isOwnBuilding(f.ur(), Main.UP)){streetToBuilt.add(f.ur(), Main.LEFT);return 2;}
			if(isOwnBuilding(f.ml(), Main.UP)){streetToBuilt.add(f.ml(), Main.RIGHT);return 2;}
			if(isOwnBuilding(f.mr(), Main.UP)){streetToBuilt.add(f.mr(), Main.LEFT);return 2;}
			if(isOwnBuilding(f.dl(), Main.UP)){streetToBuilt.add(f.ul(), Main.DOWN);return 2;}
			if(isOwnBuilding(f.dr(), Main.UP)){streetToBuilt.add(f.ur(), Main.DOWN);return 2;}
			
			if(isOwnStreet(f.dr(), Main.LEFT)){streetToBuilt.add(f.ur(), Main.DOWN);return 2;}
			if(isOwnStreet(f.dl(), Main.RIGHT)){streetToBuilt.add(f.ul(), Main.DOWN);return 2;}
			if(isOwnStreet(f.dl(), Main.LEFT)){streetToBuilt.add(f.ul(), Main.DOWN);return 2;}
			if(isOwnStreet(f.dr(), Main.RIGHT)){streetToBuilt.add(f.ur(), Main.DOWN);return 2;}
			if(isOwnStreet(f.ml(), Main.LEFT)){streetToBuilt.add(f.ml(), Main.RIGHT);return 2;}
			if(isOwnStreet(f.mr(), Main.RIGHT)){streetToBuilt.add(f.mr(), Main.LEFT);return 2;}
			if(isOwnStreet(ulul, Main.DOWN)){streetToBuilt.add(f.ml(), Main.RIGHT);return 2;}
			if(isOwnStreet(urur, Main.DOWN)){streetToBuilt.add(f.mr(), Main.LEFT);return 2;}
			if(isOwnStreet(f.ur(), Main.RIGHT)){streetToBuilt.add(f.ur(), Main.LEFT);return 2;}
			if(isOwnStreet(f.ul(), Main.LEFT)){streetToBuilt.add(f.ul(), Main.RIGHT);return 2;}
			if(isOwnStreet(ululur, Main.DOWN)){streetToBuilt.add(f.ul(), Main.RIGHT);return 2;}
			if(isOwnStreet(ururul, Main.DOWN)){streetToBuilt.add(f.ur(), Main.LEFT);return 2;}
			
			if(isOwnBuilding(f, Main.DOWN)){streetToBuilt.add(f.dr(), Main.LEFT);streetToBuilt.add(f.dl(), Main.RIGHT);return 3;}
			if(isOwnBuilding(f.ml(), Main.DOWN)){streetToBuilt.add(f.dl(), Main.LEFT);return 3;}
			if(isOwnBuilding(f.mr(), Main.DOWN)){streetToBuilt.add(f.dr(), Main.RIGHT);return 3;}
			if(isOwnBuilding(ulml, Main.DOWN)){streetToBuilt.add(f.ml(), Main.LEFT);return 3;}
			if(isOwnBuilding(urmr, Main.DOWN)){streetToBuilt.add(f.mr(), Main.RIGHT);return 3;}
			if(isOwnBuilding(ulul, Main.DOWN)){streetToBuilt.add(f.ul(), Main.LEFT);streetToBuilt.add(ulul, Main.DOWN);return 3;}
			if(isOwnBuilding(urur, Main.DOWN)){streetToBuilt.add(f.ur(), Main.RIGHT);streetToBuilt.add(urur, Main.DOWN);return 3;}
			if(isOwnBuilding(ululur, Main.DOWN)){streetToBuilt.add(ululur, Main.DOWN);return 3;}
			if(isOwnBuilding(ururul, Main.DOWN)){streetToBuilt.add(ururul, Main.DOWN);return 3;}
		}
		
		
		return Integer.MAX_VALUE;
	}
	
	public Helper.BuildingList getBuildableVillages(boolean noStreets){
		Helper.BuildingList out = new Helper.BuildingList();
		for(Field f : Server.fields){
			if(f.ur() != null && f.ul() != null)
			if(f.building[Main.UP].getIcon()==null)
			if(isEmptyBuilding(f.upup(),Main.DOWN) && isEmptyBuilding(f.ul(),Main.DOWN) && isEmptyBuilding(f.ur(),Main.DOWN) )
			if(noStreets || isOwnStreet(f.upup(), Main.DOWN) || isOwnStreet(f, Main.LEFT) || isOwnStreet(f, Main.RIGHT) )
				out.add(f,Main.UP);
			
			if(f.dr() != null && f.dl() != null)
			if(f.building[Main.DOWN].getIcon()==null)
			if(isEmptyBuilding(f.dodo(),Main.UP) && isEmptyBuilding(f.dl(),Main.UP) && isEmptyBuilding(f.dr(),Main.UP) )
			if(noStreets || isOwnStreet(f, Main.DOWN) || isOwnStreet(f.dr(), Main.LEFT) || isOwnStreet(f.dl(), Main.RIGHT) )
				out.add(f,Main.DOWN);	
			
		}
		return out;
	}
	
	public Helper.StreetList getBuildableStreets(){
		Helper.StreetList out = new Helper.StreetList();
		for(Field field : Server.fields){
			Field ur = field.neighbour[Main.UP+Main.RIGHT];
			Field ul = field.neighbour[Main.UP+Main.LEFT];
			Field dr = field.neighbour[Main.DOWN+Main.RIGHT];
			Field dl = field.neighbour[Main.DOWN+Main.LEFT];
			Field upup = ur != null?ur.neighbour[Main.UP+Main.LEFT]:null;
			Field dodo = dr != null?dr.neighbour[Main.DOWN+Main.LEFT]:null;
			Field ml = ul != null?ul.neighbour[Main.DOWN+Main.LEFT]:null;
			Field mr = ur != null?ur.neighbour[Main.DOWN+Main.RIGHT]:null;
			
			if(field.street[Main.DOWN].getIcon() == null)
			if(dodo != null && dl != null && dr != null)
			if(isOwnBuilding(dodo, Main.UP) || isOwnBuilding(field, Main.DOWN) || isOwnStreet(dodo, Main.RIGHT) || isOwnStreet(dodo, Main.LEFT) || isOwnStreet(dl, Main.RIGHT) || isOwnStreet(dr, Main.LEFT))
				out.add(field,Main.DOWN);
			
			if(field.street[Main.LEFT].getIcon() == null)
			if(ml != null && ul != null && ur != null)
			if(isOwnBuilding(field, Main.UP) || isOwnBuilding(ul, Main.DOWN) || isOwnStreet(ml, Main.RIGHT) || isOwnStreet(ul, Main.DOWN) || isOwnStreet(upup, Main.DOWN) || isOwnStreet(field, Main.RIGHT))
				out.add(field,Main.LEFT);
			
			if(field.street[Main.RIGHT].getIcon() == null)
			if(mr != null && ul != null && ur != null)
			if(isOwnBuilding(field, Main.UP) || isOwnBuilding(ur, Main.DOWN) || isOwnStreet(mr, Main.LEFT) || isOwnStreet(ur, Main.DOWN) || isOwnStreet(upup, Main.DOWN) || isOwnStreet(field, Main.LEFT))
				out.add(field,Main.RIGHT);
		}
		return out;
	}
	
	public Helper.BuildingList getBuildableTowns(){
		Helper.BuildingList out = new Helper.BuildingList();
		for(Field field : Server.fields){
			for(int i = 0; i<2; i++){
				if(field.building[i].getIcon() == Main.BUILDING_IMAGES[ownColor*Main.BUILDING_TYPE_COUNT+Main.VILLAGE])
					out.add(field,i);
			}
		}
		return out;
	}
}
