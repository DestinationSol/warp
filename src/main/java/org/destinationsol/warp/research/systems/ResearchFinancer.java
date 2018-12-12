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
package org.destinationsol.warp.research.systems;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import org.destinationsol.SolApplication;
import org.destinationsol.assets.Assets;
import org.destinationsol.game.Hero;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.UpdateAwareSystem;
import org.destinationsol.game.attributes.RegisterUpdateSystem;
import org.destinationsol.game.screens.MainGameScreen;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.warp.research.ResearchAction;
import org.destinationsol.warp.research.ResearchProvider;
import org.destinationsol.warp.research.providers.PlanetResearchProvider;
import org.destinationsol.warp.research.uiScreens.ResearchOverlayUiScreen;

import java.util.ArrayList;
import java.util.List;

@RegisterUpdateSystem()
public class ResearchFinancer implements UpdateAwareSystem {
    private static final String RESEARCH_POINT_ICON_PATH = "warp:researchPointIcon";
    private static final String[] DEFAULT_RESEARCH_SHIPS = new String[] {
            "warp:scout"
    };
    private static final ResearchProvider[] DEFAULT_RESEARCH_PROVIDERS = new ResearchProvider[] {
            new PlanetResearchProvider()
    };
    private static final float RESEARCH_EXCHANGE_RATE = 4;
    private static List<String> researchShips;
    private static List<ResearchProvider> researchProviders;
    private float researchPoints;
    private ResearchOverlayUiScreen researchOverlayUi;

    static {
        researchShips = new ArrayList<String>();
        for (String defaultShip : DEFAULT_RESEARCH_SHIPS) {
            researchShips.add(defaultShip);
        }

        researchProviders = new ArrayList<ResearchProvider>();
        for (ResearchProvider provider : DEFAULT_RESEARCH_PROVIDERS) {
            researchProviders.add(provider);
        }
    }

    public ResearchFinancer() {
        TextureAtlas.AtlasRegion researchPointIcon = Assets.getAtlasRegion(RESEARCH_POINT_ICON_PATH);
    }

    @Override
    public void update(SolGame game, float timeStep) {
        if (researchOverlayUi == null) {
            researchOverlayUi = new ResearchOverlayUiScreen(this);
        }

        SolApplication application = game.getSolApplication();
        if (researchOverlayUi != null && application.getInputManager().getTopScreen() instanceof MainGameScreen
            && !game.getScreens().mainGameScreen.hasOverlay(researchOverlayUi)) {
            game.getScreens().mainGameScreen.addOverlayScreen(researchOverlayUi);
        }

        Hero hero = game.getHero();
        if (!hero.isTranscendent() && researchShips.contains(hero.getShip().getHull().getHullConfig().getInternalName())) {
            SolShip researchShip = hero.getShip();
            for (ResearchProvider provider : researchProviders) {
                if (provider.canProvideResearch(game, researchShip)) {
                    ResearchAction action = provider.getAction(game, researchShip);
                    researchPoints += action.doResearch(game, researchShip);
                }
            }
        }
    }

    /**
     * Obtains the research points currently possessed by the player
     * @return the quantity of research points
     */
    public float getResearchPoints() {
        return researchPoints;
    }

    /**
     * Registers a ship for being eligible for collecting research
     * @param shipName the ship to the registered
     */
    public static void addResearchShips(String shipName) {
        if (researchShips.contains(shipName)) {
            return;
        }

        researchShips.add(shipName);
    }

    /**
     * Registers a new research provider with the system
     * @param provider the provider to register
     */
    public static void addResearchProvider(ResearchProvider provider) {
        if (researchProviders.contains(provider)) {
            return;
        }

        researchProviders.add(provider);
    }

    /**
     * Obtains the research providers currently registered with the system
     * @return the research providers registered
     */
    public static ResearchProvider[] getResearchProviders() {
        return researchProviders.toArray(new ResearchProvider[0]);
    }

    /**
     * Sells the research points at a fixed exchange rate
     * @param hero the hero selling the points
     * @param points the quantity of points to sell (clamped to the points that you have)
     */
    public void sellResearchPoints(Hero hero, float points) {
        researchPoints -= MathUtils.clamp(points, 0, researchPoints);
        hero.setMoney(hero.getMoney() + (points * RESEARCH_EXCHANGE_RATE));
    }
}
