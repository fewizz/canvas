#include canvas:shaders/pipeline/fog.glsl
#include canvas:shaders/pipeline/varying.glsl
#include frex:shaders/lib/math.glsl
#include frex:shaders/lib/color.glsl
#include frex:shaders/api/world.glsl
#include frex:shaders/api/player.glsl
#include frex:shaders/api/material.glsl
#include canvas:basic_light_config
#include canvas:handheld_light_config

/******************************************************
  canvas:shaders/pipeline/dev.frag
******************************************************/

#define TARGET_BASECOLOR 0
#define TARGET_EMISSIVE  1

varying vec4 shadowPos;

// for testing - not a good way to do it
float p_diffuseSky(vec3 normal) {
	float f = dot(frx_skyLightVector(), normal);
	f = f > 0.0 ? 0.4 * f : 0.2 * f;
	return 0.6 + frx_skyLightStrength() * f;
}

/**
 * Offers results similar to vanilla in GUI, assumes a fixed transform.
 * Vanilla GUI light setup looks like this:
 *
 * light(GL_LIGHT0, GL_POSITION, -0.96104145, -0.078606814, -0.2593495, 0
 * light(GL_LIGHT0, GL_DIFFUSE, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
 * light(GL_LIGHT0, GL_AMBIENT, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
 * light(GL_LIGHT0, GL_SPECULAR, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
 *
 * light(GL_LIGHT1, GL_POSITION, -0.26765957, -0.95667744, 0.100838766, 0
 * light(GL_LIGHT1, GL_DIFFUSE, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
 * light(GL_LIGHT1, GL_AMBIENT, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
 * light(GL_LIGHT1, GL_SPECULAR, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
 *
 * glShadeModel(GL_FLAT);
 * glLightModel(GL_LIGHT_MODEL_AMBIENT, 0.4F, 0.4F, 0.4F, 1.0F);
 */
float p_diffuseGui(vec3 normal) {
	normal = normalize(gl_NormalMatrix * normal);
	float light = 0.4
	+ 0.6 * clamp(dot(normal.xyz, vec3(-0.96104145, -0.078606814, -0.2593495)), 0.0, 1.0)
	+ 0.6 * clamp(dot(normal.xyz, vec3(-0.26765957, -0.95667744, 0.100838766)), 0.0, 1.0);
	return min(light, 1.0);
}

float p_diffuse (vec3 normal) {
	return frx_isGui() ? p_diffuseGui(normal) : p_diffuseSky(normal);
}

vec4 aoFactor(vec2 lightCoord, float ao) {
	// accelerate the transition from 0.4 (should be the minimum) to 1.0
	float bao = (ao - 0.4) / 0.6;
	bao = clamp(bao, 0.0, 1.0);
	bao = 1.0 - bao;
	bao = bao * bao * (1.0 - lightCoord.x * 0.6);
	bao = 0.4 + (1.0 - bao) * 0.6;

	vec4 sky = texture2D(frxs_lightmap, vec2(0.03125, lightCoord.y));
	ao = mix(bao, ao, frx_luminance(sky.rgb));
	return vec4(ao, ao, ao, 1.0);
}

vec4 ambientLight(frx_FragmentData fragData, float exposure) {
	vec4 result;
	vec4 block = texture2D(frxs_lightmap, vec2(fragData.light.x, 0.03125));
	vec4 sky = texture2D(frxs_lightmap, vec2(0.03125, fragData.light.y));
	float skyFactor = fragData.diffuse ? 0.5 + exposure * 0.2 : 0.7;
	result = max(block, sky * skyFactor);

#if HANDHELD_LIGHT_RADIUS != 0
	vec4 held = frx_heldLight();

	if (held.w > 0.0 && !frx_isGui()) {
		float d = clamp(gl_FogFragCoord / (held.w * HANDHELD_LIGHT_RADIUS), 0.0, 1.0);
		d = 1.0 - d * d;

		vec4 maxBlock = texture2D(frxs_lightmap, vec2(0.96875, 0.03125));

		held = vec4(held.xyz, 1.0) * maxBlock * d;

		result = min(result + held, 1.0);
	}
#endif

	return result;
}

frx_FragmentData frx_createPipelineFragment() {
	return frx_FragmentData (
		texture2D(frxs_spriteAltas, frx_texcoord, frx_matUnmippedFactor() * -4.0),
		frx_color,
		frx_matEmissive() ? 1.0 : 0.0,
		!frx_matDisableDiffuse(),
		!frx_matDisableAo(),
		frx_normal,
		pv_lightcoord,
		pv_ao
	);
}

void frx_writePipelineFragment(in frx_FragmentData fragData) {
	vec4 a = fragData.spriteColor * fragData.vertexColor;

	if (frx_isGui()) {
		if (fragData.diffuse) {
			float df = p_diffuseGui(frx_normal);
			df = df + (1.0 - df) * fragData.emissivity;
			a *= vec4(df, df, df, 1.0);
		}
	} else {

		// NB: this "lighting model" is a made-up garbage
		// temporary hack to see / test the shadow map quality

		float exposure = dot(frx_skyLightVector(), frx_normal);

		vec3 shadowCoords = shadowPos.xyz / shadowPos.w;

		// Transform from screen coordinates to texture coordinates
		shadowCoords = shadowCoords * 0.5 + 0.5;
		float shadowDepth = texture2D(frxs_shadowMap, shadowCoords.xy).x;
		float directSkyLight = shadowDepth < shadowCoords.z ? 0.0 : max(0.0, exposure * 0.3);

		a *= mix(ambientLight(fragData, exposure), frx_emissiveColor(), fragData.emissivity);
		a += vec4(frx_emissiveColor().xyz * directSkyLight, 0.0);

		if (fragData.ao) {
			a *= aoFactor(fragData.light, fragData.aoShade);
		}
	}

	if (frx_matFlash()) {
		a = a * 0.25 + 0.75;
	} else if (frx_matHurt()) {
		a = vec4(0.25 + a.r * 0.75, a.g * 0.75, a.b * 0.75, a.a);
	}

	// WIP: remove - various api tests
	//a = min(vec4(1.0, 1.0, 1.0, 1.0), a + vec4(frx_smoothedEyeBrightness().y));
	//a = min(vec4(1.0, 1.0, 1.0, 1.0), a + vec4(0.0, 0.0, frx_eyePos().y / 255.0, 0.0));
	//if (frx_playerFlag(FRX_PLAYER_EYE_IN_LAVA)) {
	//	a = min(vec4(1.0, 1.0, 1.0, 1.0), a + vec4(0.0, 0.0, 1.0, 0.0));
	//}
	//a = min(vec4(1.0, 1.0, 1.0, 1.0), a + vec4(frx_rainGradient()));
	//a = min(vec4(1.0, 1.0, 1.0, 1.0), a + vec4(frx_smoothedRainGradient()));
	//if (frx_renderTarget() == TARGET_ENTITY) {
	//	a = vec4(0.0, 1.0, 0.0, a.a);
	//}
	//a = vec4(frx_vanillaClearColor(), a.a);

	gl_FragData[TARGET_BASECOLOR] = p_fog(a);
	gl_FragDepth = gl_FragCoord.z;
	gl_FragData[TARGET_EMISSIVE] = vec4(fragData.emissivity * a.a, 0.0, 0.0, 1.0);
}
