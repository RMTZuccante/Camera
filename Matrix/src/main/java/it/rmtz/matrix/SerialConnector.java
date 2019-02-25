package it.rmtz.matrix;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Nicolò Tagliaferro
 */

public class SerialConnector extends MatrixConnector {
    final byte HANDSHAkE = 1, ROTATE = 2, GO = 3, GETDISTANCES = 4, GETCOLOR = 5, GETTEMPS = 6, VICTIM = 7, READY = 8;
    byte[] buffer = new byte[10];

    int DFRONT1 = 0, DFRONT2 = 1, DRIGHT = 3, DLEFT = 4, DBACK = 5;
    int MIRROR = 1, WHITE = 0;
    int TLEFT = 0, TRIGHT = 0;
    int GOBLACK = 1, GOOBSTACLE = 2;

    /* jSerialComm page http://fazecast.github.io/jSerialComm/ */
    private SerialPort stm;

    SerialConnector(SerialPort stm, int baudRate) {
        this.stm = stm;
        /*Try opening port*/
        stm.setBaudRate(baudRate);
        if (!stm.openPort()) {
            System.err.println("Cannot open port " + stm.getSystemPortName());
            System.exit(-1);
        }
        /*Set port read to blocking, 0 = unlimited timeout, see https://github.com/Fazecast/jSerialComm/wiki/Blocking-and-Semiblocking-Reading-Usage-Example*/
        stm.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
    }

    SerialPortDataListener listener = new SerialPortDataListener() {
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            System.out.print(new String(event.getReceivedData()));
        }
    };

    void enableEvents() {
        stm.addDataListener(listener);
    }

    void disableEvents() {
        stm.removeDataListener();
    }

    @Override
    /**
     * Send the HANDSHAKE command and a number, the expected response is 2 times the number
     */
    boolean handShake() {
        stm.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 500, 0);
        byte n = 24;
        buffer[0] = HANDSHAkE;
        buffer[1] = n;
        stm.writeBytes(buffer, 2);
        int recv = stm.readBytes(buffer, 1);
        stm.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        boolean success = recv == 1 && buffer[0] == (byte) (n * 2);
        if(success) enableEvents();
        return success;
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
        buffer[1] = (byte) (right?1:0);
        buffer[2] = (byte) angle;
        stm.writeBytes(buffer, 3);
        disableEvents();
        stm.readBytes(buffer, 1);
        enableEvents();
    }

    @Override
    int go() {
        buffer[0] = GO;
        stm.writeBytes(buffer,1);
        disableEvents();
        stm.readBytes(buffer,1);
        enableEvents();
        return buffer[0];
    }

    @Override
    void victim(int packets) {
        buffer[0] = VICTIM;
        buffer[1] = (byte) packets;
        stm.writeBytes(buffer,2);
        disableEvents();
        stm.readBytes(buffer,1);
        enableEvents();
    }

    @Override
    short[] getDistances() {
        int length = 2;
        int num = 5;
        buffer[0] = GETDISTANCES;
        stm.writeBytes(buffer, 1);
        disableEvents();
        stm.readBytes(buffer, length*num);
        enableEvents();
        short[] arr = new short[num];
        for (int i = 0; i < num; i++) arr[i] = ByteBuffer.wrap(buffer, length*i,length).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return arr;
    }

    @Override
    int getColor() {
        buffer[0] = GETCOLOR;
        stm.writeBytes(buffer,1);
        disableEvents();
        stm.readBytes(buffer,1);
        enableEvents();
        return buffer[0];
    }

    @Override
    float[] getTemps() {
        int length = 4;
        int num = 2;
        buffer[0] = GETTEMPS;
        stm.writeBytes(buffer,1);
        disableEvents();
        stm.readBytes(buffer, length*num);
        enableEvents();
        float[] arr = new float[num];
        for (int i = 0; i < num; i++) arr[i] = ByteBuffer.wrap(buffer, length*i,length).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        return arr;
    }

    void waitForReady() {
        buffer[0] = 0;
        while(buffer[0]!= READY) stm.readBytes(buffer,1);
    }

    String getConnectionInfo() {
        return "Port: "+stm.getSystemPortName() + "\nBaud: "+stm.getBaudRate();
    }
}

