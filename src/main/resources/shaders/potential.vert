uniform mat4 mvp;

attribute vec3 position;
attribute vec3 center;

varying vec3 cent;
varying vec3 pos;

void main() {
    cent = center;
    pos = position;

    gl_Position = mvp * vec4(position.xyz, 1.0);
}