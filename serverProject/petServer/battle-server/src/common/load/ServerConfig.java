package common.load;

import datatool.StringHelper;
import model.http.HttpRequestUtil;
import servertool.Config;
import servertool.ServerCommonConfig;
import util.GameUtil;
import util.LogUtil;

import java.lang.reflect.Field;


public class ServerConfig extends ServerCommonConfig{

	private static ServerConfig instance = new ServerConfig();

	private ServerConfig(){
		
	}
	
	public static ServerConfig getInstance() {
		return instance;
	}

	private String ip;
	private String logsAddress;
	private int maxOnlinePlayerNum;
	private String payCenterIp;
	private int payCenterHttpPort;
	private String accountIp;
	private String globalRedisHost;
	private int globalRedisPort;
	private int threadCount;
	private String handlerPath;
	private String jsonPath;

	private String clientId;
	private String clientSecret;

	private long timeTickCycle;
	private long printSvrInfoCycle;

	private String battleServerHost;
	private int battleServerPort;

	private boolean openBattleCheck;
	@CheckHttpConnect
	private String battleCheckUrl;

	private int battleTickCycle;

	private boolean canGmEndBattle;
	private boolean debug;

	private String redisHost;
	private String redisPassword;
	private int redisDbIndex;


	public String getClientId() {
		return clientId;
	}

	public void setClientId() {
		this.clientId = Config.get("clientId");
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret() {
		this.clientSecret = Config.get("clientSecret");
	}

	public int getRedisDbIndex() {
		return redisDbIndex;
	}

	public void setRedisHost() {
		this.redisHost = Config.get("redis.host");
	}

	public String getRedisPassword() {
		return redisPassword;
	}

	public void setRedisPassword() {
		this.redisPassword = Config.get("redis.password");
	}

	public void setRedisDbIndex() {
		this.redisDbIndex = StringHelper.stringToInt(Config.get("redis.index"),0);
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setHandlePath() {
		this.handlerPath = Config.get("handlerPath");
	}

	public String getHandlePath() {
		if (StringHelper.isNull(this.handlerPath))
			setHandlePath();
		return handlerPath;
	}

	public String getGlobalRedisHost() {
		
		if(StringHelper.isNull(this.globalRedisHost))
			setGlobalRedisHost();
		return this.globalRedisHost;
	}

	public void setGlobalRedisHost() {
		
		this.globalRedisHost = Config.get("golbalredis.host");
	}

	public int getGlobalRedisPort() {
		
		
		if(globalRedisPort == 0)
			setGlobalRedisPort();
		return globalRedisPort;
		
	}

	public void setGlobalRedisPort() {
		this.globalRedisPort = StringHelper.stringToInt(Config.get("golbalredis.port"),0);
	}
	
	
	

	public String getAccountIp() {
		if(StringHelper.isNull(this.accountIp))
			return null;
		return accountIp;
	}

	public void setAccountIp(String accountIp) {
		this.accountIp = Config.get("accountIp");
	}


	private void setAccountIp(){
		this.accountIp = Config.get("accountIp");
	}

	public int getMaxOnlinePlayerNum() {
		if (maxOnlinePlayerNum <= 0) {
			setMaxOnlinePlayerNum();
		}
		return maxOnlinePlayerNum;
	}

	public void setMaxOnlinePlayerNum() {
		this.maxOnlinePlayerNum = StringHelper.stringToInt(Config.get("maxOnlinePlayerNum"), 0);
	}

	private void setPayCenterIp(){
		this.payCenterIp = Config.get("payCenterIp");
	}
	public String getPayCenterIp(){
		if(StringHelper.isNull(this.payCenterIp))
			return null;
		return this.payCenterIp;
	}
	
	private void setPayCenterHttpPort(){
		this.payCenterHttpPort = StringHelper.stringToInt(Config.get("payCenterHttpPort"),0);
	}
	public int getPayCenterHttpPort(){
		if(this.payCenterHttpPort == 0)
			return 0;
		return this.payCenterHttpPort;
	}
	
	public void init(){
		setAccountIp();
		setPayCenterHttpPort();
		setPayCenterIp();
		
		setServer();
		setIp();
		setPort();
		setMaxOnlinePlayerNum();
		
		setLogsAddress();
		setLanguage();
		setHandlePath();
		
		setMaxActive();
		setMaxIdle();
		setMaxWait();
		setHost();
		setPassword();
		setRedisport();
		setRediskey();
		setHttpPort();
		setGlobalRedisHost();
		setGlobalRedisPort();
		setThreadCount();

		setJsonPath();

		setTimeTickCycle();
		setPrintSvrInfoCycle();

		setBattleServerHost();
		setBattleServerPort();

		setBattleCheckUrl();
		setBattleTickCycle();
		setOpenBattleCheck();
		setCanGmEndBattle();
		setDebug();

		setClientId();
		setClientSecret();
		setRedisHost();
		setRedisPassword();
		setRedisDbIndex();

		//checkHttpConnect();
	}

	private void checkHttpConnect()  {
		Field[] fields = this.getClass().getDeclaredFields();
		LogUtil.info("##################before http connect test start##################");
		for (Field field : fields) {
			if (field.isAnnotationPresent(CheckHttpConnect.class)){
				String url = null;
				try {
					url = (String) field.get(this);
				} catch (Exception e) {
					LogUtil.printStackTrace(e);
				}
				HttpRequestUtil.doPostConnectTest(url, "");
			}
		}
		LogUtil.info("##################before http connect test end##################");
	}


	private void setIp(){
		this.ip = Config.get("ip");
	}
	public String getIp(){
		if(StringHelper.isNull(this.ip))
			return null;
		
		return this.ip;
	}
	
	private void setLogsAddress(){
		this.logsAddress = Config.get("logsaddress");
	}
	public String getLogsAddress(){
		if(StringHelper.isNull(this.logsAddress))
			setLogsAddress();
		
		return this.logsAddress;
	}
	
	private int httpPort;//http端口
	
	
	
	public int getHttpPort() {
		if(httpPort == 0)
			setHttpPort();
		return httpPort;
	}

	public void setHttpPort() {
		this.httpPort = StringHelper.stringToInt(Config.get("httpport"),0);
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount() {
		this.threadCount = StringHelper.stringToInt(Config.get("gameConfig.threadCount"),0);
	}

	public String getJsonPath() {
		return jsonPath;
	}

	public void setJsonPath() {
		this.jsonPath = Config.get("jsonPath");
	}

	public long getTimeTickCycle() {
		return timeTickCycle;
	}

	public void setTimeTickCycle() {
		this.timeTickCycle = GameUtil.stringToLong(Config.get("gameConfig.TimeTickCycle"), 100);
	}

	public long getPrintSvrInfoCycle() {
		return printSvrInfoCycle;
	}

	public void setPrintSvrInfoCycle() {
		this.printSvrInfoCycle = GameUtil.stringToLong(Config.get("gameConfig.PrintServerInfoCycle"), 30000);
	}

	public String getBattleServerHost() {
		return battleServerHost;
	}

	public void setBattleServerHost() {
		this.battleServerHost = Config.get("battleServer.host");
	}

	public int getBattleServerPort() {
		return battleServerPort;
	}

	public void setBattleServerPort() {
		this.battleServerPort = StringHelper.stringToInt(Config.get("battleServer.port"), 0);
	}

	public boolean isOpenBattleCheck() {
		return openBattleCheck;
	}

	public void setOpenBattleCheck() {
		try {
			this.openBattleCheck = Boolean.parseBoolean(Config.get("battleCheck.open"));
		} catch (Exception e) {
			this.openBattleCheck = false;
		}
	}

	public String getBattleCheckUrl() {
		return battleCheckUrl;
	}

	public void setBattleCheckUrl() {
		this.battleCheckUrl = Config.get("battleCheck.url");
	}

	public int getBattleTickCycle() {
		return battleTickCycle;
	}

	public void setBattleTickCycle() {
		this.battleTickCycle = StringHelper.stringToInt(Config.get("battleCheck.tickCycle"), 100);
	}

	public boolean canGmEndBattle() {
		return canGmEndBattle;
	}

	public void setCanGmEndBattle() {
		try {
			this.canGmEndBattle = Boolean.parseBoolean(Config.get("battleCheck.canGmEndBattle"));
		} catch (Exception e) {
			this.canGmEndBattle = false;
		}
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug() {
		this.debug = Boolean.parseBoolean(Config.get("debug"));
	}

	public static void main(String[] args) {
		System.out.println(ServerConfig.getInstance().getServer());
		System.out.println(ServerConfig.getInstance().getLogsAddress());
		System.out.println(ServerConfig.getInstance().getLanguage());
	}
	
	
	
}
