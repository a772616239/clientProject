package model.mistforest.trigger;

import util.LogUtil;

import java.util.ArrayList;

public class TriggerManager {
    private static TriggerManager instance = new TriggerManager();

    public static TriggerManager getInstance() {
        return instance;
    }

    public ArrayList<Trigger> loadTriggerData(String configStr) {
        try {
            ArrayList<Trigger> triggerList = new ArrayList<>();
            String[] triggerArr = configStr.split("\\+");
            for (int i = 0; i < triggerArr.length; i++) {
                if (triggerArr[i] == null || triggerArr[i].length() <= 1) {
                    continue;
                }
                Trigger trigger = new Trigger();
                String[] triggerDataArr = triggerArr[i].split("_");
                if (triggerDataArr[0] != null && triggerDataArr[0].length() > 1) {
                    String strConditionList = triggerDataArr[0].substring(1, triggerDataArr[0].length() - 1);
                    String[] arrConditionList = strConditionList.split("\\|");
                    for (int j = 0; j < arrConditionList.length; j++) {
                        if (arrConditionList[j] == null || arrConditionList[j].length() <= 1) {
                            continue;
                        }
                        String[] arrConditionSubList = arrConditionList[j].split(";");
                        ArrayList<Condition> condSubList = new ArrayList<>();
                        for (int k = 0; k < arrConditionSubList.length; k++) {
                            String strConditionSubList = arrConditionSubList[k].substring(1, arrConditionSubList[k].length() - 1);
                            if (strConditionSubList == null || strConditionSubList.length() <= 1) {
                                continue;
                            }
                            String[] arrCondParams = strConditionSubList.split(",");
                            Condition condition = new Condition();
                            for (int m = 0; m < arrCondParams.length; m++) {
                                condition.condParams.add(Integer.valueOf(arrCondParams[m]));
                            }
                            condSubList.add(condition);
                        }
                        trigger.conditionList.add(condSubList);
                    }
                }

                if (triggerDataArr[1] != null && triggerDataArr[1].length() > 1) {
                    String strEventGroup = triggerDataArr[1].substring(1, triggerDataArr[1].length() - 1);
                    String[] eventGroup = strEventGroup.split(";");
                    for (int x = 0; x < eventGroup.length; x++) {
                        if (eventGroup[x] == null || eventGroup[x].length() <= 1) {
                            continue;
                        }
                        String strEventList = eventGroup[x].substring(1, eventGroup[x].length() - 1);
                        String[] eventList = strEventList.split(",");
                        Command commond = new Command();
                        for (int y = 0; y < eventList.length; y++) {
                            commond.cmdParams.add(Integer.valueOf(eventList[y]));
                        }
                        trigger.commandList.add(commond);
                    }
                }
                triggerList.add(trigger);
            }
            return triggerList;
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }
}
