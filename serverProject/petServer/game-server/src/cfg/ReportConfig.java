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

@annationInit(value ="ReportConfig", methodname = "initConfig")
public class ReportConfig extends baseConfig<ReportConfigObject>{


private static ReportConfig instance = null;

public static ReportConfig getInstance() {

if (instance == null)
instance = new ReportConfig();
return instance;

}


public static Map<Integer, ReportConfigObject> _ix_id = new HashMap<Integer, ReportConfigObject>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (ReportConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"ReportConfig");

for(Map e:ret)
{
put(e);
}

}

public static ReportConfigObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, ReportConfigObject config){

config.setId(MapHelper.getInt(e, "id"));

config.setAutodealtype(MapHelper.getInts(e, "autoDealType"));

config.setDealneedtimes(MapHelper.getInt(e, "dealNeedTimes"));

config.setEachdaycanreporttimes(MapHelper.getInt(e, "eachDayCanReportTimes"));

config.setAutodealbandays(MapHelper.getInt(e, "autoDealBanDays"));

config.setBancommenttips(MapHelper.getInt(e, "banCommentTips"));


_ix_id.put(config.getId(),config);



}
}
