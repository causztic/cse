package lab1;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * SimpleShell.
 * Author: Lim Yao Jie
 * Accepts commands with leading and trailing spaces
 * Accepts leading and trailing spaces for cd and saves to history as-is (similar to bash)
 * Error checking for historical commands to prevent out-of-bounds
 * Accepts multiple .. on directory navigation
 */

public class SimpleShell {
	static List<String> history = new ArrayList<>();
	public static void main(String[] args) throws java.io.IOException {
		String commandLine = "";
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		ProcessBuilder pb = new ProcessBuilder();
		boolean historyCommand = false;
		while (true) {
			if (!historyCommand){
				System.out.print("jsh>");
				commandLine = console.readLine();
			}
			else {
				// call the history function and turn it off
				historyCommand = false;
			}
			try {
			// read what the user entered if it wasn't called by history.
			commandLine = commandLine.trim(); // remove leading and trailing whitespace
			// if the user entered a return, just loop again
			if (commandLine.equals("")) {
				continue;
			} else {
				String[] commandList = commandLine.split(" ");
				if (commandList[0].equals("cd")) {
					String homePath = System.getProperty("user.home");
					File currentDir = pb.directory();
					if (currentDir == null)
						currentDir = new File("");
					currentDir = new File(currentDir.getAbsolutePath());

					String directory = "";
					if (commandList.length > 1){
						String[] directoryArgs = commandList[1].split("/");
						// support multiple .., ., ~
						if (directoryArgs[0].length() == 0){
							//root directory
							directory = "/";
						} else {
							// current folder
							directory += currentDir.getAbsolutePath() + "/" + directoryArgs[0] + "/";
							currentDir = new File(directory);
						}
						directoryArgs = Arrays.copyOfRange(directoryArgs, 1, directoryArgs.length);
						for (int i = 0; i < directoryArgs.length; i++) {
							directoryArgs[i] = directoryArgs[i].trim(); // trim directory
							if (directoryArgs[i].charAt(0) == '~' && directoryArgs[i].length() == 1) {
								// home directory
								directory += homePath + "/";
								currentDir = new File(directory);
								
							} else if (directoryArgs[i].equals("..")) {
								directory = currentDir.getParent() + "/";
								currentDir = currentDir.getParentFile();
							} else if (directoryArgs[i].equals(".")) {
								directory += currentDir.getAbsolutePath() + "/";
							} else {
								// it is a normal directory
								directory += directoryArgs[i] + "/";
								currentDir = new File(directory);
							}
						}
					} else {
						// go to home folder
						directory = homePath + "/";
						currentDir = new File(directory);
					}
					//System.out.println(directory);
					history.add(commandLine);
					File file = new File(directory);
					if (file.exists())
						pb.directory(file);
					else
						throw new FileNotFoundException(directory);
				} else if (commandList[0].equals("history")) {
					history.add(commandLine);
					// history
					for (int i = 0; i < history.size(); i++){
						System.out.println((i+1) + "\t" + history.get(i));
					}
					
				} else if (commandList[0].equals("!!")) {
					if (history.size() > 0){
						// run previous command
						// dont save !! into history
						commandLine = history.get(history.size()-1);
						// if it is not history, save it
						if (!commandLine.equals("history")){
							history.add(commandLine);
						}
						historyCommand = true;
					} else {
						throw new Exception("No history found.");
					}
				} else if (commandList[0].startsWith("!")) {
					// run a specific command
					int index = Integer.parseInt(commandList[0].substring(1)) - 1;
					try {
						commandLine = history.get(index);
						// if it is not history, save it
						if (!commandLine.equals("history")){
							history.add(commandLine);
						}
						historyCommand = true;
					} catch (IndexOutOfBoundsException e){
						System.out.println(commandList[0] + ": event not found.");
					}
				} else {
					runCommand(pb, commandList, commandLine);
				}

			}
			} catch (FileNotFoundException f){
				System.err.println("cd: " + f.getMessage() + ": No such file or directory");
			} catch (Exception e){
				System.err.println(e.getMessage());
			}
		}
	}

	private static void runCommand(ProcessBuilder pb, String[] commandList, String commandLine) {
		try {
			pb.command(commandList);
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			for (String line; (line = br.readLine()) != null;) {
				System.out.println(line);
			}
			history.add(commandLine);
			br.close();
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
}