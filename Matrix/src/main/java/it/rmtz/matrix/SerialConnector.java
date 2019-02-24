package it.rmtz.matrix;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Created by Nicolò Tagliaferro
 */

public class SerialConnector extends MatrixConnector {
    final byte HANDSHAkE = 1, ROTATE = 2, GETDISTANCES = 4, GETCOLOR = 5, GETTEMPS = 6;
    byte[] buffer = new byte[8];

    int DFRONT1 = 0, DFRONT2 = 1, DRIGHT = 3, DLEFT = 4, DBACK = 5;
    int MIRROR = 1, WHITE = 0;
    int TLEFT = 0, TRIGHT = 0;
    int GOBLACK = 1, GOOBSTACLE = 2;

    /* jSerialComm page http://fazecast.github.io/jSerialComm/ */
    private SerialPort esp;

    SerialConnector(SerialPort esp) {
        this.esp = esp;
        /*Try opening port*/
        if (!esp.openPort()) {
            System.err.println("Cannot open port " + esp.getSystemPortName());
            System.exit(-1);
        }
        /*Set port read to blocking, 0 = unlimited timeout, see https://github.com/Fazecast/jSerialComm/wiki/Blocking-and-Semiblocking-Reading-Usage-Example*/
        esp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
    }

    @Override
    /**
     * Send the HANDSHAKE command and a number, the expected response is 2 times the number
     */
    boolean handShake() {
        esp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 500, 0);
        byte n = 24;
        buffer[0] = HANDSHAkE;
        buffer[1] = n;
        esp.writeBytes(buffer, 2);
        int recv = esp.readBytes(buffer, 1);
        esp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        if (recv == 1 && buffer[0] == (byte) (n * 2))
            return true;
        else
            return false;
    }

    @Override
    void rotate(int angle) {
        boolean rot = true;
        if (angle < 0) {
            rot = false;
            angle = -angle;
        }
        rotate(angle, rot);
    }

    void rotate(int angle, boolean right) {
        buffer[0] = ROTATE;
        if (right) buffer[1] = 0;//Turn right
        else buffer[0] = 1; //Turn left
        buffer[2] = (byte) angle;
        esp.writeBytes(buffer, 3);
        esp.readBytes(buffer, 1);
    }

    @Override
    int go() {
        return 0;
    }

    @Override
    int[] getDistances() {
        return new int[0];
    }

    @Override
    int getColor() {
        return 0;
    }

    @Override
    float[] getTemps() {
        return new float[0];
    }
}
