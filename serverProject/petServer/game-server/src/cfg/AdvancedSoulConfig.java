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

@annationInit(value ="AdvancedSoulConfig", methodname = "initConfig")
public class AdvancedSoulConfig extends baseConfig<AdvancedSoulConfigObject>{


private static AdvancedSoulConfig instance = null;

public static AdvancedSoulConfig getInstance() {

if (instance == null)
instance = new AdvancedSoulConfig();
return instance;

}


public static Map<Integer, AdvancedSoulConfigObject> _ix_itemid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (AdvancedSoulConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"AdvancedSoulConfig");

for(Map e:ret)
{
put(e);
}

}

public static AdvancedSoulConfigObject getByItemid(int itemid){

return _ix_itemid.get(itemid);

}



public  void putToMem(Map e, AdvancedSoulConfigObject config){

config.setItemid(MapHelper.getInt(e, "itemId"));

config.setRarity(MapHelper.getInt(e, "rarity"));

config.setPetclass(MapHelper.getInt(e, "petClass"));


_ix_itemid.put(config.getItemid(),config);



}
}
