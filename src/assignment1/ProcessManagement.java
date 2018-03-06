package assignment1;

import java.io.File;
import java.io.IOException;

public class ProcessManagement {

	// set the working directory
	private static File currentDirectory = new File("");
	// set the instructions file
	private static File instructionSet = new File("");
	public static Object lock = new Object();

	public static void main(String[] args) throws InterruptedException {

		if (args.length > 0) {
			instructionSet = new File(args[0]);
			if (instructionSet.exists()) {
				// parse the instruction file and construct a data structure, stored
				// inside ProcessGraph class
				File directoryPath = new File(currentDirectory.getAbsolutePath() + "/" + instructionSet);
				String name = instructionSet.getAbsoluteFile().getParent();
				ParseFile.generateGraph(directoryPath);

				ProcessGraph.printGraph();
				int executedCount = 0;
				// check if the all the processes have been executed
				mainLoop: while (executedCount != ProcessGraph.nodes.size()) {
					executedCount = 0;
					for (ProcessGraphNode node : ProcessGraph.nodes) {
						if (node.isExecuted()) {
							// the node has been executed, add one to the count.
							executedCount++;
						}

						if (node.allParentsExecuted()) {
							node.setRunnable();
							// if all parents are executed, set the node to be
							// runnable.
						}

						if (node.isRunnable()) {
							// it is ready to run
							ProcessBuilder pBuilder = new ProcessBuilder();
							pBuilder.command(node.getCommand().split(" "));

							if (!node.getInputFile().getName().equals("stdin")) {
								// if it is stdin, get from command.
								// otherwise, set the input file based on the graph
								// file.
								pBuilder.redirectInput(new File(name + "/" + node.getInputFile().getName()));
							}

							if (!node.getOutputFile().getName().equals("stdout")) {
								// if it is stdout, get from command.
								// otherwsise, set the output file based on the
								// graph file.
								pBuilder.redirectOutput(new File(name + "/" + node.getOutputFile().getName()));
							}

							try {
								// Wait for the process to finish before continuing
								pBuilder.start().waitFor();
								node.setExecuted();
							} catch (IOException e) {
								e.printStackTrace();
								// error running the process
								break mainLoop; // break the main loop in the event of an error.
							}
						}
					}

				}
				System.out.println("All processes ran successfully.");
			} else {
				System.err.println("File not found: " + instructionSet.getAbsolutePath());
			}
		} else {
			System.err.println("No graph file input. java assignment1.ProcessManagement filename");
		}
	}

}
