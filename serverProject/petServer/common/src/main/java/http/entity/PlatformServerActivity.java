package http.entity;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author xiao_FL
 * @date 2019/11/25
 */
public class PlatformServerActivity {
    /**
     * 活动id
     */
    private long activityId;

    /**
     * 开始展示时间戳 -1为永久展示
     */
    private long startDisTime;

    /**
     * 展示结束时间戳
     */
    private long overDisTime;

    /**
     * 活动开始时间戳
     */
    private long beginTime;

    /**
     * 活动结束时间戳
     */
    private long endTime;

    /**
     * 活动类型：1通用活动类型 2通用兑换
     */
    private int type;

    /**
     * 活动标题
     */
    private JSONObject title;

    /**
     * 活动描述
     */
    private JSONObject desc;

    /**
     * 活动使用的图片资源
     */
    private JSONObject pictureName;

    /**
     * 活动目标
     */
    private List<PlatformServerSubMission> missions;

    /**
     * 掉落物(兑换活动必须字段)
     */
    private List<PlatformDropInfo> dropInfo;

    /**
     * 兑换活动任务列
     */
    private List<PlatformServerExMission> exMission;

    /**
     * 模板类型（只有通用类型活动会用到,其余活动类型此处应为空）
     */
    private int template;

    public long getActivityId() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }

    public long getStartDisTime() {
        return startDisTime;
    }

    public void setStartDisTime(long startDisTime) {
        this.startDisTime = startDisTime;
    }

    public long getOverDisTime() {
        return overDisTime;
    }

    public void setOverDisTime(long overDisTime) {
        this.overDisTime = overDisTime;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getTitle() {
        return title;
    }

    public void setTitle(JSONObject title) {
        this.title = title;
    }

    public JSONObject getDesc() {
        return desc;
    }

    public void setDesc(JSONObject desc) {
        this.desc = desc;
    }

    public JSONObject getPictureName() {
        return pictureName;
    }

    public void setPictureName(JSONObject pictureName) {
        this.pictureName = pictureName;
    }

    public List<PlatformServerSubMission> getMissions() {
        return missions;
    }

    public void setMissions(List<PlatformServerSubMission> missions) {
        this.missions = missions;
    }

    public List<PlatformDropInfo> getDropInfo() {
        return dropInfo;
    }

    public void setDropInfo(List<PlatformDropInfo> dropInfo) {
        this.dropInfo = dropInfo;
    }

    public List<PlatformServerExMission> getExMission() {
        return exMission;
    }

    public void setExMission(List<PlatformServerExMission> exMission) {
        this.exMission = exMission;
    }

    public int getTemplate() {
        return template;
    }

    public void setTemplate(int template) {
        this.template = template;
    }
}

class PlatformActivityLang {
    private int language;

    private String content;

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

