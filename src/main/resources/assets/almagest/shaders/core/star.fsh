#version 150
#moj_import <fog.glsl>

uniform sampler2D uTexture;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 vUv;
in vec4 vColor;

out vec4 fragColor;

void main() {
    vec4 starTex = texture(uTexture, vUv);
    vec4 baseColor = starTex * vColor;
    baseColor *= ColorModulator;
    fragColor = linear_fog(baseColor, vertexDistance, FogStart, FogEnd, FogColor);
}
