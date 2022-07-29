package domain;

public class Move {
    private Player player;
    private Tile tile;
    private boolean success;

    private Move(Player player, Tile tile, boolean success) {
        this.player = player;
        this.tile = tile;
        this.success = success;
    }

    public final void init() {
        if(this.success) this.player.increaseSuccessfulHits();
        this.player.updateAccuracy();
        this.player.setAccuracyProperty(this.player.getAccuracy());
        System.out.println(this.player.toString() + " accuracy ratio:" + this.player.accuracyProperty().getValue() + "%");
        this.player.setPointsProperty(this.tile.getBoard().getTotalPoints());
        System.out.println(this.player.toString() + " total points:" + this.player.pointsProperty().getValue());

    }
    public static Move create(Player player, Tile tile, boolean success) {
        Move m = new Move(player, tile, success);
        m.init();
        return m;
    }

    public Player getPlayer() {
        return player;
    }

    public Tile getTile() {
        return tile;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return  player.toString() + " attacked at: " + tile.toString();
    }
}
