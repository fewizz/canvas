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

package grondag.canvas.pipeline.config;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonObject;
import org.lwjgl.opengl.GL21;

import grondag.canvas.pipeline.GlSymbolLookup;
import grondag.canvas.pipeline.config.util.ConfigContext;
import grondag.canvas.pipeline.config.util.NamedConfig;
import grondag.canvas.pipeline.config.util.NamedDependencyMap;

public class ImageConfig extends NamedConfig<ImageConfig> {
	public final int internalFormat;
	public final int pixelFormat;
	public final int pixelDataType;
	public final int lod;
	/** 0 if tied to framebuffer size. */
	public final int width;
	/** 0 if tied to framebuffer size. */
	public final int height;
	public final int[] texParamPairs;

	private ImageConfig(ConfigContext ctx, String name, int internalFormat, int lod, int pixelFormat, int pixelDataType, boolean depth) {
		super(ctx, name);
		this.internalFormat = internalFormat;
		this.lod = lod;
		this.pixelDataType = pixelDataType;
		this.pixelFormat = pixelFormat;
		width = 0;
		height = 0;

		if (depth) {
			texParamPairs = new int[10];
			texParamPairs[1] = GL21.GL_NEAREST;
			texParamPairs[3] = GL21.GL_NEAREST;
			texParamPairs[8] = GL21.GL_TEXTURE_COMPARE_MODE;
			texParamPairs[9] = GL21.GL_NONE;
		} else {
			texParamPairs = new int[8];
			texParamPairs[1] = GL21.GL_LINEAR;
			texParamPairs[3] = GL21.GL_LINEAR;
		}

		texParamPairs[0] = GL21.GL_TEXTURE_MIN_FILTER;
		texParamPairs[2] = GL21.GL_TEXTURE_MAG_FILTER;
		texParamPairs[4] = GL21.GL_TEXTURE_WRAP_S;
		texParamPairs[5] = GL21.GL_CLAMP;
		texParamPairs[6] = GL21.GL_TEXTURE_WRAP_T;
		texParamPairs[7] = GL21.GL_CLAMP;
	}

	ImageConfig (ConfigContext ctx, JsonObject config) {
		super(ctx, config.get(String.class, "name"));
		internalFormat = GlSymbolLookup.lookup(config, "internalFormat", "RGBA8");
		lod = config.getInt("lod", 0);
		pixelFormat = GlSymbolLookup.lookup(config, "pixelFormat", "RGBA");
		pixelDataType = GlSymbolLookup.lookup(config, "pixelDataType", "UNSIGNED_BYTE");
		width = config.getInt("size", 0);
		height = width;

		if (!config.containsKey("texParams")) {
			texParamPairs = new int[0];
		} else {
			final JsonArray params = config.get(JsonArray.class, "texParams");
			final int limit = params.size();
			texParamPairs = new int[limit * 2];

			int j = 0;

			for (int i = 0; i < limit; ++i) {
				final JsonObject p = (JsonObject) params.get(i);
				texParamPairs[j++] = GlSymbolLookup.lookup(p, "name", "NONE");
				texParamPairs[j++] = GlSymbolLookup.lookup(p, "val", "NONE");
			}
		}
	}

	public static ImageConfig defaultMain(ConfigContext ctx) {
		return new ImageConfig(ctx, "default_main", GL21.GL_RGBA8, 0, GL21.GL_RGBA, GL21.GL_UNSIGNED_BYTE, false);
	}

	public static ImageConfig defaultDepth(ConfigContext ctx) {
		return new ImageConfig(ctx, "default_depth", GL21.GL_DEPTH_COMPONENT, 0, GL21.GL_DEPTH_COMPONENT, GL21.GL_FLOAT, true);
	}

	@Override
	public NamedDependencyMap<ImageConfig> nameMap() {
		return context.images;
	}
}
