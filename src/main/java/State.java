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

    static void reduceCurrentPointIndex() {
        State.currentPointIndex--;
    }

    static void increaseCurrentPointIndex() {
        State.currentPointIndex++;
    }

    static int getLastRuneIndex() {
        return lastRuneIndex;
    }

    static void increaseLastRuneIndex() {
        lastRuneIndex++;
    }

    static LaneType getLaneType() {
        return laneType;
    }

    static void setLaneType(LaneType laneType) {
        State.laneType = laneType;
    }

    enum BehaviourType {
        NONE,
        DEAD,
        MOVING,
        FIGHTING,
        ESCAPING,
        GOING_FOR_RUNE
    }
}
