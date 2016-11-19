import model.*;

/**
 * Created by SHaaD on 15.11.2016.
 */
class AttackUtil {
    static void setAttackUnit(Wizard self, Game game, Move move, LivingUnit x) {
        double angleTo = self.getAngleTo(x);
        move.setTurn(angleTo);
        if (StrictMath.abs(angleTo) < game.getStaffSector() / 2.0D) {
            ActionType attack = self.getRemainingCooldownTicksByAction()[2] == 0
                    ? ActionType.MAGIC_MISSILE
                    : ActionType.STAFF;

            GeometryUtil.getNextIterationPosition(x.getSpeedX(), x.getAngle(), x.getX(), x.getY());
            move.setAction(attack);
            move.setCastAngle(angleTo);
            move.setMinCastDistance(self.getDistanceTo(x) - x.getRadius() - game.getMagicMissileRadius());
        }
    }
}
