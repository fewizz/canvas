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

import static org.lwjgl.system.MemoryStack.stackGet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL21;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.loader.api.FabricLoader;

import grondag.canvas.CanvasMod;
import grondag.canvas.config.Configurator;
import grondag.canvas.pipeline.Pipeline;
import grondag.canvas.texture.MaterialInfoTexture;
import grondag.canvas.varia.CanvasGlHelper;
import grondag.frex.api.config.ShaderConfig;

public class GlShader implements Shader {
	static final Pattern PATTERN = Pattern.compile("^#include\\s+(\\\"*[\\w]+:[\\w/\\.]+)[ \\t]*.*", Pattern.MULTILINE);
	private static final HashSet<String> INCLUDED = new HashSet<>();
	private static boolean isErrorNoticeComplete = false;
	private static boolean needsClearDebugOutputWarning = true;
	private static boolean needsDebugOutputWarning = true;
	private final Identifier shaderSourceId;
	protected final int shaderType;
	protected final ProgramType programType;
	private String source = null;
	private int glId = -1;
	private boolean needsLoad = true;
	private boolean isErrored = false;

	public GlShader(Identifier shaderSource, int shaderType, ProgramType programType) {
		shaderSourceId = shaderSource;
		this.shaderType = shaderType;
		this.programType = programType;
	}

	public static void forceReloadErrors() {
		isErrorNoticeComplete = false;
		clearDebugSource();
	}

	private static Path shaderDebugPath() {
		final File gameDir = FabricLoader.getInstance().getGameDirectory();

		return gameDir.toPath().normalize().resolve("canvas_shader_debug");
	}

	private static void clearDebugSource() {
		final Path path = shaderDebugPath();

		try {
			File shaderDir = path.toFile();

			if (shaderDir.exists()) {
				final File[] files = shaderDir.listFiles();

				for (final File f : files) {
					f.delete();
				}
			}

			shaderDir = path.resolve("failed").toFile();

			if (shaderDir.exists()) {
				final File[] files = shaderDir.listFiles();

				for (final File f : files) {
					f.delete();
				}

				shaderDir.delete();
			}
		} catch (final Exception e) {
			if (needsClearDebugOutputWarning) {
				CanvasMod.LOG.error(I18n.translate("error.canvas.fail_clear_shader_output", path), e);
				needsClearDebugOutputWarning = false;
			}
		}
	}

	private int glId() {
		if (needsLoad) {
			load();
		}

		return isErrored ? -1 : glId;
	}

	private void load() {
		needsLoad = false;
		isErrored = false;
		String source = null;
		String error = null;

		try {
			if (glId <= 0) {
				glId = GL21.glCreateShader(shaderType);

				if (glId == 0) {
					glId = -1;
					isErrored = true;
					return;
				}
			}

			source = getSource();

			safeShaderSource(glId, source);
			GL21.glCompileShader(glId);

			if (GL21.glGetShaderi(glId, GL21.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				isErrored = true;
				error = CanvasGlHelper.getShaderInfoLog(glId);

				if (error.isEmpty()) {
					error = "Unknown OpenGL Error.";
				}
			}
		} catch (final Exception e) {
			isErrored = true;
			error = e.getMessage();
		}

		if (isErrored) {
			if (glId > 0) {
				GL21.glDeleteShader(glId);
				glId = -1;
			}

			if (Configurator.conciseErrors) {
				if (!isErrorNoticeComplete) {
					CanvasMod.LOG.error(I18n.translate("error.canvas.fail_create_any_shader"));
					isErrorNoticeComplete = true;
				}
			} else {
				CanvasMod.LOG.error(I18n.translate("error.canvas.fail_create_shader", shaderSourceId.toString(), programType.name, error));
			}

			outputDebugSource(source, error);
		} else if (Configurator.shaderDebug) {
			outputDebugSource(source, null);
		}
	}

	/**
	 * Identical in function to {@link GL20C#glShaderSource(int, CharSequence)} but
	 * passes a null pointer for string length to force the driver to rely on the null
	 * terminator for string length.  This is a workaround for an apparent flaw with some
	 * AMD drivers that don't receive or interpret the length correctly, resulting in
	 * an access violation when the driver tries to read past the string memory.
	 *
	 * <p>Hat tip to fewizz for the find and the fix.
	 */
	private static void safeShaderSource(@NativeType("GLuint") int glId, @NativeType("GLchar const **") CharSequence source) {
		final MemoryStack stack = stackGet();
		final int stackPointer = stack.getPointer();

		try {
			final ByteBuffer sourceBuffer = MemoryUtil.memUTF8(source, true);
			final PointerBuffer pointers = stack.mallocPointer(1);
			pointers.put(sourceBuffer);

			GL21.nglShaderSource(glId, 1, pointers.address0(), 0);
			org.lwjgl.system.APIUtil.apiArrayFree(pointers.address0(), 1);
		} finally {
			stack.setPointer(stackPointer);
		}
	}

	protected String debugSourceString() {
		return shaderSourceId.getPath().toString().replace("/", "-").replace(":", "-");
	}

	private void outputDebugSource(String source, String error) {
		final String fileName = debugSourceString();
		final Path path = shaderDebugPath();

		File shaderDir = path.toFile();

		if (!shaderDir.exists()) {
			shaderDir.mkdir();
			CanvasMod.LOG.info("Created shader debug output folder" + shaderDir.toString());
		}

		if (error != null) {
			shaderDir = path.resolve("failed").toFile();

			if (!shaderDir.exists()) {
				shaderDir.mkdir();
				CanvasMod.LOG.info("Created shader debug output failure folder" + shaderDir.toString());
			}

			source += "\n\n///////// ERROR ////////\n" + error + "\n////////////////////////\n";
		}

		if (shaderDir.exists()) {
			try (FileWriter writer = new FileWriter(shaderDir.getAbsolutePath() + File.separator + fileName, false)) {
				writer.write(source);
				writer.close();
			} catch (final IOException e) {
				if (needsDebugOutputWarning) {
					CanvasMod.LOG.error(I18n.translate("error.canvas.fail_create_shader_output", path), e);
					needsDebugOutputWarning = false;
				}
			}
		}
	}

	private String getSource() {
		String result = source;

		if (result == null) {
			result = getCombinedShaderSource();

			if (programType == ProgramType.MATERIAL_VERTEX_LOGIC) {
				result = StringUtils.replace(result, "#define PROGRAM_BY_UNIFORM", "//#define PROGRAM_BY_UNIFORM");
			}

			if (shaderType == GL21.GL_FRAGMENT_SHADER) {
				result = StringUtils.replace(result, "#define VERTEX_SHADER", "#define FRAGMENT_SHADER");
			}

			if (!Configurator.wavyGrass) {
				result = StringUtils.replace(result, "#define ANIMATED_FOLIAGE", "//#define ANIMATED_FOLIAGE");
			}

			if (Pipeline.skyShadowFbo == null) {
				result = StringUtils.replace(result, "#define SHADOW_MAP_PRESENT", "//#define SHADOW_MAP_PRESENT");
			}

			result = StringUtils.replace(result, "#define _CV_MATERIAL_INFO_TEXTURE_SIZE 0", "#define _CV_MATERIAL_INFO_TEXTURE_SIZE " + MaterialInfoTexture.INSTANCE.squareSizePixels());
			result = StringUtils.replace(result, "#define _CV_MAX_SHADER_COUNT 0", "#define _CV_MAX_SHADER_COUNT " + MaterialShaderImpl.MAX_SHADERS);

			//if (Configurator.hdLightmaps()) {
			//	result = StringUtils.replace(result, "#define VANILLA_LIGHTING", "//#define VANILLA_LIGHTING");
			//
			//	if (Configurator.lightmapNoise) {
			//		result = StringUtils.replace(result, "//#define ENABLE_LIGHT_NOISE", "#define ENABLE_LIGHT_NOISE");
			//	}
			//}

			if (!MinecraftClient.IS_SYSTEM_MAC) {
				result = StringUtils.replace(result, "#version 120", "#version 130");
				result = StringUtils.replace(result, "#extension GL_EXT_gpu_shader4 : require", "//#extension GL_EXT_gpu_shader4 : require");
			}

			source = result;
		}

		return result;
	}

	private String getCombinedShaderSource() {
		final ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
		INCLUDED.clear();
		String result = loadShaderSource(resourceManager, shaderSourceId);
		result = preprocessSource(resourceManager, result);
		return processSourceIncludes(resourceManager, result);
	}

	protected String preprocessSource(ResourceManager resourceManager, String baseSource) {
		return baseSource;
	}

	protected static String loadShaderSource(ResourceManager resourceManager, Identifier shaderSourceId) {
		try (Resource resource = resourceManager.getResource(shaderSourceId)) {
			try (Reader reader = new InputStreamReader(resource.getInputStream())) {
				return CharStreams.toString(reader);
			}
		} catch (final FileNotFoundException e) {
			final String result = Pipeline.config().configSource(shaderSourceId);
			return result == null ? ShaderConfig.getShaderConfigSupplier(shaderSourceId).get() : result;
		} catch (final IOException e) {
			CanvasMod.LOG.warn("Unable to load shader resource " + shaderSourceId.toString() + " due to exception.", e);
			return "";
		}
	}

	private String processSourceIncludes(ResourceManager resourceManager, String source) {
		final Matcher m = PATTERN.matcher(source);

		while (m.find()) {
			// allow quoted arguments to #include for nicer IDE support
			final String id = StringUtils.replace(m.group(1), "\"", "");

			if (INCLUDED.contains(id)) {
				source = StringUtils.replace(source, m.group(0), "");
			} else {
				INCLUDED.add(id);
				final String src = processSourceIncludes(resourceManager, loadShaderSource(resourceManager, new Identifier(id)));
				source = StringUtils.replace(source, m.group(0), src, 1);
			}
		}

		return source;
	}

	/**
	 * Call after render / resource refresh to force shader reload.
	 */
	@Override
	public final void forceReload() {
		needsLoad = true;
		source = null;
	}

	@Override
	public boolean attach(int program) {
		final int glId = glId();

		if (glId <= 0) {
			return false;
		}

		GL21.glAttachShader(program, glId);
		return true;
	}

	@Override
	public boolean containsUniformSpec(String type, String name) {
		final String regex = "(?m)^\\s*uniform\\s+" + type + "\\s+" + name + "\\s*;";
		final Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(getSource()).find();
	}

	@Override
	public Identifier getShaderSourceId() {
		return shaderSourceId;
	}
}
