#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require // 扩展类型
precision mediump float; // 精度

in vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
out vec4 fragColor;
void main(){
    fragColor = texture(sTexture, vTextureCoord);
}