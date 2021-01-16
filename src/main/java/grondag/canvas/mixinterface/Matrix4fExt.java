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

package grondag.canvas.mixinterface;

import java.nio.FloatBuffer;

import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface Matrix4fExt {
	float a00();

	float a01();

	float a02();

	float a03();

	float a10();

	float a11();

	float a12();

	float a13();

	float a20();

	float a21();

	float a22();

	float a23();

	float a30();

	float a31();

	float a32();

	float a33();

	void a00(float val);

	void a01(float val);

	void a02(float val);

	void a03(float val);

	void a10(float val);

	void a11(float val);

	void a12(float val);

	void a13(float val);

	void a20(float val);

	void a21(float val);

	void a22(float val);

	void a23(float val);

	void a30(float val);

	void a31(float val);

	void a32(float val);

	void a33(float val);

	default void multiply(Matrix4fExt val) {
		((Matrix4f) (Object) this).multiply((Matrix4f) (Object) val);
	}

	default void loadIdentity() {
		((Matrix4f) (Object) this).loadIdentity();
	}

	default void set(Matrix4fExt val) {
		a00(val.a00());
		a01(val.a01());
		a02(val.a02());
		a03(val.a03());

		a10(val.a10());
		a11(val.a11());
		a12(val.a12());
		a13(val.a13());

		a20(val.a20());
		a21(val.a21());
		a22(val.a22());
		a23(val.a23());

		a30(val.a30());
		a31(val.a31());
		a32(val.a32());
		a33(val.a33());
	}

	default void set(Matrix4f val) {
		set((Matrix4fExt) (Object) val);
	}

	default boolean matches(Matrix4fExt val) {
		return a00() == val.a00()
			&& a01() == val.a01()
			&& a02() == val.a02()
			&& a03() == val.a03()

			&& a10() == val.a10()
			&& a11() == val.a11()
			&& a12() == val.a12()
			&& a13() == val.a13()

			&& a20() == val.a20()
			&& a21() == val.a21()
			&& a22() == val.a22()
			&& a23() == val.a23()

			&& a30() == val.a30()
			&& a31() == val.a31()
			&& a32() == val.a32()
			&& a33() == val.a33();
	}

	default boolean matches(Matrix4f val) {
		return matches((Matrix4fExt) (Object) val);
	}

	default void fastTransform(Vec3f vec) {
		final float x = vec.getX();
		final float y = vec.getY();
		final float z = vec.getZ();

		vec.set(
			a00() * x + a01() * y + a02() * z + a03(),
			a10() * x + a11() * y + a12() * z + a13(),
			a20() * x + a21() * y + a22() * z + a23());
	}

	default void translate(float x, float y, float z) {
		final float b03 = a00() * x + a01() * y + a02() * z + a03();
		final float b13 = a10() * x + a11() * y + a12() * z + a13();
		final float b23 = a20() * x + a21() * y + a22() * z + a23();
		final float b33 = a30() * x + a31() * y + a32() * z + a33();

		a03(b03);
		a13(b13);
		a23(b23);
		a33(b33);
	}

	// FIX: doesn't seem to work reliably - remove or fix
	default void invertProjection () {
		final float inv00 = 1.0f / a00();
		final float inv11 = 1.0f / a11();
		final float inv23 = 1.0f / a23();
		final float inv32 = 1.0f / a32();

		final float m20 = a20();
		final float m21 = a21();
		final float m22 = a22();

		a00(inv00);
		a01(0);
		a02(0);
		a03(0);

		a10(0);
		a11(inv11);
		a12(0);
		a13(0);

		a20(0);
		a21(0);
		a22(inv32);
		a23(0);

		a30(-m20 * inv00 * inv23);
		a31(-m21 * inv11 * inv23);
		a32(inv23);
		a33(-m22 * inv23 * inv32);
	}

	default void scale(float x, float y, float z) {
		final float b00 = a00() * x;
		final float b01 = a01() * y;
		final float b02 = a02() * z;
		final float b10 = a10() * x;
		final float b11 = a11() * y;
		final float b12 = a12() * z;
		final float b20 = a20() * x;
		final float b21 = a21() * y;
		final float b22 = a22() * z;
		final float b30 = a30() * x;
		final float b31 = a31() * y;
		final float b32 = a32() * z;

		a00(b00);
		a01(b01);
		a02(b02);
		a10(b10);
		a11(b11);
		a12(b12);
		a20(b20);
		a21(b21);
		a22(b22);
		a30(b30);
		a31(b31);
		a32(b32);
	}

	void writeToBuffer(int baseIndex, FloatBuffer floatBuffer);

	default void setOrtho(float left, float right, float bottom, float top, float near, float far) {
		loadIdentity();
		a00(2.0f / (right - left));
		a11(2.0f / (top - bottom));
		a22(2.0f / (near - far));
		a03((right + left) / (left - right));
		a13((top + bottom) / (bottom - top));
		a23((far + near) / (near - far));
	}

	default void lookAt(
		float fromX, float fromY, float fromZ,
		float toX, float toY, float toZ,
		float basisX, float basisY, float basisZ
	) {
		float viewX, viewY, viewZ;
		viewX = fromX - toX;
		viewY = fromY - toY;
		viewZ = fromZ - toZ;

		final float inverseViewLength = 1.0f / (float) Math.sqrt(viewX * viewX + viewY * viewY + viewZ * viewZ);
		viewX *= inverseViewLength;
		viewY *= inverseViewLength;
		viewZ *= inverseViewLength;

		float aX, aY, aZ;
		aX = basisY * viewZ - basisZ * viewY;
		aY = basisZ * viewX - basisX * viewZ;
		aZ = basisX * viewY - basisY * viewX;

		final float inverseLengthA = 1.0f / (float) Math.sqrt(aX * aX + aY * aY + aZ * aZ);
		aX *= inverseLengthA;
		aY *= inverseLengthA;
		aZ *= inverseLengthA;

		final float bX = viewY * aZ - viewZ * aY;
		final float bY = viewZ * aX - viewX * aZ;
		final float bZ = viewX * aY - viewY * aX;

		a00(aX);
		a10(bX);
		a20(viewX);
		a30(0.0f);
		a01(aY);
		a11(bY);
		a21(viewY);
		a21(0.0f);
		a02(aZ);
		a12(bZ);
		a22(viewZ);
		a32(0.0f);
		a03(-(aX * fromX + aY * fromY + aZ * fromZ));
		a13(-(bX * fromX + bY * fromY + bZ * fromZ));
		a23(-(viewX * fromX + viewY * fromY + viewZ * fromZ));
		a33(1.0f);
	}
}
