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
import org.destinationsol.game.planet.SolarSystem;
import org.destinationsol.game.planet.SunSingleton;
import org.destinationsol.game.ship.ForceBeacon;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.game.ship.hulls.HullConfig;
import org.destinationsol.warp.research.warnDrawers.WormholeWarnDrawer;

import java.util.ArrayList;
import java.util.List;

@RegisterUpdateSystem(priority = Integer.MIN_VALUE)
public class WormholeDistortionProvider implements UpdateAwareSystem {
    private static final String WORMHOLE_TEXTURE_PATH = "warp:distortionProjectile";
    private static final int WORMHOLE_MIN = 100;
    private static final int WORMHOLE_MAX = 600;
    private static final int WORMHOLE_VISIBLE_DISTANCE = 5;
    private static final boolean WORMHOLE_DEBUG = false;
    private static List<Wormhole> wormholes = new ArrayList<Wormhole>();
    private static List<DistortionObject> wormholeObjects = new ArrayList<DistortionObject>();
    private final TextureAtlas.AtlasRegion wormholeTexture;
    private final WormholeWarnDrawer wormholeWarnDrawer;

    public WormholeDistortionProvider() {
        wormholeTexture = Assets.getAtlasRegion(WORMHOLE_TEXTURE_PATH);
        wormholeWarnDrawer = new WormholeWarnDrawer();
    }

    @Override
    public void update(SolGame game, float timeStep) {
        if (!game.getScreens().mainGameScreen.hasWarnDrawer(wormholeWarnDrawer)) {
            game.getScreens().mainGameScreen.addWarnDrawer(wormholeWarnDrawer);
        }

        if (wormholes.isEmpty()) {
            spawnWormholes(game);
            return;
        }

        Vector2 heroPosition = game.getHero().getPosition();
        for (Wormhole wormhole : wormholes) {
            boolean isEnabled = wormhole.isEnabled();
            if (wormhole.getPosition().dst(heroPosition) < WORMHOLE_VISIBLE_DISTANCE) {
                if (!isEnabled) {
                    if (wormhole.getInstance() == null) {
                        DistortionObject instance = new DistortionObject(wormhole.getPosition(), wormhole.getConnectedWormhole().getPosition(), 10.0f);
                        wormhole.setInstance(instance);
                    }

                    DistortionObject instance = wormhole.getInstance();
                    game.getObjectManager().addObjDelayed(instance);
                    wormholeObjects.add(instance);
                    wormhole.setEnabled(true);
                }
            } else if (isEnabled && wormhole.getInstance() != null) {
                DistortionObject instance = wormhole.getInstance();
                wormholeObjects.remove(instance);
                game.getObjectManager().removeObjDelayed(instance);
                wormhole.setEnabled(false);
            }
        }
    }

    private void spawnWormholes(SolGame game) {
        long seed = SolRandom.getSeed();

        List<SolarSystem> systems = game.getPlanetManager().getSystems();

        // TODO: Why is this here? Is it resetting the random number generator after generating
        // the wormholes to ensure consistency between warp-enabled and non-warp saves?
        SolRandom.setSeed(seed);

        List<Vector2> wormholePositions = new ArrayList<Vector2>();

        // Create wormholes
        int wormholeCount = SolRandom.seededRandomInt(WORMHOLE_MIN, WORMHOLE_MAX);
        if (wormholeCount % 2 != 0) {
            // There must always be an even number of wormholes, as they come in pairs.
            wormholeCount++;
        }
        int wormholesPerSystem = wormholeCount / systems.size();
        for (SolarSystem system : systems) {
            for (int i = 0; i < wormholesPerSystem; i++) {
                Vector2 position;
                do {
                    boolean positionRight = SolRandom.seededTest(0.5f);
                    boolean positionTop = SolRandom.seededTest(0.5f);

                    float positionValueX;
                    float positionValueY;
                    if (positionRight) {
                        // Left
                        positionValueX = SolRandom.seededRandomFloat(system.getPosition().x + SunSingleton.SUN_HOT_RAD,
                                system.getPosition().x + system.getRadius());
                    } else {
                        // Right
                        positionValueX = SolRandom.seededRandomFloat(system.getPosition().x - system.getRadius(),
                                system.getPosition().x - SunSingleton.SUN_HOT_RAD);
                    }

                    if (positionTop) {
                        // Top
                        positionValueY = SolRandom.seededRandomFloat(system.getPosition().y + SunSingleton.SUN_HOT_RAD,
                                system.getPosition().y + system.getRadius());
                    } else {
                        // Bottom
                        positionValueY = SolRandom.seededRandomFloat(system.getPosition().y - system.getRadius(),
                                system.getPosition().y - SunSingleton.SUN_HOT_RAD);
                    }

                    position = new Vector2(positionValueX, positionValueY);
                } while (!isPositionFree(game, wormholePositions, position));

                wormholePositions.add(position);
            }
        }

        // Link wormholes
        do {
            int wormholeNo = SolRandom.seededRandomInt(0, wormholePositions.size());

            int connectedWormholeNo;
            do {
                connectedWormholeNo = SolRandom.seededRandomInt(0, wormholePositions.size());
            } while (connectedWormholeNo == wormholeNo);

            Vector2 entryPosition = wormholePositions.get(wormholeNo);
            Vector2 exitPosition = wormholePositions.get(connectedWormholeNo);

            Wormhole entry = new Wormhole(entryPosition);
            Wormhole exit = new Wormhole(exitPosition);
            entry.setConnectedWormhole(exit);
            exit.setConnectedWormhole(entry);

            wormholes.add(entry);
            wormholes.add(exit);

            wormholePositions.remove(entryPosition);
            wormholePositions.remove(exitPosition);
        } while (!wormholePositions.isEmpty());

        SolRandom.setSeed(seed);

        // For debugging purposes, spawn two wormholes by the spawn point for testing.
        if (WORMHOLE_DEBUG) {
            Vector2 spawnPosition = game.getGalaxyFiller().getPlayerSpawnPos(game);
            Wormhole entry = new Wormhole(new Vector2(spawnPosition.x + 10, spawnPosition.y));
            Wormhole exit = new Wormhole(new Vector2(spawnPosition.x - 10, spawnPosition.y));
            entry.setConnectedWormhole(exit);
            exit.setConnectedWormhole(entry);
            wormholes.add(entry);
            wormholes.add(exit);
        }
    }

    private boolean isPositionFree(SolGame game, List<Vector2> wormholePositions, Vector2 position) {
        if (!game.isPlaceEmpty(position, true)) {
            return false;
        }

        for (Vector2 wormhole : wormholePositions) {
            if (wormhole.dst(position) < 2) {
                return false;
            }
        }

        return true;
    }

    // TODO: make this non-static
    public static List<DistortionObject> getWormholeObjects() {
        return wormholeObjects;
    }

    public class Wormhole {
        private final Vector2 position;
        private DistortionObject instance;
        private Wormhole connectedWormhole;
        private boolean enabled;

        public Wormhole(Vector2 position) {
            this.position = position;
        }

        public Vector2 getPosition() {
            return position;
        }

        public DistortionObject getInstance() {
            return instance;
        }

        public void setInstance(DistortionObject instance) {
            this.instance = instance;
        }

        public Wormhole getConnectedWormhole() {
            return connectedWormhole;
        }

        public void setConnectedWormhole(Wormhole connectedWormhole) {
            this.connectedWormhole = connectedWormhole;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public class DistortionObject implements SolObject {
        private final Vector2 wormholePosition;
        private final Vector2 target;
        private final List<Drawable> drawables = new ArrayList<Drawable>();
        private float wormholeStability;

        public DistortionObject(Vector2 wormholePosition, Vector2 target, float stability) {
            this.wormholePosition = wormholePosition;
            this.target = new Vector2(target).add(0.2f, 0.2f);
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
                approachingShip.receiveForce(approachingShip.getVelocity().cpy().scl(10, 10), game, true);
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
        public Vector2 getVelocity() {
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
