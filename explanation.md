# Vector Calculator Optimization Explanation
The ``Solver`` is the part of the program that determines the optimal intitial movement duration, midair durations, cap throw type, edge cap bounce angle, and so on.

The ``Calculator`` is the part of the program the part that finds the best angles to hold during the jump. The ``Solver`` uses the ``Calculator`` repeatedly to find the best possible durations.

## Solver
### Part 1: Finding Ballpark Durations
1. The solver makes the durations of the initial movement and many midairs too long, so it can subtract frames later. For all other midairs, the solver uses preset values.
2. The solver reduces the duration of the initial movement until the first GP and cap bounce are above the ground/liquid (if applicable)
3. The solver calculates the efficiencies of each frame of the initial movement and midairs
4. The solver shortens the duration of the movement with the least efficient final frame (assuming that frame can be removed) until the second GP is above the ground/liquid (if applicable) and the end of the jump is above the target Y position, taking into account the maximum upwarp (note that the last dive does is not allowed to be shortened until the second GP is above the ground/liquid, to make sure it is long enough)

At this point, the durations of each piece of movement are more or less what they should be, but they will need to be tweaked to optimize the jump.

### Part 2: Cap Bounce Testing
The solver tests all permutations of cap throw and dive lengths that are within the ``Duration Search Range`` of the durations from Part 1. For each test, it iterates through the permissible throw types, testing a range of ``Edge Cap Bounce Angles``. If no throw type works, the solver makes the dive decelerate slightly and tries again. If it finds a throw type that works, records it the throw type and ``Edge Cap Bounce Angle`` and dive deceleration that made it possible.(In the future, the cap throw should be vectored suboptimally instead of holding back during the dive.)

### Part 3: Duration Testing
1. The solver loops through many different permutations of durations for the initial movement and midairs, testing how much distance each one yields using the ``Calculator``. This part is optimized for speed, so it doesn’t do as good of a job optimizing as the program will do later. It keeps track of all permutations that are within some range of the best one for more rigorous testing later. The durations tested are the within the ``Duration Search Range`` of the ballpark durations generated in Part 1. Many permuations don’t need to be tested because Mario would land on the ground / get burned from lava / the permutation is obviously ineffcient / Mario would be too high or too low at the end of the jump / etc.
2. The solver tests the best results from part 1 using the ``Calculator`` with a higher degree of optimization to find which actually yields the most distance.

## Calculator
The calculator optimizes the following parameters:
1. The angle at which the cap throw before the dive cap bounce is thrown
2. The angle at which the final cap throw is thrown (so that Mario ends up diving in the direction of the entire jump)
3. The joystick angle for the falling part of a homing cap throw (the part in which Mario's gravity returns to normal 24 frames after the cap throw begins)

Generally, the calculator vectors as strongly as possible (90 degrees from initial velocity angle), unless Mario can accelerate forward, in which case it holds in the direction of the initial velocity angle.

The exceptions for this are movement that needs a certain rotation at the end. For cap throws before dive cap bounces, the calculator starts off holding 90 degrees, but rotates in small increments opposite the direction of the movement to make Mario rotate less without losing significant distance. Then, it has Mario perform a fast turnaround at the end. Final cap throw vectors are vectored at 90 degrees until the fast turnaround. For jumps that don’t have a final cap throw, the rotation of the movement before the final dive (ex. cap bounce)’s rotation is controlled, but this is not done optimally yet.

Dives before cap bounces are optimized by turning them to the side (holding 90 degrees from the dive’s current rotation each frame). This is so that the dive can start moving in a more forward direction (thereby gaining more forward distance) while still turning to the side enough by the end to set up the cap bounce vector.

RCVs are more complex and have additional steps for their optimization, but they are optimized to spend many frames traveling forward.
