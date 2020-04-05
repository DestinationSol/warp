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

import com.badlogic.gdx.math.Vector2;
import org.destinationsol.Const;
import org.destinationsol.common.SolMath;
import org.destinationsol.common.SolRandom;
import org.destinationsol.game.AbilityCommonConfig;
import org.destinationsol.game.DmgType;
import org.destinationsol.game.ShipConfig;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.input.AiPilot;
import org.destinationsol.game.input.Guardian;
import org.destinationsol.game.item.ItemManager;
import org.destinationsol.game.item.MercItem;
import org.destinationsol.game.item.SolItem;
import org.destinationsol.game.planet.Planet;
import org.destinationsol.game.ship.AbilityConfig;
import org.destinationsol.game.ship.ShipAbility;
import org.destinationsol.game.ship.SolShip;
import org.destinationsol.game.ship.hulls.HullConfig;
import org.json.JSONObject;

public class SummonMercenariesAbility implements ShipAbility {
    private final SummonMercenariesAbilityConfig config;
    private float summonTimer;
    private SolShip[] mercenaries;
    private MercItem mercenary;

    public SummonMercenariesAbility(SummonMercenariesAbilityConfig config) {
        this.config = config;
        mercenaries = new SolShip[config.mercenaryCount];
    }

    @Override
    public boolean update(SolGame game, SolShip owner, boolean tryToUse) {
        if (mercenary == null) {
            mercenary = new MercItem(new ShipConfig(game.getHullConfigManager().getConfig(config.mercenaryName),
                    config.mercenaryItems, 0, 1, null, game.getItemMan(), ""));
        }

        if (tryToUse) {
            clearMercenaries(game);
            summonTimer = config.summonDuration;

            for (int mercenaryNo = 0; mercenaryNo < config.mercenaryCount; mercenaryNo++) {
                ShipConfig mercConfig = mercenary.getConfig();
                Guardian guardian = new Guardian(game, mercConfig.hull, owner.getPilot(), owner.getPosition(),
                        owner.getHull().config, SolRandom.randomFloat(180));
                AiPilot pilot = new AiPilot(guardian, true, owner.getPilot().getFaction(), false,"Merc", Const.AI_DET_DIST);
                Vector2 position = getPos(game, owner, mercConfig.hull);
                if (position == null) {
                    return false;
                }
                SolShip merc = game.getShipBuilder().buildNewFar(game, position, new Vector2(), 0, 0,
                        pilot, mercConfig.items, mercConfig.hull, null, true, mercConfig.money,
                        null, true)
                        .toObject(game);

                merc.setMerc(mercenary);
                mercenary.setSolShip(merc);

                game.getObjectManager().addObjDelayed(merc);
                mercenaries[mercenaryNo] = merc;
            }
            return true;
        }

        if (summonTimer <= 0) {
            clearMercenaries(game);
            return false;
        }

        summonTimer -= game.getTimeStep();
        return false;
    }

    /**
     * Finds the position at which to spawn the mercenary
     *
     * @param game The instance of the game we're dealing with
     * @param ship The ship
     * @param hull The hull of the mercenary in question
     * @return The position to spawn the mercenary at, or null for no available position
     * <p>
     * Taken from the engine module.
     */
    private static Vector2 getPos(SolGame game, SolShip ship, HullConfig hull) {
        Vector2 position = new Vector2();
        float dist = ship.getHull().config.getApproxRadius() + Guardian.DIST + hull.getApproxRadius();
        Vector2 heroPos = ship.getPosition();
        Planet nearestPlanet = game.getPlanetManager().getNearestPlanet(heroPos);
        boolean nearGround = nearestPlanet.isNearGround(heroPos);
        float fromPlanet = SolMath.angle(nearestPlanet.getPosition(), heroPos);
        for (int i = 0; i < 50; i++) {
            float relAngle;
            if (nearGround) {
                relAngle = fromPlanet;
            } else {
                relAngle = SolRandom.randomInt(180);
            }
            SolMath.fromAl(position, relAngle, dist);
            position.add(heroPos);
            if (game.isPlaceEmpty(position, false)) {
                return position;
            }
            dist += Guardian.DIST;
        }
        return null;
    }

    private void clearMercenaries(SolGame game) {
        for (int mercenaryNo = 0; mercenaryNo < config.mercenaryCount; mercenaryNo++) {
            if (mercenaries[mercenaryNo] == null) {
                continue;
            }
            // Make the mercenaries self-destruct
            mercenaries[mercenaryNo].getItemContainer().clear();
            mercenaries[mercenaryNo].receiveDmg(mercenaries[mercenaryNo].getLife() + 1, game, mercenaries[mercenaryNo].getPosition(), DmgType.ENERGY);
        }
    }

    @Override
    public AbilityConfig getConfig() {
        return config;
    }

    @Override
    public AbilityCommonConfig getCommonConfig() {
        return config.getCommonConfig();
    }

    @Override
    public float getRadius() {
        return Float.MAX_VALUE;
    }

    public static class SummonMercenariesAbilityConfig implements AbilityConfig {
        private final float rechargeTime;
        private final float summonDuration;
        private String mercenaryName;
        private final int mercenaryCount;
        private String mercenaryItems;
        private final AbilityCommonConfig commonConfig;

        public SummonMercenariesAbilityConfig(float rechargeTime, float summonDuration, String mercenaryName,
                                              int mercenaryCount, String mercenaryItems, AbilityCommonConfig commonConfig) {
            this.rechargeTime = rechargeTime;
            this.summonDuration = summonDuration;
            this.commonConfig = commonConfig;
            this.mercenaryName = mercenaryName;
            this.mercenaryItems = mercenaryItems;
            this.mercenaryCount = mercenaryCount;
        }

        @Override
        public ShipAbility build() {
            return new SummonMercenariesAbility(this);
        }

        @Override
        public SolItem getChargeExample() {
            return null;
        }

        @Override
        public float getRechargeTime() {
            return rechargeTime;
        }

        public AbilityCommonConfig getCommonConfig() {
            return commonConfig;
        }

        @Override
        public void appendDesc(StringBuilder sb) {
            sb.append("Summons some mercenaries to aid the ship.");
        }

        public static AbilityConfig load(JSONObject abNode, ItemManager itemManager, AbilityCommonConfig cc) {
            float rechargeTime = abNode.getFloat("rechargeTime");
            float summonDuration = abNode.getFloat("summonDuration");
            String mercenaryType = abNode.getString("mercenary");
            String mercenaryItems = abNode.getString("mercenaryItems");
            int mercenaryCount = abNode.getInt("mercenaryCount");
            return new SummonMercenariesAbilityConfig(rechargeTime, summonDuration, mercenaryType, mercenaryCount, mercenaryItems, cc);
        }
    }
}
