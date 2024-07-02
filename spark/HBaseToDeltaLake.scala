import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat

object HBaseToDeltaLake {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("HBase to Databricks")
      .config("spark.hadoop.fs.s3a.access.key", sys.env("AWS_ACCESS_KEY_ID"))
      .config("spark.hadoop.fs.s3a.secret.key", sys.env("AWS_SECRET_ACCESS_KEY"))
      .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
      .getOrCreate()

    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", "zookeeper1.example.com,zookeeper2.example.com,zookeeper3.example.com")
    hbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
    hbaseConf.set(TableInputFormat.INPUT_TABLE, "UEFA_CHAMPIONS_LEAGUE")

    val scan = new Scan()
    scan.addColumn(Bytes.toBytes("team"), Bytes.toBytes("name"))
    scan.addColumn(Bytes.toBytes("team"), Bytes.toBytes("country"))
    scan.addColumn(Bytes.toBytes("match"), Bytes.toBytes("date"))
    scan.addColumn(Bytes.toBytes("match"), Bytes.toBytes("opponent"))
    scan.addColumn(Bytes.toBytes("match"), Bytes.toBytes("score"))
    scan.setCaching(500)
    scan.setCacheBlocks(false)
    
    // Use your timestamp filter
    val timestamp = getLastRunTimestamp() // Implement this to get the last run timestamp
    scan.setTimeRange(timestamp, System.currentTimeMillis())

    hbaseConf.set(TableInputFormat.SCAN, TableMapReduceUtil.convertScanToString(scan))

    val hBaseRDD = spark.sparkContext.newAPIHadoopRDD(
      hbaseConf,
      classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[Result]
    )

    import spark.implicits._

    val hBaseDF = hBaseRDD.map {
      case (_, result) =>
        val rowKey = Bytes.toString(result.getRow)
        val teamName = Bytes.toString(result.getValue(Bytes.toBytes("team"), Bytes.toBytes("name")))
        val teamCountry = Bytes.toString(result.getValue(Bytes.toBytes("team"), Bytes.toBytes("country")))
        val matchDate = Bytes.toString(result.getValue(Bytes.toBytes("match"), Bytes.toBytes("date")))
        val matchOpponent = Bytes.toString(result.getValue(Bytes.toBytes("match"), Bytes.toBytes("opponent")))
        val matchScore = Bytes.toString(result.getValue(Bytes.toBytes("match"), Bytes.toBytes("score")))
        (rowKey, teamName, teamCountry, matchDate, matchOpponent, matchScore)
    }.toDF("rowKey", "teamName", "teamCountry", "matchDate", "matchOpponent", "matchScore")

    // Write DataFrame to Databricks Delta Lake
    hBaseDF.write
      .format("delta")
      .mode(SaveMode.Overwrite)
      .save("s3a://league_stand_entries/delta/")
  }

  def getLastRunTimestamp(): Long = {
    // Implement logic to retrieve the last run timestamp, e.g., from a file or a database
    0L
  }
}
