/**
 * Created by SHaaD on 13.11.2016.
 */
public class State {
    private static BehaviourType behaviour = BehaviourType.MOVING;

    public static BehaviourType getBehaviour() {
        return behaviour;
    }

    public static void setBehaviour(BehaviourType behaviour) {
        State.behaviour = behaviour;
    }

    public static enum BehaviourType {
        DEAD,
        MOVING,
        FIGHTING,
        ESCAPING
    }
}
