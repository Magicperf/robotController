package com.magicperf.robot.service;

public interface RobotService {
    void executeMove(String command, Long time);
    void enableAuto();
}
