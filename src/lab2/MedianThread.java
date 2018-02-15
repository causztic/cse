package lab2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MedianThread {

	public static void main(String[] args) throws InterruptedException, FileNotFoundException {
		// TODO: read data from external file and store it in an array
		// Note: you should pass the file as a first command line argument at
		// runtime.
		List<Integer> integers = new ArrayList<>();
		if (args.length > 0) {
			String fileName = args[0];
			File file = new File(fileName);
			Scanner sc = new Scanner(file);
			sc.useDelimiter(" ");
			while (sc.hasNext()) {
				integers.add(Integer.parseInt(sc.next().trim()));
			}
			sc.close();
		} else {
			throw new FileNotFoundException("Please input the file for reading.");
		}
		// define number of threads
		int numOfThread = 2;
		if (args.length > 1) {
			numOfThread = Integer.valueOf(args[1]);// this way, you can pass
													// number of threads as
			// a second command line argument at runtime.
		}

		// TODO: partition the array list into N subArrays, where N is the
		// number of threads
		int chunkSize = integers.size() / numOfThread;
		List<List<Integer>> integerChunks = new ArrayList<>();

		for (int i = integers.size(); i >= chunkSize; i -= chunkSize) {
			int from = i - chunkSize;
			if (from < chunkSize)
				from = 0;
			integerChunks.add(integers.subList(from, i));
		}

		final long startTime = System.currentTimeMillis();

		List<MedianMultiThread> threads = new ArrayList<>();
		for (List<Integer> subArray : integerChunks) {
			MedianMultiThread thread = new MedianMultiThread(subArray);
			threads.add(thread);
			thread.start();
		}

		List<List<Integer>> arrays = new ArrayList<>();
		for (int i = 0; i < threads.size(); i++) {
			MedianMultiThread thread = threads.get(i);
			thread.join();
			arrays.add(thread.getInternal());
		}

		int[] sortedFullArray = mergeSortArrays(arrays, arrays.size(), integers.size());

		// TODO: use any merge algorithm to merge the sorted subarrays and store
		// it to another array, e.g., sortedFullArray.

		// TODO: get median from sortedFullArray

		// e.g, computeMedian(sortedFullArray);
		double median = computeMedian(sortedFullArray);
		// TODO: stop recording time and compute the elapsed time
		final long endTime = System.currentTimeMillis();

		// TODO: printout the final sorted array
		// commented out because it lags
		// System.out.println(sortedFullArray);

		// TODO: printout median
		System.out.printf("The Median value is ... %.0f\n", median);
		System.out.printf("Time taken for %d threads: %d milliseconds\n", numOfThread, endTime - startTime);

	}

	private static double computeMedian(int[] inputArray) {
		// TODO: implement your function that computes median of values of an
		// array
		if (inputArray.length % 2 == 0) {
			return (inputArray[inputArray.length / 2] + inputArray[inputArray.length / 2 + 1]) / 2.0;
		} else {
			// get the middle
			return inputArray[inputArray.length / 2 + 1];
		}
	}

	// merge k sorted arrays with min heap.
	// https://www.geeksforgeeks.org/merge-k-sorted-arrays/
	// O(nk*Logk) time.
	private static int[] mergeSortArrays(List<List<Integer>> arrays, int k, int nk) {
		// 1. Create an output array of size n*k.
		int[] result = new int[nk];
		// 2. Create a min heap of size k and insert 1st element in all the
		// arrays into the heap
		MinHeap mh = new MinHeap();
		for (int i = 0; i < arrays.size(); i++) {
			mh.insert(arrays.get(i).get(0));
		}
		// 3. Repeat following steps n*k times.
		for (int i = 0; i < nk; i++) {
			// a) Get minimum element from heap (minimum is always at root) and
			// store it in output array.
			int min = mh.getMin();
			result[i] = min;
			for (int j = 0; j < arrays.size(); j++) {
				List<Integer> array = arrays.get(j);

				if (min == array.get(0)) {
					// b) Replace heap root with next element from the array
					// from which the element is extracted.
					// If the array doesn’t have any more elements, then replace
					// root with infinite. After replacing the root, heapify the
					// tree.
					if (array.size() > 1) {
						// replace the heap root with the next element
						// heapify
						array.remove(0);
						mh.replaceRootAndHeapify(array.get(0));
					} else {
						// replace the root with infinite
						// heapify
						arrays.remove(j);
						mh.replaceRootAndHeapify(Integer.MAX_VALUE);
					}
					break;
				}
			}
		}
		return result;
	}
}

// extend Thread
class MedianMultiThread extends Thread {
	private List<Integer> list;

	public List<Integer> getInternal() {
		return list;
	}

	MedianMultiThread(List<Integer> array) {
		list = array;
	}

	public void run() {
		list = mergeSort(list);
	}

	// This merge sort was adapted from my previous term's code
	private static int[] mergeSort(int[] input){
        if (input.length == 1){
            return input;
        }
        int[] left = Arrays.copyOfRange(input, 0, input.length / 2);
        int[] right = Arrays.copyOfRange(input, input.length / 2, input.length);

        left = mergeSort(left);
        right = mergeSort(right);

        // System.out.printf("LEFT: %s \t RIGHT: %s \n", Arrays.toString(left), Arrays.toString(right));
        // loop through the left and right to merge
        int[] output = new int[left.length + right.length];

        int leftPointer = 0;
        int rightPointer = 0;
        int outputPointer = 0;

        while(leftPointer < left.length || rightPointer < right.length){

            if (leftPointer == left.length) {
                output[outputPointer] = right[rightPointer];
                rightPointer++;
            } else if (rightPointer == right.length){
                output[outputPointer] = left[leftPointer];
                leftPointer++;
            } else if (left[leftPointer] < right[rightPointer]){
                output[outputPointer] = left[leftPointer];
                leftPointer++;
            } else {
                output[outputPointer] = right[rightPointer];
                rightPointer++;
            }
            outputPointer++;
        }

        return output;
	}
	private static List<Integer> mergeSort(List<Integer> list){
		int[] array = new int[list.size()];
		for (int i = 0; i < list.size(); i++){
			array[i] = list.get(i);
		}
		array = mergeSort(array);
		List<Integer> result = new ArrayList<>();
		for (int i = 0; i < array.length; i++){
			result.add(array[i]);
		}
		return result;
	}


}