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

@annationInit(value ="MistPlayerSkillCfg", methodname = "initConfig")
public class MistPlayerSkillCfg extends baseConfig<MistPlayerSkillCfgObject>{


private static MistPlayerSkillCfg instance = null;

public static MistPlayerSkillCfg getInstance() {

if (instance == null)
instance = new MistPlayerSkillCfg();
return instance;

}


public static Map<Integer, MistPlayerSkillCfgObject> _ix_id = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MistPlayerSkillCfg) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MistPlayerSkillCfg");

for(Map e:ret)
{
put(e);
}

}

public static MistPlayerSkillCfgObject getById(int id){

return _ix_id.get(id);

}



public  void putToMem(Map e, MistPlayerSkillCfgObject config){

config.setId(MapHelper.getInt(e, "ID"));

config.setType(MapHelper.getInt(e, "Type"));

config.setMaxstack(MapHelper.getInt(e, "maxStack"));

config.setCooldown(MapHelper.getInt(e, "coolDown"));


_ix_id.put(config.getId(),config);



}
}
