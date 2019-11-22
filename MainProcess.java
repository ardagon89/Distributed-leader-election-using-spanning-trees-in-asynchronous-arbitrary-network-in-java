import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class MainProcess implements Runnable {

	private static int[] processes;
	private static int[][] adjacencyMatrix;
	private static Set<Thread> processSet;
	public MySignal mySignal = new MySignal();
	public MessagingSys myMessageSys;
	public Iterator<Thread> itr;
	private boolean completed;
	private IDs ids;
	private static Set<Semaphore> semSet;
	public Iterator<Semaphore> semItr;
	private int round;

	public MainProcess(int[] processes, int[][] adjacencyMatrix) {
		MainProcess.processes = processes;
		adjacencyMatrix = adjacencyMatrix;
		myMessageSys = new MessagingSys(processes.length, processes, adjacencyMatrix);
		this.completed = false;
		this.ids = new IDs(processes);
		semSet = new HashSet<>();
		this.round = 0;
	}

	public void spawnProcesses() throws InterruptedException {
		this.round++;
		System.out.println(this.round+" round started in main process");
		processSet = new HashSet<>();
		// spawn n number of threads, store them in a set
		for (int i = 0; i < processes.length; i++) {
			Semaphore sem = new Semaphore(1);
			Thread process = new Thread(new RingProcess(this.ids, mySignal, i, myMessageSys, sem, processes.length));
			process.setName(String.valueOf(processes[i]));
			//Keeping count of how many threads have been started by me in the current round
			mySignal.add();
			process.start();
			processSet.add(process);
			semSet.add(sem);
		}
		// run the algorithm till leader is found and everyone knows it
		while (!this.completed) {
			// sleep for 10ms if the round is in progress
			while (!mySignal.roundCompleted()) {
				Thread.sleep(10);
			}

			itr = processSet.iterator();
			while (itr.hasNext()) {
				Thread thread = itr.next();
				// System.out.println(thread.getName()+":"+thread.getState());
				//Keeping track for how many threads have found the leader
				if (thread.getState() != Thread.State.TERMINATED)
					//Keeping track of how many threads have been signaled to begin the next round
					mySignal.add();
			}
			
			//If all the threads know the leader then set the completed flag to true
			if (this.mySignal.roundCompleted()) {
				this.completed = true;
			}
			
			//System.out.println(this.round+" round ended in main process");
			this.round++;
			//System.out.println(this.round+" round started in main process");
			this.semItr = semSet.iterator();
			while(this.semItr.hasNext())
			{
				Semaphore sem=this.semItr.next();
				sem.release();
			}
		}

		//If completed flag is true join all child threads to the main thread
		itr = processSet.iterator();
		// all the processes know the leader, terminating them
		while (itr.hasNext()) {
			itr.next().join();
		}
//		System.out.println("Parent thread exits in round "+ this.round);
		System.out.println("Parent thread exits");
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			spawnProcesses();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}