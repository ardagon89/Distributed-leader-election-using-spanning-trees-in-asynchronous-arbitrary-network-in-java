import java.util.*;
import java.util.concurrent.Semaphore;

public class RingProcess implements Runnable {

	int processId;
	MySignal mySignal;
	private int loc;
	private MessagingSys myMsgSys;
	private int send;
	private double roundsLeft;
	private boolean completed;
	private Semaphore sem;
	private int round;
	private int numberOfProcesses;
	private int[][] parents;
	private HashMap<Integer, ArrayList<Integer> > children;
	private HashMap<Integer, ArrayList> maxId;
	private int neighbours;

	//Initialize the RingProcess object
	public RingProcess(IDs ids, MySignal mySignal, int loc, MessagingSys myMsgSys, Semaphore sem, int length) {
		this.processId = ids.getMyId(loc);
		this.mySignal = mySignal;
		this.loc = loc;
		this.myMsgSys = myMsgSys;
		this.send = this.processId;
		this.roundsLeft = Math.pow(2, this.processId);
		this.completed = false;
		this.sem = sem;
		this.round = 0;
		this.numberOfProcesses = length;
		//An array for saving the parents of the node for the given spanning tree
		this.parents = new int[length][2];
		for (int i = 0; i<length; i++){
			parents[i][0] = this.myMsgSys.getProcessId(i);
			parents[i][1] = -1;
		}
		//A Hasmap for saving the children of the node for the given spanning tree
		children = new HashMap<>();
		for (int i = 0; i<length; i++){
			children.put(this.myMsgSys.getProcessId(i), new ArrayList());
		}
		//A Hasmap for saving the maxids of the node recieved from convergecast for the given spanning tree
		maxId = new HashMap<>();
		for (int i = 0; i<length; i++){
			maxId.put(this.myMsgSys.getProcessId(i), new ArrayList());
		}
		this.neighbours = this.myMsgSys.findNeighbours(this.loc);
	}

	@Override
	public void run() {
		// run indefinite loop until I find a leader or 100 iterations whichever earlier
		while (!this.completed || this.round < 100) {
			try {
				this.sem.acquire();
				round++;
				//System.out.println(this.round+" round started in "+this.processId);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Initiate a tree with me as root
			if(this.round == 1) {
				for (int i = 0; i < numberOfProcesses; i++) {
					myMsgSys.put(this.loc, i, new Message("search", this.round, this.processId, this.processId, -1));
				}
			}

			// read message from neighbor
			Message msgNeighbor = null;
			for (int i=0; i<numberOfProcesses;i++) {

				msgNeighbor = myMsgSys.get(i, this.loc, this.round);
				// if there is a message I process
				while (msgNeighbor != null ) {
					// received id is smaller than my id
					String messageContent = msgNeighbor.msg;
					int messageRound = msgNeighbor.round;
					int messageRoot = msgNeighbor.root;
					int messageParent = msgNeighbor.parent;

					//For each search message send an ack or a nack
					if (messageContent == "search") {
						//Send a nack if the root recieves the search message
						if(messageRoot == this.processId)
						{
							myMsgSys.put(this.loc, this.myMsgSys.getIndex(messageParent), new Message("nack", this.round, this.processId, messageRoot, -1));
						}
						else
						{
							//Send an ack if this is the first message for the given spanning tree
							if (parents[this.myMsgSys.getIndex(messageRoot)][1] == -1) 
							{
								parents[this.myMsgSys.getIndex(messageRoot)][1] = messageParent;
								myMsgSys.put(this.loc, this.myMsgSys.getIndex(messageParent), new Message("ack", this.round, this.processId, messageRoot, -1));
								for (int j = 0; j < numberOfProcesses; j++) {
									myMsgSys.put(this.loc, j, new Message("search", this.round, this.processId, messageRoot, -1));
								}
							} 
							//For all subsequent messages send nack
							else 
							{
								myMsgSys.put(this.loc, this.myMsgSys.getIndex(messageParent), new Message("nack", this.round, this.processId, messageRoot, -1));
							}
						}
					}
					//Record for each neighbor whether you got an ack or nack
					else if (messageContent == "ack"){
						children.get(messageRoot).add(messageParent);
					}
					//Record for each neighbor whether you got an ack or nack
					else if (messageContent == "nack"){
						children.get(messageRoot).add(-1);
					}
					//Record the maxId received by convergecast
					else if (messageContent == "convergecast"){
						maxId.get(messageRoot).add(msgNeighbor.maxId);
					}

					//If I received a reply from all my neighbors
					if(children.get(messageRoot).size()==this.neighbours)
					{
						ArrayList ls = children.get(messageRoot);
						boolean result = true;
						int noofchildren = 0;
						for(int k=0; k<ls.size(); k++)
						{
							if(!ls.get(k).equals(-1))
							{
								result=false;
								noofchildren++;
							}
						}

						//If I a leaf node convergecast my id
						if(result)
						{
							myMsgSys.put(this.loc, this.myMsgSys.getIndex(this.parents[this.myMsgSys.getIndex(messageRoot)][1]), new Message("convergecast", this.round, this.processId, messageRoot, this.processId));
						}
						//Else convergecast max Id out of myself and all the children
						else
						{

						ArrayList mi = maxId.get(messageRoot);
						if(noofchildren == mi.size())
						{
							int tempmaxId = -1;
							for(int l=0; l<mi.size(); l++)
							{
								if((int) mi.get(l) > tempmaxId)
								{
									tempmaxId = (int) mi.get(l);
								}
							}

							//If I am the root of this tree
							if(messageRoot==this.processId)
							{
								//And my Id is greatest then output leader
								if(tempmaxId<this.processId)
								{
									System.out.println("I am the LEADER. My processId is "+ this.processId);
									this.completed = true;
								}
								//Otherwise output the ID of my leader
								else
								{
									System.out.println("My processId is "+ this.processId+" and my leader is "+ tempmaxId);
									this.completed = true;
								}
							}
							else
							{
								//If I am not the root then convergecast max ID out of me and all my children
								if(tempmaxId < this.processId)
								{
									tempmaxId = this.processId;
								}
								myMsgSys.put(this.loc, this.myMsgSys.getIndex(this.parents[this.myMsgSys.getIndex(messageRoot)][1]), new Message("convergecast", this.round, this.processId, messageRoot, tempmaxId));
							}
						}
						}
					}
					msgNeighbor = myMsgSys.get(i, this.loc, this.round);
				}
			}
			
			//System.out.println(this.round+" round ended in "+this.processId);
			// Work of current round completed, so reducing the number of working threads
			mySignal.sub();
		}
		// I have found the leader so joining parent thread.
		System.out.println("Joining parent thread. My id is " + this.processId);
	}
}