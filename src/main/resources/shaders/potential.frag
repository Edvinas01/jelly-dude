// Don't forget to specify the radius!
uniform float radius;

varying vec3 cent;
varying vec3 pos;

void main() {
    float dx = cent.x - pos.x;
    float dy = cent.y - pos.y;

    // Distance squared. Also potential is: CONSTANT / (d * d), but we can skip that.
    float d = (dx * dx + dy * dy) / radius;

    gl_FragColor = vec4(0.0, 0.0, 1.0, max(1.0 - d, 0.0));
}