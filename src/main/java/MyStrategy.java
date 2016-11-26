import model.*;

import java.util.ArrayList;
import java.util.List;

public final class MyStrategy implements Strategy {
    private List<BehaviourModule> behaviours = new ArrayList<>();
    private boolean isInit;

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        if (!isInit) {
            State.setLaneType(getLaneType(self));

            LanePointsHolder lanePointsHolder = new LanePointsHolder(game.getMapSize());
            behaviours.add(new DeathCheckerModule(lanePointsHolder));
            behaviours.add(new EscapeModule(lanePointsHolder));
            behaviours.add(new RuneModule(lanePointsHolder));
            behaviours.add(new MovementModule(lanePointsHolder));
            behaviours.add(new AttackModule(lanePointsHolder));
            isInit = true;
        }
        behaviours.forEach(x -> x.updateMove(self, world, game, move));
    }

    private LaneType getLaneType(Wizard self) {
        switch ((int) self.getId()) {
            case 1:
            case 2:
            case 6:
            case 7:
                return LaneType.TOP;
            case 3:
            case 8:
                return LaneType.MIDDLE;
            case 4:
            case 5:
            case 9:
            case 10:
                return LaneType.BOTTOM;
            default:
                return LaneType.BOTTOM;
        }
    }
}
