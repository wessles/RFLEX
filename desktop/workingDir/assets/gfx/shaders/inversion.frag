#ifdef GL_ES
#define LOWP lowp
    precision lowp float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time;
uniform sampler2D u_texture;

void main() {
	// Get texel
	vec4 cta = texture2D(u_texture, v_texCoords) * v_color;
	cta.r = 1.0 - cta.r;
	cta.g = 1.0 - cta.g;
	cta.b = 1.0 - cta.b;
	cta.rgb *= 0.5;

	// Apply
    gl_FragColor = cta;
}
