// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Elevator;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Elevator;

/**
 * Moves the elevator down to the default, stowed position, potentially bringing the robort up the chain
 */
public class ElevatorDownCommand extends Command {

  private final Elevator m_elevator;

  /** Creates a new ElevatorDownCommand. */
  public ElevatorDownCommand(Elevator elevator) {
    m_elevator = elevator;
    // No requirements needed since it uses PIDs
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_elevator.setPosition(0);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return true;
  }
}
