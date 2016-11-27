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
        ArrayList<Point> controlPointsForLane = lanePointsHolder.getControlPointsForLane(State.getLane());

        List<Wizard> wizards = Arrays.stream(world.getWizards())
                .filter(wizard -> self.getDistanceTo(wizard) + self.getRadius() <= wizard.getCastRange())
                .filter(wizard -> wizard.getFaction() != self.getFaction()).collect(Collectors.toList());

        if (shouldEscape(self, world, game, controlPointsForLane)) {
            State.setCurrentPointIndex(Utils.getNearestSafeControlPointIndex(self, world, controlPointsForLane));
            State.setBehaviour(State.BehaviourType.ESCAPING);
            return;
        }

        if (shouldRetreat(self, world, game, wizards)) {
            int nearestControlPointIndex = Utils.getNearestControlPointIndex(self, controlPointsForLane);
            State.setCurrentPointIndex(nearestControlPointIndex > 1 ? nearestControlPointIndex - 1 : 0);
            State.setBehaviour(State.BehaviourType.RETREATING);
            return;
        }

        if (State.getBehaviour() == State.BehaviourType.ESCAPING && controlPointsForLane.size() > State.getCurrentPointIndex() + 2) {
            State.increaseCurrentPointIndex(2);
        }

        State.setBehaviour(State.BehaviourType.MOVING);
    }

    private boolean shouldRetreat(Wizard self, World world, Game game, List<Wizard> wizards) {
        return wizards.size() >= 2 || isMinionThreatExists(self, world, game);
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
        boolean buildingThreatExists = isBuildingThreatExists(self, world);
        if (buildingThreatExists) {
            return true;
        }

        boolean wizardThreatExists = Arrays.stream(world.getWizards())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) <= x.getCastRange() + 50)
                .findAny().isPresent();
        if (wizardThreatExists) {
            return true;
        }

        boolean minionThreatExists = isMinionThreatExists(self, world, game);

        return minionThreatExists && lifeRemaining <= 0.3;

    }

    private boolean isBuildingThreatExists(Wizard self, World world) {
        return Arrays.stream(world.getBuildings())
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) + self.getRadius() <= x.getAttackRange() + 100)
                .findAny().isPresent();
    }

    private boolean isMinionThreatExists(Wizard self, World world, Game game) {
        return Arrays.stream(world.getMinions())
                .filter(x -> !(x.getFaction() == Faction.NEUTRAL && x.getRemainingActionCooldownTicks() == 0))
                .filter(x -> x.getFaction() != self.getFaction())
                .filter(x -> self.getDistanceTo(x) + self.getRadius() <= 200)
                .findFirst().isPresent();
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
                .filter(x -> x.getRemainingActionCooldownTicks() <= 150)
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
