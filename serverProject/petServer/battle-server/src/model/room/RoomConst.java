package model.room;

public class RoomConst {
    public static class RoomStateEnum {
        public final static int closed = 0;
        public final static int init = 1;
        public final static int battling = 2;
        public final static int verifyResult = 3;
        public final static int battleEnd = 4;
    }

    public static class RoomStateTime {
        public final static long initTime = 60000;
        public final static long battleTime = 240000;
        public final static long verifyResultTime = 5000;
        public final static long endBattleTime = 3000;
    }
}
