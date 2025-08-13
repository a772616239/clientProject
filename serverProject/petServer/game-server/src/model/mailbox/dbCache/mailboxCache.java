/*CREATED BY TOOL*/

package model.mailbox.dbCache;

import annotation.annationInit;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.mailbox.cache.mailboxUpdateCache;
import model.mailbox.entity.mailboxEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import model.player.util.PlayerUtil;

@annationInit(value = "mailboxCache", methodname = "load")
public class mailboxCache extends baseCache<mailboxCache> implements IbaseCache {

    /******************* MUST HAVE ********************************/

    private static mailboxCache instance = null;

    public static mailboxCache getInstance() {

        if (instance == null) {
            instance = new mailboxCache();
        }
        return instance;

    }


    public String getDaoName() {

        return "mailboxDAO";
    }

    public BaseDAO getDao() {

        return AppContext.getBean("mailboxDAO");
    }

    public void load(baseCache o) {

        if (instance == null) {
            instance = (mailboxCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(mailboxEntity v) {
        getInstance().putBaseEntity(v);
    }

    public static mailboxEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx))
            return null;


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null)
            return null;

        return (mailboxEntity) v;

    }

    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    public baseUpdateCache getUpdateCache() {

        return mailboxUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }


    public void putToMem(BaseEntity v) {

        mailboxEntity t = (mailboxEntity) v;
        String linkPlayerIdx = t.getLinkplayeridx();
        if(linkPlayerIdx != null){
            mailBoxMap.put(linkPlayerIdx, t);
        }

    }

    /*******************MUST HAVE END ********************************/

    private static Map<String, mailboxEntity> mailBoxMap = new ConcurrentHashMap<>();

    /**
     * 通过playerIdx取得mailboxEntity
     * @param playerIdx
     * @return
     */
    public mailboxEntity getMailBoxByPlayerIdx(String playerIdx) {
        if (playerIdx == null) {
            return null;
        }

        mailboxEntity entity = mailBoxMap.get(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new mailboxEntity(playerIdx);
            put(entity);
        }

        return entity;
    }

    public boolean deleteAllPlayerMailByTemplateId(long templateId) {
        for (mailboxEntity entity : mailBoxMap.values()) {
            SyncExecuteFunction.executeConsumer(entity, e -> {
                entity.deleteMailByTemplateId(templateId);
            });
        }
        return true;
    }
}
