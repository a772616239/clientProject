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

@annationInit(value ="Marquee", methodname = "initConfig")
public class Marquee extends baseConfig<MarqueeObject>{


private static Marquee instance = null;

public static Marquee getInstance() {

if (instance == null)
instance = new Marquee();
return instance;

}


public static Map<Integer, MarqueeObject> _ix_id = new HashMap<Integer, MarqueeObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (Marquee) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"Marquee");

for(Map e:ret)
{
put(e);
}

}

public static MarqueeObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MarqueeObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setStarttime(MapHelper.getStr(e, "startTime"));

config.setEndtime(MapHelper.getStr(e, "endTime"));

config.setMarqueetemplateid(MapHelper.getInt(e, "marqueeTemplateId"));

config.setInterval(MapHelper.getInt(e, "interval"));

config.setCycletype(MapHelper.getInt(e, "cycleType"));

config.setValidday(MapHelper.getInts(e, "validDay"));

config.setTimescope(MapHelper.getIntArray(e, "timeScope"));


_ix_id.put(config.getId(),config);



}
}
