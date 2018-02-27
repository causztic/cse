package lab4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class FileOperation {
	private static File currentDirectory = new File(System.getProperty("user.dir"));
	private static final List<String> SORT_ITEMS = Arrays.asList("time", "name", "size");
	public static void main(String[] args) throws java.io.IOException {
		
		String commandLine;

		BufferedReader console = new BufferedReader
				(new InputStreamReader(System.in));

		while (true) {
			// read what the user entered
			System.out.print("jsh>");
			commandLine = console.readLine();

			// clear the space before and after the command line
			commandLine = commandLine.trim();

			// if the user entered a return, just loop again
			if (commandLine.equals("")) {
				continue;
			}
			// if exit or quit
			else if (commandLine.equalsIgnoreCase("exit") | commandLine.equalsIgnoreCase("quit")) {
				System.exit(0);
			}

			// check the command line, separate the words
			String[] commandStr = commandLine.split(" ");
			List<String> command = new ArrayList<String>();
			for (int i = 0; i < commandStr.length; i++) {
				command.add(commandStr[i]);
			}
			
			switch(commandStr[0]){
			case "create": 
				// Create a file
				create(currentDirectory, commandStr[1]);
				break;
			case "delete":
				delete(currentDirectory, commandStr[1]);
				break;
			case "display":
				cat(currentDirectory, commandStr[1]);
				break;
			case "list":
				// list property #{item}
				String sortMethod = "";
				String viewMethod = "";
				if (commandStr.length > 1){
					viewMethod = commandStr[1];
				}
				if (commandStr.length > 2){
					sortMethod = commandStr[2];
				}
				ls(currentDirectory, viewMethod, sortMethod);
				break;
			default: 
				// other commands
				ProcessBuilder pBuilder = new ProcessBuilder(command);
				pBuilder.directory(currentDirectory);
				try{
					Process process = pBuilder.start();
					// obtain the input stream
					InputStream is = process.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);

					// read what is returned by the command
					String line;
					while ( (line = br.readLine()) != null)
						System.out.println(line);

					// close BufferedReader
					br.close();
				}
				// catch the IOexception and resume waiting for commands
				catch (IOException ex){
					System.out.println(ex);
					continue;
				}
			}
		}
	}

	/**
	 * Create a file
	 * @param dir - current working directory
	 * @param command - name of the file to be created
	 */
	public static void create(File dir, String name) {
		File file = new File(dir.getAbsolutePath() + "/" + name);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Delete a file
	 * @param dir - current working directory
	 * @param name - name of the file to be deleted
	 */
	public static void delete(File dir, String name) {
		File file = new File(dir.getAbsolutePath() + "/" + name);
		if (!file.delete())
			System.err.println("File not deleted. If it was a directory, ensure that it is empty.");
	}

	/**
	 * Display the file
	 * @param dir - current working directory
	 * @param name - name of the file to be displayed
	 */
	public static void cat(File dir, String name) {
		File file = new File(dir.getAbsolutePath() + "/" + name);
		BufferedReader reader;
		String line;
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
			    System.out.println(line);
			}
			reader.close();
		} catch (Exception e) {
			System.err.println("File not found.");
		}
	}

	/**
	 * Function to sort the file list
	 * @param list - file list to be sorted
	 * @param sort_method - control the sort type
	 * @return sorted list - the sorted file list
	 */
	private static File[] sortFileList(File[] list, String sort_method) {
		// sort the file list based on sort_method
		// if sort based on name
		if (sort_method.equalsIgnoreCase("name")) {
			Arrays.sort(list, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return (f1.getName()).compareTo(f2.getName());
				}
			});
		}
		else if (sort_method.equalsIgnoreCase("size")) {
			Arrays.sort(list, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Long.valueOf(f1.length()).compareTo(f2.length());
				}
			});
		}
		else if (sort_method.equalsIgnoreCase("time")) {
			Arrays.sort(list, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
				}
			});
		}
		return list;
	}

	/**
	 * List the files under directory
	 * @param dir - current directory
	 * @param display_method - control the list type
	 * @param sort_method - control the sort type
	 */
	public static void ls(File dir, String displayMethod, String sortMethod) {
		if (displayMethod.equals("")){
			for(File file: dir.listFiles()){
				System.out.println(file.getName());
			}
		} else if (displayMethod.equals("property")) {
			// property
			File[] files = dir.listFiles();
			if (SORT_ITEMS.contains(sortMethod)){
				files = sortFileList(files, sortMethod);
			}
			for (File file: files){
				System.out.printf("%-15s\tSize: %d\tLast Modified: %s\n", file.getName(), file.length(), new Date(file.lastModified()).toString());
			}
		}
	}

	/**
	 * Find files based on input string
	 * @param dir - current working directory
	 * @param name - input string to find in file's name
	 * @return flag - whether the input string is found in this directory and its subdirectories
	 */
	public static boolean find(File dir, String name) {
		boolean flag = false;
		// TODO: find files
		return flag;
	}

	/**
	 * Print file structure under current directory in a tree structure
	 * @param dir - current working directory
	 * @param depth - maximum sub-level file to be displayed
	 * @param sort_method - control the sort type
	 */
	public static void tree(File dir, int depth, String sort_method) {
		// TODO: print file tree
	}

	// TODO: define other functions if necessary for the above functions

}