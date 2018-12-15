# The research system
## Overview
### Research
Currently, the research system is designed around the collection of research points.
These points have a pre-defined exchange rate to actual in-game currency, which allows
the player to use the science that they have collected to earn money instead of mining
asteroids or defeating enemies.
### Research Actions
To collect research, the player must perform scientific endeavours, such as visiting
new planets or collecting data from hostile environments. The "research" button should
glow orange when you are collecting research.

Currently, you can collect science in the following ways:
- [Solar Research](researchActions/solarResearchAction.md)
- [Planet Research](researchActions/planetResearchAction.md)

## API usage
### Adding a new research type
#### Research Actions
Research actions are the actual implementations of science gathering. They are invoked
every update through their ```doResearch``` method, which returns the quantity of
research points obtained that frame. A research action is simply a class inheriting
from ```ResearchProvider```. It cannot be used directly by the ResearchSystem and
must instead be provided to it via a ResearchProvider.

An example of a minimal research action is provided below:
```java
package org.destinationsol.warp.research.actions;

import org.destinationsol.game.SolGame;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.warp.research.ResearchAction;

public class MyResearchAction implements ResearchAction {
    private static final float RESEARCH_PER_SECOND = 1.5f;

    public float getMaxYield() {
        return Float.POSITIVE_INFINITY;
    }

    public float doResearch(SolGame game, SolShip researchShip) {
        return RESEARCH_PER_SECOND * game.getTimeStep();
    }

    public boolean isResearchComplete() {
        // This action never finishes
        return false;
    }

    public String getObjective() {
        return "Accumulating science...";
    }

    public String getDescription() {
        return "Accumulates science automatically.";
    }
}
```

See the Javadoc in
[```ResearchAction.java```](../src/main/java/org/destinationsol/warp/research/ResearchAction.java)
for more information on what each method is supposed to do.

For some other examples of implementing research actions, see
[SolarResearchAction.java](../src/main/java/org/destinationsol/warp/research/actions/SolarResearchAction.java)
and
[PlanetResearchAction.java](../src/main/java/org/destinationsol/warp/research/actions/PlanetResearchAction.java).

#### Research Providers
To discover what research the player can do, the ResearchSystem relies on research
providers to provide the research actions that can be performed. A research provider
is a class inheriting from ```ResearchProvider``` that has been registered with the
```ResearchSystem``` using the ```ResearchSystem.addResearchProvider``` method.

An example of a minimal research provider is provided below:
```java
package org.destinationsol.warp.research.providers;

import org.destinationsol.game.SolGame;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.warp.research.ResearchAction;
import org.destinationsol.warp.research.ResearchProvider;
import org.destinationsol.warp.research.actions.MyResearchAction;

public class MyResearchProvider implements ResearchProvider {
    private MyResearchAction myResearchAction;

    public MyResearchProvider() {
        myResearchAction = new MyResearchAction();
    }

    public boolean canProvideResearch(SolGame game, SolShip researchShip) {
        return true;
    }

    public ResearchAction getAction(SolGame game, SolShip researchShip) {
        return myResearchAction;
    }

    public ResearchAction[] getDiscoveredActions() {
        return new ResearchAction[1] { myResearchAction };
    }
}
```

See the Javadoc in
[```ResearchProvider.java```](../src/main/java/org/destinationsol/warp/research/ResearchProvider.java)
for more information on what each method is supposed to do.

For some other examples of implementing research providers, see
[SolarResearchProvider.java](../src/main/java/org/destinationsol/warp/research/providers/SolarResearchProvider.java)
and
[PlanetResearchProvider.java](../src/main/java/org/destinationsol/warp/research/providers/PlanetResearchProvider.java).
