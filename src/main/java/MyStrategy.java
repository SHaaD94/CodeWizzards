import game.module.AttackModule;
import game.module.AttackModuleImpl;
import game.module.MovementModule;
import game.module.MovementModuleImpl;
import model.*;

import java.util.Arrays;

public final class MyStrategy implements Strategy {
    private final MovementModule movementModule = new MovementModuleImpl();
    private final AttackModule attackModule = new AttackModuleImpl();

    @Override
    public void move(Wizard self, World world, Game game, Move move) {
        movementModule.changeMovement(self, world, game, move);
        attackModule.chooseAttack(self, world, game, move);

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
