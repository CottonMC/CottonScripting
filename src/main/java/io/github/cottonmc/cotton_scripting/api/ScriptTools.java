package io.github.cottonmc.cotton_scripting.api;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ScriptTools {
	public static RayTraceResult rayTraceFromLook(ServerCommandSource source, int limit) {
		Entity entity = source.getEntity();
		if (entity == null) return RayTraceResult.ZERO;
		World world = entity.getEntityWorld();
		Vec3d cameraPos = entity.getCameraPosVec(1f);
//		Vec3d rotation = getHeadRotationVec(entity.getPitch(1f), entity.getHeadYaw());
		Vec3d rotation = entity.getRotationVec(1f);
		double lerpX = MathHelper.lerp(-1.0E-7D, rotation.x, cameraPos.x);
		double lerpY = MathHelper.lerp(-1.0E-7D, rotation.y, cameraPos.y);
		double lerpZ = MathHelper.lerp(-1.0E-7D, rotation.z, cameraPos.z);
		double opLerpX = MathHelper.lerp(-1.0E-7D, cameraPos.x, rotation.x);
		double opLerpY = MathHelper.lerp(-1.0E-7D, cameraPos.y, rotation.y);
		double opLerpZ = MathHelper.lerp(-1.0E-7D, cameraPos.z, rotation.z);
		int x = MathHelper.floor(opLerpX);
		int y = MathHelper.floor(opLerpY);
		int z = MathHelper.floor(opLerpZ);

		double diffX = lerpX - opLerpX;
		double diffY = lerpY - opLerpY;
		double diffZ = lerpZ - opLerpZ;
		int signX = MathHelper.sign(diffX);
		int signY = MathHelper.sign(diffY);
		int signZ = MathHelper.sign(diffZ);
		double opDiffX = signX == 0 ? 1.7976931348623157E308D : (double)signX / diffX;
		double opDiffY = signY == 0 ? 1.7976931348623157E308D : (double)signY / diffY;
		double opDiffZ = signZ == 0 ? 1.7976931348623157E308D : (double)signZ / diffZ;
		double travelX = opDiffX * (signX > 0 ? 1.0D - MathHelper.fractionalPart(opLerpX) : MathHelper.fractionalPart(opLerpX));
		double travelY = opDiffY * (signY > 0 ? 1.0D - MathHelper.fractionalPart(opLerpY) : MathHelper.fractionalPart(opLerpY));
		double travelZ = opDiffZ * (signZ > 0 ? 1.0D - MathHelper.fractionalPart(opLerpZ) : MathHelper.fractionalPart(opLerpZ));
		BlockPos tracedPos = new BlockPos(x, y, z);
		BlockState tracedState = world.getBlockState(tracedPos);
		int travel = 0;
		do {
			if (travelX > 1.0D && travelY > 1.0D && travelZ > 1.0D) {
				return new RayTraceResult(tracedPos, tracedState);
			}

			if (travelX < travelY) {
				if (travelX < travelZ) {
					x += signX;
					travelX += opDiffX;
				} else {
					z += signZ;
					travelZ += opDiffZ;
				}
			} else if (travelY < travelZ) {
				y += signY;
				travelY += opDiffY;
			} else {
				z += signZ;
				travelZ += opDiffZ;
			}

			tracedPos = new BlockPos(x, y, z);
			tracedState = world.getBlockState(tracedPos);
			System.out.println(tracedPos.toString() + tracedState.toString());
			travel++;
		} while(tracedState.isAir() && travel < limit);
		return new RayTraceResult(tracedPos, tracedState);
	}

	static Vec3d getHeadRotationVec(float pitch, float yaw) {
		float adjPitch = pitch * 0.017453292F;
		float adjYaw = -yaw * 0.017453292F;
		float yawCos = MathHelper.cos(adjYaw);
		float yawSin = MathHelper.sin(adjYaw);
		float pitchCos = MathHelper.cos(adjPitch);
		float pitchSin = MathHelper.sin(adjPitch);
		return new Vec3d((double)(yawSin * pitchCos), (double)(-pitchSin), (double)(yawCos * pitchCos));
	}
}
