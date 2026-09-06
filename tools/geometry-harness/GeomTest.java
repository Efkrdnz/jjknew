import net.efkrdnz.jjkstrongest.domain.*;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

public class GeomTest {
    // Unlimited Void's own barrier settings, transcribed from DomainDefinition. Not
    // imported from it: DomainDefinition pulls in Minecraft's registries, which is exactly
    // what this harness exists to run without.
    static final DomainShellProfile VOID_SHELL = new DomainShellProfile(140, 1.5f, 1.0f, 140);

    static int pass = 0, fail = 0;
    static void check(String name, boolean ok, String detail) {
        if (ok) { pass++; System.out.println("  PASS  " + name); }
        else { fail++; System.out.println("  FAIL  " + name + "   " + detail); }
    }
    static boolean near(double a, double b, double eps) { return Math.abs(a - b) <= eps; }

    // The mesh's own convention, transcribed from DomainUVRenderer.buildUnitSphere:
    //   x = sin(theta)cos(phi), y = cos(theta), z = sin(theta)sin(phi)
    //   u = phi/2pi, v = theta/pi
    static int meshCell(Vec3 d) {
        double theta = Math.acos(Math.max(-1, Math.min(1, d.y)));
        double phi = Math.atan2(d.z, d.x);
        double u = phi / (Math.PI * 2.0); if (u < 0) u += 1.0;
        double v = theta / Math.PI;
        int lat = Math.min(15, (int) (v * 16));
        int lon = Math.min(31, (int) (u * 32));
        return lat * 32 + lon;
    }

    public static void main(String[] args) {
        Random rng = new Random(20260905L);

        System.out.println("longAxis: perpendicular to direction, and unit length");
        boolean perpOk = true, unitOk = true;
        for (int i = 0; i < 20000; i++) {
            Vec3 dir = new Vec3(rng.nextDouble()*2-1, rng.nextDouble()*2-1, rng.nextDouble()*2-1).normalize();
            if (dir.length() < 0.5) continue;
            float roll = (float)(rng.nextDouble() * Math.PI * 2);
            Vec3 u = DomainOcclusion.longAxis(dir, roll);
            if (!near(u.dot(dir), 0.0, 1e-9)) perpOk = false;
            if (!near(u.length(), 1.0, 1e-9)) unitOk = false;
        }
        check("perpendicular to direction (20k random)", perpOk, "dot != 0");
        check("unit length (20k random)", unitOk, "|u| != 1");

        Vec3 u0 = DomainOcclusion.longAxis(new Vec3(0,0,1), 0f);
        check("dir=+Z, roll=0 -> +X", near(u0.x,1,1e-9)&&near(u0.y,0,1e-9)&&near(u0.z,0,1e-9), "got " + u0);

        Vec3 uUp = DomainOcclusion.longAxis(new Vec3(0,1,0), 0f);
        check("degenerate dir=+Y is finite and unit",
              !Double.isNaN(uUp.x) && near(uUp.length(),1,1e-9), "got " + uUp);
        check("degenerate dir=+Y perpendicular",
              near(uUp.dot(new Vec3(0,1,0)),0,1e-9), "got " + uUp);

        System.out.println("\nDomainOcclusion.clip");
        DomainSphere sphere = new DomainSphere(new Vec3(0,0,0), 30.0, -1000.0, DomainPhase.ACTIVE, 1f);
        // direction=+Z with roll=pi gives long axis -X
        Vec3 dirZ = new Vec3(0,0,1);
        float rollPi = (float) Math.PI;

        DomainOcclusion.Clip crossing = DomainOcclusion.clip(new Vec3(40,0,0), dirZ, rollPi, 30.0, sphere);
        check("crossing: not blocked", !crossing.blocked(), "blocked");
        check("crossing: length 30 -> 25", near(crossing.length(), 25.0, 1e-6), "len=" + crossing.length());
        check("crossing: centre moves to x=42.5", near(crossing.position().x, 42.5, 1e-6), "pos=" + crossing.position());
        check("crossing: impact lands on the surface",
              crossing.impact()!=null && near(crossing.impact().length(), 30.0, 1e-6),
              "impact=" + crossing.impact());

        DomainOcclusion.Clip clear = DomainOcclusion.clip(new Vec3(80,0,0), dirZ, rollPi, 30.0, sphere);
        check("clear of the sphere: untouched", !clear.blocked() && near(clear.length(),30.0,1e-9) && clear.impact()==null,
              "len=" + clear.length() + " impact=" + clear.impact());

        DomainOcclusion.Clip inside = DomainOcclusion.clip(new Vec3(0,0,0), dirZ, rollPi, 10.0, sphere);
        check("wholly inside: blocked", inside.blocked(), "not blocked");

        System.out.println("\nDomainShell.cellFor agrees with the sphere mesh's UV");
        int mismatches = 0; Vec3 firstBad = null;
        for (int i = 0; i < 50000; i++) {
            Vec3 d = new Vec3(rng.nextDouble()*2-1, rng.nextDouble()*2-1, rng.nextDouble()*2-1);
            if (d.length() < 0.2) continue;
            d = d.normalize();
            if (DomainShell.cellFor(d.x,d.y,d.z) != meshCell(d)) { mismatches++; if (firstBad==null) firstBad = d; }
        }
        check("50k random directions map to the same cell", mismatches == 0,
              mismatches + " mismatches, first at " + firstBad);
        check("+X is longitude 0", DomainShell.cellFor(1,0,0) % 32 == 0, "lon=" + (DomainShell.cellFor(1,0,0)%32));
        check("+Z is a quarter turn round", DomainShell.cellFor(0,0,1) % 32 == 8, "lon=" + (DomainShell.cellFor(0,0,1)%32));
        check("+Y is the top band", DomainShell.cellFor(0,1,0) / 32 == 0, "lat=" + (DomainShell.cellFor(0,1,0)/32));
        check("-Y is the bottom band", DomainShell.cellFor(0,-1,0) / 32 == 15, "lat=" + (DomainShell.cellFor(0,-1,0)/32));

        System.out.println("\nTwo failure shapes from one grid");
        DomainShell pressed = new DomainShell(VOID_SHELL);
        int ticks = 0;
        while (!pressed.isShattered() && ticks < 5000) { pressed.applyPressure(DomainShell.FULL / 440f); ticks++; }
        check("even pressure shatters the whole shell", pressed.isShattered(), "not shattered after " + ticks);
        check("...in roughly the intended ~440 ticks", ticks >= 400 && ticks <= 480, "took " + ticks);

        DomainShell punched = new DomainShell(VOID_SHELL);
        Vec3 spot = new Vec3(1, 0.3, 0.2).normalize();
        int punches = 0;
        while (punched.breachCount() == 0 && punches < 500) { punched.applyStrike(spot, 26.0f, 2); punches++; }
        check("concentrated strikes open a hole", punched.breachCount() > 0, "no breach after " + punches);
        check("...in roughly ten hits", punches >= 6 && punches <= 16, "took " + punches);
        check("...while the shell overall is still healthy",
              punched.totalIntegrity() > 0.9f, "integrity=" + punched.totalIntegrity());
        check("...and the hole is where it was hit",
              punched.isOpenTowards(spot.x, spot.y, spot.z), "breach elsewhere");
        check("...and not on the far side",
              !punched.isOpenTowards(-spot.x, -spot.y, -spot.z), "opposite side also open");

        System.out.println("\nDomainSphere.clampMovement");
        DomainSphere room = new DomainSphere(new Vec3(0,0,0), 30.0, -1.0, DomainPhase.ACTIVE, 1f);
        double half = 0.3; // player half-width

        // walking straight at the wall from inside: the outward component goes, the rest stays
        Vec3 atWall = room.clampMovement(new Vec3(29.5,0,0), half, new Vec3(1,0,0), null);
        check("inside, into the wall: outward motion removed", near(atWall.x, 0, 1e-6), "x=" + atWall.x);

        // running along the wall must still slide, not stick
        Vec3 along = room.clampMovement(new Vec3(29.5,0,0), half, new Vec3(0.4,0,0.4), null);
        check("inside, along the wall: tangential motion kept", along.z > 0.35, "z=" + along.z);
        check("inside, along the wall: still inside afterwards",
              new Vec3(29.5,0,0).add(along).length() <= 30.0 + 1e-6,
              "|p|=" + new Vec3(29.5,0,0).add(along).length());

        // free movement in open space is untouched
        Vec3 free = room.clampMovement(new Vec3(0,0,0), half, new Vec3(0.5,0,0.2), null);
        check("inside, well clear: movement untouched", near(free.x,0.5,1e-9) && near(free.z,0.2,1e-9), "got " + free);

        // the floor plane holds you up
        Vec3 falling = room.clampMovement(new Vec3(0,-1.0,0), half, new Vec3(0,-0.5,0), null);
        check("floor plane stops a fall", near(-1.0 + falling.y, -1.0, 1e-9), "y=" + falling.y);

        // and it does not stop you rising
        Vec3 rising = room.clampMovement(new Vec3(0,-1.0,0), half, new Vec3(0,0.5,0), null);
        check("floor plane does not block going up", near(rising.y, 0.5, 1e-9), "y=" + rising.y);

        // from outside the shell is solid too
        Vec3 barging = room.clampMovement(new Vec3(30.5,0,0), half, new Vec3(-1,0,0), null);
        check("outside, into the wall: inward motion removed", near(barging.x, 0, 1e-6), "x=" + barging.x);

        // a breach is a way through, from either side
        DomainShell holed = new DomainShell(VOID_SHELL);
        Vec3 through = new Vec3(1,0,0);
        for (int i = 0; i < 60 && !holed.isOpenTowards(through.x, through.y, through.z); i++)
            holed.applyStrike(through, 26.0f, 2);
        check("a hole can actually be opened", holed.isOpenTowards(1,0,0), "still sealed");
        Vec3 escaping = room.clampMovement(new Vec3(29.5,0,0), half, new Vec3(1,0,0), holed);
        check("breach lets you out", near(escaping.x, 1.0, 1e-9), "x=" + escaping.x);
        Vec3 blockedElsewhere = room.clampMovement(new Vec3(-29.5,0,0), half, new Vec3(-1,0,0), holed);
        check("the far side is still sealed", near(blockedElsewhere.x, 0, 1e-6), "x=" + blockedElsewhere.x);

        // no clamp should ever lengthen a move
        Random r2 = new Random(7L);
        boolean grew = false;
        for (int i = 0; i < 20000; i++) {
            Vec3 p = new Vec3(r2.nextDouble()*80-40, r2.nextDouble()*80-40, r2.nextDouble()*80-40);
            Vec3 mv = new Vec3(r2.nextDouble()-0.5, r2.nextDouble()-0.5, r2.nextDouble()-0.5);
            if (room.clampMovement(p, half, mv, null).length() > mv.length() + 1e-9) grew = true;
        }
        check("a clamp never lengthens a move (20k random)", !grew, "some clamp grew the vector");

        System.out.println("\nclampFloorWithin (the floor that outlives the shell)");
        Vec3 standing = new Vec3(5, -1.0, 0);
        Vec3 held = room.clampFloorWithin(standing, new Vec3(0,-0.8,0), 30.0);
        check("floor still holds inside the carved footprint", near(standing.y + held.y, -1.0, 1e-9), "y=" + held.y);
        Vec3 outsideFootprint = new Vec3(45, -1.0, 0);
        Vec3 unheld = room.clampFloorWithin(outsideFootprint, new Vec3(0,-0.8,0), 30.0);
        check("and not beyond it \u2014 no invisible floor across the world",
              near(unheld.y, -0.8, 1e-9), "y=" + unheld.y);
        Vec3 climbing = room.clampFloorWithin(standing, new Vec3(0,0.6,0), 30.0);
        check("floor-only clamp never blocks going up", near(climbing.y, 0.6, 1e-9), "y=" + climbing.y);
        Vec3 sideways = room.clampFloorWithin(standing, new Vec3(0.9,0,0.4), 30.0);
        check("floor-only clamp leaves horizontal motion alone",
              near(sideways.x,0.9,1e-9) && near(sideways.z,0.4,1e-9), "got " + sideways);

        // The plane is a floor, not a magnet. Someone already below it -- in a cave under
        // the domain -- must be left alone entirely; clamping them would launch them up to
        // the plane in a single tick.
        Vec3 inACaveBelow = new Vec3(5, -21.0, 0);
        Vec3 undisturbed = room.clampFloorWithin(inACaveBelow, new Vec3(0,-0.8,0), 30.0);
        check("something already below the plane is not yanked up to it",
              near(undisturbed.y, -0.8, 1e-9), "y=" + undisturbed.y + " (a 20-block launch)");
        Vec3 walkingBelow = room.clampFloorWithin(inACaveBelow, new Vec3(0.5,0,0.5), 30.0);
        check("...and can still walk about down there",
              near(walkingBelow.x,0.5,1e-9) && near(walkingBelow.z,0.5,1e-9), "got " + walkingBelow);

        // Landing writes movement.y = floorY - pos.y and vanilla adds that to getY(). The
        // round-trip can leave you a few ULPs under the plane; without slack in contains()
        // that reads as "outside the domain" and drops you through your own floor.
        double aHairUnder = Math.nextDown(-1.0);
        check("a hair under the plane still counts as inside",
              room.contains(0, aHairUnder, 0), "contains() said outside at y=" + aHairUnder);
        check("and the floor still catches you there",
              near(room.clampFloorWithin(new Vec3(0, aHairUnder, 0), new Vec3(0,-0.8,0), 30.0).y,
                   -1.0 - aHairUnder, 1e-9), "not caught");
        check("but a block under it is genuinely outside",
              !room.contains(0, -2.0, 0), "contains() said inside below the floor");

        System.out.println("\nholes (a hole and a crack are two different clocks)");
        DomainShell punchedShell = new DomainShell(VOID_SHELL);
        Vec3 spotH = new Vec3(1, 0, 0);
        int swings = 0;
        while (!punchedShell.isOpenTowards(spotH.x, spotH.y, spotH.z) && swings < 500) {
            punchedShell.applyStrike(spotH, 26.0f, 2);
            swings++;
        }
        check("ten or so clean swings open a hole", punchedShell.isOpenTowards(spotH.x, spotH.y, spotH.z) && swings <= 12, "took " + swings + " swings");
        check("...and it counts as one breach", punchedShell.breachCount() == 1, "breaches=" + punchedShell.breachCount());

        // 140 ticks is seven seconds. This is the number the whole request turns on: the old
        // build sealed the hole on the FIRST regen tick, sixty ticks in, while it still
        // looked wide open.
        for (int t = 0; t < 139; t++) punchedShell.tickRegen();
        check("still open at 139 ticks", punchedShell.isOpenTowards(spotH.x, spotH.y, spotH.z), "closed early");
        punchedShell.tickRegen();
        check("closed on the 140th", !punchedShell.isOpenTowards(spotH.x, spotH.y, spotH.z), "still open");
        check("and the breach is no longer counted", punchedShell.breachCount() == 0, "breaches=" + punchedShell.breachCount());

        // The crack outlives the hole and heals on its own slower clock.
        int cellH = DomainShell.cellFor(spotH);
        check("the crack is still visible after the hole shuts",
              punchedShell.integrityAt(cellH) < DomainShell.FULL * 0.5f, "integrity " + punchedShell.integrityAt(cellH));
        for (int t = 0; t < 400; t++) punchedShell.tickRegen();
        check("...and eventually heals away entirely",
              near(punchedShell.integrityAt(cellH), DomainShell.FULL, 1e-3), "integrity " + punchedShell.integrityAt(cellH));

        // Hitting an open hole has to hold it open. Before, hurt() returned early on a dead
        // cell and swinging into a gap did nothing at all.
        DomainShell heldShell = new DomainShell(VOID_SHELL);
        while (!heldShell.isOpenTowards(spotH.x, spotH.y, spotH.z)) heldShell.applyStrike(spotH, 26.0f, 2);
        for (int t = 0; t < 100; t++) heldShell.tickRegen();
        heldShell.applyStrike(spotH, 26.0f, 2);
        for (int t = 0; t < 100; t++) heldShell.tickRegen();
        check("hitting an open hole keeps it open past its original clock",
              heldShell.isOpenTowards(spotH.x, spotH.y, spotH.z), "it closed anyway");

        // Zero on the wire means open, so the client can predict collision without the
        // packet carrying a second array.
        DomainShell wire = new DomainShell(VOID_SHELL);
        while (!wire.isOpenTowards(spotH.x, spotH.y, spotH.z)) wire.applyStrike(spotH, 26.0f, 2);
        byte[] snap = wire.snapshot();
        DomainShell far = new DomainShell(VOID_SHELL);
        far.applyCells(snap);
        check("a hole survives the round trip to a client",
              far.isOpenTowards(spotH.x, spotH.y, spotH.z), "client thinks it is closed");
        check("and a merely cracked cell does not read as one",
              (snap[DomainShell.cellFor(new Vec3(-1, 0, 0))] & 0xFF) != 0, "an untouched cell serialised as open");
        check("client breach count matches", far.breachCount() == wire.breachCount(),
              far.breachCount() + " vs " + wire.breachCount());

        System.out.println("\napplyFacePressure (two barriers meeting)");
        // directionOf must be the exact inverse of cellFor, or a clash wears down a face
        // pointing somewhere other than the rival.
        int wrong = 0;
        for (int cell = 0; cell < DomainShell.CELLS; cell++)
            if (DomainShell.cellFor(DomainShell.directionOf(cell)) != cell) wrong++;
        check("directionOf round-trips through cellFor for all 512 cells", wrong == 0, wrong + " cells disagreed");

        DomainShell pressed2 = new DomainShell(VOID_SHELL);
        Vec3 towardRival = new Vec3(1, 0, 0);
        for (int i = 0; i < 400; i++) pressed2.applyFacePressure(towardRival, 1.0f, 0.5);
        int facing = DomainShell.cellFor(towardRival);
        int behind = DomainShell.cellFor(new Vec3(-1, 0, 0));
        check("the face pointing at the rival is worn down",
              pressed2.integrityAt(facing) < DomainShell.FULL * 0.5f, "integrity " + pressed2.integrityAt(facing));
        check("...and the far side is untouched",
              near(pressed2.integrityAt(behind), DomainShell.FULL, 1e-6), "integrity " + pressed2.integrityAt(behind));
        check("a face breaches before the shell shatters",
              pressed2.breachCount() > 0 && !pressed2.isShattered(),
              "breaches=" + pressed2.breachCount() + " shattered=" + pressed2.isShattered());

        DomainShell untouched = new DomainShell(VOID_SHELL);
        untouched.applyFacePressure(new Vec3(0, 0, 0), 5.0f, 0.5);
        check("a degenerate direction does nothing rather than NaN-ing the grid",
              near(untouched.totalIntegrity(), 1.0f, 1e-6), "integrity " + untouched.totalIntegrity());

        System.out.println("\nfracture (broken as one piece, not worn through)");
        // The collapse pass draws a cell below about an eighth as an opening and skips the
        // shard sitting over it, so a shell "shattered" by taking every cell to zero would
        // blink out instead of coming apart. These are the guard rails on that.
        DomainShell cracked = new DomainShell(VOID_SHELL);
        Vec3 breaker = new Vec3(0, 0, 25);
        cracked.fracture(0.18f, breaker);
        check("cracking the shell opens nothing", cracked.breachCount() == 0,
              "breaches=" + cracked.breachCount());
        float lowestCell = DomainShell.FULL;
        float highestCell = 0.0f;
        for (int cell = 0; cell < DomainShell.CELLS; cell++) {
            lowestCell = Math.min(lowestCell, cracked.integrityAt(cell));
            highestCell = Math.max(highestCell, cracked.integrityAt(cell));
        }
        check("every cell stays above the shard pass's hole threshold",
              lowestCell > DomainShell.FULL * 0.13f, "lowest " + (lowestCell / DomainShell.FULL));
        check("...and no part of it comes out uncracked",
              highestCell < DomainShell.FULL * 0.4f, "highest " + (highestCell / DomainShell.FULL));
        check("the shell as a whole reads as badly broken",
              cracked.totalIntegrity() < 0.35f, "integrity " + cracked.totalIntegrity());
        check("the break starts where whatever broke it is standing",
              cracked.weakestDirection().normalize().dot(breaker.normalize()) > 0.9,
              "break dir " + cracked.weakestDirection());
        byte[] crackedWire = cracked.snapshot();
        int openOnWire = 0;
        for (byte b : crackedWire) if ((b & 0xFF) == 0) openOnWire++;
        check("and the client is told about none of it as holes", openOnWire == 0,
              openOnWire + " cells serialised as open");

        DomainShell evenly = new DomainShell(VOID_SHELL);
        evenly.fracture(0.18f, null);
        check("with no direction it cracks evenly",
              near(evenly.integrityAt(0), evenly.integrityAt(DomainShell.CELLS - 1), 1e-3),
              evenly.integrityAt(0) + " vs " + evenly.integrityAt(DomainShell.CELLS - 1));

        DomainShell alreadyOpen = new DomainShell(VOID_SHELL);
        while (!alreadyOpen.isOpenTowards(spotH.x, spotH.y, spotH.z)) alreadyOpen.applyStrike(spotH, 26.0f, 2);
        int openBefore = alreadyOpen.breachCount();
        alreadyOpen.fracture(0.18f, spotH);
        check("a hole that was already there stays a hole",
              alreadyOpen.breachCount() == openBefore && alreadyOpen.isOpenTowards(spotH.x, spotH.y, spotH.z),
              "breaches " + openBefore + " -> " + alreadyOpen.breachCount());

        System.out.println("\nDomainIntersect");
        DomainSphere uv = new DomainSphere(new Vec3(0,0,0), 30, -1000, DomainPhase.ACTIVE, 1f);
        DomainSphere shrineNear = DomainSphere.openField(new Vec3(20,0,0), 100);
        check("a shrine engulfing the void still counts as intersecting",
              DomainIntersect.intersects(uv, shrineNear), "reported apart");
        check("...but the lens is null there, as documented",
              DomainIntersect.intersect(uv, shrineNear) == null, "lens returned");
        check("far apart does not intersect",
              !DomainIntersect.intersects(uv, DomainSphere.openField(new Vec3(500,0,0), 100)), "reported touching");

        System.out.println("\nsealed from the moment it is cast");
        Vec3 outward = new Vec3(0, 0, 40);
        for (DomainPhase ph : new DomainPhase[]{DomainPhase.EXPANDING, DomainPhase.SETTLING, DomainPhase.ACTIVE}) {
            DomainSphere s2 = new DomainSphere(new Vec3(0, 0, 0), 30, -1000, ph, 0.5f);
            Vec3 from = new Vec3(0, 0, 20);
            Vec3 moved = s2.clampMovement(from, 0.3, outward, null);
            check(ph + " holds you in", from.add(moved).length() <= 30.5,
                  "ended at " + from.add(moved).length());
        }
        check("COLLAPSING is not sealed, so the shell is the way out",
              !DomainPhase.COLLAPSING.isSealed(), "still sealed");
        check("...and EXPANDING is", DomainPhase.EXPANDING.isSealed(), "not sealed");

        System.out.println("\nRippleField (the footstep ring buffer)");
        RippleField ripples = new RippleField();
        float[] packed = new float[RippleField.FLOATS];
        check("a fresh field packs to nothing live", ripples.pack(packed, 100) == 0, "live " + ripples.pack(packed, 100));
        ripples.emit(3.0, -4.0, 100, 0.5f);
        check("one ripple is live on its own tick", ripples.pack(packed, 100) == 1, "live " + ripples.liveCount(100));
        check("...packed at slot 0 as dx, dz, birth seconds, strength",
              near(packed[0], 3.0, 1e-6) && near(packed[1], -4.0, 1e-6) && near(packed[2], 5.0, 1e-6) && near(packed[3], 0.5, 1e-6),
              packed[0] + "," + packed[1] + "," + packed[2] + "," + packed[3]);
        check("a ripple born in the future packs with zero strength",
              ripples.pack(packed, 99) == 0 && packed[3] == 0.0f, "strength " + packed[3]);
        check("still live at the end of its lifetime", ripples.liveCount(100 + RippleField.LIFETIME_TICKS) == 1, "dead early");
        check("gone one tick after it", ripples.liveCount(101 + RippleField.LIFETIME_TICKS) == 0, "still live");
        ripples.prune(101 + RippleField.LIFETIME_TICKS);
        check("prune clears the slot for good", ripples.liveCount(100) == 0, "came back");

        RippleField ring = new RippleField();
        for (int i = 0; i < RippleField.CAPACITY; i++) ring.emit(i, 0, 200, 1.0f);
        check("sixteen fill the ring", ring.liveCount(200) == RippleField.CAPACITY, "live " + ring.liveCount(200));
        ring.emit(99, 0, 201, 1.0f);
        ring.pack(packed, 201);
        check("the seventeenth evicts the oldest, not the newest",
              ring.liveCount(201) == RippleField.CAPACITY && near(packed[0], 99.0, 1e-6) && near(packed[4], 1.0, 1e-6),
              "slot0 dx=" + packed[0] + " slot1 dx=" + packed[4]);
        ring.emit(0, 0, 201, 0.0f);
        check("a zero-strength emit is ignored rather than taking a slot", near(packed[4], 1.0, 1e-6) && ring.liveCount(201) == RippleField.CAPACITY, "slot taken");
        boolean threw = false;
        try { ring.pack(new float[8], 201); } catch (IllegalArgumentException e) { threw = true; }
        check("a short buffer is refused, not silently truncated", threw, "no exception");

        System.out.println("\n" + pass + " passed, " + fail + " failed");
        if (fail > 0) System.exit(1);
    }
}
