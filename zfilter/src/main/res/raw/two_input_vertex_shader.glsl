#version 300 es
uniform mat4 uMVPMatrix;// 投影矩阵
uniform mat4 uTexMatrix;// 纹理矩阵

in vec4 aPosition;// 顶点坐标

in vec4 aTextureCoord;// 纹理坐标

in vec4 aTextureCords2;// 纹理坐标

out vec2 vTextureCoord;
out vec2 vTextureCords2;

void main() {
    gl_Position = uMVPMatrix * aPosition;

    vTextureCoord = (uTexMatrix * aTextureCoord).xy;

    vTextureCords2 = (uTexMatrix * aTextureCords2).xy;
}
