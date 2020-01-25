/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grondag.canvas.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder.ChunkData;

import grondag.canvas.mixinterface.AccessChunkRendererData;


@Mixin(ChunkData.class)
public class MixinChunkRenderData implements AccessChunkRendererData {
	@Shadow
	private Set<RenderLayer> initializedLayers;
	@Shadow
	private Set<RenderLayer> nonEmptyLayers;
	@Shadow
	private boolean empty;

	@Override
	public boolean canvas_markInitialized(RenderLayer renderLayer) {
		return initializedLayers.add(renderLayer);
	}

	@Override
	public void canvas_markPopulated(RenderLayer renderLayer) {
		empty = false;
		nonEmptyLayers.add(renderLayer);
	}
}
