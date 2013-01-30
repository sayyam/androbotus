package com.androbotus;


public class VideoThread2IT {
	
	/*
	@Test
	public void testVideoCaptureServlet() {
		try {
			Robot robot = new Robot();
			MessageHandler mh = new UDPMessageHandlerImpl();
			DatagramSocket socket = new DatagramSocket();
			
			// make a stream of 1000 frames
			for (int i = 0; i < 1000000; i++) {
				BufferedImage image = robot.createScreenCapture(new Rectangle(
						0, 0, 320, 200));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(image, "jpeg", bos);
				bos.flush();
				byte[] data = bos.toByteArray();
				bos.close();
				CameraMessage cm = new CameraMessage();
				cm.setData(data);
				cm.setFrameNum(i);
				
				mh.sendMessage(InetAddress.getLocalHost(), VideoServlet.VIDEO_PORT, socket, cm);
				System.out.println("Image sent..");
				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
