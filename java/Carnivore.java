//
//  Carnivore.java
//  Carnivore
//
//  Created by Carlos Castellanos on 10/21/04.
//  Copyright 2004 __MyCompanyName__. All rights reserved.
//

package carlos.maxivore;

public interface Carnivore {

    public abstract void addCarniOutputListener(CarniOutputListener col);

    public abstract void removeCarniOutputListener(CarniOutputListener col);
	
}
