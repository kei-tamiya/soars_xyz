package module.xyz;

import env.Spot;

public class Time {
	public static void getTicks(Spot spot) {
		time.Time clock = spot.getTimeVariable("Clock");
		int minute = spot.getTimeVariable("Clock").getMinutes();
		minute = minute;
		clock.setMinutes(minute);
	}
}