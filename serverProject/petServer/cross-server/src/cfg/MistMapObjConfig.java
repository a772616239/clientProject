/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;
import protocol.MistForest.MistUnitTypeEnum;
import util.GameUtil;

@annationInit(value = "MistMapObjConfig", methodname = "initConfig")
public class MistMapObjConfig extends baseConfig<MistMapObjConfigObject> {


    private static MistMapObjConfig instance = null;

    public static MistMapObjConfig getInstance() {

        if (instance == null)
            instance = new MistMapObjConfig();
        return instance;

    }


    public static Map<Integer, MistMapObjConfigObject> _ix_id = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MistMapObjConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistMapObjConfig");

        for (Map e : ret) {
            put(e);
        }

    }

    public static MistMapObjConfigObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, MistMapObjConfigObject config) {

        config.setId(MapHelper.getInt(e, "ID"));

        config.setMaprule(MapHelper.getInt(e, "MapRule"));

        config.setMaplevel(MapHelper.getInt(e, "MapLevel"));

        config.setObjtype(MapHelper.getInt(e, "ObjType"));

        config.setDelayborntime(MapHelper.getInt(e, "DelayBornTime"));

        config.setInitpos(MapHelper.getInts(e, "InitPos"));

        config.setInittoward(MapHelper.getInts(e, "InitToward"));

        config.setInitprop(MapHelper.getIntArray(e, "InitProp"));

        config.setRebornpropchangeinfo(MapHelper.getInts(e, "RebornPropChangeInfo"));


        _ix_id.put(config.getId(), config);

        long key = GameUtil.mergeIntToLong(config.getMaprule(), config.getMaplevel());
        if (config.getObjtype() == MistUnitTypeEnum.MUT_PosObj_VALUE) {
            List<MistMapObjConfigObject> bornPos = level_bornPosMap.get(key);
            if (bornPos == null) {
                bornPos = new ArrayList<>();
                level_bornPosMap.put(key, bornPos);
            }
            bornPos.add(config);
        } else {
            List<MistMapObjConfigObject> objList = level_objMap.get(key);
            if (objList == null) {
                objList = new ArrayList<>();
                level_objMap.put(key, objList);
            }
            objList.add(config);
        }
    }

    /**************************分割线*********************************/
    public Map<Long, List<MistMapObjConfigObject>> level_objMap = new HashMap<>();

    public Map<Long, List<MistMapObjConfigObject>> level_bornPosMap = new HashMap<>();

    public List<MistMapObjConfigObject> getMapObjByMaplevel(int mistRule, int maplevel) {
        List<MistMapObjConfigObject> obj0List = level_objMap.get(GameUtil.mergeIntToLong(mistRule, 0)); // 0表示所有层数都有
        List<MistMapObjConfigObject> objList = level_objMap.get(GameUtil.mergeIntToLong(mistRule, maplevel));
        if (obj0List == null || maplevel >= 1000) {
            return objList;
        } else if (objList == null) {
            return obj0List;
        } else {
            List<MistMapObjConfigObject> totalObjList = new ArrayList<>();
            totalObjList.addAll(obj0List);
            totalObjList.addAll(objList);
            return totalObjList;
        }
    }

    public List<MistMapObjConfigObject> getBornPosByMaplevel(int mistRule, int maplevel) {
        List<MistMapObjConfigObject> obj0List = level_bornPosMap.get(GameUtil.mergeIntToLong(mistRule, 0)); // 0表示所有层数都有
        List<MistMapObjConfigObject> objList = level_bornPosMap.get(GameUtil.mergeIntToLong(mistRule, maplevel));
        if (obj0List == null || maplevel >= 1000) {
            return objList;
        } else if (objList == null) {
            return obj0List;
        } else {
            List<MistMapObjConfigObject> totalObjList = new ArrayList<>();
            totalObjList.addAll(obj0List);
            totalObjList.addAll(objList);
            return totalObjList;
        }
    }
}
