#include canvas:shaders/pipeline/varying.glsl

/******************************************************
  canvas:shaders/pipeline/dev.vert
******************************************************/

varying vec4 shadowPos;

void frx_writePipelineVertex(in frx_VertexData data) {
	// WIP: remove - various api tests
	//if (data.vertex.z < frx_viewDistance() * -0.25) {
	//	data.color = vec4(0.0, 0.0, 0.0, 1.0);
	//}

	// world pos consistency test
	//vec3 worldPos = data.vertex.xyz + frx_modelOriginWorldPos();
	//float f = fract(worldPos.x / 32.0);
	//data.color = vec4(f, f, f, 1.0);

	// apply transforms
	//data.normal *= gl_NormalMatrix;

	if (frx_modelOriginType() == MODEL_ORIGIN_SCREEN) {
		vec4 viewCoord = gl_ModelViewMatrix * data.vertex;
		gl_ClipVertex = viewCoord;
		gl_FogFragCoord = length(viewCoord.xyz);
		gl_Position = gl_ProjectionMatrix * viewCoord;
	} else {
		data.vertex += frx_modelToCamera();
		vec4 viewCoord = frx_viewMatrix() * data.vertex;
		gl_ClipVertex = viewCoord;
		gl_FogFragCoord = length(viewCoord.xyz);
		gl_Position = frx_projectionMatrix() * viewCoord;
	}

	shadowPos  = frx_shadowViewProjectionMatrix() * data.vertex;

	pv_lightcoord = data.light;
	pv_ao = data.aoShade;
}
