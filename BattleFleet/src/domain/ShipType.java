package domain;

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public enum ShipType {
    CARRIER(1, 5, 500, 1000, Color.DARKORANGE),
    BATTLESHIP(2, 4, 250, 500, Color.DODGERBLUE),
    CRUISER(3, 3, 100, 250, Color.INDIGO),
    SUBMARINE(4, 3, 100, 0, Color.DARKOLIVEGREEN),
    DESTROYER(5, 2, 50, 0, Color.SILVER);

    private final int index;
    private final int size;
    private final int hitReward;
    private final int sinkBonus;
    private final Color color;
    private static Map mapping = new HashMap<>();

    ShipType(int index, int size, int hitReward, int sinkBonus, Color color) {
        this.index = index;
        this.size = size;
        this.hitReward = hitReward;
        this.sinkBonus = sinkBonus;
        this.color = color;
    }

    static {
        for (ShipType shipType : ShipType.values()) {
            mapping.put(shipType.index, shipType);
        }
    }

    public static ShipType indexOf(int shipType) {
        return (ShipType) mapping.get(shipType);
    }



    public int getSize() {
        return size;
    }

    public int getHitReward() {
        return hitReward;
    }

    public int getSinkBonus() {
        return sinkBonus;
    }

    public Color getColor() {
        return color;
    }
}

