# vector-calculator
Vector Calculator is a program for optimizing jumps in Super Mario Odyssey, primarily for use in TASing. Special thanks to MonsterDruide1 for some menu code and to Tetraxile, whose Absolute Joystick mod is bundled in this program's release.

To open the program, double click `vector-calculator.jar` or run the command `java -jar vector-calculator.jar`. You need to have Java 25 installed.

Check out the [tutorial](tutorial.md) for step-by-step instructions on TASing a trickjump with Vector Calculator. Read below for a detailed description of the various modes and settings the program has to offer.

## Optimization Modes
Vector Calculator has 3 modes: `Solve`, `Calculate (Solve Dives)`, and `Calculate`, of which `Solve` is the most automated and `Calculate` is the least automated.

* `Solve`: Determines optimal durations and inputs for each piece of movement

* `Calculate (Solve Dives)`: Determines optimal inputs for each piece of movement, ensures that the dive before the cap bounce is a possible duration, and lengthens or shortens the last dive until it lands at the target Y position

* `Calculate`: Determines optimal inputs for each piece of movement given the durations the user inputted

## Properties
Vector Calculator has several properties that can be configured by the user. Some of these are only visible depending on the settings of other properties. There are two sets of properties: `General Properties` and `Midair Properties`.

### General Properties

* **Calculator Mode**: The mode the calculator is running in (`Solve`, `Calculate (Solve Dives)`, or `Calculate`)

* **Initial Coordinates**: The coordinates of Mario one frame before he jumps

* **Calculate Using**: Whether the jump is calculated based on its `Initial Angle`, `Target Angle`, or `Target Coordinates`
    * `Initial Angle`: The direction Mario is moving in one frame before the jump
    * `Target Angle`: The overall direction Mario is moving in during the jump
    * `Target Coordinates`: The position where Mario lands at the end of the jump

* **Target Y Position** *(appears if the user is calculating using an initial or target angle)*: The Y position where Mario lands at the end of the jump

* **Solve for Initial Angle** *(only for RCVs in `Solve` mode)*: Whether Vector Calculator should solve for the optimal initial angle

* **Initial Movement Category**: The category of the initial movement (ex. `Jump`, `Roll`, or `RCV`)
    * `Optimal Distance Motion` finds the optimal initial motion type for the jump (either a triple jump, MCCT RCV, or sideflip)
    * `None` means that no initial movement is calculated, and the first midair is treated as the beginning of the jump (ex. this could be used for a jump that starts with a dive)

* **Initial Movement Type**: The specific type of the initial movement (ex. `Triple Jump`, `GP Roll`, or `Upthrow RCV`)
    * `Optimal Distance RCV` tests all the different types of RCVs, but only currently works in the `Calculate` mode.

* **Duration Type** *(not available in `Solve` mode)*: Whether to specify the duration of the initial movement in terms of frames or vertical displacement
    * **Frames**: The number of frames the initial movement lasts. (The `Solve` mode solves for this value, so you do not need to enter anything yourself in that mode.)
    * **Vertical Displacement**: How many units Mario falls during the initial movement (a larger negative number means Mario falls for longer)

* **Vault Cap Return Frame** *(only for vaults)*: The frame Cappy returns to Mario's head during a vault (only needs to be edited to be correct if the jump has a triple throw)

* **Frames of Holding A/B** *(for some movement types)*: For certain jump types, the duration of holding A/B affects how much height is gained. This parameter ranges from 1 to 10 frames.

* **Moonwalk Frames** *(for some movement types)*: How many frames of coyote time before jumping. The coyote time is spent in the idle animation so that no speed is lost.

* **Initial Horizontal Speed** *(configurable for some movement types)*: Mario's horizontal speed when the initial movement begins

* **Vector Direction**: Whether the initial movement is vectored left or right

* **Midairs**: What type of midair movements (ex. cap throws, dives, cap bounces) the jump has. Setting this to `Custom` allows you to specify any combination of midairs, but the `Solve` mode does not currently support custom midairs.

* **Triple Throw**: For Spinless and Simple Tech midairs, this preset determines whether the throw before the cap bounce is or is not a triple throw. Use `Test Both` if you want the Solver to test with and without triple throw to figure out which is best. For `MCCT First` and `CB First` midairs, this parameter specifies whether the homing throw is a MCCT or triple throw.

* **Maximum Upwarp**: The biggest upwarp Vector Calculator will allow. Just like in the game, the speed on the frame of the upwarp is also taken into consideration, so Mario moving up 15 units on a frame where his vertical velocity is -16 is a 31 unit upwarp. The default value of 40 is generally good, but if you are finishing a height jump at an angle or trying to upwarp before the +2 vertical velocity frame of the dive, this value may need to be reduced.

* **Gravity**: Whether the jump is in regular or moon gravity. Moon gravity is not yet supported for either of the solve modes.

* **0 Degree Axis**: Whether the X or Z axis is considered 0 by the mod the user is using. This is X except for my practice mod fork.

* **Camera Angle**: Which angle the camera is pointing toward during the jump, which shifts the joystick angles the program calculates.
    * `Initial Angle`: the jump's initial angle
    * `Target Angle`: the jump's target angle
    * `Absolute` *(Recommended)*: The joystick angles are fixed instead of being dependent on the camera angle. This is for use with tetraxile's Absolute Joystick mod (bundled with the current release of Vector Calculator). Using tetraxile's mod and this setting allows you to TAS jumps without a top-down camera angle.
    * `Custom`: Allows the user to specify any camera angle

### Midair Properties

* **Mode**, **Midairs**, **Triple Throw**, and **Maximum Upwarp** are also present in this properties tab for convenience.

* **Duration Search Range**: For the Solve mode, this parameter represents how many frames the program searches in each direction to find the best durations for each part of the movement once it has found a rough optimization of the jump. Set this to a lower value if the calculation is taking too long. (For RCVs, the program limits the range to 3 even if the user enters a higher range value.)

* **Enable Turnarounds**: Whether the program uses fast turnarounds to optimize cap throws.

* **Edge Cap Bounce Angle**: How far to the side Cappy is thrown relative to the dive angle. The solve modes solve for this value.

* **Edge Cap Bounce Angle Tolerance**: How precise, in degrees, the cap bounce is allowed to be. If the cap bounce is failing in game, set this to a higher value, but this may reduce the distance of the jump.

* **First Dive Deceleration**: How much deceleration the first dive has on the first frame to help make the cap bounce possible. The solve modes solve for this value.

* **Turn During First Dive**: Whether to turn during the first dive, which usually, if not always, results in more distance. Use `Test Both` to test both options.

* **CB Cap Return Frame**: The frame during the cap bounce that Cappy returns to Mario's head.

* **Homing Throw Type**:
    * `Relax`: Relax tech is used (neutral joystick when Cappy is homed to make him return faster)
    * `Relaxless`: No relax tech is used
    * `Custom`: Allows for further customization (especially useful for unusual cases like wall HCTs)
        * **Homing Throw Angle**: How sharply Cappy is thrown
        * **Neutral Joystick During Homing**: Whether the joystick is neutral on the frame Cappy is homed
        * **Homing Direction**: The direction Cappy is homed in
        * **Frames Before Home**: The number of frames before Cappy is homed
        * **HCT Cap Return Frame**: The frame that Cappy returns to Mario's head

* **Ground/Liquid Under Midairs**: Set to `Uniform` if the ground/liquid under the entire jump is the same height and type. Otherwise, if there is ground or liquid, set to `Varied`.
    * **Type**: Whether there is ground, lava, or poison underneath the jump.
    * **Height**: The height of the ground, lava, or poison underneath the jump.

## Midair Movement Table

This table appears below the list of general/midair properties and displays the sequence of actions Mario takes during the jump, as well as their durations. Except in the `Solve` mode, each action's duration can be edited by the user. If the **Midairs** property is set to `Custom`, the entire sequence of actions can be edited. Use the plus and minus buttons to add and remove rows, and click on a movement's name to display a list of possible options.

## Menu Options

Vector Calculator now has options for saving and loading project files, undoing and redoing steps, and more.

`Save as User Defaults` saves the current project's parameters as the user defaults, which can be restored by clicking `Reset to User Defaults`. Every new project file is created with these defaults. User defaults are saved to the file `user-defaults.xml`, which is located in the same folder as `vector-calculator.jar`.

`Reset to Program Defaults` resets the current project's parameters to the program's default values. (This option does not change the user defaults.)

## Vector Display Window

After you click `Solve`/`Calculate`, the program will optimize the jump, then a window will open with two tables displaying information about the optimized jump.

The upper table displays the following attributes of the jump:

* **Initial Angle**: The jump's initial angle (if the user specified it), or what it should be to achieve the optimal distance with the jump

* **Target Angle**: The jump's target angle

* **Initial Joystick Angle**: The joystick angle that can be held in game so that Mario moves in the initial angle before the jump

* **Final Position**: Mario's coordinates at the end of the jump

* **Horizontal Displacement**: The total horizontal distance the jump travels

* **Vertical Displacement**: How much higher or lower Mario is at the end of the jump than at the start

* **Total Frames**: The number of frames for which the jump lasts

* **Made Jump**: Whether or not Mario made the jump based on the initial and target coordinates the user entered. (This is not reliable if the coordinates were not inputted precisely.)

The lower table provides frame-by-frame information about the movement, which can be used to diagnose issues if Mario behaves differently in-game than expected. The **Value** column displays how valuable each frame is toward maximizing the jump's distance. If a frame has a blank value, Mario is moving upward and this frame should not be removed in a trickjumping context. Frames with low values are the best to remove when optimizing a jump using the Calculate (Solve Dives) or Calculate modes.

## Current Limitations
* The `Solve` mode does not yet support custom midairs, 2P jumps, or moon gravity.
* Skew is not accounted for, affecting jumps such as triple jump semi-verticals if the triple jump is short enough
* `Solve` only optimizes for distance, not for speed, so it is not useful in all speedrunning contexts.
* Solving for RCVs is time-intensive.
* Optimization for final cap throw-less jumps can be improved
* Vector Calculator does not yet fully optimize for non-moonwalk edge triples, as it is most optimal to start off the edge triple running more straight and then turning to the side.