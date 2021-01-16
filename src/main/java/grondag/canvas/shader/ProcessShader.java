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

package grondag.canvas.shader;

import net.minecraft.util.Identifier;

import grondag.canvas.buffer.format.CanvasVertexFormats;
import grondag.canvas.shader.GlProgram.Uniform1iImpl;
import grondag.canvas.shader.GlProgram.Uniform2iImpl;
import grondag.frex.api.material.UniformRefreshFrequency;

public class ProcessShader {
	private final Identifier fragmentId;
	private final Identifier vertexId;
	private final String[] samplers;
	private GlProgram program;
	private Uniform2iImpl size;
	private Uniform1iImpl lod;

	public ProcessShader(Identifier vertexId, Identifier fragmentId, String... samplers) {
		this.fragmentId = fragmentId;
		this.vertexId = vertexId;
		this.samplers = samplers;
	}

	public int samplerCount() {
		return samplers.length;
	}

	public void unload() {
		if (program != null) {
			program.unload();
			program = null;
		}
	}

	public ProcessShader activate() {
		if (program == null) {
			final Shader vs = GlShaderManager.INSTANCE.getOrCreateVertexShader(vertexId, ProgramType.PROCESS);
			final Shader fs = GlShaderManager.INSTANCE.getOrCreateFragmentShader(fragmentId, ProgramType.PROCESS);
			program = new GlProgram(vs, fs, CanvasVertexFormats.PROCESS_VERTEX_UV, ProgramType.PROCESS);
			size = (Uniform2iImpl) program.uniform2i("frxu_size", UniformRefreshFrequency.ON_LOAD, u -> u.set(1, 1));
			lod = (Uniform1iImpl) program.uniform1i("frxu_lod", UniformRefreshFrequency.ON_LOAD, u -> u.set(0));

			int tex = 0;

			for (final String samplerName : samplers) {
				final int n = tex++;
				program.uniformSampler2d(samplerName, UniformRefreshFrequency.ON_LOAD, u -> u.set(n));
			}

			program.load();
		}

		program.activate();

		return this;
	}

	public ProcessShader size(int w, int h) {
		if (program != null && GlProgram.activeProgram() == program) {
			size.set(w, h);
			size.upload();
		}

		return this;
	}

	public ProcessShader lod(int lod) {
		if (program != null && GlProgram.activeProgram() == program) {
			this.lod.set(lod);
			this.lod.upload();
		}

		return this;
	}
}
