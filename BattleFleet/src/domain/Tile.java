package domain;

import javafx.event.EventTarget;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Tile extends Rectangle implements EventTarget {
    private int x;
    private int y;
    private Board board; // tile belongs to a certain grid
    private Ship ship = null; // tile may have part of a ship
    private boolean hit = false;
    private boolean occupied = false;

    public Tile(double width, double height, int x, int y, Board board) {
        super(width, height);
        this.setFill(Color.CADETBLUE);
        this.setStroke(Color.BLACK);
        this.x = x;
        this.y = y;
        this.board = board;
    }

    public boolean shootTile() {
        this.setHit(true);
        if (ship != null) {
            ship.hit();
            this.board.addToPlayerTotalPoints(ship.getType().getHitReward());
            System.out.println(ship.getType() + " is hit! -> Reward: " + ship.getType().getHitReward() + " points!");
            this.setFill(Color.RED);
            if (ship.getState() == Ship.State.DESTROYED) {
                for(Tile t : ship.getShipCoordinates()) {
                    t.setFill(Color.BLACK);
                    // Set surrounding tiles as hit
                    if(this.getBoard().isEnemy()) {
                        List<Tile> neighbours = t.getBoard().getNeighbours(t.get_X(), t.get_Y(), true);
                        for (Tile n : neighbours) {
                            if (!t.getFill().equals(Color.BLACK)) n.setFill(Color.GRAY);
                            n.setHit(true);
                        }
                    }
                }
                this.board.decreaseShipsRemaining();
                this.board.addToPlayerTotalPoints(ship.getType().getSinkBonus());
                System.out.println(ship.getType() + " is Destroyed -> Bonus points for sinking: " + ship.getType().getSinkBonus());
            }
            return true;
        } else {
            this.setFill(Color.GRAY);
        }
        return false;
    }

    public int get_X() {
        return x;
    }

    public int get_Y() {
        return y;
    }

    public Board getBoard() {
        return board;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isHit() {
        return hit;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public String toString() {
        return "(" + this.get_X() + ", " + this.get_Y() + ")";
    }
}
