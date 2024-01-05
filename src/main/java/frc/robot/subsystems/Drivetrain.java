/// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import frc.robot.*;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
//import edu.wpi.first.math.VecBuilder;
//import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.*;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;

import java.util.List;

import com.kauailabs.navx.frc.AHRS;

import com.pathplanner.lib.util.PathPlannerLogging;

public class Drivetrain extends SubsystemBase {

  // private final SparkMaxSwerveModule leftModuleFront, leftModuleBack,
  // rightModuleFront, rightModuleBack;
  private final SwerveModule[] swerveMods;

  private final SwerveDriveOdometry m_swerveDrivetrainOdometry;

  private final AHRS navXGyro;

  public final Field2d m_field2d;

  private final Timer autoTrajectoryTimer;

  private Trajectory currentAutoTrajectory;

  private Rotation2d fieldOrientedOffset;

  public double speedPercent;

  private Rotation2d angle = new Rotation2d();

  public final SendableChooser<SwerveModule> testModuleChooser = new SendableChooser<SwerveModule>();

  /** Creates a new Drivetrain. */
  public Drivetrain() {

    // The angle offset arguments for these swerve modules are placeholders for now
    /*
     * leftModuleFront = new
     * SparkMaxSwerveModule(Constants.kDrivetrain.Mod0.DRIVE_MOTOR_ID,
     * Constants.kDrivetrain.Mod0.ANGLE_MOTOR_ID,
     * Constants.kDrivetrain.Mod0.ANGLE_OFFSET.getRadians());
     * leftModuleBack = new
     * SparkMaxSwerveModule(Constants.kDrivetrain.Mod1.DRIVE_MOTOR_ID,
     * Constants.kDrivetrain.Mod1.ANGLE_MOTOR_ID,
     * Constants.kDrivetrain.Mod1.ANGLE_OFFSET.getRadians());
     * rightModuleFront = new
     * SparkMaxSwerveModule(Constants.kDrivetrain.Mod2.DRIVE_MOTOR_ID,
     * Constants.kDrivetrain.Mod2.ANGLE_MOTOR_ID,
     * Constants.kDrivetrain.Mod2.ANGLE_OFFSET.getRadians());
     * rightModuleBack = new
     * SparkMaxSwerveModule(Constants.kDrivetrain.Mod3.DRIVE_MOTOR_ID,
     * Constants.kDrivetrain.Mod3.ANGLE_MOTOR_ID,
     * Constants.kDrivetrain.Mod3.ANGLE_OFFSET.getRadians());
     */

    swerveMods = new SwerveModule[] {
      new SwerveModule(0, Constants.kDrivetrain.Mod0.CONSTANTS),
      new SwerveModule(1, Constants.kDrivetrain.Mod1.CONSTANTS),
      new SwerveModule(2, Constants.kDrivetrain.Mod2.CONSTANTS),
      new SwerveModule(3, Constants.kDrivetrain.Mod3.CONSTANTS)
    };

    navXGyro = new AHRS(Constants.kDrivetrain.NAVX_PORT);

    Timer.delay(1.0);
    resetModulesToAbsolute();
    resetHeading();

    m_field2d = new Field2d();

    autoTrajectoryTimer = new Timer();

    // Creates and pushes Field2d to SmartDashboard.
    SmartDashboard.putData(m_field2d);

    m_swerveDrivetrainOdometry = new SwerveDriveOdometry(
        Constants.kDrivetrain.kSwerveKinematics,
        getRotation2d(),
        getPositions(),
        new Pose2d(8.28, 4, Rotation2d.fromDegrees(0)));

    /*m_swerveDrivetrainOdometry = new SwerveDrivePoseEstimator(
      Constants.kDrivetrain.kSwerveKinematics, 
      getRotation2d(), 
      getPositions(), 
      new Pose2d(8.28, 4, Rotation2d.fromDegrees(180)),
      VecBuilder.fill(
        0.5, 
        0.5, 
        0.5),
      VecBuilder.fill(0.9, 0.9, 0.9));*/

    fieldOrientedOffset = new Rotation2d();

    speedPercent = 1;

    PathPlannerLogging.setLogActivePathCallback(this::setField2d);

    testModuleChooser.setDefaultOption("Left Front", swerveMods[0]);

    testModuleChooser.addOption("Left Back", swerveMods[1]);
    testModuleChooser.addOption("Right Front", swerveMods[2]);
    testModuleChooser.addOption("Right Back", swerveMods[3]);

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run

    updateOdometry();

    m_field2d.setRobotPose(getPose());

    SmartDashboard.putNumber("Left Front Distance", getPositions()[0].distanceMeters);
    SmartDashboard.putNumber("Left Back Distance", getPositions()[1].distanceMeters);
    SmartDashboard.putNumber("Right Front Distance", getPositions()[2].distanceMeters);
    SmartDashboard.putNumber("Right Back Distance", getPositions()[3].distanceMeters);

  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation

    for(SwerveModule mod : swerveMods) {

      mod.setSimulationPosition();

    }

  }

  /**
   * Sets the idle mode of all drive motors to either brake mode or coast mode.
   * 
   * @param enableBrakeMode Whether or not the idle mode of all
   *                        drive motors should be set to brake mode(false to set
   *                        to coast mode).
   * 
   */
  public void setDriveIdleMode(boolean enableBrakeMode) {

    for(SwerveModule mod : swerveMods) {

      mod.setDriveIdleMode(enableBrakeMode);

    }

  }

  /**
   * Sets the idle mode of all angle motors to either brake mode or coast mode.
   * 
   * @param enableBrakeMode Whether or not the idle mode of all
   *                        angle motors should be set to brake mode(false to set
   *                        to coast mode).
   * 
   */
  public void setAngleIdleMode(boolean enableBrakeMode) {

    for(SwerveModule mod : swerveMods) {

      mod.setAngleIdleMode(enableBrakeMode);

    }

  }

  /**
   * Drives the robot at either given field-relative X, Y, and rotational
   * velocities or given
   * robot-relative forward, sideways, and rotational velocities.
   * 
   * <p>
   * If using robot-relative velocities, the X component of the Translation2d
   * object should be the forward velocity
   * and the Y component should be the sideways velocity.
   * 
   * @param transform       A Transform2d object representing either the desired
   *                        field-relative velocities in meters/second for the
   *                        robot to move at along the X and Y axes of the
   *                        field(forwards/backwards from driver POV), or the
   *                        desired robot-relative forward
   *                        and sideways velocities in meters/second for the robot
   *                        to move at, as well as the desired velocity in
   *                        radians/second for the
   *                        robot to rotate at.
   * 
   * @param isOpenLoop      Whether the accordingly generated states for the given
   *                        velocities should be set using open loop control for
   *                        the drive motors
   *                        of the swerve modules.
   * @param isFieldRelative Whether the given velocities are relative to the field
   *                        or not.
   */
  public void swerveDrive(Transform2d transform, boolean isOpenLoop, boolean isFieldRelative) {

    SwerveModuleState[] states = toModuleStates(transform, isFieldRelative);

    for(SwerveModule mod : swerveMods) {

      mod.setDesiredState(states[mod.moduleNumber], isOpenLoop);

    }

    angle = angle.plus(transform.getRotation().times(0.02));

  }

  /**
   * Runs a single module with the corresponding motor IDs to the given module number, 
   * using the state generated for the module selected on the Test Module sendable chooser.
   * 
   * @param transform A Transform2d object representing the desired
   *                  field-relative velocities in meters/second that the
   *                  robot would move at along the X and Y axes of the field, 
   *                  as well as the desired velocity in radians/second that 
   *                  the robot would rotate at.
   * 
   * @param idModuleNumber The module whose motor IDs should be run.
   */
  public void testSelectedModule(Transform2d transform, int idModuleNumber) {

    SwerveModuleState[] states = toModuleStates(transform, true);

    swerveMods[idModuleNumber].setDesiredState(states[testModuleChooser.getSelected().moduleNumber], false);

  }

  /**
   * Sends a given list of poses to the Field2d trajectory object.
   * 
   * @param poses The desired poses to send to the Field2d trajectory object.
   */
  public void setField2d(List<Pose2d> poses) {

    // Pushes the trajectory to Field2d.
    m_field2d.getObject("Trajectory").setPoses(poses);

  }

  /**
   * Resets and starts a timer in order to provide a time
   * since the beginning of the trajectory to sample from.
   */
  public void startAutoTrajectoryTimer() {

    autoTrajectoryTimer.reset();
    autoTrajectoryTimer.start();

  }

  /**
   * Sets the auto trajectory used to sample the state at each time step from.
   * 
   * @param trajectory The current auto trajectory to sample from.
   */
  public void setCurrentAutoTrajectory(Trajectory trajectory) {

    currentAutoTrajectory = trajectory;

  }

  /**
   * Samples and obtains the rotation at the current time step of the current auto
   * trajectory.
   * 
   * @return The rotation of the robot of at the current time step of the current
   *         auto trajectory.
   */
  public Rotation2d getAutoTrajectoryRotation() {

    return currentAutoTrajectory.sample(autoTrajectoryTimer.get()).poseMeters.getRotation();

  }

  /**
   * Updates the drivetrain odometry object to the robot's current position on the
   * field.
   * 
   * @return The new updated pose of the robot.
   */
  public Pose2d updateOdometry() {

    return m_swerveDrivetrainOdometry.update(getRotation2d(), getPositions());

  }

  /**
   * Returns the current pose of the robot without updating
   * the odometry.
   * 
   * @return The current estimated pose of the robot.
   */
  public Pose2d getPose() {
    return m_swerveDrivetrainOdometry.getPoseMeters();
  }

  /**
   * Obtains and returns the current positions of all drivetrain swerve modules.
   * 
   * @return The current positions of all drivetrain swerve modules.
   */
  public SwerveModulePosition[] getPositions() {

    SwerveModulePosition[] positions = new SwerveModulePosition[4];

    for(SwerveModule mod : swerveMods) {

      positions[mod.moduleNumber] = mod.getPosition();

    }

    return positions;

  }

  /**
   * Obtains and returns the current states of all drivetrain swerve modules.
   * 
   * @return The current states of all drivetrain swerve modules.
   */
  public SwerveModuleState[] getStates() {

    SwerveModuleState[] states = new SwerveModuleState[4];

    for(SwerveModule mod : swerveMods) {

      states[mod.moduleNumber] = mod.getState();

    }

    return states;

  }

  /**
   * Obtains and returns the target states that the drivetrain swerve modules have
   * been set to.
   * 
   * @return The target states that the drivetrain swerve modules have been set
   *         to.
   */
  public SwerveModuleState[] getTargetStates() {

    SwerveModuleState[] targetStates = new SwerveModuleState[4];

    for(SwerveModule mod : swerveMods) {

      targetStates[mod.moduleNumber] = mod.getTargetState();

    }

    return targetStates;

  }

  /**
   * Obtains and returns the current absolute angle readings
   * in degrees from the CANCoders of all swerve modules without offsets.
   * 
   * @return The current absolute angle readings in degrees from the CANCoders
   *         of all swerve modules without offsets.
   */
  public double[] getCANCoderAngles() {

    double[] angles = new double[4];

    for(SwerveModule mod : swerveMods) {

      angles[mod.moduleNumber] = mod.getCANcoder().getDegrees();

    }

    return angles;

  }

  /**
   * Sets the positions of the integrated angle motor
   * encoders of all swerve modules to the absolute position
   * readings of the CANCoders with their offsets being taken
   * into account.
   */
  public void resetModulesToAbsolute() {

    for(SwerveModule mod : swerveMods) {

      mod.resetToAbsolute();

    }

  }

  /**
   * Resets the position of the odometry object using a specified position.
   * 
   * @param position The desired position to reset the odometry of the robot to.
   */
  public void resetOdometry(Pose2d position) {

    m_swerveDrivetrainOdometry.resetPosition(getRotation2d(), getPositions(), position);

  }

  /**
   * 
   * @return
   */
  public void resetFieldOrientedHeading() {
    double error = getHeading() - 180;
    fieldOrientedOffset = Rotation2d.fromDegrees(error);
  }

  public void reverseFieldOrientedHeading() {
    double error = getHeading();
    fieldOrientedOffset = Rotation2d.fromDegrees(error);
  }

  /**
   * Obtains and returns the current heading of the robot as a Rotation2d from the
   * gyro object.
   * 
   * @return The current heading of the robot as a Rotation2d.
   */
  public Rotation2d getRotation2d() {

    return RobotBase.isReal()? Rotation2d.fromDegrees(getHeading()) : angle;

  }

  /**
   * Obtains and returns the current heading of the robot going positive
   * counter-clockwise from 0 to 360 degrees from the gyro object.
   *
   * @return The current heading of the robot going counter-clockwise positive
   *         from 0 to 360 degrees.
   */
  public double getHeading() {

    return Constants.kDrivetrain.INVERT_GYRO ? -navXGyro.getYaw() + 180 : navXGyro.getYaw() + 180;

  }

  public Rotation2d getRotationalVelocity() {

    return Rotation2d.fromDegrees(navXGyro.getRate());

  }

  public boolean facingDoubleSub() {
    double degrees = getPose().getRotation().getDegrees();
    return (degrees > 0 && degrees < 45) || (degrees > 315 && degrees < 360) || (degrees > 135 && degrees < 225);
  }

  /**
   * Obtains and returns the current pitch of the robot from -180 to 180 degrees,
   * with an offset of 1 degree from the gyro object.
   * 
   * @return The current pitch of the robot from -180 to 180 degrees, with an
   *         offset of 1 degree.
   */
  public double getPitch() {

    return navXGyro.getPitch() + 1;

  }

  /**
   * Obtains and returns the current roll of the robot from -180 to 180 degrees,
   * with an offset of 1.7 degrees from the gyro object.
   * 
   * @return The current pitch of the robot from -180 to 180 degrees, with an
   *         offset of 1.7 degrees.
   */
  public double getRoll() {

    return navXGyro.getRoll() + 1.7;

  }

  /**
   * Resets the gyro yaw axis to a heading of 0.
   */
  public void resetHeading() {

    navXGyro.reset();

  }

  public SwerveModuleState[] toModuleStates(Transform2d transform, boolean isFieldRelative) {

    Rotation2d rotationWithOffset = getRotation2d().minus(fieldOrientedOffset);
    if (rotationWithOffset.getDegrees() > 360) {
      rotationWithOffset.minus(Rotation2d.fromDegrees(360));
    }
    if (rotationWithOffset.getDegrees() < 0) {
      rotationWithOffset.plus(Rotation2d.fromDegrees(360));
    }

    SwerveModuleState[] states = Constants.kDrivetrain.kSwerveKinematics.toSwerveModuleStates(
        isFieldRelative
            ? ChassisSpeeds.discretize(
                ChassisSpeeds.fromFieldRelativeSpeeds(
                  transform.getX() * speedPercent,
                  transform.getY() * speedPercent,
                  transform.getRotation().getRadians() * speedPercent,
                  rotationWithOffset),
                0.02)
            : ChassisSpeeds.discretize(
                new ChassisSpeeds(
                  transform.getX() * speedPercent, 
                  transform.getY() * speedPercent,
                  transform.getRotation().getRadians() * speedPercent),
                0.02));

    SwerveDriveKinematics.desaturateWheelSpeeds(states, Constants.kDrivetrain.MAX_LINEAR_VELOCITY);

    return states;

  }

  /**
   * Calculates and returns the current chassis speeds of the drivetrain using
   * the average forward and sideways velocities of the individual swerve modules
   * and the rotational velocity measured by the gyro.
   * 
   * @return The current chassis speeds of the drivetrain.
   */
  public ChassisSpeeds getChassisSpeeds() {

    double forwardVelocity = 0;
    double sidewaysVelocity = 0;

    for(SwerveModule mod : swerveMods) {

      forwardVelocity += mod.getState().speedMetersPerSecond * mod.getState().angle.getSin();
      sidewaysVelocity += mod.getState().speedMetersPerSecond * mod.getState().angle.getCos();

    }

    forwardVelocity /= 4;
    sidewaysVelocity /= 4;

    return new ChassisSpeeds(forwardVelocity, sidewaysVelocity, getRotationalVelocity().getRadians());

  }

  /**
   * Sets the desired states of all drivetrain motors to the given robot-relative chassis 
   * speeds after being converted to swerve module states.
   * 
   * @param speeds The desired chassis speeds to move the drivetrain at.
   * 
   */
  public void setChassisSpeeds(ChassisSpeeds speeds) {

    setModuleStates(Constants.kDrivetrain.kSwerveKinematics.toSwerveModuleStates(ChassisSpeeds.discretize(speeds, 0.02)));

  }

  /**
   * Sets the desired states of all drivetrain swerve modules to a specified
   * arrary of states using
   * closed loop control for the drive motors of the swerve modules.
   * 
   * @param states The desired states for all drivetrain swerve modules to be set
   *               to.
   */
  public void setModuleStates(SwerveModuleState[] states) {

    SwerveDriveKinematics.desaturateWheelSpeeds(states, Constants.kDrivetrain.MAX_LINEAR_VELOCITY);

    for(SwerveModule mod : swerveMods) {

      mod.setDesiredState(states[mod.moduleNumber], false);

    }

  }

  /**
   * Sets the drive and angle motors of all swerve modules to given drive and
   * angle motor
   * percent outputs.
   * 
   * @param drivePercentOutput The percent output between -1 and 1 to set all
   *                           drive motors to.
   * @param anglePercentOutput The percent output between -1 and 1 to set all
   *                           angle motors to.
   */
  public void setPercentOutput(double drivePercentOutput, double anglePercentOutput) {

    for(SwerveModule mod : swerveMods) {

      mod.setPercentOutput(drivePercentOutput, anglePercentOutput);

    }

  }

  /**
   * Sets all drivetrain swerve modules to states with speeds of 0 and the current
   * angles of the modules.
   */
  public void stopDrive() {

    for(SwerveModule mod : swerveMods) {

      mod.setDesiredState(new SwerveModuleState(), false);

    }

  }

}