package matrix;

public class Cell {
    Cell north, south, east, west;
    boolean visited, mirror, black;
    Victim victim;
    boolean considered = false; //Needed for pathinding not to pass on the same cell while looking
    int weight;

    public Cell() {
        north = south = east = west = null;
        visited = mirror = black = false;
        victim = Victim.NONE;
        weight = 5;
    }

    public Cell(Cell north, Cell south, Cell east, Cell west) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }

    public enum Victim {
        NONE,
        H,
        S,
        U,
        HEAT
    }
}
