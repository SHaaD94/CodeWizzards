/**
 * Created by SHaaD on 13.11.2016.
 */
//todo: remove this singlethone
public class State {
    private static BehaviourType behaviour = BehaviourType.MOVING;

    private static int currentPointIndex;

    public static BehaviourType getBehaviour() {
        return behaviour;
    }

    public static void setBehaviour(BehaviourType behaviour) {
        State.behaviour = behaviour;
    }

    public static int getCurrentPointIndex() {
        return currentPointIndex;
    }

    public static void setCurrentPointIndex(int currentPointIndex) {
        State.currentPointIndex = currentPointIndex;
    }

    public static void reduceCurrentPointIndex() {
        State.currentPointIndex--;
    }

    public static void increaseCurrentPointIndex() {
        State.currentPointIndex++;
    }

    public enum BehaviourType {
        DEAD,
        MOVING,
        FIGHTING,
        ESCAPING,
        GOING_FOR_RUNE
    }
}
