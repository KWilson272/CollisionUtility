# CollisionUtility
CollisionUtility is a small addon for the ProjectKorra plugin. This plugin allows you to configure 
the ability collisions using straightforward and intuitive syntax.

## Installation
To install this plugin, place the jar into your server's `plugins/` folder

## Requirements and Compatibility
This plugin was made for the following versions. It is not guaranteed to work on other versions, 
however it is quite likely that it will.
- Spigot `1.16.5+`
- ProjectKorra `1.12.0`

## Syntax
Lines that start with `#` are comment lines, and will not be read by the parser.

### Operators
Operators are symbols used to determine how collisions act. The operators currently recognized by
the plugin are: `>, <, =, x, ~`

#### `>` - Usage: `Operand1 > Operand2`
This operator should be used when the first operand should remove the second operand, but continue 
after the collision.

#### `<` - Usage: `Operand1 < Operand2`
This operator works just like `>` The first operand will be removed by the second operand, with the
second operand progressing after the collision.

#### `=` - Usage: `Operand1 = Operand2`
This operator creates a collision in which both abilities collide with each other and are removed.

#### `x` - Usage: `Operand1 x Operand2`
This operator is used to remove a collision. Regardless of any previous lines in the collisions 
file, the two operands will not collide.

#### `~` - Usage: `Operand1 ~ Operand2`
This operator is used when you would like a collision to take place, but neither ability should be
removed. This may be useful when you want an ability to just *interact* with another ability.

### Collision Declarations
Collisions are declared using the following syntax: `Operand1 Operator Operand2`. Operands can
either be `Groups` or `Ability Names`. To reference an ability, you simply write the name. To 
reference a group, you must use the prefix `$` before the name.

#### Group Declarations
Groups can be used when multiple abilities will have the same collision. For example, the four 
basic abilities AirSwipe, EarthBlast, FireBlast, and WaterManipulation all collide together. Rather
than having to write that out, you can declare them as a group as such:
`Group "BasicAbilities" {AirSwipe, EarthBlast, FireBlast, WaterManipulation}`

As mentioned above, referencing a group in a collision just requires that you prefix the group's 
name with the `$` character. Here's an example of how it would look in the collisions file:
`FireBlast > $BasicAbilities`

Groups can even contain other groups. Let's say you want AirShield and FireShield to block all
basic abilities, plus AirBlade and Fireball. To do this you can just create another group in the 
collisions file:
`Group "ShieldableAbilities" {$BasicAbilities, AirBlade, FireBall}`

#### Examples
- `FireBlast > WaterManipulation` - FireBlast collides with WaterManipulation, and WaterManipulation is removed. FireBlast continues.
- `AirSpout < EarthBlast` - AirSpout collides with EarthBlast, and is removed. EarthBlast continues.
- `EarthBlast = AirSwipe` - EarthBlast and AirSwipe collide. Both are removed.
- `Combustion x FireWheel` - Combustion and FireWheel will not collide. Neither will be removed.
- `Torrent ~ IceWall` - Torrent and IceWall will collide. Neither will be removed.
- `FireBlastCharged > $BasicAbilities` - Charged FireBlast will collide with every ability in the BasicAbilities group. Charged
FireBlast will not be removed, while the other ability will.
- `$BasicAbilities > $SpoutAbilities` - All abilities in the BasicAbilities group will collide with all abilities in the SpoutAbilities
group. The abilities in BasicAbilities will continue progressing, while the abilities in SpoutAbilities will be removed.

### Ability Aliases
Because of the way ProjectKorra ability naming works, some abilities can only be referenced 
for collisions using aliases. Use the following aliases to refer to certain abilities. If an ability
is not present in the table, use its in-game name.  

#### Fire
| Ability                   | Alias            |
|---------------------------|------------------|
| FireBlast (Charged Form)  | FireBlastCharged |
| FireBlast (Charged Form)  | ChargedFireBlast |
| FireBlast (Charged Form)  | CFB              |
| FireBlast (Standard Form) | FireBlast        |

#### Water
| Ability                 | Alias          |
|-------------------------|----------------|
| IceSpike (Blast Form)   | IceSpikeBlast  |
| IceSpike (Pillar Form)  | IceSpike       |
| IceSpike (Field Form)   | IceSpike       |
| WaterSpout (Spout Form) | WaterSpout     |
| WaterSpout (Wave Form)  | WaterSpoutWave |
| WaterSpout (Wave Form)  | WaterWave      |
| Torrent (Blast Form)    | Torrent        |
| Torrent (Wave Form)     | TorrentWave    |
| Torrent (Ring Form)     | TorrentRing    |
| Surge (Wave Form)       | Surge          |
| Surge (Wave Form)       | SurgeWave      |
| Surge (Wall Form)       | SurgeWall      |
| Surge (Wall Form)       | SurgeShield    |
| WaterArms (Grapple)     | WaterArmsWhip  |
| WaterArms (Grab)        | WaterArmsWhip  |
| WaterArms (Pull)        | WaterArmsWhip  |
| WaterArms (Punch)       | WaterArmsWhip  |

*Some abilities may share the same alias due to PK limitations*
