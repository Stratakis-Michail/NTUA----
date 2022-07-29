package domain;

import java.util.ArrayList;
import java.util.List;

public class Ship {
    private List<Tile> shipCoordinates;
    private ShipType type;
    private Board board;
    private int health;
    private State state;

    public Ship(int typeIndex, Board board) {
        this.type = ShipType.indexOf(typeIndex);
        this.health = this.type.getSize();
        this.state = State.INTACT;
        this.shipCoordinates = new ArrayList<>();
        this.board = board;
    }

    public List<Tile> getShipCoordinates() {
        return shipCoordinates;
    }

    public void setShipCoordinates(List<Tile> shipCoordinates) {
        this.shipCoordinates = shipCoordinates;
    }

    public ShipType getType() {
        return type;
    }

    public Board getBoard() {
        return board;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void hit() {
        if (--health == 0)
            this.setState(State.DESTROYED);
        if (health > 0) {
            this.setState(State.COMPROMISED);
        }
    }

    protected enum State {
        INTACT, COMPROMISED, DESTROYED
    }
}
