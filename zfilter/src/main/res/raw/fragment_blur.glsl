#version 300 es

precision mediump float;// 精度
#define PI 3.14159265359

const vec2 samplerSteps = vec2(60.0/1280.0, 60.0/1080.0);
const int samplerRadius = 8;

in vec2 vTextureCoord;

uniform sampler2D sTexture;// 输入流
uniform vec2 transform;// 旋转角度

uniform float aspectRatio;

uniform float angle;

out vec4 fragColor;


float random(vec2 seed){
    return fract(sin(dot(seed, vec2(12.9898, 78.233))) * 43758.5453);
}

vec2 scaleTex(float scaleRatio){
    vec2 center = vec2(0.5, 0.5);
    vec2 tex = (vTextureCoord - center) / scaleRatio + center;
    return tex;
}


vec2 rotate(vec2 tex, float angle){
    vec2 center = vec2(0.5, 0.5);
    mat2 rotate = mat2(cos(angle), sin(angle),
    -sin(angle), cos(angle));
    tex-=center;
    tex.x*=aspectRatio;
    tex *= rotate;
    tex.x*=1.0/aspectRatio;
    tex+=center;
    return tex;
}

void main() {

    float strength = 1.0;
    strength = 1.;
    vec4 resultColor = vec4(0.0);
    float blurPixels = 0.0;
    vec2 center = vec2(0.5, 0.5);


    float offset = random(vTextureCoord);

    for (int i = -samplerRadius; i <= samplerRadius; i ++) {
        float percent = (float(i) + offset) / float(samplerRadius);
        float weight = 1.0 - abs(percent);
        vec2 coord = vTextureCoord + samplerSteps * percent * strength;
        coord = rotate(coord, angle);
        resultColor += texture(sTexture, coord) * weight;
        blurPixels += weight;
    }
    resultColor /= blurPixels;

    if (vTextureCoord.y>transform.y || vTextureCoord.y<transform.y){
        vec2 tex = scaleTex(0.5);
        tex = rotate(tex, angle);
        tex+=transform;
        if (tex.x > 1.0 || tex.y>1.0||tex.x<0.0||tex.y<0.0){
            fragColor = resultColor;
        } else {
            fragColor = texture(sTexture, tex);
        }
    } else {
        fragColor = resultColor;
    }
}
