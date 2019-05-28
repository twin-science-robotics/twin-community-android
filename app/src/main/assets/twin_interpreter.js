/**
 *
 * Copyright 2018 Twin. All rights reserved.
 *
 */

var TwinJS = {};

// Generated code
TwinJS.code = "";

// JS interpreter
TwinJS.interpreter = {};

// Initialization function of interpreter
TwinJS.initInterpreter = function(interpreter, scope) {

    var alertWrapper = function(text) {
      return alert(arguments.length ? text : '');
    };

    interpreter.setProperty(scope, 'alert',
        interpreter.createNativeFunction(alertWrapper));

    var consoleWrapper = interpreter.createObject(interpreter.OBJECT);

    interpreter.setProperty(scope, 'console', consoleWrapper);

    var logWrapper = function(obj) {
        return interpreter.createPrimitive(console.log(obj));
    };

    interpreter.setProperty(consoleWrapper, 'log', interpreter.createNativeFunction(logWrapper));

    var twinWrapper = interpreter.createObject(interpreter.OBJECT);

    interpreter.setProperty(scope, 'Twin', twinWrapper);

    // Twin.sendMessage

    var sendMessageWrapper = function(message) {
        return interpreter.createPrimitive(Twin.sendMessage(message));
    }

    interpreter.setProperty(twinWrapper, 'sendMessage', interpreter.createNativeFunction(sendMessageWrapper));

    // Twin.getMessage

    var getMessageWrapper = function(message, isAnalog) {
        return interpreter.createPrimitive(Twin.getMessage(message, isAnalog));
    }

    interpreter.setProperty(twinWrapper, 'getMessage', interpreter.createNativeFunction(getMessageWrapper));

    // Twin.getDeviceShakeStatus

    var getDeviceShakeStatusWrapper = function() {
        return interpreter.createPrimitive(Twin.getDeviceShakeStatus());
    }

    interpreter.setProperty(twinWrapper, 'getDeviceShakeStatus', interpreter.createNativeFunction(getDeviceShakeStatusWrapper));

    // Twin.getDeviceOrientation

    var getDeviceOrientationWrapper = function(orientation) {
        return interpreter.createPrimitive(Twin.getDeviceOrientation(orientation));
    }

    interpreter.setProperty(twinWrapper, 'getDeviceOrientation', interpreter.createNativeFunction(getDeviceOrientationWrapper));

    // Twin.sleep

    var sleepWrapper = function(duration) {
        return interpreter.createPrimitive(Twin.sleep(duration));
    }

    interpreter.setProperty(twinWrapper, 'sleep', interpreter.createNativeFunction(sleepWrapper));

    // TwinJSSession

    var twinJSSessionWrapper = interpreter.createObject(interpreter.OBJECT);

    interpreter.setProperty(scope, 'TwinJSSession', twinJSSessionWrapper);

    var returnMessageWrapper = function(message) {
        return interpreter.createPrimitive(TwinJSSession.returnMessage(message));
    }

    interpreter.setProperty(twinJSSessionWrapper, 'returnMessage', interpreter.createNativeFunction(returnMessageWrapper));

    var decrementInfiniteLoopTrapWrapper = function() {
        return interpreter.createPrimitive(TwinJSSession.decrementInfiniteLoopTrap());
    }

    interpreter.setProperty(twinJSSessionWrapper, 'decrementInfiniteLoopTrap', interpreter.createNativeFunction(decrementInfiniteLoopTrapWrapper));
};

// Main initialization
TwinJS.init = function(code) {

    TwinJS.code = code;

    TwinJS.interpreter = new Interpreter(TwinJS.code, TwinJS.initInterpreter);

    TwinJSSession.reset();

    TwinJSSession.nextStep();
}

// Code run session
var TwinJSSession = {};

// Infinite loop
TwinJSSession.INFINITE_LOOP_TRAP = 1000;

// Pause status flag
TwinJSSession.IS_PAUSED = 0;

// Stop status flag
TwinJSSession.IS_STOPPED = 0;

// Message carrier
TwinJSSession.MESSAGE = "";

// Reset flags
TwinJSSession.reset = function() {

    TwinJSSession.INFINITE_LOOP_TRAP = 1000;

    TwinJSSession.IS_PAUSED = 0;

    TwinJSSession.IS_STOPPED = 0;

    TwinJSSession.MESSAGE = "";
}

TwinJSSession.getStopStatus = function() {
    return TwinJSSession.IS_STOPPED ? true : false;
}

TwinJSSession.setStopStatus = function(status) {
    TwinJSSession.IS_STOPPED = status ? true : false;
}

// Run code
TwinJSSession.nextStep = function() {

    // console.log("TwinJSSession - Next Step");

    if(TwinJSSession.IS_STOPPED) {
        TwinJSSession.stop();
        return;
    }

    if(TwinJSSession.IS_PAUSED) {
        return;
    }

    if(TwinJS.interpreter.step()) {
        setTimeout(TwinJSSession.nextStep, 0);
    } else {
        TwinJSSession.complete();
    }
}

// Pause current session
TwinJSSession.pause = function() {

    // console.log("TwinJSSession - Pause");

    TwinJSSession.IS_PAUSED = 1;


}

// Resume current session
TwinJSSession.resume = function(message) {

    // console.log("TwinJSSession - Resume - Message: " + message);

    TwinJSSession.IS_PAUSED = 0;

    TwinJSSession.MESSAGE = message;

    TwinJSSession.nextStep();
}

// Resume current session
TwinJSSession.stop = function() {

    console.log("TwinJSSession - Stop");

    throw "Code evaluation stopped by client.";
}

// Complete evaluation
TwinJSSession.complete = function() {

    console.log("TwinJSSession - Complete");

    throw "Code evaluation completed.";
}

// Helper function to return message, used in blockly
TwinJSSession.returnMessage = function(text) {

    // text variable uses as placeholder. value is not imporant. message is carried by TwinJSSession.MESSAGE

    // console.log("TwinJSSession - ReturnMessage - Text: " + text);

    var result = TwinJSSession.MESSAGE.trim();

    if(result == "" || result == '0') {
        return false;
    }

    return result;
}

// Helper function to return infinite loop trap limit value

TwinJSSession.decrementInfiniteLoopTrap = function() {

    console.log("TwinJSSession - decrementInfiniteLoopTrap - Value: " + TwinJSSession.INFINITE_LOOP_TRAP);

    TwinJSSession.INFINITE_LOOP_TRAP -= 1;

    return TwinJSSession.INFINITE_LOOP_TRAP;
}



