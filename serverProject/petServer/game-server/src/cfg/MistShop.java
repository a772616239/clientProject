/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.base.baseConfig;

@annationInit(value ="MistShop", methodname = "initConfig")
public class MistShop extends baseConfig<MistShopObject>{


private static MistShop instance = null;

public static MistShop getInstance() {

if (instance == null)
instance = new MistShop();
return instance;

}


public static Map<Integer, MistShopObject> _ix_id = new HashMap<Integer, MistShopObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistShop) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "MistShop");

for(Map e:ret)
{
put(e);
}

}

public static MistShopObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistShopObject config){

config.setId(MapHelper.getInt(e, "ID"));

config.setGoodslist(MapHelper.getIntArray(e, "GoodsList"));


_ix_id.put(config.getId(),config);


}
}
