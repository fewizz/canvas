#include frex:shaders/api/context.glsl
#include canvas:shaders/internal/varying.glsl

/******************************************************
  frex:shaders/api/sampler.glsl
******************************************************/

uniform sampler2D frxs_spriteAltas;

/**
 * When a texture atlas is in use, the renderer will automatically
 * map from normalized coordinates to texture coordinates before the
 * fragment shader runs. But this doesn't help if you want to
 * re-sample during fragment shading using normalized coordinates.
 *
 * This function will remap normalized coordinates to atlas coordinates.
 * It has no effect when the bound texture is not an atlas texture.
 */
vec2 frx_mapNormalizedUV(vec2 coord) {
	return _cvv_spriteBounds.xy + coord * _cvv_spriteBounds.zw;
}

/**
 * Takes texture atlas coordinates and remaps them to normalized.
 * Has no effect when the bound texture is not an atlas texture.
 */
vec2 frx_normalizeMappedUV(vec2 coord) {
	return _cvv_spriteBounds.z == 0.0 ? coord : (coord - _cvv_spriteBounds.xy) / _cvv_spriteBounds.zw;
}

#ifdef VANILLA_LIGHTING
uniform sampler2D frxs_lightmap;
#endif


#ifdef SHADOW_MAP_PRESENT
#ifdef FRAGMENT_SHADER
uniform sampler2D frxs_shadowMap;
#endif
#endif
