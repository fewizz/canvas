#include frex:shaders/api/header.glsl
#include canvas:shaders/pipeline/pipeline.glsl
#include frex:shaders/lib/sample.glsl
#include canvas:shaders/pipeline/post/bloom_options.glsl

/******************************************************
  canvas:shaders/pipeline/post/downsample.frag
******************************************************/
uniform sampler2D _cvu_input;

varying vec2 _cvv_texcoord;

void main() {
	gl_FragData[0] = frx_sample13(_cvu_input, _cvv_texcoord, BLOOM_DOWNSAMPLE_DIST_VEC / frxu_size, max(0, frxu_lod - 1));
}
