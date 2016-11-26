import model.*;

import java.util.Optional;
import java.util.stream.Stream;

public class AttackModule implements BehaviourModule {
    private final LanePointsHolder lanePointsHolder;

    public AttackModule(LanePointsHolder lanePointsHolder) {
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        Optional<LivingUnit> min = getUnitToAttack(self.getCastRange(), self, world);
        if (min.isPresent()) {
            LivingUnit x = min.get();

            AttackUtil.setAttackUnit(self, game, move, x);

            State.setBehaviour(State.BehaviourType.FIGHTING);
        }
    }

    private Optional<LivingUnit> getUnitToAttack(Double scanDistance, Wizard self, World world) {
        Stream<LivingUnit> units = Utils.getLivingUnitStream(world);

        return units
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> getDistanceToMe(self, x) <= scanDistance + x.getRadius())
                .min((o1, o2) -> {
                    int compareDistanceResult = getDistanceToMe(self, o1).compareTo(getDistanceToMe(self, o2));
                    int compareHPResult = Double.compare(o1.getLife() * 1.0 / o1.getMaxLife(), o2.getLife() * 1.0 / o2.getMaxLife());

                    if (compareHPResult != 0)
                        return compareHPResult;

                    return compareDistanceResult;
                });
    }

    private Double getDistanceToMe(Wizard self, LivingUnit o1) {
        return o1.getDistanceTo(self) + o1.getRadius() * 0.2;
    }
}
