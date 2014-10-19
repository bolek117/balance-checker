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

package BalanceChecker.Client;

import BalanceChecker.Misc.UserModel;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @version 1.0
 * @author Michał Witas
 */
public class Client {
    
    /** Server address with default value. */
    protected static String host = "localhost";
    
    /** Server port with default value. */
    protected static int portNumber = 6969;
    
    /** Instance of user interface. */
    protected static ClientView view;
    
    /** Buffered input from server. */
    protected static BufferedReader serverReader;
    
    /** Buffered input from client. */
    protected static BufferedReader clientInput;
    
    /** Connection to the server. */
    protected static Socket socket;
    
    /** Output to the server. */
    protected static PrintWriter out;
    
    /** Instance of user state. */
    protected static UserModel userState;

    public static void main(String[] args) throws IOException {
        
        initialize();        
        parseArguments(args);        
            
        if (!initializeConnection()) return;
        
        String line;
        String userInput;
        
        while(true) {         
            String serverResponse = serverReader.readLine();
            
            String[] responses;
            if (serverResponse == null) {
                view.printError("Connection closed by server");
                socket.close();
                break;
            } else {
                responses = serverResponse.split(":::");  // Extract messages from data
            }
            
            for(String content : responses) {
                if (!content.isEmpty()) {
                    String[] cmd = content.split("::");  // Extract fields from message

                    if (cmd.length != 2) {
                        view.getUnknownResponse();
                    } else {
                        handleResponses(cmd);
                    }
                }
            }
            
            view.getPrompt(userState);
            userInput = clientInput.readLine().toLowerCase();
            
            if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                out.println("quit");
                socket.close();
                break;
            } else {
                out.println(userInput);
            }
        }
    }
    
    /** 
     * Initialize needed objects.
     */
    protected static void initialize() {
        view = new ClientView();
        userState = new UserModel(0);
    }
    
    /**
     * Parse startup arguments
     * @param args Startup arguments
     */
    protected static void parseArguments(String[] args) {
        if (args.length != 2) {
            view.getArgumentMissing();
        } else {
            host = args[0];
            
            try {
                portNumber = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                view.getDefaultPortUsed();
            }
        }
    }

    /**
     * Initialize sockets and inputs/outputs
     * @return true if all is initialized, else false
     */
    private static boolean initializeConnection() {
        try {
            socket = new Socket(host, portNumber);
            serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientInput = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch(IOException ex) {
            view.printError(ex.getMessage());
            return false;
        }
        
        return true;
    }

    /**
     * Handle server responses
     * @param cmd Command form server
     */
    private static void handleResponses(String[] cmd) {
        switch(cmd[0]) {
            case "text": {
                view.printServerResponse(cmd[1]);
            } break;
            case "username": {
                if (userState.isLoggedIn()) {
                    userState.setUsername(cmd[1]);
                }
            } break;
            case "login": {
                if (cmd[1].equals("admin")) {
                    userState.setIsLoggedIn(true);
                    userState.setIsAdmin(true);
                } else if (cmd[1].equals("user")) {
                    userState.setIsLoggedIn(true);
                    userState.setIsAdmin(false);
                }
            } break;
            case "logout": {
                userState.logOut();
            } break;
            case "balance": {
                if (userState.isLoggedIn()) {
                    float balance = 0;

                    try {
                        balance = Float.parseFloat(cmd[1]);
                    } catch (NumberFormatException ex) {
                        userState.setBalance(0.0f);
                        view.getUnknownResponse();
                        break;
                    }

                    userState.setBalance(balance);
                }
            } break;
            case "help": {
                view.setHelp(cmd[1]);
            } break;
            case "actionnotallowed": {
                view.print("You are not allowed to perform this action");
            } break;
            default: {
                view.getUnknownResponse();
            }
        }
    }
}
