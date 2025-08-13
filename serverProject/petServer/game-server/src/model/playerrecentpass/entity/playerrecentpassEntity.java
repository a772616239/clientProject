/**
 * created by tool DAOGenerate
 */
package model.playerrecentpass.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import model.obj.BaseObj;
import model.playerrecentpass.dbCache.playerrecentpassCache;
import protocol.Common.EnumFunction;
import protocol.RecentPassedDB.DB_PlayerRecentPass;
import protocol.RecentPassedDB.DB_RecentPlayerInfo;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class playerrecentpassEntity extends BaseObj {

    public String getClassType() {
        return "playerrecentpassEntity";
    }

    @Override
    public void putToCache() {
        playerrecentpassCache.put(this);
    }

    @Override
    public void transformDBData() {
        if (this.dbBuilder != null) {
            this.data = this.dbBuilder.build().toByteArray();
        }
    }

    /**
     *
     */
    private String playeridx;

    /**
     *
     */
    private byte[] data;


    /**
     * 获得
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 设置
     */
    public void setData(byte[] data) {
        this.data = data;
    }


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return playeridx;
    }

    private DB_PlayerRecentPass.Builder dbBuilder;

    public DB_PlayerRecentPass.Builder getDbBuilder() {
        if (this.dbBuilder == null && this.data != null) {
            try {
                this.dbBuilder = DB_PlayerRecentPass.parseFrom(this.data).toBuilder();
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }

        if (this.dbBuilder == null) {
            this.dbBuilder = DB_PlayerRecentPass.newBuilder();
        }
        return this.dbBuilder;
    }

    public playerrecentpassEntity(){}

    public playerrecentpassEntity(String playerIdx) {
        this.playeridx = playerIdx;
    }

    public void updateRecentPassTeam(EnumFunction function, DB_RecentPlayerInfo recentPlayerInfo) {
        if (function == null || recentPlayerInfo == null) {
            return;
        }

        getDbBuilder().putRecentPass(function.getNumber(), recentPlayerInfo);
    }

    public DB_RecentPlayerInfo getRecentPlayerTeam(EnumFunction function) {
        if (function == null) {
            return null;
        }
        return getDbBuilder().getRecentPassMap().get(function.getNumber());
    }

}