package it.rmtz.matrix;

/**
 * Created by Nicolò Tagliaferro
 */

public abstract class MatrixConnector {

    abstract boolean handShake();

    abstract void waitForReady();

    abstract void rotate(int angle);

    abstract int go();

    abstract void victim(int packets);
    
    abstract short[] getDistances();

    abstract int getColor();

    abstract float[] getTemps();
}
