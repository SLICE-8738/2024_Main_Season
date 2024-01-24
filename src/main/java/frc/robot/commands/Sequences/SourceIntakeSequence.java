// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.Sequences;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.Indexer.StoreNote;
import frc.robot.commands.Shooter.ReverseShooterCommand;
import frc.robot.commands.Shooter.StowShooterCommand;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class SourceIntakeSequence extends SequentialCommandGroup {
  /** Creates a new SourceIntakeSequence. */
  public SourceIntakeSequence(Shooter shooter, Intake intake, Indexer indexer) {
    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    addCommands(new StowShooterCommand(shooter),
    new ReverseShooterCommand(shooter, indexer),
    new StoreNote(indexer, intake));
  }
}
