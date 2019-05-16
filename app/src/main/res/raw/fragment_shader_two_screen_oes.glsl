#version 300 es
precision mediump float; // 精度

in vec2 vTextureCoord;
uniform sampler2D sTexture;
out vec4  gl_FragColor;
void main(){
    vec2 st = vTextureCoord.xy;
    st.y *=2.0;
    st = fract(st);
    gl_FragColor = texture(sTexture, st);
}