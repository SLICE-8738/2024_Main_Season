// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Shooter;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.shuffleboard.SimpleWidget;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.ShooterMath;
import frc.robot.ShooterMath.ShotDetails;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Limelights;
import frc.robot.subsystems.Shooter;

/**
 * Aims and spins up the flywheels to prepare for a shot into the speaker
 */
public class PrepareShooterCommand extends Command {
  Shooter m_shooter;

  ShuffleboardTab shooterTestTab;
  SimpleWidget originalVelocityWidget, velocityMultiplierWidget, distanceWidget, currentAngleWidget, desiredAngleWidget, desiredSpeedWidget;
  /** Creates a new ShootCommand. */
  public PrepareShooterCommand(Shooter shooter) {
    m_shooter = shooter;

    shooterTestTab = Shuffleboard.getTab("Shooter Testing");
    originalVelocityWidget = shooterTestTab.add("Original Flywheel Velocity", 0);
    velocityMultiplierWidget = shooterTestTab.add("Flywheel Velocity Multiplier", 1);
    distanceWidget = shooterTestTab.add("Robot Distance", 0);
    //currentAngleWidget = shooterTestTab.add("Current Shooter Angle", 0);
    desiredAngleWidget = shooterTestTab.add("Desired Shooter Angle", 0);
    desiredSpeedWidget = shooterTestTab.add("Desired Flywheel Speed", 0); 

    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(shooter);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // Figures our the current position
    //Translation2d currentPosition = m_drivetrain.getPose().getTranslation();
    // Determines distance to the speaker
    //double distanceToSpeaker = currentPosition.getDistance(Constants.kFieldPositions.SPEAKER_POSITION);
    double distanceToSpeaker = Limelights.getShooterLimelight().getCameraTargetSpacePose().getX();
    distanceWidget.getEntry().setDouble(distanceToSpeaker);

    // Uses distance info the calculate optimal shot
    ShotDetails shotDetails = ShooterMath.getShot(distanceToSpeaker);
    // Sets the flywheel speed and aim angle to the appropriate values 
    double multiplier = velocityMultiplierWidget.getEntry().getDouble(1);
    originalVelocityWidget.getEntry().setDouble(shotDetails.getFlywheelVelocity());
    double speed = shotDetails.getFlywheelVelocity() * multiplier;
    m_shooter.spinFlywheel(speed);
    
    //currentAngleWidget.getEntry().setDouble(m_shooter.getAngle());
    desiredSpeedWidget.getEntry().setDouble(speed);
    desiredAngleWidget.getEntry().setDouble(shotDetails.getShooterAngle());
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
