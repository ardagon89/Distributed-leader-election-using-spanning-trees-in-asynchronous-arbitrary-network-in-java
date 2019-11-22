//Message class for creating an object of Message type which can contain all required data
public class Message {
	String msg;
	int round;
	int parent;
	int root;
	int maxId;
	
	//Initializing the message object
	public Message(String msg, int round, int parent, int root, int maxId)
	{
		this.msg = msg;
		this.round = round;
		this.parent = parent;
		this.root = root;
		this.maxId = maxId;
	}
	
	//Printable format of the Message object
	public String toString()
	{
		return root+":"+parent+":"+msg+":"+round+":"+maxId;
	}
	
	//Method to set the current round in the message
	public Message setRound(int round)
	{
		this.round = round;
		return this;
	}

	//Method to set the current parent in the message
	public Message setParent(int processId)
	{
		this.parent = processId;
		return this;
	}

	//Method to set the current maxid in the message
	public Message setMaxId(int maxId)
	{
		this.maxId = maxId;
		return this;
	}
}
