import model.Game;
import model.Move;
import model.Wizard;
import model.World;

/**
 * Created by SHaaD on 26.11.2016.
 */
public class DeathCheckerModule implements BehaviourModule {
    private final LanePointsHolder lanePointsHolder;

    public DeathCheckerModule(LanePointsHolder lanePointsHolder) {
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        int nearestControlPointIndex =
                Utils.getNearestControlPointIndex(self, lanePointsHolder.getControlPointsForLane(State.getLane()));
        // it means that character was killed
        if (State.getCurrentPointIndex() - nearestControlPointIndex > 4) {
            State.setCurrentPointIndex(nearestControlPointIndex);
            State.setBehaviour(State.BehaviourType.MOVING);
            System.out.println("we are dead");
        }
    }
}
