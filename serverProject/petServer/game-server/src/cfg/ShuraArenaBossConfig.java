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

@annationInit(value ="ShuraArenaBossConfig", methodname = "initConfig")
public class ShuraArenaBossConfig extends baseConfig<ShuraArenaBossConfigObject>{


private static ShuraArenaBossConfig instance = null;

public static ShuraArenaBossConfig getInstance() {

if (instance == null)
instance = new ShuraArenaBossConfig();
return instance;

}


public static Map<Integer, ShuraArenaBossConfigObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ShuraArenaBossConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ShuraArenaBossConfig");

for(Map e:ret)
{
put(e);
}

}

public static ShuraArenaBossConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ShuraArenaBossConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setFightmakeid(MapHelper.getInts(e, "fightMakeId"));

config.setDifficult(MapHelper.getInt(e, "difficult"));

config.setArea(MapHelper.getInt(e, "area"));

config.setScoreaddition(MapHelper.getInt(e, "scoreAddition"));

config.setBossbuff(MapHelper.getInt(e, "bossBuff"));

config.setPlayerbuff(MapHelper.getInt(e, "playerBuff"));

config.setPetlvincr(MapHelper.getInt(e, "petlvIncr"));


_ix_id.put(config.getId(),config);



}
}
