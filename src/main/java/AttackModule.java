import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.PI;

public class AttackModule implements BehaviourModule {
    private final LanePointsHolder lanePointsHolder;

    public AttackModule(LanePointsHolder lanePointsHolder) {
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        if (State.getBehaviour() == State.BehaviourType.ESCAPING/* || State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE*/) {
            return;
        }

        Stream<LivingUnit> units = getLivingUnitStream(world);

        Optional<LivingUnit> min = units
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> getDistanceToMe(self, x) <= self.getCastRange())
                .min((o1, o2) -> {
                    int compareDistanceResult = getDistanceToMe(self, o1).compareTo(getDistanceToMe(self, o2));
                    int compareHPResult = Double.compare(o1.getLife() * 1.0 / o1.getMaxLife(), o2.getLife() * 1.0 / o2.getMaxLife());

                    if (compareHPResult != 0)
                        return compareHPResult;

                    return compareDistanceResult;
                });
        min.ifPresent(x -> {
            long wizardCount = Arrays.stream(world.getWizards())
                    .filter(wizard -> self.getDistanceTo(wizard) <= wizard.getCastRange() * 1.5)
                    .filter(wizard -> wizard.getFaction() != self.getFaction())
                    .count();
            if (self.getDistanceTo(x) <= self.getCastRange() * 0.8 || wizardCount >= 2) {
                move.setSpeed(-game.getWizardForwardSpeed());

                int currentPointIndex = getPreviousPointIndex();

                ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(State.getLaneType());
                Point previousPoint = controlPointsForLane.get(currentPointIndex);
                checkIfCurrentPointIsPassed(self, previousPoint);
                setStrafeSpeed(self, previousPoint, game, move);
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
