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

import blue.endless.jankson.JsonObject;

import grondag.canvas.CanvasMod;
import grondag.canvas.pipeline.config.util.ConfigContext;
import grondag.canvas.pipeline.config.util.NamedConfig;
import grondag.canvas.pipeline.config.util.NamedDependencyMap;

public class FramebufferConfig extends NamedConfig<FramebufferConfig> {
	public final AttachmentConfig[] colorAttachments;
	public final AttachmentConfig depthAttachment;

	FramebufferConfig(ConfigContext ctx, JsonObject config) {
		super(ctx, config.get(String.class, "name"));
		colorAttachments = AttachmentConfig.deserialize(ctx, config);

		if (config.containsKey("depthAttachment")) {
			depthAttachment = new AttachmentConfig(ctx, config.getObject("depthAttachment"), true);
		} else {
			depthAttachment = null;
		}
	}

	private FramebufferConfig(ConfigContext ctx) {
		super(ctx, "default");
		colorAttachments = new AttachmentConfig[] {AttachmentConfig.defaultMain(ctx)};
		depthAttachment = AttachmentConfig.defaultDepth(ctx);
	}

	public static FramebufferConfig makeDefault(ConfigContext context) {
		return new FramebufferConfig(context);
	}

	@Override
	public boolean validate() {
		boolean valid = super.validate();

		for (final AttachmentConfig c : colorAttachments) {
			valid &= c.validate();

			if (c.isDepth) {
				CanvasMod.LOG.warn(String.format("Invalid pipeline config - depth attachment %s used as color attachment on framebuffer %s.",
						c.image.name, name));

				valid = false;
			}
		}

		if (depthAttachment != null) {
			valid &= depthAttachment.validate();

			if (!depthAttachment.isDepth) {
				CanvasMod.LOG.warn(String.format("Invalid pipeline config - color attachment %s used as depth attachment on framebuffer %s.",
						depthAttachment.image.name, name));

				valid = false;
			}
		}

		return valid;
	}

	@Override
	public NamedDependencyMap<FramebufferConfig> nameMap() {
		return context.frameBuffers;
	}
}
