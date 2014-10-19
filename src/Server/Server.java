/*
 * Copyright (C) 2014 Michał Witas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package BalanceChecker.Server;

import java.net.*;
import java.io.*;

/**
 * Balance checker server
 * @version 1.0
 * @author Michał Witas
 */
public class Server {
    
    /** Port used for listening. */
    protected static int port = 6969;
    
    /** Instance of server socket. */
    protected static ServerSocket serverSocket = null;
    
    /** Instance of view. */
    protected static ServerView view;
    
    /** Internal index of connected users. */
    protected static int clientId;
    
    /**
     * Method starts socket, instantiate view and wait for clients connections.
     * @param args 
     */
    public static void main(String args[]) {
        view = new ServerView();
        clientId = 0;
        
        if (createSocket() != false) {
            view.serverMotd(port);            
            
            try {
                listenForConnections(serverSocket);
            } catch (IOException ex) {
                view.printError(ex.getMessage());
            }            
        } else {
            view.printError("Unable to open socket for listening");
        }
    }
    
    /**
     * Method creating new server socket
     * @return True on success, false if any exception occured.
     */
    protected static boolean createSocket() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Method listening for client connections
     * @param socket Socket for listening
     * @throws IOException 
     */
    protected static void listenForConnections(ServerSocket socket) throws IOException {
        try {
            while (true) {
                Socket clientSocket = socket.accept();
                
                try {
                    Worker worker = new Worker(clientSocket, nextClientId());
                    worker.start();
                } catch (IOException e) {
                    view.printError(e.getMessage());
                }
            }
        } finally {
            socket.close();
        }
    }
    
    /**
     * Get ID of the next client and increment internal counter
     * @return Id of next client
     */
    protected static int nextClientId() {
        ++clientId;
        return clientId;
    }
}
