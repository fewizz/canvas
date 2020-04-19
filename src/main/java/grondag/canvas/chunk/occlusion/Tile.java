package grondag.canvas.chunk.occlusion;

import static grondag.canvas.chunk.occlusion.Constants.A_NEGATIVE;
import static grondag.canvas.chunk.occlusion.Constants.A_POSITIVE;
import static grondag.canvas.chunk.occlusion.Constants.B_NEGATIVE;
import static grondag.canvas.chunk.occlusion.Constants.B_POSITIVE;
import static grondag.canvas.chunk.occlusion.Constants.EDGE_BOTTOM;
import static grondag.canvas.chunk.occlusion.Constants.EDGE_BOTTOM_LEFT;
import static grondag.canvas.chunk.occlusion.Constants.EDGE_BOTTOM_RIGHT;
import static grondag.canvas.chunk.occlusion.Constants.EDGE_LEFT;
import static grondag.canvas.chunk.occlusion.Constants.EDGE_RIGHT;
import static grondag.canvas.chunk.occlusion.Constants.EDGE_TOP;
import static grondag.canvas.chunk.occlusion.Constants.EDGE_TOP_LEFT;
import static grondag.canvas.chunk.occlusion.Constants.EDGE_TOP_RIGHT;
import static grondag.canvas.chunk.occlusion.Constants.INSIDE_0;
import static grondag.canvas.chunk.occlusion.Constants.INSIDE_1;
import static grondag.canvas.chunk.occlusion.Constants.INSIDE_2;
import static grondag.canvas.chunk.occlusion.Constants.LOW_HEIGHT;
import static grondag.canvas.chunk.occlusion.Constants.LOW_WIDTH;
import static grondag.canvas.chunk.occlusion.Constants.OUTSIDE_0;
import static grondag.canvas.chunk.occlusion.Constants.OUTSIDE_1;
import static grondag.canvas.chunk.occlusion.Constants.OUTSIDE_2;
import static grondag.canvas.chunk.occlusion.Constants.POS_012_III;
import static grondag.canvas.chunk.occlusion.Constants.POS_012_IIX;
import static grondag.canvas.chunk.occlusion.Constants.POS_012_IXI;
import static grondag.canvas.chunk.occlusion.Constants.POS_012_IXX;
import static grondag.canvas.chunk.occlusion.Constants.POS_012_XII;
import static grondag.canvas.chunk.occlusion.Constants.POS_012_XIX;
import static grondag.canvas.chunk.occlusion.Constants.POS_012_XXI;
import static grondag.canvas.chunk.occlusion.Constants.POS_012_XXX;
import static grondag.canvas.chunk.occlusion.Constants.POS_INVERSE_MASK_0;
import static grondag.canvas.chunk.occlusion.Constants.POS_INVERSE_MASK_1;
import static grondag.canvas.chunk.occlusion.Constants.POS_INVERSE_MASK_2;
import static grondag.canvas.chunk.occlusion.Data.a0;
import static grondag.canvas.chunk.occlusion.Data.a1;
import static grondag.canvas.chunk.occlusion.Data.a2;
import static grondag.canvas.chunk.occlusion.Data.b0;
import static grondag.canvas.chunk.occlusion.Data.b1;
import static grondag.canvas.chunk.occlusion.Data.b2;
import static grondag.canvas.chunk.occlusion.Data.event0;
import static grondag.canvas.chunk.occlusion.Data.event1;
import static grondag.canvas.chunk.occlusion.Data.event2;
import static grondag.canvas.chunk.occlusion.Data.lowCornerW0;
import static grondag.canvas.chunk.occlusion.Data.lowCornerW1;
import static grondag.canvas.chunk.occlusion.Data.lowCornerW2;
import static grondag.canvas.chunk.occlusion.Data.lowExtent0;
import static grondag.canvas.chunk.occlusion.Data.lowExtent1;
import static grondag.canvas.chunk.occlusion.Data.lowExtent2;
import static grondag.canvas.chunk.occlusion.Data.lowSpanB0;
import static grondag.canvas.chunk.occlusion.Data.lowSpanB1;
import static grondag.canvas.chunk.occlusion.Data.lowSpanB2;
import static grondag.canvas.chunk.occlusion.Data.lowTileA0;
import static grondag.canvas.chunk.occlusion.Data.lowTileA1;
import static grondag.canvas.chunk.occlusion.Data.lowTileA2;
import static grondag.canvas.chunk.occlusion.Data.lowTileX;
import static grondag.canvas.chunk.occlusion.Data.lowTileY;
import static grondag.canvas.chunk.occlusion.Data.maxPixelX;
import static grondag.canvas.chunk.occlusion.Data.maxPixelY;
import static grondag.canvas.chunk.occlusion.Data.minPixelX;
import static grondag.canvas.chunk.occlusion.Data.minPixelY;
import static grondag.canvas.chunk.occlusion.Data.position0;
import static grondag.canvas.chunk.occlusion.Data.position1;
import static grondag.canvas.chunk.occlusion.Data.position2;
import static grondag.canvas.chunk.occlusion.Data.positionLow;
import static grondag.canvas.chunk.occlusion.Data.save_lowCornerW0;
import static grondag.canvas.chunk.occlusion.Data.save_lowCornerW1;
import static grondag.canvas.chunk.occlusion.Data.save_lowCornerW2;
import static grondag.canvas.chunk.occlusion.Data.save_lowTileX;
import static grondag.canvas.chunk.occlusion.Data.save_lowTileY;
import static grondag.canvas.chunk.occlusion.Data.save_positionLow;
import static grondag.canvas.chunk.occlusion.Rasterizer.printMask8x8;

abstract class Tile {
	private Tile() {}

	static void moveLowTileRight() {
		++lowTileX;

		assert lowTileX < LOW_WIDTH;

		lowCornerW0 += lowTileA0;
		lowCornerW1 += lowTileA1;
		lowCornerW2 += lowTileA2;

		int pos = positionLow;

		if (((pos & OUTSIDE_0) == 0 && (position0 & A_NEGATIVE) != 0) || ((pos & INSIDE_0) == 0 && (position0 & A_POSITIVE) != 0)) {
			pos &= POS_INVERSE_MASK_0;
			if (lowCornerW0 < 0) pos |= OUTSIDE_0; else if (lowCornerW0 >= lowExtent0) pos |= INSIDE_0;
		}

		if (((pos & OUTSIDE_1) == 0 && (position1 & A_NEGATIVE) != 0) || ((pos & INSIDE_1) == 0 && (position1 & A_POSITIVE) != 0)) {
			pos &= POS_INVERSE_MASK_1;
			if (lowCornerW1 < 0) pos |= OUTSIDE_1; else if (lowCornerW1 >= lowExtent1) pos |= INSIDE_1;
		}

		if (((pos & OUTSIDE_2) == 0 && (position2 & A_NEGATIVE) != 0) || ((pos & INSIDE_2) == 0 && (position2 & A_POSITIVE) != 0)) {
			pos &= POS_INVERSE_MASK_2;
			if (lowCornerW2 < 0) pos |= OUTSIDE_2; else if (lowCornerW2 >= lowExtent2) pos |= INSIDE_2;
		}

		positionLow = pos;
	}

	static void moveLowTileLeft() {
		--lowTileX;

		assert lowTileX >= 0;

		lowCornerW0 -= lowTileA0;
		lowCornerW1 -= lowTileA1;
		lowCornerW2 -= lowTileA2;

		int pos = positionLow;

		if (((pos & OUTSIDE_0) == 0 && (position0 & A_POSITIVE) != 0) || ((pos & INSIDE_0) == 0 && (position0 & A_NEGATIVE) != 0)) {
			pos &= POS_INVERSE_MASK_0;
			if (lowCornerW0 < 0) pos |= OUTSIDE_0; else if (lowCornerW0 >= lowExtent0) pos |= INSIDE_0;
		}

		if (((pos & OUTSIDE_1) == 0 && (position1 & A_POSITIVE) != 0) || ((pos & INSIDE_1) == 0 && (position1 & A_NEGATIVE) != 0)) {
			pos &= POS_INVERSE_MASK_1;
			if (lowCornerW1 < 0) pos |= OUTSIDE_1; else if (lowCornerW1 >= lowExtent1) pos |= INSIDE_1;
		}

		if (((pos & OUTSIDE_2) == 0 && (position2 & A_POSITIVE) != 0) || ((pos & INSIDE_2) == 0 && (position2 & A_NEGATIVE) != 0)) {
			pos &= POS_INVERSE_MASK_2;
			if (lowCornerW2 < 0) pos |= OUTSIDE_2; else if (lowCornerW2 >= lowExtent2) pos |= INSIDE_2;
		}

		positionLow  = pos;
	}

	static void moveLowTileUp() {
		++lowTileY;

		assert lowTileY < LOW_HEIGHT;

		lowCornerW0 += (b0 + lowSpanB0);
		lowCornerW1 += (b1 + lowSpanB1);
		lowCornerW2 += (b2 + lowSpanB2);

		int pos = positionLow;

		if (((pos & OUTSIDE_0) == 0 && (position0 & B_NEGATIVE) != 0) || ((pos & INSIDE_0) == 0 && (position0 & B_POSITIVE) != 0)) {
			pos &= POS_INVERSE_MASK_0;
			if (lowCornerW0 < 0) pos |= OUTSIDE_0; else if (lowCornerW0 >= lowExtent0) pos |= INSIDE_0;
		}

		if (((pos & OUTSIDE_1) == 0 && (position1 & B_NEGATIVE) != 0) || ((pos & INSIDE_1) == 0 && (position1 & B_POSITIVE) != 0)) {
			pos &= POS_INVERSE_MASK_1;
			if (lowCornerW1 < 0) pos |= OUTSIDE_1; else if (lowCornerW1 >= lowExtent1) pos |= INSIDE_1;
		}

		if (((pos & OUTSIDE_2) == 0 && (position2 & B_NEGATIVE) != 0) || ((pos & INSIDE_2) == 0 && (position2 & B_POSITIVE) != 0)) {
			pos &= POS_INVERSE_MASK_2;
			if (lowCornerW2 < 0) pos |= OUTSIDE_2; else if (lowCornerW2 >= lowExtent2) pos |= INSIDE_2;
		}

		positionLow = pos;
	}

	static long computeLowTileCoverage() {
		switch(positionLow)  {

		default:
			// most cases have at least one outside edge
			return 0L;

		case POS_012_III:
			// all inside
			return -1L;

		case POS_012_XII:
			return buildLowMask(position0, a0, b0, lowCornerW0, event0);

		case POS_012_IXI:
			return buildLowMask(position1, a1, b1, lowCornerW1, event1);

		case POS_012_IIX:
			return buildLowMask(position2, a2, b2, lowCornerW2, event2);

		case POS_012_XIX:
			return buildLowMask(position0, a0, b0, lowCornerW0, event0)
					& buildLowMask(position2, a2, b2, lowCornerW2, event2);

		case POS_012_XXI:
			return buildLowMask(position0, a0, b0, lowCornerW0, event0)
					& buildLowMask(position1, a1, b1, lowCornerW1, event1);

		case POS_012_IXX:
			return buildLowMask(position1, a1, b1, lowCornerW1, event1)
					& buildLowMask(position2, a2, b2, lowCornerW2, event2);

		case POS_012_XXX:
			return buildLowMask(position0, a0, b0, lowCornerW0, event0)
					& buildLowMask(position1, a1, b1, lowCornerW1, event1)
					& buildLowMask(position2, a2, b2, lowCornerW2, event2);
		}
	}


	static void pushLowTile() {
		save_lowTileX = lowTileX;
		save_lowTileY = lowTileY;
		save_lowCornerW0 = lowCornerW0;
		save_lowCornerW1 = lowCornerW1;
		save_lowCornerW2 = lowCornerW2;
		save_positionLow = positionLow;
	}

	static void popLowTile() {
		lowTileX = save_lowTileX;
		lowTileY = save_lowTileY;
		lowCornerW0 = save_lowCornerW0;
		lowCornerW1 = save_lowCornerW1;
		lowCornerW2 = save_lowCornerW2;
		positionLow = save_positionLow;

		assert lowTileX < LOW_WIDTH;
		assert lowTileY < LOW_HEIGHT;
	}


	static long buildLowMaskTest(int pos, int stepA, int stepB, int wy, int[] event) {
		final long  oldResult = buildLowMaskOld(pos, stepA, stepB, wy, event);
		final long  newResult = buildLowMask(pos, stepA, stepB, wy, event);

		if (oldResult != newResult) {
			System.out.println();
			System.out.println("OLD - POSITION = " + pos);
			printMask8x8(oldResult);
			System.out.println("NEW");
			printMask8x8(newResult);
			buildLowMask(pos, stepA, stepB, wy, event);
		}

		return oldResult;
	}

	static long buildLowMaskOld(int pos, int stepA, int stepB, int wy, int[] event) {
		final int ty = Data.lowTileY << 3;

		if (ty > maxPixelY || ty + 7 < minPixelY) {
			return 0L;
		}

		final int tx = Data.lowTileX << 3;

		if (tx > maxPixelX || tx + 7 < minPixelX) {
			return 0L;
		}

		switch  (pos) {
		case EDGE_TOP: {
			assert wy >= 0;
			assert stepB < 0;

			long yMask = 0xFFL;
			long mask = 0;

			while (wy >= 0 && yMask != 0L) {
				mask |= yMask;
				yMask <<= 8;
				wy += stepB; //NB: b will be negative
			}

			return mask;
		}

		case EDGE_BOTTOM: {
			assert wy >= 0;
			assert stepB > 0;

			long yMask = 0xFF00000000000000L;
			long mask = 0;

			while (wy >= 0 && yMask != 0L) {
				mask |= yMask;
				yMask = (yMask >>> 8); // parens are to help eclipse auto-formatting
				wy -= stepB;
			}

			return mask;
		}

		case EDGE_RIGHT: {
			assert wy >= 0;
			assert stepA < 0;

			final int x = 7 - Math.min(7, -wy / stepA);
			long mask = (0xFF >> x);

			mask |= mask << 8;
			mask |= mask << 16;
			mask |= mask << 32;

			return mask;
		}

		case EDGE_LEFT: {
			assert wy >= 0;
			assert stepA > 0;

			final int x =  7 - Math.min(7, wy / stepA);
			long mask = (0xFF << x) & 0xFF;

			mask |= mask << 8;
			mask |= mask << 16;
			mask |= mask << 32;

			return mask;
		}

		case EDGE_TOP_LEFT: {
			// PERF: optimize case when shallow slope and several bottom rows are full

			assert wy >= 0;
			assert stepB < 0;
			assert stepA > 0;

			long mask = 0;
			int yShift = 0;


			while (yShift < 64 && wy >= 0) {
				// x  here is first not last
				final int x =  7 - Math.min(7, wy / stepA);
				final int yMask = (0xFF << x) & 0xFF;
				mask |= ((long) yMask) << yShift;
				wy += stepB; //NB: b will be negative
				yShift += 8;
			}

			return mask;
		}

		case EDGE_BOTTOM_LEFT: {
			assert wy >= 0;
			assert stepB > 0;
			assert stepA > 0;

			// min y will occur at x = 7;

			int yShift = 8 * 7;
			long mask = 0;

			while (yShift >= 0 && wy >= 0) {
				// x  here is first not last
				final int x =  7 - Math.min(7, wy / stepA);
				final int yMask = (0xFF << x) & 0xFF;
				mask |= ((long) yMask) << yShift;
				wy -= stepB;
				yShift -= 8;
			}

			return mask;
		}

		case EDGE_TOP_RIGHT: {
			// PERF: optimize case when shallow slope and several bottom rows are full

			// max y will occur at x = 0
			// Find highest y index of pixels filled at given x.
			// All pixels with lower y value will also be filled in given x.
			// ax + by + c = 0 so y at intersection will be y = -(ax + c) / b
			// Exploit step-wise nature of a/b here to avoid computing the first term
			// logic in other cases is similar
			assert wy >= 0;
			assert stepB < 0;
			assert stepA < 0;

			long mask = 0;
			int yShift = 0;

			while(yShift < 64 && wy >= 0) {
				final int x =  7  - Math.min(7, -wy / stepA);
				final int yMask = (0xFF >> x);
				mask |= ((long) yMask) << yShift;
				wy += stepB;
				yShift +=  8;
			}

			return mask;
		}

		case EDGE_BOTTOM_RIGHT: {
			// PERF: optimize case when shallow slope and several top rows are full

			assert wy >= 0;
			assert stepB > 0;
			assert stepA < 0;

			int yShift = 8 * 7;
			long mask = 0;

			while (yShift >= 0 && wy >= 0) {
				final int x = 7 - Math.min(7, -wy / stepA);
				final int yMask = (0xFF >> x);
				mask |= ((long) yMask) << yShift;
				wy -= stepB;
				yShift -= 8;
			}

			return mask;
		}

		default:
			assert false : "Edge flag out of bounds.";
		return 0L;
		}
	}

	static long buildLowMask(int pos, int stepA, int stepB, int wy, int[] event) {
		// PERF: check shouldn't be needed - shouldn't be called in this case
		int ty = Data.lowTileY << 3;

		if (ty > maxPixelY || ty + 7 < minPixelY) {
			return 0L;
		}

		final int tx = Data.lowTileX << 3;

		if (tx > maxPixelX || tx + 7 < minPixelX) {
			return 0L;
		}

		switch  (pos) {
		case EDGE_TOP: {
			assert wy >= 0;
			assert stepB < 0;

			long yMask = 0xFFL;
			long mask = 0;

			while (wy >= 0 && yMask != 0L) {
				mask |= yMask;
				yMask <<= 8;
				wy += stepB; //NB: b will be negative
			}

			return mask;
		}

		case EDGE_BOTTOM: {
			assert wy >= 0;
			assert stepB > 0;

			long yMask = 0xFF00000000000000L;
			long mask = 0;

			while (wy >= 0 && yMask != 0L) {
				mask |= yMask;
				yMask = (yMask >>> 8); // parens are to help eclipse auto-formatting
				wy -= stepB;
			}

			return mask;
		}

		case EDGE_RIGHT: {
			assert wy >= 0;
			assert stepA < 0;

			final int x = 7 - Math.min(7, -wy / stepA);
			long mask = (0xFF >> x);

			mask |= mask << 8;
			mask |= mask << 16;
			mask |= mask << 32;

			return mask;
		}

		case EDGE_LEFT: {
			assert wy >= 0;
			assert stepA > 0;

			final int x =  7 - Math.min(7, wy / stepA);
			long mask = (0xFF << x) & 0xFF;

			mask |= mask << 8;
			mask |= mask << 16;
			mask |= mask << 32;

			return mask;
		}

		case EDGE_TOP_LEFT: {
			assert stepB < 0;
			assert stepA > 0;

			long mask = 0;
			int yShift = 0;

			while (yShift < 64) {
				final int x = event[ty++] - tx;

				if(x > 7) return mask;

				if (x <= 0) {
					mask |= 0xFFL << yShift;
				} else {
					mask |= ((long) ((0xFF << x) & 0xFF)) << yShift;
				}

				yShift += 8;
			}

			return mask;
		}

		case EDGE_BOTTOM_LEFT: {
			assert stepB > 0;
			assert stepA > 0;

			int yShift = 56;
			long mask = 0;
			ty += 7;

			while (yShift >= 0) {
				final int x = event[ty--] - tx;

				if(x > 7) return mask;

				if (x <= 0) {
					mask |= 0xFFL << yShift;
				} else {
					mask |= ((long) ((0xFF << x) & 0xFF)) << yShift;
				}

				yShift -= 8;
			}

			return mask;
		}

		case EDGE_TOP_RIGHT: {
			assert stepB < 0;
			assert stepA < 0;

			long mask = 0;
			int yShift = 0;

			while(yShift < 64 && wy >= 0) {
				final int x = event[ty++] - tx;

				if(x < 0) return mask;

				if (x >= 7) {
					mask |= 0xFFL << yShift;
				} else {
					mask |= ((long) (0xFF >> (7 - x))) << yShift;
				}

				yShift += 8;
			}

			return mask;
		}

		case EDGE_BOTTOM_RIGHT: {
			assert stepB > 0;
			assert stepA < 0;

			int yShift = 56;
			long mask = 0;
			ty += 7;

			while (yShift >= 0) {
				final int x = event[ty--] - tx;

				if(x < 0) return mask;

				if (x >= 7) {
					mask |= 0xFFL << yShift;
				} else {
					mask |= ((long) (0xFF >> (7 - x))) << yShift;
				}

				yShift -= 8;
			}

			return mask;
		}

		default:
			assert false : "Edge flag out of bounds.";
		return 0L;
		}
	}
}