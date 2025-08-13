/**
 * created by tool DAOGenerate
 */
package model.battlerecord.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.Setter;
import model.battlerecord.dbCache.battlerecordCache;
import model.obj.BaseObj;
import protocol.BattleRecordDB.DB_ServerBattleRecord;
import util.LogUtil;

/**
 * created by tool
 */
@SuppressWarnings("serial")
@Getter
@Setter
public class battlerecordEntity extends BaseObj {

    public String getClassType() {
        return "battlerecordEntity";
    }

    @Override
    public void putToCache() {
        battlerecordCache.put(this);
    }

    @Override
    public void transformDBData() {
        if (this.serverBattleRecordBuilder != null) {
            this.data = this.serverBattleRecordBuilder.build().toByteArray();
        }
    }

    private String battleid;

    private String version;

    private byte[] data;


    public String getBaseIdx() {
        // TODO Auto-generated method stub
        return this.battleid;
    }

    private DB_ServerBattleRecord.Builder serverBattleRecordBuilder;

    public DB_ServerBattleRecord.Builder getServerBattleRecordBuilder() {
        if (this.serverBattleRecordBuilder == null && this.data != null) {
            try {
                this.serverBattleRecordBuilder = DB_ServerBattleRecord.parseFrom(this.data).toBuilder();
            } catch (InvalidProtocolBufferException e) {
                LogUtil.printStackTrace(e);
            }
        }

//        if (serverBattleRecordBuilder == null) {
//            this.serverBattleRecordBuilder = DB_ServerBattleRecord.newBuilder();
//        }

        return serverBattleRecordBuilder;
    }

    public void setVersion(String version) {
        this.version = version;
        if (this.version == null) {
            this.version = "";
        }
    }

}