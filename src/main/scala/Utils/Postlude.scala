package Utils

import Core._
import Core.Block._
import Core.Key._

import scala.annotation.tailrec
import java.io.{File, FileOutputStream}
import message.common.DataChunk
import com.google.protobuf.ByteString
import Network.Network.Node

object Postlude {
  def kWayMerge(tempFiles: List[String], outputFilePath: String): Int = {
    lazy val blocks: Array[Block] = tempFiles.map(blockFromFile).toArray
    var counter: Int = 0
    var emptyBlock: Int = 0
    val tournamentTree: Array[(Record, Int)] =
      Array.fill(blocks.length)(new Record(Key.max, Array.empty), -1)

    val file = new File(outputFilePath)
    val fileWriter = new FileOutputStream(file, file.exists())

    @tailrec
    def treePushInit(cnt: Int): Unit = {
      if (cnt < blocks.length) {
        tournamentTree.update(
          cnt,
          (new Record(blocks(cnt).block.head.key, blocks(cnt).block.head.value), cnt))
        blocks.update(cnt, new Block(blocks(cnt).block.tail))
        treePushInit(cnt + 1)
      }
    }
    def findingMinValueIndex(tree: Array[(Record, Int)]): Int = {
      if (tree.length == 1) 0
      else {
        val divideLength = (tree.length + 1) / 2
        val dividedTree = tree.grouped(divideLength)
        val first = findingMinValueIndex(dividedTree.next())
        val second = findingMinValueIndex(dividedTree.next()) + divideLength
        if (tree(first)._1.key.compare(tree(second)._1.key) < 0) first
        else second
      }
    }
    @tailrec
    def merge(): Unit = {
      val treeIndex = findingMinValueIndex(tournamentTree)
      val (data, num) = tournamentTree(treeIndex)
      assert(num >= 0)
      if (blocks(num).block.nonEmpty) {
        tournamentTree.update(treeIndex, (blocks(num).block.head, num))
        blocks.update(num, new Block(blocks(num).block.tail))
      } else {
        tournamentTree.update(treeIndex, (new Record(Key.max, Array.empty[Byte]), -1))
        emptyBlock += 1
      }
      fileWriter.write(data.key.key)
      fileWriter.write(data.value)
      counter += 1
      if (emptyBlock < blocks.length) {
        merge()
      }
    }

    treePushInit(0)
    merge()

    fileWriter.close()

    counter
  }
  def emptyDataChunk(index: Int): DataChunk = {
    DataChunk(data = ByteString.EMPTY, chunkIndex = index, isEOF = true)
  }
}
