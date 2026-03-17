/*
* hjStreamServer.java 
* Streaming server: streams video frames in UDP packets
* for clients to play in real time the transmitted movies
* 
* Enhanced with encryption support:
* - Challenge 1: AES-GCM encryption
* - Challenge 2: ChaCha20-Poly1305 encryption
* - Challenge 3: DPRG-based stream cipher
*/

import java.io.*;
import java.net.*;

class hjStreamServer {

	static public void main(String[] args) throws Exception {
		if (args.length < 3 || args.length > 4) {
			System.out.println("Usage: mySend <movie> <ip-multicast-address> <port> [config-file]");
			System.out.println("   or: mySend <movie> <ip-unicast-address> <port> [config-file]");
			System.out.println("Optional config-file enables encryption (default: config.properties)");
			System.exit(-1);
		}

		int size;
		int csize = 0;
		int count = 0;
		long time;
		DataInputStream g = new DataInputStream(new FileInputStream(args[0]));
		byte[] buff = new byte[4096];

		// Load encryption (optional)
		String configFile = args.length == 4 ? args[3] : "config.properties";
		StreamEncryption encryption = null;
		try {
			encryption = EncryptionFactory.createFromConfig(configFile);
		} catch (Exception e) {
			System.err.println("Warning: Could not load encryption from " + configFile);
			System.err.println("Continuing without encryption...");
		}

		DatagramSocket s = new DatagramSocket();
		InetSocketAddress addr = new InetSocketAddress(args[1], Integer.parseInt(args[2]));
		DatagramPacket p = new DatagramPacket(buff, buff.length, addr);
		long t0 = System.nanoTime(); // Ref. time
		long q0 = 0;

		// Movies are encoded in .dat files, where each
		// frame is encoded in a real-time sequence of MP4 frames
		// Somewhat an FFMPEG4 playing scheme .. Dont worry

		// Each frame has:
		// Short size || Long Timestamp || byte[] EncodedMP4Frame
		// You can read (frame by frame to transmit ...
		// But you must folow the "real-time" encoding conditions

		while ((size = g.readShort()) > 0) { // read frame size
			time = g.readLong(); // read timestamp
			g.readFully(buff, 0, size);
			count++;
			csize += size;

			// Encrypt frame if encryption is enabled
			byte[] frameData = buff;
			int frameSize = size;
			if (encryption != null) {
				try {
					byte[] plaintext = new byte[size];
					System.arraycopy(buff, 0, plaintext, 0, size);
					frameData = encryption.encrypt(plaintext);
					frameSize = frameData.length;
				} catch (Exception e) {
					System.err.println("Encryption error: " + e.getMessage());
					System.exit(-1);
				}
			}

			p.setData(frameData, 0, frameSize);
			p.setSocketAddress(addr);

			long t = System.nanoTime(); // what time is it?

			// Decision about the right time to transmit
			Thread.sleep(Math.max(0, ((time - q0) - (t - t0)) / 1000000));

			// send datagram (udp packet) w/ payload frame)
			s.send(p);

			// Just for awareness ... (debug)

			System.out.print(":");
		}

		long tend = System.nanoTime(); // "The end" time
		System.out.println();
		System.out.println("DONE! all frames sent: " + count);

		long duration = (tend - t0) / 1000000000;
		System.out.println("Movie duration " + duration + " s");
		System.out.println("Throughput " + count / duration + " fps");
		System.out.println("Throughput " + (8 * (csize) / duration) / 1000 + " Kbps");

	}
}
