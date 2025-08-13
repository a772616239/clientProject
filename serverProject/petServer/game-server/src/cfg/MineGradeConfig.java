/*CREATED BY TOOL*/

package cfg;

import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.base.baseConfig;

@annationInit(value = "MineGradeConfig", methodname = "initConfig")
public class MineGradeConfig extends baseConfig<MineGradeConfigObject> {


    private static MineGradeConfig instance = null;

    public static MineGradeConfig getInstance() {

        if (instance == null)
            instance = new MineGradeConfig();
        return instance;

    }


    public static Map<Integer, MineGradeConfigObject> _ix_grade = new HashMap<>();


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (MineGradeConfig) o;
        initConfig();
    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MineGradeConfig");

        for (Map e : ret) {
            put(e);
        }

        mineGradConfigList = _ix_grade.values().stream().sorted(Comparator.comparingInt(MineGradeConfigObject::getNeedexp)).collect(Collectors.toList());
    }

    public static MineGradeConfigObject getByGrade(int grade) {

        return _ix_grade.get(grade);

    }


    public void putToMem(Map e, MineGradeConfigObject config) {

        config.setGrade(MapHelper.getInt(e, "Grade"));

        config.setNeedexp(MapHelper.getInt(e, "NeedExp"));

        config.setPlusfactor_type1(MapHelper.getInt(e, "PlusFactor_Type1"));

        config.setPlusfactor_type2(MapHelper.getInt(e, "PlusFactor_Type2"));

        config.setPlusfactor_type3(MapHelper.getInt(e, "PlusFactor_Type3"));

        config.setDailylimitplusfactor(MapHelper.getInt(e, "DailyLimitPlusFactor"));


        _ix_grade.put(config.getGrade(), config);

        if (maxMineExp < config.getNeedexp()) {
            setMaxMineExp(config.getNeedexp());
        }
    }

    protected int maxMineExp;
    protected List<MineGradeConfigObject> mineGradConfigList;

    public int getMaxMineExp() {
        return maxMineExp;
    }

    public void setMaxMineExp(int maxMineExp) {
        this.maxMineExp = maxMineExp;
    }

    public MineGradeConfigObject getMineGradCfgByExp(int exp) {
        MineGradeConfigObject config = null;
        if (mineGradConfigList != null) {
            for (MineGradeConfigObject cfg : mineGradConfigList) {
                if (exp < cfg.getNeedexp()) {
                    break;
                }
                config = cfg;
            }
        }
        return config;
    }
}
