package org.destinationsol.warp.abilities;

import org.destinationsol.game.AbilityCommonConfig;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.item.ItemManager;
import org.destinationsol.game.item.Shield;
import org.destinationsol.game.item.SolItem;
import org.destinationsol.game.ship.AbilityConfig;
import org.destinationsol.game.ship.ShipAbility;
import org.destinationsol.game.ship.SolShip;
import org.json.JSONObject;

public class WarpShieldAbility implements ShipAbility {
    private final WarpShieldAbilityConfig config;
    private Shield originalShield;
    private float shieldTimer;
    private boolean warpShieldPresent;

    public WarpShieldAbility(WarpShieldAbilityConfig config) {
        this.config = config;
    }

    @Override
    public boolean update(SolGame game, SolShip owner, boolean tryToUse) {
       if (tryToUse) {
           originalShield = owner.getShield();
           if (!warpShieldPresent) {
               owner.getItemContainer().add(config.warpShield);
               warpShieldPresent = true;
           }
           owner.maybeEquip(game, config.warpShield, true);
           shieldTimer = config.shieldDuration;
           return true;
       }

       if (shieldTimer <= 0) {
           if (warpShieldPresent) {
               owner.getItemContainer().remove(config.warpShield);
               warpShieldPresent = false;
           }
           owner.maybeEquip(game, originalShield, true);
           return false;
       }

       shieldTimer -= game.getTimeStep();
       return false;
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

    public static class WarpShieldAbilityConfig implements AbilityConfig {
        private final float rechargeTime;
        private final float shieldDuration;
        private final Shield warpShield;
        private final AbilityCommonConfig commonConfig;

        public WarpShieldAbilityConfig(float rechargeTime, float shieldDuration, Shield warpShield, AbilityCommonConfig commonConfig) {
            this.rechargeTime = rechargeTime;
            this.shieldDuration = shieldDuration;
            this.commonConfig = commonConfig;
            this.warpShield = warpShield;
        }

        @Override
        public ShipAbility build() {
            return new WarpShieldAbility(this);
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
            sb.append("Activates the ship's warp shield, which redirects all projectiles in normal space around it.");
        }

        public static AbilityConfig load(JSONObject abNode, ItemManager itemManager, AbilityCommonConfig cc) {
            float rechargeTime = abNode.getFloat("rechargeTime");
            float shieldDuration = abNode.getFloat("shieldDuration");
            String warpShieldName = abNode.getString("shield");
            itemManager.parseItems(warpShieldName);
            Shield shield = (Shield) itemManager.getExample(warpShieldName);
            return new WarpShieldAbilityConfig(rechargeTime, shieldDuration, shield, cc);
        }
    }
}
