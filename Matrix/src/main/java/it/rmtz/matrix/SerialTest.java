package it.rmtz.matrix;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Arrays;
import java.util.Scanner;

public class SerialTest {
    public static void main(String[] args) {
        SerialConnector stm = null;

        Scanner sc = new Scanner(System.in);
        while (true) {
            switch (sc.nextLine().toLowerCase()) {
                case "connect":
                    System.out.print("Port: ");
                    stm = new SerialConnector(SerialPort.getCommPort(sc.nextLine()),115200);
                    break;
                case "handshake":
                    System.out.print("Handshake result: ");
                    System.out.println(stm.handShake());
                    break;
                case "rotate":
                    System.out.print("Angle: ");
                    stm.rotate(sc.nextInt());
                    sc.nextLine(); //consuming a nextline char
                    System.out.println("Rotate ended.");
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
            }
        }

    }
}
