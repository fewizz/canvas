/*******************************************************************************
 * Copyright 2019, 2020 grondag
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.canvas.mixinterface;

import java.util.Set;
import java.util.SortedSet;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FpsSmoother;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface WorldRendererExt {

	MinecraftClient canvas_mc();

	int canvas_renderDistance();

	void canvas_reload();

	ClientWorld canvas_world();

	double canvas_lastCameraChunkUpdateX();

	double canvas_lastCameraChunkUpdateY();

	double canvas_lastCameraChunkUpdateZ();

	void canvas_updateLastCameraChunkPositions();

	int canvas_camereChunkX();

	int canvas_camereChunkY();

	int canvas_camereChunkZ();

	/** Updates stored values
	 * @param yaw
	 * @param pitch
	 * @param cameraPos */
	boolean canvas_checkNeedsTerrainUpdate(Vec3d cameraPos, float pitch, float yaw);

	void canvas_setNeedsTerrainUpdate(boolean needsUpdate);

	TextureManager canvas_textureManager();

	EntityRenderDispatcher canvas_entityRenderDispatcher();

	BufferBuilderStorage canvas_bufferBuilders();

	Frustum canvas_getCapturedFrustum ();

	void canvas_setCapturedFrustum (Frustum frustum);

	void canvas_setCapturedFrustumPosition(Frustum frustum);

	void canvas_captureFrustumIfNeeded(Matrix4f matrix4f2, Matrix4f matrix4f, Vec3d cameraPos, boolean hasCapturedFrustum, Frustum frustum2);

	int canvas_getAndIncrementFrameIndex();

	FpsSmoother canvas_chunkUpdateSmoother();

	boolean canvas_canDrawEntityOutlines();

	Framebuffer canvas_entityOutlinesFramebuffer();

	ShaderEffect canvas_entityOutlineShader();

	Set<BlockEntity> canvas_noCullingBlockEntities();

	void canvas_drawBlockOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState);

	void canvas_renderWorldBorder(Camera camera);

	Long2ObjectMap<SortedSet<BlockBreakingInfo>> canvas_blockBreakingProgressions();

	void canvas_renderEntity(Entity entity, double d, double e, double f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider);

	void canvas_renderWeather(LightmapTextureManager lightmapTextureManager, float f, double d, double e, double g);

	void canvas_setEntityCount(int count);

	boolean canvas_shouldSortTranslucent(double x, double y, double z);

	VertexFormat canvas_vertexFormat();
}
