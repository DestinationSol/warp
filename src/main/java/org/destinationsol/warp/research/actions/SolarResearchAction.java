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
import org.destinationsol.game.planet.SolSystem;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.warp.research.ResearchAction;

public class SolarResearchAction implements ResearchAction {
    private static final float SOLAR_YIELD = 6.0f;
    private static final float RESEARCH_DISTANCE_RATE = 0.06f;
    private final SolSystem solarSystem;
    private float currentSolarYield;

    public SolarResearchAction(SolSystem solarSystem) {
        this.solarSystem = solarSystem;
    }

    /**
     * Obtains the maximum amount of research possible from this activity.
     *
     * @return the research quantity
     */
    @Override
    public float getMaxYield() {
        return 200;
    }

    /**
     * Does some research, potentially yielding research points
     *
     * @param game         the game instance to research in
     * @param researchShip the ship doing the research
     * @return the quantity of research points gained
     */
    @Override
    public float doResearch(SolGame game, SolShip researchShip) {
        if (isResearchComplete()) {
            return 0;
        }

        float research = (SOLAR_YIELD / (solarSystem.getPosition().dst(researchShip.getPosition()) * RESEARCH_DISTANCE_RATE) * game.getTimeStep());
        currentSolarYield += research;
        return research;
    }

    /**
     * Checks if the research has been completed
     *
     * @return if the research has been completed
     */
    @Override
    public boolean isResearchComplete() {
        return (currentSolarYield >= getMaxYield());
    }

    /**
     * Returns a description of the action to be completed
     *
     * @return the description
     */
    @Override
    public String getObjective() {
        return "Study the sun of " + solarSystem.getName() + " " + Integer.toString((int) currentSolarYield) + "/"
                + Integer.toString((int) getMaxYield());
    }

    /**
     * Returns a description of how to complete the action required
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return "Get as close to the sun in the " + solarSystem.getName()
                + " system. The closer you get, the quicker this can be researched.";
    }
}
