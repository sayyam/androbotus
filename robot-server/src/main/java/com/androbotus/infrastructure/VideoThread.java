/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.androbotus.infrastructure;


/**
 * Implements video thread that acquires video frames via datagram socket and assembles a video stream 
 * @author maximlukichev
 *
 */
public class VideoThread {
/*	private final static String STREAM_URL = "rtmp://localhost/video/";
	private final static int FRAMES_TO_ENCODE = Integer.MAX_VALUE;
	private final static int X = 0;
	private final static int Y = 0;
	private final static int HEIGHT = 480;
	private final static int WIDTH = 640;
	
	private DatagramSocket socket;
	private MessageHandler mh;
	private String streamName;
	
	private boolean started;
	private boolean stopped;
	private IStreamCoder coder;
	private IContainer container;
	
	//technical temp fields
	private long startTime = -1;
	private long endTime = -1;
	private IRational frameRate;
	
	public VideoThread(String streamName, DatagramSocket socket, MessageHandler mh) throws Exception {
		this.socket = socket;
		this.mh = mh;
		this.streamName = streamName;
	}
	
	private void init() throws Exception {
		//setup container format
	    IContainerFormat containerFormatLive = IContainerFormat.make();
	    String streamFullName = STREAM_URL + streamName;
	    containerFormatLive.setOutputFormat("flv", streamFullName, null);
	    
	    //create container
	    container = IContainer.make();
	    container.setInputBufferLength(0);
	    int retVal = container.open(streamFullName, IContainer.Type.WRITE, containerFormatLive);
	    if (retVal < 0) {
	    	throw new Exception("Could not open output container for live stream");
	    }
	    IStream stream = container.addNewStream(0);
	    
	    
	    //define coder
	    coder = stream.getStreamCoder();
	    coder.setNumPicturesInGroupOfPictures(5);
	    ICodec codec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_H264);
	    coder.setCodec(codec);
	    coder.setBitRate(200000);
	    coder.setPixelType(Type.YUV420P);
	    coder.setHeight(HEIGHT);
	    coder.setWidth(WIDTH);
	    coder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
	    coder.setGlobalQuality(0);
	    
	    frameRate = IRational.make(5, 1);
	    coder.setFrameRate(frameRate);
	    coder.setTimeBase(IRational.make(frameRate.getDenominator(), frameRate.getNumerator()));
	    Properties props = new Properties();
	    InputStream is = VideoThread.class.getResourceAsStream("/libx264-normal.ffpreset");
	    props.load(is);
	    Configuration.configure(props, coder);
	    coder.open();
	    container.writeHeader();
	    
	    //start counting time
	    startTime = System.currentTimeMillis();
	}	
	
	public void start() throws Exception {
		if (!started){
			init();
		}	
		started = true;
		
	}	
	
	public void stop(){
		started = false;
		if (container != null){
			container.writeTrailer();
		}
	}	
	
	public void run() {
		boolean isFirstFrame = true;
		long startTime = System.currentTimeMillis();
		while (true) {
			
			if (!started){
				//wait till the thread is actually started
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}	
			try {
				//long now = System.currentTimeMillis();
				CameraMessage m = mh.receiveMessage(socket);
				//if (!(m instanceof CameraMessage)){
				//	throw new Exception("Unsupported message type: " + m.getClass().getName());
				//}
				
				CameraMessage cm = (CameraMessage)m;
				processFrame(cm, isFirstFrame, startTime);
				isFirstFrame = false;
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	private void processFrame(CameraMessage frame, boolean isFirst, long startTime) throws Exception {
		//grab the video frame
		byte[] data = frame.getData();
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
		//convert it for Xuggler
		BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		convertedImage.getGraphics().drawImage(image, 0, 0, null);
		
		//start the encoding process
		IPacket packet = IPacket.make();
		IConverter converter = ConverterFactory.createConverter(convertedImage, Type.YUV420P);
		//set how much time passed since the start of streaming
		long timeStamp = (System.currentTimeMillis() - startTime) * 1000; 
		IVideoPicture outFrame = converter.toPicture(convertedImage, timeStamp);
		if (isFirst) {
			//make first frame keyframe
			outFrame.setKeyFrame(true);
		}
		outFrame.setQuality(0);
		coder.encodeVideo(packet, outFrame, 0);
		outFrame.delete();
		if (packet.isComplete()) {
			container.writePacket(packet);
			//set the time when the encoding of a frame is done
			endTime = timeStamp;
		}
	}*/
}
