/*
 *  Copyright 2016 Google Inc. All Rights Reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @fileoverview Generators for the Turtle Blockly demo on Android.
 * @author fenichel@google.com (Rachel Fenichel)
 */
'use strict';

var Twin = {};

Twin.SYNCHRON_BYTES = "AA-44-1C";

Twin.ANALOG_READ_MESSAGE_ID = "04";
Twin.ANALOG_READ_MESSAGE_LENGTH = "03";

Twin.ANALOG_WRITE_MESSAGE_ID = "03";
Twin.ANALOG_WRITE_MESSAGE_LENGTH = "02";

Twin.DIGITAL_READ_MESSAGE_ID = "02";
Twin.DIGITAL_READ_MESSAGE_LENGTH = "02";

Twin.DIGITAL_WRITE_MESSAGE_ID = "01";
Twin.DIGITAL_WRITE_MESSAGE_LENGTH = "02";

Twin.DETECT_TOUCH_MESSAGE_ID = "05";
Twin.DETECT_TOUCH_MESSAGE_LENGTH = "02";

Twin.GET_ULTRASONIC_VALUE_MESSAGE_ID = "0C";
Twin.GET_ULTRASONIC_VALUE_MESSAGE_LENGTH = "02";

Twin.SET_RGB_COLOR_MESSAGE_ID = "0D";
Twin.SET_RGB_COLOR_MESSAGE_LENGTH = "09";

Twin.SET_BUZZER_NOTE_MESSAGE_ID = "0F";
Twin.SET_BUZZER_NOTE_MESSAGE_LENGTH = "02";

Twin.SET_BUZZER_MUSIC_MESSAGE_ID = "10";
Twin.SET_BUZZER_MUSIC_MESSAGE_LENGTH = "02";

Twin.DIGITAL_TO_ANALOG_MAP = { "4": "6", "8": "8", "12": "11" };

Twin.analogRead = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.ANALOG_READ_MESSAGE_ID + "-" + Twin.ANALOG_READ_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {

    if(i == 0) {
      data[i] = Twin.DIGITAL_TO_ANALOG_MAP[data[i]];
    }

    message += "-" + Twin.convertInputToHex(data[i]);
  }

  return ['TwinJSSession.returnMessage(Twin.getMessage(\'' + message + '\', \'1\'))', Blockly.JavaScript.ORDER_NONE];
}


Twin.analogWrite = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.ANALOG_WRITE_MESSAGE_ID + "-" + Twin.ANALOG_WRITE_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {
    message += "-" + Twin.convertInputToHex(data[i]);
  }

  return 'Twin.sendMessage(\'' + message + '\');\n';
}

Twin.digitalRead = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.DIGITAL_READ_MESSAGE_ID + "-" + Twin.DIGITAL_READ_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {
    message += "-" + Twin.convertInputToHex(data[i]);
  }

  return ['TwinJSSession.returnMessage(Twin.getMessage(\'' + message + '\'))', Blockly.JavaScript.ORDER_NONE];
}

Twin.digitalWrite = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.DIGITAL_WRITE_MESSAGE_ID + "-" + Twin.DIGITAL_WRITE_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {
    message += "-" + Twin.convertInputToHex(data[i]);
  }

  return 'Twin.sendMessage(\'' + message + '\');\n';
}

Twin.detectTouch = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.DETECT_TOUCH_MESSAGE_ID + "-" + Twin.DETECT_TOUCH_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {
    message += "-" + Twin.convertInputToHex(data[i]);
  }

  return ['TwinJSSession.returnMessage(Twin.getMessage(\'' + message + '\'))', Blockly.JavaScript.ORDER_NONE];
}

Twin.getDeviceOrientation = function(orientation) {

  return ['Twin.getDeviceOrientation(\'' + orientation + '\')', Blockly.JavaScript.ORDER_NONE];
}

Twin.getDeviceShakeStatus = function() {

    return ['Twin.getDeviceShakeStatus()', Blockly.JavaScript.ORDER_NONE];
}

Twin.getUltrasonicValue = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.GET_ULTRASONIC_VALUE_MESSAGE_ID + "-" + Twin.GET_ULTRASONIC_VALUE_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {
    message += "-" + Twin.convertInputToHex(data[i]);
  }

  return ['TwinJSSession.returnMessage(Twin.getMessage(\'' + message + '\', \'1\'))', Blockly.JavaScript.ORDER_NONE];
}

Twin.setBuzzerMusic = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.SET_BUZZER_MUSIC_MESSAGE_ID + "-" + Twin.SET_BUZZER_MUSIC_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {
    message += "-" + Twin.convertInputToHex(data[i]);
  }

  return 'Twin.sendMessage(\'' + message + '\');\n';
}

Twin.setBuzzerNote = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.SET_BUZZER_NOTE_MESSAGE_ID + "-" + Twin.SET_BUZZER_NOTE_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {
    message += "-" + Twin.convertInputToHex(data[i]);
  }

  return 'Twin.sendMessage(\'' + message + '\');\n';
}

Twin.setRGBColor = function(data) {

  var message = Twin.SYNCHRON_BYTES + "-" + Twin.SET_RGB_COLOR_MESSAGE_ID + "-" + Twin.SET_RGB_COLOR_MESSAGE_LENGTH;

  for(var i = 0; i < data.length; i++) {
    message += "-" + data[i];
  }

  return 'Twin.sendMessage(\'' + message + '\');\n';
}

Twin.sleep = function(duration) {

  return 'Twin.sleep(\'' + duration + '\');\n';
}

Twin.convertHexToByteArray = function(hex) {

  return hex.replace('#', '').split(/(?=(?:..)*$)/);
}

Twin.convertInputToHex = function(input) {

  var hex = parseInt(input).toString(16).toUpperCase();

  if(hex.length < 2) {
    hex = "0" + hex;
  }

  return hex;
}

Twin.scaleInputToArduino = function(input, input_min, input_max, output_min, output_max) {

  input = parseInt(input);
  input_min = parseInt(input_min);
  input_max = parseInt(input_max);
  output_min = parseInt(output_min);
  output_max = parseInt(output_max);

  var result = ((input - input_min) * (output_max - output_min) / (input_max - input_min) + output_min);

  return Math.round(result);
}

Twin.scaleAngle = function(angle) {

  angle = parseInt(angle) / 2;

  return Twin.scaleInputToArduino(angle, 0, 180, 0, 255);
}

Twin.scaleLevel = function(level) {

  level = parseInt(level);

  var result = 0;

  switch(level) {

    case 1:
      result = 30; // 1E
      break;

    case 2:
      result = 83; // 53
      break;

    case 3:
      result = 135; // 87
      break;

    case 4:
      result = 183; // B7
      break;

    case 5:
      result = 255; // FF
      break;
  }

  return result;
}

Twin.scaleStatus = function(status) {

  return Twin.scaleInputToArduino(status, 0, 1, 0, 255);
}

Twin.scaleVelocity = function(velocity) {

  return Twin.scaleInputToArduino(velocity, 0, 100, 0, 255);
}

Blockly.JavaScript['start_play'] = function(block) {
  // No need to take an action
  return "";
};

Blockly.JavaScript['actionDCMotor'] = function(block) {

  var output = block.getFieldValue('output');

  var velocity = block.getFieldValue('velocity');
  velocity = Twin.scaleVelocity(velocity);

  return Twin.analogWrite([output, velocity]);
};

Blockly.JavaScript['actionServoMotor'] = function(block) {

  var output = block.getFieldValue('output');

  var angle = block.getFieldValue('angle');
  angle = Twin.scaleAngle(angle);

  return Twin.analogWrite([output, angle]);
};

Blockly.JavaScript['lightSoundBarGraph'] = function(block) {

  var output = block.getFieldValue('output');

  var level = block.getFieldValue('level');
  level = Twin.scaleLevel(level);

  return Twin.analogWrite([output, level]);
};

Blockly.JavaScript['lightSoundTripleRGBLed'] = function(block) {

  var first_rgb = block.getFieldValue('7_1_rgb');
  var second_rgb = block.getFieldValue('7_2_rgb');
  var third_rgb = block.getFieldValue('7_3_rgb');

  var rgb_led = [];
  rgb_led = rgb_led.concat(Twin.convertHexToByteArray(first_rgb));
  rgb_led = rgb_led.concat(Twin.convertHexToByteArray(second_rgb));
  rgb_led = rgb_led.concat(Twin.convertHexToByteArray(third_rgb));

  return Twin.setRGBColor(rgb_led);
};

Blockly.JavaScript['lightSoundLongLed'] = function(block) {

  var output = block.getFieldValue('output');

  var status = block.getFieldValue('status');
  status = Twin.scaleStatus(status);

  return Twin.digitalWrite([output, status]);
};

Blockly.JavaScript['lightSoundBuzzerNote'] = function(block) {

  var output = block.getFieldValue('output');

  var note = block.getFieldValue('note');

  return Twin.setBuzzerNote([output, note]);
};

Blockly.JavaScript['lightSoundBuzzerMusic'] = function(block) {

  var output = block.getFieldValue('output');

  var music = block.getFieldValue('music');

  return Twin.setBuzzerMusic([output, music]);
};

Blockly.JavaScript['conditionDeviceShakeStatus'] = function(block) {

    return Twin.getDeviceShakeStatus();
};

Blockly.JavaScript['conditionDeviceOrientation'] = function(block) {

  var orientation = block.getFieldValue('orientation');

  return Twin.getDeviceOrientation(orientation);
};

Blockly.JavaScript['detectData'] = function(block) {

  var input = block.getFieldValue('input');

  var dataType = block.getFieldValue('dataType');

  if(dataType == "movement") {

    return Twin.digitalRead([input, "0"]);

  } else {

    return Twin.analogRead([input, "0", "0"]);
  }
};

Blockly.JavaScript['detectButtonPress'] = function(block) {

  var input = block.getFieldValue('input');

  return Twin.digitalRead([input, "0"]);
};

Blockly.JavaScript['detectRemoteControl'] = function(block) {

  var input = block.getFieldValue('input');

  return Twin.digitalRead([input, "0"]);
};

Blockly.JavaScript['detectPotantiometer'] = function(block) {

  var input = block.getFieldValue('input');

  return Twin.analogRead([input, "0", "0"]);
};

Blockly.JavaScript['detectTouch'] = function(block) {

  var input = block.getFieldValue('input');

  return Twin.detectTouch([input, "0"]);
};

Blockly.JavaScript['detectUltrasonic'] = function(block) {

  return Twin.getUltrasonicValue(["0", "0"]);
};

Blockly.JavaScript['detectProximity'] = function(block) {

  var input = block.getFieldValue('input');


  return Twin.analogRead([input, "0", "0"]);
};

Blockly.JavaScript['sleep'] = function(block) {

  var duration = block.getFieldValue('duration');

  return Twin.sleep(duration);
};

// Blockly.JavaScript.INFINITE_LOOP_TRAP = 'if(TwinJSSession.decrementInfiniteLoopTrap() == 0) throw "Infinite loop.";\n';

