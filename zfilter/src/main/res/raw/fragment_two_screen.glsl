#version 300 es
precision mediump float;// 精度
in vec2 vTextureCoord;

uniform sampler2D sTexture;

out vec4 fragColor;


void main() {
    vec4 fColor;
    if (vTextureCoord.y>0.5){
        fColor = texture(sTexture, vec2(vTextureCoord.x, vTextureCoord.y-0.25));
    } else {
        fColor = texture(sTexture, vec2(vTextureCoord.x, vTextureCoord.y+0.25));
    }
    fragColor = fColor;
}
