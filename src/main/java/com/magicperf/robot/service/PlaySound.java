package com.magicperf.robot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PlaySound implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(PlaySound.class);
    private String wavFileName;

    public PlaySound(String wavFile){
        this.wavFileName = wavFile;
    }

    @Override
    public void run() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        // Run a shell command
        processBuilder.command("bash", "-c", "aplay /home/pi/sounds/"+wavFileName);
        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!");
            } else {
                //abnormal...
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
