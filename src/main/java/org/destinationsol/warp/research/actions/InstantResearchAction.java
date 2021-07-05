/*
 * Copyright 2021 The Terasology Foundation
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
import org.destinationsol.game.ship.SolShip;

public class InstantResearchAction implements ResearchAction {
    private float researchYield;
    private String objective;
    private String description;
    private boolean researchYielded;

    public InstantResearchAction(float researchYield, String objective, String description) {
        this.researchYield = researchYield;
        this.objective = objective;
        this.description = description;
    }

    /**
     * Obtains the maximum amount of research possible from this activity.
     *
     * @return the research quantity
     */
    @Override
    public float getMaxYield() {
        return researchYield;
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
        if (researchYielded) {
            return 0;
        }

        researchYielded = true;
        return researchYield;
    }

    /**
     * Checks if the research has been completed
     *
     * @return if the research has been completed
     */
    @Override
    public boolean isResearchComplete() {
        return researchYielded;
    }

    /**
     * Returns a description of the objective of the action to be completed
     *
     * @return the description
     */
    @Override
    public String getObjective() {
        return objective;
    }

    /**
     * Returns a description of how to complete the action required
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return description;
    }
}
