/*CREATED BY TOOL*/

package cfg;
import JsonTool.readJsonFile;
import annotation.annationInit;
import common.load.ServerConfig;
import datatool.MapHelper;
import model.base.baseConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@annationInit(value ="MailTemplateConfig", methodname = "initConfig")
public class MailTemplateConfig extends baseConfig<MailTemplateConfigObject>{


private static MailTemplateConfig instance = null;

public static MailTemplateConfig getInstance() {

if (instance == null)
instance = new MailTemplateConfig();
return instance;

}


public static Map<Integer, MailTemplateConfigObject> _ix_templateid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (MailTemplateConfig) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"MailTemplateConfig");

for(Map e:ret)
{
put(e);
}

}

public static MailTemplateConfigObject getByTemplateid(int templateid){

return _ix_templateid.get(templateid);

}



public  void putToMem(Map e, MailTemplateConfigObject config){

config.setTemplateid(MapHelper.getInt(e, "templateId"));

config.setMailtype(MapHelper.getInt(e, "mailType"));

config.setSender(MapHelper.getInt(e, "sender"));

config.setTitle_tipids(MapHelper.getInt(e, "title_tipIds"));

config.setBody_tipsid(MapHelper.getInt(e, "body_tipsId"));

config.setAttachment(MapHelper.getIntArray(e, "attachment"));

config.setExpiretime(MapHelper.getInt(e, "expireTime"));


_ix_templateid.put(config.getTemplateid(),config);



}
}
