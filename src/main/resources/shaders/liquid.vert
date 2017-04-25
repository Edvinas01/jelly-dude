uniform vec2 viewport;
uniform mat4 mvp;

attribute vec3 position;
attribute vec4 color;

varying vec4 col;
varying vec2 t;

void main() {
    vec4 clipPosition = mvp * vec4(position.xyz, 1.0);
    col = color;

    // Map the vertex to the buffer texture that fills the screen.
    // No need to divide by w, we are using orthogonal projection so its always 1.
    t = (clipPosition.xy + vec2(1.0, 1.0)) / 2.0;

    gl_Position = clipPosition;
}