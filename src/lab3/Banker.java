package lab3;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

// package Week3;

public class Banker {
	private int numberOfCustomers;	// the number of customers
	private int numberOfResources;	// the number of resources

	private int[] available; 	// the available amount of each resource
	private int[][] maximum; 	// the maximum demand of each customer
	private int[][] allocation;	// the amount currently allocated
	private int[][] need;		// the remaining needs of each customer

	/**
	 * Constructor for the Banker class.
	 * @param resources          An array of the available count for each resource.
	 * @param numberOfCustomers  The number of customers.
	 */
	public Banker (int[] resources, int numberOfCustomers) {
		// TODO: set the number of resources
		this.numberOfResources = resources.length;

		// TODO: set the number of customers
		this.numberOfCustomers = numberOfCustomers;

		// TODO: set the value of bank resources to available
		this.available = resources;

		// TODO: set the array size for maximums, allocation, and need
		maximum = new int[numberOfCustomers][numberOfResources];
		allocation = new int[numberOfCustomers][numberOfResources];
		need = new int[numberOfCustomers][numberOfResources];
	}

	/**
	 * Sets the maximum number of demand of each resource for a customer.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param maximumDemand  An array of the maximum demanded count for each resource.
	 */
	public void setMaximumDemand(int customerIndex, int[] maximumDemand) {
		// TODO: add customer, update maximum and need
		maximum[customerIndex] = Arrays.copyOf(maximumDemand, numberOfResources);
		need[customerIndex] = Arrays.copyOf(maximumDemand, numberOfResources);
	}

	/**
	 * Prints the current state of the bank.
	 */
	public void printState() {
		// TODO: print available
		System.out.println("\nCurrent State:");
		System.out.println("Customers\tMaximum\t\tAllocation\tNeed\t\tAvailable");
		
		System.out.printf("%d\t\t%s\t%s\t%s\t%s\n", 
				0,  Arrays.toString(maximum[0]), Arrays.toString(allocation[0]),
				Arrays.toString(need[0]), Arrays.toString(available));
		
		for (int i = 1; i < numberOfCustomers; i++){
			System.out.printf("%d\t\t%s\t%s\t%s\n", 
					i, Arrays.toString(maximum[i]), Arrays.toString(allocation[i]),
					Arrays.toString(need[i]));
		}
	}

	/**
	 * Requests resources for a customer loan.
	 * If the request leave the bank in a safe state, it is carried out.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param request        An array of the requested count for each resource.
	 * @return true if the requested resources can be loaned, else false.
	 */
	public synchronized boolean requestResources(int customerIndex, int[] request) {
		// TODO: print the request
		System.out.printf("Customer %d requesting %s\n", customerIndex, Arrays.toString(request));
		
		// TODO: check if request larger than available
		// TODO: check if request larger than need
		for (int i = 0; i < numberOfResources; i++){
			if (request[i] > available[i])
				return false;
			if (request[i] > need[customerIndex][i])
				return false;
		}
		
		// TODO: check if the state is safe
		// TODO: if it is safe, allocate the resources to customer customerNumber
		if (checkSafe(customerIndex, request)){
			for (int i = 0; i < request.length; i++){
				allocation[customerIndex][i] += request[i];
				need[customerIndex][i] -= request[i];
				available[i] -= request[i];
			}
			return true;
		}
		
		return false;
	}

	/**
	 * Releases resources borrowed by a customer. Assume release is valid for simplicity.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param release        An array of the release count for each resource.
	 */
	public synchronized void releaseResources(int customerIndex, int[] release) {
		// TODO: print the release
		System.out.printf("Customer %d releasing %s\n", customerIndex, Arrays.toString(release));
		
		// TODO: release the resources from customer customerNumber
		for (int i = 0; i < release.length; i++){
			allocation[customerIndex][i] -= release[i];
			available[i] += release[i];
		}
	
	}

	/**
	 * Checks if the request will leave the bank in a safe state.
	 * @param customerIndex  The customer's index (0-indexed).
	 * @param request        An array of the requested count for each resource.
	 * @return true if the requested resources will leave the bank in a
	 *         safe state, else false
	 */
	private synchronized boolean checkSafe(int customerIndex, int[] request) {
		
		int[] tempAvail = new int[numberOfResources];
		int[][] tempNeed = new int[numberOfCustomers][numberOfResources];
		int[][] tempAllocation = new int[numberOfCustomers][numberOfResources];
		
		for (int i = 0; i < numberOfResources; i++){
			//temp_avail = available - request; 
			//temp_need(customerNumber) = need - request; 
			//temp_allocation(customerNumber) = allocation + request;
			tempAvail[i] = available[i] - request[i];
			tempNeed[customerIndex][i] = need[customerIndex][i] - request[i];
			tempAllocation[customerIndex][i] = allocation[customerIndex][i] + request[i];
		}
		
		// work = temp_avail
		int[] work = Arrays.copyOf(tempAvail, numberOfResources);
		
		// finish(all) = false;
		boolean[] finish = new boolean[numberOfCustomers]; // defaults to false on creation
		
		boolean possible = true;
		
		while (possible){
			possible = false;
			
			// for customer Ci = 1:n
			for (int customer = 0; customer < numberOfCustomers; customer++){
				boolean isNeedSafe = true;
				
				// temp_need(ci) <= work
				for (int resource = 0; resource < numberOfResources; resource++){
					//System.out.printf("Customer %d, Resource #%d, %d > %d\n", customer, resource, tempNeed[customer][resource], work[resource]);
					if (tempNeed[customer][resource] > work[resource]){
						// need more than available, needSafe is false
						isNeedSafe = false;
					} else {
						isNeedSafe = true;
					}
				}
				
				if (!finish[customer] && isNeedSafe){
					possible = true;
					// add the temporary allocation to work.
					for (int resource = 0; resource < numberOfResources; resource++){
						work[resource] += tempAllocation[customer][resource];
					}
					finish[customer] = true;
				}
			}
		}
		boolean allTrue = true;
		for (int i = 0; i < finish.length; i++){
			if (!finish[i]){
				allTrue = false;
				break;
			}
		}
		return allTrue;
	}

	/**
	 * Parses and runs the file simulating a series of resource request and releases.
	 * Provided for your convenience.
	 * @param filename  The name of the file.
	 */
	public static void runFile(String filename) {

		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filename));

			String line = null;
			String [] tokens = null;
			int [] resources = null;

			int n, m;

			try {
				n = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 1.");
				fileReader.close();
				return;
			}

			try {
				m = Integer.parseInt(fileReader.readLine().split(",")[1]);
			} catch (Exception e) {
				System.out.println("Error parsing n on line 2.");
				fileReader.close();
				return;
			}

			try {
				tokens = fileReader.readLine().split(",")[1].split(" ");
				resources = new int[tokens.length];
				for (int i = 0; i < tokens.length; i++)
					resources[i] = Integer.parseInt(tokens[i]);
			} catch (Exception e) {
				System.out.println("Error parsing resources on line 3.");
				fileReader.close();
				return;
			}

			Banker theBank = new Banker(resources, n);

			int lineNumber = 4;
			while ((line = fileReader.readLine()) != null) {
				tokens = line.split(",");
				if (tokens[0].equals("c")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.setMaximumDemand(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("r")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.requestResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("f")) {
					try {
						int customerIndex = Integer.parseInt(tokens[1]);
						tokens = tokens[2].split(" ");
						resources = new int[tokens.length];
						for (int i = 0; i < tokens.length; i++)
							resources[i] = Integer.parseInt(tokens[i]);
						theBank.releaseResources(customerIndex, resources);
					} catch (Exception e) {
						System.out.println("Error parsing resources on line "+lineNumber+".");
						fileReader.close();
						return;
					}
				} else if (tokens[0].equals("p")) {
					theBank.printState();
				}
			}
			fileReader.close();
		} catch (IOException e) {
			System.out.println("Error opening: "+filename);
		}

	}

	/**
	 * Main function
	 * @param args  The command line arguments
	 */
	public static void main(String [] args) {
		if (args.length > 0) {
			runFile(args[0]);
		}
	}

}