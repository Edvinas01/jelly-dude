uniform sampler2D tex;

varying vec4 col;
varying vec2 t;

float depthColorShift = 0.8;
float step = 0.005;

int samples = 5;

vec2 lightPosition = normalize(vec2(0.0, 1.0));
vec4 mixColor = vec4(depthColorShift, depthColorShift, depthColorShift, 1.0);

void main() {
    vec4 c = texture2D(tex, t);

    if (c.b > 0.7) {
        float depth = 0.0;

        // vec4 outColor = vec4(0.1, 0.13, c.b, 0.01);
        vec4 outColor = vec4(col.r, col.g, col.b, col.a);

        for (int i = 0; i < samples; i++) {
            depth += texture2D(tex, t + (step * float(i) * lightPosition)).b / float(samples);
        }

        outColor = mix(mixColor, outColor, depth);
        gl_FragColor = outColor;
    }
    else discard;
}