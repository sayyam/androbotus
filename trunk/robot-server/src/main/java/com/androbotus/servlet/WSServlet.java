/**
 *  This file is part of Androbotus project.
 *
 *  Androbotus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Androbotus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Androbotus.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.androbotus.servlet;


/**
 * The main servlet that translates sensors data to web ui
 * @author maximlukichev
 *
 */
public class WSServlet {/*extends WebSocketServlet {

	private static final long serialVersionUID = 5279825994434790154L;

	@Override
	protected StreamInbound createWebSocketInbound(String subProtocol, HttpServletRequest request) {
		return new SensorInboud();
	}
	
	private static class SensorInboud extends MessageInbound {
		private WsOutbound outbound;
		private int msgCount = 0;
		private Thread runningThread;
		
		@Override
		protected void onOpen(WsOutbound outbound) {
			this.outbound = outbound;
			Thread t = new Thread(new Runnable() {
				
				public void run() {
					 try {
						while (true) {
							//send message every second
							sendMessageToClient();
							Thread.sleep(1000);
						}
					 } catch (Exception e){
						 e.printStackTrace();
					 }
				}
			});
			this.runningThread = t;
			t.start();
		}
		
		private void sendMessageToClient() throws IOException {
			CharBuffer cb =CharBuffer.wrap("Message " + msgCount++);
			outbound.writeTextMessage(cb);
		}
		
		@Override
		protected void onClose(int status) {
			if (runningThread.isAlive()){
				runningThread.interrupt();
			}
			this.outbound = null;
		}
		
		@Override
		protected void onTextMessage(CharBuffer message) throws IOException {
			System.out.println("Message from client: " + message.toString());
		}
		
		@Override
		protected void onBinaryMessage(ByteBuffer message) throws IOException {
			//Do nothing
		}
	}*/
	
}
