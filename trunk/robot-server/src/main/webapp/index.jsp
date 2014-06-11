<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Androbotus - Main</title>
		
		<link rel="stylesheet" href="scripts/codemirror/lib/codemirror.css">
		<link rel="stylesheet" href="scripts/codemirror/addon/hint/show-hint.css">
		<link rel="stylesheet" href="scripts/jgauge/css/jgauge.css" type="text/css" />
        <!--link rel="stylesheet" href="scripts/jgauge/css/page.css" type="text/css" /-->
        
		<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
		<script src="scripts/jqueryrotate/jQueryRotate.1.7.js"></script>
        <script src="scripts/jgauge/js/jgauge-0.3.0.a3.js"></script>
		<script src="scripts/codemirror/lib/codemirror.js"></script>
		<script src="scripts/codemirror/addon/hint/show-hint.js"></script>
		<script src="scripts/codemirror/addon/hint/javascript-hint.js"></script>
		<script src="scripts/codemirror/mode/javascript/javascript.js"></script>
		
		<script type="text/javascript">
			//this is is a switch to turn on/off keybord inputs when typing in a script
			var editorEnabled = false;
						
			//vars	
			//var attitude;
			var rollChart;
			var pitchChart;
			var yawChart;
			var flChart;
			var frChart;
			var rlChart;
            var rrChart;

            //code mirror variable
            var editor;
			function executeBtnPressed(start){
                if (start && !editorEnabled){
                    //editor is not in 'edit' mode, so just exit
                    return;
                }
                executeScript(start);
			}

            function enableEditor(enabled){
                editorEnabled = enabled;
                if (editorEnabled) {
                    $('#edit_btn').attr("value", "CANCEL");
                    $('#run_btn').removeAttr("disabled");
                } else {
                    $('#edit_btn').attr("value", "EDIT");
                    $('#run_btn').attr("disabled", "true");
                }
            }

            function executeScript(start){
                //-1 means stop current script. 1 means start new script
                var value = '-1';
                if (start){
                    value = '1';
                }

                $('#code_action').attr('value',value);
                $('#code_value').attr('value',editor.getValue());
                $.ajax({
                    url: 'http://localhost:8080/androbotus/webaccess',
                    type: 'POST',
                    data: $('#code_form').serialize(),
                    dataType: 'x-www-form-urlencoded',
                    success: function(data) {
                                appendToLog("script action: " + value);
                            }
                });
                $('#code_value').attr('value',"");
            }

            //emulates click on the button with given id
			function keyBtnPressed(id){
			 	$('#' + id).click();
			}
			
			//sends new control value to the server. The value is obtained via corresponding button click
			function btnPressed(code) {
			    //if editor enabled - button actions are not allowed
                if (editorEnabled){
                    return;
                }
			  	
                appendToLog(code);
			  
                //post the control data to server
                var controlJson = createControlJson(code);
                sendControlData(controlJson);
			}

			function sendControlData(controlJson){
                $('#payload').attr('value',controlJson);
                $.ajax({
                    url: 'http://localhost:8080/androbotus/webaccess',
                    type: 'POST',
                    data: $('#control_form').serialize(),
                    dataType: 'x-www-form-urlencoded',
                    success: function(data) {appendToLog("success");}
                });
                $('#payload').attr('value',"");
			}
			
			function sendElementValue(code, element) {
	        	console.log("changed " + element.val());					
				var controlJson = createControlJson(code, element.val());
				sendControlData(controlJson);
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
					type = 'ROLL_BURST';
					value = -1;
				} else if (code == 'Right' ) {
					//roll right
					type = 'ROLL_BURST';
					value = 1;
				} else if (code == 'Up') {
					//nose down
					type = 'PITCH_BURST';
					value = -1;
				} else if (code == 'Down') {
					//nose up
					type = 'PITCH_BURST';
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
					type = 'YAW_BURST';
					value = -1;
				} else if (code == 'D') {
					//yaw right
					type = 'YAW_BURST';
					value = 1;
				} else if (code == 'trimLeft') {
					//trim roll left
					type = 'ROLL';
					value = -1;
				} else if (code == 'trimRight') {
					//trim roll right
					type = 'ROLL';
					value = 1;
				} else if (code == 'trimForward') {
					//trim pitch forward: nose down
					type = 'PITCH';
					value = 1;
				} else if (code == 'trimBackward') {
					//trim pitch backward: nose up
					type = 'PITCH';
					value = -1;
				} else if (code == 'trimYawLeft') {
					//trim yaw left
					type = 'YAW';
					value = -1;
				} else if (code == 'trimYawRight') {
					//trim yaw right
					type = 'YAW';
					value = 1;
				} 
				else {
				    //for all other cases send the same type as the code
				    type = code;
				}
				
				//var json = {type: "", controlValue: ""};
				var controlJson = '{"type":"' + type + '", "data":{"value":"' + value + '"}}';
			
				return controlJson;
			}


            function createScriptControlJson(type, value, script){
                var controlJson = '{"type":"' + type + '", "data":{"value":"' + value + '", "script":"'+ script +'"}}';
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
    			} else if (event.keyCode == 80) {
    				//P
	    			btnPressed('trimForward');
    			} else if (event.keyCode == 186) {
    				//;
	    			btnPressed('trimBackward');
    			} else if (event.keyCode == 76) {
    				//L
	    			btnPressed('trimLeft');
    			} else if (event.keyCode == 222) {
    				//'
	    			btnPressed('trimRight');
    			} else if (event.keyCode == 188) {
    				//<
	    			btnPressed('trimYawLeft');
    			} else if (event.keyCode == 188) {
    				//>
	    			btnPressed('trimYawRight');
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
                    $("#video").attr("src","data:image/jpeg;base64,"+ data);
                }
            });
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
       			    updateGuage(pitchChart, -180, 180, attitude.attitudeMap.SENSOR_PITCH);
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
        var pitchChart = createRollPitchGuage('pitch', 'Pitch', -180, 180, true);
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
            var burst = $("#burst");
            var burstDuration = $("#burstDuration");
			var codeEditor = $();
			
            pparam.bind('click', function(){sendElementValue('PPARAM', pparam);});
            iparam.bind('click', function(){sendElementValue('IPARAM', iparam);});
            dparam.bind('click', function(){sendElementValue('DPARAM', dparam);});
            imax.bind('click', function(){sendElementValue('IMAX', imax);});
            rollcorr.bind('click', function(){sendElementValue('ROLL_CORR', rollcorr);});
            pitchcorr.bind('click', function(){sendElementValue('PITCH_CORR', pitchcorr);});
            yawcorr.bind('click', function(){sendElementValue('YAW_CORR', yawcorr);});
            gyroalpha.bind('click', function(){sendElementValue('LOW_PASS_GYRO', gyroalpha);});
			burst.bind('click', function(){sendElementValue('BURST', burst);});
			burstDuration.bind('click', function(){sendElementValue('BURST_DURATION', burstDuration);});
						
			//init code editor
			editor = CodeMirror.fromTextArea(document.getElementById("code"), {
			    autoCloseBrackets: true,
				lineNumbers: true,
				extraKeys: {"Ctrl-Space": "autocomplete"}
			});		
			
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
				<table>
				<tr>
				    <td>
					<div>
						P: <input id="pparam" type="number" min="0" max="1" step=".01" value=".1"/>  
						I: <input id="iparam" type="number" min="0" max="1" step=".01" value="0"/>
						D: <input id="dparam" type="number" min="0" max="1" step=".01" value=".1"/>
						IMAX: <input id="imax" type="number" min="0" max="100" value="0"/>
					</div>
					<div>
						Roll-Corr: <input id="rollcorr" type="number" min="-1" max="1" step=".1" value=".1"/>  
						Pitch-Corr: <input id="pitchcorr" type="number" min="-1" max="1" step=".1" value=".1"/>  
						Yaw-Corr: <input id="yawcorr" type="number" min="-1" max="1" step=".1" value="0"/>
						Gyro-alpha: <input id="gyroalpha" type="number" min="0" max="1" step=".1" value="0.9"/>
					</div>
					<div>	
						Burst: <input id="burst" type="number" min="-100" max="100" step="1" value="0"/>
						Burst Duration: <input id="burstDuration" type="number" min="0" max="1000" step="50" value="0"/>
					</div>
					</td>
					<td>
					<div>
                        <textarea id="log" lines="15" col="20">Output log</textarea>
                    </div>
                    <div id="sensors"></div>
					</td>
				</tr>
				</table>
				<div><input id="reset" type=BUTTON OnClick="btnPressed('RESET');" VALUE="RESET"/></div>
				<form id="control_form" method="POST">
				    <input type="hidden" name="type" value="control" />
				    <input type="hidden" id="payload" name="payload" value=""/>
				</form>
			</div>
		</div>
		<div>
		    <textarea id="code" name="code">
function playNote(val, tact){
	robot.THRUST=-100;
	robot.THRUST=val;
	robot.sleep(tactDuration*tact);
}
function playLowSol(tact){
	playNote(10, tact);
}
function playLowLa(tact){
	playNote(12, tact);
}
function playLowSi(tact){
	playNote(14, tact);
}
function playDo(tact){
	playNote(16, tact);
}
function playRe(tact){
	playNote(18, tact);
}
function playMi(tact){
	playNote(20, tact);
}
function playFa(tact){
	playNote(22, tact);
}
function pause(){
	robot.THRUST=-100;
	robot.sleep(10);
}
var tactDuration = 300;

while (true){
	playMi(2);playDo(2);playMi(2);playDo(2);
	playFa(2);playMi(2);playRe(4);
	playLowSol(2);pause();playLowSol(2);pause();playLowSol(2);playLowLa(1);playLowSi(1);
	playDo(2);pause();playDo(2);pause();playDo(4);

	//tear down the cycle
	playNote(0,0);
	robot.sleep(1000);
}</textarea>
		    <form id="code_form" method="POST">
                <input type="hidden" name="type" value="script" />
                <input id="code_action" type="hidden" name="action" value="-1" />
                <input type="hidden" id="code_value" name="code" value=""/>
            </form>
			<span>
			    <input id="edit_btn" type=BUTTON OnClick="enableEditor(!editorEnabled);" value="EDIT"/>
				<input id="run_btn" type=BUTTON OnClick="executeBtnPressed(true);" value="RUN" disabled/>
				<input id="stop_btn" type=BUTTON OnClick="executeBtnPressed(false);" value="STOP"/>
			</span>
		</div>	
	</body>
</html>