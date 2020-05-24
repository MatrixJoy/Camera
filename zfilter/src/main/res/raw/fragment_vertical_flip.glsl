#version  300 es
precision mediump float;// 精度
in vec2 vTextureCoord;

uniform sampler2D sTexture;

out vec4 fragColor;

void main(){
    if (vTextureCoord.y>0.5){
        fragColor = texture(sTexture, vTextureCoord);
    } else {
        fragColor = texture(sTexture, vec2(vTextureCoord.x, 1.0-vTextureCoord.y));
    }
}
