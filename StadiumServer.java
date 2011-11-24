import java.net.*;
import java.util.*;
import java.io.*;

public class StadiumServer
{
	// Defining the static port and address variables
	private static final String ADDR = "228.5.6.7";
	private static final String STARTMESG = "Start";
	private static final String GROUPIE_MESG = "I'm looking for a group!";
	private static final String MEMBER_MESG = "You are in my group!";
	private static final int MULTICAST_PORT = 6666;
	private static final int MAX_SILENCE_COUNT = 36;
	protected static boolean stop = false;

	// My Info
	private static String hostname = null;

	// Net Info
	private static HashSet<String> groupies = new HashSet<String>();
	private static String leader = null;

	public static void main( String[] args )
	{
		InetAddress multicastGroup;
		MulticastSocket multicastSocket;

		hostname = getHostname();
		leader = hostname;

		// Join a Multicast group and wait for the multicast message to start
		try {
			multicastGroup = InetAddress.getByName( ADDR );
			multicastSocket = new MulticastSocket( MULTICAST_PORT );

			// JOIN THE MULTICAST GROUP AND PREP DATAGRAM PACKET
			multicastSocket.joinGroup(multicastGroup);
			multicastSocket.setLoopbackMode(true);

			multicastSocket.setSoTimeout(100);

			byte[] buffer = new byte[ STARTMESG.length() ];
			DatagramPacket received = new DatagramPacket( buffer, buffer.length );


			while ( true ) {
				try {
					multicastSocket.receive( received );
					String str = new String( received.getData() );

					if( str.equals( STARTMESG ) ) {
						//System.out.println( "RECEIVED: " + str  );
						groupies.add( hostname );
						break;
					}
				} catch (SocketTimeoutException e) {
					//we don't really care.
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
			return;
		}

		//represents how many times we've sent a message without receiving a response
		int silenceCount = 0;

		//You only get here if you received a Start message.
		while( silenceCount < MAX_SILENCE_COUNT )
		{
			if(leader == null)
				System.out.println("I AM SO STUPID!");
			// If I am the leader, send out a needGroupies message
			if( leader.equals( hostname ) ) {
				sendWantGroup(multicastSocket, multicastGroup);
			}
			if(!listenForResponses(multicastSocket, multicastGroup)) {
				silenceCount++;
			} else {
				silenceCount = 0 ;
			}

			//System.out.printf("Silence Count = %d \n", silenceCount);
		}

		System.out.println( "The number of machines in the stadium network is: " + groupies.size() );
	}

	public static void sendWantGroup(MulticastSocket socket, InetAddress multicastGroup)
	{
		DatagramPacket needGroupies = null;
		try {
			String myGroupies = serialize(groupies);
			String message = GROUPIE_MESG  + myGroupies;
			needGroupies = new DatagramPacket( message.getBytes(), message.length(), multicastGroup, MULTICAST_PORT );
			socket.send( needGroupies );
		}
		catch( Exception e ) {
			//System.out.println( "Problem sending wantGroup message" );
		}
	}

	private static void sendAddedToGroup(String machine, MulticastSocket socket, InetAddress multicastGroup) {
		DatagramPacket packet = null;
		try {
			String myGroupies = serialize(groupies);
			String message = machine + MEMBER_MESG  + myGroupies;
			packet = new DatagramPacket( message.getBytes(), message.length(), multicastGroup, MULTICAST_PORT );
			socket.send( packet );
		}
		catch( Exception e ) {
			System.out.println( "Problem sending addedToGroup message" );
		}
	}


	public static boolean listenForResponses(MulticastSocket socket, InetAddress multicastGroup)
	{
		boolean rv = false;
		//System.out.println("Listening ");
		try {
			byte[] buffer = new byte[10*1024];
			DatagramPacket data = new DatagramPacket(buffer, buffer.length);
			socket.receive(data);
			String message = new String(buffer, 0, data.getLength());

			rv = !(message.equals(""));
				

			//System.out.println("Received the message "+message);

			if(isWantGroupMessage(message) && iAmLeader()) {
				groupies.addAll(extractData(message, GROUPIE_MESG));
				for(String machine: groupies) {
					if(!machine.equals(hostname)) {
						//send message to machine: I added you to my group. Here's my group members.
						sendAddedToGroup(machine, socket, multicastGroup);
					}
				}

				leader = elect(groupies);

				//System.out.printf("My groupies are %s. My leader is %s \n", serialize(groupies), leader);
			}

			//This is actually a unicast
			if(isAddedToGroupMessage(message)) {
				groupies.addAll(extractData(message, hostname+MEMBER_MESG));

				//System.out.printf("My groupies are %s \n", serialize(groupies));
			}
		} catch(SocketTimeoutException e) {
			//we don't really care.
		} catch(Exception e) {
			e.printStackTrace();
		}
		return rv;
	}

	private static boolean isWantGroupMessage(String message) {
		return message.startsWith(GROUPIE_MESG);
	}

	private static boolean isAddedToGroupMessage(String message) {
		return message.startsWith(hostname + MEMBER_MESG);
	}

	private static boolean iAmLeader() {
		return leader.equals(hostname);
	}

	public static HashSet<String> extractData(String packetData, String prefix) 
	{
		//chop off the first -prefix.length- characters.
		String serialized = packetData.substring(prefix.length());

		return unserialize(serialized);
	}

	public static String serialize(HashSet<String> set) {
		String result = "";

		for(String elem: set)
			result += elem + ",";
		return result;
	}

	public static HashSet<String> unserialize(String str) {

		HashSet<String> result = new HashSet<String>();

		for(String elem: str.split(","))
			if(elem.length() > 0)
				result.add(elem);

		return result;
	}

	public static String elect(HashSet<String> contenders) {

		Iterator<String> it = contenders.iterator();
		return it.next();
	}

	public static String getHostname() {
		String hostname = "";
		try {
			InetAddress addr = InetAddress.getLocalHost();

			// Get IP Address
			byte[] ipAddr = addr.getAddress();

			// Get hostname
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
		}
		return hostname.substring(0, hostname.indexOf(".rutgers.edu"));
	}
}
