attribute vec3 a_position;

uniform mat4 u_projTrans;

varying vec4 coord;

void main(void) {
    gl_Position = u_projTrans * vec4(a_position.xy, 0.0, 1.0);
    coord = gl_Vertex;
}