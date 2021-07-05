/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.destinationsol.warp.research.actions;

import org.destinationsol.game.SolGame;
import org.destinationsol.game.planet.Planet;
import org.destinationsol.game.ship.SolShip;

public class PlanetResearchAction implements ResearchAction {
    private static final float DEFAULT_PLANET_YIELD = 0.5f;
    private static final float DEFAULT_PLANET_YIELD_MAX = 120;
    private final Planet planetToResearch;
    private float research;

    public PlanetResearchAction(Planet planetToResearch) {
        this.planetToResearch = planetToResearch;
    }

    @Override
    public float getMaxYield() {
        return DEFAULT_PLANET_YIELD_MAX * (planetToResearch.getSystem().getConfig().hard ? 2 : 1);
    }

    @Override
    public float doResearch(SolGame game, SolShip researchShip) {
        if (isResearchComplete()) {
            return 0;
        }

        if (planetToResearch.isNearGround(researchShip.getPosition())) {
            float researchPoints = DEFAULT_PLANET_YIELD * (planetToResearch.getSystem().getConfig().hard ? 2 : 1) * game.getTimeStep();
            research += researchPoints;
            return researchPoints;
        }

        return 0;
    }

    /**
     * Checks if the research has been completed
     *
     * @return if the research has been completed
     */
    @Override
    public boolean isResearchComplete() {
        return (research >= getMaxYield());
    }

    /**
     * Returns a description of the action to be completed
     *
     * @return the description
     */
    @Override
    public String getObjective() {
        return "Study " + planetToResearch.getName() + " in " + planetToResearch.getSystem().getName() + " "
                + Integer.toString((int) research) + "/" + Integer.toString((int) getMaxYield());
    }

    /**
     * Returns a description of how to complete the action required
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return "Land on " + planetToResearch.getName() + " in " + planetToResearch.getSystem().getName()
                + " and explore. The research will automatically accumulate when near the planet's surface.";
    }
}
