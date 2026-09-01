# vector-calculator
Vector Calculator is a program for optimizing jumps in Super Mario Odyssey, primarily for use in TASing. Special thanks to MonsterDruide1 for some menu code and to Tetraxile, whose Absolute Joystick mod is bundled in this program's release.

To open the program, double-click `vector-calculator.jar` or run the command `java -jar vector-calculator.jar`. You need to have Java 25 or later installed.

Check out the [tutorial](tutorial.md) for step-by-step instructions on TASing a trickjump with Vector Calculator. Read below for a detailed description of the various modes and settings the program has to offer.

If you are curious about how the porgram optimizes jumps, read the [explanation](explanation.md).

## Optimization Modes
Vector Calculator has three modes: `Solve`, `Calculate (Solve Dives)`, and `Calculate`, of which `Solve` is the most automated and `Calculate` is the least automated.

* `Solve`: Determines optimal durations and inputs for each piece of movement

* `Calculate (Solve Dives)`: Determines optimal inputs for each piece of movement, ensures that the dive before the cap bounce is a possible duration, and lengthens or shortens the last dive until it lands at the target Y position

* `Calculate`: Determines optimal inputs for each piece of movement given the durations the user entered

Running `Solve` usually takes the longest. Once you have solved for the optimal durations using `Solve` mode, if you want to make changes that would not affect the durations (ex. adjust the target angle or the initial coordinates), you should switch to `Calculate (Solve Dives)` mode for faster calculation.

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

* **Two Player Mode**: Whether the jump is in two player mode

* **Rocket Flower**: Whether the jump is performed with a rocket flower

* **Gravity**: Whether the jump is in regular or moon gravity.

* **Initial Movement**: The category of the initial movement (ex. `Jump`, `Roll`, or `RCV`)
    * `Optimal Distance Motion` finds the optimal initial motion type for the jump (either a triple jump, MCCT RCV, or sideflip)
    * `None` means that no initial movement is calculated, and the first midair is treated as the beginning of the jump (ex. this could be used for a jump that starts with a dive)

* **Initial Movement Type**: The specific type of the initial movement (ex. `Triple Jump`, `GP Roll`, or `Upthrow RCV`)
    * `Optimal Distance RCV` tests all the different types of RCVs, but currently only works in the `Calculate` mode.

* **Duration Type** *(not available in `Solve` mode)*: Whether to specify the duration of the initial movement in terms of frames or vertical displacement
    * **Frames**: The number of frames the initial movement lasts. (The `Solve` mode solves for this value, so you do not need to enter anything yourself in that mode.)
    * **Vertical Displacement**: How many units Mario falls during the initial movement (a larger negative number means Mario falls for longer)

* **Vault Cap Return Frame** *(only for vaults)*: The frame Cappy returns to Mario's head during a vault (only needs to be edited to be correct if the jump has a triple throw)

* **Frames of Holding A/B** *(for some movement types)*: For certain jump types, the duration of holding A/B affects how much height is gained. This parameter ranges from 1 to 10 frames.

* **Initial Horizontal Speed** *(for some movement types)*: Mario's horizontal speed when the initial movement begins

* **Coyote Time** *(for some movement types)*: What type of coyote time to use for the jump. The `Solve` mode does not currently solve for the type or duration of coyote time.
    * `Moonwalk`: Mario's coyote time is in the idle animation so that no speed is lost.
    * `Running`: Mario runs during the coyote time (i.e. edge triple for triple jumps)
    * `None`: Mario jumps immediately

* **Moonwalk Frames** *(for some movement types)*: How many frames of moonwalk coyote time before jumping (maximum 5 frames).

* **Running Frames** *(for some movement types)*: How many frames of running coyote before jumping (maximum 6 frames). Speedloss during these frames is calculated.

* **Crouching Frames** *(for long jumps only)*: How many frames of crouching coyote time are experienced after any moonwalk/running frames (maximum 4 frames).

* **Vector Direction**: Whether the initial movement is vectored left or right

* **Custom Initial Facing Angle** *(for some movement types)*: Whether the user gets to specify an initial facing angle for the jump.

* **Initial Facing Angle** *(only if custom initial facing angle)* The angle Mario is facing when the jump begins, which determines Mario's initial vertical velocity for some jump types.

* **Midairs**: What type of midair movements (ex. cap throws, dives, cap bounces) the jump has. Setting this to `Custom` allows you to specify any combination of midairs, but the `Solve` mode does not currently support custom midairs.

* **Homing Triple Throw** *(some midair presets)* Whether the homing throw is a triple throw.

* **Triple Throw Before Dive CB** *(some midair presets)*: Whether the cap throw before the cap bounce is a triple throw. Use `Test Both` if you want the Solver to test with and without triple throw to figure out which is best.

* **CB Type** *(2P mode only)* Whether the cap bounce is a dive cap bounce or a 2P midair vault.

* **Reverse Bonk** *(2P mode only)* Whether the jump ends in a reverse bonk.

* **Solve for Maximum Upwarp** *(With reverse bonk only)* Whether to calculate the biggest possible upwarp based on the reverse bonk angle, or to let the user specify the maximum upwarp.

* **Maximum Upwarp**: The biggest upwarp Vector Calculator will allow. Just like in the game, the speed on the frame of the upwarp is also taken into consideration, so Mario moving up 15 units on a frame where his vertical velocity is -16 is a 31 unit upwarp. The default value of 40 is generally good, but if you are finishing a height jump at an angle or trying to upwarp before the +2 vertical velocity frame of the dive, this value may need to be reduced. **Set this parameter to 0 for bonk or overshoot jumps.**

* **Test Yanks**: Whether to test holding back at the end of certain vectorable movement or sideways at the end of certain non-vectorable movement, which in some cases results in extra distance because it results in a better initial velocity angle for the next midair.

* **0 Degree Axis**: Whether the X or Z axis is considered 0 by the mod the user is using. Select `Z` for MrKatzenGaming's fork of LunaKit or my smo-practice fork; otherwise, select `X`.

* **Camera Angle**: Which angle the camera is pointing toward during the jump, which shifts the joystick angles the program calculates.
    * `Initial Angle`: the jump's initial angle
    * `Target Angle`: the jump's target angle
    * `Absolute` *(Recommended)*: The joystick angles are fixed instead of being dependent on the camera angle. This is for use with tetraxile's Absolute Joystick mod (bundled with the current release of Vector Calculator). Using tetraxile's mod and this setting allows you to TAS jumps without a top-down camera angle. In the mod, press down the left stick to toggle between regular and absolute joystick angles.
    * `Custom`: Allows the user to specify any camera angle

### Midair Properties

* **Mode**, **Midairs**, **Homing Triple Throw**, **Triple Throw Before Dive CB**, **Reverse Bonk**, **Solve for Maximum Upwarp**, and **Maximum Upwarp** are also present in this properties tab for convenience.

* **Duration Search Range**: For the Solve mode, this parameter represents how many frames the program searches in each direction to find the best durations for each part of the movement once it has found a rough optimization of the jump. Set this to a lower value if the calculation is taking too long.

* **Enable Turnarounds**: Whether the program uses fast turnarounds to optimize cap throws.

* **First Cap Throw Vector Angle**: How strongly of an angle to throw Cappy at for the cap throw before the cap bounce. The solve modes solve for this value.

* **Edge Cap Bounce Angle**: How far to the side Cappy is thrown relative to the dive angle. The solve modes solve for this value.

* **Edge Cap Bounce Angle Tolerance**: How precise, in degrees, the cap bounce is allowed to be. If the cap bounce is failing in game, set this to a higher value, but this may reduce the distance of the jump.

* **First Dive Deceleration**: How much deceleration the first dive has on the first frame. This parameter should not be set to anything but 0, and it is only still part of the program because jumps solved using previous versions may include a dive deceleration to make the cap bounce possible.

* **Turn During First Dive**: Whether to turn during the first dive, which almost always results in more distance. Use `Test Both` to test both options.

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

* ** Optimize First Cap Throw Falling** *(moon gravity only)* ** Whether or not to rotate Mario to the first dive angle at the end of the preceding cap throw's falling component instead of during the cap throw's reduced gravity component, allowing for more vectoring.

* **Optimize Final Cap Throw Falling** Whether or not to rotate Mario to the final dive angle during at the end of the final cap throw's falling component instead of during the cap throw's reduced gravity component, allowing for more vectoring.

* **Custom Final Cap Throw Angle**: Set to `Yes` to have Mario throw Cappy at the optimal angle for the final cap throw. If this angle does not work because Cappy homes into an object or Cappy is too far away to be able to reverse bonk, set to `No`.

* **Final Cap Throw Angle** *(only if custom final cap throw angle)*: How far Cappy is thrown to the side relative to the final dive angle.

* **Reverse Bonk Angle** *(only with reverse bonk)*: How many degrees Mario reverse bonks at relative to the final dive angle. A positive reverse bonk angle is to the left if the `Vector Direction` is `Left`, and to the right if the `Vector Direction` is `Right`. A negative reverse bonk angle is in the opposite direction.

* **Final GP Frames** *(only with reverse bonk)*: How many frames the final ground pound lasts before the final dive.

* **Final Dive Angle**: How many degrees to the side Mario dives at relative to the direction of the rest of the jump. A positive final dive angle is to the left if the `Vector Direction` is `Left`, and to the right if the `Vector Direction` is `Right`. A negative final dive angle is in the opposite direction.

* **Ground/Liquid Under Midairs**: Set to `Uniform` if the ground/liquid under the entire jump is the same height and type. Otherwise, if there is ground or liquid, set to `Varied`. A higher `Duration Search Range` is recommended for jumps over ground or liquid.
    * **Type**: Whether there is ground, lava, or poison underneath the jump.
    * **Height**: The height of the ground, lava, or poison underneath the jump. You can obtain this value by standing on the ground or falling into the lava/poison, and recording Mario's Y coordinate. If the jump fails from landing on the ground, first make sure your initial Y coordinate is right. If the issue is still not resolved, try increasing the ground height a tiny bit.

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

* **Target Angle**: The jump's target angle (the overall direction Mario moves in over the course of the entire jump)

* **Initial Joystick Angle**: The joystick angle that can be held in game so that Mario moves in the initial angle before the jump

* **Initial Facing Angle** The direction Mario is facing at the beginning of the jump. By default, a reasonable angle for forward moonwalk or spinvault is specified. Do not worry about precisely matching this angle unless the jump is not behaving as expected.

* **Final Position**: Mario's coordinates at the end of the jump

* **Horizontal Displacement**: The total horizontal distance the jump travels

* **Ledge Horizontal Displacement**: The total horizontal distance the jump travels, taking into account how far off the edge of the starting and ending ledges Mario can stand. Not relevant if Mario is not jumping off the edge of a ledge.

* **Vertical Displacement**: How much higher or lower Mario is at the end of the jump than at the start

* **Total Frames**: The number of frames for which the jump lasts

* **Made Jump**: Whether or not Mario made the jump based on the initial and target coordinates the user entered. (This is not reliable if the coordinates were not inputted precisely.)

The lower table provides frame-by-frame information about the movement, which can be used to diagnose issues if Mario behaves differently in-game than expected. The **Value** column displays how valuable each frame is toward maximizing the jump's distance. If a frame has a blank value, Mario is moving upward and this frame should not be removed in a trickjumping context. Frames with low values are the best to remove when optimizing a jump using the Calculate (Solve Dives) or Calculate modes.

## Current Limitations
* The `Solve` mode does not yet support custom midairs
* Skew is not accounted for, affecting jumps such as triple jump semi-verticals if the triple jump is short enough
* Jumps over ground are not properly optimized in moon gravity
* Minor optimizations can still be made to allow for slightly more vectoring during the cap throw preceding the cap bounce
* `Solve` only optimizes for distance, not for speed, so it is not useful in all speedrunning contexts
* Some calculations are still very time-intensive
