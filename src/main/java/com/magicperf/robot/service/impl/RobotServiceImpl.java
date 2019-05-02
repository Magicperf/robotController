package com.magicperf.robot.service.impl;

import com.magicperf.robot.service.RobotService;
import com.pi4j.io.gpio.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
@Component
public class RobotServiceImpl implements RobotService {
    private static final Logger logger = LoggerFactory.getLogger(RobotServiceImpl.class);

    final GpioController gpio = GpioFactory.getInstance();
    final GpioPinDigitalOutput pin7 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, PinState.LOW);
    final GpioPinDigitalOutput pin11 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, PinState.LOW);
    final GpioPinDigitalOutput pin13 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, PinState.LOW);
    final GpioPinDigitalOutput pin15 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, PinState.LOW);

    @Override
    public void move(String command, Long time){
        pin7.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        pin11.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        pin13.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        pin15.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);

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
        pin11.low();
        pin13.low();
        pin7.pulse(time,false);
        pin15.pulse(time,false);
    }

    private void reverse(Long time){
        pin7.low();
        pin15.low();
        pin11.pulse(time,false);
        pin13.pulse(time,false);
    }

    private void turnLeft(Long time){
        pin15.low();
        pin7.pulse(time,false);
        pin11.pulse(time,false);
        pin13.pulse(time,false);
    }

    private void turnRight(Long time){
        pin7.low();
        pin11.low();
        pin13.low();
        pin15.pulse(time,false);
    }

    private void pivotLeft(Long time){
        pin11.low();
        pin15.low();
        pin7.pulse(time,false);
        pin13.pulse(time,false);
    }

    private void pivotRight(Long time){
        pin7.low();
        pin13.low();
        pin11.pulse(time,false);
        pin15.pulse(time,false);
    }

    @PreDestroy
    public void destroy(){
        logger.info("Destroy called...shutting down");
        gpio.shutdown();
        while(!gpio.isShutdown()){}
        logger.info("GPIO is shutdown");
    }
}
