import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MovementModule implements BehaviourModule {
    private final LanePointsHolder lanePointsHolder;

    MovementModule(LanePointsHolder lanePointsHolder) {
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        if (self.getLife() == 0) {
            State.setBehaviour(State.BehaviourType.DEAD);
            return;
        }
        if (shouldWaitCreeps(self, world)) {
            return;
        }

        ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(State.getLaneType());

        handleRuneBehaviour(self, world, game);

        if (isStateNotMoving() && State.getBehaviour() != State.BehaviourType.GOING_FOR_RUNE) {
            State.setCurrentPointIndex(Utils.getNearestPoint(self, controlPointsForLane));
            State.setBehaviour(State.BehaviourType.MOVING);
        }

        Point currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
        if (shouldEscape(self, world, game, controlPointsForLane)) {
            State.setCurrentPointIndex(Utils.getNearestPoint(self, controlPointsForLane) - 1);
            currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
            State.setBehaviour(State.BehaviourType.ESCAPING);
        }

        double distanceToPoint = self.getDistanceTo(currentPoint.getX(), currentPoint.getY());
        if (distanceToPoint <= Constants.POINT_RADIUS && State.getBehaviour() != State.BehaviourType.ESCAPING) {
            if (State.getCurrentPointIndex() + 1 < controlPointsForLane.size()) {
                State.increaseCurrentPointIndex();
            }
            currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
        }

        move.setSpeed(game.getWizardForwardSpeed());
        if (State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE && State.getBehaviour() != State.BehaviourType.ESCAPING) {
            Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);
            move.setTurn(self.getAngleTo(nearestRune.getX(), nearestRune.getY()));
        } else {
            move.setTurn(self.getAngleTo(currentPoint.getX(), currentPoint.getY()));
        }

        checkCollisions(self, world, game, move);

        System.out.println(State.getBehaviour());
    }

    private void handleRuneBehaviour(Wizard self, World world, Game game) {
        if (shouldGoForRune(self, world, game)) {
            Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);

            double distanceToNearestRune = self.getDistanceTo(nearestRune.getX(), nearestRune.getY());

            long bonusCount = Arrays.stream(world.getBonuses())
                    .filter(x -> self.getDistanceTo(x) <= Constants.RUNE_SCAN_DISTANCE).count();

            boolean runePickedUpOrDoesntExist = distanceToNearestRune == 0 ||
                    (distanceToNearestRune <= self.getVisionRange() - 20 && bonusCount == 0);

            if (runePickedUpOrDoesntExist && getTicksToReachRune(self, game, distanceToNearestRune) < 1) {
                State.setBehaviour(State.BehaviourType.NONE);
                State.increaseLastRuneIndex();
            } else {
                State.setBehaviour(State.BehaviourType.GOING_FOR_RUNE);
            }
        }
    }

    private void checkCollisions(Wizard self, World world, Game game, Move move) {
        Stream<CircularUnit> circularUnitStream = Arrays.stream(world.getWizards()).filter(x -> !x.isMe()).map(x -> (CircularUnit) x);
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getBuildings()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getMinions()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getTrees()).map(x -> (CircularUnit) x));
        circularUnitStream = circularUnitStream.filter(x -> self.getDistanceTo(x) <= Constants.COLLISION_SEARCH_DISTANCE);

        List<CircularUnit> unitsInVisionRange = circularUnitStream.collect(Collectors.toList());

        Optional<Tree> collidedTree = getCollidedTree(self, world);
        if (collidedTree.isPresent()) {
            Tree tree = collidedTree.get();
            AttackUtil.setAttackUnit(self, game, move, tree);
            return;
        }

        int iterationCountRight = getIterationCount(self, world, game, move, unitsInVisionRange, game.getWizardMaxTurnAngle());
        if (iterationCountRight == 0) {
            return;
        }

        int iterationCountLeft = getIterationCount(self, world, game, move, unitsInVisionRange, -game.getWizardMaxTurnAngle());

        if (iterationCountLeft <= iterationCountRight) {
            move.setTurn(-game.getWizardMaxTurnAngle());
            move.setStrafeSpeed(-game.getWizardStrafeSpeed());
        } else {
            move.setTurn(game.getWizardMaxTurnAngle());
            move.setStrafeSpeed(game.getWizardStrafeSpeed());
        }
    }

    private Optional<Tree> getCollidedTree(Wizard self, World world) {
        Point point = GeometryUtil.getNextIterationPosition(self.getAngle(), self.getX(), self.getY());
        List<Tree> trees = Arrays.stream(world.getTrees())
                .filter(x -> GeometryUtil.areCollides(point.getX(), point.getY(), self.getRadius(), x.getX(), x.getY(), x.getRadius()))
                .collect(Collectors.toList());
        return trees.stream().findFirst();
    }


    private int getIterationCount(Wizard self, World world, Game game, Move move, List<CircularUnit> unitsInVisionRange, double rotateAngle) {
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
            if (collisionCount != 0 || mapCollisionsExist(self, nextLocation, world)) {
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
        if (lifeRemaining >= Constants.HP_TO_ESCAPE) {
            return false;
        }
        boolean buildingThreatExists = Arrays.stream(world.getBuildings())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) <= x.getAttackRange() + 200)
                .findAny().isPresent();
        if (buildingThreatExists) {
            return true;
        }

        boolean wizardThreatExists = Arrays.stream(world.getWizards())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) <= x.getCastRange() + 200)
                .findAny().isPresent();
        if (wizardThreatExists) {
            return true;
        }

        boolean minionThreatExists = Arrays.stream(world.getMinions())
                .filter(x -> !(x.getFaction() == Faction.NEUTRAL && x.getRemainingActionCooldownTicks() == 0))
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> game.getDartRadius() + 30 >= self.getDistanceTo(x)
                        || game.getOrcWoodcutterAttackRange() + 30 >= self.getDistanceTo(x))
                .findFirst().isPresent();

        return minionThreatExists && lifeRemaining <= 0.3;

    }

    private boolean isStateNotMoving() {
        return State.getBehaviour() != State.BehaviourType.MOVING;
    }


    private boolean shouldGoForRune(Wizard self, World world, Game game) {
        Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);
        double distanceToNearestRune = self.getDistanceTo(nearestRune.getX(), nearestRune.getY());

        if (distanceToNearestRune > Constants.RUNE_ATTRACT_RADIUS) {
            return false;
        }

        int runeInterval = game.getBonusAppearanceIntervalTicks();

        int currentTickWithTimeForReachingRune = world.getTickIndex() + getTicksToReachRune(self, game, distanceToNearestRune);

        return currentTickWithTimeForReachingRune / runeInterval > State.getLastRuneIndex()
                || world.getTickIndex() / runeInterval > State.getLastRuneIndex();
    }

    private int getTicksToReachRune(Wizard self, Game game, double distanceToNearestRune) {
        return (int) ((distanceToNearestRune - self.getRadius()) / game.getWizardForwardSpeed()) + 1;
    }

    private boolean shouldWaitCreeps(Wizard self, World world) {
        Point stopPoint = lanePointsHolder.getStopPoint(State.getLaneType());
        return self.getDistanceTo(stopPoint.getX(), stopPoint.getY()) <= 100
                && world.getTickIndex() <= lanePointsHolder.getTicksWaiting(State.getLaneType());
    }

}
