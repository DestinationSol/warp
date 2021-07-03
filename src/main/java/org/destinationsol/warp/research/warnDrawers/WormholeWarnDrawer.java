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
package org.destinationsol.warp.research.warnDrawers;

import com.badlogic.gdx.graphics.Color;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.screens.WarnDrawer;
import org.destinationsol.warp.research.systems.WormholeDistortionProvider;

public class WormholeWarnDrawer extends WarnDrawer {
    private static final float WARN_UPDATE_INTERVAL = 0.5f;
    private float warnUpdateTimer = WARN_UPDATE_INTERVAL;

    public WormholeWarnDrawer() {
        super("Distortion Near", Color.ORANGE);
    }

    @Override
    protected boolean shouldWarn(SolGame game) {
        warnUpdateTimer -= game.getTimeStep();
        if (warnUpdateTimer <= 0) {
            warnUpdateTimer = WARN_UPDATE_INTERVAL;
            return WormholeDistortionProvider.getWormholeObjects().stream().anyMatch(
                element -> element.getPosition().dst(game.getHero().getPosition()) < 5.0f);
        } else {
            return false;
        }
    }
}
