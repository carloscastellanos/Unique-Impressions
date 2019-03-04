//
//  Maxivore.java
//  Maxivore
//
//  Created by Carlos Castellanos on 1/17/05.
//  Copyright 2005. All rights reserved.
//

package carlos.maxivore;

import cc.netart.carnivore.*;
import com.cycling74.max.*;

public class Maxivore extends MaxObject implements CarniOutputListener {

    private String portName = "127.0.0.1";
    private int portNumber = 6667;
    private CarnivoreWrapper myCarni = new CarnivoreWrapper(portName, portNumber, this);
    private boolean running = false;

    // constructor
    Maxivore() {
        declareIO(1, 1);
        createInfoOutlet(false);
        setInletAssist(0, "bang or start message starts listening");
        setOutletAssist(0, "carnivore data");
    }
    
    // this method gets the carni data and sends it out
    public void messageReceived(CarnivoreListener carni, String str) {
        post(str);
        outlet(0, Atom.newAtom(str));
    }
    
    public void start() {
        if(!running) {
            try {
                myCarni.startCarnivore();
                //myCarni.getCarni().addCarniOutputListener(this);
                running = true;
            } catch( Exception e ) {
                post("error connecting to carnivore");
            }
        }
    }

    public void stop() {
        myCarni.stopCarnivore();
        running = false;
    }
    
    public void bang() {
        start();
    }
    
}