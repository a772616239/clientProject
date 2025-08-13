/**
 * created by tool DAOGenerate
 */
package model.petfragment.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import common.GlobalData;
import common.IdGenerator;
import model.obj.BaseObj;
import protocol.PetDB.SerializablePetFragment;
import protocol.PetMessage.PetFragment;
import protocol.PetMessage.SC_PetFragmetUpdate;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;

import java.util.ArrayList;
import java.util.List;

import static protocol.MessageId.MsgIdEnum.SC_PetFragmetUpdate_VALUE;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class petfragmentEntity extends BaseObj {
    public petfragmentEntity() {
    }

    @Override
    public String getClassType() {
        return "petfragmentEntity";
    }

    @Override
    public void putToCache() {
        //TODO
    }

    @Override
    public void transformDBData() {

    }

    /**
     * 主键
     */
    private String idx;

    /**
     * 宠物碎片所属玩家idx
     */
    private String playeridx;

    /**
     * 宠物碎片信息
     */
    private byte[] fragment;

    /**
     * 获得主键
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置主键
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }

    /**
     * 获得宠物碎片所属玩家idx
     */
    public String getPlayeridx() {
        return playeridx;
    }

    /**
     * 设置宠物碎片所属玩家idx
     */
    public void setPlayeridx(String playeridx) {
        this.playeridx = playeridx;
    }

    /**
     * 获得宠物碎片信息
     */
    public byte[] getFragment() {
        return fragment;
    }

    /**
     * 设置宠物碎片信息
     */
    public void setFragment(byte[] fragment) {
        this.fragment = fragment;
    }

    @Override
    public String getBaseIdx() {
        return idx;
    }

    /***************************分割**********************************/
    private List<PetFragment> fragmentList;

    public List<PetFragment> getFragmentList() {
        return fragmentList;
    }

    public void setFragmentList(List<PetFragment> fragmentList) {
        this.fragmentList = fragmentList;
    }

    public void toBuilder() throws InvalidProtocolBufferException {
        if (fragment != null) {
            fragmentList = SerializablePetFragment.parseFrom(fragment).toBuilder().getFragmentList();
        }
    }

    public void refresh() {
        if (fragmentList != null) {
            SerializablePetFragment.Builder serializableRune = SerializablePetFragment.newBuilder();
            serializableRune.addAllFragment(fragmentList);
            fragment = serializableRune.build().toByteArray();
        }
    }

    public petfragmentEntity(String initPlayerId) {
        idx = IdGenerator.getInstance().generateId();
        playeridx = initPlayerId;
        fragmentList = new ArrayList<>();
    }

    /**
     * 消息：推送宠物碎片数量
     *
     * @param playerId     玩家id
     * @param fragmentList 碎片信息
     */
    public static void sendFragmentAmount(String playerId, List<PetFragment> fragmentList) {
        SC_PetFragmetUpdate.Builder result = SC_PetFragmetUpdate.newBuilder();
        RetCode.Builder retCode = RetCode.newBuilder();
        retCode.setRetCode(RetCodeEnum.RCE_Success);
        result.setResult(retCode);
        result.addAllFragment(fragmentList);
        GlobalData.getInstance().sendMsg(playerId, SC_PetFragmetUpdate_VALUE, result);
    }
}