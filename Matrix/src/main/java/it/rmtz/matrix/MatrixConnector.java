/**
 * Created by Nicolò Tagliaferro
 */

public abstract class MatrixConnector {

    int DFRONT1, DFRONT2, DRIGHT, DLEFT, DBACK; //Indexes of response of getDistances
    int MIRROR, WHITE; //Values returned by getColor
    int TLEFT, TRIGHT; //Indexes of response of getTemps
    int GOBLACK, GOOBSTACLE; //Values returned by go

    abstract boolean handShake();

    abstract void rotate(int angle);

    abstract int go();

    abstract void victim(int packets);

    abstract int[] getDistances();

    abstract int getColor();

    abstract float[] getTemps();
}
