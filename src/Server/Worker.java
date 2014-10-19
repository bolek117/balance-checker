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

import BalanceChecker.Misc.UserModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Class handling operations for one connected client
 * @version 1.0
 * @author Michał Witas
 */
public class Worker extends Thread {
    
    /** socket representing connection to the client. */
    private Socket socket;
    
    /** buffered input character stream. */
    private BufferedReader in;
    
    /** View for actual controler. */
    protected ServerView view;
    
    /** Flag indicating if connection is still active (user not entered quit command). */
    protected boolean connectionActive = true;
    
    /** Object representing connected client. */
    protected UserModel client;
    
    /**
     * Default constructor for object initialization
     * @param socket Client socket
     * @param clientId Unique identifier of a client
     * @throws IOException 
     */
    public Worker(Socket socket, int clientId) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
        client = new UserModel(clientId);   
        view = new ServerView(socket.getOutputStream(), client);
    }
    
    @Override
    public void run() {
        view.print("[Info] Client connected");
        view.userMotd();
        
        do {
            dispatchRequests();
        } while(connectionActive == true);
        
        killThread();
    }
    
    /**
     * Controller invoking other method for given commands and redirecting results to proper views.
     */
    public void dispatchRequests() {
        try {
            String stri= in.readLine();
            String[] str;
            if (stri != null) {
                str = stri.toLowerCase().split(" ", 3);
            } else {
                str = new String[1];
                str[0] = "quit";
            }
            
            view.print("[Request] " + str[0]);
            
            switch(str[0]) {
                case "exit":
                case "quit": {
                    closeConnection();
                } break;
                case "help": {
                    if (str.length == 2) view.getHelp(str[1]);
                    else view.getHelp();
                } break;
                case "login": {
                    if (str.length == 3) {
                        client.logIn(str[1], str[2]);
                        view.getLoginMessage();
                    } else view.getHelp("login");
                } break;
                case "changebalance": {
                    if (client.isAdmin() && str.length == 3) {
                        view.getBalance(client.changeUserBalance(str[1], str[2]));                        
                    } else view.getHelp("changebalance");
                } break;
                case "checkbalance": {
                    if (client.isAdmin() && str.length == 2) {
                        view.getBalance(client.checkUserBalance(str[1]));
                    } else view.getHelp("checkbalance");
                } break;
                case "createuser": {
                    if (client.isAdmin() && str.length == 3) {
                        view.getConfirm(client.createUser(str[1], str[2]));
                    } else view.getHelp("createuser");
                } break;
                case "withdraw": {
                    if (client.isLoggedIn() && str.length == 2) {
                        if (client.withdraw(str[1])) {
                            view.getBalance();
                        } else {
                            view.getActionNotAllowed();
                        }
                    } else view.getHelp("withdraw");
                } break;
                case "balance": {
                    if (client.isLoggedIn()) {
                        view.getBalance();
                    } else view.getHelp("balance");
                } break;
                case "logout": {
                    if (client.isLoggedIn()) {
                        client.logOut();
                        view.getLogoutMessage();
                    } else view.getHelp("logout");
                } break;
                default: {
                    view.getUnknownCommandMessage();
                }
                
            }
        } catch (IOException ex) {
            view.printError(ex.getMessage());
            closeConnection();
        }
        
        view.executeSendToUser();
    }
    
    /**
     * Method executing all actions needed by app logic when connection is closed.
     */
    protected void closeConnection() {
        connectionActive = false;
    }
    
    /**
     * Method executing all actions needed by interfaces when connection is closed.
     */
    protected void killThread() {
        view.print("[Info] Socket closed");
        
        try {
            socket.close();
        } catch (IOException ex) {
            // Ignore, if there is no socket to close, we do not have any problem...
        }
    }
    
}
