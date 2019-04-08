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
}
