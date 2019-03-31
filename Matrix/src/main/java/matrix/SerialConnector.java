package matrix;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class SerialConnector {
    private final static byte HANDSHAkE = 1, ROTATE = 2, GO = 3, GETDISTANCES = 4, GETCOLOR = 5, GETTEMPS = 6, VICTIM = 7, SETDEBUG = 8, SETBLACK = 9, RESET = 10;
    private final static byte STX = 2, ETX = 3, RES = -128, READY = 8;
    public final static int GOBLACK = 1, GOOBSTACLE = 2, GORISE = 3;

    private byte[] buffer = new byte[10];
    /* jSerialComm page http://fazecast.github.io/jSerialComm/ */
    private SerialPort stm;
    private boolean ready;
    private byte status;
    private byte toRead;

    public SerialConnector(SerialPort stm, int baudRate) {
        this.stm = stm;
        this.ready = false;
        this.status = -1;

        /*Try opening port*/
        stm.setBaudRate(baudRate);
        if (!stm.openPort()) {
            System.err.println("Cannot open port " + stm.getSystemPortName());
            System.exit(-1);
        }
    }

    private void enableEvents() {
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
                    if (buf[0] == STX) {
                        stm.readBytes(buf, 1);
                        StringBuilder toPrint = new StringBuilder();
                        while (buf[0] != ETX) {
                            toPrint.append((char) buf[0]);
                            stm.readBytes(buf, 1);
                        }
                        System.out.print(toPrint);
                    } else {
                        if (buf[0] == READY) ready = true;
                        else {
                            status = (byte) ((buf[0] & RES) == RES ? buf[0] ^ RES : -buf[0]);
                            if (toRead > 0) {
                                stm.readBytes(buffer, toRead);
                                toRead = 0;
                            }
                        }
                        synchronized (serialConnector) {
                            serialConnector.notify();
                        }
                    }
                }
            }
        });
    }

    /**
     * Send the HANDSHAKE command and a number, the expected response is 2 times the number
     */
    public synchronized boolean handShake() {
        stm.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 500, 0);
        Random r = new Random();
        byte[] b = new byte[1];
        r.nextBytes(b);
        buffer[0] = HANDSHAkE;
        buffer[1] = b[0];
        stm.writeBytes(buffer, 2);
        int recv = stm.readBytes(buffer, 1);
        stm.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
        boolean success = recv == 1 && buffer[0] == (byte) (b[0] * 2);
        return success;
    }

    public synchronized void rotate(int angle) {
        boolean rot = true;
        if (angle < 0) {
            rot = false;
            angle = -angle;
        }
        rotate(angle, rot);
    }

    public synchronized void rotate(int angle, boolean right) {
        waitReady();
        buffer[0] = ROTATE;
        buffer[1] = (byte) (right ? 0 : 1);
        buffer[2] = (byte) angle;
        stm.writeBytes(buffer, 3);
        waitResult();
    }

    public synchronized int go() {
        waitReady();
        buffer[0] = GO;
        stm.writeBytes(buffer, 1);
        return waitResult();
    }

    public synchronized void victim(int packets) {
        waitReady();
        buffer[0] = VICTIM;
        buffer[1] = (byte) packets;
        stm.writeBytes(buffer, 2);
        waitResult();
    }

    public synchronized Distances getDistances() {
        waitReady();
        toRead = 10;
        buffer[0] = GETDISTANCES;
        stm.writeBytes(buffer, 1);
        waitFor(GETDISTANCES);

        short[] arr = new short[5];
        for (int i = 0; i < 5; i++)
            arr[i] = ByteBuffer.wrap(buffer, 2 * i, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return new Distances(arr[0], arr[3], arr[2], arr[1], arr[4]);
    }

    public synchronized Color getColor() {
        waitReady();
        toRead = 8;
        buffer[0] = GETCOLOR;
        stm.writeBytes(buffer, 1);
        waitFor(GETCOLOR);

        short[] arr = new short[4];
        for (int i = 0; i < 4; i++)
            arr[i] = ByteBuffer.wrap(buffer, 2 * i, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
        return new Color(Short.toUnsignedInt(arr[0]), Short.toUnsignedInt(arr[1]), Short.toUnsignedInt(arr[2]), Short.toUnsignedInt(arr[3]));
    }

    public synchronized Temps getTemps() {
        waitReady();
        toRead = 8;
        buffer[0] = GETTEMPS;
        stm.writeBytes(buffer, 1);
        waitFor(GETTEMPS);

        float[] arr = new float[2];
        for (int i = 0; i < 2; i++)
            arr[i] = ByteBuffer.wrap(buffer, 4 * i, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        return new Temps(arr[0], arr[1]);
    }

    public synchronized void setDebug(byte level) {
        waitReady();
        buffer[0] = SETDEBUG;
        buffer[1] = level;
        stm.writeBytes(buffer, 2);
    }

    public synchronized void setBlackThreshold(byte blackThreshold) {
        waitReady();
        buffer[0] = SETBLACK;
        buffer[1] = blackThreshold;
        stm.writeBytes(buffer, 2);
    }

    public synchronized void reset() {
        waitReady();
        buffer[0] = RESET;
        stm.writeBytes(buffer, 1);
    }

    private synchronized void waitFor(byte purpose) {
        while (status != -purpose) waitTC();
        status = -1;
    }

    private synchronized void waitReady() {
        while (!ready) waitTC();
        ready = false;
    }

    private synchronized byte waitResult() {
        while (status < 0) waitTC();
        byte out = status;
        status = -1;
        return out;
    }

    private synchronized void waitTC() {
        try {
            wait();
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the robot to be ready...");
        }
    }

    public String getConnectionInfo() {
        return "Port: " + stm.getSystemPortName() + "\nBaud: " + stm.getBaudRate();
    }

    public class Distances {
        private short frontL;
        private short frontR;
        private short left;
        private short right;
        private short back;

        public Distances(short frontL, short frontR, short left, short right, short back) {
            this.frontL = frontL;
            this.frontR = frontR;
            this.left = left;
            this.right = right;
            this.back = back;
        }

        public short getFrontL() {
            return frontL;
        }

        public short getFrontR() {
            return frontR;
        }

        public short getLeft() {
            return left;
        }

        public short getRight() {
            return right;
        }

        public short getBack() {
            return back;
        }

        @Override
        public String toString() {
            return "Distances{" +
                    "frontL=" + frontL +
                    ", frontR=" + frontR +
                    ", left=" + left +
                    ", right=" + right +
                    ", back=" + back +
                    '}';
        }
    }

    public class Temps {
        private float left;
        private float right;

        public Temps(float left, float right) {
            this.left = left;
            this.right = right;
        }

        public float getLeft() {
            return left;
        }

        public float getRight() {
            return right;
        }

        @Override
        public String toString() {
            return "Temps{" +
                    "left=" + left +
                    ", right=" + right +
                    '}';
        }
    }

    public class Color {
        private int red;
        private int green;
        private int blue;
        private int ambient;

        public Color(int red, int green, int blue, int ambient) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.ambient = ambient;
        }

        public int getRed() {
            return red;
        }

        public int getGreen() {
            return green;
        }

        public int getBlue() {
            return blue;
        }

        public int getAmbient() {
            return ambient;
        }

        @Override
        public String toString() {
            return "Color{" +
                    "red=" + red +
                    ", green=" + green +
                    ", blue=" + blue +
                    ", ambient=" + ambient +
                    '}';
        }
    }
}
