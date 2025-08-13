/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;

@annationInit(value ="MistItem", methodname = "initConfig")
public class MistItem extends baseConfig<MistItemObject>{


private static MistItem instance = null;

public static MistItem getInstance() {

if (instance == null)
instance = new MistItem();
return instance;

}


public static Map<Integer, MistItemObject> _ix_itemid = new HashMap<Integer, MistItemObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistItem) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistItem");

for(Map e:ret)
{
put(e);
}

}

public static MistItemObject getByItemid(int itemid){

return _ix_itemid.get(itemid);

}



public  void putToMem(Map e, MistItemObject config){

config.setItemid(MapHelper.getInt(e, "ItemId"));

config.setItemprice(MapHelper.getInts(e, "ItemPrice"));

config.setIsshopitem(MapHelper.getBoolean(e, "IsShopItem"));


_ix_itemid.put(config.getItemid(),config);



}
}
