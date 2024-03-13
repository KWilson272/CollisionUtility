# CollisionUtility
CollisionUtility is a small addon for the ProjectKorra plugin. This plugin allows you to configure the ability collisions using straightforward and intuitive syntax.

## Installation
To install this plugin, place the jar into your server's `plugins/` folder

## Requirements and Compatibility
This plugin was made for the following versions. It is not guaranteed to work on other versions, however it is quite likely that it will.
- Spigot `1.20.1`
- ProjectKorra `1.11.2`

## Syntax
Lines that start with `#` are comment lines, and will not be read by the parser.

### Operators
Operators are symbols used to determine how collisions act. The operators currently recognized by the plugin are: `>, <, =, x`

#### `>` - Usage: `Operand1 > Operand2`
This operator should be used when the first operand should remove the second operand, but continue after the collision. 
For example, if you want Charged FireBlast to remove WaterManipulation, but continue after colliding, you would put a line 
in the collisions file with the syntax: `FireBlastCharged > WaterManipulation`

#### `<` - Usage: `Operand1 < Operand2`

This operator works similarly to `>`, just inverted. This operator should be used when you want the first operand to be 
removed by the second operand upon colliding, with the second operand progressing after the collision. If you want AirSwipe 
to be removed by EarthSmash, you would put the following line in the collisions file: `AirSwipe < EarthSmash`

#### `=` - Usage: `Operand1 = Operand2`

This operator creates a collision in which both abilities collide with each other and are removed. If you want EarthBlast to 
collide with WaterManipulation, and have both of those abilities be removed, you would put the following line in the collisions
file: `EarthBlast = WaterManipulation`

#### `x` - Usage: `Operand1 x Operand2`

This operator is used to remove a collision. Regardless of any previous lines in the collisions file, the two operands will not collide. 
This becomes useful when you would like to remove a collision created by the use of a group. Working off of our last example, let's say
you wanted to remove the `EarthBlast = WaterManipulation` collision. You could delete that line, or, you could put the following
line in the collisions file: `EarthBlast x WaterManipulation`

### Collision Declarations
Collisions are declared using the following syntax: `Operand1 Operator Operand2`. Operands can either be `Groups` or `Ability Names`. To reference an ability, 
you simply write the name. To reference a group, you must use the prefix `$` before the name.

- `FireBlast > WaterManipulation` - FireBlast collides with WaterManipulation, and WaterManipulation is removed. FireBlast continues.
- `AirSpout < EarthBlast` - AirSpout collides with EarthBlast, and is removed. EarthBlast continues.
- `EarthBlast = AirSwipe` - EarthBlast and AirSwipe collide. Both are removed.
- `Combustion x FireWheel` - Combustion and FireWheel will not collide. Neither will be removed.
- `FireBlastCharged > $BasicAbilities` - Charged FireBlast will collide with every ability in the BasicAbilities group. Charged
FireBlast will not be removed, while the other ability will.
- `$BasicAbilities > $SpoutAbilities` - All Abilities in the BasicAbilities group will collide with all abilities in the SpoutAbilities
group. The BasicAbilities will continue progressing, while the SpoutAbilities will be removed.
