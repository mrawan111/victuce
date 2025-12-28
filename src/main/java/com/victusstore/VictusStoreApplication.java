package com.victusstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.Socket;

@SpringBootApplication
@EnableScheduling
public class VictusStoreApplication {

    public static void main(String[] args) {
        // Check if SSH tunnel is active before starting
        boolean tunnelActive = false;
        try (Socket socket = new Socket("localhost", 5432)) {
            tunnelActive = socket.isConnected();
            socket.close();
        } catch (Exception e) {
            // Tunnel not active - expected if SSH tunnel isn't established
        }
        
        if (!tunnelActive) {
            System.err.println("\n" + "=".repeat(80));
            System.err.println("ERROR: SSH Tunnel is not active!");
            System.err.println("=".repeat(80));
            System.err.println("Please establish the SSH tunnel before starting the application:");
            System.err.println("  ssh -L 5432:localhost:5432 home-server@196.221.167.63");
            System.err.println("=".repeat(80) + "\n");
        }
        
        SpringApplication.run(VictusStoreApplication.class, args);
    }
}
