package it.rmtz.matrix;

/**
 * Created by Nicol� Tagliaferro
 */

public class Step {
    Matrix.Direction direction;
    Step next;

    Step() {

    }

    Step(Matrix.Direction direction) {
        this.direction = direction;
    }

    Step(Matrix.Direction d, Step next) {
        direction = d;
        this.next = next;
    }
}
