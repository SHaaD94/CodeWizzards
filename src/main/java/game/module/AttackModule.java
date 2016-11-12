package game.module;

import model.Game;
import model.Move;
import model.Wizard;
import model.World;

/**
 * Created by SHaaD on 12.11.2016.
 */
public interface AttackModule {
    void chooseAttack(Wizard self, World world, Game game, Move move);
}
