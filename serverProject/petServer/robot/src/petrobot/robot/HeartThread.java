package petrobot.robot;

import java.util.Map;

public class HeartThread implements Runnable{

	private Map<Integer, Robot> robots = null;
	
	public HeartThread(Map<Integer, Robot> robots) {
		super();
		this.robots = robots;
	}


	@Override
	public void run() {
		if(robots == null){
			return;
		}
		
		for(int name : robots.keySet()){
			Robot robot = robots.get(name);
			if(robot == null || !robot.isReady()){
				continue;
			}
			robot.heart();
		}
	}
	
}
