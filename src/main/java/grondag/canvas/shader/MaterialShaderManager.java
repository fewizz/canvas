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

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.minecraft.util.Identifier;

import grondag.canvas.CanvasMod;
import grondag.canvas.config.Configurator;
import grondag.fermion.sc.unordered.SimpleUnorderedArrayList;
import grondag.fermion.varia.IndexedInterner;

public enum MaterialShaderManager {
	INSTANCE;

	private final SimpleUnorderedArrayList<MaterialShaderImpl> shaders = new SimpleUnorderedArrayList<>();

	MaterialShaderManager() {
		if (Configurator.enableLifeCycleDebug) {
			CanvasMod.LOG.info("Lifecycle Event: MaterialShaderManager init");
		}
	}

	public synchronized MaterialShaderImpl find(int vertexShaderIndex, int fragmentShaderIndex, ProgramType programType) {
		final long key = key(vertexShaderIndex, fragmentShaderIndex, programType);
		MaterialShaderImpl result = KEYMAP.get(key);

		if (result == null) {
			result = create(vertexShaderIndex, fragmentShaderIndex, programType);
			KEYMAP.put(key, result);
		}

		return result;
	}

	private synchronized MaterialShaderImpl create(int vertexShaderIndex, int fragmentShaderIndex, ProgramType programType) {
		final MaterialShaderImpl result = new MaterialShaderImpl(shaders.size(), vertexShaderIndex, fragmentShaderIndex, programType);
		shaders.add(result);

		boolean isNew;

		if (programType.isDepth) {
			isNew = DEPTH_VERTEX_INDEXES.add(vertexShaderIndex);
			isNew |= DEPTH_FRAGMENT_INDEXES.add(fragmentShaderIndex);
		} else {
			isNew = VERTEX_INDEXES.add(vertexShaderIndex);
			isNew |= FRAGMENT_INDEXES.add(fragmentShaderIndex);
		}

		// ensure shaders are recompiled when new sub-shader source referenced
		if (isNew) {
			GlProgramManager.INSTANCE.reload();
		}

		return result;
	}

	public MaterialShaderImpl get(int index) {
		return shaders.get(index);
	}

	/** Tracks which vertex sub-shaders are in use by materials. */
	private static final IntOpenHashSet VERTEX_INDEXES = new IntOpenHashSet();

	/** Tracks which fragmet sub-shaders are in use by materials. */
	private static final IntOpenHashSet FRAGMENT_INDEXES = new IntOpenHashSet();

	/** Tracks which vertex depth sub-shaders are in use by materials. */
	private static final IntOpenHashSet DEPTH_VERTEX_INDEXES = new IntOpenHashSet();

	/** Tracks which fragmet depth sub-shaders are in use by materials. */
	private static final IntOpenHashSet DEPTH_FRAGMENT_INDEXES = new IntOpenHashSet();

	public static final IndexedInterner<Identifier> VERTEX_INDEXER = new IndexedInterner<>(Identifier.class);
	public static final IndexedInterner<Identifier> FRAGMENT_INDEXER = new IndexedInterner<>(Identifier.class);
	private static final Long2ObjectOpenHashMap<MaterialShaderImpl> KEYMAP = new Long2ObjectOpenHashMap<>();

	private static long key(int vertexShaderIndex, int fragmentShaderIndex, ProgramType programType) {
		// PERF: don't need key space this big
		return programType.ordinal() | ((long) fragmentShaderIndex << 16) | ((long) vertexShaderIndex << 32);
	}

	public static final int DEFAULT_VERTEX_INDEX = VERTEX_INDEXER.toHandle(ShaderData.DEFAULT_VERTEX_SOURCE);
	public static final int DEFAULT_FRAGMENT_INDEX = FRAGMENT_INDEXER.toHandle(ShaderData.DEFAULT_FRAGMENT_SOURCE);

	static int[] vertexIds(ProgramType programType) {
		return programType.isDepth ? DEPTH_VERTEX_INDEXES.toIntArray() : VERTEX_INDEXES.toIntArray();
	}

	static int[] fragmentIds(ProgramType programType) {
		return programType.isDepth ? DEPTH_FRAGMENT_INDEXES.toIntArray() : FRAGMENT_INDEXES.toIntArray();
	}
}
