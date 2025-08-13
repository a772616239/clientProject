package model;

import com.alibaba.fastjson.JSONObject;
import common.GlobalThread;
import common.HttpRequestUtil;
import common.HttpRequestUtil.PlatFormRetCode;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import common.tick.Tickable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Activity.ActivityTypeEnum;
import protocol.Common.EnumFunction;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2021/1/28
 */
public class FunctionManager implements Runnable, Tickable {
    private static FunctionManager instance;

    public static FunctionManager getInstance() {
        if (instance == null) {
            synchronized (FunctionManager.class) {
                if (instance == null) {
                    instance = new FunctionManager();
                }
            }
        }
        return instance;
    }

    private FunctionManager() {
    }

    private final Map<EnumFunction, Boolean> functionOpenCondition = new ConcurrentHashMap<>();

    private long nextQueryTime;

    public static final long QUERY_INTERVAL = TimeUtil.MS_IN_A_MIN;

    public boolean init() {
        //全部功能初始化为开放中
        for (EnumFunction value : EnumFunction.values()) {
            if (value == EnumFunction.NullFuntion || value == EnumFunction.UNRECOGNIZED) {
                continue;
            }
            functionOpenCondition.put(value, true);
        }
        return GlobalTick.getInstance().addTick(this);
    }

    public boolean functionOpening(EnumFunction function) {
        if (function == null || function == EnumFunction.NullFuntion) {
            return true;
        }
        Boolean condition = functionOpenCondition.get(function);
        if (condition == null) {
            LogUtil.error("model.FunctionManager.functionOpening, function is not exist in manager:" + function);
        }
        return Boolean.TRUE.equals(condition);
    }

    public boolean functionClosed(EnumFunction function) {
        return !functionOpening(function);
    }

    public boolean closeFunction(EnumFunction function) {
        if (function == null || function == EnumFunction.UNRECOGNIZED) {
            return false;
        }

        LogUtil.info("model.FunctionManager.openFunction, function:" + function);

        boolean closeRet = doFunctionCloseOperate(function);
        if (closeRet) {
            this.functionOpenCondition.put(function, false);
            return true;
        }

        LogUtil.error("model.FunctionManager.closeFunction, function:" + function + ", close failed");

        return false;
    }

    /**
     * 此处执行关闭功能后的后续操作
     *
     * @param function
     * @return
     */
    private boolean doFunctionCloseOperate(EnumFunction function) {
        if (function == EnumFunction.TheWar) {
            CrossServerManager.getInstance().kickOutAllWarPlayers();
        } else if (function == EnumFunction.MistForest) {
            CrossServerManager.getInstance().kickOutAllMistPlayer();
        }
        return true;
    }

    public boolean openFunction(EnumFunction function) {
        if (function == null || function == EnumFunction.UNRECOGNIZED) {
            return false;
        }

        LogUtil.info("model.FunctionManager.openFunction, function:" + function);

        boolean openRet = doFunctionOpenOperate(function);
        if (openRet) {
            this.functionOpenCondition.put(function, true);
            return true;
        }

        LogUtil.error("model.FunctionManager.openFunction, function:" + function + ", open failed");
        return false;
    }

    private boolean doFunctionOpenOperate(EnumFunction function) {
        return true;
    }

    public boolean changeFunctionStatus(EnumFunction function, boolean newStatus) {
        if (function == null) {
            return false;
        }

        Boolean oldStatue = this.functionOpenCondition.get(function);
        if (oldStatue != null && oldStatue == newStatus) {
            LogUtil.debug("model.FunctionManager.changeFunctionStatus, function:" + function
                    + ",old status is equal new status:" + newStatus);
            return true;
        }

        if (newStatus) {
            return openFunction(function);
        } else {
            return closeFunction(function);
        }
    }

    @Override
    public synchronized void onTick() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (this.nextQueryTime < currentTime) {
            GlobalThread.getInstance().execute(this);
            this.nextQueryTime = currentTime + QUERY_INTERVAL;
        }
    }

    @Override
    public void run() {
        String functionUrl = ServerConfig.getInstance().getFunctionFindAll();
        String clientId = ServerConfig.getInstance().getClientId();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("clientId", clientId);
        String queryResult = HttpRequestUtil.doPost(functionUrl, jsonObj.toJSONString());
        if (StringUtils.isEmpty(queryResult)) {
            return;
        }

        FunctionFindAllRet findAllRet = JSONObject.parseObject(queryResult, FunctionFindAllRet.class);
        if (findAllRet.getRetCode() != PlatFormRetCode.SUCCESS) {
            LogUtil.error("model.FunctionManager.run, query function open status failed, retCode:"
                    + findAllRet.getRetCode() + ",msg:" + findAllRet.getRetDes());
            return;
        }

        if (findAllRet.isNotEmpty()) {
            for (FunctionStatus data : findAllRet.getData()) {
                changeFunctionStatus(data.getFunction(), data.isOpen());
            }
        }
    }

    /**
     * 此处只需要处理服务器本地配置的活动
     * 暂时只处理许愿池和零元购
     */
    private static final Map<ActivityTypeEnum, EnumFunction> ACTIVITY_TYPE_FUNCTION_MAP;
    static {
        Map<ActivityTypeEnum, EnumFunction> temp = new HashMap<>();

        temp.put(ActivityTypeEnum.ATE_WishWell, EnumFunction.WishingWell);
        temp.put(ActivityTypeEnum.ATE_ZeroCostPurchase, EnumFunction.ZeroCostPurchase);

        ACTIVITY_TYPE_FUNCTION_MAP = Collections.unmodifiableMap(temp);
    }

    public boolean activityIsOpen(ActivityTypeEnum activityType) {
        if (activityType == null
                || activityType == ActivityTypeEnum.ATE_Null
                || activityType == ActivityTypeEnum.UNRECOGNIZED) {
            return false;
        }
        return functionOpening(ACTIVITY_TYPE_FUNCTION_MAP.get(activityType));
    }
}

@Getter
@Setter
class FunctionFindAllRet {
    private int retCode;
    private String retDes;
    private List<FunctionStatus> data;

    public boolean isNotEmpty() {
        return CollectionUtils.isNotEmpty(data);
    }
}

@Getter
@Setter
class FunctionStatus {
    private int functionId;
    private String functionName;
    private int flag;

    public EnumFunction getFunction() {
        return EnumFunction.forNumber(functionId);
    }

    public boolean isOpen() {
        return flag == 1;
    }
}