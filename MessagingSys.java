import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/// Class for simulating links/channels between nodes
public class MessagingSys {
	static Queue<Message>[][] messagelist;
	static int length;
	private static int[] processes;
	private int[][] adjacencyMatrix;

	//Method for initializing the private variables
	public MessagingSys(int length, int[] processes, int[][] adjacencyMatrix) {
		this.length = length;
		this.processes = processes;
		this.adjacencyMatrix = adjacencyMatrix;
		messagelist = new Queue[length][length];
		for (int i=0; i<length; i++){
			messagelist[i] = (Queue<Message>[]) new Queue[length];
			for (int j=0; j<length; j++){
				messagelist[i][j] = new LinkedList<>();
			}
		}
	}

	//Method for finding processId based on index
	 int getProcessId(int index){
		return processes[index];
	}

	//Method for finding the next id
	int findNextId(){
		return Arrays.stream(processes).max().getAsInt();
	}

	//Method for finding Index based on the process ID
	 int getIndex(int processId){
		for (int i=0; i< processes.length; i++){
			if (processes[i] == processId){
				return i;
			}
		}
		return -1;
	}

	 //Method for finding neighbors
	int findNeighbours(int location){
		int count = 0;
		for (int i=0; i<length; i++){
			if (adjacencyMatrix[location][i] == 1){
				count++;
			}
		}

		return count;
	}

	// gets the message from my incoming channel/link
	public Message get(int sender, int receiver, int round) {
		if (receiver == -1 || sender == -1){
			return null;
		}
		Queue<Message> queue = messagelist[receiver][sender];
		if(queue.size()>0)
		{
			try
			{
				if(queue.peek().round<=round)
				{
					return queue.remove();
				}
				else
				{
					return null;
				}
			}
			catch(Exception ex)
			{
				System.out.println(ex + " in MessagingSys.get()");
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	// places the message in my outgoing channel/link with randomized delay
	public void put(int sender, int receiver, Message Msg) {
		if (adjacencyMatrix[sender][receiver] == 1){
			messagelist[receiver][sender].add(Msg.setRound((int)(Math.random() * 10) + 1 + Msg.round));
		}
	}
	
	//Method for printing the 2D array of message queues 
	public void print()
	{
		for(int i=0; i<messagelist.length; i++)
		{
			for (int j=0; j<messagelist.length; j++) {
				System.out.print(messagelist[i][j].peek() + " ");
			}
			System.out.println(" ");
		}
	}
}