package game.module;

import model.Game;
import model.Move;
import model.Wizard;
import model.World;

/**
 * Created by SHaaD on 12.11.2016.
 */
public interface MovementModule {
    void changeMovement(Wizard self, World world, Game game, Move move);
}
