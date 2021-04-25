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
package org.destinationsol.warp.research.providers;

import org.destinationsol.game.SolGame;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.warp.research.actions.ResearchAction;

/**
 * Represents a provider of research, which can allocate and record the research done
 */
public interface ResearchProvider {
    /**
     * Returns the name for the ResearchProvider, which is used to maintain its state.
     * @return the name of the ResearchProvider.
     */
    String getName();

    /**
     * Returns true if the provider is currently capable of doing research
     * @param game the game to research in
     * @param researchShip the ship to research with
     * @return if the provided can currently do any research
     */
    boolean canProvideResearch(SolGame game, SolShip researchShip);

    /**
     * Obtains the current research action
     * @param game the game to research in
     * @param researchShip the ship to research with
     * @return the current research action
     */
    ResearchAction getAction(SolGame game, SolShip researchShip);

    /**
     * Obtains the currently discovered research actions
     * @return the currently discovered actions
     */
    ResearchAction[] getDiscoveredActions();

    /**
     * Resets the internal state of the research provider.
     */
    void reset();
}
