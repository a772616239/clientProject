package server.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bowlong.third.FastJSON;
import common.GameConst;
import common.GameConst.Ban;
import common.GameConst.ChatRetCode;
import common.HttpRequestUtil;
import common.HttpRequestUtil.PlatFormRetCode;
import common.IdGenerator;
import common.SyncExecuteFunction;
import common.entity.NewChatAuthorityData;
import common.load.ServerConfig;
import common.tick.GlobalTick;
import io.javalin.Javalin;
import io.javalin.event.EventType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import model.activity.ActivityManager;
import model.chatreport.dbCache.chatreportCache;
import model.comment.dbCache.commentCache;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import model.petrune.dbCache.petruneCache;
import model.petrune.entity.petruneEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import model.training.TrainingManager;
import model.training.bean.TrainRankSortInfo;
import model.training.dbCache.trainingCache;
import model.training.entity.trainingEntity;
import platform.PlatformManager;
import platform.PlatformManager.TemplateTypeEnum;
import platform.logs.entity.PlayerCrossArenaInfoListResult;
import platform.logs.statistics.ArtifactStatistics;
import platform.logs.statistics.EndlessSpireStatistics;
import platform.logs.statistics.GemStatistics;
import platform.logs.statistics.PetStatistics;
import platform.logs.statistics.RuneStatistics;
import platform.purchase.PurchaseManager;
import protocol.Common.EnumMarqueeScene;
import protocol.Common.Reward;
import protocol.GameplayDB.DB_MailTemplate;
import protocol.GameplayDB.DB_MailTemplate.Builder;
import protocol.GameplayDB.DB_Marquee;
import protocol.GameplayDB.MarqueeCycle;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Rune;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TrainingDB.TrainDBMap;
import server.http.entity.ChatExtInfoContent;
import server.http.entity.GemInfo;
import server.http.entity.IllegalIpRequestException;
import server.http.entity.NewChatPermission;
import server.http.entity.PetInfo;
import server.http.entity.PlatformCrossArenaInfo;
import server.http.entity.PlatformOwnedWarGridsInfo;
import server.http.entity.PlatformPlayerBaseInfo;
import server.http.entity.PlatformPlayerDetailsInfo;
import server.http.entity.PlatformPurchaseData;
import server.http.entity.PlatformRetCode.MailAddRet;
import server.http.entity.PlatformRetCode.PlatformBaseRet;
import server.http.entity.PlatformRetCode.PlatformChatResult;
import server.http.entity.PlatformRetCode.PlatformCommonResult;
import server.http.entity.PlatformRetCode.RetCode;
import server.http.entity.PlayerInfoBaseListResult;
import server.http.entity.RuneInfo;
import server.http.entity.report.AutoDealCommentCancelBan;
import server.http.entity.report.AutoDealCommentCancelBan.AutoDealCommentCancelBanInstance;
import server.http.entity.report.PlatformReportOperate;
import server.http.entity.report.ReportConst.ReportQueryType;
import server.http.entity.report.ReportQuery;
import server.http.entity.report.ReportResult;
import server.http.entity.report.UnreportedCommentQuery;
import server.http.entity.report.UnreportedCommentQueryResult;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 */
public class HttpServer {
    private static HttpServer instance = new HttpServer();

    public static HttpServer getInstance() {
        if (instance == null) {
            synchronized (HttpServer.class) {
                if (instance == null) {
                    instance = new HttpServer();
                }
            }
        }
        return instance;
    }

    private HttpServer() {
    }


    public void run() {
        Javalin app = Javalin.create();
        app.contextPath("");
        app.port(ServerConfig.getInstance().getHttpPort());
        app.defaultContentType("application/json");
        app.defaultCharacterEncoding("UTF-8");
        app.event(EventType.SERVER_START_FAILED, event -> {
            LogUtil.error("start http server failed");
            System.exit(0);
        });
        app.exception(Exception.class, (exception, ctx) -> LogUtil.printStackTrace(exception));
        openAllowIps(app);
        afterLog(app);
        app.start();


        // 玩家充值
        addPurchase(app);
        addMail(app);
        //跑马灯
        addMarquee(app);
        addPlatformActivity(app);
        addPlatformActivityNotice(app);
        addPlatformChatAuthority(app);
        addBan(app);
        // 通用评论
        addReport(app);
        //玩家相关信息查询
        addPlayerInfo(app);
        addKickOut(app);
        addPlatformStatistics(app);
    }

    private void afterLog(Javalin app) {
        app.after(req -> {
            LogUtil.info("HttpServer,receive path:" + req.path() + ", req:" + req.body()
                    + ", source ip:" + req.ip() + ",result:" + req.resultString());
        });
    }

    /**
     * ip限制
     */

    private void openAllowIps(Javalin app) {
        app.before(req -> {
//            LogUtil.info("HttpServer,receive path:" + req.path() + ", req:" + req.body() + ", source ip:" + req.ip());

            if (!ServerConfig.getInstance().getAllowIps().contains(req.ip())) {
                req.status(403);
                req.result("禁止访问");
                LogUtil.warn("illegal ip request = " + req.ip());

                //抛出异常以结束后续逻辑执行
                throw new IllegalIpRequestException("illegal ip request = " + req.ip() + ", request path:" + req.getMatchedPath$javalin());
            }
        });
    }

    private void addKickOut(Javalin app) {
        //使用平台统一接口定义,fcm:
        app.post(ServerConfig.getInstance().getHttpServerBanKickOut(), req -> {
            JSONArray userIdArray = JSONArray.parseArray(req.body());
            LogUtil.info("Receive kick out msg from platform, info:" + req.body());
            for (Object userId : userIdArray) {
                playerEntity player = playerCache.getInstance().getPlayerByUserId((String) userId);
                if (player != null) {
                    player.kickOut(RetCodeEnum.RCE_KickOut_AntiLimit);
                }
            }
        });
    }

    /**
     * ===================================start 个人信息=======================================
     */

    private void addPlayerInfo(Javalin app) {
        app.post(ServerConfig.getInstance().getHttpServerPlayerBaseInfo(), req -> {
            try {
                LogUtil.debug("httpServer playerInfo_baseList, req param = " + req.body());
                JSONObject jsonObject = JSONObject.parseObject(req.body());

                PlayerInfoBaseListResult result = new PlayerInfoBaseListResult();
                if (!jsonObject.containsKey("userId")
                        || !jsonObject.containsKey("roleId")
                        || !jsonObject.containsKey("name")
                        || !jsonObject.containsKey("shortId")) {
                    req.result(result.toJSONString());
                    return;
                }

                String userId = jsonObject.getString("userId");
                String roleId = jsonObject.getString("roleId");
                String name = jsonObject.getString("name");
                int shortId = jsonObject.getIntValue("shortId");

                playerEntity player = getPlayer(shortId, userId, roleId, name);
                if (player != null) {
                    result.addBaseInfo(new PlatformPlayerBaseInfo(player));
                    req.result(result.toJSONString());
                    LogUtil.debug("server.http.HttpServer.playerInfo_baseList, query buy key word");
                    return;
                }

                int pageNum = jsonObject.containsKey("pageNum") ? jsonObject.getIntValue("pageNum") : 0;
                int pageMaxSize = ServerConfig.getInstance().getPlatformPageMaxSize();

                //模糊查询名字
                List<playerEntity> playerByNameLike = playerCache.getInstance().getPlayerByNameLike(name);
                if (playerByNameLike != null && !playerByNameLike.isEmpty()) {
                    int startIndex = getStartIndex(playerByNameLike.size(), pageNum, pageMaxSize);
                    int endIndex = getEndIndex(playerByNameLike.size(), pageNum, pageMaxSize);
                    for (int i = startIndex; i < endIndex; i++) {
                        result.addBaseInfo(new PlatformPlayerBaseInfo(playerByNameLike.get(i)));
                    }
                    result.setTotalSize(playerByNameLike.size());
                    result.setSuccess(true);
                    req.result(result.toJSONString());
                    LogUtil.debug("server.http.HttpServer.playerInfo_baseList, query buy name like, resultString = " + result.toJSONString());
                    return;
                }

                //当模糊搜索未找到目标玩家时,判断是否需要继续搜索,所有搜索关键字为""时继续搜索
                if (!"".equals((userId + roleId + name).trim())) {
                    result.setSuccess(false);
                    req.result(result.toJSONString());
                    LogUtil.debug("server.http.HttpServer.playerInfo_baseList, key word is empty, return");
                    return;
                }

                //查询所有
                List<String> allPlayerIdx = playerCache.getInstance().getAllPlayerIdx();
                LogUtil.debug("server.http.HttpServer.playerInfo_baseList, allPlayerIdx size = " + allPlayerIdx.size());
                int startIndex = getStartIndex(allPlayerIdx.size(), pageNum, pageMaxSize);
                int endIndex = getEndIndex(allPlayerIdx.size(), pageNum, pageMaxSize);

                for (int i = startIndex; i < endIndex; i++) {
                    result.addBaseInfo(new PlatformPlayerBaseInfo(playerCache.getByIdx(allPlayerIdx.get(i))));
                }

                result.setTotalSize(allPlayerIdx.size());
                req.result(result.toJSONString());
                LogUtil.debug("server.http.HttpServer.playerInfo_baseList, query all player");
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        });

        app.post(ServerConfig.getInstance().getCrossArenaInfoList(), req -> {
            try {
                LogUtil.debug("httpServer crossArenaInfo_list, req param = " + req.body());
                JSONObject jsonObject = JSONObject.parseObject(req.body());

                PlayerCrossArenaInfoListResult result = new PlayerCrossArenaInfoListResult();
                if (!jsonObject.containsKey("userId")
                        || !jsonObject.containsKey("roleId")
                        || !jsonObject.containsKey("name")
                        || !jsonObject.containsKey("shortId")) {
                    req.result(result.toJSONString());
                    return;
                }

                String userId = jsonObject.getString("userId");
                String roleId = jsonObject.getString("roleId");
                String name = jsonObject.getString("name");
                int shortId = jsonObject.getIntValue("shortId");

                playerEntity player = getPlayer(shortId, userId, roleId, name);
                if (player != null) {
                    result.addBaseInfo(new PlatformCrossArenaInfo(player.getIdx()));
                    req.result(result.toJSONString());
                    LogUtil.debug("server.http.HttpServer.crossArenaInfo_list, query buy key word");
                    return;
                }

                int pageNum = jsonObject.containsKey("pageNum") ? jsonObject.getIntValue("pageNum") : 0;
                int pageMaxSize = ServerConfig.getInstance().getPlatformPageMaxSize();

                //模糊查询名字
                List<playerEntity> playerByNameLike = playerCache.getInstance().getPlayerByNameLike(name);
                if (playerByNameLike != null && !playerByNameLike.isEmpty()) {
                    int startIndex = getStartIndex(playerByNameLike.size(), pageNum, pageMaxSize);
                    int endIndex = getEndIndex(playerByNameLike.size(), pageNum, pageMaxSize);
                    for (int i = startIndex; i < endIndex; i++) {
                        result.addBaseInfo(new PlatformCrossArenaInfo(playerByNameLike.get(i).getIdx()));
                    }
                    result.setTotalSize(playerByNameLike.size());
                    result.setSuccess(true);
                    req.result(result.toJSONString());
                    LogUtil.debug("server.http.HttpServer.crossArenaInfo_list, query buy name like, resultString = " + result.toJSONString());
                    return;
                }

                //当模糊搜索未找到目标玩家时,判断是否需要继续搜索,所有搜索关键字为""时继续搜索
                if (!"".equals((userId + roleId + name).trim())) {
                    result.setSuccess(false);
                    req.result(result.toJSONString());
                    LogUtil.debug("server.http.HttpServer.crossArenaInfo_list, key word is empty, return");
                    return;
                }

                //查询所有
                List<String> allPlayerIdx = playerCache.getInstance().getAllPlayerIdx();
                LogUtil.debug("server.http.HttpServer.crossArenaInfo_list, allPlayerIdx size = " + allPlayerIdx.size());
                int startIndex = getStartIndex(allPlayerIdx.size(), pageNum, pageMaxSize);
                int endIndex = getEndIndex(allPlayerIdx.size(), pageNum, pageMaxSize);

                for (int i = startIndex; i < endIndex; i++) {
                    result.addBaseInfo(new PlatformCrossArenaInfo(playerCache.getByIdx(allPlayerIdx.get(i)).getIdx()));
                }

                result.setTotalSize(allPlayerIdx.size());
                req.result(result.toJSONString());
                LogUtil.debug("server.http.HttpServer.crossArenaInfo_list, query all player");
            } catch (Exception e) {
                LogUtil.printStackTrace(e);
            }
        });


        app.post(ServerConfig.getInstance().getHttpServerPlayerDetails(), req -> {
            LogUtil.debug("httpServer playerInfo_details");
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            JSONObject result = new JSONObject();
            if (jsonObject.containsKey("roleId")) {
                String roleId = jsonObject.getString("roleId");
                playerEntity entity = playerCache.getByIdx(roleId);
                if (entity != null) {
                    result.put("success", true);
                    result.put("playerDetails", new PlatformPlayerDetailsInfo(entity));
                    req.result(result.toJSONString());
                    return;
                }
            }
            result.put("success", false);
            req.result(result.toJSONString());
        });

        app.post(ServerConfig.getInstance().getHttpServerPlayerGem(), req -> {
            LogUtil.debug("httpServer playerInfo_gem");
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            JSONObject result = new JSONObject();
            if (jsonObject.containsKey("roleId")) {
                String roleId = jsonObject.getString("roleId");
                petgemEntity runeByPlayer = petgemCache.getInstance().getEntityByPlayer(roleId);
                if (runeByPlayer != null) {
                    JSONArray gemInfoArray = new JSONArray();
                    if (runeByPlayer.getGemListBuilder() != null) {
                        for (Gem gem : runeByPlayer.getGemListBuilder().getGemsMap().values()) {
                            gemInfoArray.add(new GemInfo(runeByPlayer.getPlayeridx(), gem));
                        }
                    }
                    result.put("success", true);
                    result.put("gemInfo", gemInfoArray);
                    req.result(result.toJSONString());
                    return;
                }
            }
            result.put("success", false);
            req.result(result.toJSONString());
        });


        app.post(ServerConfig.getInstance().getHttpServerPlayerRune(), req -> {
            LogUtil.debug("httpServer playerInfo_rune");
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            JSONObject result = new JSONObject();
            if (jsonObject.containsKey("roleId")) {
                String roleId = jsonObject.getString("roleId");
                petruneEntity runeByPlayer = petruneCache.getInstance().getEntityByPlayer(roleId);
                if (runeByPlayer != null) {
                    JSONArray runeInfoArray = new JSONArray();
                    if (runeByPlayer.getRuneListBuilder() != null) {
                        for (Rune rune : runeByPlayer.getRuneListBuilder().getRuneMap().values()) {
                            runeInfoArray.add(new RuneInfo(roleId, rune));
                        }
                    }
                    result.put("success", true);
                    result.put("runeInfo", runeInfoArray);
                    req.result(result.toJSONString());
                    return;
                }
            }
            result.put("success", false);
            req.result(result.toJSONString());
        });


        app.post(ServerConfig.getInstance().getHttpServerPlayerPet(), req -> {
            LogUtil.debug("httpServer playerInfo_pet");
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            JSONObject result = new JSONObject();
            if (!jsonObject.containsKey("name")
                    || !jsonObject.containsKey("userId")
                    || !jsonObject.containsKey("roleId")
                    || !jsonObject.containsKey("shortId")) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            String name = jsonObject.getString("name");
            String userId = jsonObject.getString("userId");
            String roleId = jsonObject.getString("roleId");
            int shortId = jsonObject.getIntValue("shortId");

            playerEntity player = getPlayer(shortId, userId, roleId, name);
            if (player != null) {
                petEntity temp = petCache.getInstance().getEntityByPlayer(player.getIdx());
                if (temp != null) {
                    Collection<Pet> petList = temp.peekAllPetByUnModify();
                    if (petList != null) {
                        JSONArray petResultList = new JSONArray();
                        for (Pet pet : petList) {
                            petResultList.add(new PetInfo(player.getIdx(), pet, player.getDb_data().getPetAbilityAddition()));
                        }

                        result.put("petInfo", petResultList);
                        result.put("success", true);
                        req.result(result.toJSONString());
                    }
                }
            }
        });

        final String playerIdx = "playerIdx";
        app.post(ServerConfig.getInstance().getPlayerSkipNewBeeGuide(), req -> {
            JSONObject reqJson = JSONObject.parseObject(req.body());
            if (!reqJson.containsKey(playerIdx)) {
                req.result(new PlatformBaseRet(RetCode.failed, "参数错误").toJsonString());
                return;
            }

            playerEntity entity = playerCache.getByIdx(reqJson.getString(playerIdx));
            if (entity == null) {
                req.result(new PlatformBaseRet(RetCode.failed, "目标玩家未找到").toJsonString());
                return;
            }

            SyncExecuteFunction.executeConsumer(entity, e -> {
                //先将玩家踢下线
                entity.kickOut(RetCodeEnum.RCE_KickOut_SkipNewBeeGuide);
                entity.getDb_data().getNewBeeInfoBuilder().clearPlayerNewbeeStep();
                entity.getDb_data().getNewBeeInfoBuilder().addPlayerNewbeeStep(-1);
            });
            req.result(new PlatformBaseRet(RetCode.success).toJsonString());
        });

        app.post(ServerConfig.getInstance().getQueryPlayerOwnedWarGrids(), req -> {
            JSONObject reqJson = JSONObject.parseObject(req.body());
            JSONObject result = new JSONObject();
            if (!reqJson.containsKey("userId")
                    || !reqJson.containsKey("roleId")
                    || !reqJson.containsKey("name")
                    || !reqJson.containsKey("shortId")) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            String userId = reqJson.getString("userId");
            String roleId = reqJson.getString("roleId");
            String name = reqJson.getString("name");
            int shortId = reqJson.getIntValue("shortId");

            playerEntity player = getPlayer(shortId, userId, roleId, name);
            if (player == null) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            result.put("success", true);
            result.put("ownedWarGridsInfo", new PlatformOwnedWarGridsInfo(player));
            req.result(result.toJSONString());
        });

        app.post(ServerConfig.getInstance().getQueryTrainPlayerData(), req -> {
            JSONObject reqJson = JSONObject.parseObject(req.body());
            JSONObject result = new JSONObject();
            if (!reqJson.containsKey("userId")
                    || !reqJson.containsKey("roleId")
                    || !reqJson.containsKey("name")
                    || !reqJson.containsKey("shortId")
                    || !reqJson.containsKey("mapId")) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            String userId = reqJson.getString("userId");
            String roleId = reqJson.getString("roleId");
            String name = reqJson.getString("name");
            int shortId = reqJson.getIntValue("shortId");
            int mapId = reqJson.getIntValue("mapId");

            playerEntity player = getPlayer(shortId, userId, roleId, name);
            if (player == null) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            result.put("name", player.getName());
            trainingEntity trainEntity = trainingCache.getInstance().getCacheByPlayer(player.getIdx());
            if (trainEntity == null) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            int state = 0; // 未开启
            TrainDBMap.Builder tranDb = trainEntity.getTrainMapByMapId(mapId);
            if (trainEntity.getInfoDB().containsEndMap(mapId)) {
                state = 2; // 已结束
            } else if (tranDb != null) {
                state = 1; // 开启中
            }
            result.put("state", state);
            if (state == 0) {
                result.put("success", true);
                req.result(result.toJSONString());
                return;
            }
            int playerRank = -1;
            if (tranDb != null) {
                int maxPointId = 0;
                for (int pointId: tranDb.getCurpathList()) {
                    if (pointId > maxPointId) {
                        maxPointId = pointId;
                    }
                }
                result.put("maxPos", maxPointId);

                if (state == 2) {
                    playerRank = tranDb.getEndRankMC();
                    if (playerRank == 0) {
                        playerRank = -1;
                    }
                } else {
                   playerRank = TrainingManager.getInstance().getPlayerRank(player.getIdx(), mapId);
                }

            }
            result.put("rank", playerRank);

            result.put("success", true);
            req.result(result.toJSONString());
        });

        app.post(ServerConfig.getInstance().getQueryTrainRank(), req -> {
            JSONObject reqJson = JSONObject.parseObject(req.body());
            JSONObject result = new JSONObject();
            if (!reqJson.containsKey("mapId")
                    || !reqJson.containsKey("startIndex")
                    || !reqJson.containsKey("pageSize")) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            int mapId = reqJson.getIntValue("mapId");
            int startIndex = reqJson.getIntValue("startIndex");
            int queryNum = reqJson.getIntValue("pageSize");
            if (startIndex < 1 || queryNum < 0) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            if (queryNum > 50) {
                result.put("success", false);
                req.result(result.toJSONString());
                return;
            }
            int totalSize = TrainingManager.getInstance().getTotalRankSize(mapId);
            result.put("totalSize", totalSize);
            Map<Integer, TrainRankSortInfo> rankMap = TrainingManager.getInstance().getTrainRankData(mapId, startIndex, queryNum);
            if (rankMap != null && !rankMap.isEmpty()) {
                JSONArray rankArray = new JSONArray();
                playerEntity player;
                for (Entry<Integer, TrainRankSortInfo> entry : rankMap.entrySet()) {
                    JSONObject rankObj = new JSONObject();
                    rankObj.put("roleId", entry.getValue().getPlayerId());
                    player = playerCache.getByIdx(entry.getValue().getPlayerId());
                    if (player != null) {
                        rankObj.put("roleName", player.getName());
                    }
                    rankObj.put("rank", entry.getKey());
                    rankArray.add(rankObj);
                }
                result.put("rankData", rankArray);
            }
            result.put("success", true);
            req.result(result.toJSONString());
        });
    }

    private static int getStartIndex(int totalSize, int queryPage, int pageMaxSize) {
        if (queryPage <= 1 || totalSize < pageMaxSize) {
            return 0;
        }
        return Math.min(totalSize, (queryPage - 1) * pageMaxSize);
    }

    private static int getEndIndex(int totalSize, int queryPage, int pageMaxSize) {
        if (queryPage <= 1 || totalSize < pageMaxSize) {
            return Math.min(pageMaxSize, totalSize);
        }

        return Math.min(queryPage * pageMaxSize, totalSize);
    }


    private playerEntity getPlayer(int shortId, String userId, String roleId, String name) {
        playerEntity player = playerCache.getInstance().getPlayerByShortId(shortId);
        if (player == null) {
            player = playerCache.getInstance().getPlayerByUserId(userId);
        }

        if (player == null) {
            player = playerCache.getByIdx(roleId);
        }

        if (player == null) {
            player = playerCache.getInstance().getPlayerByName(name);
        }
        return player;
    }

    /**
     * ===================================End 个人信息=======================================
     */

    private void addPlatformActivityNotice(Javalin app) {
        app.post(ServerConfig.getInstance().getHttpServerActivityNoticeAdd(), req -> {
            PlatformBaseRet platformBaseRet = HttpUtil.parseActivityNoticeAndAdd(req.body());
            req.result(JSONObject.toJSONString(platformBaseRet));
        });

        app.delete(ServerConfig.getInstance().getHttpServerActivityNoticeDelete(), req -> {
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            if (jsonObject.containsKey("noticeId")) {
                ActivityManager.getInstance().deleteActivityNotice(jsonObject.getLongValue("noticeId"));
            }
            req.result(JSONObject.toJSONString(new PlatformBaseRet(RetCode.success)));
        });
    }

    private void addBan(Javalin app) {
        app.post(ServerConfig.getInstance().getHttpServerBan(), req -> {
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            if (!jsonObject.containsKey("type") || !jsonObject.containsKey("endTime")
                    || !jsonObject.containsKey("bannedPlayerList") || !jsonObject.containsKey("text")) {
                req.result(JSONObject.toJSONString(new PlatformBaseRet(RetCode.failed, "参数缺失")));
                return;
            }
            //type  1:封号  2:禁言
            int type = jsonObject.getIntValue("type");
            if (type != Ban.ROLE && type != Ban.CHAT && type != Ban.COMMENT) {
                req.result(JSONObject.toJSONString(new PlatformBaseRet(RetCode.failed, "不支持类型")));
                return;
            }

            long endTime = jsonObject.getLongValue("endTime");
            if (endTime < GlobalTick.getInstance().getCurrentTime()) {
                req.result(JSONObject.toJSONString(new PlatformBaseRet(RetCode.failed, "封禁时间错误")));
                return;
            }

            JSONArray bannedPlayerList = jsonObject.getJSONArray("bannedPlayerList");
            List<playerEntity> entityList = new ArrayList<>();
            List<String> banPlayerList = new ArrayList<>();
            for (Object obj : bannedPlayerList) {
                String name = (String) obj;
                playerEntity player = playerCache.getInstance().getPlayerByName(name);
                if (player == null) {
                    req.result(JSONObject.toJSONString(new PlatformBaseRet(RetCode.failed, "未知玩家 : " + name)));
                    return;
                }
                entityList.add(player);
                banPlayerList.add(player.getIdx());
            }
            String banMsg = jsonObject.getString("text");
            long idNum = PlatformManager.getInstance().addBanMsg(banMsg);
            if (!entityList.isEmpty()) {
                for (playerEntity entity : entityList) {
                    SyncExecuteFunction.executeConsumer(entity, e -> {
                        entity.ban(type, endTime, idNum);
                    });
                }
            }
            req.result(new PlatformCommonResult(RetCode.success).setData(banPlayerList).toJsonString());
        });

        app.post(ServerConfig.getInstance().getHttpServerCancelBan(), req -> {
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            if (!jsonObject.containsKey("type") || !jsonObject.containsKey("playerList")) {
                req.result(JSONObject.toJSONString(new PlatformBaseRet(RetCode.failed, "参数缺失")));
                return;
            }

            //type  1:封号  2:禁言
            int type = jsonObject.getIntValue("type");
            if (type != Ban.ROLE && type != Ban.CHAT && type != Ban.COMMENT) {
                req.result(JSONObject.toJSONString(new PlatformBaseRet(RetCode.failed, "不支持类型")));
                return;
            }

            JSONArray bannedPlayerList = jsonObject.getJSONArray("playerList");
            for (Object obj : bannedPlayerList) {
                playerEntity player = playerCache.getByIdx((String) obj);
                if (player != null) {
                    SyncExecuteFunction.executeConsumer(player, p -> player.cancelBan(type));
                }
            }
            req.result(JSONObject.toJSONString(new PlatformBaseRet(RetCode.success)));
        });
    }

    private void addReport(Javalin app) {
        //查询
        app.post(ServerConfig.getInstance().getHttpServerReportQuery(), req -> {
            ReportQuery query = JSONObject.parseObject(req.body(), ReportQuery.class);
            if (!query.checkParams()) {
                req.result(new PlatformCommonResult(RetCode.failed, "参数错误").toJsonString());
                return;
            }

            ReportResult result = null;
            if (query.getQueryTypeSource().getQueryType() == ReportQueryType.RQT_COMMENT) {
                result = commentCache.getInstance().queryReportComment(query.getQueryTypeSource().getQuerySource(), query.isFirstQuery());
            } else if (query.getQueryTypeSource().getQueryType() == ReportQueryType.RQT_CHAT) {
                result = chatreportCache.getInstance().queryReport(query.isFirstQuery());
            }

            if (result == null) {
                req.result(new PlatformCommonResult(RetCode.failed, "未知错误").toJsonString());
            } else {
                req.result(new PlatformCommonResult(RetCode.success).setData(result).toJsonString());
            }
        });

        //查询处理结果
        app.post(ServerConfig.getInstance().getHttpServerReportOperate(), req -> {
            PlatformReportOperate operate = JSONObject.parseObject(req.body(), PlatformReportOperate.class);
            if (!operate.checkParams()) {
                req.result(new PlatformCommonResult(RetCode.failed, "参数错误").toJsonString());
                return;
            }

            boolean operateResult = false;
            if (operate.getQueryType() == ReportQueryType.RQT_CHAT) {
                operateResult = chatreportCache.getInstance().reportOperate(operate.getIdx(), operate.getOperate());
            } else if (operate.getQueryType() == ReportQueryType.RQT_COMMENT) {
                operateResult = commentCache.getInstance().reportOperate(operate.getIdx(), operate.getOperate());
            }

            if (operateResult) {
                req.result(new PlatformCommonResult(RetCode.success).toJsonString());
            } else {
                req.result(new PlatformCommonResult(RetCode.failed, "未知错误").toJsonString());
            }
        });

        //自动处理解除封禁(此处只处理评论解禁,因为评论需要解除对应模块的评论屏蔽),聊天只有一个解除禁言,无其他操作
        app.post(ServerConfig.getInstance().getHttpServerReportAutoDealCancelBan(), req -> {
            LogUtil.info("server.http.HttpServer.addReport, ReportAutoDealUnshielded, receive req:" + req.body());
            AutoDealCommentCancelBan unshielded = JSONObject.parseObject(req.body(), AutoDealCommentCancelBan.class);
            if (!unshielded.checkParams()) {
                req.result(new PlatformCommonResult(RetCode.failed, "参数错误").toJsonString());
                return;
            }

            for (AutoDealCommentCancelBanInstance banInstance : unshielded.getCancelList()) {
                playerEntity player = playerCache.getByIdx(banInstance.getPlayerIdx());
                if (player == null) {
                    continue;
                }
                //首先解除禁评
                SyncExecuteFunction.executeConsumer(player, p -> player.cancelBan(Ban.COMMENT));
                //解除屏蔽
                commentCache.getInstance().unshielded(banInstance.getPlayerIdx(), banInstance.getQuerySource());
            }

            req.result(new PlatformBaseRet(RetCode.success).toJsonString());
        });

        //评论监控
        app.post(ServerConfig.getInstance().getHttpServerQueryUnReportedComment(), req -> {
            UnreportedCommentQuery unreportedComment = JSONObject.parseObject(req.body(), UnreportedCommentQuery.class);
            if (!unreportedComment.checkParams()) {
                req.result(new PlatformCommonResult(RetCode.failed, "参数错误").toJsonString());
                return;
            }

            //先检查是不是模糊查询, 模糊查询未找到玩家直接返回
            if (!unreportedComment.checkName()) {
                req.result(new PlatformCommonResult(RetCode.failed, "没有目标玩家").toJsonString());
                return;
            }

            UnreportedCommentQueryResult result = commentCache.getInstance().queryUnreportedComment(unreportedComment);
            if (result == null) {
                req.result(new PlatformCommonResult(RetCode.failed, "暂无记录").toJsonString());
            } else {
                req.result(new PlatformCommonResult(RetCode.success).setData(result).toJsonString());
            }
        });
    }

    private void addMarquee(Javalin app) {
        app.post(ServerConfig.getInstance().getHttpServerMarqueeAdd(), req -> {
            JSONObject jsonObject = JSONObject.parseObject(req.body());
//            LogUtil.info("receive new marquee, info =" + jsonObject.toJSONString());
            PlatformBaseRet platformBaseRet = new PlatformBaseRet();
            if (jsonObject.containsKey("noticeId") && jsonObject.containsKey("startTime") && jsonObject.containsKey("endTime")
                    && jsonObject.containsKey("text") && jsonObject.containsKey("cycleTimes") && jsonObject.containsKey("interval")) {
                DB_Marquee.Builder builder = DB_Marquee.newBuilder();
                Long startTime = jsonObject.getLong("startTime");
                builder.setStartTime(startTime);
                builder.setNextSendTime(startTime);
                builder.setEndTime(jsonObject.getLong("endTime"));
                builder.setCycleTimes(jsonObject.getIntValue("cycleTimes"));
                builder.setNoticeId(jsonObject.getIntValue("noticeId"));
                builder.putAllContent(GameUtil.parseStrToLanguageNumContentMap(jsonObject.getString("text")));
                builder.setInterval(jsonObject.getIntValue("interval") * TimeUtil.MS_IN_A_S);

                if (builder.getStartTime() > builder.getEndTime()
                        || GlobalTick.getInstance().getCurrentTime() > builder.getEndTime()) {
                    platformBaseRet.setRetCode(RetCode.failed);
                    platformBaseRet.setMsg("时间设置错误");
                    req.result(JSONObject.toJSONString(platformBaseRet));
                    return;
                }

                if (builder.getCycleTimes() <= 0) {
                    platformBaseRet.setRetCode(RetCode.failed);
                    platformBaseRet.setMsg("单次循环次数需要>=1");
                    req.result(JSONObject.toJSONString(platformBaseRet));
                    return;
                }

                //跑马灯最低限制60s
                if (builder.getInterval() < GameConst.MARQUEE_MIN_INTERVAL_S) {
                    platformBaseRet.setRetCode(RetCode.failed);
                    platformBaseRet.setMsg("循环间隔设置错误");
                    req.result(JSONObject.toJSONString(platformBaseRet));
                    return;
                }

                //循环
                if (jsonObject.containsKey("cycle")) {
                    MarqueeCycle cycle = HttpUtil.parseMarqueeCycle(jsonObject.getJSONObject("cycle"));
                    if (null != cycle) {
                        builder.setCycle(cycle);
                    }
                }

                if (jsonObject.containsKey("scenes")) {
                    JSONArray scenes = jsonObject.getJSONArray("scenes");
                    for (Object scene : scenes) {
                        if (scene instanceof Integer) {
                            builder.addScenesValue((Integer) scene);
                        }
                    }
                } else {
                    //默认处理为主界面
                    builder.addScenes(EnumMarqueeScene.EMS_MainScene);
                }

                if (jsonObject.containsKey("priority")) {
                    builder.setPriorityValue(jsonObject.getIntValue("priority"));
                }

                if (jsonObject.containsKey("duration")) {
                    builder.setDuration(jsonObject.getIntValue("duration"));
                }

                if (!PlatformManager.getInstance().addMarquee(builder)) {
                    platformBaseRet.setRetCode(RetCode.failed);
                    platformBaseRet.setMsg("未知错误");
                } else {
                    platformBaseRet.setRetCode(RetCode.success);
                }
            } else {
                platformBaseRet.setRetCode(RetCode.failed);
                platformBaseRet.setMsg("参数缺失");
            }
            req.result(JSONObject.toJSONString(platformBaseRet));
        });

        app.post(ServerConfig.getInstance().getHttpServerMarqueeDelete(), req -> {
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            PlatformBaseRet platformBaseRet = new PlatformBaseRet();
            if (jsonObject.containsKey("noticeId")) {
                PlatformManager.getInstance().deleteMarquee(jsonObject.getIntValue("noticeId"));
                platformBaseRet.setRetCode(RetCode.success);
            } else {
                platformBaseRet.setRetCode(RetCode.failed);
                platformBaseRet.setMsg("unknown error");
            }
            req.result(JSONObject.toJSONString(platformBaseRet));
        });
    }

    /**
     * 邮件相关服务
     *
     * @param app
     */
    private void addMail(Javalin app) {
        //添加新邮件
        app.post(ServerConfig.getInstance().getHttpServerMailAdd(), req -> {
            LogUtil.debug("receive addMail template request: body =" + req.body());
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            if (!jsonObject.containsKey("startTime") || !jsonObject.containsKey("expireTime")
                    || !jsonObject.containsKey("title") || !jsonObject.containsKey("body")
                    || !jsonObject.containsKey("annex") || !jsonObject.containsKey("userType")) {
                req.result(new MailAddRet(RetCode.failed, "error params").toJsonString());
                return;
            }

            Set<String> targetPlayerSet = new HashSet<>();
            Long startTime = jsonObject.getLong("startTime");
            Long expireTime = jsonObject.getLong("expireTime");

            //1:全服 2：在线 3指定
            //在线需要设置时间段
            //指定需要设置目标玩家
            int userType = jsonObject.getIntValue("userType");
            if (userType == TemplateTypeEnum.TTE_NOW_ALL_PLAYER) {
                //
            } else if (userType == TemplateTypeEnum.TTE_RANGE_ONLINE_PLAYER) {
                if (startTime >= expireTime || expireTime <= GlobalTick.getInstance().getCurrentTime()) {
                    req.result(new MailAddRet(RetCode.failed, "time cfg error").toJsonString());
                    return;
                }
            } else if (userType == TemplateTypeEnum.TTE_NOW_TARGET_PLAYER) {
                //根据名字查询
                if (jsonObject.containsKey("targetPlayerList")) {
                    JSONArray targetPlayerList = jsonObject.getJSONArray("targetPlayerList");
                    for (Object obj : targetPlayerList) {
                        if (!(obj instanceof String)) {
                            continue;
                        }
                        String name = (String) obj;
                        String idxByName = playerCache.getInstance().getIdxByName(name);
                        if (idxByName != null) {
                            targetPlayerSet.add(idxByName);
                        } else {
                            LogUtil.debug("unKnown playerName :" + name);
                            req.result(new MailAddRet(RetCode.failed, "未知玩家名:" + name).toJsonString());
                            return;
                        }
                    }
                }

                //根据ShortId查询
                if (jsonObject.containsKey("targetPlayerId")) {
                    JSONArray targetPlayerIdList = jsonObject.getJSONArray("targetPlayerId");
                    for (Object obj : targetPlayerIdList) {
                        if (!(obj instanceof Integer)) {
                            continue;
                        }
                        int id = (Integer) obj;
                        String idxByShortId = playerCache.getInstance().getPlayerIdxByShortId(id);
                        if (idxByShortId != null) {
                            targetPlayerSet.add(idxByShortId);
                        } else {
                            LogUtil.debug("unKnown playerId :" + id);
                            req.result(new MailAddRet(RetCode.failed, "未知玩家Id:" + id).toJsonString());
                            return;
                        }
                    }
                }

                //根据RoleId查询
                if (jsonObject.containsKey("targetRoleId")) {
                    JSONArray targetPlayerIdList = jsonObject.getJSONArray("targetRoleId");
                    for (Object obj : targetPlayerIdList) {
                        if (!(obj instanceof String)) {
                            continue;
                        }
                        String roleId = (String) obj;
                        if (playerCache.getByIdx(roleId) != null) {
                            targetPlayerSet.add(roleId);
                        } else {
                            LogUtil.debug("unKnown playerRole :" + roleId);
                            req.result(new MailAddRet(RetCode.failed, "未知玩家RoleId:" + roleId).toJsonString());
                            return;
                        }
                    }
                }

                //指定玩家但是未填指定玩家
                if (targetPlayerSet.isEmpty()) {
                    req.result(new MailAddRet(RetCode.failed, "目标玩家为空").toJsonString());
                    return;
                }
            }

            Builder builder = DB_MailTemplate.newBuilder();
            builder.addAllTargetPlayer(targetPlayerSet);
            builder.setTemplateId(IdGenerator.getInstance().generateIdNum());
            builder.setStartTime(startTime);
            builder.setExpireTime(expireTime);
            builder.setType(userType);

            builder.putAllTitle(GameUtil.parseStrToLanguageNumContentMap(jsonObject.getString("title")));
            builder.putAllBody(GameUtil.parseStrToLanguageNumContentMap(jsonObject.getString("body")));
            JSONArray annex = jsonObject.getJSONArray("annex");
            for (Object obj : annex) {
                if (obj instanceof JSONObject) {
                    JSONObject object = (JSONObject) obj;
                    if (!object.containsKey("type") || !object.containsKey("id") || !object.containsKey("count")) {
                        continue;
                    }
                    Reward reward = RewardUtil.parseReward(object.getInteger("type"), object.getInteger("id"),
                            object.getInteger("count"));

                    if (reward != null) {
                        builder.addRewards(reward);
                    } else {
                        //配置附件错误
                        req.result(new MailAddRet(RetCode.failed, "rewards is error type =" + object.getInteger("type") + ", id = "
                                + object.getInteger("id") + "count = " + object.getInteger("count")).toJsonString());
                        return;
                    }
                }
            }

            //非必须字段
            if (jsonObject.containsKey("msgIndex")) {
                builder.setMsgIndex(jsonObject.getLong("msgIndex"));
            }
            if (jsonObject.containsKey("retainDays")) {
                builder.setRetainDays(jsonObject.getInteger("retainDays"));
            }

            if (jsonObject.containsKey("paramList")) {
                for (Object o : jsonObject.getJSONArray("paramList")) {
                    builder.addParams(o.toString());
                }
            }

            if (!PlatformManager.getInstance().addMailTemplate(builder)) {
                req.result(new MailAddRet(RetCode.failed, "未知错误").toJsonString());
            } else {
                req.result(new MailAddRet(RetCode.success, "success", builder.getTemplateId()).toJsonString());
            }
        });

        final String TEMPLATE_ID = "templateId";

        app.post(ServerConfig.getInstance().getHttpServerMailDelete(), req -> {
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            PlatformBaseRet mailDeleteRet = new PlatformBaseRet();
            if (jsonObject.containsKey(TEMPLATE_ID)) {
                PlatformManager.getInstance().deleteTemplate(jsonObject.getLongValue(TEMPLATE_ID));
                mailDeleteRet.setRetCode(RetCode.success);
            } else {
                mailDeleteRet.setRetCode(RetCode.failed);
                mailDeleteRet.setMsg("参数缺失");
            }
            req.result(JSONObject.toJSONString(mailDeleteRet));
        });


        //撤销邮件
        app.post(ServerConfig.getInstance().getHttpServerMailCancel(), req -> {
            JSONObject reqJson = JSONObject.parseObject(req.body());
            if (!reqJson.containsKey(TEMPLATE_ID)) {
                req.result(new PlatformBaseRet(RetCode.failed, "参数错误").toJsonString());
                return;
            }

            long templateId = reqJson.getLongValue(TEMPLATE_ID);
            boolean cancelResult = PlatformManager.getInstance().cancelMailTemplate(templateId);
            if (cancelResult) {
                req.result(new PlatformBaseRet(RetCode.success, "成功").toJsonString());
            } else {
                req.result(new PlatformBaseRet(RetCode.failed, "未知错误").toJsonString());
            }
        });
    }

    private void addPurchase(Javalin app) {
        app.post(ServerConfig.getInstance().getHttpPlatformPurchase(), req -> {
            PlatformPurchaseData data = FastJSON.parseObject(req.body(), PlatformPurchaseData.class);
            LogUtil.info("user purchase:" + FastJSON.format(data));
            if (data == null) {
                LogUtil.error("addPurchase parseObject data is null");
            }
            boolean result = PurchaseManager.getInstance().settlePurchaseByPlatformPurchaseData(data);
            printPurchaseResult(data, result);
            req.result(result ? "success" : "failed");
        });
    }

    private void printPurchaseResult(PlatformPurchaseData data, boolean result) {
        if (result) {
            LogUtil.info("user purchase orderNum:{} success", data.getOrderNo());
        } else {
            LogUtil.error("user purchase orderNum:{} failed", data.getOrderNo());
        }
    }

    private void addPlatformActivity(Javalin app) {
        app.post(ServerConfig.getInstance().getHttpPlatformActivity(), req -> {
            String body = req.body();
            LogUtil.info("receive activity data: " + body);
            if (HttpUtil.parseAndAddActivities(body)) {
                req.result("success");
            } else {
                req.result("failure");
            }
        });

        app.delete(ServerConfig.getInstance().getHttpPlatformActivity(), req -> {
            String id = req.queryParam("activityId");
            if (id != null) {
                ActivityManager.getInstance().removeServerActivity(Long.parseLong(id));
                req.result("success");
            } else {
                req.result("failure");
            }
        });
    }

    private void addPlatformChatAuthority(Javalin app) {
        app.get(ServerConfig.getInstance().getHttpPlatformChat(), req -> {
            String userId = req.queryParam("userId");
            PlatformChatResult result = new PlatformChatResult();
            LogUtil.debug("platform chat authority query:userId = " + userId);
            if (userId != null) {
                playerEntity player = playerCache.getInstance().getPlayerByUserId(userId);
                if (player != null) {
                    LogUtil.debug("platform chat authority query:player = " + player.toString());
                    result.setExtInfo(FastJSON.toJSONString(new ChatExtInfoContent(HttpRequestUtil.getChatMsg(player))));
                    result.setRoleId(player.getBaseIdx());
                    result.setAuthority(player.getCurChatState());
                    result.setMsg(player.getChatStateMsg());
                } else {
                    result.setAuthority(0);
                }
            } else {
                result.setAuthority(0);
            }
            req.result(FastJSON.format(result));
        });


        final String data = "data";
        final String roleId = "roleId";
        final String userId = "userId";
        app.post(ServerConfig.getInstance().getHttpServerNewChat(), req -> {
            JSONObject jsonObject = JSONObject.parseObject(req.body());
            if (!jsonObject.containsKey(data)) {
                req.result(new NewChatPermission(PlatFormRetCode.ERROR_PARAMS).toJson());
                return;
            }

            JSONObject subObj = JSONObject.parseObject(jsonObject.getString(data));
            if (!subObj.containsKey(roleId) || !subObj.containsKey(userId)) {
                req.result(new NewChatPermission(PlatFormRetCode.ERROR_PARAMS).toJson());
                return;
            }

            String playerIdx = subObj.getString(roleId);
            playerEntity entity = playerCache.getByIdx(playerIdx);
            if (entity == null || !Objects.equals(entity.getUserid(), subObj.getString(userId))) {
                req.result(new NewChatPermission(PlatFormRetCode.ERROR_PARAMS, "玩家不存在").toJson());
                return;
            }

            NewChatAuthorityData authorityData = new NewChatAuthorityData();
            authorityData.setAppPermission(entity.getCurChatState() == ChatRetCode.OPEN);
            authorityData.setAppPermissionMsg(entity.getChatStateMsg());

            NewChatPermission permission = new NewChatPermission(PlatFormRetCode.SUCCESS);
            permission.setData(authorityData.toJson());
            req.result(permission.toJson());
        });
    }

    private void addPlatformStatistics(Javalin app) {

        app.post(ServerConfig.getInstance().getArtifactStatistics(), req -> {
            req.result(ArtifactStatistics.getInstance().queryData());
        });

        app.post(ServerConfig.getInstance().getEndlessSpireStatistics(), req -> {
            req.result(EndlessSpireStatistics.getInstance().queryData());
        });

        app.post(ServerConfig.getInstance().getPetGemStatistics(), req -> {
            req.result(GemStatistics.getInstance().queryData());
        });
        app.post(ServerConfig.getInstance().getPetStatistics(), req -> {
            req.result(PetStatistics.getInstance().queryData());
        });
        app.post(ServerConfig.getInstance().getPetRuneStatistics(), req -> {
            req.result(RuneStatistics.getInstance().queryData());
        });
    }
}











