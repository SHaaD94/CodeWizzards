import model.Game;
import model.Move;
import model.Wizard;
import model.World;

/**
 * Created by SHaaD on 13.11.2016.
 */
interface BehaviourModule {
    void updateMove(Wizard self, World world, Game game, Move move);
}
