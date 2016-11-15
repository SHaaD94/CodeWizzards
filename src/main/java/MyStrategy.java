import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MyStrategy implements Strategy {
    private List<BehaviourModule> behaviours = new ArrayList<>();
    private boolean isInit;

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        if (!isInit) {
            LaneType laneType = LaneType.TOP;
            if (self.getId() == 5 || self.getId() == 6) {
                laneType = LaneType.MIDDLE;
            } else if (self.getId() > 6) {
                laneType = LaneType.BOTTOM;
            }
            behaviours.add(new MovementModule(laneType, new LanePointsHolder(game.getMapSize())));
            behaviours.add(new AttackModule());
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
