/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.utilities;

import java.io.IOException;

/**
 * Utility Class used to convert numbers in byte array containing representation
 * of each digit as byte. This will increase write performances.
 * 
 * 
 * This code is an adaption/improvement over the code found at the following
 * address http://etutorials.org/Programming/Java+performance+tuning/Chapter+5.+
 * Strings/5.3+Conversions+to+Strings/.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class NumberToByteArray {
	// Hardcode some byte arrays to make them quickly available
	public static final byte[] INFINITY = { 73, 110, 102, 105, 110, 105, 116,
			121 };

	public static final byte[] NaN = { 78, 97, 78 };

	public static final byte[] NEGATIVE_INFINITY = { 45, 73, 110, 102, 105,110, 105, 116, 121 };

	public static final byte[] MIN_VALUE = { 45, 50, 49, 52, 55, 52, 56, 51,54, 52, 56 };

	public static final byte[] POSITIVE_INFINITY = { 73, 110, 102, 105, 110,105, 116, 121 };

	public static final byte[] DOUBLE_ZERO = { 48, 46, 48 };

	public static final byte[] MINUS_DOUBLE_ZERO = { 45, 48, 46, 48 };

	public static final byte[] DOUBLE_ZERO2 = { 48, 46, 48, 48 };

	public static final byte[] DOUBLE_ZERO0 = { 48, 46 };

	public static final byte[] DOT_ZERO = { 46, 48 };

	/**
	 * Array containing byte arrays containing "zeros" The element at index N
	 * within this array contains a byte array of N zero. <br>
	 * As you can notice, at index 0 there is an empty array, while at index 1
	 * there is a byte array containing representation for a single zero, at
	 * index 2 there is a byte array containing representation for 2 zeros and
	 * so on.
	 */
	public static final byte[][] ZEROS = {
			{},
			{ 48 },
			{ 48, 48 },
			{ 48, 48, 48 },
			{ 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 },
			{ 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48 }, };

	/** byte representation of each digit, starting from "0" to "Z" */
	private static final byte[] byteForDigit = { 48, 49, 50, 51, 52, 53, 54,
			55, 56, 57, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107,
			108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120,
			121, 122 };

	// And required double related constants.
	private static final long DoubleSignMask = 0x8000000000000000L;

	private static final long DoubleExpMask = 0x7ff0000000000000L;

	private static final int DoubleExpShift = 52;

	private static final int DoubleExpBias = 1023;

	/**
	 * double array containing powers of 10.
	 */
	private static final double[] d_tenthPowers = { 1e-323D, 1e-322D, 1e-321D,
			1e-320D, 1e-319D, 1e-318D, 1e-317D, 1e-316D, 1e-315D, 1e-314D,
			1e-313D, 1e-312D, 1e-311D, 1e-310D, 1e-309D, 1e-308D, 1e-307D,
			1e-306D, 1e-305D, 1e-304D, 1e-303D, 1e-302D, 1e-301D, 1e-300D,
			1e-299D, 1e-298D, 1e-297D, 1e-296D, 1e-295D, 1e-294D, 1e-293D,
			1e-292D, 1e-291D, 1e-290D, 1e-289D, 1e-288D, 1e-287D, 1e-286D,
			1e-285D, 1e-284D, 1e-283D, 1e-282D, 1e-281D, 1e-280D, 1e-279D,
			1e-278D, 1e-277D, 1e-276D, 1e-275D, 1e-274D, 1e-273D, 1e-272D,
			1e-271D, 1e-270D, 1e-269D, 1e-268D, 1e-267D, 1e-266D, 1e-265D,
			1e-264D, 1e-263D, 1e-262D, 1e-261D, 1e-260D, 1e-259D, 1e-258D,
			1e-257D, 1e-256D, 1e-255D, 1e-254D, 1e-253D, 1e-252D, 1e-251D,
			1e-250D, 1e-249D, 1e-248D, 1e-247D, 1e-246D, 1e-245D, 1e-244D,
			1e-243D, 1e-242D, 1e-241D, 1e-240D, 1e-239D, 1e-238D, 1e-237D,
			1e-236D, 1e-235D, 1e-234D, 1e-233D, 1e-232D, 1e-231D, 1e-230D,
			1e-229D, 1e-228D, 1e-227D, 1e-226D, 1e-225D, 1e-224D, 1e-223D,
			1e-222D, 1e-221D, 1e-220D, 1e-219D, 1e-218D, 1e-217D, 1e-216D,
			1e-215D, 1e-214D, 1e-213D, 1e-212D, 1e-211D, 1e-210D, 1e-209D,
			1e-208D, 1e-207D, 1e-206D, 1e-205D, 1e-204D, 1e-203D, 1e-202D,
			1e-201D, 1e-200D, 1e-199D, 1e-198D, 1e-197D, 1e-196D, 1e-195D,
			1e-194D, 1e-193D, 1e-192D, 1e-191D, 1e-190D, 1e-189D, 1e-188D,
			1e-187D, 1e-186D, 1e-185D, 1e-184D, 1e-183D, 1e-182D, 1e-181D,
			1e-180D, 1e-179D, 1e-178D, 1e-177D, 1e-176D, 1e-175D, 1e-174D,
			1e-173D, 1e-172D, 1e-171D, 1e-170D, 1e-169D, 1e-168D, 1e-167D,
			1e-166D, 1e-165D, 1e-164D, 1e-163D, 1e-162D, 1e-161D, 1e-160D,
			1e-159D, 1e-158D, 1e-157D, 1e-156D, 1e-155D, 1e-154D, 1e-153D,
			1e-152D, 1e-151D, 1e-150D, 1e-149D, 1e-148D, 1e-147D, 1e-146D,
			1e-145D, 1e-144D, 1e-143D, 1e-142D, 1e-141D, 1e-140D, 1e-139D,
			1e-138D, 1e-137D, 1e-136D, 1e-135D, 1e-134D, 1e-133D, 1e-132D,
			1e-131D, 1e-130D, 1e-129D, 1e-128D, 1e-127D, 1e-126D, 1e-125D,
			1e-124D, 1e-123D, 1e-122D, 1e-121D, 1e-120D, 1e-119D, 1e-118D,
			1e-117D, 1e-116D, 1e-115D, 1e-114D, 1e-113D, 1e-112D, 1e-111D,
			1e-110D, 1e-109D, 1e-108D, 1e-107D, 1e-106D, 1e-105D, 1e-104D,
			1e-103D, 1e-102D, 1e-101D, 1e-100D, 1e-99D, 1e-98D, 1e-97D, 1e-96D,
			1e-95D, 1e-94D, 1e-93D, 1e-92D, 1e-91D, 1e-90D, 1e-89D, 1e-88D,
			1e-87D, 1e-86D, 1e-85D, 1e-84D, 1e-83D, 1e-82D, 1e-81D, 1e-80D,
			1e-79D, 1e-78D, 1e-77D, 1e-76D, 1e-75D, 1e-74D, 1e-73D, 1e-72D,
			1e-71D, 1e-70D, 1e-69D, 1e-68D, 1e-67D, 1e-66D, 1e-65D, 1e-64D,
			1e-63D, 1e-62D, 1e-61D, 1e-60D, 1e-59D, 1e-58D, 1e-57D, 1e-56D,
			1e-55D, 1e-54D, 1e-53D, 1e-52D, 1e-51D, 1e-50D, 1e-49D, 1e-48D,
			1e-47D, 1e-46D, 1e-45D, 1e-44D, 1e-43D, 1e-42D, 1e-41D, 1e-40D,
			1e-39D, 1e-38D, 1e-37D, 1e-36D, 1e-35D, 1e-34D, 1e-33D, 1e-32D,
			1e-31D, 1e-30D, 1e-29D, 1e-28D, 1e-27D, 1e-26D, 1e-25D, 1e-24D,
			1e-23D, 1e-22D, 1e-21D, 1e-20D, 1e-19D, 1e-18D, 1e-17D, 1e-16D,
			1e-15D, 1e-14D, 1e-13D, 1e-12D, 1e-11D, 1e-10D, 1e-9D, 1e-8D,
			1e-7D, 1e-6D, 1e-5D, 1e-4D, 1e-3D, 1e-2D, 1e-1D, 1e0D, 1e1D, 1e2D,
			1e3D, 1e4D, 1e5D, 1e6D, 1e7D, 1e8D, 1e9D, 1e10D, 1e11D, 1e12D,
			1e13D, 1e14D, 1e15D, 1e16D, 1e17D, 1e18D, 1e19D, 1e20D, 1e21D,
			1e22D, 1e23D, 1e24D, 1e25D, 1e26D, 1e27D, 1e28D, 1e29D, 1e30D,
			1e31D, 1e32D, 1e33D, 1e34D, 1e35D, 1e36D, 1e37D, 1e38D, 1e39D,
			1e40D, 1e41D, 1e42D, 1e43D, 1e44D, 1e45D, 1e46D, 1e47D, 1e48D,
			1e49D, 1e50D, 1e51D, 1e52D, 1e53D, 1e54D, 1e55D, 1e56D, 1e57D,
			1e58D, 1e59D, 1e60D, 1e61D, 1e62D, 1e63D, 1e64D, 1e65D, 1e66D,
			1e67D, 1e68D, 1e69D, 1e70D, 1e71D, 1e72D, 1e73D, 1e74D, 1e75D,
			1e76D, 1e77D, 1e78D, 1e79D, 1e80D, 1e81D, 1e82D, 1e83D, 1e84D,
			1e85D, 1e86D, 1e87D, 1e88D, 1e89D, 1e90D, 1e91D, 1e92D, 1e93D,
			1e94D, 1e95D, 1e96D, 1e97D, 1e98D, 1e99D, 1e100D, 1e101D, 1e102D,
			1e103D, 1e104D, 1e105D, 1e106D, 1e107D, 1e108D, 1e109D, 1e110D,
			1e111D, 1e112D, 1e113D, 1e114D, 1e115D, 1e116D, 1e117D, 1e118D,
			1e119D, 1e120D, 1e121D, 1e122D, 1e123D, 1e124D, 1e125D, 1e126D,
			1e127D, 1e128D, 1e129D, 1e130D, 1e131D, 1e132D, 1e133D, 1e134D,
			1e135D, 1e136D, 1e137D, 1e138D, 1e139D, 1e140D, 1e141D, 1e142D,
			1e143D, 1e144D, 1e145D, 1e146D, 1e147D, 1e148D, 1e149D, 1e150D,
			1e151D, 1e152D, 1e153D, 1e154D, 1e155D, 1e156D, 1e157D, 1e158D,
			1e159D, 1e160D, 1e161D, 1e162D, 1e163D, 1e164D, 1e165D, 1e166D,
			1e167D, 1e168D, 1e169D, 1e170D, 1e171D, 1e172D, 1e173D, 1e174D,
			1e175D, 1e176D, 1e177D, 1e178D, 1e179D, 1e180D, 1e181D, 1e182D,
			1e183D, 1e184D, 1e185D, 1e186D, 1e187D, 1e188D, 1e189D, 1e190D,
			1e191D, 1e192D, 1e193D, 1e194D, 1e195D, 1e196D, 1e197D, 1e198D,
			1e199D, 1e200D, 1e201D, 1e202D, 1e203D, 1e204D, 1e205D, 1e206D,
			1e207D, 1e208D, 1e209D, 1e210D, 1e211D, 1e212D, 1e213D, 1e214D,
			1e215D, 1e216D, 1e217D, 1e218D, 1e219D, 1e220D, 1e221D, 1e222D,
			1e223D, 1e224D, 1e225D, 1e226D, 1e227D, 1e228D, 1e229D, 1e230D,
			1e231D, 1e232D, 1e233D, 1e234D, 1e235D, 1e236D, 1e237D, 1e238D,
			1e239D, 1e240D, 1e241D, 1e242D, 1e243D, 1e244D, 1e245D, 1e246D,
			1e247D, 1e248D, 1e249D, 1e250D, 1e251D, 1e252D, 1e253D, 1e254D,
			1e255D, 1e256D, 1e257D, 1e258D, 1e259D, 1e260D, 1e261D, 1e262D,
			1e263D, 1e264D, 1e265D, 1e266D, 1e267D, 1e268D, 1e269D, 1e270D,
			1e271D, 1e272D, 1e273D, 1e274D, 1e275D, 1e276D, 1e277D, 1e278D,
			1e279D, 1e280D, 1e281D, 1e282D, 1e283D, 1e284D, 1e285D, 1e286D,
			1e287D, 1e288D, 1e289D, 1e290D, 1e291D, 1e292D, 1e293D, 1e294D,
			1e295D, 1e296D, 1e297D, 1e298D, 1e299D, 1e300D, 1e301D, 1e302D,
			1e303D, 1e304D, 1e305D, 1e306D, 1e307D, 1e308D };

	/**
	 * Assumes i is positive. Returns the magnitude of i in base 10.
	 */
	private static long tenthPower(long i) {
		if (i < 10L)
			return 1;
		else if (i < 100L)
			return 10L;
		else if (i < 1000L)
			return 100L;
		else if (i < 10000L)
			return 1000L;
		else if (i < 100000L)
			return 10000L;
		else if (i < 1000000L)
			return 100000L;
		else if (i < 10000000L)
			return 1000000L;
		else if (i < 100000000L)
			return 10000000L;
		else if (i < 1000000000L)
			return 100000000L;
		else if (i < 10000000000L)
			return 1000000000L;
		else if (i < 100000000000L)
			return 10000000000L;
		else if (i < 1000000000000L)
			return 100000000000L;
		else if (i < 10000000000000L)
			return 1000000000000L;
		else if (i < 100000000000000L)
			return 10000000000000L;
		else if (i < 1000000000000000L)
			return 100000000000000L;
		else if (i < 10000000000000000L)
			return 1000000000000000L;
		else if (i < 100000000000000000L)
			return 10000000000000000L;
		else if (i < 1000000000000000000L)
			return 100000000000000000L;
		else
			return 1000000000000000000L;
	}

	/**
	 * Returns the magnitude of the specified double
	 * 
	 * @param d
	 *            the double for which to retrieve the magnitude
	 * 
	 * @return the magnitude of the provided double
	 */
	private static int magnitude(double d) {
		long doubleToLongBits = Double.doubleToLongBits(d);
		int magnitude = (int) ((((doubleToLongBits & DoubleExpMask) >> DoubleExpShift) - DoubleExpBias) * 0.301029995663981);

		if (magnitude < -323)
			magnitude = -323;
		else if (magnitude > 308)
			magnitude = 308;

		if (d >= d_tenthPowers[magnitude + 323]) {
			while (magnitude < 309 && d >= d_tenthPowers[magnitude + 323])
				magnitude++;
			magnitude--;
			return magnitude;
		} else {
			while (magnitude > -324 && d < d_tenthPowers[magnitude + 323])
				magnitude--;
			return magnitude;
		}
	}

	/**
	 * long array containing powers of 10.
	 */
	private static final long[] l_tenthPowers = { 1, 10L, 100L, 1000L, 10000L,
			100000L, 1000000L, 10000000L, 100000000L, 1000000000L,
			10000000000L, 100000000000L, 1000000000000L, 10000000000000L,
			100000000000000L, 1000000000000000L, 10000000000000000L,
			100000000000000000L, 1000000000000000000L };

	/**
	 * Retrieve the n-th digit of a specified number
	 * 
	 * @param l
	 *            The number for which we need to find the n-th digit
	 * @param n
	 *            the index of the required digit (1-based = first digit)
	 * @return the n-th digit of the specified number.
	 */
	public static long getNthDigit(long l, int n) {
		return (l / (tenthPower(l) / l_tenthPowers[n - 1])) % 10;
	}


	public void append(FastByteArrayWrapper ba, double d) throws IOException {
		if (d == Double.NEGATIVE_INFINITY)
			ba.write(NEGATIVE_INFINITY);
		else if (d == Double.POSITIVE_INFINITY)
			ba.write(POSITIVE_INFINITY);
		else if (Double.isNaN(d))
			ba.write(NaN);
		else if (d == 0.0) {
			if ((Double.doubleToLongBits(d) & DoubleSignMask) != 0)
				ba.write(MINUS_DOUBLE_ZERO);
			else
				ba.write(DOUBLE_ZERO);
		} else {
			if (d < 0) {
				ba.write(45);
				d = -d;
			}

			if (d >= 0.001 && d < 0.01) {
				long i = (long) (d * 1E20);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				ba.write(DOUBLE_ZERO2);
				appendFractDigits(ba, i, -1);
			} else if (d >= 0.01 && d < 0.1) {
				long i = (long) (d * 1E19);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				ba.write(DOUBLE_ZERO);
				appendFractDigits(ba, i, -1);
			} else if (d >= 0.1 && d < 1) {
				long i = (long) (d * 1E18);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				ba.write(DOUBLE_ZERO0);
				appendFractDigits(ba, i, -1);
			} else if (d >= 1 && d < 10) {
				long i = (long) (d * 1E17);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				appendFractDigits(ba, i, 1);
			} else if (d >= 10 && d < 100) {
				long i = (long) (d * 1E16);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				appendFractDigits(ba, i, 2);
			} else if (d >= 100 && d < 1000) {
				long i = (long) (d * 1E15);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				appendFractDigits(ba, i, 3);
			} else if (d >= 1000 && d < 10000) {
				long i = (long) (d * 1E14);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				appendFractDigits(ba, i, 4);
			} else if (d >= 10000 && d < 100000) {
				long i = (long) (d * 1E13);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				appendFractDigits(ba, i, 5);
			} else if (d >= 100000 && d < 1000000) {
				long i = (long) (d * 1E12);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				appendFractDigits(ba, i, 6);
			} else if (d >= 1000000 && d < 10000000) {
				long i = (long) (d * 1E11);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				appendFractDigits(ba, i, 7);
			} else {
				int magnitude = magnitude(d);
				long i;
				if (magnitude < -305)
					i = (long) (d * 1E18 / d_tenthPowers[magnitude + 324]);
				else
					i = (long) (d / d_tenthPowers[magnitude + 323 - 17]);
				i = i % 100 >= 50 ? (i / 100) + 1 : i / 100;
				appendFractDigits(ba, i, 1);
				ba.write('E');
				append(ba, magnitude);
			}
		}
	}

	public void append(FastByteArrayWrapper ba, int i) throws IOException {
		if (i < 0) {
			if (i == Integer.MIN_VALUE) {
				// cannot make this positive due to integer overflow
				ba.write(MIN_VALUE);
			}
			ba.write(45);
			i = -i;
		}
		int c;
		if (i < 10) {
			// one digit
			ba.write(byteForDigit[i]);
		} else if (i < 100) {
			// two digits
			ba.write(byteForDigit[i / 10]);
			ba.write(byteForDigit[i % 10]);
		} else if (i < 1000) {
			// three digits
			ba.write(byteForDigit[i / 100]);
			ba.write(byteForDigit[(c = i % 100) / 10]);
			ba.write(byteForDigit[c % 10]);
		} else if (i < 10000) {
			// four digits
			ba.write(byteForDigit[i / 1000]);
			ba.write(byteForDigit[(c = i % 1000) / 100]);
			ba.write(byteForDigit[(c %= 100) / 10]);
			ba.write(byteForDigit[c % 10]);
		} else if (i < 100000) {
			// five digits
			ba.write(byteForDigit[i / 10000]);
			ba.write(byteForDigit[(c = i % 10000) / 1000]);
			ba.write(byteForDigit[(c %= 1000) / 100]);
			ba.write(byteForDigit[(c %= 100) / 10]);
			ba.write(byteForDigit[c % 10]);
		} else if (i < 1000000) {
			// six digits
			ba.write(byteForDigit[i / 100000]);
			ba.write(byteForDigit[(c = i % 100000) / 10000]);
			ba.write(byteForDigit[(c %= 10000) / 1000]);
			ba.write(byteForDigit[(c %= 1000) / 100]);
			ba.write(byteForDigit[(c %= 100) / 10]);
			ba.write(byteForDigit[c % 10]);
		} else if (i < 10000000) {
			// seven digits
			ba.write(byteForDigit[i / 1000000]);
			ba.write(byteForDigit[(c = i % 1000000) / 100000]);
			ba.write(byteForDigit[(c %= 100000) / 10000]);
			ba.write(byteForDigit[(c %= 10000) / 1000]);
			ba.write(byteForDigit[(c %= 1000) / 100]);
			ba.write(byteForDigit[(c %= 100) / 10]);
			ba.write(byteForDigit[c % 10]);
		} else if (i < 100000000) {
			// eight digits
			ba.write(byteForDigit[i / 10000000]);
			ba.write(byteForDigit[(c = i % 10000000) / 1000000]);
			ba.write(byteForDigit[(c %= 1000000) / 100000]);
			ba.write(byteForDigit[(c %= 100000) / 10000]);
			ba.write(byteForDigit[(c %= 10000) / 1000]);
			ba.write(byteForDigit[(c %= 1000) / 100]);
			ba.write(byteForDigit[(c %= 100) / 10]);
			ba.write(byteForDigit[c % 10]);
		} else if (i < 1000000000) {
			// nine digits
			ba.write(byteForDigit[i / 100000000]);
			ba.write(byteForDigit[(c = i % 100000000) / 10000000]);
			ba.write(byteForDigit[(c %= 10000000) / 1000000]);
			ba.write(byteForDigit[(c %= 1000000) / 100000]);
			ba.write(byteForDigit[(c %= 100000) / 10000]);
			ba.write(byteForDigit[(c %= 10000) / 1000]);
			ba.write(byteForDigit[(c %= 1000) / 100]);
			ba.write(byteForDigit[(c %= 100) / 10]);
			ba.write(byteForDigit[c % 10]);
		} else {
			// ten digits
			ba.write(byteForDigit[i / 1000000000]);
			ba.write(byteForDigit[(c = i % 1000000000) / 100000000]);
			ba.write(byteForDigit[(c %= 100000000) / 10000000]);
			ba.write(byteForDigit[(c %= 10000000) / 1000000]);
			ba.write(byteForDigit[(c %= 1000000) / 100000]);
			ba.write(byteForDigit[(c %= 100000) / 10000]);
			ba.write(byteForDigit[(c %= 10000) / 1000]);
			ba.write(byteForDigit[(c %= 1000) / 100]);
			ba.write(byteForDigit[(c %= 100) / 10]);
			ba.write(byteForDigit[c % 10]);
		}
	}

	private static void appendFractDigits(FastByteArrayWrapper ba, long i,
			int decimalOffset) throws IOException {
		long mag = tenthPower(i);
		long c;
		while (i > 0) {
			c = i / mag;
			ba.write(byteForDigit[(int) c]);
			decimalOffset--;
			if (decimalOffset == 0)
				ba.write(46);
			c *= mag;
			if (c <= i)
				i -= c;
			mag = mag / 10;
		}
		if (i != 0)
			ba.write(byteForDigit[(int) i]);
		else if (decimalOffset > 0) {
			ba.write(ZEROS[decimalOffset]);
			decimalOffset = 1;
		}

		decimalOffset--;
		if (decimalOffset == 0)
			ba.write(DOT_ZERO);
		else if (decimalOffset == -1)
			ba.write(48);
	}

}