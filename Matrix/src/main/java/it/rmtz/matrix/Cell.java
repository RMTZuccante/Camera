package it.rmtz.matrix;

public class Cell {
    Cell north, south, east, west;
    boolean visited = false, mirror = false, black = false, victim = false;
    boolean considered = false;
    int weight = 5;

    Cell() {
        north = south = east = west = null;
    }

    public Cell(Cell north, Cell south, Cell east, Cell west) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }
}
