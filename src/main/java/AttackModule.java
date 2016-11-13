import model.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class AttackModule implements BehaviourModule {
    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        Stream<LivingUnit> units = Arrays.stream(world.getWizards());
        units = Stream.concat(units, Arrays.stream(world.getBuildings()));
        units = Stream.concat(units, Arrays.stream(world.getMinions()));

        Optional<LivingUnit> min = units
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> getDistanceToMe(self, x) <= self.getCastRange() - 5)
                .min((o1, o2) -> getDistanceToMe(self, o1).compareTo(getDistanceToMe(self, o2)));
        //self.getRemainingCooldownTicksByAction()[2]
        min.ifPresent(x -> {
            double angleTo = self.getAngleTo(x);
            move.setTurn(angleTo);
            move.setSpeed(-game.getWizardForwardSpeed());
            if (StrictMath.abs(self.getAngleTo(x)) < game.getStaffSector() / 2.0D) {
                move.setAction(ActionType.MAGIC_MISSILE);
                move.setCastAngle(self.getAngleTo(x));
                move.setMinCastDistance(self.getDistanceTo(x) - x.getRadius() + game.getMagicMissileRadius());
            }

//            move.setStrafeSpeed(game.getWizardStrafeSpeed());

            State.setBehaviour(State.BehaviourType.FIGHTING);
        });
    }

    private Double getDistanceToMe(Wizard self, LivingUnit o1) {
        return o1.getDistanceTo(self) + o1.getRadius() * 0.2;
    }
}
