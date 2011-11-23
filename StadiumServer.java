import java.net.*;
import java.util.*;

public class StadiumServer
{
	// Defining the static port and address variables
	private static final String ADDR = "228.5.6.7";
	private static final String STARTMESG = "Start";
	private static final String GROUPIE_MESG = "I'm looking for a group!";
	private static final String MEMBER_MESG = "I am in your group!";
	private static final String ELECTION_MESG = "Fight the power!";
	private static final String UPDATE = "Update";
	private static final int PORT = 6666;

	// My Info
	private static String hostname = null;

	// Net Info
	private static Set<String> groupies = new Set<String>();
	private static String leader = null;

	public static void updateGroupiesAndLeader( ArrayList<String> newGroupies, String newLeader )
	{
		groupies = newGroupies;
		leader = newLeader;
	}

	public static void updateAllMyGroupiesAsLeader()
	{
		// TODO: I want to iterate through all my groupies and update their net info
	}

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
					hostname = System.getenv( "HOSTNAME" );
					groupies.add( hostname );
					leader = hostname;
					break;
				}
			}
		}
		catch ( Exception e ) {
			System.out.println( "SOMETHING WENT WRONG!" );
			return;
		}

		while( true )
		{
			// If I am the leader, send out a needGroupies message
			if( leader.equals( hostname ) ) {
				try {
					String myGroupies = groupies.serialize();
					String message = GROUPIE_MESSAGE  + myGroupies;
					DatagramPacket needGroupies = new DatagramPacket( message.getBytes(), message.length(), message, PORT );
					socket.send( needGroupies );
				}
				catch( Exception e ) {
					System.out.println( "Problem sending needGroupies message" );
				}

				try {
					byte[] buffer = new byte[ GROUPIE_MESG.length() + 1024 ];
					DatagramPacket getGroupieMesg = new DatagramPacket( buffer, buffer.length );

					boolean sad = true;

					// Check for groupie messages for 5 secods, if nothing assume you are alone in the current pairing
					for( int i = 0 ; i < 5 ; i++ ) {
						socket.receive( getGroupieMesg );
						String str = new String( getGroupieMesg.getData() );

						String sender = (InetAddress)(needGroupies.getSocketAddress()).getHostname();

						//we found a group.
						if( str.startsWith( GROUPIE_MESG ) ) {
							groupies.add(sender);
							sad = false;
							break;
						}
						else if(str.startsWith (MEMBER_MESG) ) {
							groupies.add(sender);
							sad = false;
						}
						else {
							Thread.sleep( 1000 );
						}
					}
					
					//No one wants to be friends so let's die.
					if(sad) {
						System.out.println(groupies.size());
						System.exit(0);
					}
				}
				catch( Exception e ) {
					System.out.println( "Something went wrong with receiving a groupie message" );
				}
			}
		}

		// TODO: If leader, send out pair message. Mesh groupie lists.
		// TODO: Elect a leader, leader keeps count of how many are in your sub network
		// TODO: Check if there is at least another person in your network. If not, return count
		// TODO: Leaders continue partnering up
	}
}
