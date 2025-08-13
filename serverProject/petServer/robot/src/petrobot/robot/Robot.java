package petrobot.robot;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petrobot.robot.index.IndexManager;
import petrobot.robot.net.Client;
import petrobot.robotConst.DealResultConst;
import petrobot.tick.GlobalTick;
import petrobot.util.BaseObj;
import petrobot.util.LogUtil;
import protocol.GM.CS_GM;
import protocol.GM.CS_GM.Builder;
import protocol.LoginProto.CS_Login;
import protocol.LoginProto.CS_Ping;
import protocol.LoginProto.ClientData;
import protocol.MessageId.MsgIdEnum;

/**
 * 机器人
 */
@Setter
@Getter
public class Robot extends BaseObj {

    private static final Logger LOGGER = LoggerFactory.getLogger(Robot.class);

    private ScheduledFuture<?> future;

    private int id;

    private Client client;

    private String loginName;

    private boolean ready;

    private String userId;

    private boolean initRobot;

    private boolean online;

    private AtomicInteger dealResult = new AtomicInteger(1);

    private PlayerData data = new PlayerData();

    private int index;

    private long lastPrintMistTime;

    private int methodId;

    public int getDealResult() {
        return dealResult.intValue();
    }

    public void setDealResult(int dealResult) {
        this.dealResult.set(dealResult);
        if (dealResult == DealResultConst.CUR_STEP_RUNNING) {
            curStepStartTime.set(GlobalTick.getInstance().getCurrentTime());
        }
    }

    public Robot(Client client, String name) {
        this.client = client;
        this.loginName = name;
    }

    public void createRole() {
    }

    public void stop() {
        this.future.cancel(true);
        RobotManager.getInstance().removeRobot(this.getId());
        this.client.close();
        LOGGER.error(this.loginName + "退出游戏");
    }

    private AtomicLong curStepStartTime = new AtomicLong();
    /**
     * 当前步骤超时时间  战斗结算延时目前是30s
     **/
    private static long STEP_OVER_TIME_OUT = 1000 * 40;

    /**
     * 和服务器的心跳
     */
    public void heart() {
        if (client == null || !client.getChannel().isActive()) {
            if (RobotConfig.getInstance().isRobotCycle()) {
				reStart();
            } else {
                LogUtil.error("robot[" + getId() + "] client is null");
                return;
            }
        }
        client.send(MsgIdEnum.CS_Ping_VALUE, CS_Ping.newBuilder());

        //最后一个逻辑循环执行不判断超时
        if (getIndex() == IndexManager.getIns().maxIndex && RobotConfig.getInstance().isRobotCycle()) {
            return;
        }

        if (GlobalTick.getInstance().getCurrentTime() - curStepStartTime.get() > STEP_OVER_TIME_OUT) {
            LogUtil.error("robot[" + getLoginName() + "] execute step time out, step = " + getIndex()
                    + ", method id:" + IndexManager.getIns().METHOD_MAPPING.get(getIndex()));
        }
    }

    public void reStart() {
        if (this.client != null) {
            if (this.client.getChannel() != null) {
                this.client.getChannel().close();
            }
        }

        RobotConfig config = RobotConfig.getInstance();
        Client client = new Client(config.getDomain(), config.getPort());
        client.connect();

        this.client = client;
        this.data = new PlayerData();

        RobotManager.getInstance().registerRobot(client, this);

        init();
        setIndex(1);
        setDealResult(DealResultConst.CUR_STEP_SUCCESS);
    }

    @Override
    public String getBaseIdx() {
        return null;
    }

    @Override
    public String getClassType() {
        return null;
    }

    @Override
    public void putToCache() {

    }

    @Override
    public void transformDBData() {
    }

    /**
     * init 必须要在玩家登陆后玩家存在才能加资源
     */
    public void init() {
        CS_Login.Builder logInBuilder = CS_Login.newBuilder();
        logInBuilder.setIsResume(false);
        logInBuilder.setToken(String.valueOf(getId()));
        logInBuilder.setUserId(getUserId());
        logInBuilder.setClientData(ClientData.newBuilder().build());
        getClient().send(MsgIdEnum.CS_Login_VALUE, logInBuilder);

    }

    public void addInitRes() {
        Builder builder = CS_GM.newBuilder();
        builder.setStr("res");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

/*
		builder.setStr("pet");
		getClient().send(MsgIdEnum.CS_GM_VALUE, builder);
*/

        builder.setStr("addDiamond|100000");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

        builder.setStr("addMail|2");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

        builder.setStr("addMail|3");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

        builder.setStr("addMail|4");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

        builder.setStr("addItem|1100|10");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

        builder.setStr("level|50");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

//		builder.setStr("addExp|50000");
//		getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

        builder.setStr("recharge|2");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);

        builder.setStr("setmistpermitlevel|1");
        getClient().send(MsgIdEnum.CS_GM_VALUE, builder);
    }
}
