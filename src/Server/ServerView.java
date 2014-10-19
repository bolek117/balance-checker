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
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class providing server user interface
 * @version 1.0
 * @author Michał Witas
 */
public class ServerView {
    
    /** Flag determining if connection is established and active. */
    protected boolean connectionEstablished = false;
    
    /** Associative array of commands available to all users. */
    Map<String,String> commands;
    
    /** Associative array of commands available to administrators. */
    Map<String,String> adminCommands;
    
    /** Formatted output character stream. */
    private PrintWriter out;
    
    /** Informations about actual user. */
    private UserModel client;
    
    /** Buffered user output. */
    protected StringBuilder userOutput = new StringBuilder();
    
    /**
     * Default constructor for server view.
     */
    public ServerView() {
        connectionEstablished = false;
        defineHelpMessages();
    }
    
    /**
     * Default constructor for interaction with client.
     * @param outputStream Stream used for sending data to client
     * @param client Informations about actual client
     */
    public ServerView(OutputStream outputStream, UserModel client) {
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)), true);
        connectionEstablished = true;
        this.client = client;
        
        defineHelpMessages();
    }
    
    /**
     * Method loading help messages to the memory.
     */
    protected final void defineHelpMessages() {
        // ! - for not logged in only
        // * - for logged in only
        // space - for both
        commands = new LinkedHashMap<>();      
        this.commands.put("help", " help [command] - this help message or command usage if [command] is defined");
        this.commands.put("login", "!login <login> <password> - login as user with specified <login>/<password> pair");
        this.commands.put("balance", "*balance - shows actual account balance");
        this.commands.put("withdraw", "*withdraw <ammount> - withdraw <ammount> of money from your account");
        this.commands.put("logout", "*logout - log out from system");
        this.commands.put("quit", " quit - close connection");
        
        adminCommands = new LinkedHashMap<>();        
        this.adminCommands.put("changebalance", "*changeBalance <username> <balance> - set user balance by given value");
        this.adminCommands.put("checkbalance", "*checkBalance <username> - check balance for given user");
        this.adminCommands.put("createuser", "*createUser <username> <password> - create new user with given username/password");
        
    }
    
    /**
     * Print message to the server console.
     * @param message Message to print
     */
    public void print(String message) {
        if (client == null) {
            System.out.println("[" + new java.util.Date().toString() + "] " + message);
        } else {
            System.out.println("[" + new java.util.Date().toString() + "] [id:" + client.getId() + "] " + message);
        }
    }
    
    /**
     * Print error to the server console.
     * @param message Message to print
     */
    public void printError(String message) {
        System.err.println("[" + new java.util.Date().toString() + "] [Error] " + message);
    }
    
    /**
     * Print Motd to the server console.
     * @param port Port of which server is listening
     */
    public void serverMotd(int port) {
        print("[info] Server started on port " + port);
    }
    
    /**
     * Print Motd to the user.
     */
    public void userMotd() {
        //sendToUser("Balance checker v1.0");
        //getHelpHint();
        getHelp();
        
        executeSendToUser();
    }
    
    /**
     * Print help with all available commands.
     */
    public void getHelp() {
        StringBuilder text = new StringBuilder();
        
        text.append("Available commands:\n");
        /*+ "- commands marked with * are available to logged users only\n"
        + "- Required parameters are marked with <parameterName>\n"
        + "- Optional parameters are marked with [parameterName]\n\n");*/

        Map<String,String> tmp = new LinkedHashMap<>();
        if (client.isAdmin()) {
            tmp.putAll(adminCommands);
        }

        tmp.putAll(commands);

        Collection<String> list = tmp.values();

        for(Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            text.append(iterator.next());

            if (iterator.hasNext()) {
                text.append("\n");
            }
        }

        sendToUser(text.toString(), "help");
    }
    
    /**
     * Print help with usage of given command
     * @param command Command for which help should be printed
     */
    public void getHelp(String command) {
        StringBuilder text = new StringBuilder();
        
        command = command.toLowerCase();
        
        if (commands.containsKey(command)) {
            String commandText = commands.get(command);
            boolean allowed = false;
            
            if (client.isLoggedIn() && (commandText.startsWith("*") || commandText.startsWith(" "))) {
                allowed = true;
            } else if (!client.isLoggedIn() && (commandText.startsWith("!") || commandText.startsWith(" "))) {
                allowed = true;
            }
            
            if (allowed) {
                text.append("Command usage: ").append(commandText.substring(1));
            } else {
                getUnknownCommandMessage();
                return;
            }
        } else if (client.isAdmin() && adminCommands.containsKey(command)) {
            String commandText = adminCommands.get(command);
            boolean allowed = false;
            
            if (client.isAdmin() && (commandText.startsWith("*") || commandText.startsWith(" "))) {
                allowed = true;
            }
            
            if (allowed) {
                text.append("Command usage: ");
                text.append(commandText.substring(1));
            } else {
                getUnknownCommandMessage();
                return;
            }                    
        } else {
            getUnknownCommandMessage();
            return;
        }

        sendToUser(text.toString());
        
    }
    
    /**
     * Print hint about help to the user.
     */
    public void getHelpHint() {
        sendToUser("Enter help to list all available commands");
    }
    
    /**
     * Print message about unknown message.
     */
    public void getUnknownCommandMessage() {
        sendToUser("Unknown command, type help to list all available commands.");
    }
    
    /**
     * Print message after successful or failed login process.
     */
    public void getLoginMessage() {
        if (client.isLoggedIn()) {
            sendToUser((client.isAdmin() ? "admin" : "user"), "login");
            sendToUser(client.getUsername(), "username");
            getBalance();
            getHelp();
            //sendToUser("You are successful logged in");
        } else {
            sendToUser("null", "login");
            sendToUser("null", "username");
            sendToUser("Invalid login and/or password");
        }
    }
    
    /**
     * Print message after logout.
     */
    public void getLogoutMessage() {
        sendToUser("", "logout");
        sendToUser("You are safely logged out");
    }
    
    /**
     * Print message if given action is not allowed
     */
    public void getActionNotAllowed() {
        sendToUser("", "actionnotallowed");
    }
    
    /**
     * Print account balance for actual user.
     */
    public void getBalance() {
        if (client.getBalance().isNaN()) {
            getActionNotAllowed();
        } else {
            sendToUser(client.getBalance().toString(), "balance");
            sendToUser("Your balance: " + client.getBalance().toString());
        }
    }
    
    /**
     * Print given number as a account balance
     * @param balance Balance to print to the screen
     */
    public void getBalance(Float balance) {
        if (balance.isNaN()) {
            getActionNotAllowed();
        } else {
            sendToUser(balance.toString());
        }
    }
    
    /**
     * Print response for functions returning true/false response.
     * @param state True/false response from other function
     */
    public void getConfirm(boolean state) {
        sendToUser((state ? "Success" : "Failed"));
    }
    
    /**
     * Append message to response buffer
     * @param message Message to send
     */
    public void sendToUser(String message) {
        sendToUser(message, "text");
    }
    
    /**
     * Send message to user marking command with symbol
     * @param message Message to send
     * @param commandSymbol Symbol of command (arbitrary but client must know it)
     */
    public void sendToUser(String message, String commandSymbol) {
        if (message.isEmpty()) {
            message = commandSymbol;
        }
        
        if (connectionEstablished == true) {
            userOutput.append(":::");
            userOutput.append(commandSymbol);
            userOutput.append("::");
            message = message.replace('\n', '_');
           
            userOutput.append(message.replace('\n', '_'));
            if (userOutput.toString().endsWith("_")) {
                userOutput.deleteCharAt(userOutput.length()-1);
            }
        } else {
            printError("Connection to user not established");
        }
    }
    
    /**
     * Send buffer to the user.
     */
    public void executeSendToUser() {
        if (connectionEstablished == true) {
            out.println(userOutput.toString());
            userOutput.setLength(0);
        }
    }
    
}
