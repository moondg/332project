# Class Design
## Class vs Object
Class: Contains data and necessary function with key features

Object: Useful functions with certain Class

Functions in Object can be injected into Class. All up to you
## Class: Key extends Ordered[Key]
### member
+ key: Array[Byte]
### method
+ compare
## Class: Record extend Ordered[Record]
### member
+ key: Key
+ value: Array[Byte]
### method
+ override compare(a: Record, b: Record): Int
+ raw: Array[Byte]

## Class: KeyRange
### member
+ start: Key
+ end: Key
### method
+ contains(record: Record): Boolean
+ contains(key: Key): Boolean
## Type: Node = (IPAddr, Port)
## Type: Table = List[(KeyRange, Node)]
## Class: TournamentTree
### member
+ table: Tree
### method
+ merge: Unit