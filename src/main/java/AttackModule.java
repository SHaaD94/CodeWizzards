import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.PI;

public class AttackModule implements BehaviourModule {
    private final LanePointsHolder lanePointsHolder;

    public AttackModule(LanePointsHolder lanePointsHolder) {
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        if (State.getBehaviour() == State.BehaviourType.ESCAPING) {
            return;
        }

        Optional<LivingUnit> min = getUnitToAttack(self.getCastRange(), self, world);
        if (min.isPresent()) {
            LivingUnit x = min.get();
            List<Wizard> wizards = Arrays.stream(world.getWizards())
                    .filter(wizard -> self.getDistanceTo(wizard) <= wizard.getCastRange() * 1.5)
                    .filter(wizard -> wizard.getFaction() != self.getFaction()).collect(Collectors.toList());

            if ((self.getDistanceTo(x) <= self.getCastRange() * 0.8 || wizards.size() >= 2
                    || State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE)
                    && !shouldFightForRune(self, game, x, wizards)) {
                move.setSpeed(-game.getWizardForwardSpeed());

                int currentPointIndex = getPreviousPointIndex();

                Point previousPoint;
                ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(State.getLaneType());
                previousPoint = controlPointsForLane.get(currentPointIndex);

                checkIfCurrentPointIsPassed(self, previousPoint);
                setStrafeSpeed(self, previousPoint, game, move);
            }
            AttackUtil.setAttackUnit(self, game, move, x);

            State.setBehaviour(State.BehaviourType.FIGHTING);
        } else {
            if (State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE) {
                Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);
                double angleTo = self.getAngleTo(nearestRune.getX(), self.getY());
                if (angleTo >= PI / 2) {
                    move.setSpeed(0);
                }
            }
        }
    }

    private boolean shouldFightForRune(Wizard self, Game game, LivingUnit x, List<Wizard> wizards) {
        return State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE
                && wizards.size() == 1
                && x == wizards.get(0)
                && ((wizards.get(0).getLife() + game.getMagicMissileDirectDamage()) <= self.getLife());
    }

    //todo work on dat shit
    private void smartMoveToPoint(Wizard self, Move move, Game game) {
        Point nearestRune = Utils.getNearestRune(lanePointsHolder, self);
        double angleToRune = self.getAngleTo(nearestRune.getX(), nearestRune.getY());
        move.setTurn(angleToRune);
        if (Math.abs(angleToRune) >= 0 && Math.abs(angleToRune) < PI / 2) {
            move.setStrafeSpeed(-game.getWizardStrafeSpeed());
            move.setStrafeSpeed(game.getWizardForwardSpeed());
        } else if (Math.abs(angleToRune) >= PI / 2 && Math.abs(angleToRune) < PI) {
            move.setStrafeSpeed(-game.getWizardStrafeSpeed());
            move.setStrafeSpeed(game.getWizardBackwardSpeed());
        } else if (Math.abs(angleToRune) >= PI && Math.abs(angleToRune) < 3 * PI / 4) {
            move.setStrafeSpeed(game.getWizardStrafeSpeed());
            move.setStrafeSpeed(game.getWizardBackwardSpeed());
        } else {
            move.setStrafeSpeed(game.getWizardStrafeSpeed());
            move.setStrafeSpeed(game.getWizardForwardSpeed());
        }
    }

    private Optional<LivingUnit> getUnitToAttack(Double scanDistance, Wizard self, World world) {
        Stream<LivingUnit> units = getLivingUnitStream(world);

        return units
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> getDistanceToMe(self, x) <= scanDistance)
                .min((o1, o2) -> {
                    int compareDistanceResult = getDistanceToMe(self, o1).compareTo(getDistanceToMe(self, o2));
                    int compareHPResult = Double.compare(o1.getLife() * 1.0 / o1.getMaxLife(), o2.getLife() * 1.0 / o2.getMaxLife());

                    if (compareHPResult != 0)
                        return compareHPResult;

                    return compareDistanceResult;
                });
    }

    private int getPreviousPointIndex() {
        int currentPointIndex = State.getCurrentPointIndex();
        if (currentPointIndex > 0) {
            currentPointIndex--;
        }
        return currentPointIndex;
    }

    private void checkIfCurrentPointIsPassed(Wizard self, Point currentPoint) {
        double distanceToPoint = self.getDistanceTo(currentPoint.getX(), currentPoint.getY());
        if (distanceToPoint <= 20) {
            State.reduceCurrentPointIndex();
        }
    }

    private void setStrafeSpeed(Wizard self, Point previousControlPoint, Game game, Move move) {
        Point leftStrafeResult = GeometryUtil.getNextIterationPosition(self.getAngle() - PI / 2, self.getX(), self.getY());
        Point rightStrafeResult = GeometryUtil.getNextIterationPosition(self.getAngle() + PI / 2, self.getX(), self.getY());

        double leftStrafeToPoint = GeometryUtil.getDistanceBetweenPoints(previousControlPoint, leftStrafeResult);
        double rightStrafeToPoint = GeometryUtil.getDistanceBetweenPoints(previousControlPoint, rightStrafeResult);
        if (leftStrafeToPoint < rightStrafeToPoint) {
            move.setStrafeSpeed(-game.getWizardStrafeSpeed());
        } else {
            move.setStrafeSpeed(game.getWizardStrafeSpeed());
        }
    }

    private Stream<LivingUnit> getLivingUnitStream(World world) {
        Stream<LivingUnit> units = Arrays.stream(world.getWizards());
        units = Stream.concat(units, Arrays.stream(world.getBuildings()));
        units = Stream.concat(units, Arrays.stream(world.getMinions())
                .filter(x -> !(x.getFaction() == Faction.NEUTRAL && x.getRemainingActionCooldownTicks() == 0)));
        return units;
    }

    private Double getDistanceToMe(Wizard self, LivingUnit o1) {
        return o1.getDistanceTo(self) + o1.getRadius() * 0.2;
    }
}
