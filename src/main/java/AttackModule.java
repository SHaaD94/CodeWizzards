import model.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class AttackModule implements BehaviourModule {
    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        if (State.getBehaviour() == State.BehaviourType.ESCAPING) {
            return;
        }

        Stream<LivingUnit> units = getLivingUnitStream(world);

        Optional<LivingUnit> min = units
                .filter(x -> x.getFaction() != self.getFaction()
                        /*&& (State.getBehaviour() == State.BehaviourType.GOING_FOR_RUNE && x.getFaction() != Faction.NEUTRAL)*/)
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
        //self.getRemainingCooldownTicksByAction()[2]
        min.ifPresent(x -> {
            move.setSpeed(-game.getWizardForwardSpeed());
            AttackUtil.setAttackUnit(self, game, move, x, ActionType.MAGIC_MISSILE);

            //move.setStrafeSpeed(game.getWizardStrafeSpeed());

            State.setBehaviour(State.BehaviourType.FIGHTING);
        });
    }

    private Stream<LivingUnit> getLivingUnitStream(World world) {
        Stream<LivingUnit> units = Arrays.stream(world.getWizards());
        units = Stream.concat(units, Arrays.stream(world.getBuildings()));
        units = Stream.concat(units, Arrays.stream(world.getMinions()));
        return units;
    }

    private Double getDistanceToMe(Wizard self, LivingUnit o1) {
        return o1.getDistanceTo(self) + o1.getRadius() * 0.2;
    }
}
