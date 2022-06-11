/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.moulberry.notenoughupdates.cosmetics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class ShaderManager implements IResourceManagerReloadListener {
	private final ResourceLocation shaderLocation = new ResourceLocation("notenoughupdates:shaders");
	private final HashMap<String, Shader> shaderMap = new HashMap<>();

	private static final ShaderManager INSTANCE = new ShaderManager();

	public static ShaderManager getInstance() {
		return INSTANCE;
	}

	@Override
	public void onResourceManagerReload(IResourceManager iResourceManager) {
		shaderMap.values().forEach(it -> {
			GL20.glDeleteProgram(it.program);
		});
		shaderMap.clear();
	}

	public static class Shader {
		public final int program;

		public Shader(int program) {
			this.program = program;
		}
	}

	public int getShader(String name) {
		if (!shaderMap.containsKey(name)) {
			reloadShader(name);
		}
		return shaderMap.get(name).program;
	}

	public int loadShader(String name) {
		if (!shaderMap.containsKey(name)) {
			reloadShader(name);
		}
		GL20.glUseProgram(shaderMap.get(name).program);
		return shaderMap.get(name).program;
	}

	public void loadData(String name, String var, Object value) {
		int location = GL20.glGetUniformLocation(shaderMap.get(name).program, var);

		if (value instanceof Integer) {
			GL20.glUniform1i(location, (Integer) value);
		} else if (value instanceof Float) {
			GL20.glUniform1f(location, (Float) value);
		} else if (value instanceof Vector2f) {
			Vector2f vec = (Vector2f) value;
			GL20.glUniform2f(location, vec.x, vec.y);
		} else if (value instanceof Vector3f) {
			Vector3f vec = (Vector3f) value;
			GL20.glUniform3f(location, vec.x, vec.y, vec.z);
		} else if (value instanceof Vector4f) {
			Vector4f vec = (Vector4f) value;
			GL20.glUniform4f(location, vec.x, vec.y, vec.z, vec.w);
		} else {
			throw new UnsupportedOperationException("Failed to load data into shader: Unsupported data type.");
		}
	}

	private void reloadShader(String name) {
		int vertex = -1;
		String sourceVert = getShaderSource(name, GL20.GL_VERTEX_SHADER);
		if (!sourceVert.isEmpty()) {
			vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
			GL20.glShaderSource(vertex, sourceVert);
			GL20.glCompileShader(vertex);

			if (GL20.glGetShaderi(vertex, 35713) == 0) {
				System.err.println(GL20.glGetShaderInfoLog(vertex, 100));
			}
		}

		int fragment = -1;
		String sourceFrag = getShaderSource(name, GL20.GL_FRAGMENT_SHADER);
		if (!sourceFrag.isEmpty()) {
			fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
			GL20.glShaderSource(fragment, sourceFrag);
			GL20.glCompileShader(fragment);

			if (GL20.glGetShaderi(fragment, 35713) == 0) {
				System.err.println(GL20.glGetShaderInfoLog(fragment, 100));
			}
		}

		int compute = -1;
		String sourceCompute = getShaderSource(name, GL43.GL_COMPUTE_SHADER);
		if (!sourceCompute.isEmpty()) {
			compute = GL20.glCreateShader(GL43.GL_COMPUTE_SHADER);
			GL20.glShaderSource(compute, sourceCompute);
			GL20.glCompileShader(compute);

			if (GL20.glGetShaderi(compute, 35713) == 0) {
				System.err.println(GL20.glGetShaderInfoLog(compute, 100));
			}
		}

		int program = GL20.glCreateProgram();
		if (vertex != -1) GL20.glAttachShader(program, vertex);
		if (fragment != -1) GL20.glAttachShader(program, fragment);
		if (compute != -1) GL20.glAttachShader(program, compute);

		GL20.glLinkProgram(program);

		if (vertex != -1) GL20.glDeleteShader(vertex);
		if (fragment != -1) GL20.glDeleteShader(fragment);
		if (compute != -1) GL20.glDeleteShader(compute);

		if (GL20.glGetProgrami(program, 35714) == 0) {
			System.err.println(GL20.glGetProgramInfoLog(program, 100));
		}
		GL20.glValidateProgram(program);
		if (GL20.glGetProgrami(program, 35715) == 0) {
			System.err.println(GL20.glGetProgramInfoLog(program, 100));
		}

		shaderMap.put(name, new Shader(program));
	}

	public String getShaderSource(String name, int type) {
		String ext = "";
		if (type == GL20.GL_VERTEX_SHADER) {
			ext = ".vert";
		} else if (type == GL20.GL_FRAGMENT_SHADER) {
			ext = ".frag";
		} else if (type == GL43.GL_COMPUTE_SHADER) {
			ext = ".compute";
		} else {
			return "";
		}
		ResourceLocation location = new ResourceLocation(
			shaderLocation.getResourceDomain(),
			shaderLocation.getResourcePath() + "/" + name + ext
		);
		try (InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()) {
			StringBuilder source = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = br.readLine()) != null) {
				source.append(line).append("\n");
			}
			return source.toString();
		} catch (IOException ignored) {
		}
		return "";
	}
}
