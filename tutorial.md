# Vector Calculator Tutorial

This tutorial is for anyone who wants to learn how to TAS jumps using Vector Calculator. Previous TASing experience is not required, though following the tutorial will take longer if you do not have any.

## Part 1: Setting up TASing tools

Join the [Super Mario Odyssey TAS server](https://discord.gg/HdB2P586ch) and follow the instructions in `#resources` to set up either `smo-practice` or `smo-lunakit` to TAS with. If using `smo-practice`, make sure you install and test the `smo-practice-server` before continuing.

This tutorial assumes that you will be using [TSV-TAS-2](https://github.com/xiivler/TSV-TAS-2) as your script format because it works most smoothly with Vector Calculator. Make sure to familiarize yourself with the script format by reading its [documentation](https://docs.google.com/document/d/1vW-swF3k96YxaIJqXbtRXbQ54mKKgeWfPFlW2hYBa_Q/edit?usp=sharing). This script format is compatible with both `smo-practice` and `smo-lunakit`.

Additionally, copy the folder `absolute-joystick` into the folder `sd:/atmosphere/exefs_patches` on your Switch's SD card. (There should now be a file `sd:/atmosphere/exefs_patches/absolute_stick_angles/3CA12DFAAF9C82DA064D1698DF79CDA1.ips`.) This mod, created by tetraxile, causes the game to ignore the camera angle when translating joystick angles to Mario's movement, so you do not have to point the camera downward to prevent it from turning left and right. This step is not required to use Vector Calculator, but the rest of the tutorial assumes you have completed it.

Note: To uninstall this mod at any time so that you can play the game normally, simply delete the `absolute-joystick` folder from the SD card.

Finally, if you haven't already, download the latest version of Vector Calculator from GitHub (under Releases).

## Part 2: TASing a Vault

If double-clicking Vector Calculator does not open it, navigate to its directory in a terminal window and run the command `java -jar vector-calculator.jar`. If this does not work, make sure you have Java 25 installed.

Click the menu item `Reset to Program Defaults` in the `File` menu.

In Super Mario Odyssey, stand at the approximate point at which the vault should begin.

In Vector Calculator, click the `value` cell of the `Initial Coordinates` row (it says `(0, 0, 0)`) and then enter Mario's X, Y, and Z coordinates from the game.

Now, stand at the point where you believe the vault will land, and enter Mario's coordinates in the `Target Coordinates` row.

Select `Vault` as the `Initial Movement Type`, and choose whether you want a left or right vector for the vault in the `Vector Direction` row.

Select the type of midairs you want from the `Midairs` row. If you want to have a triple throw, set the value of the `Triple Throw` row to `Yes`.

Now, press the `Solve` button. After some time, a window will open with frame-by-frame information about the jump. This is a good time to save your Vector Calculator project, which you can do by pressing Control + S while the main window is selected (Command + S for Mac users).

Make a copy of `ud-vault.tsv` from the `Templates` folder and open this file in a spreadsheet editor or even a text editor. Replace the current value for `$initial_joystick_angle` with the number Vector Calculator displays in the window that opened after you clicked `Solve`. The camera angle can be set to whatever you want; the Target Angle from Vector Calculator usually looks good. Use `tsv-tas.py` to generate the script file and send it to your Switch.

#### (SMO Practice Only)

In the SMO practice server, use the `tp` command to teleport to where you think Mario should ground pound to begin the up-down vault.

#### (LunaKit Only)

Insert a line at the top of your copy of `ud-vault.tsv` that reads `$position = (x; y; z)`, replacing `x`, `y`, and `z` with the coordinates you think Mario should ground pound to begin the up-down vault. Configure all the other LunaKit variables as necessary using this [documentation](https://docs.google.com/document/d/1vW-swF3k96YxaIJqXbtRXbQ54mKKgeWfPFlW2hYBa_Q/edit?tab=t.8gdkfr3frur8#heading=h.gu4mp3nt9u0p).

#### (Both Mods)

Now, run the TSV-TAS script and see if you get a vault. Keep tweaking the teleport coordinates until you get a vault as close to the edge as possible.

Once you have done this, click the `Copy to Clipboard` button in Vector Calculator and paste the result to overwrite the commented row at the bottom of the template. Run the script again and the inputs that Vector Calculator solved will be performed in-game.

## Part 3: TASing other jumps

You should now be able to TAS other types of jumps as well. The moonwalk triple and RCV templates are the next easiest to use. If you are familiar with TASing Super Mario Odyssey, you can also write your own inputs leading up to the jump or modify the templates further to suit your needs.