# Compare List/Array/Seq

## Array
+ For optimization
+ mutable
+ no proper `toString`
+ compare by reference. not by-value
+ O(1) for access with index

## List
+ linked list
+ immutable
+ goot at pattern matching
+ fast enough

## Reference
+ [Performance Characteristics](https://docs.scala-lang.org/overviews/collections-2.13/performance-characteristics.html)
+ [Performance test](https://tobyhobson.com/posts/scala/collections-performance/)
+ [Structure sharing](https://www.scala-lang.org/api/2.13.3/scala/collection/immutable/List.html)

    > **Space**: List implements **structural sharing** of the tail list. This means that many operations are either zero- or constant-memory cost.

    We will use `List` as default datatype. This gives simplicity of implementing like this.
    ```scala
    type Table = List[(KeyRange, IP, Port)]

    @tailrec
    def partitioning(table: Table, records: List[Record]): Unit = {
        table match {
            case [] => DoNothing
            case (h:t) => {
                val keyRange = h._1
                val ip = h._2
                val port = h._3
                val (sending, remaining) = records span keyRange.contains
                sendBlock(ip, port, sending)
                partitioning(t, remaining)
            }
        }
    }
    ```
    Reason why using `span`,
    > [Note: c span p is equivalent to (but possibly more efficient than) (c takeWhile p, c dropWhile p), provided the evaluation of the predicate p does not cause any side-effects.](https://www.scala-lang.org/api/2.13.3/scala/collection/immutable/List.html#span(p:A=%3EBoolean):(List[A],List[A]))