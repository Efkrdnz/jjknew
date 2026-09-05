#!/bin/sh
# Runs the domain geometry assertions against the real sources in src/main/java.
# No Gradle, no Minecraft, no network — just javac and java.
set -e
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../.." && pwd)
OUT="$HERE/build"

rm -rf "$OUT"
mkdir -p "$OUT/src/net/efkrdnz/jjkstrongest/domain"

# The classes under test, copied verbatim from the mod. If one of these grows a
# dependency on something outside the stubs, the harness will say so loudly.
for f in DomainPhase DomainSphere DomainShell DomainShellProfile DomainOcclusion DomainIntersect RippleField; do
    cp "$ROOT/src/main/java/net/efkrdnz/jjkstrongest/domain/$f.java" "$OUT/src/net/efkrdnz/jjkstrongest/domain/"
done
cp -r "$HERE/net" "$OUT/src/"
cp "$HERE/GeomTest.java" "$OUT/src/"

javac -nowarn -d "$OUT/classes" $(find "$OUT/src" -name '*.java')
java -cp "$OUT/classes" GeomTest
