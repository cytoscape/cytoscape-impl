/*

  RESULTS OF RUNNING TESTS ON A COMPUTER.  THESE PERFORMANCE TESTS
  MEASURE THE PERFORMANCE OF COMPUTING UNIQUE SETS OF INTEGERS FROM
  SETS OF INTEGERS CONTAINING POTENTIAL DUPLICATES, USING 4 DIFFERENT
  ALGORITHMIC STRATEGIES.  THE PROBLEM IS: GIVEN A SET OF N INTEGERS
  IN THE RANGE [0,N-1], RETURN A SUBSET WITH DUPLICATES REMOVED.

  Giving the JVM half a gig of memory, against the same random
  bytes file, under "same conditions".  Note: the set of unique
  integers returned by the heap and tree test is ordered as a result
  of the nature of the algorithm.

           Milliseconds taken to perform test case:

      N     | MinIntHeap  |  IntHash    |  BitSet     |  IntBTree   |
  ----------+------+------+------+------+------+------+------+------+
            | init |repeat| init |repeat| init |repeat| init |repeat|
            +------+------+------+------+------+------+------+------+
           1|     0      0|     0      0|     0      0|     0    N/A|
         100|     0      1|     0      0|     0      1|    10       |
        1000|     3      3|     5      1|     2      2|    12       |
       10000|    13      6|    11      5|    12      3|    28       |
       50000|    36     26|    31     18|    25      9|    73       |
      100000|    61     53|    48     47|    33     16|   133       |
      200000|   134    117|   111     90|    57     26|   260       |
      500000|   410    375|   300    230|   133     57|   754       |
     1000000|   950    920|   600    500|   227    113|  1670       |
     2000000|  2210   2190|  1200   1030|   450    220|  3751       |
     5000000|  6700   6550|  3200   2800|  1180    718| 11103       |
    10000000| 15000  15000|  6400   5775|  2740   1920| 24402       |
    15000000| 24000       |  8925   8220|  4500       | 38500       |
    20000000| 34500       | 12875       |  6000       |   N/A       |
    30000000| 55000       |     X       |  9620       |             |
    40000000| 81000       |             | 12150       |             |
    50000000|     X       |             |     X       |             |
            | (out of     |             |             |             |
            | memory)     |             |             |             |

*/
package org.cytoscape.util.intr;

import org.cytoscape.util.intr.IntEnumerator;
import org.cytoscape.util.intr.MinIntHeap;

import java.io.IOException;
import java.io.InputStream;


/**
 *
 */
 // TODO Turn into JUnit tests
public class MinIntHeapPerformance {
	/**
	 * Argument at index 0: a number representing the number of elements to be
	 * tossed onto a heap.  If N elements are tossed onto a heap, each element
	 * shall be in the range [0, N-1].<p>
	 * Standard input is read, and should contain bytes [read: binary data] of
	 * input defining
	 * integer elements to be tossed onto the heap, with enough
	 * bytes to define N integers (each integer is 4 bytes of standard input).
	 * Integers are
	 * defined from the input by taking groups of 4 consecutive bytes from input,
	 * each group defining a single integer by interpreting the first byte in
	 * a group to be the most significant bits of the integer etc.  The
	 * range [0, N-1] of each integer is satisifed by dividing the positized
	 * value of each assembled
	 * four-byte integer by N, and taking the remainder as the element to be
	 * tossed onto the heap.<p>
	 * Writes to standard out the ordered set of input integers with
	 * duplicates pruned, such that each output integer is followed by the
	 * system's newline separator character sequence.  The integers written are
	 * in plaintext, unlike the format of the input.<p>
	 * Output to standard error is the time taken to use the heap to order
	 * the input, with duplicates removed.  The output format is simply a
	 * plaintext
	 * integer representing the number of milliseconds required for this
	 * test case, followed by the system's newline separator character
	 * sequence.  Basically, a timer starts
	 * right before calling the MinIntHeap constructor with an array of
	 * input integers; the timer stops after we've instantiated a new array to
	 * contain the ordered list of elements with duplicates removed, and after
	 * we've completely filled the array with these elements.  Note that
	 * the process of instantiating this array is time consuming and has nothing
	 * to do with the algorithm we're trying to test; this operation is
	 * included in this time trial anyways.<p>
	 * An optional second argument to this program is the string 'repeat' -
	 * this command-line option will run a first test, and then a repeated test
	 * using the same objects that were used in the first test (basically, the
	 * purpose of the repeated test is to not instantiate anything) - all
	 * output information is based off of the repeated test.
	 */
	public static void main(String[] args) throws Exception {
		int N = Integer.parseInt(args[0]);
		boolean repeat = false;

		if ((args.length > 1) && args[1].equalsIgnoreCase("repeat"))
			repeat = true;

		int[] elements = new int[N];
		InputStream in = System.in;
		byte[] buff = new byte[4];
		int inx = 0;
		int off = 0;
		int read;

		while ((inx < N) && ((read = in.read(buff, off, buff.length - off)) > 0)) {
			off += read;

			if (off < buff.length)
				continue;
			else
				off = 0;

			elements[inx++] = (0x7fffffff & assembleInt(buff)) % N;
		}

		if (inx < N)
			throw new IOException("premature end of input");

		// Lose reference to as much as we can.
		in = null;
		buff = null;

		// Load the classes we're going to use into the classloader.
		_THE_TEST_CASE_(new int[] { 0, 3, 4, 3, 9, 9, 1 });

		// Sleep, collect garbage, have a snack, etc.
		Thread.sleep(1000);

		// Wake up.  Warm up.  Get up on your feet.
		for (int i = 0; i < 100; i++) {
			int foo = (i * 4) / 8;
		}

		// Start timer.
		long millisBegin = System.currentTimeMillis();

		// Run the test.  Quick, stopwatch is ticking!
		int[] uniqueElements = _THE_TEST_CASE_(elements);

		// Stop timer.
		long millisEnd = System.currentTimeMillis();

		// Print the time taken to standard error.
		if (!repeat)
			System.err.println(millisEnd - millisBegin);

		// Print sorted array to standard out.
		if (!repeat)
			for (int i = 0; i < uniqueElements.length; i++)
				System.out.println(uniqueElements[i]);

		// Run repeated test if that's what the command line told us.
		if (repeat) {
			for (int i = 0; i < uniqueElements.length; i++)
				uniqueElements[i] = 0;

			millisBegin = System.currentTimeMillis();
			_REPEAT_TEST_CASE_(elements, uniqueElements);
			millisEnd = System.currentTimeMillis();
			System.err.println((millisEnd - millisBegin) + " (repeated test)");

			for (int i = 0; i < uniqueElements.length; i++)
				System.out.println(uniqueElements[i]);
		}
	}

	private static final int assembleInt(byte[] fourConsecutiveBytes) {
		int firstByte = (((int) fourConsecutiveBytes[0]) & 0x000000ff) << 24;
		int secondByte = (((int) fourConsecutiveBytes[1]) & 0x000000ff) << 16;
		int thirdByte = (((int) fourConsecutiveBytes[2]) & 0x000000ff) << 8;
		int fourthByte = (((int) fourConsecutiveBytes[3]) & 0x000000ff) << 0;

		return firstByte | secondByte | thirdByte | fourthByte;
	}

	// Keep a reference to our data structure so that we can determine how
	// much memory was consumed by our algorithm (may be implemented in future).
	static MinIntHeap _THE_HEAP_ = null;

	private static final int[] _THE_TEST_CASE_(final int[] elements) {
		_THE_HEAP_ = new MinIntHeap();
		_THE_HEAP_.toss(elements, 0, elements.length);

		final IntEnumerator iter = _THE_HEAP_.orderedElements(true);
		final int[] returnThis = new int[iter.numRemaining()];
		final int numElements = returnThis.length;

		for (int i = 0; i < numElements; i++)
			returnThis[i] = iter.nextInt();

		return returnThis;
	}

	private static final void _REPEAT_TEST_CASE_(final int[] elements, final int[] output) {
		_THE_HEAP_.empty();
		_THE_HEAP_.toss(elements, 0, elements.length);

		final IntEnumerator iter = _THE_HEAP_.orderedElements(true);
		final int numElements = iter.numRemaining();

		if (numElements != output.length)
			throw new IllegalStateException("output array is incorrect size");

		for (int i = 0; i < numElements; i++)
			output[i] = iter.nextInt();
	}
}
