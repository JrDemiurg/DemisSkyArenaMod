package net.jrdemiurge.skyarena.config;

import java.util.Map;

public class TrophyConfig {
    public int cooldown;
    public Map<String, EffectConfig> effects;

    public static class EffectConfig {
        public int duration;
        public int amplifier;
    }
}
