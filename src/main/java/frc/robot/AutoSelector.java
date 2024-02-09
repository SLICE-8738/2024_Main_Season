// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import frc.robot.commands.Shooter.PrepareShooterCommand;
import frc.robot.commands.Shooter.ReverseShooterCommand;
import frc.robot.commands.Shooter.SpinDownCommand;
import frc.robot.commands.Shooter.SpinFlywheelCommand;
import frc.robot.commands.Indexer.NudgeIndexer;
import frc.robot.commands.Indexer.StoreNote;
import frc.robot.commands.Intake.SpinIntakeCommand;
import frc.robot.commands.Shooter.AutoShootCommand;
import frc.robot.commands.Shooter.SpinUpCommand;
import frc.robot.commands.Shooter.StowShooterCommand;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;

import java.util.Optional;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.util.PIDConstants;
import com.pathplanner.lib.util.ReplanningConfig;

/**
 * This class primarily manages the creation and updating of the autonomous mode
 * and starting position sendable choosers on Shuffleboard.
 * 
 * <p>
 * {@link SendableChooser See SendableChooser class here}
 */
public class AutoSelector {

    public enum StartingPosition {

        AMP_SIDE("Amp Side"),
        MIDDLE("Middle"),
        SOURCE_SIDE("Source Side");

        public final String value;

        StartingPosition(String value) {

            this.value = value;

        }

    }

    public enum DesiredMode {

        TEST_PATH_MODE("Test Path"),
        SCORE_4_SPEAKER("Score 4 Speaker"),
        SCORE_1_AMP_AND_3_SPEAKER("Score 1 Amp And 3 Speaker"),
        SCORE_3_SPEAKER("Score 3 Speaker"),
        SCORE_1_AMP_AND_2_SPEAKER("Score 1 Amp And 2 Speaker");

        public final String value;

        DesiredMode(String value) {

            this.value = value;

        }

    }

    public static StartingPosition storedStartingPosition;
    public DesiredMode storedDesiredMode;

    public SendableChooser<StartingPosition> startingPositionChooser;
    public SendableChooser<DesiredMode> modeChooser;

    private Optional<PathPlannerAuto> autoRoutine = Optional.empty();

    private Pose2d initialAutoPose;

    public double initialAutoPoseXOffset = 0;
    public double initialAutoPoseYOffset = 0;
    public double initialAutoPoseRotationOffset = 0;

    private final Drivetrain m_drivetrain;

    public AutoSelector(Drivetrain drivetrain, Shooter shooter, Indexer indexer, Intake intake) {

        m_drivetrain = drivetrain;

        startingPositionChooser = new SendableChooser<StartingPosition>();

        startingPositionChooser.setDefaultOption("Amp Side", StartingPosition.AMP_SIDE);

        startingPositionChooser.addOption("Middle", StartingPosition.MIDDLE);
        startingPositionChooser.addOption("Source Side", StartingPosition.SOURCE_SIDE);

        modeChooser = new SendableChooser<DesiredMode>();

        modeChooser.setDefaultOption(DesiredMode.TEST_PATH_MODE.value, DesiredMode.TEST_PATH_MODE);

        modeChooser.addOption(DesiredMode.SCORE_4_SPEAKER.value, DesiredMode.SCORE_4_SPEAKER);
        modeChooser.addOption(DesiredMode.SCORE_1_AMP_AND_3_SPEAKER.value, DesiredMode.SCORE_1_AMP_AND_3_SPEAKER);
        modeChooser.addOption(DesiredMode.SCORE_3_SPEAKER.value, DesiredMode.SCORE_3_SPEAKER);
        modeChooser.addOption(DesiredMode.SCORE_1_AMP_AND_2_SPEAKER.value, DesiredMode.SCORE_1_AMP_AND_2_SPEAKER);

        AutoBuilder.configureHolonomic(
            m_drivetrain::getPose,
            m_drivetrain::resetOdometry,
            m_drivetrain::getChassisSpeeds,
            m_drivetrain::setChassisSpeeds,
            new HolonomicPathFollowerConfig(
                new PIDConstants(Constants.kDrivetrain.TRANSLATION_KP),
                new PIDConstants(Constants.kDrivetrain.ROTATION_KP),
                Constants.kDrivetrain.MAX_MODULE_VELOCITY,
                Constants.kDrivetrain.DRIVE_BASE_RADIUS,
                new ReplanningConfig(false, false)),
            () -> DriverStation.getAlliance().get() == Alliance.Red,
            m_drivetrain);

        NamedCommands.registerCommand("Shooter Spin Up", new SpinUpCommand(shooter));
        NamedCommands.registerCommand("Shooter Prepare", new PrepareShooterCommand(shooter, indexer , drivetrain));
        NamedCommands.registerCommand("Shooter Reverse", new ReverseShooterCommand(shooter, indexer));
        NamedCommands.registerCommand("Shooter Shoot", new AutoShootCommand(shooter, indexer, drivetrain));
        NamedCommands.registerCommand("Shooter Spin Down", new SpinDownCommand(shooter));
        NamedCommands.registerCommand("Shooter Spin Flywheel", new SpinFlywheelCommand(shooter, drivetrain));
        NamedCommands.registerCommand("Shooter Stow", new StowShooterCommand(shooter));
        NamedCommands.registerCommand("Indexer Nudge", new NudgeIndexer(indexer));
        NamedCommands.registerCommand("Indexer Store Note", new StoreNote(indexer, intake));
        NamedCommands.registerCommand("Intake Spin", new SpinIntakeCommand(intake, shooter));
        
    }

    public void updateAutoSelector() {

        StartingPosition startingPosition = startingPositionChooser.getSelected();
        DesiredMode desiredMode = modeChooser.getSelected();

        if (storedStartingPosition != startingPosition || storedDesiredMode != desiredMode) {

            System.out.println("Auto selection changed, updating creator; Starting Position: " + startingPosition.name()
                    + ", Desired Mode: " + desiredMode.name());

            autoRoutine = getAutoRoutineForParams(startingPosition, desiredMode);

            updateInitialAutoPoseOffset(desiredMode);

        }

        storedStartingPosition = startingPosition;
        storedDesiredMode = desiredMode;

    }

    private Optional<PathPlannerAuto> getAutoRoutineForParams(StartingPosition position, DesiredMode mode) {

        try {

            return Optional.of(new PathPlannerAuto(mode == DesiredMode.TEST_PATH_MODE? mode.value : position.value + " " + mode.value));

        }
        catch(Exception e) {

            DriverStation.reportError(
                "Could not construct a valid PathPlannerauto for selected starting position and mode. Error: " + e.toString() + " " + e.getMessage(), true);
            return Optional.empty();

        }
 
    }

    public void updateInitialAutoPoseOffset(DesiredMode mode) {

        Pose2d currentPose = m_drivetrain.getPose();

        initialAutoPose = PathPlannerAuto.getStaringPoseFromAutoFile(mode.value);

        if (currentPose != null && initialAutoPose != null) {

            Transform2d offset = initialAutoPose.minus(currentPose);

            initialAutoPoseXOffset = offset.getX();
            initialAutoPoseYOffset = offset.getY();
            initialAutoPoseRotationOffset = offset.getRotation().getDegrees();

        }

    }

    public void reset() {

        autoRoutine = Optional.empty();
        storedDesiredMode = null;

        initialAutoPose = null;

    }

    public PathPlannerAuto getAutoRoutine() {

        return autoRoutine.get();

    }

    public String getStoredDesiredMode() {

        if (storedDesiredMode != null) {

            return storedDesiredMode.value;

        } else {

            return "None Stored";

        }

    }

    public String getStoredStartingPosition() {

        if (storedStartingPosition != null) {

            return storedStartingPosition.value;

        } else {

            return "None Stored";

        }

    }

}