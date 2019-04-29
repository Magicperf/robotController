package com.magicperf.robot.controller;

import com.magicperf.robot.model.CommandModel;
import com.magicperf.robot.service.RobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/robot")
public class RobotController {
    private static final Logger logger = LoggerFactory.getLogger(RobotController.class);

    @Autowired
    RobotService robotService;

    @PostMapping("/")
    public void action(@RequestBody CommandModel commandModel){
        logger.info("-------Starting Action-------");
        robotService.move(commandModel.getCommand(), Long.valueOf(commandModel.getTime()));
        logger.info("-------Ending Action-------");
    }
}
