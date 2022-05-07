#version 130

uniform sampler2D textureIn;
uniform int something;
uniform vec2 dvdPosition;

//Algorithm by sam hocevar
vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

//Algorithm by hughsk
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}
void main() {
    vec2 diskUv = vec2(1, 634) / 1024;
    vec2 diskSize = vec2(162, 78) / 1024;
    float renderScale = 0.5;
    vec2 renderSize = diskSize * renderScale;
    vec2 positionTopLeft = dvdPosition / 1024;
    vec2 positionBottomRight = positionTopLeft + renderSize;
    vec4 texture = texture2D(textureIn, gl_TexCoord[0].st);
    // vec4 texture = vec4(1.0, 0.0, 1.0, 1.0);
    vec2 offset = gl_TexCoord[0].st - positionTopLeft;

    if (0 <= offset.x && offset.x < renderSize.x && 0 <= offset.y && offset.y < renderSize.y) {
        vec4 diskTexture = texture2D(textureIn, diskUv + offset / renderScale);
        // vec4 diskTexture = vec4(1.0, 0.0, 0.0, 1.0);
        if (diskTexture.w > 0.5) {
            vec3 hehe = rgb2hsv(diskTexture.xyz);

            float otherHue = float(something) / 255.0;
            if (otherHue > 1){
                hehe.x = 1;
            } else if (otherHue < 0){
                hehe.x = 0;
            } else {
                hehe.x = otherHue;
            }

            texture = vec4(hsv2rgb(hehe), 1.0);
        }
    }
    gl_FragColor = texture;
}
