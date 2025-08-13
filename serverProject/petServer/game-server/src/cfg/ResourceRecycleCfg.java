/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.Getter;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import model.consume.ConsumeUtil;
import protocol.Common;

@annationInit(value = "ResourceRecycleCfg", methodname = "initConfig")
public class ResourceRecycleCfg extends baseConfig<ResourceRecycleCfgObject> {

    private static int patrolFightNum;
    private static int patrolTreasure;
    private static int patrolRewardRate;


    private static int braveChallengePoint;

    private static ResourceRecycleCfg instance = null;

    public static ResourceRecycleCfg getInstance() {

        if (instance == null)
            instance = new ResourceRecycleCfg();
        return instance;

    }

    private static final Map<Common.EnumFunction, Common.Consume> baseConsumeMap = new HashMap<>();

    private static final Map<Common.EnumFunction, Common.Consume> advancedConsumeMap = new HashMap<>();


    public static Map<Integer, ResourceRecycleCfgObject> _ix_functionid = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (ResourceRecycleCfg) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "ResourceRecycleCfg");

        for (Map e : ret) {
            put(e);
        }
        initConsumeMap();
        initField();

    }

    private void initField() {
        initPatrolField();

        initBraveChallengeField();

    }

    private void initBraveChallengeField() {
        ResourceRecycleCfgObject cfg = getByFunctionid(Common.EnumFunction.CourageTrial.getNumber());
        if (cfg != null) {
            int[] params = cfg.getParams();
            if (params.length < 1) {
                throw new RuntimeException("ResourceRecycleCfg courage trial  config error cause by params length not enough");
            }
            braveChallengePoint = params[0];
        }
    }

    private void initPatrolField() {
        ResourceRecycleCfgObject cfg = getByFunctionid(Common.EnumFunction.Patrol.getNumber());
        if (cfg != null) {
            int[] params = cfg.getParams();
            if (params.length < 3) {
                throw new RuntimeException("ResourceRecycleCfg patrol config error cause by params length not enough");
            }
            patrolFightNum = params[0];
            patrolTreasure = params[1];
            patrolRewardRate = params[2];
        }
    }

    private void initConsumeMap() {
        Common.Consume baseConsume;
        Common.Consume advancedConsume;
        Common.EnumFunction function;
        for (ResourceRecycleCfgObject cfg : _ix_functionid.values()) {
            if (cfg.getFunctionid() <= 0) {
                continue;
            }
            function = Common.EnumFunction.forNumber(cfg.getFunctionid());
            baseConsume = ConsumeUtil.parseConsume(cfg.getBaseconsume());
            advancedConsume = ConsumeUtil.parseConsume(cfg.getAdvancedconsume());
            baseConsumeMap.put(function, baseConsume);
            advancedConsumeMap.put(function, advancedConsume);
        }
    }

    public static ResourceRecycleCfgObject getByFunctionid(int functionid) {

        return _ix_functionid.get(functionid);

    }


    public void putToMem(Map e, ResourceRecycleCfgObject config) {

        config.setFunctionid(MapHelper.getInt(e, "functionId"));

        config.setBaseconsume(MapHelper.getInts(e, "baseConsume"));

        config.setAdvancedconsume(MapHelper.getInts(e, "advancedConsume"));

        config.setParams(MapHelper.getInts(e, "params"));


        _ix_functionid.put(config.getFunctionid(), config);


    }

    public Common.Consume getBaseConsume(Common.EnumFunction function) {
        return baseConsumeMap.get(function);
    }

    public Common.Consume getAdvancedConsume(Common.EnumFunction function) {
        return advancedConsumeMap.get(function);
    }

    public static int getPatrolFightNum() {
        return patrolFightNum;
    }

    public static int getPatrolTreasure() {
        return patrolTreasure;
    }

    public static int getPatrolRewardRate() {
        return patrolRewardRate;
    }

    public static int getBraveChallengePoint() {
        return braveChallengePoint;
    }

}
