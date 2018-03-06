package assignment1;

import java.io.File;
import java.io.IOException;

public class ProcessManagement {

    //set the working directory
    private static File currentDirectory = new File("files");
    //set the instructions file
    private static File instructionSet = new File("test2.txt");
    public static Object lock=new Object();

    public static void main(String[] args) throws InterruptedException {

        //parse the instruction file and construct a data structure, stored inside ProcessGraph class
        ParseFile.generateGraph(new File(currentDirectory.getAbsolutePath() + "/"+instructionSet));

        ProcessGraph.printGraph();
        int executedCount = 0;
        
        // check if the all the processes have been executed
       while (executedCount != ProcessGraph.nodes.size()){
        	executedCount = 0;
            for (ProcessGraphNode node: ProcessGraph.nodes){
            	if (node.isExecuted()){
            		// the node has been executed, add one to the count.
            		executedCount++;
            	}
            	
            	if (node.allParentsExecuted()){
            		node.setRunnable();
            	}
            	
            	if (node.isRunnable()){
            		// it is ready to run
            		ProcessBuilder pBuilder = new ProcessBuilder();
            		pBuilder.command(node.getCommand().split(" "));
            		
            		if (!node.getInputFile().getName().equals("stdin")){
            			// it is stdin, get from command.
            			// otherwise, set the input file based on the process.
            			pBuilder.redirectInput(node.getInputFile());
            		}
            		
            		if (!node.getOutputFile().getName().equals("stdout")){
            			pBuilder.redirectOutput(node.getOutputFile());
            		}
            		
            		try {
						pBuilder.start().waitFor();
						node.setExecuted();
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            }
            
        }

        System.out.println("All processes finished successfully");
    }

}
