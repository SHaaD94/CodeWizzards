import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovementModule implements BehaviourModule {
    private final LaneType laneType;
    private final LanePointsHolder lanePointsHolder;

    private Wizard prevWizard;
    private World prevWorld;
    private Move prevMove;

    private boolean isInitialized;

    private int currentPointIndex;

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
            currentPointIndex--;
            currentPoint = controlPointsForLane.get(currentPointIndex);
            State.setBehaviour(State.BehaviourType.ESCAPING);
        }

        double distanceToPoint = self.getDistanceTo(currentPoint.getX(), currentPoint.getY());
        if (distanceToPoint <= Constants.POINT_RADIUS && State.getBehaviour() != State.BehaviourType.ESCAPING) {
            currentPointIndex++;
            currentPoint = controlPointsForLane.get(currentPointIndex);
        }

        move.setSpeed(game.getWizardForwardSpeed());
        move.setTurn(self.getAngleTo(currentPoint.getX(), currentPoint.getY()));

        updateState(self, world, move);

        checkCollisions(self, world, game, move);
    }

    private void checkCollisions(Wizard self, World world, Game game, Move move) {
        Stream<CircularUnit> circularUnitStream = Arrays.stream(world.getWizards()).filter(x -> !x.isMe()).map(x -> (CircularUnit) x);
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getBuildings()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getMinions()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getTrees()).map(x -> (CircularUnit) x));

        circularUnitStream = circularUnitStream.filter(x -> self.getDistanceTo(x) <= self.getVisionRange());

        List<CircularUnit> unitsInVisionRange = circularUnitStream.collect(Collectors.toList());

        int iterationCountRight = getIterationCount(self, game, move, unitsInVisionRange, game.getWizardMaxTurnAngle());
        if (iterationCountRight == 0) {
            return;
        }
        int iterationCountLeft = getIterationCount(self, game, move, unitsInVisionRange, -game.getWizardMaxTurnAngle());

        move.setTurn(iterationCountLeft <= iterationCountRight ? -game.getWizardMaxTurnAngle() : game.getWizardMaxTurnAngle());

        System.out.println(iterationCountRight);
    }

    private int getIterationCount(Wizard self, Game game, Move move, List<CircularUnit> unitsInVisionRange, double rotateAngle) {
        int iterationCount = 0;
        double currentAngle = self.getAngle();
        double currentX = self.getX();
        double currentY = self.getY();
        boolean hasCollisions = true;
        while (hasCollisions) {
            hasCollisions = false;
            //todo: improve, for now it is ok
            Point point = GeometryUtil.getNextIterationPosition(self.getAngle(), currentX, currentY);

            long collisionCount = unitsInVisionRange.stream().filter(x ->
                    GeometryUtil.areCollides(point.getX(), point.getY(), self.getRadius(), x.getX(), x.getY(), x.getRadius()))
                    .count();
            if (collisionCount != 0) {
                currentAngle += rotateAngle;
                Point newPoint = GeometryUtil.getNextIterationPosition(currentAngle, currentX, currentY);
                currentX = newPoint.getX();
                currentY = newPoint.getY();
                iterationCount++;
                hasCollisions = true;
            }
        }
        if (iterationCount != 0) {
            move.setTurn(game.getWizardMaxTurnAngle());
        }
        return iterationCount;
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
            } else {
                currentPointIndex = lanePointsHolder.getControlPointsForLane(laneType).size() - 1;
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
