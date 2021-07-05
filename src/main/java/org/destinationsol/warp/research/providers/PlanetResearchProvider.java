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
import org.destinationsol.game.planet.Planet;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.warp.research.actions.ResearchAction;
import org.destinationsol.warp.research.actions.PlanetResearchAction;

import java.util.HashMap;
import java.util.Map;

public class PlanetResearchProvider implements ResearchProvider {
    private Map<Planet, ResearchAction> planetResearchMap = new HashMap<Planet, ResearchAction>();

    public String getName() {
        return "PlanetResearchProvider";
    }

    /**
     * Returns true if the provider is currently capable of doing research
     *
     * @return if the provided can currently do any research
     */
    @Override
    public boolean canProvideResearch(SolGame game, SolShip researchShip) {
        Planet nearestPlanet = game.getPlanetManager().getNearestPlanet(researchShip.getPosition());

        if (!nearestPlanet.isNearGround(researchShip.getPosition())) {
            return false;
        }

        if (planetResearchMap.containsKey(nearestPlanet)) {
            // The planet has been researched
            return !planetResearchMap.get(nearestPlanet).isResearchComplete();
        } else {
            // The planet has not yet been researched
            return true;
        }
    }

    /**
     * Obtains the current research action
     *
     * @param game         the game to research in
     * @param researchShip the ship to research with
     * @return the current research action
     */
    @Override
    public ResearchAction getAction(SolGame game, SolShip researchShip) {
        Planet nearestPlanet = game.getPlanetManager().getNearestPlanet(researchShip.getPosition());
        if (!planetResearchMap.containsKey(nearestPlanet)) {
            planetResearchMap.put(nearestPlanet, new PlanetResearchAction(nearestPlanet));
        }
        return planetResearchMap.get(nearestPlanet);
    }

    /**
     * Obtains the currently discovered research actions
     *
     * @return the currently discovered actions
     */
    @Override
    public ResearchAction[] getDiscoveredActions() {
        return planetResearchMap.values().toArray(new ResearchAction[0]);
    }

    /**
     * Resets the internal state of the research provider.
     */
    @Override
    public void reset() {
        planetResearchMap.clear();
    }
}
