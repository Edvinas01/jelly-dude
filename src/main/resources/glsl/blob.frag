varying vec4 coord;

uniform int blobCount = 0;
uniform samplerBuffer tbo;



float calcD(vec3 blob, float size, float intensity){
    float d = distance(vec3(coord), blob);
    if(d <= 0){
        return 0.0;
    }

    d/=sqrt(size);

    return exp(-1*intensity*d*d);
}

float filter(float dist, float x1, float x2, float x3, float x4){
  if(dist < x1 || dist > x4){
    return 0.0;
  }
  if(dist > x2 && dist < x3){
    return 1.0;
  }
  return dist;
}

float cap(float dist, float begin, float end){
   float it = 0;
   if(dist < begin){
     return 0.0;
   }else if(dist > end){
     return 1.0;
   } else {
      float pi = 3.14;
      return .5 + .5 * cos (pi*(-1+1/(end-begin)*(dist-begin)) );
   }
}

void main(void) {

   float dist = 0;
   float sum = 0;
   vec3 color = vec3(0,0,0);

   for(int i=0 ; i<blobCount ; ++i){
      //read tbo
      vec3 tboBlob = texelFetch(tbo, i).xyz;
      vec3 tboColor = texelFetch(tbo,i+blobCount).xyz;
      float tboSize = texelFetch(tbo,i+2*blobCount).x;

      float d = calcD(tboBlob, tboSize, 2);

      float s = dot(tboBlob - vec3(coord), vec3(1,0,1));

      dist += d;
      sum += d * s;
      color += d * tboColor;
   }

   if(length(color)>1.5){
     color = 1.5*normalize(color);
   }

   //default
   //vec3 baseColor = vec3(0.2, 0.8, 0.1);

   //with weight
   vec3 baseColor = color;

   float it = cap(dist,0.7,0.75);
   float specMap = cap(1-dist,0.0,0.3);
   float trans = it * (1 - 0.25*cap(dist,0.8, 1));

   float spec = sum * it * specMap;
   vec4 specColor = .4 * vec4(1,1,1,1) * spec;

   gl_FragColor = vec4(it * baseColor, trans ) + specColor;

}
