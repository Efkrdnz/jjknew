#version 150

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec3 vertexPos;

out vec4 fragColor;

void main() {
    // calculate which face of the cubemap based on vertex position
    vec3 pos = normalize(vertexPos - vec3(0.5, 0.5, 0.5));
    
    vec2 uv;
    int face = 0;
    
    // determine dominant axis
    vec3 absPos = abs(pos);
    if (absPos.x >= absPos.y && absPos.x >= absPos.z) {
        // X dominant - left or right
        if (pos.x > 0.0) {
            // right face (2, 1) in 2x3 grid
            face = 5;
            uv = vec2(-pos.z / absPos.x, -pos.y / absPos.x);
        } else {
            // left face (0, 1)
            face = 3;
            uv = vec2(pos.z / absPos.x, -pos.y / absPos.x);
        }
    } else if (absPos.y >= absPos.x && absPos.y >= absPos.z) {
        // Y dominant - top or bottom
        if (pos.y > 0.0) {
            // top face (1, 0)
            face = 1;
            uv = vec2(pos.x / absPos.y, pos.z / absPos.y);
        } else {
            // bottom face (0, 0)
            face = 0;
            uv = vec2(pos.x / absPos.y, -pos.z / absPos.y);
        }
    } else {
        // Z dominant - front or back
        if (pos.z > 0.0) {
            // front face (1, 1)
            face = 4;
            uv = vec2(pos.x / absPos.z, -pos.y / absPos.z);
        } else {
            // back face (2, 0)
            face = 2;
            uv = vec2(-pos.x / absPos.z, -pos.y / absPos.z);
        }
    }
    
    // convert from [-1, 1] to [0, 1]
    uv = (uv + 1.0) * 0.5;
    
    // map to 2x3 cubemap layout
    // [BOTTOM][TOP][BACK]
    // [LEFT][FRONT][RIGHT]
    vec2 finalUV;
    if (face == 0) {
        // bottom (0, 0)
        finalUV = vec2(uv.x / 3.0, uv.y / 2.0);
    } else if (face == 1) {
        // top (1, 0)
        finalUV = vec2((1.0 + uv.x) / 3.0, uv.y / 2.0);
    } else if (face == 2) {
        // back (2, 0)
        finalUV = vec2((2.0 + uv.x) / 3.0, uv.y / 2.0);
    } else if (face == 3) {
        // left (0, 1)
        finalUV = vec2(uv.x / 3.0, (1.0 + uv.y) / 2.0);
    } else if (face == 4) {
        // front (1, 1)
        finalUV = vec2((1.0 + uv.x) / 3.0, (1.0 + uv.y) / 2.0);
    } else {
        // right (2, 1)
        finalUV = vec2((2.0 + uv.x) / 3.0, (1.0 + uv.y) / 2.0);
    }
    
    // sample the texture
    vec4 color = texture(Sampler0, finalUV);
    
    fragColor = vec4(color.rgb, 1.0);
}