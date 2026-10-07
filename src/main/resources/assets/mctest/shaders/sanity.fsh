#version 120

// Angepasst für Vanilla Forge (Keine OptiFine gcolor/depthtex0 Abhängigkeit)

#define CA_STRENGTH   0.004   // Etwas stärker, da wir keine Depth-Skalierung haben
#define CA_RADIAL     1.0
#define CA_SMEAR
#define SHARPEN       1.5
#define CONTRAST      1.15
#define SATURATION    0.3
#define EXPOSURE      1.1

// Forge stellt uns die Bildschirm-Textur als 'screenTexture' zur Verfügung
uniform sampler2D screenTexture;
uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;

varying vec2 texcoord;

vec3 sampleCA(vec2 uv, float s) {
    vec2 dir = uv - 0.5;
    vec2 off = vec2(s, 0.0) + dir * CA_RADIAL * s * 2.0;

    #ifdef CA_SMEAR
        float r = (texture2D(screenTexture, uv + off).r + texture2D(screenTexture, uv + off * 2.0).r) * 0.5;
        float b = (texture2D(screenTexture, uv - off).b + texture2D(screenTexture, uv - off * 2.0).b) * 0.5;
    #else
        float r = texture2D(screenTexture, uv + off).r;
        float b = texture2D(screenTexture, uv - off).b;
    #endif
    float g = texture2D(screenTexture, uv).g;
    return vec3(r, g, b);
}

void main() {
    vec2 px = vec2(1.0 / viewWidth, 1.0 / viewHeight);
    vec2 uv = texcoord;
    float s = CA_STRENGTH;

    // Chromatic Aberration
    vec3 center = sampleCA(uv, s);

    // Sharpen
    vec3 blur = sampleCA(uv + vec2(px.x, 0.0), s)
              + sampleCA(uv + vec2(-px.x, 0.0), s)
              + sampleCA(uv + vec2(0.0, px.y), s)
              + sampleCA(uv + vec2(0.0, -px.y), s);
    blur *= 0.25;

    float lumC = dot(center, vec3(0.299, 0.587, 0.114));
    float lumB = dot(blur,   vec3(0.299, 0.587, 0.114));
    vec3 col = center + vec3((lumC - lumB) * SHARPEN);

    // Exposure / Contrast / Saturation
    col *= EXPOSURE;
    col = (col - 0.5) * CONTRAST + 0.5;
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(lum), col, SATURATION);

    gl_FragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}