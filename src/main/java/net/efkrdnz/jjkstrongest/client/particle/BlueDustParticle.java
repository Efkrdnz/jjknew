
package net.efkrdnz.jjkstrongest.client.particle;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;

@OnlyIn(Dist.CLIENT)
public class BlueDustParticle extends TextureSheetParticle {
	public static BlueDustParticleProvider provider(SpriteSet spriteSet) {
		return new BlueDustParticleProvider(spriteSet);
	}

	public static class BlueDustParticleProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public BlueDustParticleProvider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new BlueDustParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

	private final SpriteSet spriteSet;
	private float angularVelocity;
	private float angularAcceleration;

	protected BlueDustParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
		super(world, x, y, z);
		this.spriteSet = spriteSet;
		this.setSize(0.2f, 0.2f);
		this.quadSize *= 4f;
		this.lifetime = (int) Math.max(1, 15 + (this.random.nextInt(10) - 5));
		this.gravity = 0f;
		this.hasPhysics = false;
		this.xd = vx * 1;
		this.yd = vy * 1;
		this.zd = vz * 1;
		this.angularVelocity = 0.3f;
		this.angularAcceleration = 0.01f;
		this.pickSprite(spriteSet);
	}

	/**
	 * Retunes a freshly created dust into an Unlimited Void interior mote.
	 *
	 * <p>Same particle, different job: as combat dust it lives fifteen ticks and spins
	 * hard, which inside a thirty-block sphere reads as a spark rather than something
	 * suspended in the air. A mote drifts for ten seconds and barely turns.
	 */
	public void asInteriorMote(int lifetime, float spin, float size) {
		this.lifetime = Math.max(1, lifetime);
		this.angularVelocity = spin;
		this.angularAcceleration = spin * 0.01f;
		this.quadSize = size;
	}

	@Override
	public int getLightColor(float partialTick) {
		return 15728880;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}

	@Override
	public void tick() {
		super.tick();
		this.oRoll = this.roll;
		this.roll += this.angularVelocity;
		this.angularVelocity += this.angularAcceleration;
	}
}
