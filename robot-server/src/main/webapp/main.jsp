<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Androbotus - Main</title>
		<script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
		<script type="text/javascript">
					
			function keyBtnPressed(id){
			 	$('#' + id).click();
			}
			
			function btnPressed(code, id) {
			  appendToLog(code);
			  
			  //post the control data to server
			  var controlJson = createControlJson(code);
			  $.post(
			  	'http://localhost:8080/androbotus/webaccess', 
			  	controlJson,
    			function(){appendToLog('succed');}
    		  );	
			}
			
			function appendToLog(text){
				$('#log').append('\n' + text);
			  	$('#log').scrollTop($('#log')[0].scrollHeight);

			}
			
			function createControlJson(code){
				
				var type = 'DEFAULT';
				var value = 0;
				if (code == 'Left'){
					//left
					type = 'STEERING';
					value = -1;
				} else if (code == 'Right' ) {
					//right
					type = 'STEERING';
					value = 1;
				} else if (code == 'Up') {
					//up
					type = 'ACCELERATION';
					value = 1;
				} else if (code == 'Down') {
					//down
					type = 'ACCELERATION';
					value = -1;
				}
				//var json = {type: "", controlValue: ""};
				var controlJson = '{"type":"' + type + '", "controlValue":"' + value + '"}';
			
				return controlJson;
			}
			
			function updateSensors(){
				//request server to get new sensor data
				$.ajax({
                	type: "GET",
                	url: 'http://localhost:8080/androbotus/webaccess',
                	contentType: 'application/json; charset=utf-8',
                    dataType: 'json',
                	success: function(json){
                		$("#sensors").html("");
                		for (var i = 0; i < json.sensors.length; i++){
                			var sensor = json.sensors[i];
                			var sensorDiv = createSensorDiv(sensor.name, sensor.value);
                			$("#sensors").append(sensorDiv); 
                		}
                	} 
            	});
			}
			
			function createSensorDiv(name, value){
				return "<div>" + name + ": " + value + "</div>";
			}
			
			document.addEventListener('keydown', function(event) {
   				if(event.keyCode == 37) {
        			keyBtnPressed('leftBtn');
    			} else if(event.keyCode == 39) {
        			keyBtnPressed('rightBtn');
    			} else if(event.keyCode == 38) {
        			keyBtnPressed('upBtn');
    			} else if(event.keyCode == 40) {
        			keyBtnPressed('downBtn');
    			} 
			});
			
			var cnt = 0;
			setInterval(function(){updateSensors()},30);
			
			var frameCnt = 0;
			var newImage = new Image();
			var imageBase ="video.jpg"; 
			newImage.src = imageBase;
			function refreshVideoFrame() {
				if(newImage.complete) {
    				document.getElementById("video").src = newImage.src;
    				newImage = new Image();
    				newImage.src = 'http://localhost:8080/androbotus/webvideo' + "#" + frameCnt;
        			cnt++;
				}
			}
			//refresh image with 30 fps rate to make illusion of video. Poor man's solution though...
			setInterval(function(){refreshVideoFrame()}, 30);
		</script>
		<style>
      		video {
        		width: 320px;
        		height: 240px;
        		border: 1px solid black;
      		}
      
      		.video-container {
        		width: 320px;
        		height: 240px;
        		display: inline-block;
        		text-align: center;
      		}
	    </style>
	</head>
	<body>
		<h1>The Androbotus Controller</h1>
		<!-- div class="video-container">
      		<video poster="http://www.html5rocks.com/en/tutorials/video/basics/star.png" controls>
        		<source src="http://www.html5rocks.com/en/tutorials/video/basics/Chrome_ImF.ogv" type='video/ogg; codecs="theora, vorbis"' />
      		</video>
    	</div -->
    	
    	<div class="video-container">
      		<img id="video"></img>
    	</div>
    	<div>
			<table>
			<tr>
				<td/>
				<td><INPUT id="upBtn" TYPE=BUTTON OnClick="btnPressed('Up', 'upBtn');" VALUE="^"/></td>
				<td/>
			</tr>
			<tr>
				<td><INPUT id="leftBtn" TYPE=BUTTON OnClick="btnPressed('Left', 'leftBtn');" VALUE="<"/></td>
				<td><INPUT id="downBtn" TYPE=BUTTON OnClick="btnPressed('Down', 'downBtn');" VALUE="v"/></td>
				<td><INPUT id="rightBtn" TYPE=BUTTON OnClick="btnPressed('Right','rightBtn');" VALUE=">"/></td>
			</tr>
			</table>			
			<div>
				<span>
					<textarea id="log" lines="15" col="20">Output log</textarea>
				</span>	
				<span id="sensors">
				</span>
			</div>	
		</div>
	</body>
</html>