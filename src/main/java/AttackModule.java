import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.PI;

public class AttackModule implements BehaviourModule {
    private final LaneType laneType;
    private final LanePointsHolder lanePointsHolder;

    public AttackModule(LaneType laneType, LanePointsHolder lanePointsHolder) {
        this.laneType = laneType;
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        if (State.getBehaviour() == State.BehaviourType.ESCAPING || State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE) {
            return;
        }

        Stream<LivingUnit> units = getLivingUnitStream(world);

        Optional<LivingUnit> min = units
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> getDistanceToMe(self, x) <= self.getCastRange())
                .min((o1, o2) -> {
                    int compareResult = getDistanceToMe(self, o1).compareTo(getDistanceToMe(self, o2));
                    boolean firstIsOneShot = o1.getLife() <= game.getMagicMissileDirectDamage();
                    boolean secondIsOneShot = o2.getLife() <= game.getMagicMissileDirectDamage();

                    if (firstIsOneShot && secondIsOneShot) {
                        return compareResult;
                    } else if (firstIsOneShot) {
                        return -1;
                    } else if (secondIsOneShot) {
                        return 1;
                    }

                    return compareResult;
                });
        min.ifPresent(x -> {
            long wizardCount = Arrays.stream(world.getWizards())
                    .filter(wizard -> self.getDistanceTo(wizard) <= wizard.getCastRange() * 1.5)
                    .filter(wizard -> wizard.getFaction() != self.getFaction())
                    .count();
            if (self.getDistanceTo(x) <= self.getCastRange() * 0.7 || wizardCount >= 2) {
                move.setSpeed(-game.getWizardForwardSpeed());

                int currentPointIndex = getPreviousPointIndex();

                checkIfCurrentPointIsPassed(self, currentPointIndex);
                setStrafeSpeed(self, currentPointIndex, game, move);
            }
            AttackUtil.setAttackUnit(self, game, move, x);

            State.setBehaviour(State.BehaviourType.FIGHTING);
        });
    }

    private int getPreviousPointIndex() {
        int currentPointIndex = State.getCurrentPointIndex();
        if (currentPointIndex > 0) {
            currentPointIndex--;
        }
        return currentPointIndex;
    }

    private void checkIfCurrentPointIsPassed(Wizard self, int currentPointIndex) {
        ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(laneType);
        Point currentPoint = controlPointsForLane.get(currentPointIndex);
        double distanceToPoint = self.getDistanceTo(currentPoint.getX(), currentPoint.getY());
        if (distanceToPoint <= 20) {
            State.reduceCurrentPointIndex();
        }
    }

    private void setStrafeSpeed(Wizard self, int currentPointIndex, Game game, Move move) {
        Point point = lanePointsHolder.getControlPointsForLane(laneType).get(currentPointIndex);
        Point leftStrafeResult = GeometryUtil.getNextIterationPosition(self.getAngle() - PI / 2, self.getX(), self.getY());
        Point rightStrafeResult = GeometryUtil.getNextIterationPosition(self.getAngle() + PI / 2, self.getX(), self.getY());

        double leftStrafeToPoint = GeometryUtil.getDistanceBetweenPoints(point, leftStrafeResult);
        double rightStrafeToPoint = GeometryUtil.getDistanceBetweenPoints(point, rightStrafeResult);
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
