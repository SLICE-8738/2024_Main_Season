// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Drivetrain;

import frc.robot.Constants;
import frc.robot.subsystems.Drivetrain;
import frc.slicelibs.PolarJoystickFilter;
import frc.slicelibs.util.config.JoystickFilterConfig;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * Maintains driver control of robot translation, but automatically turns to face the speaker.
 */
public class AlignWithSpeakerCommand extends Command {
  /** Creates a new SwerveDriveCommand. */
  private final Drivetrain m_drivetrain;

  private final GenericHID m_driverController;
  private final PolarJoystickFilter translationFilter;

  private final boolean m_isOpenLoop;
  private final boolean m_isFieldRelative;

  private final PIDController rotationController;

  public AlignWithSpeakerCommand(Drivetrain drivetrain, GenericHID driverController, boolean isOpenLoop,
      boolean isFieldRelative) {
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(drivetrain);

    m_drivetrain = drivetrain;

    m_driverController = driverController;

    m_isOpenLoop = isOpenLoop;
    m_isFieldRelative = isFieldRelative;

    translationFilter = new PolarJoystickFilter(new JoystickFilterConfig(
        0.07,
        0.85,
        Constants.OperatorConstants.driveExponent,
        Constants.OperatorConstants.driveExponentPercent));

    rotationController = new PIDController(Constants.kDrivetrain.kPSpeakerAlignRotation, Constants.kDrivetrain.kISpeakerAlignRotation, Constants.kDrivetrain.kDSpeakerAlignRotation);
    rotationController.enableContinuousInput(0, 360);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {

    m_drivetrain.setPercentOutput(0, 0);

  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {

    double[] translation = translationFilter.filter(m_driverController.getRawAxis(1), m_driverController.getRawAxis(0));

    double translationX = translation[0] * Constants.kDrivetrain.MAX_LINEAR_VELOCITY;
    double translationY = translation[1] * Constants.kDrivetrain.MAX_LINEAR_VELOCITY;

    // find the angle to speaker
    Translation2d currentPosition = m_drivetrain.getPose().getTranslation();
    Translation2d directionToSpeaker = Constants.kFieldPositions.SPEAKER_POSITION.minus(currentPosition);
    Rotation2d targetAngle = directionToSpeaker.getAngle();

    // Run PID Controller
    double turnAmount = rotationController.calculate(m_drivetrain.getHeading(), targetAngle.getDegrees());

    m_drivetrain.swerveDrive(
        new Transform2d(new Translation2d(translationX, translationY), new Rotation2d(turnAmount)),
        m_isOpenLoop,
        m_isFieldRelative);

  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {

    m_drivetrain.swerveDrive(
      new Transform2d(), 
      m_isOpenLoop,
      m_isFieldRelative);

  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {

    return false;

  }

}