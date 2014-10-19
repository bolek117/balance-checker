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

package BalanceChecker.Misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class providing user related functions for authentication and balance status checks
 * @version 1.0
 * @author Michał Witas
 */
public class UserModel {
    
    /** Flag indicating if user is initialized (data from file loaded to memory). */
    protected boolean initialized = false;
    
    /** Flag indicating if user is actually logged in. */
    protected boolean loggedIn = false;
    
    /** Flag indicating if user have administrator permissions. */
    protected boolean isAdmin = false;
    
    /** Internal unique identifier of the user. */
    protected int id;
    
    /** Username (login) of the user. */
    protected String username;
    
    /** Password of the user loaded from the file. */
    private String password;
    
    /** State of account. */
    private Float balance;
    
    /**
     * Default constructor. Assigns ID and reset all fields to default value.
     * @param id Identifier of user
     */
    public UserModel(int id) {
        this.id = id;
        logOut();   // Zero all variables
    }
    
    /**
     * Get id of current user.
     * @return Identifier of the user
     */
    public int getId() {
        return id;
    }
    
    /**
     * Perform log in operation. 
     * Loads state of the user from file.
     * Checks username and password and if match, set loggedIn to true
     * 
     * @param username Username (login) of the user
     * @param password Password of the user
     * @return True if success, false if failed
     */
    public boolean logIn(String username, String password) {
        if (!isLoggedIn()) {
            loadState(username);

            if (username.equals(this.username) && password.equals(this.password)) {
                setIsLoggedIn(true);
            } else {
                setIsLoggedIn(false);
            }

            return isLoggedIn();
        } else {
            return true;
        }
    }
    
    /**
     * Reset all variables to default value and logs out user from the system.
     */
    public final void logOut() {
        this.username = "";
        this.password = "";
        this.loggedIn = false;
        this.isAdmin = false;
        this.initialized = false;
        resetBalance();
    }
    
    /**
     * Set if user is logged in or not
     * @param state True if logged in, else false
     */
    public void setIsLoggedIn(boolean state) {
        this.loggedIn = state;
    }
    
    /**
     * Checks if user is logged in.
     * @return True if logged in, else false
     */
    public boolean isLoggedIn() {
        return this.loggedIn;
    }
    
    /**
     * Set if user is an administrator or not
     * @param state True if administrator, else false
     */
    public void setIsAdmin(boolean state) {
        this.isAdmin = state;
    }
    
    /**
     * Checks if user have administrator permissions.
     * @return True if user is admin, else false
     */
    public boolean isAdmin() {
        return isAdmin;
    }
    
    /**
     * Seter for username field
     * @param username Username of client
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Getter for username field
     * @return Username
     */
    public String getUsername() {
        return this.username;
    }
    
    /**
     * Load state of the user from file.
     * @param username Username of user which state should be loaded
     * @return True if loading was successful, if any error occured - false
     */
    protected boolean loadState(String username) {        
        if (initialized == false) {
            BufferedReader br = null;
            boolean success = false;

            logOut();
            setUsername(username);
            balance = 0.0f;

            try {
                br = new BufferedReader(new FileReader(getFilepathForUser()));

                String line;
                for(int i=0;(line = br.readLine()) != null;++i) {
                    switch(i) {
                        case 0: 
                            this.username = line; 
                            break;
                        case 1: 
                            this.password = line; 
                            break;
                        case 2:
                            this.isAdmin = Boolean.parseBoolean(line);
                            success = true; // All three values are needed for proper initialization
                            break;
                        default: {
                            balance += Float.parseFloat(line);
                        } break;
                    }
                }
                
                initialized = success;                
            } catch (IOException | NumberFormatException ex) {
                initialized = false;
                logOut();
                
                return false;
            }
        }
        
        return initialized;
    }
    
    /**
     * Returns path to the file with user state for current user
     * @return Path to the file with user state
     */
    protected String getFilepathForUser() {
        return getFilepathForUser(this.username);
    }
    
    /**
     * Returns path to the file with user state
     * @param username Username for which path should be generated
     * @return Path to the file.
     */
    protected String getFilepathForUser(String username) {
        return "users/" + username + ".txt";
    }
    
    /**
     * Change account balance by given ammount of money.
     * @param ammount Ammount of cash. If godMode is false, only negative values are allowed.
     * @param godMode Allows to "create" money (ammount more than 0). Default false, true only admin's and loading from file
     * @return True if all was ok, else false
     */
    protected boolean changeBalance(float ammount, boolean godMode) {
        
        // Block negative ammount and ammount bigger than balance
        if (!godMode && (ammount > 0.0f || balance < -ammount)) {
            return false;
        }
        
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(getFilepathForUser(), true));
            output.append(Float.toString(ammount) + "\n");
            output.close();
        } catch(IOException ex) {
            return false;
        }
        
        balance += ammount;
        
        return true;
    }
    
    /**
     * The same as changebalance(float, boolean=false)
     * @param ammount Ammount of money to change balance
     * @return  True if success, else false
     */
    protected boolean changeBalance(float ammount) {
        // Without god mode
        return changeBalance(ammount, false);
    }
    
    /**
     * Refresh internal state of account balance.
     * @return True if reload is ok, else false
     */
    protected boolean reloadBalance() {
        balance = 0.0f;
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(getFilepathForUser()));

            String line;
            for(int i=0;(line = br.readLine()) != null;++i) {
                switch(i) {
                    case 0:
                    case 1:
                    case 2:
                        // Do nothing
                        break;
                    default: {
                        balance += Float.parseFloat(line);
                    } break;
                }
            }
            
            return true;
        } catch (IOException | NumberFormatException ex) {
            logOut();
            return false;
        }
    }
    
    /**
     * Set balance to Not a number value to indicate that balance is not initialized.
     */
    protected void resetBalance() {
        balance = Float.NaN;
    }
    
    /**
     * Getter for balance field
     * @return Balance of user account
     */
    public Float getBalance() {
        return balance;
    }
    
    /**
     * Set balance to given state
     * @param balance Balance to which account should be set
     */
    public void setBalance(float balance) {
        this.balance = balance;
    }

    /**
     * Withdraw ammount of money from account
     * @param ammount Ammount of money
     * @return True if success, else false
     */
    public boolean withdraw(String ammount) {
        reloadBalance();
        Float amm;
        
        try {
            amm = Float.parseFloat(ammount) * -1;
        } catch(NumberFormatException ex) {
            return false;
        }
        
        return changeBalance(amm);        
    }
    
    /**
     * Method creating new user and assiging default values to member fields
     * @param username Username for new user
     * @param password Password for new user
     * @return True if success, else false
     */
    public boolean createUser(String username, String password) {
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(getFilepathForUser(username)));
            output.append(username + "\n");
            output.append(password + "\n");
            output.append("false\n"); // Admins can be created only via modifications of file
            output.append("0.0\n");
            output.close();
        } catch (IOException ex) {
            return false;
        }

        return true;
    }
    
    /**
     * Method returning balance for user with given username
     * @param username Username of user which balance should be checked
     * @return User balance if success, Float.NaN if user not found or any error occured
     */
    public Float checkUserBalance(String username) {
        UserModel user = new UserModel(0);
        if (!user.loadState(username)) {
            return Float.NaN;
        }

        return user.getBalance();
    }
    
    /**
     * Change balance for given user by ammount of money
     * @param username Username for which balance should be changed
     * @param ammount Ammount of money that should be added/removed from account
     * @return New user balance or Float.NaN if any errors occured
     */
    public Float changeUserBalance(String username, String ammount) {
        Float amm = 0.0f;
        
        try {
            amm = Float.parseFloat(ammount);
        } catch (NumberFormatException ex) {
            return Float.NaN;
        }
        
        UserModel user = new UserModel(0);
        
        if (!user.loadState(username)) {
            return Float.NaN;
        }
        
        user.changeBalance(amm, true);
        return user.getBalance();
    }
}
