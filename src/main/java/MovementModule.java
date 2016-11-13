import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

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

        if (isStateNotMoving()) {
            currentPointIndex = getNearestPoint(self, controlPointsForLane);
            State.setBehaviour(State.BehaviourType.MOVING);
        }

        Point currentPoint = controlPointsForLane.get(currentPointIndex);
        if (isLowHealthAndNotFirstPoint(self, controlPointsForLane)) {
            currentPointIndex -= currentPointIncrementor;
            currentPoint = controlPointsForLane.get(currentPointIndex);
            State.setBehaviour(State.BehaviourType.ESCAPING);
        }

        double distanceToPoint = self.getDistanceTo(currentPoint.getX(), currentPoint.getY());
        if (distanceToPoint <= Constants.POINT_RADIUS && State.getBehaviour() != State.BehaviourType.ESCAPING) {
            currentPointIndex += currentPointIncrementor;
            currentPoint = controlPointsForLane.get(currentPointIndex);
        }

        move.setSpeed(game.getWizardForwardSpeed());
        move.setTurn(self.getAngleTo(currentPoint.getX(), currentPoint.getY()));

        updateState(self, world, move);

        //todo: should be implemented
        //checkCollisions(self, world, game, move);
    }

    private void checkCollisions(Wizard self, World world, Game game, Move move) {
        Stream<CircularUnit> circularUnitStream = /*Arrays.stream(world.getWizards()).filter(x -> !x.isMe()).map(x -> (CircularUnit) x);
        circularUnitStream = Stream.concat(circularUnitStream, */Arrays.stream(world.getBuildings()).map(x -> (CircularUnit) x);
        /*circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getMinions()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getTrees()).map(x -> (CircularUnit) x));
*/
        Point point = new Point(self.getX() - self.getSpeedX() * 3, self.getY() - self.getSpeedY() * 3);
        long count = circularUnitStream.filter(x ->
                GeometryUtil.areCollides(point.getX(), point.getY(), self.getRadius(), x.getX(), x.getY(), x.getRadius()))
                .peek(System.out::println)
                .count();
        if (count != 0) {
            move.setTurn(game.getWizardMaxTurnAngle());
        }
    }

    private boolean isLowHealthAndNotFirstPoint(Wizard self, ArrayList<Point> controlPointsForLane) {
        return self.getLife() <= self.getMaxLife() * 0.5 && currentPointIndex > 0 && currentPointIndex < controlPointsForLane.size();
    }

    private boolean isStateNotMoving() {
        return State.getBehaviour() != State.BehaviourType.MOVING;
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
