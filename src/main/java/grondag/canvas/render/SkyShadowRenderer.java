/*
 *  Copyright 2019, 2020 grondag
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package grondag.canvas.render;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;

import grondag.canvas.buffer.encoding.CanvasImmediate;
import grondag.canvas.material.property.MaterialTarget;
import grondag.canvas.pipeline.Pipeline;
import grondag.canvas.pipeline.PipelineManager;
import grondag.canvas.varia.MatrixState;

public class SkyShadowRenderer {
	private static boolean active = false;
	private static boolean renderEntityShadows = false;

	private static void begin() {
		assert !active;
		active = true;
		final int size = Pipeline.skyShadowSize;
		PipelineManager.setProjection(size, size);
		RenderSystem.viewport(0, 0, size, size);
	}

	private static void end() {
		assert active;
		active = false;
		PipelineManager.setProjection(PipelineManager.width(), PipelineManager.height());
		RenderSystem.viewport(0, 0, PipelineManager.width(), PipelineManager.height());
	}

	public static boolean isActive() {
		return active;
	}

	public static void render(CanvasWorldRenderer canvasWorldRenderer, double cameraX, double cameraY, double cameraZ, CanvasImmediate immediate) {
		if (Pipeline.skyShadowFbo != null) {
			begin();

			// WIP: will need purpose-specific methods for each frustum/render type
			MatrixState.set(MatrixState.REGION);
			canvasWorldRenderer.renderTerrainLayer(false, cameraX, cameraY, cameraZ);
			MatrixState.set(MatrixState.CAMERA);

			if (Pipeline.config().skyShadow.allowEntities && MinecraftClient.getInstance().options.entityShadows) {
				immediate.drawCollectors(MaterialTarget.MAIN, false);
			}

			end();
		}
	}

	/** Preserves entityShadows option state, overwriting it temporarily if needed to prevent vanilla from rendering shadows. */
	public static void beforeEntityRender(MinecraftClient mc) {
		if (Pipeline.skyShadowFbo != null) {
			renderEntityShadows = mc.options.entityShadows;
			mc.options.entityShadows = false;
		}
	}

	/** Restores entityShadows option state. */
	public static void afterEntityRender(MinecraftClient mc) {
		if (Pipeline.skyShadowFbo != null) {
			mc.options.entityShadows = renderEntityShadows;
		}
	}
}
