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

    private Boolean autoEnabled;

    final GpioController gpio = GpioFactory.getInstance();
    final GpioPinDigitalOutput input3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, PinState.LOW);
    final GpioPinDigitalOutput input4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, PinState.LOW);
    final GpioPinDigitalOutput input2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, PinState.LOW);
    final GpioPinDigitalOutput input1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, PinState.LOW);
    final GpioPinDigitalInput irInput1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_16, PinPullResistance.PULL_UP);
    final GpioPinDigitalInput irInput2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_UP);
    final GpioPinDigitalInput irInput3 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_UP);
    final GpioPinDigitalInput irInput4 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_06, PinPullResistance.PULL_UP);
    final GpioPinDigitalInput irInput5 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23, PinPullResistance.PULL_UP);
    final GpioPinDigitalInput irInput6 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_25, PinPullResistance.PULL_UP);

    @PostConstruct
    public void init(){
        autoEnabled = false;
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
                turnLeft();
                try {
                    Thread.sleep(250);
                }catch(Exception e){
                    logger.error(e.getMessage());
                }
                resetPins();
            }
        });
        irInput2.addListener(new GpioPinListenerDigital(){
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {
                turnRight();
                try {
                    Thread.sleep(250);
                }catch(Exception e){
                    logger.error(e.getMessage());
                }
            }
        });
        executorService.submit(new PlaySound("Protoss/Units/Dragoon/pdrwht01.wav"));
    }

    @Override
    public void executeMove(String command, Long time){
        if(autoEnabled){
            return;
        }
        logger.info("Starting move for command: " + command);
        if(time == null){
            logger.error("Time is null!");
            executorService.submit(new PlaySound("Protoss/Units/Dragoon/pdrwht02.wav"));
            return;
        }
        String wavFilePath = null;
        if(StringUtils.equalsIgnoreCase(command,"W")){
            logger.info("Moving forward for: " + time);
            forward();
            wavFilePath = "Protoss/Units/Dragoon/pdrwht05.wav";
        }else if(StringUtils.equalsIgnoreCase(command,"S")){
            logger.info("Moving reverse for: " + time);
            reverse();
            wavFilePath = "Protoss/Units/Dragoon/pdrwht06.wav";
        }else if(StringUtils.equalsIgnoreCase(command,"A")){
            logger.info("Turning left for: " + time);
            turnLeft();
            wavFilePath = "Protoss/Units/Dragoon/pdrwht07.wav";
        }else if(StringUtils.equalsIgnoreCase(command,"D")){
            logger.info("Turning right for: " + time);
            turnRight();
            wavFilePath = "Protoss/Units/Dragoon/pdryes01.wav";
        }else if(StringUtils.equalsIgnoreCase(command,"Q")){
            logger.info("Pivot left for: " + time);
            pivotLeft();
            wavFilePath = "Protoss/Units/Dragoon/Marine_Attack00.wav";
        }else if(StringUtils.equalsIgnoreCase(command,"E")){
            logger.info("Pivot right for: " + time);
            pivotRight();
            wavFilePath = "Protoss/Units/Dragoon/Marine_Attack00.wav";
        }else{
            logger.error("No Command recognized!: " + command);
            return;
        }
        try {
            Thread.sleep(time);
            resetPins();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        executorService.submit(new PlaySound(wavFilePath));
    }

    @Override
    public void enableAuto() {
        this.autoEnabled = true;
        forward();
    }

    private void forward(){
        input4.low();
        input2.low();
        input3.high();
        input1.high();
    }

    private void reverse(){
        input3.low();
        input1.low();
        input4.high();
        input2.high();
    }

    private void turnLeft(){
        input1.low();
        input2.low();
        input4.low();
        input3.high();
    }

    private void turnRight(){
        input2.low();
        input3.low();
        input4.low();
        input1.high();
    }

    private void pivotLeft(){
        input4.low();
        input1.low();
        input3.high();
        input2.high();
    }

    private void pivotRight(){
        input3.low();
        input2.low();
        input4.high();
        input1.high();
    }

    private void resetPins(){
        input1.low();
        input2.low();
        input3.low();
        input4.low();
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
