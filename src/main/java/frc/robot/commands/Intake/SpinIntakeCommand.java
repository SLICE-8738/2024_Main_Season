// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Intake;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;

/**
 * Runs the intake wheels (including the vectoring ramp wheels) in order to pick up a note from the ground
 */
public class SpinIntakeCommand extends Command {
  private final double normalSpeed = 0.5; // Normal speed for the intake
  private final double fastSpeed = 1.0; // Fast speed for the intake
  private final Intake m_intake;
  private final Shooter m_shooter;
  private boolean note;
  /** Creates a new SpinIntakeCommand. */
  public SpinIntakeCommand(Intake intake, Shooter shooter) {
    m_intake = intake;
    m_shooter = shooter;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(intake);
  }
  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    m_intake.intakeSpeedIncrease(normalSpeed, fastSpeed);




  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    // Check if the shooter is stowed
    if (m_shooter.isStowed(2)) {
      // If it is, stop the lower intake wheels but continuing spinning the ramp wheels to send the note into the shooter
      m_intake.runIntakeEntranceOnly(0);
    }else {
      // If it isn't, stop all motors to wait for the shooter to be stowed
      m_intake.runIntake(0);
    }


  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {


  if (note && !m_intake.isStoredIntake()) {
    return true;

  } else {
    note = m_intake.isStoredIntake();
    return false;
  }
}

}