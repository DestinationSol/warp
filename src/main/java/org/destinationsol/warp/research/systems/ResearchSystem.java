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

import com.badlogic.gdx.math.MathUtils;
import org.destinationsol.SolApplication;
import org.destinationsol.game.Hero;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.UpdateAwareSystem;
import org.destinationsol.game.attributes.RegisterUpdateSystem;
import org.destinationsol.game.item.SolItem;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.ui.nui.NUIManager;
import org.destinationsol.ui.nui.NUIScreenLayer;
import org.destinationsol.ui.nui.screens.MainGameScreen;
import org.destinationsol.ui.nui.widgets.UIWarnButton;
import org.destinationsol.warp.research.actions.ResearchAction;
import org.destinationsol.warp.research.providers.ResearchProvider;
import org.destinationsol.warp.research.providers.PlanetResearchProvider;
import org.destinationsol.warp.research.providers.SolarResearchProvider;
import org.destinationsol.warp.research.providers.WormholeResearchProvider;
import org.destinationsol.warp.research.uiScreens.ResearchUiScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.Keyboard;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.ColumnLayout;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@RegisterUpdateSystem
public class ResearchSystem implements UpdateAwareSystem {
    private static final Logger logger = LoggerFactory.getLogger(ResearchSystem.class);
    private static float researchPoints;
    private static final String RESEARCH_POINT_ICON_PATH = "warp:researchPointIcon";
    private static final String[] DEFAULT_RESEARCH_SHIPS = new String[] {
            "warp:scout",
            "warp:explorer"
    };
    private static final ResearchProvider[] DEFAULT_RESEARCH_PROVIDERS = new ResearchProvider[] {
            new PlanetResearchProvider(),
            new SolarResearchProvider(),
            new WormholeResearchProvider()
    };
    private static final float RESEARCH_EXCHANGE_RATE = 4;
    private static List<String> researchShips;
    private static List<ResearchProvider> researchProviders;
    private static boolean researchButtonPressed;
    private ResearchUiScreen researchUiScreen;
    private UIWarnButton researchButton;

    static {
        researchShips = new ArrayList<String>();
        for (String defaultShip : DEFAULT_RESEARCH_SHIPS) {
            addResearchShip(defaultShip);
        }

        researchProviders = new ArrayList<ResearchProvider>();
        for (ResearchProvider provider : DEFAULT_RESEARCH_PROVIDERS) {
            addResearchProvider(provider);
        }
    }

    @Inject
    public ResearchSystem() {
    }

    @Override
    public void update(SolGame game, float timeStep) {
        if (game.getHero().isDead()) {
            return;
        }

        SolApplication application = game.getSolApplication();
        if (researchUiScreen == null) {
            researchButtonPressed = false;
            insertResearchButton(application);

            // Only show the research UI when flying a research-capable ship.
            // Don't check if the player is currently transcendent.
            if (!game.getHero().isTranscendent() && researchShips.contains(
                    game.getHero().getShip().getHull().getHullConfig().getInternalName())) {
                // Either started a new game or continued an existing one.
                researchUiScreen = new ResearchUiScreen(this);

                game.getItemMan().parseItems("warp:researchCharge");
                SolItem researchItem = game.getItemMan().getExample("warp:researchCharge");
                // If it's a new game (no research charge present), then reset all research.
                if (game.getHero().getItemContainer().count(researchItem) <= 0) {
                    game.getHero().getItemContainer().add(researchItem);
                    researchPoints = 0;
                    resetResearchProviders();
                }
            }
        }

        if (researchButtonPressed) {
            boolean screenShown = application.getInputManager().isScreenOn(researchUiScreen);
            if (screenShown) {
                application.getNuiManager().setScreen(application.getGame().getScreens().mainGameScreen);
                researchButtonPressed = researchUiScreen.isClosing();
            } else {
                application.getInputManager().addScreen(application, researchUiScreen);
                researchButtonPressed = false;
            }
        }

        Hero hero = game.getHero();
        if (!hero.isTranscendent() && researchShips.contains(hero.getShip().getHull().getHullConfig().getInternalName())) {
            researchButton.setVisible(true);
            SolShip researchShip = hero.getShip();
            for (ResearchProvider provider : researchProviders) {
                if (provider.canProvideResearch(game, researchShip)) {
                    ResearchAction action = provider.getAction(game, researchShip);
                    if (action != null) {
                        researchPoints += action.doResearch(game, researchShip);
                        researchButton.enableWarn();
                    }
                }
            }
        } else {
            researchButton.setVisible(false);
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
    public static void addResearchShip(String shipName) {
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

    private void insertResearchButton(SolApplication application) {
        NUIManager nuiManager = application.getNuiManager();
        if (nuiManager.hasScreenOfType(MainGameScreen.class)) {
            for (NUIScreenLayer uiScreen : nuiManager.getScreens()) {
                if (uiScreen instanceof MainGameScreen) {
                    ColumnLayout menuItems;
                    if (application.isMobile()) {
                        menuItems = uiScreen.find("rightMenuList", ColumnLayout.class);
                    } else {
                        menuItems = uiScreen.find("menuList", ColumnLayout.class);
                    }

                    if (menuItems == null) {
                        logger.error("Could not find NUI menu list for MainGameScreen! Has the UI layout changed?");
                        break;
                    }

                    // Check for an existing button, since NUI screens are not currently unloaded until the game exits
                    for (UIWidget menuItem : menuItems) {
                        if (menuItem instanceof UIWarnButton && ((UIWarnButton)menuItem).getText().equals("Research")) {
                            researchButton = (UIWarnButton) menuItem;
                            return;
                        }
                    }

                    researchButton = new UIWarnButton();
                    // TODO: Don't hard-code the button key binding
                    researchButton.setKey(Keyboard.Key.R);
                    researchButton.setText("Research");
                    researchButton.subscribe(button -> researchButtonPressed = true);
                    menuItems.addWidget(researchButton);
                    return;
                }
            }
        }
    }

    private static void resetResearchProviders() {
        for (ResearchProvider provider : researchProviders) {
            provider.reset();
        }
    }
}
