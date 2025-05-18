package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SimpleUdpEchoServer {
    public static void main(String[] args) {
        int port = 54321; // Используйте порт вашего сервера
        System.out.println("Simple UDP Server listening on port " + port);
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(requestPacket); // Ожидание пакета

                InetAddress clientAddress = requestPacket.getAddress();
                int clientPort = requestPacket.getPort();
                String received = new String(requestPacket.getData(), 0, requestPacket.getLength());

                System.out.printf("Received packet from %s:%d, Data length: %d\n",
                        clientAddress.getHostAddress(), clientPort, received.length());

                // Можно добавить отправку простого ответа для проверки
                // byte[] responseData = "ACK".getBytes();
                // DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                // socket.send(responsePacket);
                // System.out.println("Sent ACK");
            }
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}