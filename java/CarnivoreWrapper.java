//
//  CarnivoreWrapper.java
//  CarnivoreWrapper
//
//  Created by Carlos Castellanos on 10/17/04.
//

package carlos.maxivore;

import java.util.*;
import java.io.*;

public class CarnivoreWrapper {
    // Default connection info
    private String carnivoreIP = "127.0.0.1";
    private int carnivorePort = 6667;
    //private String carnivoreDataFile = "carnivore_data.txt";
    //private String carnivoreCGIsource = "http://www.somedomain.com/cgi-bin/carnivore_client_zero.pl";
    //private CarniToOSC carni;
    //private int oscPort = 57000;
    //private String oscHost = "localhost";
    private static CarnivoreListener carnivore = null;
    
    // Contructors
    public CarnivoreWrapper() {
    }
    
    public CarnivoreWrapper(String host) {
        this.carnivoreIP = host;
    }
    
    public CarnivoreWrapper(int port) {
        this.carnivorePort = port;
    }
    
    public CarnivoreWrapper(String host, int port) {
        this.carnivoreIP = host;
        this.carnivorePort = port;
    }
    
    public static Carnivore getCarni() {
        return carnivore;
    }

 ////////////////////////////////////////
// Start and stop the Carnivore Listener

    public void startCarnivore() throws Exception
    {
    
        System.out.print("Establishing connection to Carnivore server at " +  carnivoreIP + " on port " + carnivorePort + "... ");
        // init Carnivore Listner
        carnivore = new CarnivoreListener(carnivoreIP, carnivorePort);
        
        try {
            carnivore.startListening();
        } catch (Exception e) {
             System.out.println("Failed to establish a connection to the Carnivore server! " + e.getMessage());
        }
    }

    public void startCarnivore(boolean hex) throws Exception
    {
    
        System.out.print("Establishing connection to Carnivore server at " +  carnivoreIP + " on port " + carnivorePort + "... ");
        // init Carnivore Listner
        carnivore = new CarnivoreListener(carnivoreIP, carnivorePort);
        
        // process packet byte-by-byte if hex is true
        carnivore.processHexBytes = hex;
            
        try {
            carnivore.startListening();
        } catch (Exception e) {
             System.out.println("Failed to establish a connection to the Carnivore server! " + e.getMessage());
        }
    }


    public void startCarnivore(String fileOrURL) throws Exception {
        // init Carnivore Listener
        carnivore = new CarnivoreListener();
        
        // check to see if it's a url or filename
        if(fileOrURL.startsWith("http://") || fileOrURL.startsWith("HTTP://")) {
            System.out.print("Establishing connection to Carnivore.  Data coming via url: " + fileOrURL);
            carnivore.setURL(fileOrURL);
        } else {
            System.out.print("Establishing connection to Carnivore.  Data coming from local file: " + fileOrURL);
            carnivore.setFile(fileOrURL);
        }
        
        try {
            carnivore.startListening();
        } catch (Exception e) {
             System.out.println("Failed to establish a connection to the Carnivore server! " + e.getMessage());
        }
    }
    
    public void startCarnivore(String fileOrURL, boolean hex) throws Exception {
        // init Carnivore Listener
        carnivore = new CarnivoreListener();
        
        // check to see if it's a url or filename
        if(fileOrURL.startsWith("http://") || fileOrURL.startsWith("HTTP://")) {
            System.out.print("Establishing connection to Carnivore.  Data coming via url: " + fileOrURL);
            carnivore.setURL(fileOrURL);
        } else {
            System.out.print("Establishing connection to Carnivore.  Data coming from local file : " + fileOrURL);
            carnivore.setFile(fileOrURL);
        }
        
        // process packet byte-by-byte if hex is true
        carnivore.processHexBytes = hex;
            
        try {
            carnivore.startListening();
        } catch (Exception e) {
             System.out.println("Failed to establish a connection to the Carnivore server! " + e.getMessage());
        }
    }

    
    public void stopCarnivore()
    {
        if (carnivore != null) {
            carnivore.stopListening();
        }
    }
   
   
   
}


