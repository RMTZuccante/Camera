package matrix.cell;

import matrix.cartesian.Plane;
import matrix.cell.Cell;

public class RisingCell extends Cell {
    private Plane[] floors = new Plane[2];

    public RisingCell(Cell c, Plane plane) {
        super(c.north, c.south, c.east, c.west);
        victim = c.victim;
        visited = c.visited;
        mirror = c.mirror;
        black = c.black;
        weight = 20;
        if (north != null) north.south = this;
        if (south != null) south.north = this;
        if (east != null) east.west = this;
        if (west != null) west.east = this;
        visited = true;
        floors[0] = plane;
    }

    public Plane getOtherFloor(Plane actual) {
        return floors[0] == actual ? floors[1] : floors[0];
    }

    public void setNewFloor(Plane floor) {
        if (floor != floors[0]) floors[1] = floor;
        else throw new RuntimeException("Given floor is the same as starting floor");
    }

    public String getFloorId(Plane floor) {
        return this.hashCode() + "" + (floor == floors[0] ? 0 : 1);
    }

    @Override
    public void setVictim(Victim victim) {

    }

    @Override
    public void setBlack(boolean black) {

    }

    @Override
    public void setMirror(boolean mirror) {

    }

    @Override
    public void setWest(Cell w) {
        if ((east == north && north == south && south == west && west == null) || east != null) super.setWest(w);
    }

    @Override
    public void setEast(Cell e) {
        if ((east == north && north == south && south == west && west == null) || west != null) super.setEast(e);
    }

    @Override
    public void setSouth(Cell s) {
        if ((east == north && north == south && south == west && west == null) || north != null) super.setSouth(s);
    }

    @Override
    public void setNorth(Cell n) {
        if ((east == north && north == south && south == west && west == null) || south != null) super.setNorth(n);
    }
}
