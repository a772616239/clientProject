/*CREATED BY TOOL*/

package cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import model.base.baseConfig;
import datatool.MapHelper;
import annotation.annationInit;
import common.load.ServerConfig;
import JsonTool.readJsonFile;

@annationInit(value = "CrossArenaHonor", methodname = "initConfig")
public class CrossArenaHonor extends baseConfig<CrossArenaHonorObject> {

	private static CrossArenaHonor instance = null;

	public static CrossArenaHonor getInstance() {

		if (instance == null)
			instance = new CrossArenaHonor();
		return instance;

	}

	public static Map<Integer, CrossArenaHonorObject> _ix_id = new HashMap<>();

	public void initConfig(baseConfig o) {
		if (instance == null)
			instance = (CrossArenaHonor) o;
		initConfig();
	}

	private void initConfig() {
		List<Map> ret = readJsonFile.getMaps(ServerConfig.getInstance().getJsonPath(), "CrossArenaHonor");

		for (Map e : ret) {
			put(e);
		}

	}

	public static CrossArenaHonorObject getById(int id) {

		return _ix_id.get(id);

	}

	public void putToMem(Map e, CrossArenaHonorObject config) {

		config.setId(MapHelper.getInt(e, "id"));

		config.setBigtype(MapHelper.getInt(e, "bigType"));

		config.setParm(MapHelper.getInt(e, "parm"));

		config.setAward(MapHelper.getIntArray(e, "award"));

		config.setMissiontype(MapHelper.getInt(e, "missionType"));

		_ix_id.put(config.getId(), config);

	}
}
