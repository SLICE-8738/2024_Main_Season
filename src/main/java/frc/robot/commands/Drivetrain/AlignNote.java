// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Drivetrain;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Limelight;

public class AlignNote extends Command { 
  private final Drivetrain m_drivetrain;
  public AlignNote(Drivetrain drivetrain) {
    addRequirements(drivetrain);
    m_drivetrain = drivetrain;

    Constants.kAutonomous.ALIGN_CONTROLLER_X.setSetpoint(0);
    Constants.kAutonomous.ALIGN_CONTROLLER_ROTATION.setSetpoint(0);

  }




  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    final double translationX =  Constants.kAutonomous.ALIGN_CONTROLLER_X.calculate(m_drivetrain.getRobotTargetSpace().getY());
    final double rotation = Constants.kAutonomous.ALIGN_CONTROLLER_ROTATION.calculate(m_drivetrain.getRobotTargetSpace().getRotation().getRadians());
    m_drivetrain.swerveDrive(new Transform2d(translationX, 0, new Rotation2d(rotation)), false, false);

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_drivetrain.swerveDrive(new Transform2d(), false, false);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
