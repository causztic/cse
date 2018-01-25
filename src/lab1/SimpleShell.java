package lab1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleShell {
	static List<String> history = new ArrayList<>();
	public static void main(String[] args) throws java.io.IOException {
		String commandLine = "";
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		ProcessBuilder pb = new ProcessBuilder();
		boolean historyCommand = false;
		while (true) {
			// read what the user entered
			if (!historyCommand){
				System.out.print("jsh>");
				commandLine = console.readLine();
			}
			else {
				historyCommand = false;
			}
			// TODO: adding a history feature

			// if the user entered a return, just loop again
			if (commandLine.equals("")) {
				continue;
			} else {
				String[] commandList = commandLine.split(" ");
				if (commandList[0].equals("cd") && commandList.length > 1) {
					String[] directoryArgs = commandList[1].split("/");
					String homePath = System.getProperty("user.home");
					File currentDir = pb.directory();
					if (currentDir == null)
						currentDir = new File("");
					currentDir = new File(currentDir.getAbsolutePath());

					String directory = "";
					// support .., ., ~
					for (int i = 0; i < directoryArgs.length; i++) {
						if (directoryArgs[i].length() == 0) {
							// root directory
							directory += "/";
						} else {
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
					}
					//System.out.println(directory);
					history.add(commandLine);
					pb.directory(new File(directory));
				} else if (commandList[0].equals("history")) {
					history.add(commandLine);
					// history
					for (int i = 0; i < history.size(); i++){
						System.out.println((i+1) + "\t" + history.get(i));
					}
					
				} else if (commandList[0].equals("!!") && history.size() > 0) {
					// run previous command
					// dont save !! into history
					commandLine = history.get(history.size()-1);
					// if it is not history, save it
					if (!commandLine.equals("history")){
						history.add(commandLine);
					}
					historyCommand = true;
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
					} catch (ArrayIndexOutOfBoundsException e){
						System.out.println(commandList[0] + ": event not found.");
					}
				} else {
					runCommand(pb, commandList, commandLine);
				}

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
			System.out.println(e.getMessage());
		}
	}
}