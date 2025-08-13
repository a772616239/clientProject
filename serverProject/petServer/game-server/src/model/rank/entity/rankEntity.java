/**
 * created by tool DAOGenerate
 */
package model.rank.entity;

import model.obj.BaseObj;
import model.rank.dbCache.rankCache;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class rankEntity extends BaseObj {

    public String getClassType() {
        return "rankEntity";
    }

    @Override
    public void putToCache() {
        rankCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.data = getDb_data().toByteArray();
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private byte[] data;


    /**
     * 鑾峰緱
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 璁剧疆
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 鑾峰緱
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 璁剧疆
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    RankDb db_data;


    public rankEntity(String idx) {
        this.idx = idx;
    }

    public rankEntity() {
        this.data = new RankDb().toByteArray();
    }

    public String getBaseIdx() {
        return idx;
    }


    public RankDb getDb_data() {
        if (db_data == null) {
            this.db_data = getDBRankData();
        }
        return db_data;
    }

    private RankDb getDBRankData() {
        try {
            if (this.data != null) {
                return (RankDb) RankDb.parseFrom(this.data);
            } else {
                return new RankDb();
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }
}