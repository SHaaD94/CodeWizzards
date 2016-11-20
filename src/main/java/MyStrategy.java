import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class MyStrategy implements Strategy {
    private List<BehaviourModule> behaviours = new ArrayList<>();
    private boolean isInit;

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        if (!isInit) {
            LaneType laneType;
            int i = new Random().nextInt(3);
            switch (i) {
                case 0:
                    laneType = LaneType.TOP;
                    break;
                case 1:
                    laneType = LaneType.MIDDLE;
                    break;
                default:
                    laneType = LaneType.BOTTOM;
                    break;
            }
            State.setLaneType(laneType);

            LanePointsHolder lanePointsHolder = new LanePointsHolder(game.getMapSize());
            behaviours.add(new RuneModule(lanePointsHolder));
            behaviours.add(new MovementModule(lanePointsHolder));
            behaviours.add(new AttackModule(lanePointsHolder));
            isInit = true;
        }
        behaviours.forEach(x -> x.updateMove(self, world, game, move));

        interceptMessages(self);
    }

    private void interceptMessages(Wizard self) {
        Message[] messages = self.getMessages();
        if (messages != null && messages.length != 0) {
            for (Message message : messages) {
                System.out.println(message.getLane());
                System.out.println(Arrays.toString(message.getRawMessage()));
                System.out.println(message.getSkillToLearn());
            }
        }
    }
}
