#version 150

uniform float Time;
uniform float Strength;

in vec2 texCoord;
out vec4 fragColor;

const float PI = 3.14159265359;

mat2 rot(float a) {
	float s = sin(a), c = cos(a);
	return mat2(c, -s, s, c);
}

float hash11(float p) {
	p = fract(p * 0.1031);
	p *= p + 33.33;
	p *= p + p;
	return fract(p);
}

vec3 hueShift(vec3 c, float a) {
	float s = sin(a), co = cos(a);
	mat3 m = mat3(
		0.299 + 0.701*co + 0.168*s, 0.587 - 0.587*co + 0.330*s, 0.114 - 0.114*co - 0.497*s,
		0.299 - 0.299*co - 0.328*s, 0.587 + 0.413*co + 0.035*s, 0.114 - 0.114*co + 0.292*s,
		0.299 - 0.300*co + 1.250*s, 0.587 - 0.588*co - 1.050*s, 0.114 + 0.886*co - 0.203*s
	);
	return clamp(m * c, 0.0, 1.0);
}

float sdSegment(vec2 p, vec2 a, vec2 b) {
	vec2 pa = p - a;
	vec2 ba = b - a;
	float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
	return length(pa - ba * h);
}

float lineAA(float d, float w) {
	return 1.0 - smoothstep(w, w * 1.9, d);
}

vec2 hypotrochoid(float th, float R, float r, float d) {
	float k = (R - r) / r;
	return vec2(
		(R - r) * cos(th) + d * cos(k * th),
		(R - r) * sin(th) - d * sin(k * th)
	);
}

void pickParams(float id, out float R, out float r, out float d, out float spin, out float petals) {
	float a = hash11(id * 7.1);
	float b = hash11(id * 11.3);
	float c = hash11(id * 19.7);

	float ri = floor(3.0 + a * 7.0);
	float rj = floor(2.0 + b * 6.0);

	R = 1.00;
	r = R * (rj / ri);
	d = 0.55 + 0.35 * c;

	spin = (hash11(id * 29.9) * 2.0 - 1.0) * 0.9;
	petals = ri + rj;
}

float symmetricSpiro(vec2 p, float t, float k) {
	float segMin = 1e9;

	float block = t * 0.5;
	float id0 = floor(block);
	float id1 = id0 + 1.0;
	float u = fract(block);
	float tr = smoothstep(0.00, 0.12, u);

	float R0, r0, d0, s0, m0;
	float R1, r1, d1, s1, m1;
	pickParams(id0, R0, r0, d0, s0, m0);
	pickParams(id1, R1, r1, d1, s1, m1);

	float R = mix(R0, R1, tr);
	float r = mix(r0, r1, tr);
	float d = mix(d0, d1, tr);
	float spin = mix(s0, s1, tr);
	float petals = mix(m0, m1, tr);

	float Nf = mix(6.0, 10.0, smoothstep(0.2, 0.9, k));
	float N = floor(Nf + 0.5);

	float scale = 0.52;
	vec2 pp = p * scale;

	float w1 = 0.55 + 0.45 * sin(t * 0.35);
	float w2 = 0.55 + 0.45 * sin(t * 0.47 + 1.7);

	float angSpin = t * (0.20 + 0.65 * k) + spin;

	for (int s = 0; s < 64; s++) {
		float fs = float(s) / 64.0;
		float thA = fs * 2.0 * PI;
		float thB = (float(s + 1) / 64.0) * 2.0 * PI;

		float thAw = thA + sin(thA * petals + t * 0.8) * 0.02 * k;
		float thBw = thB + sin(thB * petals + t * 0.8) * 0.02 * k;

		vec2 a = hypotrochoid(thAw, R, r, d);
		vec2 b = hypotrochoid(thBw, R, r, d);

		a *= (0.78 + 0.06 * w1);
		b *= (0.78 + 0.06 * w2);

		for (int i = 0; i < 10; i++) {
			float fi = float(i);
			if (fi >= N) break;

			float ra = (fi / N) * 2.0 * PI + angSpin;
			mat2 rr = rot(ra);

			vec2 A = rr * a;
			vec2 B = rr * b;

			float dseg = sdSegment(pp, A, B);
			segMin = min(segMin, dseg);
		}
	}

	return segMin;
}

void main() {
	float k = clamp(Strength, 0.0, 1.0);
	float t = Time;

	vec2 uv = texCoord;
	vec2 p = uv * 2.0 - 1.0;

	float r = length(p);
	float vign = smoothstep(1.20, 0.16, r);

	float d = symmetricSpiro(p, t, k);

	float w = (0.0036 - 0.0016 * k) * (0.55 + 0.45 * vign);
	float wires = lineAA(d, w);
	float glow = lineAA(d, w * 2.4) * 0.50;

	float phase = fract(t * 0.5);
	float snap = smoothstep(0.992, 1.0, phase) * (0.08 + 0.18 * k);

	vec3 baseCol = vec3(0.08, 0.70, 1.00);
	vec3 altCol = vec3(0.75, 0.20, 0.95);
	vec3 col = mix(baseCol, altCol, 0.20 + 0.55 * k);
	col = hueShift(col, (t * 0.10) * (0.22 + 0.65 * k));

	vec3 bg = vec3(0.01, 0.015, 0.03);
	float mixAmt = clamp(wires + glow * 0.9, 0.0, 1.0);

	col = mix(bg, col, mixAmt);
	col += vec3(0.35, 0.70, 1.0) * glow * (0.10 + 0.25 * k);
	col *= (0.85 + 0.15 * vign);

	float alpha = (wires * (0.16 + 0.52 * k) + glow * (0.12 + 0.46 * k)) * vign;
	alpha += snap * vign;

	alpha = clamp(alpha, 0.0, 0.86);

	fragColor = vec4(col, alpha);
}
