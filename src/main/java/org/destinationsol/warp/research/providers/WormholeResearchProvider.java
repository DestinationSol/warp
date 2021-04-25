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
package org.destinationsol.warp.research.providers;

import org.destinationsol.game.SolGame;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.warp.research.actions.ResearchAction;
import org.destinationsol.warp.research.actions.WormholeResearchAction;
import org.destinationsol.warp.research.systems.WormholeDistortionProvider;

import java.util.HashMap;
import java.util.Map;

public class WormholeResearchProvider implements ResearchProvider {
    private Map<WormholeDistortionProvider.DistortionObject, WormholeResearchAction> wormholeResearchMap
            = new HashMap<WormholeDistortionProvider.DistortionObject, WormholeResearchAction>();

    public String getName() {
        return "WormholeResearchProvider";
    }

    /**
     * Returns true if the provider is currently capable of doing research
     *
     * @param game         the game to research in
     * @param researchShip the ship to research with
     * @return if the provided can currently do any research
     */
    @Override
    public boolean canProvideResearch(SolGame game, SolShip researchShip) {
        WormholeDistortionProvider.DistortionObject wormhole = getNearestWormhole(game, researchShip);
        if (wormhole == null) {
            return false;
        }

        ResearchAction wormholeAction = wormholeResearchMap.get(wormhole);
        if (wormholeAction == null) {
            return true;
        } else {
            return !wormholeAction.isResearchComplete();
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
        WormholeDistortionProvider.DistortionObject nearestWormhole = getNearestWormhole(game, researchShip);
        if (nearestWormhole == null) {
            return null;
        }

        WormholeResearchAction researchAction = wormholeResearchMap.get(nearestWormhole);
        if (researchAction == null) {
            researchAction = new WormholeResearchAction(nearestWormhole,
                    game.getPlanetManager().getNearestSystem(nearestWormhole.getPosition()));
            wormholeResearchMap.put(nearestWormhole, researchAction);
        }

        return researchAction;
    }

    /**
     * Obtains the currently discovered research actions
     *
     * @return the currently discovered actions
     */
    @Override
    public ResearchAction[] getDiscoveredActions() {
        return wormholeResearchMap.values().toArray(new ResearchAction[0]);
    }

    /**
     * Returns the nearest wormhole to the specified ship. It may return null if there are no wormholes present.
     *
     * @param game the game instance to use
     * @param researchShip the ship to check proximity with
     * @return the nearest wormhole, or null if there are none present
     */
    private WormholeDistortionProvider.DistortionObject getNearestWormhole(SolGame game, SolShip researchShip) {
        WormholeDistortionProvider.DistortionObject nearestObject = null;
        float nearestDistance = Float.POSITIVE_INFINITY;

        for (WormholeDistortionProvider.DistortionObject object : WormholeDistortionProvider.getWormholes()) {
            float distance = researchShip.getPosition().dst(object.getPosition());
            if (distance < nearestDistance && distance < 3.0f) {
                nearestDistance = distance;
                nearestObject = object;
            }
        }

        return nearestObject;
    }

    /**
     * Resets the internal state of the research provider.
     */
    @Override
    public void reset() {
        wormholeResearchMap.clear();
    }
}
