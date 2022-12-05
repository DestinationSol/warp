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
package org.destinationsol.warp.research.uiScreens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import org.destinationsol.Const;
import org.destinationsol.SolApplication;
import org.destinationsol.common.SolColor;
import org.destinationsol.game.Hero;
import org.destinationsol.menu.MenuLayout;
import org.destinationsol.ui.SolInputManager;
import org.destinationsol.ui.SolUiBaseScreen;
import org.destinationsol.ui.SolUiControl;
import org.destinationsol.ui.UiDrawer;
import org.destinationsol.ui.nui.screens.MainGameScreen;
import org.destinationsol.warp.research.actions.ResearchAction;
import org.destinationsol.warp.research.providers.ResearchProvider;
import org.destinationsol.warp.research.systems.ResearchSystem;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ResearchUiScreen extends SolUiBaseScreen {
    private static final float BUTTON_WIDTH = 0.86f;
    private static final float BUTTON_HEIGHT = 0.04f;
    private static final int BUTTONS_PER_PAGE = Const.ITEM_GROUPS_PER_PAGE;
    private final ResearchSystem researchSystem;
    private Rectangle background;
    private SolUiControl researchTextControl;
    private Map<Integer, List<SolUiControl>> researchButtons;
    private SolUiControl previousButton;
    private SolUiControl nextButton;
    private SolUiControl sellResearchControl;
    private SolUiControl closeControl;
    private int actionsPage;

    public ResearchUiScreen(ResearchSystem researchSystem) {
        this.researchSystem = researchSystem;
    }

    public void onAdd(SolApplication application) {
        controls = new ArrayList<SolUiControl>();

        // NOTE: Removing the following line prevents the menu from being shown in-game.
        MenuLayout layout = application.getLayouts().menuLayout;

        background = new Rectangle(0.1f, 0.1f, 0.9f, 0.525f);

        researchTextControl = new SolUiControl(getButtonRectangle(0.105f, 0.105f), false);
        researchTextControl.setDisplayName("Research: " + Integer.toString((int) researchSystem.getResearchPoints()));
        controls.add(researchTextControl);

        researchButtons = new HashMap<Integer, List<SolUiControl>>();
        populateResearchButtons();

        float researchButtonsHeight = (BUTTONS_PER_PAGE * 1.05f * BUTTON_HEIGHT);
        sellResearchControl = new SolUiControl(getButtonRectangle(0.105f, 0.2f + researchButtonsHeight + BUTTON_HEIGHT), false);
        sellResearchControl.setDisplayName("Sell All Research");
        controls.add(sellResearchControl);

        previousButton = new SolUiControl(getHalfButtonRectangle(0.105f, 0.2f + researchButtonsHeight), false);
        previousButton.setDisplayName("Previous");
        controls.add(previousButton);

        nextButton = new SolUiControl(getHalfButtonRectangle(0.105f + (BUTTON_WIDTH / 2), 0.2f + researchButtonsHeight), false);
        nextButton.setDisplayName("Next");
        controls.add(nextButton);

        // TODO: Stop hard-coding the escape key when the exception
        //       "Denied access to class (not allowed with this module's permissions): org.destinationsol.GameOptions"
        //       is fixed.
        closeControl = new SolUiControl(null, false, Input.Keys.ESCAPE);
        controls.add(closeControl);

        actionsPage = 0;
    }

    @Override
    public void drawBackground(UiDrawer uiDrawer, SolApplication solApplication) {
        uiDrawer.draw(background, SolColor.UI_BG);
    }

    @Override
    public boolean isCursorOnBackground(SolInputManager.InputPointer inputPointer) {
        return background.contains(inputPointer.x, inputPointer.y);
    }

    @Override
    public void updateCustom(SolApplication solApplication, SolInputManager.InputPointer[] inputPointers, boolean clickedOutside) {
        if (clickedOutside) {
            closeControl.maybeFlashPressed(Input.Keys.ESCAPE);
            return;
        }

        if (closeControl.isJustOff()) {
            solApplication.getNuiManager().setScreen(solApplication.getGame().getScreens().mainGameScreen);
            return;
        }

        researchTextControl.setDisplayName("Research: " + Integer.toString((int) researchSystem.getResearchPoints()));

        researchButtons.values().forEach(buttons -> controls.removeAll(buttons));
        researchButtons.clear();
        populateResearchButtons();

        sellResearchControl.setEnabled(((MainGameScreen) solApplication.getNuiManager().getScreens().stream()
                .filter(screen -> screen instanceof MainGameScreen).findFirst().get())
                .getTalkButton().isEnabled()
                && (int) researchSystem.getResearchPoints() > 0 && !solApplication.getGame().getHero().isTranscendent());

        previousButton.setEnabled(actionsPage > 0);

        if (previousButton.isJustOff()) {
            actionsPage--;
        }

        nextButton.setEnabled(actionsPage < researchButtons.keySet().size() - 1);

        if (nextButton.isJustOff()) {
            actionsPage++;
        }

        if (sellResearchControl.isJustOff()) {
            Hero hero = solApplication.getGame().getHero();
            researchSystem.sellResearchPoints(hero, researchSystem.getResearchPoints());
        }
    }

    @Override
    public boolean reactsToClickOutside() {
        return true;
    }

    public boolean isClosing() {
        return closeControl.isJustOff();
    }

    private Rectangle getButtonRectangle(float x, float y) {
        return new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    private Rectangle getHalfButtonRectangle(float x, float y) {
        return new Rectangle(x, y, BUTTON_WIDTH / 2, BUTTON_HEIGHT);
    }

    private void populateResearchButtons() {
        int actionNo = 0;
        int heightOffset = 0;
        for (ResearchProvider provider : ResearchSystem.getResearchProviders()) {
            for (ResearchAction action : provider.getDiscoveredActions()) {
                int pageNo = actionNo / BUTTONS_PER_PAGE;
                if (actionNo % BUTTONS_PER_PAGE == 0) {
                    heightOffset = 0;
                } else {
                    heightOffset++;
                }

                SolUiControl researchButton = new SolUiControl(getButtonRectangle(0.105f, 0.15f + (heightOffset * 1.005f * BUTTON_HEIGHT)), false);
                researchButton.setDisplayName(action.getObjective());

                if (!researchButtons.containsKey(pageNo)) {
                    researchButtons.put(pageNo, new ArrayList<SolUiControl>());
                }

                researchButtons.get(pageNo).add(researchButton);
                actionNo++;
            }
        }

        if (researchButtons.size() > 0) {
            controls.addAll(researchButtons.get(actionsPage));
        }
    }
}
