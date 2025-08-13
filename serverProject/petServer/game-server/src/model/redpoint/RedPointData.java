package model.redpoint;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import protocol.RedPointIdEnum.RedPointId;

/*
*@author Hammer
*2021年12月14日
*/
public class RedPointData {

	private Map<Integer, RedPointStateEnum> redPointStateMap = new HashMap<>();
	private Set<Integer> cliCtlPointTypeMap = new HashSet<>();

	public RedPointStateEnum setState(int type, RedPointStateEnum state) {
		RedPointStateEnum pre = redPointStateMap.put(type, state);
		return pre == null? RedPointStateEnum.COMMON : pre;
	}

	public void addCliCtlType(Collection<RedPointId> clientCtlPoint) {
		for (RedPointId redPointId : clientCtlPoint) {
			cliCtlPointTypeMap.add(redPointId.getNumber());
		}
	}

	public void clearCliCtlPointMap() {
		cliCtlPointTypeMap.clear();
	}

	public boolean isCliCtl(int type) {
		return cliCtlPointTypeMap.contains(type);
	}

}

