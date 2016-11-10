package module.xyz;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import env.Agent;
import env.Spot;

public class Floor {

	public HashMap<String,Spot> celMap; //全てのセルスポットのマップ変数<XYZ,spot>
	public int maxX;
	public int maxY;
	public int maxZ;
	public int minX;
	public int minY;
	public int minZ;
	public int widthX;
	public int heightY;
	
	/**
	 * コンストラクタ
	 * @param spot
	 */
	@SuppressWarnings("unchecked")
	public Floor(Spot spot) {
		celMap =  (HashMap<String,Spot>)spot.getEquip("CellMap");
		maxX = spot.getIntVariable("maxX");
		maxY = spot.getIntVariable("maxY");
		maxZ = spot.getIntVariable("maxZ");
		minX = spot.getIntVariable("minX");
		minY = spot.getIntVariable("minY");
		minZ = spot.getIntVariable("minZ");
		widthX = spot.getIntVariable("widthX");
		heightY = spot.getIntVariable("heightY");
	}
	
	/**
	 * 生活スペーススポットの頂点スポット集合を返すメソッド
	 * @return 頂点スポット集合
	 */
	public HashSet<Spot> getLifeSpacesSet(int x, int y, int z) {
		HashSet<Spot> spotSet = new HashSet<Spot>();
		Iterator<Spot> spotIt = celMap.values().iterator();
		while(spotIt.hasNext()) {
			Spot spot = spotIt.next();
			Spot leaderSpot = spot.getSpotVariable("LeaderSpot");
			if(isLifeSpaceAvailable(spot, x, y, z)) 
				spotSet.add(spot);
		}
		return spotSet;
	}
	
	// 占有空間を集合変数に返すメソッド
	public HashSet<Spot> setLifeSpacesHashSet(Agent agent, int x, int y, int z) {
		return setLifeSpacesHashSet(agent.getSpot(), x, y, z);
	}
	
	// 占有空間を集合変数に返すメソッド
	public HashSet<Spot> setLifeSpacesHashSet(Spot spot, int x, int y, int z) {
		HashSet<Spot> spotSet = new HashSet<Spot>();
		int cx = spot.getIntVariable("XCoordinate");
		int cy = spot.getIntVariable("YCoordinate");
		int cz = spot.getIntVariable("ZCoordinate");
		
		for(int i=cx; i<cx+x; i++) {
			for(int j=cy; j<cy+y; j++) {
				for(int k=cz; k<=cz+z; k++) {
					Spot tmpSpot = celMap.get(Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k));
					tmpSpot.setKeyword("CellType", "LivingSpace");
					spotSet.add(tmpSpot);
				}
			}
		}
		return spotSet;
	}
	
	// 占有空間を集合変数に返すメソッド
	public HashSet<Spot> setLifeSpacesSet(Agent agent, int x, int y, int z) {
		return setLifeSpacesSet(agent.getSpot(), x, y, z);
	}
	
	// 占有空間を集合変数に返すメソッド
	public HashSet<Spot> setLifeSpacesSet(Spot spot, int x, int y, int z) {
		HashSet<Spot> spotSet = new HashSet<Spot>();
		int cx = spot.getIntVariable("XCoordinate");
		int cy = spot.getIntVariable("YCoordinate");
		int cz = spot.getIntVariable("ZCoordinate");
		
		for(int i=cx; i<cx+x; i++) {
			for(int j=cy; j<cy+y; j++) {
				for(int k=cz; k<=cz+z; k++) {
					Spot tmpSpot = celMap.get(Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k));
					tmpSpot.setKeyword("CellType", "LivingSpace");
					spotSet.add(tmpSpot);
				}
			}
		}
		return spotSet;
	}
	
	// 予約する占有空間を集合変数に返すメソッド
	public HashSet<Spot> reserveLifeSpacesSet(Agent agent, int x, int y, int z) {
		return reserveLifeSpacesSet(agent.getSpotVariable("GoalSpot"), x, y, z);
	}
	
	// 予約する占有空間を集合変数に返すメソッド
	public HashSet<Spot> reserveLifeSpacesSet(Spot spot, int x, int y, int z) {
		HashSet<Spot> spotSet = new HashSet<Spot>();
		int cx = spot.getIntVariable("XCoordinate");
		int cy = spot.getIntVariable("YCoordinate");
		int cz = spot.getIntVariable("ZCoordinate");
		
		for(int i=cx; i<cx+x; i++) {
			for(int j=cy; j<cy+y; j++) {
				for(int k=cz; k<=cz+z; k++) {
					Spot tmpSpot = celMap.get(Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k));
					tmpSpot.setKeyword("CellType", "LivingSpaceReserve");
					spotSet.add(tmpSpot);
				}
			}
		}
		return spotSet;
	}
	
	// set2からset1の共通部分を消すメソッド
	public HashSet<Spot> removeSet(HashSet<Spot> set1, HashSet<Spot> set2) {
		Iterator<Spot> it1 = set1.iterator();
		set2.removeAll(set1);
		return set2;
	}
	
	// 自分の占有空間の全ての入口（通路上）を取得するメソッド
	public HashSet<Spot> getLifeSpaceEntranceSet(HashSet<Spot> set) {
		HashSet<Spot> spotSet = new HashSet<Spot>();
		Iterator<Spot> it = set.iterator();
		while(it.hasNext()) {
			Spot spot = it.next();
			Spot northSpot = spot.getSpotVariable("northSpot");
			Spot eastSpot = spot.getSpotVariable("eastSpot");
			Spot southSpot = spot.getSpotVariable("southSpot");
			Spot westSpot = spot.getSpotVariable("westSpot");
			Spot topSpot = spot.getSpotVariable("topSpot");
			Spot bottomSpot = spot.getSpotVariable("bottomSpot");
			
			
			if(northSpot!=spot && northSpot.getKeyword("CellType").equals("corridor"))
				spotSet.add(northSpot);
			if(eastSpot!=spot && eastSpot.getKeyword("CellType").equals("corridor"))
				spotSet.add(eastSpot);
			if(southSpot!=spot && southSpot.getKeyword("CellType").equals("corridor"))
				spotSet.add(southSpot);
			if(westSpot!=spot && westSpot.getKeyword("CellType").equals("corridor"))
				spotSet.add(westSpot);
			if(topSpot!=spot && topSpot.getKeyword("CellType").equals("corridor"))
				spotSet.add(topSpot);
			if(bottomSpot!=spot && bottomSpot.getKeyword("CellType").equals("corridor"))
				spotSet.add(bottomSpot);
		}
		
		return spotSet;
	}
	
	// 自分の占有空間の入口を渡してその隣の占有空間上のスポットを決定するメソッド
	public Spot getEntranceInLifeSpace(Agent agent) {
		Spot entranceSpot = agent.getSpotVariable("LivingSpaceEntrance");
		Spot northSpot = entranceSpot.getSpotVariable("northSpot");
		Spot eastSpot = entranceSpot.getSpotVariable("eastSpot");
		Spot southSpot = entranceSpot.getSpotVariable("southSpot");
		Spot westSpot = entranceSpot.getSpotVariable("westSpot");
		Spot topSpot = entranceSpot.getSpotVariable("topSpot");
		Spot bottomSpot = entranceSpot.getSpotVariable("bottomSpot");
		HashSet<Spot> LifeSpaceSet = (HashSet<Spot>)agent.getEquip("LivingSpaceSet");
		Spot tmpSpot = entranceSpot;
		
		if(northSpot!=entranceSpot && LifeSpaceSet.contains(northSpot))
			tmpSpot = northSpot;
		if(eastSpot!=entranceSpot && LifeSpaceSet.contains(eastSpot))
			tmpSpot = eastSpot;
		if(southSpot!=entranceSpot && LifeSpaceSet.contains(southSpot))
			tmpSpot = southSpot;
		if(westSpot!=entranceSpot && LifeSpaceSet.contains(westSpot))
			tmpSpot = westSpot;
		if(topSpot!=entranceSpot && LifeSpaceSet.contains(topSpot))
			tmpSpot = topSpot;
		if(bottomSpot!=entranceSpot && LifeSpaceSet.contains(bottomSpot))
			tmpSpot = bottomSpot;
		return tmpSpot;
	}
	
	// 占有空間を設置可能かどうか判定するメソッド
	public boolean isLifeSpaceAvailable(Agent agent, int x, int y, int z) {
		return isLifeSpaceAvailable(agent.getSpot(), x, y, z);
	}
	
	// 占有空間を設置可能かどうか判定するメソッド
	public boolean isLifeSpaceAvailable(Spot spot, int x, int y, int z) {
		int cx = spot.getIntVariable("XCoordinate");
		int cy = spot.getIntVariable("YCoordinate");
		int cz = spot.getIntVariable("ZCoordinate");
		
		if(cx+x-1 > widthX || cx < minX)
			return false;
		else if(cy+y-1 > heightY || cy < minY)
			return false;
		else if(cz+z > maxZ)
			return false;
		
		for(int i=cx; i<cx+x; i++) {
			for(int j=cy; j<cy+y; j++) {
				for(int k=cz; k<=cz+z; k++) {
					Spot tmpSpot = celMap.get(Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k));
					if(!tmpSpot.getKeyword("CellType").equals("corridor"))
						return false;
				}
			}
		}
		
		return true;
	}
	
	// 引数のスポットに隣接している通路スポットを返すメソッド
	public HashSet<Spot> setNextToReceptionStreet(Spot spot) {
		HashSet<Spot> spotSet = new HashSet<Spot>();
		Spot northSpot = spot.getSpotVariable("northSpot");
		Spot eastSpot = spot.getSpotVariable("eastSpot");
		Spot southSpot = spot.getSpotVariable("southSpot");
		Spot westSpot = spot.getSpotVariable("westSpot");
		Spot topSpot = spot.getSpotVariable("topSpot");
		Spot bottomSpot = spot.getSpotVariable("bottomSpot");
		
		if(northSpot!=spot && northSpot.getKeyword("CellType").equals("corridor"))
			spotSet.add(northSpot);
		if(eastSpot!=spot && eastSpot.getKeyword("CellType").equals("corridor"))
			spotSet.add(eastSpot);
		if(southSpot!=spot && southSpot.getKeyword("CellType").equals("corridor"))
			spotSet.add(southSpot);
		if(westSpot!=spot && westSpot.getKeyword("CellType").equals("corridor"))
			spotSet.add(westSpot);
		if(topSpot!=spot && topSpot.getKeyword("CellType").equals("corridor"))
			spotSet.add(topSpot);
		if(bottomSpot!=spot && bottomSpot.getKeyword("CellType").equals("corridor"))
			spotSet.add(bottomSpot);
		return spotSet;
	}
	
	public HashSet<Spot> getInfectionAffectAreas(Spot spot, int domainX, int domainY, int domainZ) {
		HashSet<Spot> spotSet = new HashSet<Spot>();
		
		int cx = spot.getIntVariable("XCoordinate");
		int cy = spot.getIntVariable("YCoordinate");
		int cz = spot.getIntVariable("ZCoordinate");
		
		HashSet<Spot> wallSet = fetchNearAttrSpots(spot, "wall", domainX, domainY, domainZ);
		int wallMinX = cx - domainX;
		int wallMaxX = cx + domainX;
		int wallMinY = cy - domainY;
		int wallMaxY = cy + domainY;
		
		Iterator<Spot> wallSpotIt = wallSet.iterator();
        while(wallSpotIt.hasNext()) {
        	Spot wallSpot = wallSpotIt.next();
        	int wallX = wallSpot.getIntVariable("XCoordinate");
        	int wallY = wallSpot.getIntVariable("YCoordinate");
        	int dx = cx - wallX;
        	int dy = cy - wallY;
        	
        	if (cy == wallY) {
        		if (dx < 0)
            		wallMaxX = wallX;
            	if (dx > 0)
            		wallMinX = wallX;
        	}
        	
        	if (cx == wallX) {
        		if (dy < 0)
            		wallMaxY = wallY;
            	if (dy > 0)
            		wallMinY = wallY;
        	}
        }
        
        int mx = maxX;
        int my = maxY;
		for (int i=cx-domainX; i<=cx+domainX; i++) {
			if (i <= 0 || i > mx || i < wallMinX || i > wallMaxX)
				continue;
			for (int j=cy-domainY; j <=cy+domainY; j++) {
				if (j <= 0 || j > my || j < wallMinY || j > wallMaxY)
					continue;
				for(int k=cz-domainZ; k<=cz+domainZ; k++) {
					Spot tmpSpot = celMap.get(Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k));
					if (tmpSpot == spot)
						continue;
					
					if (tmpSpot.getKeyword("CellType").equals("wall"))
						continue;
					
					spotSet.add(tmpSpot);
				}
			}
		}
		return spotSet;
	}
	
	public HashSet<Spot> fetchNearAttrSpots(Spot spot, String keyword, int domainX, int domainY, int domainZ) {
		HashSet<Spot> s = new HashSet();
		
		int cx = spot.getIntVariable("XCoordinate");
		int cy = spot.getIntVariable("YCoordinate");
		int cz = spot.getIntVariable("ZCoordinate");
		
		int mx = maxX;
		int my = maxY;
				
		for (int i=cx-domainX; i<=cx+domainX; i++) {
			if (i <= 0 || i > mx)
				continue;
			for (int j=cy-domainY; j <=cy+domainY; j++) {
				if (j <= 0 || j > my)
					continue;
				for(int k=cz-domainZ; k<=cz+domainZ; k++) {
					Spot tmpSpot = celMap.get(Integer.toString(i) + "_" + Integer.toString(j) + "_" + Integer.toString(k));
					if (tmpSpot == spot)
						continue;
					
					if (tmpSpot.getKeyword("CellType").equals(keyword)) {
						s.add(tmpSpot);
					}
				}
			}
		}
		
		return s;
	} 
	
	public void AffectSurroundAgent(Agent agent) {
		Spot cs = agent.getSpot();
		HashSet<Spot> affectSpotSet = cs.getEquip("InfectionAffectSpotSet");
		
		Iterator<Spot> spotIt = affectSpotSet.iterator();
		while(spotIt.hasNext()) {
			Spot spot = spotIt.next();
			spot.setKeyword("SpotInfection", "yes");
		}
	}
}
