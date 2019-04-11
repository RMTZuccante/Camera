package utils;

import com.fazecast.jSerialComm.SerialPort;
import matrix.communication.SerialConnector;

import java.util.Scanner;
import java.util.logging.Logger;

import static utils.Utils.RMTZ_LOGGER;
import static utils.Utils.setupLogger;

public class SerialTest {
    private final static Logger logger = Logger.getLogger(RMTZ_LOGGER);
    public static void main(String[] args) {
        setupLogger(true);
        Scanner sc = new Scanner(System.in);
        SerialConnector stm = null;

        if (args.length > 0) {
            stm = new SerialConnector(SerialPort.getCommPort(args[0]), 115200);
        } else {
            for (SerialPort p : SerialPort.getCommPorts()) {
                logger.info('\t' + p.getDescriptivePortName());
                if (p.getDescriptivePortName().contains("Maple")) {
                    new SerialConnector(p, 115200);
                    break;
                }
            }
            if (stm == null) {
                System.out.print("Port: ");
                stm = new SerialConnector(SerialPort.getCommPort(sc.nextLine()), 115200);
            }
        }
        logger.info("Port opened.");
        while (!stm.handShake()) logger.info("Trying handshake...");
        logger.info("Connected!");

        while (true) {
            System.out.print("Inserisci comando: ");
            switch (sc.nextLine().toLowerCase()) {
                case "rotate":
                    System.out.print("Angle: ");
                    stm.rotate(sc.nextInt());
                    sc.nextLine(); //consuming a nextline char
                    logger.info("Rotate ended.");
                    break;
                case "go":
                    logger.info("Go ended with code: " + stm.go());
                    break;
                case "getdistances":
                    logger.info("Distances: " + stm.getDistances());
                    break;
                case "getcolor":
                    logger.info("Color: " + stm.getColor());
                    break;
                case "gettemps":
                    logger.info("Temperatures: " + stm.getTemps());
                    break;
                case "getinclination":
                    logger.info("Inclination: " + stm.getInclination());
                case "victim":
                    System.out.print("Packets: ");
                    stm.victim(sc.nextInt());
                    sc.nextLine(); //consuming a nextline char
                    logger.info("Victim ended.");
                    break;
                case "setdebug":
                    System.out.print("Level number: ");
                    sc.nextLine(); //consuming a nextline char
                    stm.setDebug(sc.nextByte());
                    break;
                case "setblack":
                    System.out.print("New black threshold: ");
                    sc.nextLine();
                    stm.setBlackThreshold(sc.nextByte());
                    break;
                case "reset":
                    logger.info("Resetting the robot...");
                    stm.reset();
                    break;
                case "getconnectioninfo":
                    logger.info(stm.getConnectionInfo());
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    logger.info("Unknown command!");
                    break;
            }
        }
    }
}
