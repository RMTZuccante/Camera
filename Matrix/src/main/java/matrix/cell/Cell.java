package matrix.cell;

public class Cell {
    protected Cell north, south, east, west;
    public boolean visited;
    protected boolean mirror, black;
    protected Victim victim;
    public boolean considered = false; //Needed for pathinding not to pass on the same cell while looking
    public int weight;

    public enum Victim {
        NONE,
        H,
        S,
        U,
        HEAT
    }

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

    public void unset() {
        if (north != null)
            north = north.south = null;
        if (south != null)
            south = south.north = null;
        if (east != null)
            east = east.west = null;
        if (west != null)
            west = west.east = null;
    }

    public boolean isMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
    }

    public boolean isBlack() {
        return black;
    }

    public void setBlack(boolean black) {
        this.black = black;
    }

    public Victim getVictim() {
        return victim;
    }

    public void setVictim(Victim victim) {
        this.victim = victim;
    }

    public Cell getNorth() {
        return north;
    }

    public void setNorth(Cell north) {
        this.north = north;
    }

    public Cell getSouth() {
        return south;
    }

    public void setSouth(Cell south) {
        this.south = south;
    }

    public Cell getEast() {
        return east;
    }

    public void setEast(Cell east) {
        this.east = east;
    }

    public Cell getWest() {
        return west;
    }

    public void setWest(Cell west) {
        this.west = west;
    }
}
