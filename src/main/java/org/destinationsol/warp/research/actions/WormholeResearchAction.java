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
import org.destinationsol.game.planet.SolSystem;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.warp.research.systems.WormholeDistortionProvider;

public class WormholeResearchAction implements ResearchAction {
    private static final float WORMHOLE_YIELD = 1.0f;
    private static final float MAX_WORMHOLE_YIELD = 10.0f;
    private WormholeDistortionProvider.DistortionObject distortionObject;
    private SolSystem researchSystem;
    private float currentYield;

    public WormholeResearchAction(WormholeDistortionProvider.DistortionObject distortionObject, SolSystem researchSystem) {
        this.distortionObject = distortionObject;
        this.researchSystem = researchSystem;
    }

    /**
     * Obtains the maximum amount of research possible from this activity.
     *
     * @return the research quantity
     */
    @Override
    public float getMaxYield() {
        return MAX_WORMHOLE_YIELD;
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
        if (distortionObject.getPosition().dst(researchShip.getPosition()) > 2.0f || isResearchComplete()) {
            return 0;
        }

        float yield = WORMHOLE_YIELD * game.getTimeStep();
        currentYield += yield;
        return yield;
    }

    /**
     * Checks if the research has been completed
     *
     * @return if the research has been completed
     */
    @Override
    public boolean isResearchComplete() {
        return currentYield >= getMaxYield();
    }

    /**
     * Returns a description of the objective of the action to be completed
     *
     * @return the description
     */
    @Override
    public String getObjective() {
        return "Investigate gravitational anomaly near the " + researchSystem.getName() + " system.";
    }

    /**
     * Returns a description of how to complete the action required
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return "A gravitational disturbance has been detected near the " + researchSystem.getName() + " system." + "\n" +
                "Locate the source of the anomaly and investigate.";
    }

    public WormholeDistortionProvider.DistortionObject getDistortionObject() {
        return distortionObject;
    }
}
