<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Androbotus - Main</title>
		<link rel="stylesheet" href="scripts/jgauge/css/jgauge.css" type="text/css" />
        <!--link rel="stylesheet" href="scripts/jgauge/css/page.css" type="text/css" /-->
		<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
		<script src="scripts/jqueryrotate/jQueryRotate.1.7.js"></script>
        <script src="scripts/jgauge/js/jgauge-0.3.0.a3.js"></script>

		<script type="text/javascript">
			//vars	
			//var attitude;
			var rollChart;
			var pitchChart;
			var yawChart;
			var flChart;
			var frChart;
			var rlChart;
            var rrChart;

			
			//emulates click on the button with given id		
			function keyBtnPressed(id){
			 	$('#' + id).click();
			}
			
			//sends new control value to the server. The value is obtained via corresponding button click
			function btnPressed(code) {
			  appendToLog(code);
			  
			  //post the control data to server
			  var controlJson = createControlJson(code);
			  $.post('http://localhost:8080/androbotus/webaccess', controlJson,
    				function(){appendToLog('succed');}
    		  );	
			}
			
			//append the given text string to the end of log stream
			function appendToLog(text){
				$('#log').append('\n' + text);
			  	$('#log').scrollTop($('#log')[0].scrollHeight);

			}
			
			//creates json object corresponding to a button code to be sent to the server as a new control value 			
			function createControlJson(code) {
				createControlJson(code, 0);
			}
			
			//creates json object corresponding to a button code to be sent to the server as a new control value 			
			function createControlJson(code, value){
				
				var type = 'DEFAULT';
				if (!value) {
					value = 0;
				}	
				if (code == 'Left'){
					//roll left
					type = 'ROLL';
					value = -1;
				} else if (code == 'Right' ) {
					//roll right
					type = 'ROLL';
					value = 1;
				} else if (code == 'Up') {
					//nose down
					type = 'PITCH';
					value = -1;
				} else if (code == 'Down') {
					//nose up
					type = 'PITCH';
					value = 1;
				} else if (code == 'W') {
					//thrust up
					type = 'THRUST';
					value = 1;
				} else if (code == 'S') {
					//thrust down
					type = 'THRUST';
					value = -1;
				} else if (code == 'A') {
					//yaw left
					type = 'YAW';
					value = -1;
				} else if (code == 'D') {
					//yaw right
					type = 'YAW';
					value = 1;
				} else {
				    //for all other cases send the same type as the code
				    type = code;
				}
				
				//var json = {type: "", controlValue: ""};
				var controlJson = '{"type":"' + type + '", "controlValue":"' + value + '"}';
			
				return controlJson;
			}
			
			//requests new sensor data from the server and displays it in the 'sensor' div
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
			
			//creates sensor div
			function createSensorDiv(name, value){
				return "<div>" + name + ": " + value + "</div>";
			}
			
			//hook up listener for the control inputs
			document.addEventListener('keydown', function(event) {
   			if(event.keyCode == 37) {
        			keyBtnPressed('leftBtn');
    			} else if(event.keyCode == 39) {
        			keyBtnPressed('rightBtn');
    			} else if(event.keyCode == 38) {
        			keyBtnPressed('upBtn');
    			} else if(event.keyCode == 40) {
        			keyBtnPressed('downBtn');
    			} else if (event.keyCode == 65) {
	    			btnPressed('A');
    			} else if (event.keyCode == 87) {
	    			btnPressed('W');
    			} else if (event.keyCode == 68) {
	    			btnPressed('D');
    			} else if (event.keyCode == 83) {
	    			btnPressed('S');
    			}
			});			
			
		//create a speedometer-like guage to display roll and pitch angles	
		function createRollPitchGuage(id, name, min, max, hasRange){
		    var g = new jGauge(); // Create a new jGauge.
            g.id = id;
            g.label.suffix = name; // Make the value label bytes.
            g.ticks.count = 5;
            g.ticks.start = min;
            g.ticks.end = max;
            g.needle.imagePath = 'scripts/jgauge/img/jgauge_needle_default.png';
            g.imagePath = 'scripts/jgauge/img/jgauge_face_default.png';
            g.range.color = 'rgba(34, 139, 34, 0.0)';
            g.label.precision = 0; // 0 decimals (whole numbers).
            if (hasRange){
                g.range.color = 'rgba(34, 139, 34, 0.2)';
            }
            g.range.start = -160;
            g.range.end = -20;

            g.range.thickness = 36;
            g.range.radius = 55;

            return g;
		}
		
		//request new roll/pitch/yaw values from the server to plot them on the guage 	
		function updateGuage(guage, min, max, value) {
            var val = 0;
            if (value){
                val = value;
            }
            if (val < min){
                val = min;
            }
            if (val > max){
                val = max;
            }
            guage.setValue(val);
		}

		//create guage for engines thrust. A single guage displays thrust level for 2 engines
		function createIndivThrust(id, name) {
		    var g = new jGauge(); // Create a new jGauge.
            g.id = id; // Link the new jGauge to the placeholder DIV.
            g.imagePath = 'scripts/jgauge/img/jgauge_face_taco.png';
            g.segmentStart = -225
            g.segmentEnd = 45
            g.width = 170;
            g.height = 170;
            g.needle.imagePath = 'scripts/jgauge/img/jgauge_needle_taco.png';
        	g.needle.xOffset = 0;
        	g.needle.yOffset = 0;
            g.label.yOffset = 55;
            g.label.color = '#fff';
            g.label.precision = 0; // 0 decimals (whole numbers).
        	g.label.suffix = name; // Make the value label watts.
        	g.ticks.labelRadius = 45;
            g.ticks.labelColor = '#0ce';
            g.ticks.start = 0;
            g.ticks.end = 100;
            g.ticks.count = 11;
            g.ticks.color = 'rgba(0, 0, 0, 0)';
            g.range.color = 'rgba(0, 0, 0, 0)';

            return g;
		}

	    var cnt = 0;
		//retreive and display video data
		//var frameCnt = 0;
		//var newImage = new Image();
		//var imageBase ="video.jpg";
		//newImage.src = imageBase;
		function refreshVideoFrame() {
		    $.ajax({
        	    type: "GET",
          		url: 'http://localhost:8080/androbotus/webaccess/video',
             	contentType: 'application/json; charset=utf-8',
        		dataType: 'json',
                success: function(json){
                    //the json contains base64 encoded image data
                    var data = json.data;
                    if (!data)
                        return;
                    $("video").src = "data:image/jpeg;base64,"+ data;
                }
            });
		}

		//bind parameter input boxes to listen the keyboard events
		function postParameter(code, element) {
	        console.log("changed " + element.val());
			//$(this).data("previousValue", $(this).val());
					
			var controlJson = createControlJson(code, element.val());
			//send new parameter
			$.post('http://localhost:8080/androbotus/webaccess',
				controlJson,
    			function(){
    			    appendToLog('Updated param:' + code + ":" + element.val());
    			}
    		);
		}

        var attitude = {};
		function updateAttitude() {
			$.ajax({
				type: "GET",
  		       	url: 'http://localhost:8080/androbotus/webaccess/attitude',
     	     	contentType: 'application/json; charset=utf-8',
				dataType: 'json',
         		success: function(json){
         	  		//if success then get the fresh attitude and update corresponding chart sections
         	  		attitude.attitudeMap = json.attitudeMap;
         		}
         	});

            if (attitude.attitudeMap){
       		    if (rollChart){
       			    updateGuage(rollChart, -90, 90, attitude.attitudeMap.SENSOR_ROLL);
       		    }
       		    if (pitchChart){
       			    updateGuage(pitchChart, -90, 90, attitude.attitudeMap.SENSOR_PITCH);
       		    }
       		    if (yawChart){
                    updateGuage(yawChart, -180, 180, attitude.attitudeMap.SENSOR_YAW);
                }
       		    if (flChart){
       			    updateGuage(flChart, 0, 100, attitude.attitudeMap.FL);
       		    }
       		    if (frChart){
       			    updateGuage(frChart, 0, 100, attitude.attitudeMap.FR);
       		    }
       		    if (rlChart){
       		        updateGuage(rlChart, 0, 100, attitude.attitudeMap.RL);
       		    }
       		    if (rrChart){
                    updateGuage(rrChart, 0, 100, attitude.attitudeMap.RR);
                }
       		}
       		delete attitude.attitudeMap;  //delete the reference explicitly
		}

		//create all the charts first
        var flChart = createIndivThrust('fl-thrust', 'FR');
        var frChart = createIndivThrust('fr-thrust', 'FL');
        var rlChart = createIndivThrust('rl-thrust', 'RL');
        var rrChart = createIndivThrust('rr-thrust', 'RR');
        var rollChart = createRollPitchGuage('roll', 'Roll', -90, 90, true);
        var pitchChart = createRollPitchGuage('pitch', 'Pitch', -90, 90, true);
        var yawChart = createRollPitchGuage('yaw', 'Yaw', -180, 180, false);

		$(document).ready(function() {
		    //init charts
		    flChart.init();
            frChart.init();
            rlChart.init();
            rrChart.init();
            rollChart.init();
            rollChart.setValue(0);
            pitchChart.init();
            pitchChart.setValue(0);
            yawChart.init();
            yawChart.setValue(0);

            //update rate is 25 frames per second
			setInterval(function(){updateAttitude();}, 40);

            //refresh image with 25 fps rate to make illusion of video. Poor mans solution though...
            setInterval(function(){refreshVideoFrame()}, 40);
				
            //bind param controls
            var pparam = $("#pparam");
            var dparam = $("#dparam");
            var iparam = $("#iparam");
            var imax = $("#imax");
            var rollcorr = $("#rollcorr");
            var pitchcorr = $("#pitchcorr");
            var yawcorr = $("#yawcorr");
            var gyroalpha = $("#gyroalpha");
			
            pparam.bind('click', function(){postParameter('PPARAM', pparam);});
            iparam.bind('click', function(){postParameter('IPARAM', iparam);});
            dparam.bind('click', function(){postParameter('DPARAM', dparam);});
            imax.bind('click', function(){postParameter('IMAX', imax);});
            rollcorr.bind('click', function(){postParameter('ROLL_CORR', rollcorr);});
            pitchcorr.bind('click', function(){postParameter('PITCH_CORR', pitchcorr);});
            yawcorr.bind('click', function(){postParameter('YAW_CORR', yawcorr);});
            gyroalpha.bind('click', function(){postParameter('LOW_PASS_GYRO', gyroalpha);});

            //init all the parameters
            //postParameter('PPARAM', pparam);
            //postParameter('IPARAM', iparam);
            //postParameter('DPARAM', dparam);
            //postParameter('IMAX', imax);
            //postParameter('ROLL_CORR', rollcorr);
            //postParameter('PITCH_CORR', pitchcorr);
            //postParameter('YAW_CORR', yawcorr);
            //postParameter('LOW_PASS_GYRO', gyroalpha);

		});
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
    	<div>
    		<table>
    			<tr>
    				<td>
    					<div class="video-container" style="width: 400px; height: 300px; margin: 0 auto">
      					<img id="video"></img>
    					</div>
    				</td>
    				<td>
    					<div id="roll" class="jgauge"></div>
    					<div id="pitch" class="jgauge"></div>
    					<div id="yaw" class="jgauge"></div>
    				</td>
    				<td>
    					<div>
    					    <div id="fl-thrust" class="jgauge"></div>
    					    <div id="fr-thrust" class="jgauge"></div>
    					</div>
                        <div>
                            <div id="rl-thrust" class="jgauge"></div>
                            <div id="rr-thrust" class="jgauge"></div>
                        </div>
    				</td>
    			</tr>
    		</table>
    	</div>
    	<div>
			<table>
			<tr>
				<td/>
				<td><INPUT id="upBtn" TYPE=BUTTON OnClick="btnPressed('Up');" VALUE="^"/></td>
				<td/>
			</tr>
			<tr>
				<td><INPUT id="leftBtn" TYPE=BUTTON OnClick="btnPressed('Left');" VALUE="<"/></td>
				<td><INPUT id="downBtn" TYPE=BUTTON OnClick="btnPressed('Down');" VALUE="v"/></td>
				<td><INPUT id="rightBtn" TYPE=BUTTON OnClick="btnPressed('Right');" VALUE=">"/></td>
			</tr>
			</table>			
			<div>	
				<div>
					P: <input id="pparam" type="number" min="0" max="1" step=".01" value=".1"/>  
					I: <input id="iparam" type="number" min="0" max="1" step=".01" value="0"/>
					D: <input id="dparam" type="number" min="0" max="1" step=".01" value=".1"/>
					IMAX: <input id="imax" type="number" min="0" max="100" value="0"/>
					Roll-Corr: <input id="rollcorr" type="number" min="0" max="1" step=".1" value=".1"/>  
					Pitch-Corr: <input id="pitchcorr" type="number" min="0" max="1" step=".1" value=".1"/>  
					Yaw-Corr: <input id="yawcorr" type="number" min="0" max="1" step=".1" value="0"/>
					Gyro-alpha: <input id="gyroalpha" type="number" min="0" max="1" step=".1" value="0.9"/>
				</div>
				<div><INPUT id="reset" TYPE=BUTTON OnClick="btnPressed('RESET');" VALUE="RESET"/></div>
				<span>
					<textarea id="log" lines="15" col="20">Output log</textarea>
				</span>	
				<span id="sensors">
				</span>
			</div>
		</div>
	</body>
</html>