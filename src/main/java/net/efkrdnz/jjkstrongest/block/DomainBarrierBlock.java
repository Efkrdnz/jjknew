
package net.efkrdnz.jjkstrongest.block;


import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;

/**
 * The old domain shell, one block at a time.
 *
 * <p>Nothing places this any more — the shell is an analytic sphere with its own
 * collision and its own shader. The block stays registered so worlds that still
 * contain it load without missing-block errors, and because it is handy for debugging
 * where a shell used to sit.
 *
 * @deprecated superseded by {@code net.efkrdnz.jjkstrongest.domain.DomainSphere}
 */
@Deprecated
public class DomainBarrierBlock extends Block {
	public DomainBarrierBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.GLASS).strength(-1, 3600000).lightLevel(s -> 15));
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 15;
	}

	@Override
	public PathType getBlockPathType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) {
		return PathType.BLOCKED;
	}
}
