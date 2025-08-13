/**
 * created by tool DAOGenerate
 */
package model.gloryroad.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import common.GlobalData;
import model.gloryroad.dbCache.gloryroadCache;
import model.obj.BaseObj;
import protocol.GloryRoad.GloryRoadQuizRecord;
import protocol.GloryRoad.SC_GloryRoadQuizRecordAdd;
import protocol.GloryRoadDB.DB_GloryRoadPlayerInfo;
import protocol.MessageId.MsgIdEnum;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class gloryroadEntity extends BaseObj {

    public String getClassType() {
        return "gloryroadEntity";
    }

    @Override
    public void putToCache() {
        gloryroadCache.put(this);
    }

    @Override
    public void transformDBData() {
        this.data = getDbBuilder().build().toByteArray();
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

    private DB_GloryRoadPlayerInfo.Builder dbBuilder;

    public DB_GloryRoadPlayerInfo.Builder getDbBuilder() {
        if (this.dbBuilder == null && this.data != null) {
            try {
                this.dbBuilder = DB_GloryRoadPlayerInfo.parseFrom(this.data).toBuilder();
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }

        if (this.dbBuilder == null) {
            this.dbBuilder = DB_GloryRoadPlayerInfo.newBuilder();
        }

        return this.dbBuilder;
    }

    public void addQuizRecord(GloryRoadQuizRecord quizRecord) {
        if (quizRecord == null) {
            return;
        }
        getDbBuilder().addRecords(quizRecord);

        if (!GlobalData.getInstance().checkPlayerOnline(getPlayeridx())) {
            return;
        }

        SC_GloryRoadQuizRecordAdd.Builder builder = SC_GloryRoadQuizRecordAdd.newBuilder();
        builder.setRecords(quizRecord);
        GlobalData.getInstance().sendMsg(getPlayeridx(), MsgIdEnum.SC_GloryRoadQuizRecordAdd_VALUE, builder);
    }

    public void clear() {
        int topRank = getDbBuilder().getTopRank();
        getDbBuilder().clear();
        getDbBuilder().setTopRank(topRank);
    }

    public void setTopRank(int newRank) {
        if (getDbBuilder().getTopRank() <= 0 || newRank < getDbBuilder().getTopRank()) {
            getDbBuilder().setTopRank(newRank);
        }
    }
}