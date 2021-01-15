package com.dfsek.terra.config.loaders.config.biome;

import com.dfsek.tectonic.exception.LoadException;
import com.dfsek.tectonic.loading.ConfigLoader;
import com.dfsek.tectonic.loading.TypeLoader;
import com.dfsek.terra.api.math.ProbabilityCollection;
import com.dfsek.terra.api.math.noise.samplers.NoiseSampler;
import com.dfsek.terra.api.platform.TerraPlugin;
import com.dfsek.terra.api.world.biome.TerraBiome;
import com.dfsek.terra.biome.BiomeProvider;
import com.dfsek.terra.biome.StandardBiomeProvider;
import com.dfsek.terra.biome.pipeline.BiomePipeline;
import com.dfsek.terra.biome.pipeline.expand.FractalExpander;
import com.dfsek.terra.biome.pipeline.mutator.BorderMutator;
import com.dfsek.terra.biome.pipeline.mutator.ReplaceMutator;
import com.dfsek.terra.biome.pipeline.mutator.SmoothMutator;
import com.dfsek.terra.biome.pipeline.source.RandomSource;
import com.dfsek.terra.biome.pipeline.stages.ExpanderStage;
import com.dfsek.terra.biome.pipeline.stages.MutatorStage;
import com.dfsek.terra.config.loaders.Types;
import com.dfsek.terra.config.loaders.config.NoiseBuilderLoader;
import com.dfsek.terra.debug.Debug;
import com.dfsek.terra.generation.config.NoiseBuilder;
import net.jafama.FastMath;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class BiomeProviderBuilderLoader implements TypeLoader<BiomeProvider.BiomeProviderBuilder> {
    private final TerraPlugin main;

    public BiomeProviderBuilderLoader(TerraPlugin main) {
        this.main = main;
    }

    @Override
    public BiomeProvider.BiomeProviderBuilder load(Type t, Object c, ConfigLoader loader) throws LoadException {
        Map<String, Object> map = (Map<String, Object>) c;

        StandardBiomeProvider.StandardBiomeProviderBuilder builder = new StandardBiomeProvider.StandardBiomeProviderBuilder(seed -> {
            Map<String, Object> source = (Map<String, Object>) map.get("source");
            ProbabilityCollection<TerraBiome> sourceBiomes = (ProbabilityCollection<TerraBiome>) loader.loadType(Types.TERRA_BIOME_PROBABILITY_COLLECTION_TYPE, source.get("biomes"));
            NoiseSampler sourceNoise = new NoiseBuilderLoader().load(NoiseBuilder.class, source.get("noise"), loader).build(FastMath.toInt(seed));

            List<Map<String, Object>> stages = (List<Map<String, Object>>) map.get("pipeline");
            BiomePipeline.BiomePipelineBuilder pipelineBuilder = new BiomePipeline.BiomePipelineBuilder(2);

            for(Map<String, Object> stage : stages) {
                for(Map.Entry<String, Object> entry : stage.entrySet()) {
                    Map<String, Object> mutator = (Map<String, Object>) entry.getValue();
                    NoiseSampler mutatorNoise = new NoiseBuilderLoader().load(NoiseBuilder.class, mutator.get("noise"), loader).build(FastMath.toInt(seed));

                    if(entry.getKey().equals("expand")) {
                        if(mutator.get("type").equals("FRACTAL"))
                            pipelineBuilder.addStage(new ExpanderStage(new FractalExpander(mutatorNoise)));
                        else throw new LoadException("No such expander \"" + mutator.get("type"));
                    } else if(entry.getKey().equals("mutate")) {
                        if(mutator.get("type").equals("SMOOTH"))
                            pipelineBuilder.addStage(new MutatorStage(new SmoothMutator(mutatorNoise)));
                        else if(mutator.get("type").equals("REPLACE")) {
                            String fromTag = mutator.get("from").toString();
                            ProbabilityCollection<TerraBiome> replaceBiomes = (ProbabilityCollection<TerraBiome>) loader.loadType(Types.TERRA_BIOME_PROBABILITY_COLLECTION_TYPE, mutator.get("to"));
                            pipelineBuilder.addStage(new MutatorStage(new ReplaceMutator(fromTag, replaceBiomes, mutatorNoise)));
                        } else if(mutator.get("type").equals("BORDER")) {
                            String fromTag = mutator.get("from").toString();
                            String replaceTag = mutator.get("replace").toString();
                            ProbabilityCollection<TerraBiome> replaceBiomes = (ProbabilityCollection<TerraBiome>) loader.loadType(Types.TERRA_BIOME_PROBABILITY_COLLECTION_TYPE, mutator.get("to"));
                            pipelineBuilder.addStage(new MutatorStage(new BorderMutator(fromTag, replaceTag, mutatorNoise, replaceBiomes)));
                        } else throw new LoadException("No such mutator type \"" + mutator.get("type"));
                    } else throw new LoadException("No such mutator \"" + entry.getKey() + "\"");
                }
            }
            BiomePipeline pipeline = pipelineBuilder.build(new RandomSource(sourceBiomes, sourceNoise));
            Debug.info("Biome Pipeline scale factor: " + pipeline.getSize());
            return pipeline;
        }, main);
        if(map.containsKey("resolution")) builder.setResolution(Integer.parseInt(map.get("resolution").toString()));
        if(map.containsKey("blend")) {
            Map<String, Object> blend = (Map<String, Object>) map.get("blend");
            if(blend.containsKey("amplitude")) builder.setNoiseAmp(Integer.parseInt(blend.get("amplitude").toString()));
            if(blend.containsKey("noise"))
                builder.setBuilder(new NoiseBuilderLoader().load(NoiseBuilder.class, blend.get("noise"), loader));
        }
        return builder;
    }
}