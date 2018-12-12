package org.destinationsol.warp.abilities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import org.destinationsol.assets.Assets;
import org.destinationsol.game.AbilityCommonConfig;
import org.destinationsol.game.SolGame;
import org.destinationsol.game.drawables.DrawableLevel;
import org.destinationsol.game.drawables.DrawableObject;
import org.destinationsol.game.drawables.RectSprite;
import org.destinationsol.game.item.ItemManager;
import org.destinationsol.game.item.Shield;
import org.destinationsol.game.item.SolItem;
import org.destinationsol.game.ship.AbilityConfig;
import org.destinationsol.game.ship.ShipAbility;
import org.destinationsol.game.ship.SolShip;
import org.json.JSONObject;

import java.util.Collections;

public class WarpShieldAbility implements ShipAbility {
    private static final float SHIELD_TRANSPARENCY = 0.2f;
    private final WarpShieldAbilityConfig config;
    private Shield originalShield;
    private float shieldTimer;

    public WarpShieldAbility(WarpShieldAbilityConfig config) {
        this.config = config;
    }

    @Override
    public boolean update(SolGame game, SolShip owner, boolean tryToUse) {
       if (tryToUse) {
           originalShield = owner.getShield();
           shieldTimer = config.shieldDuration;
           return true;
       }

       if (shieldTimer <= 0 || owner.shouldBeRemoved(game)) {
           owner.getItemContainer().remove(config.warpShield);
           if (originalShield != null) {
               owner.maybeEquip(game, originalShield, true);
               originalShield = null;
           }
           return false;
       } else {
           boolean warpShieldPresent = false;
           for (int i = 0; i < owner.getItemContainer().groupCount(); i++) {
               for (SolItem item : owner.getItemContainer().getGroup(i)) {
                   if (item == config.warpShield) {
                       warpShieldPresent = true;
                   }
               }
           }

           if (!warpShieldPresent) {
               owner.getItemContainer().add(config.warpShield);
               owner.maybeEquip(game, config.warpShield, true);
           }

           owner.maybeEquip(game, config.warpShield, true);
           RectSprite shieldSprite = new RectSprite(config.warpShieldTex, owner.getHull().config.getSize() * Shield.SIZE_PERC * 2, 0, 0,
                   new Vector2(), DrawableLevel.PART_FG_0, 0, 0, Color.WHITE, true);
           game.getObjectManager().addObjDelayed(new DrawableObject(Collections.singletonList(shieldSprite),
                   owner.getPosition(), new Vector2(), null, true, false));
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
        private final TextureAtlas.AtlasRegion warpShieldTex;
        private final AbilityCommonConfig commonConfig;

        public WarpShieldAbilityConfig(float rechargeTime, float shieldDuration, Shield warpShield, TextureAtlas.AtlasRegion warpShieldTex, AbilityCommonConfig commonConfig) {
            this.rechargeTime = rechargeTime;
            this.shieldDuration = shieldDuration;
            this.commonConfig = commonConfig;
            this.warpShield = warpShield;
            this.warpShieldTex = warpShieldTex;
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
            String warpShieldTexName = abNode.optString("shieldTex", warpShieldName);
            itemManager.parseItems(warpShieldName);
            TextureAtlas.AtlasRegion warpShieldTex = null;
            try {
                warpShieldTex = Assets.getAtlasRegion(warpShieldTexName);
            } catch (Exception ignore) {
            }
            Shield shield = (Shield) itemManager.getExample(warpShieldName);
            return new WarpShieldAbilityConfig(rechargeTime, shieldDuration, shield, warpShieldTex, cc);
        }
    }
}
