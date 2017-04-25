uniform sampler2D tex;
varying vec2 t;

const float depthColorShift = 0.8;
const float step = 0.005;

const int samples = 5;

const vec2 lightPosition = normalize(vec2(0.0, 1.0));
const vec4 mixColor = vec4(depthColorShift, depthColorShift, depthColorShift, 1.0);

void main() {
    vec4 c = texture2D(tex, t);

    if(c.b > 0.7) {
        float depth = 0;
        vec4 outColor = vec4(0.01, 0.13, c.b, 0.01);

        for (int i = 0; i < samples; i++) {
            depth += texture2D(tex, t + (step * i * lightPosition)).b / samples;
        }

        outColor = mix(mixColor, outColor, depth);
        gl_FragColor = outColor;
    }
    else discard;
}