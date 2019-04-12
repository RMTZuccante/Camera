package matrix.communication;

import matrix.Matrix;

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
    private static Matrix mat;
    private static boolean lock = false;

    public static void listen(Matrix mat) {
        stopListening();
        try {
            socket = new DatagramSocket(1042);
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Error opening socket: " + e.getMessage());
            System.exit(-1);
        }
        CheckPointSaver.mat = mat;
        instange = new CheckPointSaver();
        instange.start();
    }

    public static void stopListening() {
        if (instange != null && instange.isAlive()) {
            socket.close();
            instange.interrupt();
            instange = null;
        }
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            DatagramPacket recvPacket = new DatagramPacket(recv, recv.length);
            try {
                socket.receive(recvPacket);
                if (recv[0] == 42) {
                    logger.log(Level.SEVERE, "Lack of progress, setting position to last checkpoint");
                    CheckPointSaver.mat.backToCheckPoint(0);
                }
            } catch (IOException e) {
            }
        }
    }
}
