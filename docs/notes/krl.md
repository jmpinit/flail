# KUKA Robot Language

Ad-hoc notes on the structure of the language assembled in the course of trying
to write code to generate valid KRL programs.

## Beginning

```
&ACCESS RVP
&REL 1
```

`BAS(#INITMOV,0` <- what does this actually do? Ask on the forums.

## Special Variables

`CIRC_TYPE = #BASE` Space-related orientation control during circular motion.
`CIRC_TYPE = #PATH` Path-related orientation control during circular motion.

Warn if CIRC_TYPE is set when ORI_TYPE is #JOINT, because it's meaningless.

`ORI_TYPE = #CONSTANT`

Linear motions: The orientation remains constant during the path motion. The
programmed orientation is disregarded for the end point.
Circular motions: The orientation remains constant during the circular motion.
The programmed orientation is disregarded for the end point.

`ORI_TYPE = #VAR`

Linear motions: During the path motion, the orientation changes continuously
from the start point to the end point.
Circular motions: During the circular motion, the orientation changes
continuously from the start orientation to the orientation of the end point.

`ORI_TYPE = #JOINT`

Linear motions: During the path motion, the orientation of the tool changes continuously from the start position to the end position. The wrist axis singularity (axis 5) is avoided.
Circular motions: During the circular motion, the orientation of the tool changes continuously from the start position to the end position (axis 5).

## Comments

Start with `;`

## Datatypes

`INT foo`
`foo = {FRAME: X 0,Y 0,Z 0,A 0,B 0,C 0}`

## Variable Assignment

`FOO = 1`

## Built-in Functions

* `PTP e6axis`, `PTP e6axis C_PTP`, `PTP e6axis C_DIS`
* `LIN frame`, `LIN frame C_DIS`
* `WAIT SEC seconds`

## Flow Control

```
IF condition THEN
ENDIF
```

```
SWITCH variable
CASE value
CASE value
ENDSWITCH
```

## Looping

```
WHILE condition
ENDWHILE
```

```
FOR i=1 TO 10
ENDFOR
```

## Function Definitions

```
DEF Foo()
END
```

