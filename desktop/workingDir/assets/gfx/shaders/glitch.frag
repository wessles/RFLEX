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

float round(float var) {
	float _var = mod(var, 1.0);
	if(_var < 0.5)
		return var - _var;
	else
		return var + (1.0 - _var);
}

#ifdef GL_ES
	highp float rand(vec2 co)
	{
	    highp float a = 12.9898;
	    highp float b = 78.233;
	    highp float c = 43758.5453;
	    highp float dt = dot(co.xy ,vec2(a,b));
	    highp float sn = mod(dt,3.14);
	    return fract(sin(sn) * c);
	}
#else
	float rand(vec2 co) {
		return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
	}
#endif

void main() {
	// tex coords
	vec2 tc = vec2(v_texCoords.x, v_texCoords.y);

	vec4 colorOffset = vec4(0.0);

	float accuracy = 100.0 * rand(tc+u_time*100.0);
	float strokeWidth = 0.6;

	float timediv40rand = rand(vec2(u_time/40.0));
	float bigG = abs(sin(timediv40rand)) * accuracy, midG = abs(tan(timediv40rand)) * accuracy, littleG = abs(cos(timediv40rand)) * accuracy;

	float test = round(tc.y * accuracy);

	float randtctimestime = rand(vec2(tc.y) * u_time);

	if(test > littleG * (1.0 - strokeWidth) && test < littleG * (1.0 + strokeWidth)) {
		float stroke = randtctimestime * 0.02 + 0.03;
		stroke -= abs(stroke) / 2.0;
		tc.x += stroke;
		colorOffset.g += randtctimestime*0.1;
	}

	if(test > midG * (1.0 - strokeWidth*1.5) && test < midG * (1.0 + strokeWidth*1.5)) {
		float stroke = randtctimestime * 0.06;
		stroke -= abs(stroke) / 2.0;
		tc.x += stroke;
		tc.y += stroke * 0.01;
		colorOffset.r += randtctimestime*0.2;
    }

	if(test > bigG * (1.0 - strokeWidth) && test < bigG * (1.0 + strokeWidth)) {
		float stroke = rand(vec2(u_time, tc.x+tc.y)) * 0.09 + 0.01;
		stroke -= abs(stroke) / 2.0;
		tc.x += stroke;
    }

	// Get texel
	vec4 cta = texture2D(u_texture, tc) * v_color + colorOffset;

	// Apply
    gl_FragColor = cta*v_color;
}
