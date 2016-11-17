import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovementModule implements BehaviourModule {
    private final LaneType laneType;
    private final LanePointsHolder lanePointsHolder;

    private Wizard prevWizard;
    private World prevWorld;
    private Move prevMove;

    private boolean isInitialized;

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
            State.setCurrentPointIndex(getNearestPoint(self, controlPointsForLane));
            State.setBehaviour(State.BehaviourType.MOVING);
        }

        Point currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
        if (shouldEscape(self, world, game, controlPointsForLane)) {
            State.reduceCurrentPointIndex();
            currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
            State.setBehaviour(State.BehaviourType.ESCAPING);
        }

        double distanceToPoint = self.getDistanceTo(currentPoint.getX(), currentPoint.getY());
        if (distanceToPoint <= Constants.POINT_RADIUS && State.getBehaviour() != State.BehaviourType.ESCAPING) {
            State.increaseCurrentPointIndex();
            currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
        }

        move.setSpeed(game.getWizardForwardSpeed());
        move.setTurn(self.getAngleTo(currentPoint.getX(), currentPoint.getY()));

        checkCollisions(self, world, game, move);

        updateState(self, world, move);
    }

    private void checkCollisions(Wizard self, World world, Game game, Move move) {
        Stream<CircularUnit> circularUnitStream = Arrays.stream(world.getWizards()).filter(x -> !x.isMe()).map(x -> (CircularUnit) x);
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getBuildings()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getMinions()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getTrees()).map(x -> (CircularUnit) x));
        circularUnitStream = circularUnitStream.filter(x -> self.getDistanceTo(x) <= Constants.COLLISION_SEARCH_DISTANCE);

        List<CircularUnit> unitsInVisionRange = circularUnitStream.collect(Collectors.toList());

        if (State.getBehaviour() != State.BehaviourType.ESCAPING) {
            Optional<Tree> collidedTree = getCollidedTree(self, world);
            if (collidedTree.isPresent()) {
                Tree tree = collidedTree.get();

                AttackUtil.setAttackUnit(self, game, move, tree, ActionType.STAFF);
                return;
            }
        }

        int iterationCountRight = getIterationCount(self, game, move, unitsInVisionRange, game.getWizardMaxTurnAngle());
        if (iterationCountRight == 0) {
            return;
        }

        int iterationCountLeft = getIterationCount(self, game, move, unitsInVisionRange, -game.getWizardMaxTurnAngle());

        move.setTurn(iterationCountLeft <= iterationCountRight ? -game.getWizardMaxTurnAngle() : game.getWizardMaxTurnAngle());
    }

    private Optional<Tree> getCollidedTree(Wizard self, World world) {
        Point point = GeometryUtil.getNextIterationPosition(self.getAngle(), self.getX(), self.getY());
        List<Tree> trees = Arrays.stream(world.getTrees())
                //.filter(x -> self.getDistanceTo(x) <= Constants.TREE_COLLISION_SEARCH_DISTANCE)
                .filter(x -> GeometryUtil.areCollides(point.getX(), point.getY(), self.getRadius(), x.getX(), x.getY(), x.getRadius()))
                .collect(Collectors.toList());
/*todo: refactor this
        if (trees.size() <= Constants.TREE_BYPASS_COUNT) {
            return Optional.empty();
        }
*/
        return trees.stream().findFirst();
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
            Point nextLocation = GeometryUtil.getNextIterationPosition(self.getAngle(), currentX, currentY);

            long collisionCount = unitsInVisionRange.stream().filter(x ->
                    GeometryUtil.areCollides(nextLocation.getX(), nextLocation.getY(), self.getRadius(), x.getX(), x.getY(), x.getRadius()))
                    .count();
            if (collisionCount != 0 || mapCollisionsExist(self, nextLocation, prevWorld)) {
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

    private boolean mapCollisionsExist(Wizard self, Point nextLocation, World world) {
        return nextLocation.getX() - self.getRadius() <= 0
                || nextLocation.getX() + self.getRadius() >= world.getWidth()
                || nextLocation.getY() - self.getRadius() <= 0
                || nextLocation.getY() + self.getRadius() >= world.getHeight();
    }

    private boolean shouldEscape(Wizard self, World world, Game game, ArrayList<Point> controlPointsForLane) {
        boolean escapeAvailable = State.getCurrentPointIndex() > 0 && State.getCurrentPointIndex() < controlPointsForLane.size();
        if (!escapeAvailable) {
            return false;
        }
        double lifeRemaining = self.getLife() * 1.0 / self.getMaxLife();
        if (lifeRemaining >= 0.5) {
            return false;
        }
        boolean buildingThreatExists = Arrays.stream(world.getBuildings())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) <= x.getAttackRange() + 50)
                .findAny().isPresent();
        if (buildingThreatExists) {
            return true;
        }

        boolean wizardThreatExists = Arrays.stream(world.getWizards())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) <= x.getCastRange() + 50)
                .findAny().isPresent();
        if (wizardThreatExists) {
            return true;
        }

        boolean minionThreatExists = Arrays.stream(world.getMinions())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> game.getDartRadius() <= self.getDistanceTo(x)
                        || game.getOrcWoodcutterAttackRange() <= self.getDistanceTo(x))
                .findFirst().isPresent();
        if (minionThreatExists && lifeRemaining <= 0.3) {
            return true;
        }

        return false;
    }

    private boolean isStateNotMoving() {
        return State.getBehaviour() != State.BehaviourType.MOVING;
    }

    private void init(Wizard self, World world, Move move) {
        if (!isInitialized) {
            updateState(self, world, move);
            State.setCurrentPointIndex(0);
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
