<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Androbotus - Main</title>
		<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
		<script src="http://code.highcharts.com/highcharts.js"></script>
		<script src="http://code.highcharts.com/highcharts-more.js"></script>
		<script src="http://code.highcharts.com/modules/exporting.js"></script>
		<script type="text/javascript">
			
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
				} else if (code == 'PPARAM') {
					//p parameter
					type = 'PPARAM';
				} else if (code == 'IPARAM') {
					//i parameter
					type = 'IPARAM';
				} else if (code == 'DPARAM') {
					//d parameter
					type = 'DPARAM';
				} else if (code == 'IMAX') {
					//imax parameter
					type = 'IMAX';
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
		function createRollPitchGuage(name, min, max){
			return	{
	    		chart: {
	        		type: 'gauge',
	        		animation: false,
	        		plotBackgroundColor: null,
	        		plotBackgroundImage: null,
	        		plotBorderWidth: 0,
	        		plotShadow: true
	    		},
	    
	    		title: {
	        		text: name
	    		},
	    
	    		pane: {
	        		startAngle: -150,
	        		endAngle: 150,
	        		background: [{
	            		backgroundColor: {
	                		linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
	                		stops: [
	                    		[0, '#FFF'],
	                    		[1, '#333']
	                		]
	            		},
	            		borderWidth: 0,
	            		outerRadius: '109%'
	        		}, {
	            		backgroundColor: {
	                		linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
	                		stops: [
	                    		[0, '#333'],
	                    		[1, '#FFF']
	                		]
	            		},
	            		borderWidth: 1,
	            		outerRadius: '107%'
	        		}, {
	            		// default background
	        		}, {
	            		backgroundColor: '#DDD',
	            		borderWidth: 0,
	            		outerRadius: '105%',
	            		innerRadius: '103%'
	        		}]
	    		},
			       
			    // the value axis
			    yAxis: {
	        		min: min,
	        		max: max,
			        
	        		minorTickInterval: 'auto',
	        		minorTickWidth: 1,
	        		minorTickLength: 10,
	        		minorTickPosition: 'inside',
	        		minorTickColor: '#666',
	
			        tickPixelInterval: 30,
	    		    tickWidth: 2,
	        		tickPosition: 'inside',
	        		tickLength: 10,
	        		tickColor: '#666',
	        		labels: {
	            		step: 2,
	            		rotation: 'auto'
	        		},
	        		title: {
	            		text: name
	        		},
	        		plotBands: [{
	            		from: min + 60,
	            		to: max - 60 ,
	            		color: '#55BF3B' // green
	        		}, {
	            		from: min + 30,
	            		to: min + 60,
	            		color: '#DDDF0D' // yellow
	        		}, {
	            		from: max - 60,
	            		to: max - 30,
	            		color: '#DDDF0D' // yellow
	        		}, {
	            		from: min,
	            		to: min + 30,
	            		color: '#DF5353' // red
	        		}, {
	            		from: max - 30,
	            		to: max,
	            		color: '#DF5353' // red
	        		}]        
	    		},
			
	    		series: [{
	        		name: name,
	        		data: [0],
	        		tooltip: {
	            		valueSuffix: 'degree'
	        		}
	    		}]
			}
		}
		
		//request new roll/pitch/yaw values from the server to plot them on the guage 	
		function updateRollPitchGuage(chart, min, max, name) {
				if (!chart.renderer.forExport) {
		    		setInterval(function () {
		       	$.ajax({
							type: "GET",
  		       		url: 'http://localhost:8080/androbotus/webaccess/attitude',
							contentType: 'application/json; charset=utf-8',
					 		dataType: 'json',
         				success: function(json){
         	  			//if success then get the fresh attitude and update corresponding chart sections
         	  			var val = 0;
         	  			if (name == 'ROLL'){
         	  				val = json.attitudeMap.SENSOR_ROLL;		
         	  			} else if (name == 'PITCH') {
         	  				val = json.attitudeMap.SENSOR_PITCH;		         	  
         	  			} else if (name == 'YAW') {
         	  				val = json.attitudeMap.SENSOR_YAW;		         	  
         	  			}
         	  			var point = chart.series[0].points[0]
							point.update(val);
         		}
       	});
       }, 50);			
				}
		}	
		
		function updateRollPitchGuageFake(chart, min, max) {
				if (!chart.renderer.forExport) {
		    		setInterval(function () {
		        		var point = chart.series[0].points[0],
		            		newVal,
		            		inc = Math.round((Math.random() - 0.5) * 20);
		        
		        		newVal = point.y + inc;
		        		if (newVal < min || newVal > max) {
		            		newVal = point.y - inc;
		        		}
		        		
		        		point.update(newVal);
				        
				    }, 500);
				}
		}
			
					
		//create roll guage
		var roll = createRollPitchGuage('Roll', -90, 90);
		$(function () {$('#roll').highcharts(roll, function(chart){updateRollPitchGuage(chart, -90, 90, 'ROLL');});});
		//create pitch guage
		var pitch = createRollPitchGuage('Pitch', -90, 90);
		$(function () {$('#pitch').highcharts(pitch, function(chart){updateRollPitchGuage(chart, -90, 90, 'PITCH');});});
					
						
		//create guage for engines thrust. A single guage displays thrust level for 2 engines
		function createIndivThrust(name) {
				return {
	    			chart: {
	        			type: 'gauge',
	        			animation: false,
	        			plotBorderWidth: 1,
	        			plotBackgroundColor: {
	        				linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
	        				stops: [
	        					[0, '#FFF4C6'],
	        					[0.3, '#FFFFFF'],
	        					[1, '#FFF4C6']
	        				]
	        				},
	        			plotBackgroundImage: null,
	        			height: 200
	    			},
	
	    			title: {text: name + ' Engines Thrust'},
	    
	    			pane: [{
	        			startAngle: -45,
	        			endAngle: 45,
	        			background: null,
	        			center: ['25%', '145%'],
	        			size: 300
	    			}, {
	    				startAngle: -45,
	    				endAngle: 45,
	    				background: null,
	        			center: ['75%', '145%'],
	        			size: 300
	    			}],	    		        
			
	    			yAxis: [{
	        			min: 0,
	        			max: 100,
	        			minorTickPosition: 'outside',
	        			tickPosition: 'outside',
	        			labels: {
	        				rotation: 'auto',
	        				distance: 20	
	        			},
	        			plotBands: [{
	        				from: 80,
	        				to: 100,
	        				color: '#C02316',
	        				innerRadius: '100%',
	        				outerRadius: '105%'
	        			}],
	        			pane: 0,
	        			title: {
	        				text: '<span style="font-size:10px">' + name + ' Left</span>',
	        				y: -40
	        			}
	    			}, {
	        			min: 0,
	        			max: 100,
	        			minorTickPosition: 'outside',
	        			tickPosition: 'outside',
	        			labels: {
	        				rotation: 'auto',
	        				distance: 20
	        			},
	        			plotBands: [{
	        				from: 80,
	        				to: 100,
	        				color: '#C02316',
	        				innerRadius: '100%',
	        				outerRadius: '105%'
	        			}],
	        			pane: 1,
	        			title: {
	        			text: '<span style="font-size:10px">' + name + ' Right</span>',
	        			y: -40
	        		}
	    		}],
	    
	    		plotOptions: {
	    			gauge: {
	    				dataLabels: {
	    					enabled: false
	    				},
	    				dial: {
	    					radius: '100%'
	    				}
	    			}
	    		},
	    	
	
			    series: [{
	    		    data: [0],
	        		yAxis: 0
	    		}, {
	        		data: [0],
	        		yAxis: 1
	    		}]
			}
		}
			
			
		//refresh motor thrust levels
		function updateIndivThrust(chart, level) {
			//request server to get new attitude data
			setInterval(function () {
			   var left = chart.series[0].points[0];
	         var right = chart.series[1].points[0];

				$.ajax({
					type: "GET",
  		       url: 'http://localhost:8080/androbotus/webaccess/attitude',
     	     contentType: 'application/json; charset=utf-8',
					 dataType: 'json',
         		success: function(json){
         	  		//if success then get the fresh attitude and update corresponding chart sections
         	  		var leftVal = 0;
         	  		var rightVal = 0;
         	  		if (level == 'FRONT'){
         	  			leftVal = json.attitudeMap.FL;
         	     	   rightVal = json.attitudeMap.FR;		
         	  		} else if (level == 'REAR') {
         	  			leftVal = json.attitudeMap.RL;
         	     	   rightVal = json.attitudeMap.RR;		         	  
         	  		}
						left.update(leftVal, false);
						right.update(rightVal, false);
						chart.redraw();      	  
         		}
       	});
       }, 50);
    	}	
			
		function updateIndivThrustFake(chart) {
			setInterval(function() {
				var left = chart.series[0].points[0],
	            		right = chart.series[1].points[0],
	            		leftVal, 
	            		inc = (Math.random() - 0.5) * 15;
			
				leftVal =  left.y + inc;
				rightVal = leftVal + inc / 3;
				if (leftVal < 0 || leftVal > 100) {
					leftVal = left.y - inc;
				}
				if (rightVal < 0 || rightVal > 100) {
					rightVal = leftVal;
				}
			
				left.update(leftVal, false);
				right.update(rightVal, false);
				chart.redraw();
			
			}, 500);
	
		}
		
		//create thrust guage for front motors
		var frontChart = createIndivThrust('Front');
		$(function() {
			$('#front-thrust').highcharts(frontChart, function(chart){updateIndivThrust(chart, 'FRONT')});
		});
		//create thrust guage for rear motors
		var rearChart = createIndivThrust('Rear');
		$(function() {
			$('#rear-thrust').highcharts(rearChart, function(chart){updateIndivThrust(chart, 'REAR')});
		});
			
			
			
			var cnt = 0;
			//this function is turned off since all the sensors should use charts as of now
			//setInterval(function(){updateSensors()},30);
			
			//retreive and display video data
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
			
			//video is currently switched off
			//refresh image with 30 fps rate to make illusion of video. Poor man's solution though...
			//setInterval(function(){refreshVideoFrame()}, 30);
			
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
			
			$(document).ready(function() {
				//bind param controls
				var pparam = $("#pparam");
				var dparam = $("#dparam");
				var iparam = $("#iparam");
				var imax = $("#imax");
			
				pparam.bind('click', function(){postParameter('PPARAM', pparam);});
				iparam.bind('click', function(){postParameter('IPARAM', iparam);});
				dparam.bind('click', function(){postParameter('DPARAM', dparam);});
				imax.bind('click', function(){postParameter('IMAX', imax);});
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
    					<div id="roll" style="width: 400px; height: 300px; margin: 0 auto"></div>
    					<div id="pitch" style="width: 400px; height: 300px; margin: 0 auto"></div>			
    				</td>
    				<td>
			    		<div id="front-thrust" style="width: 600px; height: 300px; margin: 0 auto"></div>
    					<div id="rear-thrust" style="width: 600px; height: 300px; margin: 0 auto"></div>
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
					P: <input id="pparam" type="number" min="0" max="1" step=".01" value=".75"/>  
					I: <input id="iparam" type="number" min="0" max="1" step=".01" value=".25"/>  
					D: <input id="dparam" type="number" min="0" max="1" step=".01" value=".50"/>  
					IMAX: <input id="imax" type="number" min="0" max="100" value="40"/>
				</div>
				<span>
					<textarea id="log" lines="15" col="20">Output log</textarea>
				</span>	
				<span id="sensors">
				</span>
				
			</div>
				
		</div>
	</body>
</html>