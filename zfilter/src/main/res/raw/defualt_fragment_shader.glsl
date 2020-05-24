#version  300 es
precision mediump float;// 精度
in vec2 vTextureCoord;

uniform sampler2D sTexture;

out vec4 fragColor;

void main(){
    fragColor = texture(sTexture, vTextureCoord);
}
