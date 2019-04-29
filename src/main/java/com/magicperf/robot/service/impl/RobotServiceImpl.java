package com.magicperf.robot.service.impl;

import com.magicperf.robot.service.RobotService;
import com.pi4j.io.gpio.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
public class RobotServiceImpl implements RobotService {
    private static final Logger logger = LoggerFactory.getLogger(RobotServiceImpl.class);

    final GpioController gpio = GpioFactory.getInstance();
    final GpioPinDigitalOutput frontLeftWheel = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "frontLeftWheel", PinState.LOW);
    final GpioPinDigitalOutput frontRightWheel = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, "frontRightWheel", PinState.LOW);
    final GpioPinDigitalOutput rearLeftWheel = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, "rearLeftWheel", PinState.LOW);
    final GpioPinDigitalOutput rearRightWheel = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "readRightWheel", PinState.LOW);

    @Override
    public void move(String command, Long time){
        logger.info("Starting move for command: " + command);
        if(time == null){
            logger.error("Time is null!");
            return;
        }
        if(StringUtils.equalsIgnoreCase(command,"W")){
            logger.info("Moving forward for: " + time);
            forward(time);
        }else if(StringUtils.equalsIgnoreCase(command,"S")){
            logger.info("Moving reverse for: " + time);
            reverse(time);
        }else if(StringUtils.equalsIgnoreCase(command,"A")){
            logger.info("Turning left for: " + time);
            turnLeft(time);
        }else if(StringUtils.equalsIgnoreCase(command,"D")){
            logger.info("Turning right for: " + time);
            turnRight(time);
        }else if(StringUtils.equalsIgnoreCase(command,"Q")){
            logger.info("Pivot left for: " + time);
            pivotLeft(time);
        }else if(StringUtils.equalsIgnoreCase(command,"E")){
            logger.info("Pivot right for: " + time);
            pivotRight(time);
        }else{
            logger.error("No Command recognized!: " + command);
        }
    }

    private void forward(Long time){
        frontRightWheel.low();
        rearLeftWheel.low();
        frontLeftWheel.pulse(time,true);
        rearRightWheel.pulse(time,true);
    }

    private void reverse(Long time){
        frontLeftWheel.low();
        rearRightWheel.low();
        frontRightWheel.pulse(time,true);
        rearLeftWheel.pulse(time,true);
    }

    private void turnLeft(Long time){
        rearRightWheel.low();
        frontLeftWheel.pulse(time,true);
        frontRightWheel.pulse(time,true);
        rearLeftWheel.pulse(time,true);
    }

    private void turnRight(Long time){
        frontLeftWheel.low();
        frontRightWheel.low();
        rearLeftWheel.low();
        rearRightWheel.pulse(time,true);
    }

    private void pivotLeft(Long time){
        frontRightWheel.low();
        rearRightWheel.low();
        frontLeftWheel.pulse(time,true);
        rearLeftWheel.pulse(time,true);
    }

    private void pivotRight(Long time){
        frontLeftWheel.low();
        rearLeftWheel.low();
        frontRightWheel.pulse(time,true);
        rearRightWheel.pulse(time,true);
    }
}
