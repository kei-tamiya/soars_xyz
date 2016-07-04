package module.xyz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import env.Agent;
import env.Spot;

public class Route {
	public HashMap<String,Spot> celMap; //全てのセルスポットのマップ変数<XYZ,spot>
	public LinkedList<LinkedList<Object>> lineList;
	public HashMap<Spot,Integer> spotMap;
	public HashMap<Integer,Spot> idMap;
	public List<Edge>[] nodes;
	public LinkedList<LinkedList<Object>> gymLineList;
	public HashMap<Spot,Integer> gymSpotMap;
	public HashMap<Integer,Spot> gymIdMap;
	public List<Edge>[] gymNodes;
	//public HashSet<Spot> reachableSet;
	public int maxX;
	public int maxY;
	public int maxZ;
	public int minX;
	public int minY;
	public int minZ;
	public int widthX;
	public int heightY;
	public int[][] grid;
	
	/**
	 * Routeクラスのコンストラクタ
	 * spot 座標空間系の菅理スポット
	 */
	@SuppressWarnings("unchecked")
	public Route(Spot spot) {
		celMap =  (HashMap<String,Spot>)spot.getEquip("CellMap");
		lineList = new LinkedList<LinkedList<Object>>(); // 全体のリンク情報のリスト
		spotMap = new HashMap<Spot,Integer>(); // 登場するスポットのマップ集合
		idMap = new HashMap<Integer,Spot>(); // 登場するIDのマップ集合
		gymLineList = new LinkedList<LinkedList<Object>>(); // 全体のリンク情報のリスト
		gymSpotMap = new HashMap<Spot,Integer>(); // 登場するスポットのマップ集合
		gymIdMap = new HashMap<Integer,Spot>(); // 登場するIDのマップ集合
		//reachableSet = new HashSet<Spot>();
		maxX = spot.getIntVariable("maxX");
		maxY = spot.getIntVariable("maxY");
		maxZ = spot.getIntVariable("maxZ");
		minX = spot.getIntVariable("minX");
		minY = spot.getIntVariable("minY");
		minZ = spot.getIntVariable("minZ");
		widthX = spot.getIntVariable("widthX");
		heightY = spot.getIntVariable("heightY");
		grid = new int[maxX][maxY];    //移動コスト(距離)の記録
	}
	
	/**
	 * XYZ座標からスポットを取得するメソッド
	 * @param spot 座標空間系の菅理スポット
	 * @param x XCoordinate
	 * @param y YCoordinate
	 * @param z ZCoordinate
	 * @return スポット
	 */
	public static Spot getSpotXYZ(Spot spot, int x, int y, int z) {
		HashMap<String,Spot> map =  (HashMap<String,Spot>)spot.getEquip("CellMap");
		Spot tmpSpot = map.get(Integer.toString(x) + "_" + Integer.toString(y) + "_" + Integer.toString(z));
		return tmpSpot; // 座標x,y,zのスポット
	}
	
	/**
	 * 周辺スポットを登録するメソッド
	 * @param leaderSpot 座標空間系の菅理スポット
	 * @param spot 周辺スポットを登録するスポット
	 */
	public static void surroundSpotRegister(Spot leaderSpot, Spot spot) {
		int tmpX = spot.getIntVariable("XCoordinate");
		int tmpY = spot.getIntVariable("YCoordinate");
		int tmpZ = spot.getIntVariable("ZCoordinate");
		int maxX = leaderSpot.getIntVariable("maxX");
		int maxY = leaderSpot.getIntVariable("maxY");
		int maxZ = leaderSpot.getIntVariable("maxZ");
		int northY = tmpY;
		int westX = tmpX;
		int eastX = tmpX;
		int southY = tmpY;
		int topZ = tmpZ;
		int bottomZ = tmpZ;
		
		if (tmpY > 1)
			northY = tmpY-1;
		if (tmpX > 1)
			westX = tmpX-1;
		if (tmpX < maxX)
			eastX = tmpX + 1;
		if (tmpY < maxY)
			southY = tmpY+1;
		if (tmpZ < maxZ)
			topZ = tmpZ + 1;
		if (tmpZ > 1)
			bottomZ = tmpZ - 1;
		
		Spot northSpot = getSpotXYZ(leaderSpot, tmpX, northY, tmpZ);
		Spot eastSpot = getSpotXYZ(leaderSpot, eastX, tmpY, tmpZ);
		Spot southSpot = getSpotXYZ(leaderSpot, tmpX, southY, tmpZ);
		Spot westSpot = getSpotXYZ(leaderSpot, westX, tmpY, tmpZ);
		Spot topSpot = getSpotXYZ(leaderSpot, tmpX, tmpY, topZ);
		Spot bottomSpot = getSpotXYZ(leaderSpot, tmpX, tmpY, bottomZ);
		spot.setSpotVariable("northSpot", northSpot);
		spot.setSpotVariable("eastSpot", eastSpot);
		spot.setSpotVariable("southSpot", southSpot);
		spot.setSpotVariable("westSpot", westSpot);
		spot.setSpotVariable("topSpot", topSpot);
		spot.setSpotVariable("bottomSpot", bottomSpot);
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	public void createGraph(Spot leaderSpot) throws IOException {
		lineList.clear(); // 全体のリンク情報のリスト
		spotMap.clear(); // 登場するスポットのマップ集合
		idMap.clear(); // 登場するIDのマップ集合
		int id = 0;
		
		//通路用セルの集合を作成する
		HashSet<Spot> streetList = new HashSet<Spot>();
		Iterator<Spot> streetListIt = celMap.values().iterator();
		while(streetListIt.hasNext()) {
			Spot tmpSpot = streetListIt.next();
			if(tmpSpot.getKeyword("CellType").equals("corridor"))
				streetList.add(tmpSpot);
		}
		
		//通路セルのリンクを作成する。始点・終点・コストをリストに格納。全体のリンク情報をリスト化する
		int maxX = leaderSpot.getIntVariable("maxX");
		int maxY = leaderSpot.getIntVariable("maxY");
		int maxZ = leaderSpot.getIntVariable("maxZ");
		int tmpX = 0;
		int tmpY = 0;
		int tmpZ = 0;
		int cost = 1;
		
		for (int i=0; i < maxX; i++) {
			tmpX = i+1;
			for (int j = 0; j < maxY; j++) {
				tmpY = j+1;
				
				Spot startSpot = getSpotXYZ(leaderSpot, tmpX, tmpY, tmpZ);
				Spot eastSpot = startSpot.getSpotVariable("eastSpot");
				Spot southSpot = startSpot.getSpotVariable("southSpot");
				
				if (streetList.contains(eastSpot) && startSpot != eastSpot) {
					LinkedList<Object> tmpEastList = new LinkedList<Object>();
					tmpEastList.addLast(startSpot);
					tmpEastList.addLast(eastSpot);
					tmpEastList.addLast(cost);
					lineList.addLast(tmpEastList);
					if(!spotMap.containsKey(startSpot)) {
						spotMap.put(startSpot,id);
						idMap.put(id, startSpot);
						id++;
					}
					if(!spotMap.containsKey(eastSpot)) {
						spotMap.put(eastSpot,id);
						idMap.put(id, eastSpot);
						id++;
					}
				}
				if (streetList.contains(southSpot) && startSpot != southSpot) {
					LinkedList<Object> tmpSouthList = new LinkedList<Object>();
					tmpSouthList.addLast(startSpot);
					tmpSouthList.addLast(southSpot);
					tmpSouthList.addLast(cost);
					lineList.addLast(tmpSouthList);
					if(!spotMap.containsKey(startSpot)) {
						spotMap.put(startSpot,id);
						idMap.put(id, startSpot);
						id++;
					}
					if(!spotMap.containsKey(southSpot)) {
						spotMap.put(southSpot,id);
						idMap.put(id, southSpot);
						id++;
					}
				}				
			}
		}
		// ノード数分だけEdgeを保持するリストを作成し、Edgeを保持するようArrayListを要素として持たせる
		nodes = new List[spotMap.size()];
		for (int i = 0; i < spotMap.size(); i++) {
            nodes[i] = new ArrayList<Edge>();
        }
		for(int i=0; i<lineList.size(); i++) { 
			nodes[spotMap.get(lineList.get(i).get(0))].add(new Edge(spotMap.get(lineList.get(i).get(0)), 
					spotMap.get(lineList.get(i).get(1)), (int)lineList.get(i).get(2)));
			nodes[spotMap.get(lineList.get(i).get(1))].add(new Edge(spotMap.get(lineList.get(i).get(1)), 
					spotMap.get(lineList.get(i).get(0)), (int)lineList.get(i).get(2)));//無向グラフなので、逆方向も接続する
		}
    }
	
	//体育館内のみのダイクストラ用隣接リスト作成
	@SuppressWarnings({ "unchecked", "unused" })
	public void createGymGraph(Spot leaderSpot) throws IOException {
		gymLineList.clear(); // 全体のリンク情報のリスト
		gymSpotMap.clear(); // 登場するスポットのマップ集合
		gymIdMap.clear(); // 登場するIDのマップ集合
		int id = 0;
		
		//通路用セルの集合を作成する
		HashSet<Spot> streetList = new HashSet<Spot>();
		Iterator<Spot> streetListIt = celMap.values().iterator();
		while(streetListIt.hasNext()) {
			Spot tmpSpot = streetListIt.next();
			int tmpX = tmpSpot.getIntVariable("XCoordinate");
			int tmpY = tmpSpot.getIntVariable("YCoordinate");
			if(tmpX <= widthX+1 && tmpY <= heightY+1 && tmpSpot.getKeyword("CellType").equals("corridor"))
				streetList.add(tmpSpot);
		}
		
		//通路セルのリンクを作成する。始点・終点・コストをリストに格納。全体のリンク情報をリスト化する
		int maxZ = leaderSpot.getIntVariable("maxZ");
		int tmpX = 0;
		int tmpY = 0;
		int tmpZ = 0;
		int cost = 1;
		
		for (int i=0; i < widthX; i++) {
			tmpX = i+1;
			for (int j = 0; j < heightY; j++) {
				tmpY = j+1;
				
				Spot startSpot = getSpotXYZ(leaderSpot, tmpX, tmpY, tmpZ);
				Spot eastSpot = startSpot.getSpotVariable("eastSpot");
				Spot southSpot = startSpot.getSpotVariable("southSpot");
				
				if (streetList.contains(eastSpot) && startSpot != eastSpot) {
					LinkedList<Object> tmpEastList = new LinkedList<Object>();
					tmpEastList.addLast(startSpot);
					tmpEastList.addLast(eastSpot);
					tmpEastList.addLast(cost);
					gymLineList.addLast(tmpEastList);
					if(!gymSpotMap.containsKey(startSpot)) {
						gymSpotMap.put(startSpot,id);
						gymIdMap.put(id, startSpot);
						id++;
					}
					if(!gymSpotMap.containsKey(eastSpot)) {
						gymSpotMap.put(eastSpot,id);
						gymIdMap.put(id, eastSpot);
						id++;
					}
				}
				if (streetList.contains(southSpot) && startSpot != southSpot) {
					LinkedList<Object> tmpSouthList = new LinkedList<Object>();
					tmpSouthList.addLast(startSpot);
					tmpSouthList.addLast(southSpot);
					tmpSouthList.addLast(cost);
					gymLineList.addLast(tmpSouthList);
					if(!gymSpotMap.containsKey(startSpot)) {
						gymSpotMap.put(startSpot,id);
						gymIdMap.put(id, startSpot);
						id++;
					}
					if(!gymSpotMap.containsKey(southSpot)) {
						gymSpotMap.put(southSpot,id);
						gymIdMap.put(id, southSpot);
						id++;
					}
				}				
			}
		}
		
		// ノード数分だけEdgeを保持するリストを作成し、Edgeを保持するようArrayListを要素として持たせる
		gymNodes = new List[gymSpotMap.size()];
		for (int i = 0; i < gymSpotMap.size(); i++) {
            gymNodes[i] = new ArrayList<Edge>();
        }
		for(int i=0; i<gymLineList.size(); i++) { 
			gymNodes[gymSpotMap.get(gymLineList.get(i).get(0))].add(new Edge(gymSpotMap.get(gymLineList.get(i).get(0)), 
					gymSpotMap.get(gymLineList.get(i).get(1)), (int)gymLineList.get(i).get(2)));
			gymNodes[gymSpotMap.get(gymLineList.get(i).get(1))].add(new Edge(gymSpotMap.get(gymLineList.get(i).get(1)), 
					gymSpotMap.get(gymLineList.get(i).get(0)), (int)gymLineList.get(i).get(2)));//無向グラフなので、逆方向も接続する
		}
    }
	
	@SuppressWarnings({ "unchecked", "unused" })
	public void createGraphForLivingSpaceMove(Spot leaderSpot) throws IOException {
		lineList.clear(); // 全体のリンク情報のリスト
		spotMap.clear(); // 登場するスポットのマップ集合
		idMap.clear(); // 登場するIDのマップ集合
		int id = 0;
		
		//通路用セルの集合を作成する
		HashSet<Spot> streetList = new HashSet<Spot>();
		HashSet<Spot> livingSpaceList = new HashSet<Spot>();
		Iterator<Spot> streetListIt = celMap.values().iterator();
		while(streetListIt.hasNext()) {
			Spot tmpSpot = streetListIt.next();
			if(tmpSpot.getKeyword("CellType").equals("corridor"))
				streetList.add(tmpSpot);
			if(tmpSpot.getKeyword("CellType").equals("LivingSpace"))
				livingSpaceList.add(tmpSpot);
		}
		
		//通路セルのリンクを作成する。始点・終点・コストをリストに格納。全体のリンク情報をリスト化する
		int maxX = leaderSpot.getIntVariable("maxX");
		int maxY = leaderSpot.getIntVariable("maxY");
		int maxZ = leaderSpot.getIntVariable("maxZ");
		int tmpX = 0;
		int tmpY = 0;
		int tmpZ = 0;
		int corridorCost = 1;
		int livingSpaceCost = 10;
		int corridorToLivingSpaceCost = livingSpaceCost / 2;
		
		for (int i=0; i < maxX; i++) {
			tmpX = i+1;
			for (int j = 0; j < maxY; j++) {
				tmpY = j+1;
				
				Spot startSpot = getSpotXYZ(leaderSpot, tmpX, tmpY, tmpZ);
				Spot eastSpot = startSpot.getSpotVariable("eastSpot");
				Spot southSpot = startSpot.getSpotVariable("southSpot");
				
				if (startSpot != eastSpot && streetList.contains(startSpot)) {
					if (streetList.contains(eastSpot)) {
						LinkedList<Object> tmpEastList = new LinkedList<Object>();
						tmpEastList.addLast(startSpot);
						tmpEastList.addLast(eastSpot);
						tmpEastList.addLast(corridorCost);
						lineList.addLast(tmpEastList);
						if(!spotMap.containsKey(startSpot)) {
							spotMap.put(startSpot,id);
							idMap.put(id, startSpot);
							id++;
						}
						if(!spotMap.containsKey(eastSpot)) {
							spotMap.put(eastSpot,id);
							idMap.put(id, eastSpot);
							id++;
						}
					} else if (livingSpaceList.contains(eastSpot)) {
						LinkedList<Object> tmpEastList = new LinkedList<Object>();
						tmpEastList.addLast(startSpot);
						tmpEastList.addLast(eastSpot);
						tmpEastList.addLast(corridorToLivingSpaceCost);
						lineList.addLast(tmpEastList);
						if(!spotMap.containsKey(startSpot)) {
							spotMap.put(startSpot,id);
							idMap.put(id, startSpot);
							id++;
						}
						if(!spotMap.containsKey(eastSpot)) {
							spotMap.put(eastSpot,id);
							idMap.put(id, eastSpot);
							id++;
						}
					}
				} else if (startSpot != eastSpot && livingSpaceList.contains(startSpot)) {
					if (streetList.contains(eastSpot)) {
						LinkedList<Object> tmpEastList = new LinkedList<Object>();
						tmpEastList.addLast(startSpot);
						tmpEastList.addLast(eastSpot);
						tmpEastList.addLast(corridorToLivingSpaceCost);
						lineList.addLast(tmpEastList);
						if(!spotMap.containsKey(startSpot)) {
							spotMap.put(startSpot,id);
							idMap.put(id, startSpot);
							id++;
						}
						if(!spotMap.containsKey(eastSpot)) {
							spotMap.put(eastSpot,id);
							idMap.put(id, eastSpot);
							id++;
						}
					} else if (livingSpaceList.contains(eastSpot)) {
						LinkedList<Object> tmpEastList = new LinkedList<Object>();
						tmpEastList.addLast(startSpot);
						tmpEastList.addLast(eastSpot);
						tmpEastList.addLast(livingSpaceCost);
						lineList.addLast(tmpEastList);
						if(!spotMap.containsKey(startSpot)) {
							spotMap.put(startSpot,id);
							idMap.put(id, startSpot);
							id++;
						}
						if(!spotMap.containsKey(eastSpot)) {
							spotMap.put(eastSpot,id);
							idMap.put(id, eastSpot);
							id++;
						}
					}
				}
				if (startSpot != southSpot && streetList.contains(startSpot)) {
					if (streetList.contains(southSpot)) {
						LinkedList<Object> tmpSouthList = new LinkedList<Object>();
						tmpSouthList.addLast(startSpot);
						tmpSouthList.addLast(southSpot);
						tmpSouthList.addLast(corridorCost);
						lineList.addLast(tmpSouthList);
						if(!spotMap.containsKey(startSpot)) {
							spotMap.put(startSpot,id);
							idMap.put(id, startSpot);
							id++;
						}
						if(!spotMap.containsKey(southSpot)) {
							spotMap.put(southSpot,id);
							idMap.put(id, southSpot);
							id++;
						}
					} else if (livingSpaceList.contains(southSpot)) {
						LinkedList<Object> tmpSouthList = new LinkedList<Object>();
						tmpSouthList.addLast(startSpot);
						tmpSouthList.addLast(southSpot);
						tmpSouthList.addLast(corridorToLivingSpaceCost);
						lineList.addLast(tmpSouthList);
						if(!spotMap.containsKey(startSpot)) {
							spotMap.put(startSpot,id);
							idMap.put(id, startSpot);
							id++;
						}
						if(!spotMap.containsKey(southSpot)) {
							spotMap.put(southSpot,id);
							idMap.put(id, southSpot);
							id++;
						}
					}
				} else if (startSpot != southSpot && livingSpaceList.contains(startSpot)) {
					if (streetList.contains(southSpot)) {
						LinkedList<Object> tmpSouthList = new LinkedList<Object>();
						tmpSouthList.addLast(startSpot);
						tmpSouthList.addLast(southSpot);
						tmpSouthList.addLast(corridorToLivingSpaceCost);
						lineList.addLast(tmpSouthList);
						if(!spotMap.containsKey(startSpot)) {
							spotMap.put(startSpot,id);
							idMap.put(id, startSpot);
							id++;
						}
						if(!spotMap.containsKey(southSpot)) {
							spotMap.put(southSpot,id);
							idMap.put(id, southSpot);
							id++;
						}
					} else if (livingSpaceList.contains(southSpot)) {
						LinkedList<Object> tmpSouthList = new LinkedList<Object>();
						tmpSouthList.addLast(startSpot);
						tmpSouthList.addLast(southSpot);
						tmpSouthList.addLast(livingSpaceCost);
						lineList.addLast(tmpSouthList);
						if(!spotMap.containsKey(startSpot)) {
							spotMap.put(startSpot,id);
							idMap.put(id, startSpot);
							id++;
						}
						if(!spotMap.containsKey(southSpot)) {
							spotMap.put(southSpot,id);
							idMap.put(id, southSpot);
							id++;
						}
					}
				}			
			}
		}
		// ノード数分だけEdgeを保持するリストを作成し、Edgeを保持するようArrayListを要素として持たせる
		nodes = new List[spotMap.size()];
		for (int i = 0; i < spotMap.size(); i++) {
            nodes[i] = new ArrayList<Edge>();
        }
		for(int i=0; i<lineList.size(); i++) { 
			nodes[spotMap.get(lineList.get(i).get(0))].add(new Edge(spotMap.get(lineList.get(i).get(0)), 
					spotMap.get(lineList.get(i).get(1)), (int)lineList.get(i).get(2)));
			nodes[spotMap.get(lineList.get(i).get(1))].add(new Edge(spotMap.get(lineList.get(i).get(1)), 
					spotMap.get(lineList.get(i).get(0)), (int)lineList.get(i).get(2)));//無向グラフなので、逆方向も接続する
		}
    }
	
	// 経路が存在するかを判別するメソッド
	public boolean isRoute(Agent agent) {
		if(searchRoute(agent).isEmpty())
			return false;
		else
			return true;
	}
	
	// 経路が存在するかを判別するメソッド
	public boolean isRoute(Spot startSpot, Spot goalSpot) {
		if(spotMap.get(startSpot) == null || spotMap.get(goalSpot) == null)
			return false;
		if(searchRoute(startSpot, goalSpot).isEmpty())
			return false;
		else
			return true;
	}
	
	
	// set1とset2の中にどれか1つでも接続している経路が存在するかを判別するメソッド
	public boolean isRoutes(HashSet<Spot> set1, HashSet<Spot> set2) {
		Iterator<Spot> it1 = set1.iterator();
		
		while(it1.hasNext()) {
			Spot spot1 = it1.next();
			Iterator<Spot> it2 = set2.iterator();
			while(it2.hasNext()) {
				Spot spot2 = it2.next();
				if(spotMap.get(spot1) == null || spotMap.get(spot2) == null) {
					if (it1.hasNext()) {
						continue;
					} else {
						return false;
					}
				} else {					
					if(!isRoute(spot1, spot2)) {
						return false;
					}						
				}
			}
		}
		return true;
	}
	
	// 体育館内のみの経路が存在するかを判別するメソッド
	public boolean isGymRoute(Agent agent) {
		if(searchGymRoute(agent).isEmpty())
			return false;
		else
			return true;
	}
	
	// 体育館内のみの経路が存在するかを判別するメソッド
	public boolean isGymRoute(Spot startSpot, Spot goalSpot) {
		if(gymSpotMap.get(startSpot) == null || gymSpotMap.get(goalSpot) == null)
			return false;
		if(searchGymRoute(startSpot, goalSpot).isEmpty())
			return false;
		else
			return true;
	}
	
	// 体育館内のみの、set1とset2の中にどれか1つでも接続している経路が存在するかを判別するメソッド
	public boolean isGymRoutes(HashSet<Spot> set1, HashSet<Spot> set2) {
		Iterator<Spot> it1 = set1.iterator();
		
		while(it1.hasNext()) {
			Spot spot1 = it1.next();
			Iterator<Spot> it2 = set2.iterator();
			while(it2.hasNext()) {
				Spot spot2 = it2.next();
				if(gymSpotMap.get(spot1) == null || gymSpotMap.get(spot2) == null) {
					if (it1.hasNext()) {
						continue;
					} else {
						return false;
					}
				} else {
					if(!isGymRoute(spot1, spot2)) {
						return false;
					}						
				}
			}
		}
		return true;
	}
	
	// set1とset2の中にどれか1つでも接続している経路が存在すればset1側の接続可能スポットを返すメソッド
	public Spot getHasRouteSpot(Agent agent, HashSet<Spot> set1, HashSet<Spot> set2) {
		Iterator<Spot> it1 = set1.iterator();
		Spot tmpSpot = agent.getSpot();
		
		while(it1.hasNext()) {
			Spot spot1 = it1.next();
			Iterator<Spot> it2 = set2.iterator();
			while(it2.hasNext()) {
				Spot spot2 = it2.next();
				if(spot1==spot2)
					continue;
				if(isRoute(spot1, spot2))
					return spot1;
				
				
				if(spotMap.get(spot1) == null || spotMap.get(spot2) == null) {
					if (it1.hasNext()) {
						continue;
					}
				} else {
					if(spot1==spot2)
						continue;
					if(isRoute(spot1, spot2))
						return spot1;				
				}
			}
		}
		return tmpSpot;
	}
	
	// ダイクストラ法で最短経路を探索するメソッド
	public LinkedList<Spot> searchRoute(Agent agent) {
		return searchRoute(agent.getSpotVariable("StartSpot"), agent.getSpotVariable("GoalSpot"));
	}
	
	// ダイクストラ法で最短経路を探索するメソッド
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LinkedList<Spot> searchRoute(Spot startSpot, Spot goalSpot) {
		boolean[] done = new boolean[spotMap.size()];    //訪問フラグ
	    int[] distance = new int[spotMap.size()];        //始点からの最短距離
	    int[] min_path = new int[spotMap.size()];
	    LinkedList[] g_path = new LinkedList[1];
	    g_path[0]=new LinkedList();
	     
	    Arrays.fill(distance, Integer.MAX_VALUE);    //各頂点までの距離を初期化(INF 値)
	    distance[spotMap.get(startSpot)] = 0;    //始点の距離は０

	    Queue<Edge> q = new PriorityQueue<Edge>();
	    q.add(new Edge(spotMap.get(startSpot), spotMap.get(startSpot), 0));     //始点を入れる
	    
	    while (!q.isEmpty()) {
	        Edge e = q.poll();        //最小距離(cost)の頂点を取り出す
	        if (done[e.target]) {
	            continue;
	        }

		    done[e.target] = true;    //訪問済にする
	
		    //隣接している頂点の最短距離を更新する
		    for (Edge v : nodes[e.target]) {
	            if (!done[v.target]) {
	                if (distance[e.target] + v.cost < distance[v.target]) {  //(始点～)接続元＋接続先までの距離    
	                    distance[v.target] = distance[e.target] + v.cost;    //現在記録されている距離より小さければ更新
	                    min_path[v.target] = e.target;
	                    q.add(new Edge(e.target, v.target, distance[v.target]));
	                }
	                else if(distance[e.target] + v.cost == distance[v.target]) {  //(始点～)接続元＋接続先までの距離,現在記録されている距離と同じならば確率50％で更新
	                    if(Math.random()>=0.5) { 
		                	distance[v.target] = distance[e.target] + v.cost;    
		                    min_path[v.target] = e.target;
		                    q.add(new Edge(e.target, v.target, distance[v.target]));
	                    }
	                }
	            }
		    }
	    }
	        
        int g_point = spotMap.get(goalSpot);
        LinkedList<Spot> route = new LinkedList<Spot>();
        
        if(distance[g_point]!=Integer.MAX_VALUE) {
        	startSpot.getSpotVariable("LeaderSpot").setIntVariable("RouteCost", distance[g_point]);
        	while(true) {
    	        g_path[0].push(g_point);
    	        g_point = min_path[g_point];
    	        if(g_point == 0){
    	        	break;
            	}
            }
        	for(int i=0; i<g_path[0].size(); i++)
            	route.addLast(idMap.get(g_path[0].get(i)));
        }
        return route;
	}
	
	// 体育館内のみの、ダイクストラ法で最短経路を探索するメソッド
	public LinkedList<Spot> searchGymRoute(Agent agent) {
		return searchGymRoute(agent.getSpotVariable("StartSpot"), agent.getSpotVariable("GoalSpot"));
	}
	
	// 体育館内のみの、ダイクストラ法で最短経路を探索するメソッド
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LinkedList<Spot> searchGymRoute(Spot startSpot, Spot goalSpot) {
		boolean[] done = new boolean[gymSpotMap.size()];    //訪問フラグ
	    int[] distance = new int[gymSpotMap.size()];        //始点からの最短距離
	    int[] min_path = new int[gymSpotMap.size()];
	    LinkedList[] g_path = new LinkedList[1];
	    g_path[0]=new LinkedList();

	    Arrays.fill(distance, Integer.MAX_VALUE);    //各頂点までの距離を初期化(INF 値)
	    distance[gymSpotMap.get(startSpot)] = 0;    //始点の距離は０

	    Queue<Edge> q = new PriorityQueue<Edge>();
	    q.add(new Edge(gymSpotMap.get(startSpot), gymSpotMap.get(startSpot), 0));     //始点を入れる
	    
	    while (!q.isEmpty()) {
	        Edge e = q.poll();        //最小距離(cost)の頂点を取り出す
	        if (done[e.target]) {
	            continue;
	        }

		    done[e.target] = true;    //訪問済にする
	
		    //隣接している頂点の最短距離を更新する
		    for (Edge v : gymNodes[e.target]) {
	            if (!done[v.target]) {
	                if (distance[e.target] + v.cost < distance[v.target]) {  //(始点～)接続元＋接続先までの距離    
	                    distance[v.target] = distance[e.target] + v.cost;    //現在記録されている距離より小さければ更新
	                    min_path[v.target] = e.target;
	                    q.add(new Edge(e.target, v.target, distance[v.target]));
	                }
	                else if(distance[e.target] + v.cost == distance[v.target]) {  //(始点～)接続元＋接続先までの距離,現在記録されている距離と同じならば確率50％で更新
	                    if(Math.random()>=0.5) { 
		                	distance[v.target] = distance[e.target] + v.cost;    
		                    min_path[v.target] = e.target;
		                    q.add(new Edge(e.target, v.target, distance[v.target]));
	                    }
	                }
	            }
		    }
	    }
	        
        int g_point = gymSpotMap.get(goalSpot);
        LinkedList<Spot> route = new LinkedList<Spot>();
        
        if(distance[g_point]!=Integer.MAX_VALUE) {
        	while(true) {
    	        g_path[0].push(g_point);
    	        g_point = min_path[g_point];
    	        if(g_point == 0){
    	        	break;
            	}
            }
        	for(int i=0; i<g_path[0].size(); i++)
            	route.addLast(gymIdMap.get(g_path[0].get(i)));
        }
        return route;
	}
	
//	public HashSet<Spot> reachableSet(Agent agent) {
//		return reachableSet(agent.getSpotVariable("StartSpot"));
//	}
	
	public int countRouteCost(Agent agent) {
		int cost = 0;
		LinkedList<Spot> route = (LinkedList<Spot>)agent.getEquip("RouteList");
		HashSet<Spot> livingSpace = (HashSet<Spot>)agent.getEquip("LivingSpaceSet");
		int corridorCost = 1;
		int livingSpaceCost = 10;
		int corridorToLivingSpaceCost = livingSpaceCost / 2;
		for (int i=0; i < route.size(); i++) {
			Spot tmpSpot = route.get(i);
			String tmpSpotType = tmpSpot.getKeyword("CellType");
			if (i == 0) {
				cost++;
			} else {
				Spot lastSpot = route.get(i-1);
				String lastSpotType = lastSpot.getKeyword("CellType");
				if (tmpSpotType.equals("LivingSpace")) {
					if (lastSpotType.equals("LivingSpace")) {
						if (livingSpace.contains(lastSpot) && !livingSpace.contains(tmpSpot)) {
							cost = cost + livingSpaceCost;
							int movedNum = tmpSpot.getIntVariable("MovedNum");
							movedNum++;
							tmpSpot.setIntVariable("MovedNum", movedNum);
						} else if  (livingSpace.contains(lastSpot) && livingSpace.contains(tmpSpot)) {
							cost++;
						} else if (!livingSpace.contains(lastSpot)) {
							cost = cost + livingSpaceCost;
							int movedNum = tmpSpot.getIntVariable("MovedNum");
							tmpSpot.setIntVariable("MovedNum", movedNum);
						}
					} else if (lastSpotType.equals("corridor")) {
						if (livingSpace.contains(tmpSpot)) {
							cost++;
						} else {
							cost = cost + corridorToLivingSpaceCost;
							int movedNum = tmpSpot.getIntVariable("MovedNum");
							tmpSpot.setIntVariable("MovedNum", movedNum);
						}
					}
				} else if (tmpSpotType.equals("corridor")) {
					if (lastSpotType.equals("LivingSpace")) {
						if (livingSpace.contains(lastSpot)) {
							cost++;
						} else {
							cost = cost + corridorToLivingSpaceCost;
						}
					} else if (lastSpotType.equals("corridor")) {
						cost++;
					}
				}
			}
		}
		return cost;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet<Spot> getReachableSet(HashSet startSpotSet) {
		boolean[] done = new boolean[spotMap.size()];    //訪問フラグ
	    int[] distance = new int[spotMap.size()];        //始点からの最短距離
	    HashSet<Spot> reachableSet = new HashSet<Spot>();

	    Arrays.fill(distance, Integer.MAX_VALUE);    //各頂点までの距離を初期化(INF 値)
	    Iterator<Spot> spotIt = startSpotSet.iterator();
	    while(spotIt.hasNext()) {
	    	Spot startSpot = spotIt.next();
	    	reachableSet.add(startSpot);
		    distance[spotMap.get(startSpot)] = 0;    //始点の距離は０
	
		    Queue<Edge> q = new PriorityQueue<Edge>();
		    q.add(new Edge(spotMap.get(startSpot), spotMap.get(startSpot), 0));     //始点を入れる
		    
		    while (!q.isEmpty()) {
		        Edge e = q.poll();        //最小距離(cost)の頂点を取り出す
		        if (done[e.target]) {
		            continue;
		        }
	
			    done[e.target] = true;    //訪問済にする
			    
			    
			    //隣接している頂点の最短距離を更新する
			    for (Edge v : nodes[e.target]) {
		            if (!done[v.target]) {
		                if (distance[e.target] + v.cost < distance[v.target]) {  //(始点～)接続元＋接続先までの距離    
		                    distance[v.target] = distance[e.target] + v.cost;    //現在記録されている距離より小さければ更新
		                    reachableSet.add(idMap.get(e.target));	
		                    q.add(new Edge(e.target, v.target, distance[v.target]));
		                }
		            }
			    }
		    }
		}
	    //System.out.println("reachableSet : " + reachableSet);
        return reachableSet;
	}
	
//	public HashSet<Spot> reachableGymSet(Agent agent) {
//		return reachableGymSet(agent.getSpotVariable("StartSpot"));
//	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet<Spot> reachableGymSet(HashSet startSpotSet) {
		boolean[] done = new boolean[gymSpotMap.size()];    //訪問フラグ
	    int[] distance = new int[gymSpotMap.size()];        //始点からの最短距離
	    int[] min_path = new int[gymSpotMap.size()];
	    HashSet<Spot> reachableSet = new HashSet<Spot>();

	    Arrays.fill(distance, Integer.MAX_VALUE);    //各頂点までの距離を初期化(INF 値)
	    Iterator<Spot> spotIt = startSpotSet.iterator();
	    while(spotIt.hasNext()) {
	    	Spot startSpot = spotIt.next();
		    distance[gymSpotMap.get(startSpot)] = 0;    //始点の距離は０
	
		    Queue<Edge> q = new PriorityQueue<Edge>();
		    q.add(new Edge(gymSpotMap.get(startSpot), gymSpotMap.get(startSpot), 0));     //始点を入れる
		    
		    while (!q.isEmpty()) {
		        Edge e = q.poll();        //最小距離(cost)の頂点を取り出す
		        if (done[e.target]) {
		            continue;
		        }
	
			    done[e.target] = true;    //訪問済にする
			    reachableSet.add(gymIdMap.get(e.target));
		
			    //隣接している頂点の最短距離を更新する
			    for (Edge v : gymNodes[e.target]) {
		            if (!done[v.target]) {
		                if (distance[e.target] + v.cost < distance[v.target]) {  //(始点～)接続元＋接続先までの距離    
		                    distance[v.target] = distance[e.target] + v.cost;    //現在記録されている距離より小さければ更新
		                    min_path[v.target] = e.target;
		                    q.add(new Edge(e.target, v.target, distance[v.target]));
		                }
		                else if(distance[e.target] + v.cost == distance[v.target]) {  //(始点～)接続元＋接続先までの距離,現在記録されている距離と同じならば確率50％で更新
		                    if(Math.random()>=0.5) { 
			                	distance[v.target] = distance[e.target] + v.cost;    
			                    min_path[v.target] = e.target;
			                    q.add(new Edge(e.target, v.target, distance[v.target]));
		                    }
		                }
		            }
			    }
		    } 
	    }
        
//        if(distance[g_point]!=Integer.MAX_VALUE) {
//        	while(true) {
//    	        g_path[0].push(g_point);
//    	        g_point = min_path[g_point];
//    	        if(g_point == 0){
//    	        	break;
//            	}
//            }
//        	for(int i=0; i<g_path[0].size(); i++)
//            	route.addLast(gymIdMap.get(g_path[0].get(i)));
//        }
        return reachableSet;
	}
	
	//辺情報の構造体
    class Edge implements Comparable<Edge> {
        public int source = 0;    //接続元ノード
        public int target = 0;    //接続先ノード
        public int cost = 0;      //重み

        public Edge(int source, int target, int cost) {
            this.source = source;
            this.target = target;
            this.cost = cost;
        }

        @Override
        public int compareTo(Edge o) {
            return this.cost - o.cost;
        }

        @Override
        public String toString() {    //デバッグ用
            return "source = " + source + ", target = " + target + ", cost = " + cost;
        }
    }
    
    // 主に占有空間入口までの経路を探索するメソッド
    public LinkedList<Spot> searchGoOutRoute(Agent agent) {
		return searchGoOutRoute(agent.getSpotVariable("StartSpot"), agent.getSpotVariable("GoalSpot"));
	}
    
    public LinkedList<Spot> searchGoOutRoute(Spot startSpot, Spot goalSpot){
    	LinkedList<Spot> route = new LinkedList<Spot>();
    	int cx = startSpot.getIntVariable("XCoordinate");
    	int cy = startSpot.getIntVariable("YCoordinate");
    	int cz = startSpot.getIntVariable("ZCoordinate");
    	int gx = goalSpot.getIntVariable("XCoordinate");
    	int gy = goalSpot.getIntVariable("YCoordinate");
    	int gz = goalSpot.getIntVariable("ZCoordinate");
    	int tmpX = cx;
    	int tmpY = cy;
    	int tmpZ = cz;
    	int incX = 1;
    	int incY = 1;
    	int incZ = 1;
    	if (gx-cx < 0) {
    		incX = -1;
    	}
    	if (gy-cy < 0) {
    		incY = -1;
    	}
    	if (gz-cz < 0) {
    		incZ = -1;
    	}
    	
    	if (tmpX!=gx) {
    		while(tmpX!=gx) {
        		tmpX = tmpX + incX;
        		route.addLast(celMap.get(tmpX + "_" + cy + "_" + cz));
        	}
    	}
    	if (tmpY!=gy) {
    		tmpX = gx;
    		while(tmpY!=gy) {
        		tmpY = tmpY + incY;
        		route.addLast(celMap.get(gx + "_" + tmpY + "_" + cz));
        	}
    	}
    	if (tmpZ!=gz) {
    		tmpX = gx;
    		tmpY = gy;
    		while(tmpZ!=gz) {
        		tmpZ = tmpZ + incZ;
        		route.addLast(celMap.get(gx + "_" + gy + "_" + tmpZ));
        	}
    	}
    	
    	return route;
    }
    
    // 先にX歩動いた後でY軸を合わせるよう動いた後X座標をあわせる順の経路を取得するメソッド
    public LinkedList<Spot> searchRoutePriorityYAfterX(Agent agent, int x) {
		return searchRoutePriorityYAfterX(agent.getSpotVariable("StartSpot"), agent.getSpotVariable("GoalSpot"), x);
	}
    
    // 先にX歩動いた後でY軸を合わせるよう動いた後X座標をあわせる順の経路を取得するメソッド
    public LinkedList<Spot> searchRoutePriorityYAfterX(Spot spot, int x) {
		return searchRoutePriorityYAfterX(spot.getSpotVariable("StartSpot"), spot.getSpotVariable("GoalSpot"), x);
	}
    
    // 先にX歩動いた後でY軸を合わせるよう動いた後X座標をあわせる順の経路を取得するメソッド
    public LinkedList<Spot> searchRoutePriorityYAfterX(Spot startSpot, Spot goalSpot, int x){
    	LinkedList<Spot> route = new LinkedList<Spot>();
    	int cx = startSpot.getIntVariable("XCoordinate");
    	int cy = startSpot.getIntVariable("YCoordinate");
    	int cz = startSpot.getIntVariable("ZCoordinate");
    	int gx = goalSpot.getIntVariable("XCoordinate");
    	int gy = goalSpot.getIntVariable("YCoordinate");
    	int gz = goalSpot.getIntVariable("ZCoordinate");
    	int tmpX = startSpot.getIntVariable("XCoordinate");
    	int tmpY = startSpot.getIntVariable("YCoordinate");
    	int tmpZ = startSpot.getIntVariable("ZCoordinate");
    	int incX = 1;
    	int incY = 1;
    	int incZ = 1;
    	if (gx-cx < 0) {
    		incX = -1;
    		for (int i=0; i < x; i++) {
    			tmpX = cx - i-1;
    			route.addLast(celMap.get(tmpX + "_" + cy + "_" + cz));
    		}
    	} else if (gx-cx > 0) {
    		for (int i=0; i < x; i++) {
    			tmpX = cx + i+1;
    			route.addLast(celMap.get(tmpX + "_" + cy + "_" + cz));
    		}
    	}
    	if (gy-cy < 0) {
    		incY = -1;
    	}
    	if (gz-cz < 0) {
    		incZ = -1;
    	}    	
    	
    	if (tmpY!=gy) {
    		while(tmpY!=gy) {
        		tmpY = tmpY + incY;
        		route.addLast(celMap.get(tmpX + "_" + tmpY + "_" + cz));
        	}
    	}
    	if (tmpX!=gx) {
    		tmpY = gy;
    		while(tmpX!=gx) {
        		tmpX = tmpX + incX;
        		route.addLast(celMap.get(tmpX + "_" + tmpY + "_" + cz));
        	}
    	}
    	if (tmpZ!=gz) {
    		tmpX = gx;
    		tmpY = gy;
    		while(tmpZ!=gz) {
        		tmpZ = tmpZ + incZ;
        		route.addLast(celMap.get(gx + "_" + gy + "_" + tmpZ));
        	}
    	}
    	
    	return route;
    }
    
    // 先にX方向とY方向に1歩ずつ動いた後でX軸を合わせるよう動いた後Y座標をあわせる順の経路を取得するメソッド
    public LinkedList<Spot> searchRouteAfterX1Y1(Agent agent) {
		return searchRouteAfterX1Y1(agent.getSpotVariable("StartSpot"), agent.getSpotVariable("GoalSpot"));
	}
    
    // 先にX方向とY方向に1歩ずつ動いた後でX軸を合わせるよう動いた後Y座標をあわせる順の経路を取得するメソッド
    public LinkedList<Spot> searchRouteAfterX1Y1(Spot startSpot, Spot goalSpot){
    	LinkedList<Spot> route = new LinkedList<Spot>();
    	int cx = startSpot.getIntVariable("XCoordinate");
    	int cy = startSpot.getIntVariable("YCoordinate");
    	int cz = startSpot.getIntVariable("ZCoordinate");
    	int gx = goalSpot.getIntVariable("XCoordinate");
    	int gy = goalSpot.getIntVariable("YCoordinate");
    	int gz = goalSpot.getIntVariable("ZCoordinate");
    	int tmpX = startSpot.getIntVariable("XCoordinate");
    	int tmpY = startSpot.getIntVariable("YCoordinate");
    	int tmpZ = startSpot.getIntVariable("ZCoordinate");
    	int incX = 1;
    	int incY = 1;
    	int incZ = 1;
    	if (gx-cx < 0) {
    		incX = -1;
    	}
    	if (gy-cy < 0) {
    		incY = -1;
    	}
    	if (gz-cz < 0) {
    		incZ = -1;
    	}
    	
    	if (cx >= widthX) {
    		if (tmpX!=gx) {
        		while(tmpX!=gx) {
            		tmpX = tmpX + incX;
            		route.addLast(celMap.get(tmpX + "_" + tmpY + "_" + cz));
            	}
        	}
    		if (tmpY!=gy) {
        		while(tmpY!=gy) {
            		tmpY = tmpY + incY;
            		route.addLast(celMap.get(tmpX + "_" + tmpY + "_" + cz));
            	}
        	}
    	}
    	if (cy >= heightY) {
    		if (tmpY!=gy) {
        		while(tmpY!=gy) {
            		tmpY = tmpY + incY;
            		route.addLast(celMap.get(tmpX + "_" + tmpY + "_" + cz));
            	}
        	}
    		if (tmpX!=gx) {
        		while(tmpX!=gx) {
            		tmpX = tmpX + incX;
            		route.addLast(celMap.get(tmpX + "_" + tmpY + "_" + cz));
            	}
        	}
    	}
    	if (tmpZ!=gz) {
    		tmpX = gx;
    		tmpY = gy;
    		while(tmpZ!=gz) {
        		tmpZ = tmpZ + incZ;
        		route.addLast(celMap.get(gx + "_" + gy + "_" + tmpZ));
        	}
    	}
    	
    	return route;
    }
    
    //セル空間のマップ変数のキーとなる文字列を生成するメソッド
    public static String toStringXYZ(int x, int y, int z) {
    	String s = Integer.toString(x) + "_" + Integer.toString(y) + "_" + Integer.toString(z);
    	return s;
    }
    
    //エージェントが詰まったとき周囲の空いているスポットにランダムで移動させる(現在はGlobalSpotに移動させているのでこれは使用していない)
    public static Spot moveRandomSurroundSpot(Agent agent) {
    	Spot currentSpot = agent.getSpot();
    	Spot northSpot = currentSpot.getSpotVariable("northSpot");
    	Spot eastSpot = currentSpot.getSpotVariable("eastSpot");
    	Spot southSpot = currentSpot.getSpotVariable("southSpot");
    	Spot westSpot = currentSpot.getSpotVariable("westSpot");
    	Spot tmpSpot = currentSpot;
    	int maxMemberNum = 2;
    	LinkedList<Spot> availableSpotSet = new LinkedList<Spot>();
    	if (northSpot.getIntVariable("CurrentAgentNum") < maxMemberNum && northSpot.getIntVariable("2StepLaterAgentNum") < maxMemberNum) 
    		availableSpotSet.add(northSpot);
    	if (eastSpot.getIntVariable("CurrentAgentNum") < maxMemberNum && eastSpot.getIntVariable("2StepLaterAgentNum") < maxMemberNum) 
    		availableSpotSet.add(eastSpot);
    	if (southSpot.getIntVariable("CurrentAgentNum") < maxMemberNum && southSpot.getIntVariable("2StepLaterAgentNum") < maxMemberNum) 
    		availableSpotSet.add(southSpot);
    	if (westSpot.getIntVariable("CurrentAgentNum") < maxMemberNum && westSpot.getIntVariable("2StepLaterAgentNum") < maxMemberNum) 
    		availableSpotSet.add(westSpot);
    	if (availableSpotSet.size() > 0) {
    		tmpSpot = availableSpotSet.get((int)(Math.random()*(availableSpotSet.size()-1)));
    	}
    	return tmpSpot;
    }
    
    public HashSet<Spot> getSpaceAvailablePoints(Spot leaderSpot, int x, int y, int z) throws IOException {
    	HashSet<Spot> gymEastEntranceSet = (HashSet<Spot>)leaderSpot.getEquip("EastGymEntranceSet");
    	//HashSet<Spot> gymSouthEntranceSet = (HashSet<Spot>)leaderSpot.getEquip("SouthGymEntranceSet");
    	HashSet<Spot> gymReceptionSet = (HashSet<Spot>)leaderSpot.getEquip("AcceptanceEntranceSet");
    	HashSet<Spot> points = new HashSet<Spot>();
    	HashSet<Spot> remainingPoints;
    	HashSet<Agent> agentSet = (HashSet<Agent>)leaderSpot.getEquip("AgentSet");
    	HashSet<Spot> reachableSet = new HashSet<Spot>();
    	if (x >= y) {
    		remainingPoints = (HashSet<Spot>)leaderSpot.getEquip("YokoLivingSpaceAvailableSet");    		
    	} else {
    		remainingPoints = (HashSet<Spot>)leaderSpot.getEquip("TateLivingSpaceAvailableSet");
    	}
    	Iterator<Spot> spotIt = remainingPoints.iterator();
    	while(spotIt.hasNext()) {
    		Spot itSpot = spotIt.next();
    		if (isLifeSpaceAvailable(itSpot, x, y, z) && gymEastEntranceSet.size() != 0) {
    			HashSet<Spot> tmpLifeSpaceSet = (HashSet<Spot>)setLifeSpacesSet(itSpot, x, y, z);
    			HashSet<Spot> tmpEntranceSet = (HashSet<Spot>)getLifeSpaceEntranceSet(tmpLifeSpaceSet);
    			reachableSet = getReachableSetByAster(leaderSpot, gymEastEntranceSet);
				if (hasReachableSpot(tmpEntranceSet, reachableSet)) {
					Iterator<Agent> agentIt = agentSet.iterator();
					Boolean isTruePoint = true;
	    			while (agentIt.hasNext()) {
	    				Agent itAgent = agentIt.next();
	    				HashSet<Spot> spaceSet = (HashSet<Spot>)itAgent.getEquip("LivingSpaceSet");
	    				HashSet<Spot> entranceSet = (HashSet<Spot>)getLifeSpaceEntranceSet(spaceSet);
	    				if (hasReachableSpot(entranceSet, reachableSet) && hasReachableSpot(gymReceptionSet, reachableSet)) {
	    					continue;
	    				} else {
	    					isTruePoint = false;
	    					break;
	    				}
	    			}
	    			if(isTruePoint) {
	    				points.add(itSpot);
	    			}
				}
    			resetLifeSpacesSet(tmpLifeSpaceSet);
    		}
    	}
    	
    	return points;
    }
    
    public int getDistance(Agent agent) {
    	return getDistance(agent.getSpotVariable("StartSpot"), agent.getSpotVariable("GoalSpot"));
    }
    
    public int getDistance(Spot startSpot, Spot goalSpot) {
    	int sx = startSpot.getIntVariable("XCoordinate");
    	int sy = startSpot.getIntVariable("YCoordinate");
    	//int sz = startSpot.getIntVariable("ZCoordinate");
    	int gx = goalSpot.getIntVariable("XCoordinate");
    	int gy = goalSpot.getIntVariable("YCoordinate");
    	//int gz = startSpot.getIntVariable("ZCoordinate");
    	return getManhattanDistance(sx, sy, gx, gy);
    }
    
    public static boolean hasReachableSpot(HashSet<Spot> set1, HashSet<Spot> set2) {
    	Iterator<Spot> it1 = set1.iterator();
    	while(it1.hasNext()) {
    		Spot tmpSpot = it1.next();
    		if(set2.contains(tmpSpot))
    			return true;
    	}
    	return false;
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
	
		
	// 占有空間を集合変数に返すメソッド
	public void resetLifeSpacesSet(HashSet<Spot> set) {
		Iterator<Spot> resetIt = set.iterator();
		
		while(resetIt.hasNext()) {
			Spot spot = resetIt.next();
			spot.setKeyword("CellType", "corridor");
		}
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
		
		
		
		
//		public static void main(String[] args) throws Exception {
//	        new Aster();
//	    }

	    static final int INF = Integer.MAX_VALUE;    //INF値

	    //４方向探索用
	    static final int[] dx = { 0, 1, 0, -1};
	    static final int[] dy = {-1, 0, 1,  0};
	    static final char[] dir = {'u', 'r', 'd', 'l'};

	    String path = "";    //移動経路(戻値用)

	    //マンハッタン距離を求める
	    static int getManhattanDistance(int x1, int y1, int x2, int y2) {
	        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	    }
	    //チェビシェフ距離を求める
	    static int getChevyshevDistance(int x1, int y1, int x2, int y2) {
	        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
	    }

	    public LinkedList<Spot> Aster(Spot leaderSpot, Agent agent) {
			return Aster(leaderSpot, agent.getSpotVariable("StartSpot"), agent.getSpotVariable("GoalSpot"));
		}
	    //A*(A-star)探索アルゴリズム
	    public LinkedList<Spot> Aster(Spot leaderSpot, Spot startSpot, Spot goalSpot) {
	    	LinkedList<Spot> routeByAster = new LinkedList<Spot>();
	        int[][] grid = new int[maxX][maxY];    //移動コスト(距離)の記録
	        int sx, sy, gx, gy;              //スタートとゴール位置
	        sx = sy = gx = gy = 0;

	        //避難所データのパース
	        int tmpX = 0;
	        int tmpY = 0;
	        for (int i = 0; i < maxY; i++) {
	        	tmpY = i+1;
	            for (int j = 0; j < maxX; j++) {
	            	tmpX = j+1;
	            	Spot tmpSpot = getSpotXYZ(leaderSpot, tmpX, tmpY, 0);
	            	if (!tmpSpot.getKeyword("CellType").equals("corridor")){
	            		grid[i][j] = -1;
					} else if (tmpSpot == startSpot) {
	                    grid[i][j] = 0;        //スタートは距離０
	                    sy = i;
	                    sx = j;
	                } else if (tmpSpot == goalSpot) {
	                    grid[i][j] = INF;
	                    gy = i;
	                    gx = j;
	                } else {
	                    grid[i][j] = INF;
	                }
	            }
	        }

	        //A*(A-star) 探索
	        Queue<Position> q = new PriorityQueue<Position>();

	        Position p = new Position(sx, sy);
	        p.estimate = getManhattanDistance(sx, sy, gx, gy);    //推定値
	        q.add(p);

	        while (!q.isEmpty()) {
	            p = q.poll();
	            if (p.cost > grid[p.y][p.x]) {
	                continue;
	            }
	            if (p.y == gy && p.x == gx) {    //ゴールに到達
	                path = p.path;        //移動経路(戻値用)
	                break;
	            }

	            for (int i = 0; i < dx.length; i++) {
	                int nx = p.x + dx[i];
	                int ny = p.y + dy[i];
	                if (nx < 0 || maxX <= nx || ny < 0 || maxY <= ny) {    //範囲外
	                    continue;
	                }
	                if (grid[ny][nx] > grid[p.y][p.x] + 1) {
	                    grid[ny][nx] = grid[p.y][p.x] + 1;
	                    
	                    Spot nextSpot = getSpotXYZ(leaderSpot, nx+1, ny+1, 0);
	                    Position p2 = new Position(nx, ny);
	                    p2.cost = grid[ny][nx];        //移動コスト(スタートからの移動量)
	                    p2.estimate = getManhattanDistance(nx, ny, gx, gy) + p2.cost;    //推定値
	                    p2.path = p.path + dir[i];     //移動経路(移動方向の記録)
	                    q.add(p2);
	                }
	            }
	        }
	        
	        int tmpRouteX = sx+1;
	        int tmpRouteY = sy+1;
	        for (int i=0; i < path.length(); i++) {
	        	if (path.charAt(i) == 'u') {
	        		tmpRouteY = tmpRouteY-1;
	        		Spot tmpSpot = getSpotXYZ(leaderSpot, tmpRouteX, tmpRouteY, 0);
	        		routeByAster.addLast(tmpSpot);
	        	} else if (path.charAt(i) == 'r') {
	        		tmpRouteX = tmpRouteX+1;
	        		Spot tmpSpot = getSpotXYZ(leaderSpot, tmpRouteX, tmpRouteY, 0);
	        		routeByAster.addLast(tmpSpot);
	        	} else if (path.charAt(i) == 'd') {
	        		tmpRouteY = tmpRouteY+1;
	        		Spot tmpSpot = getSpotXYZ(leaderSpot, tmpRouteX, tmpRouteY, 0);
	        		routeByAster.addLast(tmpSpot);
	        	} else if (path.charAt(i) == 'l') {
	        		tmpRouteX = tmpRouteX-1;
	        		Spot tmpSpot = getSpotXYZ(leaderSpot, tmpRouteX, tmpRouteY, 0);
	        		routeByAster.addLast(tmpSpot);
	        	}
	        }
	        return routeByAster;    //ルートを返す
	    }
	    
	    //A*(A-star)探索アルゴリズム
    public HashSet<Spot> getReachableSetByAster(Spot leaderSpot, HashSet startSpotSet) {
    	HashSet<Spot> reachaleSetByAster = new HashSet<Spot>();
        int[][] grid = new int[maxX][maxY];    //移動コスト(距離)の記録
        int sx, sy;              //スタートとゴール位置
        sx = sy = 0;

        //避難所データのパース
        int tmpX = 0;
        int tmpY = 0;
        
        for (int i = 0; i < maxY; i++) {
        	tmpY = i+1;
            for (int j = 0; j < maxX; j++) {
            	tmpX = j+1;
            	Spot tmpSpot = getSpotXYZ(leaderSpot, tmpX, tmpY, 0);
            	if (!tmpSpot.getKeyword("CellType").equals("corridor")){
            		grid[i][j] = -1;
				} else {
                    grid[i][j] = INF;
                }
            }
        }
        
        
        Iterator<Spot> spotIt = startSpotSet.iterator();
        while(spotIt.hasNext()) {
        	Spot startSpot = spotIt.next();
        	reachaleSetByAster.add(startSpot);
    	    sx = startSpot.getIntVariable("XCoordinate")-1;
    	    sy = startSpot.getIntVariable("YCoordinate")-1;
    	    grid[sy][sx] = 0; //スタート地点のコストは0
    	    
    	    //A*(A-star) 探索
            Queue<PositionForRandom> q = new PriorityQueue<PositionForRandom>();

            PositionForRandom p = new PositionForRandom(sx, sy);
            q.add(p);
            
            while (!q.isEmpty()) {
                p = q.poll();

                for (int i = 0; i < dx.length; i++) {
                    int nx = p.x + dx[i];
                    int ny = p.y + dy[i];
                    if (nx < 0 || nx >= maxX || ny < 0 || ny >= maxY) {    //範囲外
                        continue;
                    }
                    if (grid[ny][nx] > grid[p.y][p.x] + 1) {
                        grid[ny][nx] = grid[p.y][p.x] + 1;
                        
                        Spot nextSpot = getSpotXYZ(leaderSpot, nx+1, ny+1, 0);
                        PositionForRandom p2 = new PositionForRandom(nx, ny);
                        p2.cost = grid[ny][nx];        //移動コスト(スタートからの移動量)
                        q.add(p2);
                        reachaleSetByAster.add(nextSpot);
                    }
                }
            }
        }
        return reachaleSetByAster;    //到達可能地点を返す
    }
    
	    //位置情報の構造体
	    class Position implements Comparable<Position>{
	        int x;               //座標
	        int y;
	        int cost;            //移動コスト(スタートからの移動量)
	        int estimate;        //推定値(ゴールまでのマンハッタン距離＋移動コスト)
	        String path = "";    //移動経路(移動方向の記録)

	        //コンストラクタ
	        public Position(int x, int y) {
	            this.x = x;
	            this.y = y;
	        }

	        //比較関数
	        @Override
	        public int compareTo(Position o) {
	            return this.estimate - o.estimate;    //推定値で小さい順
	        }
	    }
	    
	  //位置情報の構造体
	    class PositionForRandom implements Comparable<PositionForRandom>{
	        int x;               //座標
	        int y;
	        int cost;            //移動コスト(スタートからの移動量)
	        int estimate;        //推定値(ゴールまでのマンハッタン距離＋移動コスト)
	        String path = "";    //移動経路(移動方向の記録)

	        //コンストラクタ
	        public PositionForRandom(int x, int y) {
	            this.x = x;
	            this.y = y;
	        }

	        //比較関数
	        @Override
	        public int compareTo(PositionForRandom o) {
	            return this.cost - o.cost;    //推定値で小さい順
	        }
	    }
}
