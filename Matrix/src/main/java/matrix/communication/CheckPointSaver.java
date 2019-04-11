package matrix.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utils.Utils.RMTZ_LOGGER;

public class CheckPointSaver extends Thread {
    private final static Logger logger = Logger.getLogger(RMTZ_LOGGER);
    private static DatagramSocket socket;
    private static byte[] recv = new byte[1];
    private static CheckPointSaver instange;

    private CheckPointSaver() {

    }

    public static void foo() {
        if (instange != null && instange.isAlive()) {
            socket.close();
            instange.interrupt();
            try {
                instange.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            socket = new DatagramSocket(1042);
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Error opening socket: " + e.getMessage());
            System.exit(-1);
        }
        instange = new CheckPointSaver();
        instange.start();
    }

    public static void main(String[] args) {
        CheckPointSaver.foo();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            DatagramPacket recvPacket = new DatagramPacket(recv, recv.length);
            try {
                socket.receive(recvPacket);
                if (recv[0] == 42) {
                    logger.log(Level.SEVERE, "Lack of progress, setting position to last checkpoint");
                }
            } catch (IOException e) {
            }
        }
    }
}
