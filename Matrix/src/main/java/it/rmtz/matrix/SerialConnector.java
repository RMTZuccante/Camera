package it.rmtz.matrix;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Nicolò Tagliaferro
 */

public class SerialConnector {
    final byte HANDSHAkE = 1, ROTATE = 2, GO = 3, GETDISTANCES = 4, GETCOLOR = 5, GETTEMPS = 6, VICTIM = 7, SETDEBUG = 8;
    final byte STX = 2, ETX = 3, RES = -128, READY = 8;
    byte[] buffer = new byte[10];

    int DFRONTL = 0, DFRONTR = 1, DRIGHT = 3, DLEFT = 2, DBACK = 4;
    int MIRROR = 1, WHITE = 0;
    int TLEFT = 0, TRIGHT = 1;
    int GOBLACK = 1, GOOBSTACLE = 2;

    /* jSerialComm page http://fazecast.github.io/jSerialComm/ */
    private SerialPort stm;
    private byte result;
    private boolean ready;

    public SerialConnector(SerialPort stm, int baudRate) {
        this.stm = stm;
        this.ready = false;

        /*Try opening port*/
        stm.setBaudRate(baudRate);
        if (!stm.openPort()) {
            System.err.println("Cannot open port " + stm.getSystemPortName());
            System.exit(-1);
        }
    }

    void enableEvents() {
        SerialConnector serialConnector = this;
        stm.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                byte[] buf = new byte[1];

                while (stm.bytesAvailable() > 0) {
                    stm.readBytes(buf, 1);
                    if (buf[0] == READY) {
                        ready = true;
                        synchronized (serialConnector) {
                            serialConnector.notify();
                        }
                    } else if (buf[0] == STX) {
                        stm.readBytes(buf, 1);
                        StringBuilder toPrint = new StringBuilder();
                        while (buf[0] != ETX) {
                            toPrint.append((char) buf[0]);
                            stm.readBytes(buf, 1);
                        }
                        System.out.print(toPrint);
                    } else if ((buf[0] & RES) == RES) {
                        result = (byte) (buf[0] ^ RES);
                        synchronized (serialConnector) {
                            serialConnector.notify();
                        }
                    }

                }
            }
        });
    }

    void disableEvents() {
        stm.removeDataListener();
    }

    /**
     * Send the HANDSHAKE command and a number, the expected response is 2 times the number
     */
    synchronized boolean handShake() {
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

    synchronized void rotate(int angle) {
        boolean rot = true;
        if (angle < 0) {
            rot = false;
            angle = -angle;
        }
        rotate(angle, rot);
    }

    synchronized void rotate(int angle, boolean right) {
        buffer[0] = ROTATE;
        buffer[1] = (byte) (right ? 0 : 1);
        buffer[2] = (byte) angle;
        stm.writeBytes(buffer, 3);
        try {
            wait();
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for rotation end...");
        }
    }

    synchronized int go() {
        buffer[0] = GO;
        stm.writeBytes(buffer,1);
        try {
            wait();
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for rotation end...");
        }
        return result;
    }

    synchronized void victim(int packets) {
        buffer[0] = VICTIM;
        buffer[1] = (byte) packets;
        stm.writeBytes(buffer,2);
        try {
            wait();
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for rotation end...");
        }
    }

    synchronized short[] getDistances() {
        disableEvents();
        int length = 2;
        int num = 5;
        buffer[0] = GETDISTANCES;
        stm.writeBytes(buffer, 1);
        stm.readBytes(buffer, length * num);
        enableEvents();
        short[] arr = new short[num];
        for (int i = 0; i < num; i++) arr[i] = ByteBuffer.wrap(buffer, length * i, length).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return arr;
    }

    synchronized int getColor() {
        disableEvents();
        buffer[0] = GETCOLOR;
        stm.writeBytes(buffer, 1);
        stm.readBytes(buffer, 1);
        enableEvents();
        return buffer[0];
    }

    synchronized float[] getTemps() {
        int length = 4;
        int num = 2;
        disableEvents();
        buffer[0] = GETTEMPS;
        stm.writeBytes(buffer, 1);
        stm.readBytes(buffer, length * num);
        enableEvents();
        float[] arr = new float[num];
        for (int i = 0; i < num; i++) arr[i] = ByteBuffer.wrap(buffer, length * i, length).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        return arr;
    }

    synchronized void setDebug(byte level) {
        buffer[0] = SETDEBUG;
        buffer[1] = level;
        stm.writeBytes(buffer,2);
    }

    synchronized void waitForReady() {
        while (!ready) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Error while waiting for the robot to be ready...");
            }
        }
    }

    String getConnectionInfo() {
        return "Port: " + stm.getSystemPortName() + "\nBaud: " + stm.getBaudRate();
    }
}
