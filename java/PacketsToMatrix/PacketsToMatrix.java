//
//  PacketsToMatrix.java
//  PacketsToMatrix
//
//  Created by Carlos Castellanos on 4/4/05.
//  Copyright 2005. All rights reserved.
//

package carlos.maxivore;

import cc.netart.carnivore.*;
import com.cycling74.max.*;
import java.util.regex.*;
import java.util.*;


public class PacketsToMatrix extends MaxObject implements CarniOutputListener {

    // carni stuff
    private String portName;
    private static final int portNumber = 6667; // standard carnivore port
    private CarnivoreWrapper myCarni;
    private boolean running = false;
    
    // regex stuff
    private static final String REGEX_IP = "([01]?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\\.([012]?[0-9][0-9])";
	//private static final String REGEX_IP = "([012]?[0-9][0-9])\\.([01]?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\\.([01]?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\\.([012]?[0-9][0-9])";
    private static Pattern pattern = Pattern.compile(REGEX_IP, Pattern.CASE_INSENSITIVE);
    private Matcher matcher;
    private boolean found;
    
    // pixel stuff
    private int[] matrixDim = null;
    private int numPixels;
    private int count = 0;
    private Map hash = null;
    private Map paramsHash = new HashMap(8);
    private String[] ipPair = new String[2];
    private int xCoord = 0;
    private int yCoord = 0;
    private int xCoord1 = 0;
    private int yCoord1 = 0;
    private int[][] rgbIP = new int[2][3];
    private int[] finalRGB = new int[3];
    private String currKey = null;
    
    
    // constructor
    PacketsToMatrix(Atom[] args) {
        declareIO(1, 3);
        createInfoOutlet(false);
        setInletAssist(0, "bang, start message or list with dimensions starts listening");
        setOutletAssist(0, "list containing matrix data");
        setOutletAssist(1, "old/new connections");
        setOutletAssist(2, "raw carnivore data");
        
        if(args.length > 0) {
            this.portName = args[0].getString();
        } else {
            this.portName = "127.0.0.1";
        }
    }
    
    // this method gets the carni data and sends it out
    public void messageReceived(CarnivoreListener carni, String str) {
		post("message received " + str);
		
        //post(str);
        
        int count = 0;
        
        // first parse out the ip numbers
        matcher = pattern.matcher(str);
        while(matcher.find() && count < 2) {
            post("count:" + count);
 
            // the ip pair (sender/receiver)
            ipPair[count] = matcher.group();
            // convert first 3 sets of numbers in the ip
            // to ints (to be used as rgb values)

            rgbIP[count][0] = Integer.parseInt(matcher.group(1));
            rgbIP[count][1] = Integer.parseInt(matcher.group(2));
            rgbIP[count][2] = Integer.parseInt(matcher.group(3));

/*
            rgbIP[count][0] = Integer.parseInt(matcher.group(2));
            rgbIP[count][1] = Integer.parseInt(matcher.group(3));
            rgbIP[count][2] = Integer.parseInt(matcher.group(4));
            post("matcher.group(0)=" + matcher.group(0));
            post("matcher.group(1)=" + matcher.group(1));
            post("matcher.group(2)=" + matcher.group(2));
            post("matcher.group(3)=" + matcher.group(3));
            post("matcher.group(4)=" + matcher.group(4));
*/

            found = true;
            count++;
        }
        
        if(!found) {
            post("No match found.");
            //post("quitting...");
            //stop();
            return;
        }
        
        currKey = new String(ipPair[0] + "+" + ipPair[1]);
                    
            // if the main hashmap isn't empty        
        if(!(hash.isEmpty())) {
        
            // match found in main hashMap, so alter that pixel value
            if(hash.containsKey(currKey) || hash.containsKey(ipPair[1] + "+" + ipPair[0])) {
                post("match found...");
                lightenMatched(currKey, ipPair[1] + "+" + ipPair[0]);
                newPixelFromOldConnection();
            } else {
                // no match found, so add a new pixel value to the
                // hashmap at the proper coordinates
                post("no match, add new pixel...");
                if(xCoord > matrixDim[0]-1 && yCoord > matrixDim[1]-1) {
                    // we've reached the end, so go to the beginiing
                    // of the matrix
                    xCoord = 0;
                    yCoord = 0;
                    //outlet(0, Atom.newAtom("clear"));
                }
                
                if(xCoord > matrixDim[0]-1) {
                    // go to the next row
                    xCoord = 0;
                    yCoord++;
                }
                
                // x/yCoord1
                if(xCoord1 > matrixDim[0]-1 && yCoord1 > matrixDim[1]-1) {
                    // we've reached the end, so go to the beginiing
                    // of the matrix
                    xCoord1 = 0;
                    yCoord1 = 0;
                    outlet(0, Atom.newAtom("clear"));
                }
                
                if(xCoord1 > matrixDim[0]-1) {
                    // go to the next row
                    xCoord1 = 0;
                    yCoord1++;
                }
                
                int[] colorFromIP = ipsToColor(rgbIP);
                
                // store the data
                paramsHash.put("sender", new String(ipPair[0]));
                paramsHash.put("receiver", new String(ipPair[1]));
                paramsHash.put("x", new Integer(xCoord));
                paramsHash.put("y", new Integer(yCoord));
                paramsHash.put("alpha", new Integer(0));
                paramsHash.put("red", new Integer(colorFromIP[0]));
                paramsHash.put("green", new Integer(colorFromIP[1]));
                paramsHash.put("blue", new Integer(colorFromIP[2]));
                
                post("r:" + colorFromIP[0]);
                post("g:" + colorFromIP[1]);
                post("b:" + colorFromIP[2]);
                
                // add paramsHash to the main hashmap
                hash.put(currKey, new HashMap(paramsHash));
                
                // send out the cell values
                outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord), Atom.newAtom(yCoord), Atom.newAtom("plane"), 
                        Atom.newAtom(0), Atom.newAtom("val"), Atom.newAtom(1)});
                outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord), Atom.newAtom(yCoord), Atom.newAtom("plane"), 
                        Atom.newAtom(1), Atom.newAtom("val"), Atom.newAtom(colorFromIP[0])});
                outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord), Atom.newAtom(yCoord), Atom.newAtom("plane"), 
                        Atom.newAtom(2), Atom.newAtom("val"), Atom.newAtom(colorFromIP[1])});
                outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord), Atom.newAtom(yCoord), Atom.newAtom("plane"), 
                        Atom.newAtom(3), Atom.newAtom("val"), Atom.newAtom(colorFromIP[2])});
                 
                // new pixel from new connection
                outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                        Atom.newAtom(0), Atom.newAtom("val"), Atom.newAtom(255)});
                outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                        Atom.newAtom(1), Atom.newAtom("val"), Atom.newAtom(0)});
                outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                        Atom.newAtom(2), Atom.newAtom("val"), Atom.newAtom(255)});
                outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                        Atom.newAtom(3), Atom.newAtom("val"), Atom.newAtom(0)});
                        
                
                        
// glbegin points, glcolor 1 0 0, glvertex 0 0.5 0.5, glcolor 0 1 0, glvertex 0.5 0 0.2, glcolor 0 0 0.2, glvertex 0.5 0.1 0.3, glend                        
                        
          
                xCoord++; // increment the x coordinate
                xCoord1++;
                paramsHash.clear(); // clear the params Hash
            }
        
            // now darken the other pixels
            darkenUnmatched(currKey);
            
                            
        } else {
            // this is the first packet (hashmap is empty)
            //hashList = new ArrayList(numPixels); // initialize the hashList
            post("first packet...");
            int[] colorFromIP = ipsToColor(rgbIP);
            paramsHash.put("sender", new String(ipPair[0]));
            paramsHash.put("receiver", new String(ipPair[1]));
            paramsHash.put("x", new Integer(0));
            paramsHash.put("y", new Integer(0));
            paramsHash.put("alpha", new Integer(0));
            paramsHash.put("red", new Integer(colorFromIP[0]));
            paramsHash.put("green", new Integer(colorFromIP[1]));
            paramsHash.put("blue", new Integer(colorFromIP[2]));
            
            // add paramsHash to the main hashmap
            hash.put(currKey, new HashMap(paramsHash));
            
            // send out the cell values
            outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(0), Atom.newAtom(0), Atom.newAtom("plane"), 
                    Atom.newAtom(0), Atom.newAtom("val"), Atom.newAtom(1)});
            outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(0), Atom.newAtom(0), Atom.newAtom("plane"), 
                    Atom.newAtom(1), Atom.newAtom("val"), Atom.newAtom(colorFromIP[0])});
            outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(0), Atom.newAtom(0), Atom.newAtom("plane"), 
                    Atom.newAtom(2), Atom.newAtom("val"), Atom.newAtom(colorFromIP[1])});
            outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(0), Atom.newAtom(0), Atom.newAtom("plane"), 
                    Atom.newAtom(3), Atom.newAtom("val"), Atom.newAtom(colorFromIP[2])});
                        
            // new pixel from new connection
            outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                    Atom.newAtom(0), Atom.newAtom("val"), Atom.newAtom(255)});
            outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                    Atom.newAtom(1), Atom.newAtom("val"), Atom.newAtom(0)});
            outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                    Atom.newAtom(2), Atom.newAtom("val"), Atom.newAtom(255)});
            outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                    Atom.newAtom(3), Atom.newAtom("val"), Atom.newAtom(0)});
            
            xCoord++; // increment the x coordinate
            xCoord1++;
            paramsHash.clear(); // clear the params Hash
        }
        
        // raw carnivore data
        outlet(2, Atom.newAtom(str));
            
    } // end messageReceived()
    
    // returns an array which represents the mixing of both ip numbers
    // into one 3 item array that will be the color.  the last 3
    // numbers are used to determin an rgb color value
    private int[] ipsToColor(int[][] ips) {
        int r = (ips[0][0] + ips[1][0]) / 2; // red value
        int g = (ips[0][1] + ips[1][1]) / 2; // green value
        int b = (ips[0][2] + ips[1][2]) / 2; // blue value
        finalRGB[0] = r;
        finalRGB[1] = g;
        finalRGB[2] = b;
        return finalRGB;
    }
    
    private void darkenUnmatched(String key) {
        post("darkening unmatched pixels...");
    
        Set set = new HashSet(hash.keySet());
        String[] keys = (String[]) set.toArray(new String[0]);
        
        // now darken the unmatched pixels
        for(int j = 0; j < keys.length; j++) {
            // copy the current parameters from the main hashmap
            paramsHash.putAll((HashMap) hash.get(keys[j]));
            
            int a;
            
            // darken the pixels
            if(!(keys[j].equals(key))) { // don't darken the matched element
                a = ((Integer) paramsHash.get("alpha")).intValue();
                if(a > 0) {
                    a--;
                    paramsHash.put("alpha", new Integer(a));
            
                    // store the new pixel data
                    hash.put(keys[j], new HashMap(paramsHash));
            
                    // get the current coords
                    int x = ((Integer) paramsHash.get("x")).intValue();
                    int y = ((Integer) paramsHash.get("y")).intValue();
            
                    // send out the cell values
                    outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom("plane"), 
                            Atom.newAtom(0), Atom.newAtom("val"), Atom.newAtom(a)});
                        /*
                    outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom("plane"), 
                            Atom.newAtom(1), Atom.newAtom("val"), Atom.newAtom(0)});
                    outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom("plane"), 
                            Atom.newAtom(2), Atom.newAtom("val"), Atom.newAtom(0)});
                    outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom("plane"), 
                            Atom.newAtom(3), Atom.newAtom("val"), Atom.newAtom(0)});
                        */
                }
            }
                    
            paramsHash.clear();
        }

    }
    
    private void lightenMatched(String key1, String key2) {
        post("lighteninging matched pixel...");
        String match;
        
        if(hash.containsKey(key1)) {
            match = key1;
        } else {
            match = key2;
        }
        
        // copy the current parameters from the main hashmap
        paramsHash.putAll((HashMap) hash.get(match));
        
        // lighten the matched pixel
        int a = ((Integer) paramsHash.get("alpha")).intValue();
        if(a < 255) {
            a+= 10;
            if(a > 255)
                a = 255;
            paramsHash.put("alpha", new Integer(a));
        }
        
        // store the new pixel data
        hash.put(match, new HashMap(paramsHash));
        
        // get the current coords
        int x = ((Integer) paramsHash.get("x")).intValue();
        int y = ((Integer) paramsHash.get("y")).intValue();
            
        // send out the cell values
        outlet(0, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(x), Atom.newAtom(y), Atom.newAtom("plane"), 
                Atom.newAtom(0), Atom.newAtom("val"), Atom.newAtom(a)});
                    
        paramsHash.clear();
    }
    
    private void newPixelFromOldConnection() {
        if(xCoord1 > matrixDim[0]-1 && yCoord1 > matrixDim[1]-1) {
            // we've reached the end, so go to the beginiing
            // of the matrix
            xCoord1 = 0;
            yCoord1 = 0;
            outlet(1, Atom.newAtom("clear"));
        }
                
        if(xCoord1 > matrixDim[0]-1) {
            // go to the next row
            xCoord1 = 0;
            yCoord1++;
        }
    
        // new pixel from old connection
        outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                Atom.newAtom(0), Atom.newAtom("val"), Atom.newAtom(255)});
        outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                Atom.newAtom(1), Atom.newAtom("val"), Atom.newAtom(255)});
        outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                Atom.newAtom(2), Atom.newAtom("val"), Atom.newAtom(0)});
        outlet(1, new Atom[]{Atom.newAtom("setcell"), Atom.newAtom(xCoord1), Atom.newAtom(yCoord1), Atom.newAtom("plane"), 
                Atom.newAtom(3), Atom.newAtom("val"), Atom.newAtom(0)});
    
        xCoord1++;
    }
    
    public void start() {
        if(!running) {
                // init carni listener object
        	myCarni = new CarnivoreWrapper(portName, portNumber, this);
            if(matrixDim == null)
                matrixDim = new int[]{320, 240};
            numPixels = matrixDim[0] * matrixDim[1];
            hash = new HashMap(numPixels);
            
            try {
                myCarni.startCarnivore();
                //myCarni.getCarni().addCarniOutputListener(this);
                post("start PacketsToMatrix...");
                running = true;
            } catch(Exception e) {
                post("error connecting to carnivore");
            }
        }
    }

    public void stop() {
        //myCarni.getCarni().removeCarniOutputListener(this);
        myCarni.stopCarnivore();
        running = false;
        matrixDim = null;
        hash = null;
        paramsHash.clear();
        myCarni = null;
    }
    
    public void bang() {
        start();
    }
    
    /**
     * This is where we get the (optional) dimensions
     * of the matrix from Max
     *
     * @param atomArray The matrix dimensions from Max
     *
     */
    public void list(Atom[] atomArray)
    {
        int[] dim;
        // convert a Max message containing the dimensions to an int array
        dim = Atom.toInt(atomArray);
        
        matrixDim = new int[]{dim[0], dim[1]};
        
        start();
    }


    
}
