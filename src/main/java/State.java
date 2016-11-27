import model.LaneType;

/**
 * Created by SHaaD on 13.11.2016.
 */
//todo: remove this singleton
class State {
    private static BehaviourType behaviour = BehaviourType.MOVING;

    private static int currentPointIndex = 0;

    private static int lastRuneIndex = 0;

    private static LaneType laneType;

    private static boolean cuttingTree;

    static BehaviourType getBehaviour() {
        return behaviour;
    }

    static void setBehaviour(BehaviourType behaviour) {
        State.behaviour = behaviour;
    }

    static int getCurrentPointIndex() {
        return currentPointIndex;
    }

    static void setCurrentPointIndex(int currentPointIndex) {
        State.currentPointIndex = currentPointIndex;
    }

    static void reduceCurrentPointIndex(int reduceCount) {
        State.currentPointIndex -= reduceCount;
    }

    static void reduceCurrentPointIndex() {
        State.reduceCurrentPointIndex(1);
    }

    static void increaseCurrentPointIndex() {
        State.increaseCurrentPointIndex(1);
    }

    static void increaseCurrentPointIndex(int count) {
        State.currentPointIndex += count;
    }

    static int getLastRuneIndex() {
        return lastRuneIndex;
    }

    static void increaseLastRuneIndex() {
        lastRuneIndex++;
    }

    static LaneType getLane() {
        return laneType;
    }

    static void setLaneType(LaneType laneType) {
        State.laneType = laneType;
    }

    public static boolean isCuttingTree() {
        return cuttingTree;
    }

    public static void setCuttingTree(boolean cuttingTree) {
        State.cuttingTree = cuttingTree;
    }

    enum BehaviourType {
        NONE,
        DEAD,
        MOVING,
        FIGHTING,
        ESCAPING,
        RETREATING,
        GOING_FOR_RUNE
    }
}
