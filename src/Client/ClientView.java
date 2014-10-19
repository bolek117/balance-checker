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
import java.util.Scanner;

/**
 *
 * @version 1.0
 * @author Michał Witas
 */
public class ClientView {
    
    /** Buffer for screen output. */
    private StringBuilder outputBuffer;
    
    /** Field holding actual help text. */
    private String helpText;
    
    /** Field describing which character should be used instead of newline character. */
    private char newLineChar = '_';
    
    /**
     * Default constructor.
     */
    public ClientView() {
        outputBuffer = new StringBuilder();
    }
    
    /**
     * Print message to the screen.
     * @param message Message to print
     */
    public void print(String message) {
        outputBuffer.append(message);
    }
    
    /**
     * Print server response to the screen (handle new lines)
     * @param message Server response to print
     */
    public void printServerResponse(String message) {
        print(message.replace(newLineChar, '\n'));
    }
    
    /**
     * Print message to error stream
     * @param message Message to print
     */
    public void printError(String message) {
        System.err.println(message);
    }
    
    /**
     * Print information about missing parameters.
     */
    public void getArgumentMissing() {
        printError("Arguments missing. Using default values (localhost 6969)\n"
                + "Usage example:\nbalance <host> <port> - connect to <host> on <port>.");
    }
    
    /**
     * Print information about default port usage.
     */
    public void getDefaultPortUsed() {
        printError("Wrong port number. Default port used");
    }
    
    /**
     * Print information about malformed server response.
     */
    public void getUnknownResponse() {
        print("Server response contains errors");
    }
    
    /**
     * Print user interface to the screen
     * @param userState Object representing actual user state
     */
    public void getPrompt(UserModel userState) {
        String separator = "-----------------------------------------\n";
        StringBuilder menu = new StringBuilder();
        
        menu.append(separator);
        
        if (userState.isLoggedIn()) {
            String level = userState.isAdmin() ? "Administrator" : "Regular User";
            
            menu.append(userState.getUsername()).append(" (").append(level).append(")\n");
            menu.append(separator);
            menu.append("Your balance: ").append(userState.getBalance().toString()).append(" $\n");
            
        } else {
            menu.append("You are not logged in\n");
        }
           
        menu.append(separator);
        menu.append(getHelp(userState.isLoggedIn())).append("\n");
        menu.append(separator);
        
        if (outputBuffer.length() == 0) {
            outputBuffer.append("[Empty]");
        }
        
        menu.append("Server output: ").append(outputBuffer.toString()).append("\n");
        menu.append(separator);
        
        menu.append("\n$ ");
        
        outputBuffer.setLength(0);
        System.out.print(menu.toString());
    }
    
    /**
     * Print proper messages to the screen after login procedure.
     * @param state "user" if logged in as regular user, "admin" if as administrator, "null" if login failed
     */
    public void getLoginState(String state) {
        switch(state) {
            case "user": {
                print("Logged in as regular user");
            } break;
            case "admin": {
                print("Logged in as administrator");
            } break;
            default: {
                print("Invalid login and/or password");
            }
        }
    }
    
    /**
     * Set help cache to new value
     * @param value New value of help string
     */
    public void setHelp(String value) {
        this.helpText = value.replace(newLineChar, '\n');
    }
    
    /**
     * Get actual help string from cache parsed for actual user state
     * @param isLoggedIn State of user - true if logged in, else false
     * @return Parsed help text
     */
    protected String getHelp(boolean isLoggedIn) {
        Scanner scanner = new Scanner(this.helpText);
        StringBuilder result = new StringBuilder();
        
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            
            if (!line.isEmpty()) {
                if (!isLoggedIn) {
                    if (line.startsWith(" ") || line.startsWith("!")) {
                        result.append(line.substring(1)).append("\n");
                    }
                } else {
                    if (line.startsWith(" ") || line.startsWith("*")) {
                        result.append(line.substring(1)).append("\n");
                    }
                }
            }
        }
        
        return result.toString();
    }
}
