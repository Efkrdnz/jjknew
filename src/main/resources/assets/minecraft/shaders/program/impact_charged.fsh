#version 150

uniform sampler2D DiffuseSampler;
uniform float DesaturateAmount;
uniform float GammaBoost;
uniform float Contrast;
uniform float RedTint;
uniform float Saturation;

in vec2 texCoord;
out vec4 fragColor;

// convert to grayscale
float luminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

// apply contrast
vec3 applyContrast(vec3 color, float contrast) {
    return (color - 0.5) * contrast + 0.5;
}

// apply saturation
vec3 applySaturation(vec3 color, float saturation) {
    float gray = luminance(color);
    return mix(vec3(gray), color, saturation);
}

// detect fullbright slashes
bool isFullbrightSlash(vec3 color) {
    float maxChannel = max(max(color.r, color.g), color.b);
    return maxChannel > 0.95;
}

// equalize brightness - compress range to 0.2-0.5
float equalizeBrightness(float lum) {
    // map 0.0-1.0 to 0.2-0.5 range
    return 0.2 + (lum * 0.3);
}

void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb;
    
    bool isSlash = isFullbrightSlash(color);
    
    // APPLY EFFECTS TO NON-SLASH PIXELS
    if (!isSlash) {
        // STEP 1: DESATURATE TO GRAYSCALE
        float gray = luminance(color);
        if (DesaturateAmount > 0.0) {
            color = mix(color, vec3(gray), DesaturateAmount);
        }
        
        // STEP 2: EQUALIZE BRIGHTNESS (compress to 0.2-0.5 range)
        float originalLum = luminance(color);
        float equalizedLum = equalizeBrightness(originalLum);
        
        // apply equalized brightness
        if (originalLum > 0.001) {
            color = color * (equalizedLum / originalLum);
        }
        
        // STEP 3: APPLY EXTREME CONTRAST (recreates blacks/reds)
        if (Contrast != 1.0) {
            color = applyContrast(color, Contrast);
        }
        
        // STEP 4: APPLY RED TINT (only to non-black areas)
        if (RedTint > 0.0) {
            float brightness = luminance(color);
            
            // only apply red to areas above black threshold
            if (brightness > 0.05) {
                // push toward red
                color.r = color.r + (1.0 - color.r) * RedTint * brightness;
                // suppress green/blue
                color.g = color.g * (1.0 - RedTint * 0.85);
                color.b = color.b * (1.0 - RedTint * 0.85);
            } else {
                // force pure black
                color = vec3(0.0);
            }
        }
        
        // STEP 5: BOOST SATURATION (makes reds intense)
        if (Saturation != 1.0) {
            color = applySaturation(color, Saturation);
        }
        
        // STEP 6: NO GAMMA BOOST (keep it dark)
        // disabled to maintain black/red aesthetic
    }
    
    // clamp
    color = clamp(color, 0.0, 1.0);
    
    fragColor = vec4(color, 1.0);
}