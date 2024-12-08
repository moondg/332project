# K-way Merge Sort

## Pseudocode

```C++
#define Partition = List[Record]

void copyDiskToRAM(Integer size, Partition storage)
void divideRAM(Integer number)

void K_Merge(k:Integer) {
  partition = dividerRAM(k)
  copyDiskToRAM(RAM_SIZE/(k+1), Disk)
  for i 1 to k:
    insertTournamentTree(pop(partition[i]))
  buffer << popTournamentTree
  buffer.empty
}

while !Disk.empty:
  K_Merge(k)
```

## Tournament Tree
```scala
sealed trait Tree {
  var value: Option[Record] // store cached value
  def pick: Option[Record]  // update a child node that has smaller value
  def initialize: Unit      // initialize value
}
case class Leaf(filePath) extends Tree
case class Node(left, right) extends Tree

Leaf(filePath) {
  val source: BufferedSource // Read file with buffer
  var value: Option[Record]  // pick and initialize will update value
  def pick: Option[Record]   // Some[Record] read buffer / None if empty
  def initialize: Unit       // propagate signal to every child
}

Node(left, right) {
  var value: Option[Record]  // pick and initialize will update value
  def pick: Option[Record]   // Some[Record] / None from left and right
  def initialize: Unit       // propagate signal to every child
}
def gardener(filePaths) = {
  filePaths match {
    case Nil => Leaf
    case head::Nil => Leaf
    case _ =>
      (left, right) = filePaths splitAt (filePaths.length / 2)
      Node(gardener(left), gradener(right))
  }
}
TournamentTree(inputFilePaths, outputFilePath) {
  val tree = gardener(inputFilePaths)
  def merge() = while(nonEmpty) write(tree.pick)
}
```