* Added two new optimization modes
    * `Solve` finds not only optimal inputs but also optimal durations for each part of the jump, almost entirely automating TASing jumps. It tests various types of cap throws, including various MCCT directions, to find the optimal one for the given jump.
    * `Calculate (Solve Dives)` is a less automated mode that functions like previous versions of Vector Calculator, but ensures that the dive before the cap bounce is a possible duration, and lengthens or shortens the last dive until it lands at the target Y position
* Improved optimization of jumps
    * Added edge cap bounces
    * Improved joystick inputs during homing MCCTs to better set up rainbow spin vectors
    * Improved final cap throw vectors to throw to the side
    * Implemented turning during dives for greater distance
    * Bug fixes for certain optimization cases (including spinless jumps)
    * Added option to optimize triples with moonwalk
    * Made homing MCCTs customizable, allowing for relaxless and wall MCCTs
    * Allowed rolls to be held, which is optimal if they are at high speed
    * Implemented speedflips
    * Changed how rotation is handled during cap throws before cap bounces so that they are more accurate compared to the game
* Quality of life improvements
    * Target coordinates can now be specified instead of target angle
    * Presets for common midairs (ex. MCCT first)
    * Saving projects to XML files
    * User default settings for opening a new project
    * Undo and redo buttons
    * Initial joystick angle is specified to make setting up jumps easier
    * Created TSV-TAS templates to make setting up jumps easier
* Added support for upwarps
* Updated `Absolute` setting for camera angle to correspond with tetraxile's Absolute Joystick mod
* Added ability to specify ground/lava/poison underneath midairs, which is taken into account when using the `Solve` mode
* Added column in display window to show how valuable each frame is toward maximizing the jump's distance
* Added specification of whether the player made the jump or not
* Added rocket flower jumps
* Added option for no initial movement