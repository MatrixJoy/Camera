#version 300 es
precision mediump float;// 精度
in vec2 vTextureCoord;

uniform sampler2D sTexture;

out vec4 fragColor;


void main() {
    vec4 fColor;
    if (vTextureCoord.y>2./3.){
        fColor = texture(sTexture, vec2(vTextureCoord.x, vTextureCoord.y-1./3.));
    } else if (vTextureCoord.y<1./3.){
        fColor = texture(sTexture, vec2(vTextureCoord.x, vTextureCoord.y+1./3.));
    } else {
        fColor = texture(sTexture, vTextureCoord);
    }
    fragColor = fColor;
}
