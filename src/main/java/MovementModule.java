import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.PI;

class MovementModule implements BehaviourModule {
    private final LanePointsHolder lanePointsHolder;

    MovementModule(LanePointsHolder lanePointsHolder) {
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {

        ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(State.getLane());

        if (State.getBehaviour() != State.BehaviourType.ESCAPING
                && State.getBehaviour() != State.BehaviourType.RETREATING) {
            if (State.getBehaviour() == State.BehaviourType.NONE) {
                State.setCurrentPointIndex(Utils.getNearestSafeControlPointIndex(self, world, controlPointsForLane));
                State.setBehaviour(State.BehaviourType.MOVING);
            } else if (State.getBehaviour() != State.BehaviourType.MOVING
                    && State.getBehaviour() != State.BehaviourType.GOING_FOR_RUNE) {
                State.setCurrentPointIndex(Utils.getNearestControlPointIndex(self, controlPointsForLane));
                State.setBehaviour(State.BehaviourType.MOVING);
            }
        }

        Point currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());

        double distanceToPoint = self.getDistanceTo(currentPoint.getX(), currentPoint.getY());
        if (distanceToPoint <= Constants.POINT_RADIUS
                && State.getBehaviour() != State.BehaviourType.ESCAPING
                && State.getBehaviour() != State.BehaviourType.RETREATING) {
            if (State.getCurrentPointIndex() + 1 < controlPointsForLane.size()) {
                State.increaseCurrentPointIndex();
            }
            currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
        }

        Point pointToMove = currentPoint;
        if (State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE
                && State.getBehaviour() != State.BehaviourType.ESCAPING) {
            pointToMove = Utils.getNearestRune(lanePointsHolder, self);
        }

        smartMoveToPoint(self, pointToMove, world, game, move);

        System.out.println(State.getBehaviour());
    }

    private void smartMoveToPoint(Wizard self, Point pointToMove, World world, Game game, Move move) {
        double bestMoveSpeed = 0;
        double bestStrafeSpeed = 0;
        double minDistance = Double.MAX_VALUE;
        Point positionAfterMoving = new Point(self.getX(), self.getY());
        for (double moveSpeed = -game.getWizardBackwardSpeed(); moveSpeed <= game.getWizardForwardSpeed(); moveSpeed += 1) {
            for (double strafeSpeed = -game.getWizardStrafeSpeed(); strafeSpeed <= game.getWizardStrafeSpeed(); strafeSpeed += 1) {
                double moveAngle = self.getAngle();

                Point afterMovingByX = GeometryUtil.getNextIterationPosition(moveSpeed, moveAngle, self.getX(), self.getY());
                double strafeAngle;
                if (self.getAngle() <= 0) {
                    strafeAngle = self.getAngle() + 3 * (self.getAngle() <= 0 ? -PI : PI) / 2; // 3PI/2
                } else {
                    strafeAngle = self.getAngle() + (self.getAngle() <= 0 ? -PI : PI) / 2; // PI/2
                }

                positionAfterMoving = GeometryUtil.getNextIterationPosition(strafeSpeed, strafeAngle, afterMovingByX.getX(), afterMovingByX.getY());
                double distanceBetweenPoints = GeometryUtil.getDistanceBetweenPoints(positionAfterMoving, pointToMove);
                if (mapCollisionsExist(self, positionAfterMoving, world)
                        || areCollisionExist(positionAfterMoving, self.getRadius() + 5, world)) {
                    continue;
                }
                if (distanceBetweenPoints <= minDistance) {
                    minDistance = distanceBetweenPoints;
                    bestMoveSpeed = moveSpeed;
                    bestStrafeSpeed = strafeSpeed;
                }
            }
        }

        State.setCuttingTree(false);

        Optional<Tree> collidedTree = getCollidedTree(self, world, positionAfterMoving);

        collidedTree.ifPresent(tree -> {
            State.setCuttingTree(true);
            AttackUtil.setAttackUnit(self, game, move, tree);
        });

        if ((!collidedTree.isPresent() && !anyTargetExists(self, world))
                || State.getBehaviour() == State.BehaviourType.ESCAPING) {
            move.setTurn(self.getAngleTo(pointToMove.getX(), pointToMove.getY()));
        }

        move.setSpeed(bestMoveSpeed);
        move.setStrafeSpeed(bestStrafeSpeed);
    }

    private boolean anyTargetExists(Wizard self, World world) {
        return Utils.getLivingUnitStream(world)
                .filter(x -> x.getFaction() != self.getFaction())
                .anyMatch(x -> self.getDistanceTo(x) <= self.getCastRange() + x.getRadius());
    }

    private Optional<Tree> getCollidedTree(Wizard self, World world, Point finalPosition) {
        return Arrays.stream(world.getTrees())
                .filter(x -> self.getDistanceTo(x) <= self.getCastRange())
                .filter(x -> GeometryUtil.areCollides(finalPosition.getX(), finalPosition.getY(), self.getRadius() + 10,
                        x.getX(), x.getY(), x.getRadius()))
                .min((o1, o2) -> Double.compare(self.getDistanceTo(o1), self.getDistanceTo(o2)));
    }

    private boolean areCollisionExist(Point selfPosition, double selfRadius, World world) {
        Stream<CircularUnit> circularUnitStream = Arrays.stream(world.getWizards()).filter(x -> !x.isMe()).map(x -> (CircularUnit) x);
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getBuildings()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getMinions()).map(x -> (CircularUnit) x));
        circularUnitStream = Stream.concat(circularUnitStream, Arrays.stream(world.getTrees()).map(x -> (CircularUnit) x));

        circularUnitStream = circularUnitStream.filter(x ->
                GeometryUtil.getDistanceBetweenPoints(selfPosition, x) <= Constants.COLLISION_SEARCH_DISTANCE);

        return circularUnitStream.anyMatch(x -> GeometryUtil.areCollides(selfPosition, selfRadius, x));
    }

    private boolean mapCollisionsExist(Wizard self, Point nextLocation, World world) {
        return nextLocation.getX() - self.getRadius() <= 0
                || nextLocation.getX() + self.getRadius() >= world.getWidth()
                || nextLocation.getY() - self.getRadius() <= 0
                || nextLocation.getY() + self.getRadius() >= world.getHeight();
    }

}
