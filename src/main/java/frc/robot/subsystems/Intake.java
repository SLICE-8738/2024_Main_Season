// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import java.time.chrono.IsoChronology;

import com.revrobotics.CANSparkBase;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.LimelightTable;
import au.grapplerobotics.ConfigurationFailedException;
import au.grapplerobotics.LaserCan;
import au.grapplerobotics.LaserCan.Measurement;
import au.grapplerobotics.LaserCan.RangingMode;
import au.grapplerobotics.LaserCan.TimingBudget;
/**
 * An intake pulls in the ring (or "note") that will later be launched.
 */
public class Intake extends SubsystemBase {
  private CANSparkMax intakeEntrance, intakeRamp;
  public LaserCan intakeLaser;
  private LimelightTable limelight;

  /** Creates a new Intake. */
  public Intake() {
    intakeLaser = new LaserCan(0);
    this.intakeEntrance = new CANSparkMax(0, MotorType.kBrushless);
    this.intakeRamp = new CANSparkMax(0, MotorType.kBrushless);
    limelight = Limelights.getIntakeLimelight(); // !!! PLACEHOLDER VALUE !!!

    try {
      intakeLaser.setRangingMode(RangingMode.SHORT); //sets ranging mode to short distance, which is more accurate
      intakeLaser.setTimingBudget(TimingBudget.TIMING_BUDGET_50MS); //checks every 50 milliseconds for the measurement of the laser
      intakeLaser.setRegionOfInterest(new LaserCan.RegionOfInterest(8, 8, 16, 16)); //the area where the laserCan can sense objects
    } catch (ConfigurationFailedException f) {
        // displays if the code doesn't work properly
        System.out.println("Error configuring laser CAN");
        System.out.println(f.getMessage());
    }
  }

  /**
   * Sets the spinning speed of the intake motors.
   * @param speed
   */
  public void runIntake(double speed) {
    intakeEntrance.set(speed);
    intakeRamp.set(speed);
  }

  /**
   * Sets the spinning speed of only the upper ramp intake motor
   * @param speed
   */
  public void runRampIntakeOnly(double speed) {
    intakeRamp.set(speed);
  }

  public void runIntakeEntranceOnly(double speed) {
    intakeEntrance.set(speed);
  }

  public boolean isStoredIntake() {
  //checks if the laserCan distance is more than 177.9 millimeters or less than 177.9 millimeters
  // if it returns true, there is a note in the intake
    return getIntakeLaserCanDistance() <= 177.8;
  }

    //Method that returns the distance from the laserCAN in millimeters
    public double getIntakeLaserCanDistance() {
      //returns the distance from the laserCAN in millimeters
      return intakeLaser.getMeasurement().distance_mm;
    }

  /**
   * This function changes the speed of the intake if a game piece is detected.
   * @param speed Speed to set the intake to.
   */
  public void intakeSpeedIncrease(double normalSpeed, double fastSpeed){
    if (limelight.getTargetDetected()){ // If the piece is detected, increase speed.
      runIntake(fastSpeed); 
    } 
    else { // Otherwise, run at the normal speed.
      runIntake(normalSpeed);  
    } 
  }


  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}