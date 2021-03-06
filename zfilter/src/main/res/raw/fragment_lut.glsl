#version 300 es
precision mediump float;// 精度

in vec2 vTextureCoord;
//in vec2 vTextureCords2;

uniform sampler2D sTexture;
uniform sampler2D sTexture2;
out vec4  gl_FragColor;

uniform lowp float intensity;

vec4 lookUP(vec4 color, sampler2D lookuptexture){
    highp float blueColor = color.b*63.0;

    highp vec2 quad1;

    quad1.y = floor(floor(blueColor) / 8.0);
    quad1.x = floor(blueColor) - (quad1.y * 8.0);

    highp vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 8.0);
    quad2.x = ceil(blueColor) - (quad2.y * 8.0);

    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * color.r);
    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * color.g);

    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * color.r);
    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * color.g);

    lowp vec4 newColor1 = texture(lookuptexture, texPos1);
    lowp vec4 newColor2 = texture(lookuptexture, texPos2);

    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
    return newColor;
}

void main() {
    vec4 textureColor = texture(sTexture, vTextureCoord);
    vec4 newColor = lookUP(textureColor, sTexture2);
    gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), intensity);
}
