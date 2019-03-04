//
//  CarniOutputListener.java
//  CarniOutputListener
//
//  Created by Carlos Castellanos on 10/21/04.
//  Copyright 2004 __MyCompanyName__. All rights reserved.
//

package carlos.maxivore;

import java.util.EventListener;

public interface CarniOutputListener extends EventListener {
    public abstract void messageReceived(CarnivoreListener cl, String s);
}
