package domain;

import customExceptions.CustomException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.*;

public class Game {
    public final int NUMBER_OF_SHIPS = 5;
    public int shipsToPlace = NUMBER_OF_SHIPS;
    public final int MAXIMUM_MOVES = 40;

    private final Player humanPlayer;
    private final Player botPlayer;
    private GameState state;

    public StringProperty notification;
    public int humanShipsRemaining = 5;
    public int computerShipsRemaining = 5;

    //------ Fields for shooting AI algorithm -------------------//
    private AI_ShootingPhase phase;
    private boolean leftOrTop = true;
    private boolean vertical = true;
    private boolean directionChange = false;
    // A list to hold the ship's location after successful hits
    private List<Tile> shipCoordinates = new ArrayList<>();
    // A list that holds the target Tile's neighbours by order: left -> top -> right -> bottom
    private List<Tile> targetNeighbourTiles = new ArrayList<>();


    public Game(Player human, Player computer) {
        this.humanPlayer = human;
        this.botPlayer = computer;
        this.state = GameState.PLACING_SHIPS;
        this.phase = AI_ShootingPhase.SHOOT_RANDOM;
        this.notification = new SimpleStringProperty("System Notifications");
    }

    public EventHandler<MouseEvent> getShootingHandler() {
        return event -> {
            if (this.state != GameState.STARTED || !humanPlayer.isTurnToMove()) return;
            if (humanPlayer.getMoves().size() == MAXIMUM_MOVES){
                String winner, totalpoints;
                int humanPoints = botPlayer.getPlayerBoard().getTotalPoints();
                int computerPoints = humanPlayer.getPlayerBoard().getTotalPoints();
                if(humanPoints > computerPoints) {
                    winner = humanPlayer.toString();
                    totalpoints = String.valueOf(humanPoints);
                } else  {
                    winner = botPlayer.toString();
                    totalpoints = String.valueOf(botPlayer);
                }
                setNotification("Game has finished after " + MAXIMUM_MOVES + " moves!" +
                        "Winner:" + winner + " with total points:" + totalpoints);
                finish();
            }
            Tile tile = (Tile) event.getSource();
            if (tile.isHit()) return;
            humanPlayer.addMoveToList(Move.create(humanPlayer, tile, tile.shootTile()));
            System.out.println("Player shoots: " + tile.toString());
//            if (botPlayer.getPlayerBoard().getShipsRemaining() < computerShipsRemaining) {
//
//            }
            humanPlayer.setTurnToMove(false);
            botPlayer.setTurnToMove(true);
            if (botPlayer.getPlayerBoard().getShipsRemaining() == 0) {
                setNotification("YOU WIN");
                botPlayer.setTurnToMove(true);
                finish();
            }
            new Timer().schedule(botShooting(),500);
        };
    }

    public EventHandler<MouseEvent> getPlacingHandler() {
        return event -> {

            if (this.state != GameState.PLACING_SHIPS) return;
            Tile tile = (Tile) event.getTarget();
            Board board = humanPlayer.getPlayerBoard();
            try {
                if (!board.validateAndPlaceShip(new Ship(shipsToPlace, board), tile.get_X(), tile.get_Y(),
                        event.getButton() == MouseButton.PRIMARY, false)) {
                    return;
                }
            } catch (CustomException e) {
                e.printStackTrace();
            }
            if (--shipsToPlace == 0) {
                setNotification("Player Ready!");
                shipsToPlace = NUMBER_OF_SHIPS;
                // Place ships for enemy
                try {
                    botPlacingShips();
                } catch (CustomException e) {
                    e.printStackTrace();
                }
                start();
            }
        };
    }

    private void botPlacingShips() throws CustomException {
        while (true) {
            Board board = botPlayer.getPlayerBoard();
            if(board.validateAndPlaceShip( new Ship(shipsToPlace, board),
                    new Random().nextInt(10),
                    new Random().nextInt(10),
                    new Random().nextBoolean(), false)) {
                System.out.println("Placing successful");
                if (--shipsToPlace == 0) break;
            }
        }
    }

    public TimerTask botShooting() {
        return new TimerTask() {
            @Override
            public void run() {
                while (botPlayer.isTurnToMove()) {
                    Tile tile = selectNextTarget();
                    System.out.println("Computer shoots: " + tile.toString());
                    if (tile.isHit()) {

                        continue;
                    }
                    if(tile.shootTile()) {
                        botPlayer.addMoveToList(Move.create(botPlayer, tile, true));
                        shipCoordinates.add(tile);
                        if (phase != AI_ShootingPhase.SHOOT_WITH_DIRECTION) {
                            phase = phase.next();
                        }
                    }
                    else botPlayer.addMoveToList(Move.create(botPlayer, tile, false));
                    // If a ship a is destroyed
                    if (humanPlayer.getPlayerBoard().getShipsRemaining() < humanShipsRemaining) {
                        for(Tile t : shipCoordinates) {
                            t.setFill(Color.BLACK);
                            // Set surrounding tiles as hit and color gray
                            List<Tile> neighbours = humanPlayer.getPlayerBoard().getNeighbours(t.get_X(), t.get_Y(), true);
                            for(Tile n : neighbours) {
                                if(!t.getFill().equals(Color.BLACK)) n.setFill(Color.GRAY);
                                n.setHit(true);
                            }
                        }
                        phase = AI_ShootingPhase.SHOOT_RANDOM;
                        shipCoordinates.clear();
                        targetNeighbourTiles.clear();
                        humanShipsRemaining--;
                    }
                    // If all ships are destroyed
                    if (humanShipsRemaining == 0) {
                        setNotification("YOU LOSE!!!");
                        finish();
                    }
                    botPlayer.setTurnToMove(false);
                    humanPlayer.setTurnToMove(true);
                }
            }
        };
    }

    // Shooting Logic
    private Tile selectNextTarget() {
        Board board = humanPlayer.getPlayerBoard();
        Tile tile;
        switch (phase) {
            case SHOOT_RANDOM:
                return board.getTile(new Random().nextInt(10),
                                            new Random().nextInt(10));
            case SHOOT_NEIGHBOURS:
                System.out.println(shipCoordinates);
                if (targetNeighbourTiles.isEmpty()) {
                    tile = shipCoordinates.get(0); // first successful hit
                    int x = tile.get_X();
                    int y = tile.get_Y();
                    targetNeighbourTiles = board.getNeighbours(x, y, false);
                }
                // hit a random neighbour and remove from list
                int randNeighbourIndex = new Random().nextInt(targetNeighbourTiles.size());
                tile = targetNeighbourTiles.get(randNeighbourIndex);
                targetNeighbourTiles.remove(randNeighbourIndex);
                return tile;
            case SHOOT_WITH_ORIENTATION:
                System.out.println(shipCoordinates);
                tile = shipCoordinates.get(shipCoordinates.size() - 1); // Get last target
                if (shipCoordinates.get(0).get_X() == shipCoordinates.get(shipCoordinates.size() - 1).get_X()) {
                    // ship is placed vertical
                    vertical = true;
                    if (shipCoordinates.get(0).get_Y() > shipCoordinates.get(shipCoordinates.size() - 1).get_Y()) {
                        // continue top if it exists
                        leftOrTop = true;
                        if (board.isValidPoint(tile.get_X(), tile.get_Y() - 1 )) {
                            tile = board.getTile(tile.get_X(), tile.get_Y() - 1 );
                            if (tile.isHit()) { // if hit ...
                                directionChange = true;
                                leftOrTop = false;
                                phase.next();
                                tile = shipCoordinates.get(0);
                                return board.getTile(tile.get_X(), tile.get_Y() + 1);
                            }
                        } else { // ...or if not valid -> change direction: bottom
                            directionChange = true;
                            leftOrTop = false;
                            phase.next();
                            tile = shipCoordinates.get(0);
                            return board.getTile(tile.get_X(), tile.get_Y() + 1);
                        }
                    }else {
                        leftOrTop = false;
                        // continue bottom if it exists
                        if (board.isValidPoint(tile.get_X(), tile.get_Y() + 1 )) {
                            tile = board.getTile(tile.get_X(), tile.get_Y() + 1 );
                            if (tile.isHit()) { // if hit ...
                                directionChange = true;
                                leftOrTop = true;
                                phase.next();
                                tile = shipCoordinates.get(0);
                                return board.getTile(tile.get_X(), tile.get_Y() - 1);
                            }
                        } else { // ...or if not valid -> chdtion: top
                            directionChange = true;
                            leftOrTop = true;
                            phase.next();
                            tile = shipCoordinates.get(0);
                            return board.getTile(tile.get_X(), tile.get_Y() - 1);
                        }
                    }
                }
                else {
                    // ship is placed horizontal
                    vertical = false;
                    if (shipCoordinates.get(0).get_X() > shipCoordinates.get(shipCoordinates.size() - 1).get_X()) {
                        // continue left if exists
                        leftOrTop = true;
                        if (board.isValidPoint(tile.get_X() - 1, tile.get_Y())) {
                            tile = board.getTile(tile.get_X() - 1, tile.get_Y());
                            if (tile.isHit()) { // if hit ...
                                directionChange = true;
                                leftOrTop = false;
                                phase.next();
                                tile = shipCoordinates.get(0);
                                return board.getTile(tile.get_X() + 1, tile.get_Y());
                            }
                        } else { // ...or if not valid -> change direction: right
                            directionChange = true;
                            leftOrTop = false;
                            phase.next();
                            tile = shipCoordinates.get(0);
                            return board.getTile(tile.get_X() + 1, tile.get_Y());
                        }
                    } else {
                        // continue right if exists
                        leftOrTop = false;
                        if (board.isValidPoint(tile.get_X() + 1, tile.get_Y())) {
                            tile = board.getTile(tile.get_X() + 1, tile.get_Y());
                            if (tile.isHit()) { // if hit ...
                                directionChange = true;
                                leftOrTop = true;
                                phase.next();
                                tile = shipCoordinates.get(0);
                                return board.getTile(tile.get_X() - 1, tile.get_Y());
                            }
                        } else { // ...or if not valid -> change direction: left
                            directionChange = true;
                            leftOrTop = true;
                            phase.next();
                            tile = shipCoordinates.get(0);
                            return board.getTile(tile.get_X() - 1, tile.get_Y());
                        }
                    }
                }
            case SHOOT_WITH_DIRECTION:
                System.out.println("Phase: " + phase + "> Vertical: " + vertical
                        + ", TopOrLeft: " + leftOrTop + ", changedDirection:" + directionChange);
                for(Tile t : shipCoordinates) System.out.println(shipCoordinates.indexOf(t) + ". " + t.toString());
                // if there is a direction change start from first successful hit
                int index;
                if(directionChange) {
                    index = 0;
                    directionChange = false;
                }
                else index = shipCoordinates.size() - 1;
                tile = shipCoordinates.get(index);
                if (vertical) {
                    // try top
                    if(leftOrTop) {
                        if(board.isValidPoint(tile.get_X(), tile.get_Y() - 1))
                            return board.getTile(tile.get_X(), tile.get_Y() - 1);
                    }
                    // try bottom
                    else {
                        if(board.isValidPoint(tile.get_X(), tile.get_Y() + 1))
                            return board.getTile(tile.get_X(), tile.get_Y() + 1);
                    }
                } else {
                    // try left
                    if(leftOrTop) {
                        if(board.isValidPoint(tile.get_X() - 1, tile.get_Y()))
                            return board.getTile(tile.get_X() - 1, tile.get_Y());
                    }
                    // try right
                    else {
                        if(board.isValidPoint(tile.get_X() + 1, tile.get_Y()))
                            return board.getTile(tile.get_X() + 1, tile.get_Y());
                    }
                }
                // if end of ship or bound go to the other direction
                leftOrTop = !leftOrTop;
                directionChange = true;
                return tile = shipCoordinates.get(0);
            default:
                throw new IllegalStateException("Unexpected value: " + phase);
        }
    }

    public void start() {
        // Determine player turns
        if (new Random().nextBoolean()) {
            humanPlayer.setTurnToMove(true);
            setNotification(humanPlayer.toString() + " has the first move!");
        } else {
            botPlayer.setTurnToMove(true);
            new Timer().schedule(botShooting(),100);
            setNotification(botPlayer.toString() + " has the first move!");
        }
        this.state = GameState.STARTED;

    }

    public void finish() {
        this.state = GameState.FINISHED;
    }

    public String getNotification() {
        return notification.get();
    }

    public StringProperty notificationProperty() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification.set(notification);
    }

    public enum GameState {
        PLACING_SHIPS, STARTED, FINISHED
    }

    public GameState getState() {
        return state;
    }

    private enum AI_ShootingPhase {
        SHOOT_RANDOM, SHOOT_NEIGHBOURS, SHOOT_WITH_ORIENTATION, SHOOT_WITH_DIRECTION;

        private static final AI_ShootingPhase[] phase = values();
        public AI_ShootingPhase next() {
            return phase[(this.ordinal() + 1)%phase.length];
        }
    }
}
