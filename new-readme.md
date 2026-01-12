Vector Calculator is a program for TASing jumps in Super Mario Odyssey by Xiivler. Special thanks to MonsterDruide1 for some menu code and to Tetraxile, whose absolute joystick mod is bundled in this program's release.

Check out the [tutorial](tutorial.md) for step-by-step instructions on using the mod.

Vector Calculator has 3 modes: Solve, Calculate (Solve Dives), and Calculate, of which Solve is the most automated and Calculate is the least.

Solve: Determines optimal durations and inputs for each piece of movement

Calculate (Solve Dives): Determines optimal inputs for each piece of movement, ensures that the dive before the cap bounce is a possible duration, and lengthens or shortens the last dive until it lands at the target Y position.

Calculate: Determines optimal inputs for each piece of movement given the durations the user inputted

Vector Calculator has several properties that can be configured by the user. Some of these are only visible depending on the settings of other properties.

General Properties:

Calculator Mode: The mode the calculator is running in (Solve, Calculate (Solve Dives), or Calculate)

Intial Coordinates: The coordinates of Mario one frame before he jumps

Calculate Using: Whether the jump is calculated based on an initial angle, target angle, or target coordinates

Initial Angle: The direction Mario is moving in one frame before the jump
Target Angle: The overall direction Mario is moving in during the jump
Target Coordinates: The position where Mario lands at the end of the jump
Target Y Position (appears if the user is calculating using an initial or target angle): The Y position where Mario lands at the end of the jump

Solve for Initial Angle (RCV + Solve mode only): Whether to solve for the optimal initial angle

Initial Movement Category: The category of the initial movement (ex. Jump, Roll, or RCV)
    Optimal Distance Motion finds the optimal initial motion type for the jump (either a triple jump, MCCT RCV, or sideflip)
    Optimal Distance RCV tests all the different types of RCVs, but only currently works in the Calculate mode.

Initial Movement Type: The specific type of the initial movement (ex. Triple Jump, GP Roll, or Upthrow RCV)

Duration Type (not available in Solve mode): Whether to specify the duration of the initial movement in terms of frames or vertical displacement

Frames: the number of frames the initial movement lasts
Vertical Displacement: how many units Mario falls during the initial movement (more negative is falling for longer)
    The Solve mode solves for this value, so you do not need to enter anything yourself in that mode

Vault Cap Return Frame (vaults only): the frame Cappy returns to Mario's head during a vault (only needs to be edited to be correct if the jump has a triple throw)

Frames of Holding A/B: For certain jump types, the duration of holding A/B affects how much height is gained. This parameter ranges from 1 to 10 frames.

Moonwalk Frames: How many frames of coyote time before jumping. This coyote time is spent in the idle animation so that no speed is lost.

Initial Horizontal Speed (configurable for some movement types): Mario's horizontal speed when the initial movement begins

Vector Direction: whether the initial movement is vectored left or right

Midairs: What type of midair movements (ex. cap throws, dives, cap bounces) the jump has. Setting this to Custom allows you to specify any combination of midairs, but the Solve mode does not currently support custom midairs.

Triple Throw: For Spinless and Simple Tech midairs, this preset determines whether the throw before the cap bounce is or is not a triple throw. Use Test Both if you want the Solver to test with and without triple throw to figure out which is best. For MCCT and CBV First midairs, this parameter specifies whether the homing throw is a MCCT or triple throw.

Maximum Upwarp: The biggest upwarp Vector Calculator will allow. Just like in the game, the speed on the frame of the upwarp is also taken into consideration, so Mario moving up 15 units on a frame where his vertical velocity is -16 is a 31 unit upwarp. The default value of 40 is generally good, but if you are finishing a height jump at an angle or trying to upwarp before the +2 vertical velocity frame of the dive, this value may need to be reduced.

Gravity: Whether the jump is in regular or moon gravity. Moon gravity is not yet supported for either of the solve modes.

0 Degree Axis: Whether the X or Z axis is considered 0 by the mod the user is using. This is X except for my practice mod fork.

Camera Angle:
    Which angle the camera is pointing toward during the jump, which shifts the joystick angles the program calculates.
    Initial Angle: the jump's initial angle
    Target Angle: the jump's target angle
    Absolute (Recommended): The joystick angles are fixed instead of being dependent on the camera angle. This is for use with tetraxile's absolute joystick mod. Using tetraxile's mod and this setting allows you to TAS jumps without a top-down camera angle.
    Custom: Allows the user to specify any angle

Midair Properties:

Duration Search Range: For the Solve mode, how many frames the program searches in each direction to find the best durations for each part of the movement once it has found a rough optimization of the jump. Set this to a lower value if the calculation is taking too long. For RCVs, the range is no more than 3 even if the user enters a higher range value.

Enable Turnarounds: Whether the program uses fast turnarounds to optimize cap throws.

Edge Cap Bounce Angle: How far to the side cappy is thrown relative to the dive angle. The solve modes solve for this value.
Edge Cap Bounce Angle Tolerance: How precise, in degrees, the cap bounce is allowed to be. If the cap bounce is failing in game, set this to a higher value.
First Dive Deceleration: How much deceleration the first dive has on the first frame to help make the cap bounce possible. The solve modes solve for this value.
Turn During First Dive: Whether to turn during the first dive, which usually, if not always, results in more distance. Use "Test Both" to test both options.
CB Cap Return Frame: The frame during the cap bounce that Cappy returns to Mario's head.

Homing Throw Type:
    Relax: Relax tech (neutral joystick during the homing to make cappy return faster)
    Relaxless: No relax tech
    Custom: Use for further customization, especially for unusual cases like wall HCTs
        Homing Throw Angle: how sharply cappy is thrown
        Neutral Joystick During Homing: whether the joystick is neutral on the frame Cappy is homed
        Homing Direction: the direction cappy is homed in
        Frames Before Home: The number of frames before cappy is homed
        HCT Cap Return Frame: The frame that Cappy returns to Mario's head

Ground/Liquid Under Midairs: Set to "Uniform" if the ground/liquid under the entire jump is the same height and type. Otherwise, if there is ground or liquid, set to "Varied"
Type: Whether there is ground, lava, or poison underneath the jump.
Height: The height of the ground, lava, or poison underneath the jump.

Menu Options:

Vector Calculator now has optinos for saving and loading project files, undoing and redoing steps, and more.

Save as User Defaults saves the current project's parameters as the user defaults, which can be restored by using Reset to User Defaults. New files are created with these defaults.

Reset to Program Defaults resets the current project's parameters to the program's default values.


Vector Display Window:

This is the window that provides frame-by-frame information once you click solve/calculate.

Initial Joystick Angle: The joystick angle that can be held in game so that Mario moves in the initial angle before the jump.
Horizontal Displacement:
Made Jump: Whether or not Mario made the jump based on the target coordinates. This is not reliable if the target coordinates have not been inputted exactly correctly.

The table below this gives frame-by-frame information about the movenet which can be used to debug issues in-game. The value column is how valuable the particular frame is to the overall jump. If no value is present, Mario is moving upward and this frame should not be removed in a trickjumping context. The lowest value frames are prime candidates for removal if using the Calculate (Solve Dives) or Calculate modes.