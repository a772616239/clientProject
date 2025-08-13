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

@annationInit(value ="BadWord", methodname = "initConfig")
public class BadWord extends baseConfig<BadWordObject>{


private static BadWord instance = null;

public static BadWord getInstance() {

if (instance == null)
instance = new BadWord();
return instance;

}


public static Map<String, BadWordObject> _ix_badword = new HashMap<String, BadWordObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (BadWord) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "BadWord");

for(Map e:ret)
{
put(e);
}

}

public static BadWordObject getByBadword(String badword){

return _ix_badword.get(badword);

}



public  void putToMem(Map e, BadWordObject config){

config.setBadword(MapHelper.getStr(e, "badWord"));


_ix_badword.put(config.getBadword(),config);



}
}
