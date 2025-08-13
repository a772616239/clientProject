package model.cp.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import protocol.Battle;

public class CpTeamMember implements Serializable {
    private static final long serialVersionUID = -5527360908709148119L;
    private String playerIdx;
    private String playerName;
    private int header;
    private long ability;
    private int playerLv;
    private List<Battle.BattlePetData> petData;
    private int vipLv;
    private int avatarBorder;
    private int avatarBorderRank;
    private int titleId;
    private List<Integer> newTitleId;
    private int curEquipNewTitleId;
    private int shortId;
    private int serverIndex;
    private int sex;
    private boolean uploadTeam;

    public boolean isUploadTeam() {
        return uploadTeam;
    }

    public void setUploadTeam(boolean uploadTeam) {
        this.uploadTeam = uploadTeam;
    }

    public void addPetData(Battle.BattlePetData pet) {
        if (petData == null) {
            petData = new ArrayList<>();
        }
        petData.add(pet);
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
    }

    public long getAbility() {
        return ability;
    }

    public void setAbility(long ability) {
        this.ability = ability;
    }

    public int getPlayerLv() {
        return playerLv;
    }

    public void setPlayerLv(int playerLv) {
        this.playerLv = playerLv;
    }

    public List<Battle.BattlePetData> getPetData() {
        return petData;
    }

    public void setPetData(List<Battle.BattlePetData> petData) {
        this.petData = petData;
    }

    public int getVipLv() {
        return vipLv;
    }

    public void setVipLv(int vipLv) {
        this.vipLv = vipLv;
    }

    public int getAvatarBorder() {
        return avatarBorder;
    }

    public void setAvatarBorder(int avatarBorder) {
        this.avatarBorder = avatarBorder;
    }

    public int getAvatarBorderRank() {
        return avatarBorderRank;
    }

    public void setAvatarBorderRank(int avatarBorderRank) {
        this.avatarBorderRank = avatarBorderRank;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public List<Integer> getNewTitleId() {
        return newTitleId;
    }

    public void setNewTitleId(List<Integer> newTitleId) {
        this.newTitleId = newTitleId;
    }

    public int getCurEquipNewTitleId() {
        return curEquipNewTitleId;
    }

    public void setCurEquipNewTitleId(int curEquipNewTitleId) {
        this.curEquipNewTitleId = curEquipNewTitleId;
    }

    public int getShortId() {
        return shortId;
    }

    public void setShortId(int shortId) {
        this.shortId = shortId;
    }

    public int getServerIndex() {
        return serverIndex;
    }

    public void setServerIndex(int serverIndex) {
        this.serverIndex = serverIndex;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
