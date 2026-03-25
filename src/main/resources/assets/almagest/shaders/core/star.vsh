#version 150
#moj_import <fog.glsl>

in vec3 Position;   // basePos from CPU
in vec2 UV0;
in vec4 Color;
in ivec2 UV2;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

uniform float uStarDistanceMult;
uniform float uStarDistanceAdd;
uniform float uUniverseScale;

uniform float uRotY;   // d + lo
uniform float uRotX;   // i + o + s + la

uniform float uScale;
uniform float uGamma;
uniform float uSlope;
uniform float uExposure;

uniform float uTime;
uniform float uAtmosphereFactor;

out float vertexDistance;
out vec2 vUv;
out vec4 vColor;

vec3 rotX(float r, vec3 v) {
    if (r == 0.0) return v;
    float c = cos(r);
    float s = sin(r);
    float y = v.y * c + v.z * s;
    float z = v.z * c - v.y * s;
    return vec3(v.x, y, z);
}

vec3 rotY(float r, vec3 v) {
    if (r == 0.0) return v;
    float c = cos(r);
    float s = sin(r);
    float x = v.x * c + v.z * s;
    float z = v.z * c - v.x * s;
    return vec3(x, v.y, z);
}

vec2 getCorner(int vid) {
    int c = vid % 4;
    if (c == 0) return vec2(-1,-1);
    if (c == 1) return vec2( 1,-1);
    if (c == 2) return vec2( 1, 1);
    return               vec2(-1, 1);
}

void main() {
    float absMag = UV0.x;
    float phaseOffset = UV0.y;

    // === Brightness & size ===
    float distPc = length(Position);
    float mag = absMag + 5.0 * (log(distPc) / log(10.0)) - 5.0;

    float brightness = pow(max(0.0, 1.0 - mag * uSlope), uGamma);
    brightness *= uExposure;
    float size = brightness * uScale;

    float twinkleSpeed = mix(0.0001, 0.0006, fract(sin(dot(Position, vec3(12.9898, 78.233, 37.719))) * 43758.5453));
    float twinklePhase = uTime * twinkleSpeed;
    float twinkle = sin(twinklePhase * 6.2831853 + phaseOffset) * 0.2;
    size *= (1.0 + twinkle * uAtmosphereFactor);

    // === CPU getAdjustedPos() port ===
    vec3 posNorm = normalize(Position) * uStarDistanceAdd;
    vec3 pos = Position * (uStarDistanceMult * uUniverseScale) + posNorm;

    pos = rotY(uRotY, pos);
    pos = rotX(uRotX, pos);

    // === Billboard UVs ===
    vec2 uvCorner = getCorner(gl_VertexID);
    vUv = uvCorner * 0.5 + 0.5;

    // === Billboard size ===
    vec2 corner = uvCorner * size;

    // === Extract camera axes from ModelViewMat ===
    mat3 invMV = mat3(inverse(ModelViewMat));
    vec3 rightWS = normalize(invMV[0]);
    vec3 upWS    = normalize(invMV[1]);

    // === Final world position ===
    vec3 worldPos = pos + rightWS * corner.x + upWS * corner.y;

    gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);

    vertexDistance = fog_distance(worldPos, FogShape);
    vColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
}
