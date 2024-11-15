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