#version 300 es
precision mediump float;// 精度

in vec2 vTextureCoord;
uniform sampler2D sTexture;
out vec4  gl_FragColor;

vec4 scale(float scaleRadio){
    vec2 center = vec2(0.0, 0.0);
    vec2 tex = (vTextureCoord-center) / scaleRadio + center;
    tex = mod(tex, 1.0);
    return texture(sTexture, tex);
}

void main(){
    gl_FragColor = scale(0.5);
}