// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;


//import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;

import frc.robot.subsystems.Drivetrain;

/** Contains and runs all code needed to display all necessary information on Shuffleboard.*/
public class ShuffleboardData {

    private final ShuffleboardTab driverTab, debugTab, modulesTab;

    public ShuffleboardData(Drivetrain drivetrain) {

        driverTab = Shuffleboard.getTab("Driver Tab");
        debugTab = Shuffleboard.getTab("Debug Tab");
        modulesTab = Shuffleboard.getTab("Modules Tab");

        new DrivetrainData(drivetrain);
        
    }

    public class DrivetrainData {

        public DrivetrainData(Drivetrain drivetrain) {

            //Displays the feed from the Limelight on Shuffleboard
            driverTab.addCamera("Limelight", "limelight-shooter-1", "http://10.87.38.73:5800").
            withPosition(5, 0).
            withSize(3, 3);

        }

    }

}