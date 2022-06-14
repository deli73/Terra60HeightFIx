package com.dfsek.terra.addons.chunkgenerator.config.noise;

import com.dfsek.terra.api.noise.NoiseSampler;
import com.dfsek.terra.api.properties.Properties;
import com.dfsek.terra.api.util.mutable.MutableInteger;


public record BiomeNoiseProperties(NoiseSampler base,
                                   NoiseSampler elevation,
                                   NoiseSampler carving,
                                   int blendDistance,
                                   int blendStep,
                                   double blendWeight,
                                   double elevationWeight,
                                   ThreadLocalNoiseHolder noiseHolder) implements Properties {
}
