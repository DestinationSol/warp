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
package org.destinationsol.warp.abilities;

import org.destinationsol.game.AbilityCommonConfig;
import org.destinationsol.game.Hero;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.StarPort;
import org.destinationsol.game.item.ItemManager;
import org.destinationsol.game.item.SolItem;
import org.destinationsol.game.planet.Planet;
import org.destinationsol.game.ship.AbilityConfig;
import org.destinationsol.game.ship.ShipAbility;
import org.destinationsol.game.ship.SolShip;
import org.json.JSONObject;

public class PlanetTunnelAbility implements ShipAbility {
    private final PlanetTunnelAbilityConfig config;
    private StarPort.Transcendent transcendentHero;
    private SolShip originalHero;

    public PlanetTunnelAbility(PlanetTunnelAbilityConfig config) {
        this.config = config;
    }

    @Override
    public boolean update(SolGame game, SolShip owner, boolean tryToUse) {
        Hero hero = game.getHero();
        if (tryToUse && !hero.isTranscendent()) {
            Planet nearestPlanet = game.getPlanetManager().getNearestPlanet();
            transcendentHero = new StarPort.Transcendent(owner, nearestPlanet, nearestPlanet, game);
            originalHero = hero.getShip();
            hero.setTranscendent(transcendentHero);
            return true;
        }

        if (transcendentHero != null && hero.isTranscendent()) {
            transcendentHero.update(game);
        }

        if (!hero.isTranscendent() && originalHero != null) {
            game.getObjectManager().removeObjDelayed(originalHero);
            originalHero = null;
        }

        return false;
    }

    @Override
    public AbilityConfig getConfig() {
        return config;
    }

    @Override
    public AbilityCommonConfig getCommonConfig() {
        return config.commonConfig;
    }

    @Override
    public float getRadius() {
        return Float.MAX_VALUE;
    }

    public static class PlanetTunnelAbilityConfig implements AbilityConfig {
        private final float rechargeTime;
        private final AbilityCommonConfig commonConfig;

        public PlanetTunnelAbilityConfig(float rechargeTime, AbilityCommonConfig commonConfig) {
            this.rechargeTime = rechargeTime;
            this.commonConfig = commonConfig;
        }

        @Override
        public ShipAbility build() {
            return new PlanetTunnelAbility(this);
        }

        @Override
        public SolItem getChargeExample() {
            return null;
        }

        @Override
        public float getRechargeTime() {
            return rechargeTime;
        }

        @Override
        public void appendDesc(StringBuilder sb) {
            sb.append("Transports the ship to the nearest planet via the most direct route.");
        }

        public static AbilityConfig load(JSONObject abNode, ItemManager itemManager, AbilityCommonConfig cc) {
            float rechargeTime = (float) abNode.getDouble("rechargeTime");
            return new PlanetTunnelAbilityConfig(rechargeTime, cc);
        }
    }
}
