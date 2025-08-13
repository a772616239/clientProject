package server.event.crossarena;

import model.crossarena.CrossArenaTopManager;

public class CrossArenaTopTickEvent extends CrossArenaEventAbstractCommand {

    private String groupId;

    @Override
    public void doAction() {
        CrossArenaTopManager.getInstance().onTick(groupId);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
