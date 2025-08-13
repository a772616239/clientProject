package petrobot.robot;


import io.netty.channel.Channel;
import petrobot.robot.net.Client;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RobotManager {

	private static AtomicInteger ID = new AtomicInteger();

	private Map<Integer, Robot> robots = new ConcurrentHashMap<>();

	private Map<Channel, Integer> channelRobotIdMap = new ConcurrentHashMap<>();

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);

	private static final RobotManager INSTANCE = new RobotManager();

	public static RobotManager getInstance(){
		return INSTANCE;
	}

	private RobotManager(){
		executor.scheduleWithFixedDelay(new HeartThread(robots), 20000, 10000, TimeUnit.MILLISECONDS);
	}

	public Robot createRobot(Client client, String name){
		int id = ID.incrementAndGet();
		Robot robot = new Robot(client, name);
		robot.setId(id);
		robot.setUserId("robot-"+name);
		registerRobot(client, robot);
		ScheduledFuture<?> ret = executor.scheduleWithFixedDelay(new RobotAI(robot), 1000, 1000, TimeUnit.MILLISECONDS);
		robot.setFuture(ret);
		return robot;
	}

	public Robot getRobot(int robotId){
		return robots.get(robotId);
	}

	public void removeRobot(int robotId){
		this.robots.remove(robotId);
	}

	public Robot getRobotByChannel(Channel channel) {
		Integer integer = channelRobotIdMap.get(channel);
		if (integer == null) {
			return null;
		}
		return robots.get(integer);
	}

	public void registerRobot(Client client, Robot robot) {
		if (client == null || robot == null) {
			return;
		}

		robots.put(robot.getId(), robot);
		channelRobotIdMap.put(client.getChannel(), robot.getId());
	}
}
