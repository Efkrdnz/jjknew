import net.efkrdnz.jjkstrongest.domain.*;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

public class GeomTest {
    // Unlimited Void's own barrier settings, transcribed from DomainDefinition. Not
    // imported from it: DomainDefinition pulls in Minecraft's registries, which is exactly
    // what this harness exists to run without.
    static final DomainShellProfile VOID_SHELL = new DomainShellProfile(60, 0.75f, 1.0f);

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

        System.out.println("\nDomainIntersect");
        DomainSphere uv = new DomainSphere(new Vec3(0,0,0), 30, -1000, DomainPhase.ACTIVE, 1f);
        DomainSphere shrineNear = DomainSphere.openField(new Vec3(20,0,0), 100);
        check("a shrine engulfing the void still counts as intersecting",
              DomainIntersect.intersects(uv, shrineNear), "reported apart");
        check("...but the lens is null there, as documented",
              DomainIntersect.intersect(uv, shrineNear) == null, "lens returned");
        check("far apart does not intersect",
              !DomainIntersect.intersects(uv, DomainSphere.openField(new Vec3(500,0,0), 100)), "reported touching");

        System.out.println("\n" + pass + " passed, " + fail + " failed");
        if (fail > 0) System.exit(1);
    }
}
