package frc.robot.commands;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.DriveSub;

public class Drive extends CommandBase {
  private final DriveSub drive;
  private final XboxController controller;
  private final boolean useTank = SmartDashboard.getBoolean("useTank", false);

  public Drive(DriveSub drive, XboxController controller) {
    this.drive = drive;
    this.controller = controller;

    addRequirements(drive);
  }

  @Override
  public void initialize() {

  }

  @Override
  public void execute() {
    if (useTank) {
      double leftSpeed = controller.getLeftY();
      double rightSpeed = controller.getRightY();
      drive.tank(leftSpeed, rightSpeed);
    } else {
      double speed = controller.getLeftY();
      double steering = controller.getRightX();
      drive.arcade(speed, steering);
    }
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}
