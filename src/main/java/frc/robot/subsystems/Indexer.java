// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.time.chrono.IsoChronology;

import com.revrobotics.CANSparkBase;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import au.grapplerobotics.ConfigurationFailedException;
import au.grapplerobotics.LaserCan;
import au.grapplerobotics.LaserCan.Measurement;
import au.grapplerobotics.LaserCan.RangingMode;
import au.grapplerobotics.LaserCan.TimingBudget;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Indexer extends SubsystemBase {

  // Creates private variables 
  private CANSparkMax highIndexMotor;
  private LaserCan laser;
  public Indexer() {
    highIndexMotor = new CANSparkMax(0, MotorType.kBrushless); //creates new motor
    laser = new LaserCan(0); //creates new laserCan

    try {
      //configures settings for the laserCan
      laser.setRangingMode(RangingMode.SHORT); //sets ranging mode to short distance, which is more accurate
      laser.setTimingBudget(TimingBudget.TIMING_BUDGET_50MS); //checks every 50 milliseconds for the measurement of the laser
      laser.setRegionOfInterest(new LaserCan.RegionOfInterest(8, 8, 16, 16)); //the area where the laserCan can sense objects

    } catch (ConfigurationFailedException e) {
      // displays if the code doesn't work properly
      System.out.println("Error configuring laser CAN");
      System.out.println(e.getMessage());
    }
  }

  /** Method that makes the high index motor spin depending on the isStored method */
  public void spinIndex(double speed) {
    highIndexMotor.set(speed); //sets motor speed
  }

  
  /**
   * Checks if the note is at least partially in the high index motor.
   * @return
   */
  public boolean isStoredPartially() {
    //checks if the laserCan distance is more than 177.9 millimeters or less than 177.9 millimeters
     if (getLaserCanDistance() <= 177.8) {
      //if the laserCAN distance is less than 177.9 millimeters, returns true and there is a note at the high index motor
      return true;
     } else {
      //if the laserCAN distance is more than 177.9 millimeters, returns false and there is no note at the high index motor
      return false;
     }
  }

  /**
   * Checks if the note is completely in the high index motor.
   * @return
   */
  public boolean isStoredFully() {
    //checks if the laserCan distance is more than 6.35 millimeters or less than 6.35 millimeters
     if (getLaserCanDistance() <= 6.35) {
      //if the laserCAN distance is less than 6.35 millimeters, returns true and there is a note at the high index motor
      return true;
     } else {
      //if the laserCAN distance is more than 6.35 millimeters, returns false and there is no note at the high index motor
      return false;
     }
  }
  /**
   * 
   * @return
   */
  
  /** Method that checks if a note is at the high index motor */
  public boolean isStored() {
    //checks if the laserCan distance is more than 177.9 millimeters or less than 177.9 millimeters
     if (getLaserCanDistance() <= 177.8) {
      //if the laserCAN distance is less than 177.9 millimeters, returns true and there is a note at the high index motor
      return true;
     } else {
      //if the laserCAN distance is more than 177.9 millimeters, returns false and there is no note at the high index motor
      return false;
     }
  }

   //Method that returns the distance from the laserCAN in millimeters
  public double getLaserCanDistance() {
    //returns the distance from the laserCAN in millimeters
    return laser.getMeasurement().distance_mm;
  }

  

  @Override
  public void periodic() {
  }
}