package domain;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private boolean bot;
    private Board playerBoard;
    private List<Move> moves = new ArrayList<>();
    private int successfulHits = 0;
    private boolean turnToMove = false;
    private IntegerProperty pointsProperty = new SimpleIntegerProperty(0);
    private DoubleProperty accuracyProperty = new SimpleDoubleProperty(0.0);
    private double accuracy = 0.0;

    public Player(String name, boolean bot) {
        this.name = name;
        this.bot = bot;
    }

    public boolean isBot() {
        return bot;
    }

    public Board getPlayerBoard() {
        return playerBoard;
    }

    public void setPlayerBoard(Board playerBoard) {
        this.playerBoard = playerBoard;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public void addMoveToList(Move move) {
        moves.add(move);
    }

    public void setTurnToMove(boolean turnToMove) {
        this.turnToMove = turnToMove;
    }

    public boolean isTurnToMove() {
        return turnToMove;
    }

    public int getPointsProperty() {
        return pointsProperty.get();
    }

    public IntegerProperty pointsProperty() {
        return pointsProperty;
    }

    public void setPointsProperty(int pointsProperty) {
        this.pointsProperty.set(pointsProperty);
    }

    public double getAccuracyProperty() {
        return accuracyProperty.get();
    }

    public DoubleProperty accuracyProperty() {
        return accuracyProperty;
    }

    public void setAccuracyProperty(double accuracyProperty) {
        this.accuracyProperty.set(accuracyProperty);
    }

    public double getAccuracy() {
        return accuracy;
    }

    public int getSuccessfulHits() {
        return successfulHits;
    }

    public void increaseSuccessfulHits() {
        this.successfulHits++;
    }

    public void updateAccuracy() {
        if(this.getMoves().isEmpty()) return;
        double success = getSuccessfulHits();
        double total = this.getMoves().size();
        double accuracy = success/total;
        this.accuracy = Math.round(accuracy*100);
    }

    @Override
    public String toString() {
        return "Player " + name + '{' + (this.isBot()?"computer":"human") + '}';
    }
}
