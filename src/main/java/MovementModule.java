import model.*;

import java.util.ArrayList;

public class MovementModule implements BehaviourModule {
    private final LaneType laneType;
    private final LanePointsHolder lanePointsHolder;

    private Wizard prevWizard;
    private World prevWorld;
    private Move prevMove;

    private boolean isInitialized;

    private int currentPointIndex;
    private int currentPointIncrementor;

    public MovementModule(LaneType laneType, LanePointsHolder lanePointsHolder) {
        this.laneType = laneType;
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        init(self, world, move);
        if (self.getLife() == 0) {
            State.setBehaviour(State.BehaviourType.DEAD);
            return;
        }

        ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(laneType);

        if (State.getBehaviour() == State.BehaviourType.DEAD || State.getBehaviour() == State.BehaviourType.FIGHTING) {
            currentPointIndex = getNearestPoint(self, controlPointsForLane);
            State.setBehaviour(State.BehaviourType.MOVING);
        }

        Point currentPoint = controlPointsForLane.get(currentPointIndex);
        double distanceToPoint = self.getDistanceTo(currentPoint.getX(), currentPoint.getY());
        if (distanceToPoint <= Constants.POINT_RADIUS) {
            currentPointIndex += currentPointIncrementor;
            currentPoint = controlPointsForLane.get(currentPointIndex);
        }

        move.setSpeed(game.getWizardForwardSpeed());
        move.setTurn(self.getAngleTo(currentPoint.getX(), currentPoint.getY()));
        System.out.println(currentPointIndex);

        updateState(self, world, move);
    }

    private void init(Wizard self, World world, Move move) {
        if (!isInitialized) {
            updateState(self, world, move);
            if (self.getFaction() == Faction.ACADEMY) {
                currentPointIndex = 0;
                currentPointIncrementor = 1;
            } else {
                currentPointIndex = lanePointsHolder.getControlPointsForLane(laneType).size() - 1;
                currentPointIncrementor = -1;
            }
        }

        isInitialized = true;
    }

    private int getNearestPoint(Wizard self, ArrayList<Point> points) {
        int minIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++) {
            double currentDistance = self.getDistanceTo(points.get(i).getX(), points.get(i).getY());
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private void updateState(Wizard self, World world, Move move) {
        prevWizard = self;
        prevWorld = world;
        prevMove = move;
    }
}