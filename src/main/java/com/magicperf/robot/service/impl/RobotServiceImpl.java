package com.magicperf.robot.service.impl;

import com.magicperf.robot.service.PlaySound;
import com.magicperf.robot.service.RobotService;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Component
public class RobotServiceImpl implements RobotService {
    private static final Logger logger = LoggerFactory.getLogger(RobotServiceImpl.class);

    private ExecutorService executorService;

    final GpioController gpio = GpioFactory.getInstance();
    final GpioPinDigitalOutput input3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, PinState.LOW);
    final GpioPinDigitalOutput input4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, PinState.LOW);
    final GpioPinDigitalOutput input2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, PinState.LOW);
    final GpioPinDigitalOutput input1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, PinState.LOW);
    final GpioPinDigitalInput irInput1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_16, PinPullResistance.PULL_UP);
    final GpioPinDigitalInput irInput2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_UP);

    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(2);
        input3.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        input4.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        input2.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        input1.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        irInput1.setShutdownOptions(false);
        irInput2.setShutdownOptions(false);

        irInput1.addListener(new GpioPinListenerDigital(){
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {
                reverse(Long.valueOf(250));
            }
        });
        irInput2.addListener(new GpioPinListenerDigital(){
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {
                forward(Long.valueOf(250));
            }
        });
    }

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
            executorService.submit(new PlaySound("Marine_Attack00.wav"));
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
            return;
        }
    }

    private void forward(Long time){
        input4.low();
        input2.low();
        input3.pulse(time,false);
        input1.pulse(time,false);
    }

    private void reverse(Long time){
        input3.low();
        input1.low();
        input4.pulse(time,false);
        input2.pulse(time,false);
    }

    private void turnLeft(Long time){
        input1.low();
        input2.low();
        input4.low();
        input3.pulse(time,false);
    }

    private void turnRight(Long time){
        input2.low();
        input3.low();
        input4.low();
        input1.pulse(time,false);
    }

    private void pivotLeft(Long time){
        input4.low();
        input1.low();
        input3.pulse(time,false);
        input2.pulse(time,false);
    }

    private void pivotRight(Long time){
        input3.low();
        input2.low();
        input4.pulse(time,false);
        input1.pulse(time,false);
    }

    @PreDestroy
    public void destroy(){
        logger.info("Destroy called...shutting down");
        executorService.shutdown();
        while(!executorService.isShutdown()){}
        logger.info("Thread service is shutdown");
        gpio.shutdown();
        while(!gpio.isShutdown()){}
        logger.info("GPIO is shutdown");
    }
}
