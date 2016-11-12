package game.module;

import model.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class AttackModuleImpl implements AttackModule {
    @Override
    public void chooseAttack(Wizard self, World world, Game game, Move move) {
        Stream<LivingUnit> units = Arrays.stream(world.getWizards());
        units = Stream.concat(units, Arrays.stream(world.getBuildings()));
        units = Stream.concat(units, Arrays.stream(world.getMinions()));

        Optional<LivingUnit> min = units
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> getDistanceToMe(self, x) <= self.getCastRange())
                .min((o1, o2) -> getDistanceToMe(self, o2).compareTo(getDistanceToMe(self, o1)));
        //self.getRemainingCooldownTicksByAction()[2]
        min.ifPresent(x -> {
            move.setTurn(self.getAngleTo(x));
            move.setSpeed(0);

            move.setAction(ActionType.MAGIC_MISSILE);
        });
    }

    private Double getDistanceToMe(Wizard self, LivingUnit o1) {
        return o1.getDistanceTo(self) + o1.getRadius() * 0.5;
    }
}
