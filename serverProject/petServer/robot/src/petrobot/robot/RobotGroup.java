package petrobot.robot;

import petrobot.robot.net.Client;

import java.io.IOException;

/**
 * 机器人组，一组有一个Transport
 */
public class RobotGroup {
	
	private RobotConfig config;
	
	private int index;

	public RobotGroup(RobotConfig config, int index) {
		this.config = config;
		this.index = index;
	}
	
	public void start() throws IOException{
		int index = 0;
		while(index < config.getRobotCountPerGroup()){
			Client client = new Client(config.getDomain(), config.getPort());
			client.connect();
			int robotIndex = config.getRobotBeginIndex() + index;
			Robot robot = RobotManager.getInstance().createRobot(client, config.getLoginNamePre() +"-"+ + this.index + robotIndex);
			robot.setReady(true);
			index++;
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
