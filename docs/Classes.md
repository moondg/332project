# Class Design

## Class: Key extend Ordered[Key]
### member
+ key: String
### method
+ override compare(a: Key, b: Key): Int

## Class: Record extend Ordered[Record]
### member
+ key: Key
+ value: String
### method
+ override compare(a: Record, b: Record): Int

## Class: KeyRange
### member
+ start: Key
+ end: Key
### method
+ contains(record: Record): Boolean
+ contains(key: Key): Boolean

## Class: Table
### member
+ address: List[(IP, Port)]
+ keyRanges: List[KeyRange]
### method
+ find(record: Record): (IP, Port)
+ find(key: Key): (IP, Port)

## Class: TournamentTree
### method
+ insert(record: Record): Unit
+ pop(): Record