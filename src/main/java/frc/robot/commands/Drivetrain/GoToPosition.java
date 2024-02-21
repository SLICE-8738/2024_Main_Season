// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Drivetrain;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Limelight;

public class GoToPosition extends Command {

  // initializing the needed variables
  private Drivetrain m_Drivetrain;


  private PIDController rotationController;

  /** Creates a new GoToPosition. */
  public GoToPosition(Drivetrain drivetrain, int aprilTagTarget, double targetAngle) {
    m_Drivetrain = drivetrain;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(m_Drivetrain);




    rotationController = new PIDController(Constants.kAutonomous.kPSpeakerAlignRotation, Constants.kAutonomous.kISpeakerAlignRotation, Constants.kAutonomous.kDSpeakerAlignRotation);
    rotationController.enableContinuousInput(0, 360);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {

    double turnAmount = rotationController.calculate(m_Drivetrain.getHeading(), targetAngle.getDegrees());

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
