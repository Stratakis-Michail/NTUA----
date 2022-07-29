package domain;

import customExceptions.AdjacentTilesException;
import customExceptions.CustomException;
import customExceptions.OverlapTilesException;
import customExceptions.OversizeException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class Board extends BorderPane {
    private boolean enemy;
    private double tile_size = 20;
    private final int HORIZONTAL_SIZE = 10;
    private final int VERTICAL_SIZE = 10;
    private List<Ship> ships = new ArrayList<>();
    private IntegerProperty shipsRemaining = new SimpleIntegerProperty(5);
    private int totalPoints = 0;

    public Board (int tile_size, boolean enemyBoard, EventHandler<MouseEvent> handler) {
        this.enemy = enemyBoard;
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(HORIZONTAL_SIZE * tile_size, VERTICAL_SIZE * tile_size);
        gridPane.setGridLinesVisible(true);
        VBox vBox = new VBox();
        HBox hBox = new HBox();
        for (int y = 0; y < VERTICAL_SIZE; y++) {
            for (int x = 0; x < HORIZONTAL_SIZE; x++) {
                Tile tile = new Tile(tile_size, tile_size, x, y, this);
                tile.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);
                gridPane.add(tile, x, y);
            }
            Label rowIndex = new Label(String.valueOf(y));
            rowIndex.setPrefSize(10.0, tile_size);
            vBox.getChildren().add(rowIndex);
            vBox.setPrefSize(10.0, tile_size*HORIZONTAL_SIZE);
        }
        for (int i = 0; i < HORIZONTAL_SIZE; i++) {
            Label columnIndex = new Label(String.valueOf(i));
            columnIndex.setTextAlignment(TextAlignment.CENTER);
            columnIndex.setAlignment(Pos.TOP_CENTER);
            columnIndex.setPrefSize(tile_size, 10.0);
            hBox.getChildren().add(columnIndex);
        }
        hBox.setPrefSize(tile_size*HORIZONTAL_SIZE, 10.0);
        hBox.setAlignment(Pos.TOP_CENTER);
        this.setCenter(gridPane);
        this.setLeft(vBox);
        this.setTop(hBox);
        Label label = new Label((enemy ? "Computer's" : "Player's" ) + " Battlefield:");
        label.setAlignment(Pos.CENTER);
        VBox bottom = new VBox();
        bottom.setAlignment(Pos.TOP_CENTER);
        bottom.getChildren().add(label);
        this.setBottom(bottom);

    }

    private void placeShip(Ship ship, int x, int y, boolean vertical){
            List<Tile> coordinates = new ArrayList<>();
            if (vertical) {
                for (int i = y; i < y + ship.getType().getSize(); i++) {
                    Tile tile = getTile(x, i);
                    tile.setShip(ship);
                    coordinates.add(tile);
                    if(!this.enemy)
                        tile.setFill(ship.getType().getColor());
                    tile.setOccupied(true);
                }
            }
            else {
                for (int j = x; j < x + ship.getType().getSize(); j++) {
                    Tile tile = getTile(j, y);
                    tile.setShip(ship);
                    coordinates.add(tile);
                    if(!this.enemy)
                        tile.setFill(ship.getType().getColor());
                    tile.setOccupied(true);
                }
            }
            ship.setShipCoordinates(coordinates);
            ships.add(ship);
            System.out.println("Placing ship at [" + x + ", " + y + "]");
    }

    public boolean validateAndPlaceShip(Ship ship, int x, int y, boolean vertical, boolean fromFile) throws CustomException {
        // validate placing, if loaded form file throw exception, else return false placing
        if (vertical) {
            for (int i = y; i < y + ship.getType().getSize(); i++) {
                if (!this.isValidPoint(x, i)) {
                    if (fromFile) throw new OversizeException("Out of border placing!!");
                    return false;
                }
                Tile tile = getTile(x, i);
                if (tile.isOccupied()) {
                    if(fromFile) throw new OverlapTilesException("Another ship occupies this space!");
                    return false;
                }
                for (Tile neighbour : getNeighbours(x, i, true)) {
                    if (neighbour.isOccupied()) {
                        if (fromFile) throw new AdjacentTilesException("Ships cannot touch!");
                        return false;
                    }
                }
            }
        } else {
            for (int j = x; j < x + ship.getType().getSize(); j++) {
                if (!this.isValidPoint(j, y)) {
                    if (fromFile) throw new OversizeException("Out of border placing!!");
                    return false;
                }
                Tile tile = getTile(j, y);
                if (tile.isOccupied()) {
                    if (fromFile)
                        throw new OverlapTilesException("Another ship occupies this space!");
                    return false;
                }
                for (Tile neighbour : getNeighbours(j, y, true)) {
                    if (neighbour.isOccupied()) {
                        if (fromFile) throw new AdjacentTilesException("Ships cannot touch!");
                        return false;
                    }
                }
            }
        }
        // Place after validation assessment
        placeShip(ship, x, y, vertical);
        return true;
    }

    // grid 10x10 indexes tiles from to 100 (index 0 is the Group):
    public Tile getTile(int x, int y) {
        int index = x + y * HORIZONTAL_SIZE;
        return (Tile) ((GridPane) this.getCenter()).getChildren().get(index+1);
    }

    // If placing get all surrounding tiles, else (shooting) don't include diagonals
    public List<Tile> getNeighbours(int x, int y, boolean placing) {
        List<Tile> neighbours = new ArrayList<>();
        if (isValidPoint(x - 1, y)) // has left
            neighbours.add(getTile(x - 1, y));
        if (isValidPoint(x, y -1)) // has top
            neighbours.add(getTile(x, y - 1));
        if (isValidPoint(x + 1, y))// has right
            neighbours.add(getTile(x + 1, y));
        if (isValidPoint(x, y + 1)) // has bottom
            neighbours.add(getTile(x, y+1));
        // Get Diagonal Neighbours
        if (placing){
            if (isValidPoint(x - 1, y - 1)) // has top-left
                neighbours.add(getTile(x - 1, y - 1 ));
            if (isValidPoint(x + 1, y -1)) // has top-right
                neighbours.add(getTile(x + 1, y - 1));
            if (isValidPoint(x + 1, y + 1))// has bottom-right
                neighbours.add(getTile(x + 1, y + 1));
            if (isValidPoint(x - 1, y + 1)) // has bottom-left
                neighbours.add(getTile(x -1 , y + 1));
        }
        return neighbours;
    }

    public boolean isValidPoint(int x, int y) {
        return x >= 0 && x < this.HORIZONTAL_SIZE && y >= 0 && y < this.VERTICAL_SIZE;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void addToPlayerTotalPoints(int points) {
        this.totalPoints += points;
    }

    public boolean isEnemy() {
        return enemy;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public int getShipsRemaining() {
        return shipsRemaining.get();
    }

    public IntegerProperty shipsRemainingProperty() {
        return shipsRemaining;
    }

    public void decreaseShipsRemaining() {
        this.shipsRemaining.set(shipsRemaining.get()-1);
    }
}
