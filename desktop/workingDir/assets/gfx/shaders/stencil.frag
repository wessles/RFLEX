#ifdef GL_ES
#define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_maskTexture;

void main() {
    gl_FragColor = texture2D(u_texture, v_texCoords) * v_color;
    gl_FragColor.a = texture2D(u_maskTexture, v_texCoords).a;
}
