#!/usr/bin/env python3
"""Cross-checks a core shader's json against its GLSL and against the Java that drives it.

A uniform whose name, type or count disagrees between the three compiles cleanly and then
silently does nothing at runtime, so this is the one check worth running on every shader
change. Usage:  tools/check-uniforms.py uv_interior uv_shards ...   (names under shaders/core)

For the Java side, pass  name=JAVA_CONSTANT  e.g.  uv_interior=UV_INTERIOR_SHADER  and the
setUniform / setUniformArray calls on that constant in JjkShaderManager are compared too.
"""
import json, re, sys

BASE = 'src/main/resources/assets/jjk_strongest/shaders/core/'
JAVA = 'src/main/java/net/efkrdnz/jjkstrongest/client/JjkShaderManager.java'
SCALAR = {'float': 1, 'vec2': 2, 'vec3': 3, 'vec4': 4, 'mat4': 16, 'int': 1}
VANILLA = {'ModelViewMat', 'ProjMat'}   # set by ShaderInstance.apply, not by us

def check(name, const):
    d = json.load(open(BASE + name + '.json'))
    declared = {u['name']: u for u in d['uniforms']}
    samplers = {s['name'] for s in d.get('samplers', [])}
    bad, seen = [], set()
    for stage in ('vsh', 'fsh'):
        try:
            src = open(BASE + name + '.' + stage).read()
        except FileNotFoundError:
            continue
        # uniform <type> <name>[N];
        for t, n, arr in re.findall(r'^uniform\s+(\w+)\s+(\w+)\s*(?:\[\s*(\d+)\s*\])?\s*;', src, re.M):
            if t.startswith('sampler'):
                if n not in samplers:
                    bad.append(f'{stage}: sampler {n} not declared')
                continue
            seen.add(n)
            if n not in declared:
                bad.append(f'{stage}: {n} read but never declared'); continue
            want = SCALAR.get(t, 0) * (int(arr) if arr else 1)
            if arr and t != 'float':
                bad.append(f'{stage}: {n} is an array of {t}; only float arrays upload as one buffer')
            u = declared[n]
            if want != u['count']:
                bad.append(f'{stage}: {n} is {t}{"["+arr+"]" if arr else ""} but json count {u["count"]}')
            if len(u['values']) not in (1, u['count']):
                bad.append(f'{n}: {len(u["values"])} defaults for count {u["count"]}')
    for n in declared:
        if n not in seen:
            bad.append(f'{n} declared in json but read by neither stage (driver will optimise it out)')
    # attributes: every json attribute must be an `in` of the vertex stage, and vice versa
    try:
        vsh = open(BASE + name + '.vsh').read()
        ins = set(re.findall(r'^in\s+\w+\s+(\w+)\s*;', vsh, re.M))
        attrs = set(d.get('attributes', []))
        for a in attrs - ins:
            bad.append(f'attribute {a} declared in json but the vsh has no `in` for it')
        for a in ins - attrs:
            bad.append(f'vsh reads attribute {a} that the json does not declare')
    except FileNotFoundError:
        pass
    if const:
        src = open(JAVA).read()
        calls = {}
        # Walk each setUniform(CONST, "name", ...) call by paren depth rather than by regex, so
        # casts like (float) w inside the argument list count as one argument, not a stop.
        for m in re.finditer(r'setUniform\(%s,\s*"(\w+)"' % const, src):
            depth, c, i = 1, 0, m.end()
            while i < len(src) and depth > 0:
                ch = src[i]
                if ch == '(': depth += 1
                elif ch == ')': depth -= 1
                elif ch == ',' and depth == 1: c += 1
                i += 1
            calls[m.group(1)] = c
        for m in re.finditer(r'setUniformArray\(%s,\s*"(\w+)"' % const, src):
            calls[m.group(1)] = 'array'
        for n, c in calls.items():
            if n not in declared:
                bad.append(f'Java sets {n}, json does not declare it')
            elif c != 'array' and declared[n]['count'] != c:
                bad.append(f'Java sets {n} with {c} floats, json count {declared[n]["count"]}')
            elif c == 'array' and declared[n]['count'] <= 4:
                bad.append(f'Java uploads {n} as an array but json count is {declared[n]["count"]}')
        for n in declared:
            if n not in calls and n not in VANILLA:
                bad.append(f'json declares {n}, Java never sets it')
        for m in re.finditer(r'%s\.setSampler\("(\w+)"' % const, src):
            if m.group(1) not in samplers:
                bad.append(f'Java sets sampler {m.group(1)}, not in json')
    print(f'{name}: ' + ('json, GLSL' + (' and Java' if const else '') + ' agree' if not bad else 'PROBLEMS -> ' + '; '.join(bad)))
    return not bad

ok = True
for arg in sys.argv[1:]:
    name, _, const = arg.partition('=')
    ok = check(name, const or None) and ok
sys.exit(0 if ok else 1)
