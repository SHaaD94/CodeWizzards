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
        //self.getRemainingCooldownTicksByAction()[2]
        min.ifPresent(x -> {
            double angleTo = self.getAngleTo(x);
            move.setTurn(angleTo);
            move.setSpeed(-game.getWizardForwardSpeed());
            if (StrictMath.abs(self.getAngleTo(x)) < game.getStaffSector() / 2.0D) {
                move.setAction(ActionType.MAGIC_MISSILE);
                move.setCastAngle(self.getAngleTo(x));
                move.setMinCastDistance(self.getDistanceTo(x) - x.getRadius() - game.getMagicMissileRadius());
            }

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
