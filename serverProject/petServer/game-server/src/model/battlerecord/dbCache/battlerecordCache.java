/*CREATED BY TOOL*/

package model.battlerecord.dbCache;

import annotation.annationInit;
import daoMaster.battlerecordDAO;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import java.util.Map;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.battlerecord.cache.battlerecordUpdateCache;
import model.battlerecord.entity.battlerecordEntity;
import org.apache.commons.lang.StringUtils;

@annationInit(value = "battlerecordCache", methodname = "load")
public class battlerecordCache extends baseCache<battlerecordCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static battlerecordCache instance = null;

    public static battlerecordCache getInstance() {

        if (instance == null) {
            instance = new battlerecordCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "battlerecordDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("battlerecordDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (battlerecordCache) o;
        }
        //战斗记录不默认从数据库加载全部
//        super.loadAllFromDb();

    }

    public static void put(battlerecordEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static battlerecordEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (battlerecordEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return battlerecordUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        battlerecordEntity t = (battlerecordEntity) v;

    }

    /*******************MUST HAVE END ********************************/

    public battlerecordEntity getEntity(String battleId) {
        battlerecordEntity entity = getByIdx(battleId);
        if (entity == null) {
            entity = ((battlerecordDAO) getDao()).selectByIdx(battleId);

            if (entity != null) {
                this.putBaseEntityToMem(entity);
            }
        }
        return entity;
    }

    /**
     * 判断是否需要记录
     *
     * @param battleId
     * @return
     */
    public boolean needRecord(String battleId) {
        return StringUtils.isNotEmpty(battleId) && getEntity(battleId) == null;
    }
}
