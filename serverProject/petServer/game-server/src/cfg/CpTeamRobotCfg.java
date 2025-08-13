/*CREATED BY TOOL*/

package cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;
import model.cp.entity.CpTeamMember;
import protocol.Battle;
import protocol.Common;
import protocol.CpFunction;
import protocol.CpFunction.CPTeamPlayer;
import protocol.CpFunction.CpFriendPlayer;
import server.handler.cp.CpFunctionUtil;
import util.RandomUtil;

@annationInit(value = "CpTeamRobotCfg", methodname = "initConfig")
public class CpTeamRobotCfg extends baseConfig<CpTeamRobotCfgObject> {


    private static CpTeamRobotCfg instance = null;

    public static CpTeamRobotCfg getInstance() {

        if (instance == null)
            instance = new CpTeamRobotCfg();
        return instance;

    }


    public static Map<Integer, CpTeamRobotCfgObject> _ix_id = new HashMap<>();

    public static CpTeamMember findRobotInfo(int robotId) {
        return robotInfo.get(robotId);
    }


    public void initConfig(baseConfig o) {
        if (instance == null)
            instance = (CpTeamRobotCfg) o;
        initConfig();

    }

    private static Map<Integer, CpFunction.CpFriendPlayer> robotsMap = new HashMap<>();

    private static Map<Integer, CpTeamMember> robotInfo = new HashMap<>();

    private static List<Integer> robotIds = new LinkedList<>();

    public void initRobot() {
        for (CpTeamRobotCfgObject cfg : _ix_id.values()) {
            if (cfg.getId() <= 0) {
                continue;
            }
            CpFunction.CpFriendPlayer.Builder builder = CpFunction.CpFriendPlayer.newBuilder();
            builder.setPlayerName(getRobotName(cfg));
            builder.setPlayerIdx(CpFunctionUtil.getRobotId(cfg.getId()));
            builder.setBorderId(cfg.getHeadbroder());
            builder.setHeadId(cfg.getHead());
            builder.setRobot(true);
            robotsMap.put(cfg.getId(), builder.build());
            robotIds.add(cfg.getId());
        }
        for (CpTeamRobotCfgObject cfg : _ix_id.values()) {
            if (cfg.getId() <= 0) {
                continue;
            }
            CpTeamMember member = new CpTeamMember();
            member.setPlayerName(getRobotName(cfg));
            member.setPlayerIdx(CpFunctionUtil.getRobotId(cfg.getId()));
            member.setAvatarBorder(cfg.getHeadbroder());
            member.setHeader(cfg.getHead());
            member.setTitleId(cfg.getTitileid());
            member.setCurEquipNewTitleId(cfg.getNewtitileid());
            member.setAbility(cfg.getTeamability());
            member.setVipLv(cfg.getViplv());
            member.setServerIndex(cfg.getServerindex());
            member.setShortId(cfg.getShortid());
            for (int[] ints : cfg.getTeam()) {
                if (ints.length<3){
                    continue;
                }
                Battle.BattlePetData.Builder pet = Battle.BattlePetData.newBuilder();
                pet.setPetCfgId(ints[0]);
                pet.setPetRarity(ints[1]);
                pet.setPetLevel(ints[2]);
                member.addPetData(pet.build());
            }
            member.setServerIndex(1);
            robotInfo.put(cfg.getId(), member);
        }

    }

    private String getRobotName(CpTeamRobotCfgObject cfg) {
        return ServerStringRes.getContentByLanguage(cfg.getName(), Common.LanguageEnum.forNumber(ServerConfig.getInstance().getLanguage()));
    }

    public CpFunction.CpFriendPlayer getRobotById(int robotId) {
        return robotsMap.get(robotId);
    }

    public Collection<CpFriendPlayer> randomRobot(int num) {
        if (num >= robotsMap.size()) {
            return robotsMap.values();
        }
        List<CpFunction.CpFriendPlayer> result = new ArrayList<>();

        List<Integer> robotIdx = RandomUtil.batchRandomFromList(robotIds, num, false);

        for (Integer idx : robotIdx) {
            result.add(robotsMap.get(idx));
        }
        return result;

    }

    public List<CpFunction.CpFriendPlayer> getAllRobot() {
        return new ArrayList<>(robotsMap.values());

    }


    private void initConfig() {
        List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CpTeamRobotCfg");

        for (Map e : ret) {
            put(e);
        }

    }

    public static CpTeamRobotCfgObject getById(int id) {

        return _ix_id.get(id);

    }


    public void putToMem(Map e, CpTeamRobotCfgObject config) {

        config.setId(MapHelper.getInt(e, "id"));

        config.setTeam(MapHelper.getIntArray(e, "team"));

        config.setTeamability(MapHelper.getLong(e, "teamAbility"));

        config.setShortid(MapHelper.getInt(e, "shortId"));

        config.setServerindex(MapHelper.getInt(e, "serverIndex"));

        config.setTitileid(MapHelper.getInt(e, "titileId"));

        config.setNewtitileid(MapHelper.getInt(e, "newTitileId"));

        config.setViplv(MapHelper.getInt(e, "vipLv"));

        config.setName(MapHelper.getInt(e, "name"));

        config.setHead(MapHelper.getInt(e, "head"));

        config.setHeadbroder(MapHelper.getInt(e, "headBroder"));


        _ix_id.put(config.getId(), config);


    }

    public CPTeamPlayer.Builder getCpTeamByRobotId(int robotId) {
        CpTeamRobotCfgObject cfg = getById(robotId);
        if (cfg == null) {
            return CpFunction.CPTeamPlayer.getDefaultInstance().toBuilder();
        }
        CpFunction.CPTeamPlayer.Builder robot = CpFunction.CPTeamPlayer.newBuilder();
        robot.setPlayerName(getRobotName(cfg));
        robot.setPlayerIdx(CpFunctionUtil.getRobotId(cfg.getId()));
        robot.setBorderId(cfg.getHeadbroder());
        robot.setHeader(cfg.getHead());
        return robot;
    }
}
