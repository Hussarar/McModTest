#version 120

// Vanilla Forge 1.12.2 version (no depth texture, no OptiFine uniforms)
// Red and blue copies oscillate; strength is driven by sanityIntensity (0..1)

#define CA_STRENGTH   0.004   // max RGB split at intensity 1.0
#define CA_RADIAL     1.0     // extra split toward screen edges (0 = uniform)
#define CA_SMEAR              // comment out for a single clean split (faster)

// Red and blue slide together toward the middle and back out (sine wave)
#define CA_OSC_SPEED  2.0     // radians per second
#define CA_OSC_AMOUNT 0.9    // at full insanity: 1 = split shrinks to 0 and back, 0.5 = halfway
//#define CA_OSC_CROSS        // remove // so red/blue pass through the middle and swap sides

#define SHARPEN       1.5
#define CONTRAST      1.15
#define SATURATION    0.3     // 1.0 = normal, <1 = desaturated
#define EXPOSURE      1.1

uniform sampler2D screenTexture;
uniform float viewWidth;
uniform float viewHeight;
uniform float frameTimeCounter;
uniform float sanityIntensity;   // 0 = sane, 1 = fully insane

varying vec2 texcoord;

// Splits R and B horizontally (plus a little radially) around G
vec3 sampleCA(vec2 uv, float s, vec2 osc) {
    vec2 dir = uv - 0.5;
    vec2 off = vec2(s, 0.0) + dir * CA_RADIAL * s * 2.0;
    vec2 offR = off * osc.x;   // red stretch
    vec2 offB = off * osc.y;   // blue stretch

    #ifdef CA_SMEAR
        float r = (texture2D(screenTexture, uv + offR).r + texture2D(screenTexture, uv + offR * 2.0).r) * 0.5;
        float b = (texture2D(screenTexture, uv - offB).b + texture2D(screenTexture, uv - offB * 2.0).b) * 0.5;
    #else
        float r = texture2D(screenTexture, uv + offR).r;
        float b = texture2D(screenTexture, uv - offB).b;
    #endif
    float g = texture2D(screenTexture, uv).g;
    return vec3(r, g, b);
}

void main() {
    vec2 px = vec2(1.0 / viewWidth, 1.0 / viewHeight);
    vec2 uv = texcoord;

    // --- sine oscillation, strength driven by sanity ---
    float t = frameTimeCounter * CA_OSC_SPEED;
    float w = sin(t) * 0.5 + 0.5;            // 0..1
    #ifdef CA_OSC_CROSS
        w = sin(t);                           // -1..1, passes through the middle
    #endif
    float amount = CA_OSC_AMOUNT * sanityIntensity;
    float f = 1.0 - amount * (1.0 - w);
    vec2 osc = vec2(f);                       // same factor for red AND blue

    // split distance also grows with insanity
    float s = CA_STRENGTH * sanityIntensity;

    // --- chromatic aberration ---
    vec3 center = sampleCA(uv, s, osc);

    // --- sharpen (unsharp mask on the aberrated image) ---
    vec3 blur = sampleCA(uv + vec2( px.x, 0.0), s, osc)
              + sampleCA(uv + vec2(-px.x, 0.0), s, osc)
              + sampleCA(uv + vec2(0.0,  px.y), s, osc)
              + sampleCA(uv + vec2(0.0, -px.y), s, osc);
    blur *= 0.25;

    float lumC = dot(center, vec3(0.299, 0.587, 0.114));
    float lumB = dot(blur,   vec3(0.299, 0.587, 0.114));
    vec3 col = center + vec3((lumC - lumB) * SHARPEN);

    // --- exposure / contrast / saturation ---
    col *= EXPOSURE;
    col = (col - 0.5) * CONTRAST + 0.5;
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(lum), col, SATURATION);

    // --- fade the whole look in with sanityIntensity (no hard switch at sanity 49) ---
    vec3 orig = texture2D(screenTexture, uv).rgb;
    gl_FragColor = vec4(mix(orig, clamp(col, 0.0, 1.0), sanityIntensity), 1.0);
}
