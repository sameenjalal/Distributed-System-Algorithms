import java.net.*;

public class StadiumServer
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

		// Join a Multicast group and wait for the multicast message to start
		try {
			group = InetAddress.getByName( ADDR );
			socket = new MulticastSocket( PORT );

			// JOIN THE MULTICAST GROUP AND PREP DATAGRAM PACKET
			socket.joinGroup(group);

			byte[] buffer = new byte[ STARTMESG.length() ];
			DatagramPacket receive = new DatagramPacket( buffer, buffer.length );

			while ( true ) {
				socket.receive( receive );
				String str = new String( receive.getData() );
				str = str.toString();

				if( str.equals( STARTMESG ) ) {
					System.out.println( "RECEIVED: " + str  );
					break;
				}
			}
		}
		catch ( Exception e ) {
			System.out.println( "SOMETHING WENT WRONG!" );
		}

		
	}
}
