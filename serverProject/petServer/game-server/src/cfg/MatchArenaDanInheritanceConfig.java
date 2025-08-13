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

@annationInit(value ="MatchArenaDanInheritanceConfig", methodname = "initConfig")
public class MatchArenaDanInheritanceConfig extends baseConfig<MatchArenaDanInheritanceConfigObject>{


private static MatchArenaDanInheritanceConfig instance = null;

public static MatchArenaDanInheritanceConfig getInstance() {

if (instance == null)
instance = new MatchArenaDanInheritanceConfig();
return instance;

}


public static Map<Integer, MatchArenaDanInheritanceConfigObject> _ix_curdan = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MatchArenaDanInheritanceConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MatchArenaDanInheritanceConfig");

for(Map e:ret)
{
put(e);
}

}

public static MatchArenaDanInheritanceConfigObject getByCurdan(int curdan){

return _ix_curdan.get(curdan);

}



public  void putToMem(Map e, MatchArenaDanInheritanceConfigObject config){

config.setCurdan(MapHelper.getInt(e, "curDan"));

config.setNewscore(MapHelper.getInt(e, "newScore"));


_ix_curdan.put(config.getCurdan(),config);



}
}
