package it.rmtz.matrix;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Arrays;
import java.util.Scanner;

public class SerialTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SerialConnector stm;

        if(args.length > 0) {
            stm = new SerialConnector(SerialPort.getCommPort(args[0]), 115200);
        }
        else {
            System.out.print("Port: ");
            stm = new SerialConnector(SerialPort.getCommPort(sc.nextLine()), 115200);
        }
        System.out.println("Connected!");
        System.out.println(stm.getConnectionInfo());
        System.out.println();

        while (true) {
            System.out.print("cmd: ");
            switch (sc.nextLine().toLowerCase()) {
                case "handshake":
                    System.out.print("Handshake result: ");
                    System.out.println(stm.handShake());
                    break;
                case "rotate":
                    System.out.print("Angle: ");
                    stm.rotate(sc.nextInt());
                    sc.nextLine(); //consuming a nextline char
                    System.out.println("Rotate ended.");
                    break;
                case "go":
                    System.out.println("Go ended with code: "+stm.go());
                    break;
                case "getdistances":
                    System.out.println("Distances: "+ Arrays.toString(stm.getDistances()));
                    break;
                case "getcolor":
                    System.out.println("Color: "+stm.getColor());
                    break;
                case "gettemps":
                    System.out.println("Temperatures: "+Arrays.toString(stm.getTemps()));
                    break;
                case "victim":
                    System.out.print("Packets: ");
                    stm.victim(sc.nextInt());
                    sc.nextLine(); //consuming a nextline char
                    System.out.println("Victim ended.");
                    break;
                case "getconnectioninfo":
                    System.out.println(stm.getConnectionInfo());
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Unknown command!");
                    break;
            }
        }
    }
}
