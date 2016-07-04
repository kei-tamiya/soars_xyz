package module.xyz;

import env.Spot;
import java.util.LinkedList;

public class Output {
	
	public static void outPut(Spot spot) {
		LinkedList<LinkedList<Integer>> list1 = (LinkedList<LinkedList<Integer>>)spot.getEquip("大トイレ待ち時間リスト");
		LinkedList<LinkedList<Integer>> list2 = (LinkedList<LinkedList<Integer>>)spot.getEquip("小トイレ待ち時間リスト");
		LinkedList<LinkedList<Integer>> list3 = (LinkedList<LinkedList<Integer>>)spot.getEquip("女子トイレ待ち時間リスト");
		LinkedList<LinkedList<Integer>> list4 = (LinkedList<LinkedList<Integer>>)spot.getEquip("受付待ち時間リスト");
		
		System.out.println("大トイレ待ち時間リスト : " + list1);
		System.out.println("小トイレ待ち時間リスト : " + list2);
		System.out.println("女子トイレ待ち時間リスト : " + list3);
		System.out.println("受付待ち時間リスト : " + list4);
		
	}
}
