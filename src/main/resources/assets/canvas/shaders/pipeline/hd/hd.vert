#include canvas:shaders/internal/header.glsl
#include canvas:shaders/internal/varying.glsl
#include canvas:shaders/internal/vertex.glsl
#include canvas:shaders/lib/bitwise.glsl
#include canvas:shaders/internal/diffuse.glsl
#include canvas:shaders/internal/hd/hd.glsl

/******************************************************
  canvas:shaders/internal/hd/hd.vert
******************************************************/

attribute vec4 in_color;
attribute vec2 in_uv;
attribute vec4 in_normal_ao;
attribute vec2 in_hd_lightmap;

void setupVertex() {
	gl_Position = ftransform();

	vec4 viewCoord = gl_ModelViewMatrix * gl_Vertex;
	gl_ClipVertex = viewCoord;
	gl_FogFragCoord = length(viewCoord.xyz);
	v_texcoord = textureCoord(in_uv, 0);

	#if CONTEXT_IS_BLOCK
	v_hd_lightmap = in_hd_lightmap / 32768.0;
	#endif

	#if DIFFUSE_SHADING_MODE != DIFFUSE_MODE_NONE
	v_diffuse = diffuse(diffuseNormal(viewCoord, in_normal_ao.xyz));
	#endif

	// Fixes Acuity #5
	// Adding +0.5 prevents striping or other strangeness in flag-dependent rendering
	// due to FP error on some cards/drivers.  Also made varying attribute invariant (rolls eyes at OpenGL)
	v_flags =  in_lightmap.ba + 0.5;

	#if !CONTEXT_IS_GUI
	// the lightmap texture matrix is scaled to 1/256 and then offset + 8
	// it is also clamped to repeat and has linear min/mag
	v_lightcoord = in_lightmap.rg * 0.00390625 + 0.03125;
	#endif

	v_color = in_color;
}
