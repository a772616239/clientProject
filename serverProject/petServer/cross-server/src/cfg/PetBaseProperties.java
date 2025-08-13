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

@annationInit(value ="PetBaseProperties", methodname = "initConfig")
public class PetBaseProperties extends baseConfig<PetBasePropertiesObject>{


private static PetBaseProperties instance = null;

public static PetBaseProperties getInstance() {

if (instance == null)
instance = new PetBaseProperties();
return instance;

}


public static Map<Integer, PetBasePropertiesObject> _ix_petid = new HashMap<>();

public static Map<Integer, PetBasePropertiesObject> _ix_petdebrisid = new HashMap<>();


public void initConfig(baseConfig o){
if (instance == null)
instance = (PetBaseProperties) o;
initConfig();
}


private void initConfig() {
List<Map> ret=readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(),"PetBaseProperties");

for(Map e:ret)
{
put(e);
}

}

public static PetBasePropertiesObject getByPetid(int petid){

return _ix_petid.get(petid);

}


public static PetBasePropertiesObject getByPetdebrisid(int petdebrisid){

return _ix_petdebrisid.get(petdebrisid);

}



public  void putToMem(Map e, PetBasePropertiesObject config){

config.setPetid(MapHelper.getInt(e, "petId"));

config.setMingzi(MapHelper.getStr(e, "mingzi"));

config.setPetcore(MapHelper.getInt(e, "petCore"));

config.setPetname(MapHelper.getInt(e, "petName"));

config.setShowmap(MapHelper.getInt(e, "showMap"));

config.setStartrarity(MapHelper.getInt(e, "startRarity"));

config.setMaxrarity(MapHelper.getInt(e, "maxRarity"));

config.setPetclass(MapHelper.getInt(e, "petClass"));

config.setPettype(MapHelper.getInt(e, "petType"));

config.setPettag(MapHelper.getInts(e, "petTag"));

config.setMaxuplvl(MapHelper.getInt(e, "maxUpLvl"));

config.setPetproperties(MapHelper.getIntArray(e, "petProperties"));

config.setPetextraproperties(MapHelper.getInt(e, "petExtraProperties"));

config.setPetdebrisid(MapHelper.getInt(e, "petDebrisId"));

config.setUnlockhead(MapHelper.getInt(e, "UnlockHead"));

config.setPetfinished(MapHelper.getInt(e, "petfinished"));

config.setBraverandom(MapHelper.getBoolean(e, "braveRandom"));

config.setIsoptional(MapHelper.getBoolean(e, "IsOptional"));

config.setPropertymodel(MapHelper.getInt(e, "PropertyModel"));


_ix_petid.put(config.getPetid(),config);

_ix_petdebrisid.put(config.getPetdebrisid(),config);



}
}
