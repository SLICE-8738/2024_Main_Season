// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commands.Indexer.NudgeIndexer;
import frc.robot.commands.Indexer.StoreNote;
import frc.robot.commands.Intake.SpinIntakeCommand;
import frc.robot.commands.Shooter.ShootCommand;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {

  Drivetrain m_Drivetrain = new Drivetrain();

  Intake intake = new Intake();
  SpinIntakeCommand runIntake = new SpinIntakeCommand(intake);

  Indexer m_Indexer = new Indexer();
  NudgeIndexer m_NudgeIndexer = new NudgeIndexer(m_Indexer);
  StoreNote m_StoreNote = new StoreNote(m_Indexer, intake);

  Shooter m_Shooter = new Shooter();
  ShootCommand m_ShooterCommand = new ShootCommand(m_Shooter, m_Indexer, m_Drivetrain, Button.controller2);


  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the trigger bindings
    configureBindings();
    System.out.println(ShooterMath.getShot(3));
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    Button.square2.whileTrue(runIntake);
    Button.cross2.onTrue(m_NudgeIndexer);
    Button.circle2.onTrue(m_StoreNote);
    Button.leftTrigger2.whileTrue(m_ShooterCommand);
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return null;
  }
}
