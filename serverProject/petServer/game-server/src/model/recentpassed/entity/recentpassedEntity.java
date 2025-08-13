/**
 * created by tool DAOGenerate
 */
package model.recentpassed.entity;

import cfg.GameConfig;
import common.GameConst;
import java.util.Objects;
import model.obj.BaseObj;
import model.recentpassed.RecentPassedUtil;
import model.recentpassed.dbCache.recentpassedCache;
import org.apache.commons.lang.StringUtils;
import protocol.Common.EnumFunction;
import protocol.RecentPassedDB.DB_RecentPassed;
import protocol.RecentPassedDB.DB_RecentPlayerInfo;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class recentpassedEntity extends BaseObj {

    public String getClassType() {
        return "recentpassedEntity";
    }

    @Override
    public void putToCache() {
        recentpassedCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.info = this.getDbBuilder().build().toByteArray();
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private byte[] info;


    /**
     * 获得
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得
     */
    public byte[] getInfo() {
        return info;
    }

    /**
     * 设置
     */
    public void setInfo(byte[] info) {
        this.info = info;
    }


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return idx;
    }

    private DB_RecentPassed.Builder dbBuilder;

    public DB_RecentPassed.Builder getDbBuilder() {
        if (dbBuilder == null) {
            try {
                if (this.info != null) {
                    dbBuilder =  DB_RecentPassed.parseFrom(info).toBuilder();
                }
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        }

        if (dbBuilder == null) {
            dbBuilder = DB_RecentPassed.newBuilder();
        }

        return dbBuilder;
    }

    public void addRecentPassed(String playerIdx, EnumFunction function) {
        if (containsSamePlayer(playerIdx) || !RecentPassedUtil.allowFunction(function)) {
            return;
        }

        DB_RecentPlayerInfo.Builder recentInfo = RecentPassedUtil.buildPlayerRecentInfo(playerIdx, function);
        if (recentInfo == null) {
            return;
        }

        if (getDbBuilder().getRecentPlayerInfoCount()
                >= GameConfig.getById(GameConst.CONFIG_ID).getRecentpassedcount()) {
            LogUtil.debug("recentPassed save count > cfg count, remove the first one");
            getDbBuilder().removeRecentPlayerInfo(0);
        }

        getDbBuilder().addRecentPlayerInfo(recentInfo);
    }

    private boolean containsSamePlayer(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return true;
        }

        for (DB_RecentPlayerInfo playerInfo : getDbBuilder().getRecentPlayerInfoList()) {
            if (Objects.equals(playerInfo.getPlayerIdx(), playerIdx)) {
                return true;
            }
        }

        return false;
    }
}