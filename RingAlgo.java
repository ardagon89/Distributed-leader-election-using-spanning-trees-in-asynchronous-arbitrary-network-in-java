import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RingAlgo {

	private static int numberOfProcesses;
	private static int[] processes;
	private static int[][] adjacencyMatrix;

	//Read the input file and store all process Ids in an array
	public static void populateData() throws NumberFormatException, IOException {
		// read from file: n, array of size n
		File file = new File("input.dat");
		BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
		numberOfProcesses = Integer.valueOf(reader.readLine());
		String processesLine = reader.readLine();
		processes = Arrays.asList(processesLine.split(","))
				.stream()
				.map(String::trim)
				.mapToInt(Integer::parseInt).toArray();

		//Initializing a new Adjacency Matrix
		adjacencyMatrix = new int[numberOfProcesses][numberOfProcesses];
		for (int i=0; i< numberOfProcesses; i++){
			String line = reader.readLine();
			adjacencyMatrix[i] = Arrays.asList(line.split(","))
					.stream()
					.map(String::trim)
					.mapToInt(Integer::parseInt).toArray();
		}
		reader.close();

	}

	//Main method of the entire program
	public static void main(String[] args) throws Throwable {
		// boot the algorithm
		populateData();
		//Create the master/parent process
		Thread process = new Thread(new MainProcess(processes, adjacencyMatrix));
		process.start();
	}

}