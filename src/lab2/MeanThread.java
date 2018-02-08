package lab2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MeanThread {	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException {
		// TODO: read data from external file and store it in an array
		       // Note: you should pass the file as a first command line argument at runtime.
		List<Integer> integers = new ArrayList<>();
		if (args.length > 0){
			String fileName = args[0];
			File file = new File(fileName);
			Scanner sc = new Scanner(file);
			sc.useDelimiter(" ");
			while (sc.hasNext()){
				integers.add(Integer.parseInt(sc.next().trim()));
			}
			sc.close();
		} else {
			throw new FileNotFoundException("Please input the file for reading.");
		}
		// define number of threads
		int numOfThread = 2;
		if (args.length > 1){
			numOfThread = Integer.valueOf(args[1]);// this way, you can pass number of threads as 
		     // a second command line argument at runtime.	
		}
		
		// TODO: partition the array list into N subArrays, where N is the number of threads
		int chunkSize = integers.size() / numOfThread;
		List<List<Integer>> integerChunks = new ArrayList<>();
		
		for (int i = integers.size(); i >= chunkSize; i -= chunkSize){
			int from = i - chunkSize;
			if (from < chunkSize)
				from = 0;
			integerChunks.add(integers.subList(from, i));
		}
		
		final long startTime = System.currentTimeMillis();
		// TODO: start recording time
		
		// TODO: create N threads and assign subArrays to the threads so that each thread computes mean of 
		    // its repective subarray. For example,
		List<MeanMultiThread> threads = new ArrayList<>();
		for (List<Integer> subArray: integerChunks){
			MeanMultiThread thread = new MeanMultiThread(subArray);
			threads.add(thread);
			thread.start();
		}
		
		//Tip: you can't create big number of threads in the above way. So, create an array list of threads. 
		
		// TODO: start each thread to execute your computeMean() function defined under the run() method
		   //so that the N mean values can be computed. for example, 
		double globalSum = 0;
		for (int i = 0; i < threads.size(); i++){
			MeanMultiThread thread = threads.get(i);
			thread.join();
			double mean = thread.getMean();
			globalSum += mean;
			// TODO: show the N mean values
			System.out.printf("Temporal mean value of thread %d is ... %.2f\n", i, mean);	
			// TODO: store the temporal mean values in a new array so that you can use that 
		    /// array to compute the global mean.
		}
		
		// TODO: compute the global mean value from N mean values. 
		double globalMean = globalSum / numOfThread;
		
		// TODO: stop recording time and compute the elapsed time 
		
		System.out.printf("The global mean value is ... %.5f\n", globalMean);
		final long endTime = System.currentTimeMillis();
		System.out.printf("Time taken for %d threads: %d milliseconds\n", numOfThread, endTime - startTime);
				
	}
}

//Extend the Thread class
class MeanMultiThread extends Thread {
	private List<Integer> list;
	private double mean;
	MeanMultiThread(List<Integer> array) {
		list = array;
	}
	public double getMean() {
		return mean;
	}
	private double computeMean(){
		double sum = 0;
		for (Integer listItem: list){
			sum += listItem;
		}
		return sum / list.size();
	}
	
	public void run() {
		// TODO: implement your actions here, e.g., computeMean(...)
		mean = computeMean();
	}
}