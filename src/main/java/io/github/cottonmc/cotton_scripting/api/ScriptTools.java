package io.github.cottonmc.cotton_scripting.api;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class ScriptTools {
	private static final float DEGREE = (float)Math.toRadians(1);
	private static final float PI = (float)Math.PI;

	public static RayTraceResult rayTraceFromLook(ServerCommandSource source, double limit) {
		Entity entity = source.getEntity();
		if (entity == null) return RayTraceResult.ZERO;
		World world = entity.getEntityWorld();
		float pitch = entity.pitch;
		float yaw = entity.yaw;
		Vec3d cameraLook = entity.getCameraPosVec(1.0F);
		float yawCos = MathHelper.cos(-yaw * DEGREE - PI);
		float yawSin = MathHelper.sin(-yaw * DEGREE - PI);
		float pitchCos = -MathHelper.cos(-pitch * DEGREE);
		float pitchSin = MathHelper.sin(-pitch * DEGREE);
		float xOffset = yawSin * pitchCos;
		float zOffset = yawCos * pitchCos;
		Vec3d projected = cameraLook.add((double)xOffset * limit, (double)pitchSin * limit, (double)zOffset * limit);
		BlockHitResult res = world.rayTrace(new RayTraceContext(cameraLook, projected, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, entity));
		return new RayTraceResult(res.getBlockPos(), world.getBlockState(res.getBlockPos()));
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
