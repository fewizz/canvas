/*
 *  Copyright 2019, 2020 grondag
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package grondag.canvas.varia;

import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class CanvasMath {
	private CanvasMath() { }

	/**
	 * Non-allocating substitute for {@link Vec3f#rotate(Quaternion)}.
	 */
	public static void applyRotation(Vec3f vec, Quaternion rotation) {
		final float rx = rotation.getX();
		final float ry = rotation.getY();
		final float rz = rotation.getZ();
		final float rw = rotation.getW();

		final float x = vec.getX();
		final float y = vec.getY();
		final float z = vec.getZ();

		final float qx = rw * x + ry * z - rz * y;
		final float qy = rw * y - rx * z + rz * x;
		final float qz = rw * z + rx * y - ry * x;
		final float qw = -rx * x - ry * y - rz * z;

		vec.set(
			qw * -rx + qx * rw - qy * rz + qz * ry,
			qw * -ry + qx * rz + qy * rw - qz * rx,
			qw * -rz - qx * ry + qy * rx + qz * rw);
	}

	/**
	 * Non-allocating substitute for {@link Vec3f#rotate(Quaternion)} that assumes vec.z == 0.
	 */
	public static void applyBillboardRotation(Vec3f vec, Quaternion rotation) {
		final float rx = rotation.getX();
		final float ry = rotation.getY();
		final float rz = rotation.getZ();
		final float rw = rotation.getW();

		final float x = vec.getX();
		final float y = vec.getY();

		final float qx = rw * x - rz * y;
		final float qy = rw * y + rz * x;
		final float qz = rx * y - ry * x;
		final float qw = -rx * x - ry * y;

		vec.set(
			qw * -rx + qx * rw - qy * rz + qz * ry,
			qw * -ry + qx * rz + qy * rw - qz * rx,
			qw * -rz - qx * ry + qy * rx + qz * rw);
	}

	public static void setRadialRotation(Quaternion target, Vec3f axis, float radians) {
		final float f = (float) Math.sin(radians / 2.0F);

		target.set(
			axis.getX() * f,
			axis.getY() * f,
			axis.getZ() * f,
			(float) Math.cos(radians / 2.0F));
	}
}
