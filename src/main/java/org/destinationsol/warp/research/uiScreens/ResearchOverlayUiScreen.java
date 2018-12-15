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

import org.destinationsol.SolApplication;
import org.destinationsol.game.screens.RightPaneLayout;
import org.destinationsol.ui.SolInputManager;
import org.destinationsol.ui.SolUiBaseScreen;
import org.destinationsol.ui.SolUiControl;
import org.destinationsol.warp.research.systems.ResearchSystem;

public class ResearchOverlayUiScreen extends SolUiBaseScreen {
    private final ResearchUiScreen researchUiScreen;
    private final ResearchSystem researchSystem;
    private SolUiControl researchButtonControl;
    private float lastResearchValue;

    public ResearchOverlayUiScreen(ResearchSystem researchSystem) {
        this.researchSystem = researchSystem;
        researchUiScreen = new ResearchUiScreen(researchSystem);
        lastResearchValue = researchSystem.getResearchPoints();
    }

    @Override
    public void onAdd(SolApplication application) {
        RightPaneLayout rightlayout = application.getLayouts().rightPaneLayout;
        researchButtonControl = new SolUiControl(rightlayout.buttonRect(5), true);
        researchButtonControl.setDisplayName("Research");
        controls.add(researchButtonControl);
    }

    @Override
    public void updateCustom(SolApplication solApplication, SolInputManager.InputPointer[] inputPointers, boolean clickedOutside) {

        float researchPoints = researchSystem.getResearchPoints();
        if (lastResearchValue < researchPoints) {
            researchButtonControl.enableWarn();
        }
        lastResearchValue = researchPoints;

        if (researchButtonControl.isJustOff()) {
            boolean screenShown = solApplication.getInputManager().isScreenOn(researchUiScreen);
            solApplication.getInputManager().setScreen(solApplication, solApplication.getGame().getScreens().mainGameScreen);
            if (!screenShown) {
                solApplication.getInputManager().addScreen(solApplication, researchUiScreen);
            }
        }
    }

}
