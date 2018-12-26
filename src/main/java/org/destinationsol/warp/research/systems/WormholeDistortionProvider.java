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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import org.destinationsol.assets.Assets;
import org.destinationsol.common.SolRandom;
import org.destinationsol.game.DmgType;
import org.destinationsol.game.FarObject;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.SolObject;
import org.destinationsol.game.UpdateAwareSystem;
import org.destinationsol.game.attributes.RegisterUpdateSystem;
import org.destinationsol.game.drawables.Drawable;
import org.destinationsol.game.drawables.DrawableLevel;
import org.destinationsol.game.drawables.RectSprite;
import org.destinationsol.game.planet.SolSystem;
import org.destinationsol.game.ship.ForceBeacon;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.game.ship.hulls.HullConfig;

import java.util.ArrayList;
import java.util.List;

@RegisterUpdateSystem
public class WormholeDistortionProvider implements UpdateAwareSystem {
    private static final String WORMHOLE_TEXTURE_PATH = "warp:distortionProjectile";
    private static final int WORMHOLE_MIN = 4000;
    private static final int WORMHOLE_MAX = 8000;
    private final TextureAtlas.AtlasRegion wormholeTexture;
    private List<DistortionObject> wormholes = new ArrayList<DistortionObject>();

    public WormholeDistortionProvider() {
        wormholeTexture = Assets.getAtlasRegion(WORMHOLE_TEXTURE_PATH);
    }

    @Override
    public void update(SolGame game, float timeStep) {
        if (wormholes.isEmpty()) {
            Vector2 worldExtentsMin = new Vector2();
            Vector2 worldExtentsMax = new Vector2();
            for (SolSystem system : game.getPlanetManager().getSystems()) {
                Vector2 systemPosition = system.getPosition();
                if (systemPosition.x < worldExtentsMin.x) {
                    worldExtentsMin.x = systemPosition.x - system.getRadius();
                } else if (systemPosition.x > worldExtentsMax.x) {
                    worldExtentsMax.x = systemPosition.x;
                }

                if (systemPosition.y < worldExtentsMin.y) {
                    worldExtentsMin.y = systemPosition.y - system.getRadius();
                } else if (systemPosition.y > worldExtentsMax.y) {
                    worldExtentsMax.y = systemPosition.y;
                }
            }

            for (int i = 0; i < SolRandom.seededRandomInt(WORMHOLE_MIN, WORMHOLE_MAX); i++) {
                Vector2 position;
                do {
                    float positionValueX = SolRandom.seededRandomFloat(worldExtentsMin.x, worldExtentsMax.x);
                    float positionValueY = SolRandom.seededRandomFloat(worldExtentsMin.y, worldExtentsMax.y);
                    position = new Vector2(positionValueX, positionValueY);
                } while (!game.isPlaceEmpty(position, true));

                Vector2 targetPosition;
                do {
                    float targetValueX = SolRandom.seededRandomFloat(worldExtentsMin.x, worldExtentsMax.x);
                    float targetValueY = SolRandom.seededRandomFloat(worldExtentsMin.y, worldExtentsMax.y);
                    targetPosition = new Vector2(targetValueX, targetValueY);
                } while (!game.isPlaceEmpty(targetPosition, true) && targetPosition.dst(position) > 10.0f);

                DistortionObject entryWormhole = new DistortionObject(position, targetPosition, 10.0f);
                wormholes.add(entryWormhole);
                game.getObjectManager().addObjDelayed(entryWormhole);

                DistortionObject exitWormhole = new DistortionObject(targetPosition, position, 10.0f);
                wormholes.add(exitWormhole);
                game.getObjectManager().addObjDelayed(exitWormhole);
            }
        }
    }

    private class DistortionObject implements SolObject {
        private final Vector2 wormholePosition;
        private final Vector2 target;
        private final List<Drawable> drawables = new ArrayList<Drawable>();
        private float wormholeStability;

        public DistortionObject(Vector2 wormholePosition, Vector2 target, float stability) {
            this.wormholePosition = wormholePosition;
            this.target = target;
            wormholeStability = stability;
            drawables.add(new RectSprite(wormholeTexture, 1, 0, 0, Vector2.Zero,
                    DrawableLevel.PART_FG_0, 0, 0, Color.WHITE, false));
        }

        @Override
        public void update(SolGame game) {
            SolShip approachingShip = ForceBeacon.pullShips(game, this, wormholePosition, null, null, ForceBeacon.MAX_PULL_DIST);

            if (approachingShip != null && approachingShip.getHull().getHullConfig().getType() != HullConfig.Type.STATION
                    && approachingShip.getPosition().dst(wormholePosition) < 0.1f) {
                // NOTE: Setting the same ship angle causes it to deviate until it reaches NaN and crashes.
                approachingShip.getHull().getBody().setTransform(target, 0);
            }
        }

        @Override
        public boolean shouldBeRemoved(SolGame game) {
            return false;
        }

        @Override
        public void onRemove(SolGame game) {

        }

        @Override
        public void receiveDmg(float dmg, SolGame game, Vector2 position, DmgType dmgType) {
            // Wormholes cannot be damaged, except by energy weapons
            // TODO: the wormholes are not able to detect collisions yet
            if (dmgType == DmgType.ENERGY) {
                wormholeStability -= dmg;
            }
        }

        @Override
        public boolean receivesGravity() {
            return false;
        }

        @Override
        public void receiveForce(Vector2 force, SolGame game, boolean acc) {

        }

        @Override
        public Vector2 getPosition() {
            return wormholePosition;
        }

        @Override
        public FarObject toFarObject() {
            return new FarDistortionObject(wormholePosition, target, wormholeStability);
        }

        public List<Drawable> getDrawables() {
            return drawables;
        }

        @Override
        public float getAngle() {
            return 0;
        }

        @Override
        public Vector2 getSpeed() {
            return Vector2.Zero;
        }

        @Override
        public void handleContact(SolObject other, float absImpulse, SolGame game, Vector2 collPos) {
        }

        @Override
        public Boolean isMetal() {
            return false;
        }

        @Override
        public boolean hasBody() {
            return false;
        }
    }

    public class FarDistortionObject implements FarObject {
        private final Vector2 wormholePosition;
        private final Vector2 target;
        private final float stability;

        public FarDistortionObject(Vector2 wormholePosition, Vector2 target, float stability) {
            this.wormholePosition = wormholePosition;
            this.target = target;
            this.stability = stability;
        }

        @Override
        public boolean shouldBeRemoved(SolGame game) {
            return false;
        }

        @Override
        public SolObject toObject(SolGame game) {
            return new DistortionObject(wormholePosition, target, stability);
        }

        @Override
        public void update(SolGame game) {

        }

        @Override
        public float getRadius() {
            return 1;
        }

        @Override
        public Vector2 getPosition() {
            return wormholePosition;
        }

        @Override
        public String toDebugString() {
            return "";
        }

        @Override
        public boolean hasBody() {
            return false;
        }
    }
}
