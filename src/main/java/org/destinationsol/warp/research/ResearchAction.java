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
package org.destinationsol.warp.research;

import org.destinationsol.game.SolGame;
import org.destinationsol.game.ship.SolShip;

/**
 * Represents an instance of research that can be performed
 */
public interface ResearchAction {
    /**
     * Obtains the maximum amount of research possible from this activity.
     * @return the research quantity
     */
    float getMaxYield();

    /**
     * Does some research, potentially yielding research points
     * @param game the game instance to research in
     * @param researchShip the ship doing the research
     * @return the quantity of research points gained
     */
    float doResearch(SolGame game, SolShip researchShip);

    /**
     * Checks if the research has been completed
     * @return if the research has been completed
     */
    boolean isResearchComplete();

    /**
     * Returns a description of the objective of the action to be completed
     * @return the description
     */
    String getObjective();

    /**
     * Returns a description of how to complete the action required
     * @return the description
     */
    String getDescription();
}
