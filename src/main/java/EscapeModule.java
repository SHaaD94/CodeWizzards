import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by SHaaD on 26.11.2016.
 */
public class EscapeModule implements BehaviourModule {
    private final LanePointsHolder lanePointsHolder;

    public EscapeModule(LanePointsHolder lanePointsHolder) {
        this.lanePointsHolder = lanePointsHolder;
    }

    @Override
    public void updateMove(Wizard self, World world, Game game, Move move) {
        ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(State.getLaneType());
        Point currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
        if (shouldEscape(self, world, game, controlPointsForLane)) {
            State.setCurrentPointIndex(Utils.getNearestSafeControlPointIndex(self, world, controlPointsForLane));
            currentPoint = controlPointsForLane.get(State.getCurrentPointIndex());
            State.setBehaviour(State.BehaviourType.ESCAPING);
        }

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
                .filter(x -> game.getDartRadius() + self.getRadius() >= self.getDistanceTo(x) + self.getRadius()
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