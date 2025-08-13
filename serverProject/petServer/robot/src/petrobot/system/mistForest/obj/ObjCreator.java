package petrobot.system.mistForest.obj;

public interface ObjCreator {
    <T extends MistObj> T createObj();
}
