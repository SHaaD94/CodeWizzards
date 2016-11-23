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

        ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(State.getLaneType());

        if (State.getBehaviour() == State.BehaviourType.NONE) {
            State.setCurrentPointIndex(Utils.getNearestSafeControlPointIndex(self, world, controlPointsForLane));
            State.setBehaviour(State.BehaviourType.MOVING);
        } else if (State.getBehaviour() != State.BehaviourType.MOVING
                && State.getBehaviour() != State.BehaviourType.GOING_FOR_RUNE) {
            State.setCurrentPointIndex(Utils.getNearestControlPointIndex(self, controlPointsForLane));
            State.setBehaviour(State.BehaviourType.MOVING);
        }

        Point currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
        if (shouldEscape(self, world, game, controlPointsForLane)) {
            State.setCurrentPointIndex(Utils.getNearestSafeControlPointIndex(self, world, controlPointsForLane));
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

        if (State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE) {
            Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);
            move.setTurn(self.getAngleTo(nearestRune.getX(), nearestRune.getY()));
        }

        if (State.getBehaviour() == State.BehaviourType.MOVING || State.getBehaviour() == State.BehaviourType.ESCAPING) {
            move.setTurn(self.getAngleTo(currentPoint.getX(), currentPoint.getY()));
        }

        checkCollisions(self, world, game, move);

        System.out.println(State.getBehaviour());
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

        int iterationCountRight = getIterationCount(self, world, unitsInVisionRange, game.getWizardMaxTurnAngle());
        if (iterationCountRight == 0) {
            return;
        }

        int iterationCountLeft = getIterationCount(self, world, unitsInVisionRange, -game.getWizardMaxTurnAngle());

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


    private int getIterationCount(Wizard self, World world, List<CircularUnit> unitsInVisionRange, double rotateAngle) {
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
        double lifeRemaining = getLifeAfterMaxDamage(self, world, game) * 1.0 / self.getMaxLife();
        if (lifeRemaining >= Constants.HP_TO_ESCAPE) {
            return false;
        }
        boolean buildingThreatExists = Arrays.stream(world.getBuildings())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) <= x.getAttackRange() + 300)
                .findAny().isPresent();
        if (buildingThreatExists) {
            return true;
        }

        boolean wizardThreatExists = Arrays.stream(world.getWizards())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) <= x.getCastRange() + 100)
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

    private int getLifeAfterMaxDamage(Wizard self, World world, Game game) {
        int currentLife = self.getLife();
        List<Minion> minions = Arrays.stream(world.getMinions())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> x.getRemainingActionCooldownTicks() <= 15)
                .filter(x -> game.getDartRadius() + 15 >= self.getDistanceTo(x) - self.getRadius()
                        || game.getOrcWoodcutterAttackRange() + 5 >= self.getDistanceTo(x) - self.getRadius())
                .collect(Collectors.toList());
        List<Building> buildings = Arrays.stream(world.getBuildings())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) - self.getRadius() <= x.getAttackRange() + 50)
                .filter(x -> x.getRemainingActionCooldownTicks() <= 50)
                .collect(Collectors.toList());

        List<Wizard> wizards = Arrays.stream(world.getWizards())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) - self.getRadius() <= x.getCastRange() + 10)
                //FIXME: support multiple attack types later
                .filter(x -> x.getRemainingCooldownTicksByAction()[2] <= 15)
                .collect(Collectors.toList());
        for (Minion minion : minions) {
            minion.getDamage();
        }
        for (Building building : buildings) {
            currentLife -= building.getDamage();
        }
        for (Wizard wizard : wizards) {
            Status[] statuses = wizard.getStatuses();
            boolean hasEmpower = Arrays.stream(statuses).anyMatch(x -> x.getType() == StatusType.EMPOWERED);
            double resultDamage = game.getMagicMissileDirectDamage();
            if (hasEmpower) {
                resultDamage = game.getEmpoweredDamageFactor();
            }
            currentLife -= resultDamage;
        }
        return currentLife;
    }
}
