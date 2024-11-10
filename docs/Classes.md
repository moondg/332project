# Class Designs

## Class: Record extend Ordering[String]
### member
+ key: String
+ value: String
### method
+ override compare(a: Record, b: Record): Int

## Class: TournamentTree

### method
+ insert(record: Record): Unit
+ pop(): Record