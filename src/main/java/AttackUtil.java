import model.*;

/**
 * Created by SHaaD on 15.11.2016.
 */
public class AttackUtil {
    public static void setAttackUnit(Wizard self, Game game, Move move, LivingUnit x, ActionType attackType) {
        double angleTo = self.getAngleTo(x);
        move.setTurn(angleTo);
        if (StrictMath.abs(angleTo) < game.getStaffSector() / 2.0D) {
            move.setAction(attackType);
            move.setCastAngle(angleTo);
            move.setMinCastDistance(self.getDistanceTo(x) - x.getRadius() - game.getMagicMissileRadius());
        }
    }
}
