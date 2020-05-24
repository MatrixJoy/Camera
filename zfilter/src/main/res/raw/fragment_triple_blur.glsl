#version 300 es

precision mediump float;// 精度


const vec2 samplerSteps = vec2(40.0/1280.0, 0.0);
const int samplerRadius = 8;

in vec2 vTextureCoord;

uniform sampler2D sTexture;// 输入流

out vec4 fragColor;


float random(vec2 seed){
    return fract(sin(dot(seed, vec2(12.9898, 78.233))) * 43758.5453);
}

vec2 scaleTex(float scaleRatio){
    vec2 center = vec2(0.5, 0.5);
    vec2 tex = (vTextureCoord - center) / scaleRatio + center;
    return tex;
}

void main() {

    float strength = 1.0;

    strength = 1.;
    vec4 resultColor = vec4(0.0);
    float blurPixels = 0.0;

    vec2 tex = scaleTex(1.3);

    float offset = random(tex) - 0.5;

    if (vTextureCoord.y < 1./ 3. || vTextureCoord.y > 2./3.){
        for (int i = -samplerRadius; i <= samplerRadius; i ++) {
            float percent = (float(i) + offset) / float(samplerRadius);
            float weight = 1.0 - abs(percent);
            vec2 coord = tex + samplerSteps * percent * strength;
            resultColor += texture(sTexture, coord) * weight;
            blurPixels += weight;
        }
        resultColor /= blurPixels;
    } else {
        resultColor = texture(sTexture, vTextureCoord);
    }


    fragColor = resultColor;

}
