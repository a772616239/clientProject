/*CREATED BY TOOL*/

package cfg;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value ="NewForeignInvasionBuildingsConfig", methodname = "initConfig")
public class NewForeignInvasionBuildingsConfig extends baseConfig<NewForeignInvasionBuildingsConfigObject>{


private static NewForeignInvasionBuildingsConfig instance = null;

public static NewForeignInvasionBuildingsConfig getInstance() {

if (instance == null)
instance = new NewForeignInvasionBuildingsConfig();
return instance;

}


public static Map<Integer, NewForeignInvasionBuildingsConfigObject> _ix_buildingid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (NewForeignInvasionBuildingsConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"NewForeignInvasionBuildingsConfig");

for(Map e:ret)
{
put(e);
}

}

public static NewForeignInvasionBuildingsConfigObject getByBuildingid(int buildingid){

return _ix_buildingid.get(buildingid);

}



public  void putToMem(Map e, NewForeignInvasionBuildingsConfigObject config){

config.setBuildingid(MapHelper.getInt(e, "buildingId"));

config.setDefaultmonsterwave(MapHelper.getInt(e, "defaultMonsterWave"));

config.setFreerewards(MapHelper.getIntArray(e, "freeRewards"));

config.setFightmake(MapHelper.getInt(e, "fightMake"));

config.setWavechangesneedcount(MapHelper.getInt(e, "waveChangesNeedCount"));

config.setRiseratio(MapHelper.getInt(e, "riseRatio"));

config.setLowerratio(MapHelper.getInt(e, "lowerRatio"));

config.setWavelowerlimit(MapHelper.getInt(e, "waveLowerLimit"));

config.setFreemailtemplate(MapHelper.getInt(e, "freeMailTemplate"));

config.setBuildingname(MapHelper.getInt(e, "buildingName"));


_ix_buildingid.put(config.getBuildingid(),config);



}
}
