import java.net.*;

public class StartStadiumCounter
{
	// Defining the static port and address variables
	private static final String ADDR = "228.5.6.7";
	// private static final String STARTMESG = "Let's get ready to rumble!";
	private static final String STARTMESG = "Let's get ready to RUMBLE!";
	private static final int PORT = 6666;

	public static void main( String[] args )
	{
		InetAddress group;
		MulticastSocket socket;

		// Join a Multicast group and send the group the start message
		try {
			group = InetAddress.getByName( ADDR );
			socket = new MulticastSocket( PORT );

			// JOIN THE MULTICAST GROUP AND PREP DATAGRAM PACKET
			socket.joinGroup(group);
			DatagramPacket start = new DatagramPacket( STARTMESG.getBytes(), STARTMESG.length(), group, PORT );

			socket.send( start );
			System.out.println( "SENT: " + STARTMESG );
		}
		catch ( Exception e ) {
			System.out.println( "SOMETHING WENT WRONG!" );
		}
	}
}
