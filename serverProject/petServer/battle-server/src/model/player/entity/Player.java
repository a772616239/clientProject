/**
 * created by tool DAOGenerate
 */
package model.player.entity;

import com.google.protobuf.GeneratedMessageV3.Builder;
import datatool.StringHelper;
import model.obj.BaseObj;
import model.warpServer.WarpServerManager;
import protocol.Battle.BattlePetData;
import protocol.Battle.CS_BattleResult;
import protocol.Battle.ExtendProperty;
import protocol.Battle.PlayerBaseInfo;
import protocol.Battle.SkillBattleDict;
import protocol.MessageId.MsgIdEnum;
import protocol.ServerTransfer.BS_GS_TransferBattleMsg;
import protocol.ServerTransfer.PvpBattlePlayerInfo;
import util.LogUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * created by tool
 */
@SuppressWarnings("serial")
public class Player extends BaseObj {


    public String getClassType() {
        return "Player";
    }

    /**
     *
     */
    private String idx;

    /**
     *
     */
    private String userid;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private int avatar;

    /**
     *
     */
    private int avatarBorder;

    /**
     *
     */
    private int avatarBorderRank;

    /**
     *
     */
    private int level;

    /**
     *
     */
    private int experience;

    /**
     *
     */
    private int vip;

    /**
     *
     */
    private int vipexperience;

    private boolean isRobot = false;

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
    public String getUserid() {
        return userid;
    }

    /**
     * 设置
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * 获得
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获得
     */
    public int getAvatar() {
        return avatar;
    }

    /**
     * 设置
     */
    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    /**
     * 获得
     */
    public int getAvatarBorder() {
        return avatarBorder;
    }

    /**
     * 设置
     */
    public void setAvatarBorder(int avatarBorder) {
        this.avatarBorder = avatarBorder;
    }

    /**
     * 获得
     */
    public int getAvatarBorderRank() {
        return avatarBorderRank;
    }

    /**
     * 设置
     */
    public void setAvatarBorderRank(int avatarBorderRank) {
        this.avatarBorderRank = avatarBorderRank;
    }

    /**
     * 获得
     */
    public int getLevel() {
        return level;
    }

    /**
     * 设置
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 获得
     */
    public int getExperience() {
        return experience;
    }

    /**
     * 设置
     */
    public void setExperience(int experience) {
        this.experience = experience;
    }

    /**
     * 获得
     */
    public int getVip() {
        return vip;
    }

    /**
     * 设置
     */
    public void setVip(int vip) {
        this.vip = vip;
    }

    /**
     * 获得
     */
    public int getVipexperience() {
        return vipexperience;
    }

    /**
     * 设置
     */
    public void setVipexperience(int vipexperience) {
        this.vipexperience = vipexperience;
    }


    public String getBaseIdx() {

        return idx;
    }

    /**
     * ==================================自动生成分割线================================================
     */

    public Player() {
        this.skillList = new ArrayList<>();
        this.petDataList = new ArrayList<>();
        this.extendPropList = new LinkedList<>();
        this.battleResult = CS_BattleResult.newBuilder();
    }

    private boolean online;

    private int fromServerIndex;
    private int camp;
    private String roomId;

    private boolean readyBattle;
    private int battlePower; // 战斗力

    private CS_BattleResult.Builder battleResult; // 用于判断是否收到该玩家结算消息

    private List<SkillBattleDict> skillList;
    private List<BattlePetData> petDataList;
    private List<ExtendProperty> extendPropList;

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getFromServerIndex() {
        return fromServerIndex;
    }

    public void setFromServerIndex(int fromServerIndex) {
        this.fromServerIndex = fromServerIndex;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isReadyBattle() {
        return readyBattle;
    }

    public void setReadyBattle(boolean readyBattle) {
        this.readyBattle = readyBattle;
    }

    public int getBattlePower() {
        return battlePower;
    }

    public void setBattlePower(int battlePower) {
        this.battlePower = battlePower;
    }

    public CS_BattleResult.Builder getBattleResult() {
        return battleResult;
    }

    public void mergeBattleResult(CS_BattleResult battleResult) {
        if (battleResult == null) {
            return;
        }
        this.battleResult.mergeFrom(battleResult);
    }

    public List<SkillBattleDict> getSkillList() {
        return skillList;
    }

    public void addSkillList(List<SkillBattleDict> skillList) {
        this.skillList.addAll(skillList);
    }

    public List<BattlePetData> getPetDataList() {
        return petDataList;
    }

    public void addPetDataList(List<BattlePetData> petDataList) {
        this.petDataList.addAll(petDataList);
    }

    public List<ExtendProperty> getExtendPropList() {
        return extendPropList;
    }

    public void addExtendPorp(ExtendProperty extendProp) {
        this.extendPropList.add(extendProp);
    }

    public void clear() {
        setName(null);
        setAvatar(0);
        setAvatarBorder(0);
        setLevel(0);
        setFromServerIndex(0);
        setCamp(0);
        setRoomId(null);
        setReadyBattle(false);
        setBattlePower(0);
        setOnline(false);
        skillList.clear();
        petDataList.clear();
        extendPropList.clear();
        battleResult.clear();
    }

    public void onPlayerLogin(PvpBattlePlayerInfo playerInfo, String roomId, boolean isResume) {
        LogUtil.info("player login id=" + getIdx() + ",fromServerIndex=" + playerInfo.getFromSvrIndex());
        // TODO login logic
        setName(playerInfo.getPlayerInfo().getPlayerName());
        setAvatar(playerInfo.getPlayerInfo().getAvatar());
        setAvatarBorder(playerInfo.getPlayerInfo().getAvatarBorder());
        setAvatarBorderRank(playerInfo.getPlayerInfo().getAvatarBorderRank());
        setLevel(playerInfo.getPlayerInfo().getLevel());
        setFromServerIndex(playerInfo.getFromSvrIndex());
        setCamp(playerInfo.getCamp());
        skillList.addAll(playerInfo.getPlayerSkillIdListList());
        petDataList.addAll(playerInfo.getPetListList());
        extendPropList.addAll(playerInfo.getExtendPropList());

        setRoomId(roomId);

        setOnline(true);
    }

    public void onPlayerLogout() {
        LogUtil.info("player logout id=" + getIdx() + ",userId=" + getUserid());
        // TODO logout logic
        clear();
    }

    public void offline() {
        setOnline(false);
    }

    public PlayerBaseInfo.Builder builderPlayerBaseInfo() {
        PlayerBaseInfo.Builder builder = PlayerBaseInfo.newBuilder();
        builder.setPlayerId(getIdx());
        if (getName() != null) {
            builder.setPlayerName(getName());
        }
        builder.setLevel(getLevel());
        builder.setAvatar(getAvatar());
        builder.setAvatarBorder(getAvatarBorder());
        builder.setAvatarBorderRank(getAvatarBorderRank());
        return builder;
    }

    public boolean sendBattleMsgToServer(int serverType, int msgId, Builder<?> builder) {
        if (isRobot) {
            return true;
        }
        if (fromServerIndex <= 0) {
            return false;
        }
        if (!isOnline()) {
            return false;
        }
        BS_GS_TransferBattleMsg.Builder builder1 = BS_GS_TransferBattleMsg.newBuilder();
        builder1.setPlayerIdx(getIdx());
        builder1.setMsgId(msgId);
        builder1.setMsgData(builder.build().toByteString());
        return WarpServerManager.getInstance().sendMsgToServer(serverType, fromServerIndex, MsgIdEnum.BS_GS_TransferBattleMsg_VALUE, builder1);
    }

    public void onTick(long curTime) {

    }

    public boolean isRobot() {
        return isRobot;
    }

    public void setRobot(boolean robot) {
        isRobot = robot;
    }
}