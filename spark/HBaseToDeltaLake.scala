/*
SparkSession Initialization:

SparkSession.builder()...getOrCreate(): Initializes SparkSession with necessary configurations for the application name and AWS S3 credentials.
HBase Configuration:

HBaseConfiguration.create(): Creates a configuration object for HBase.
Setting hbase.zookeeper.quorum and hbase.zookeeper.property.clientPort to connect to HBase Zookeeper.
Scan Configuration:

Scan object defines which columns to fetch (team:name, team:country, match:date, match:opponent, match:score) and applies a timestamp filter.
HBase RDD Creation:

newAPIHadoopRDD: Creates an RDD from HBase using TableInputFormat with specified configuration (hbaseConf).
DataFrame Creation:

map: Maps each record in HBase RDD to a tuple of values (rowKey, teamName, teamCountry, matchDate, matchOpponent, matchScore).
toDF("rowKey", ...): Converts RDD to DataFrame with specified column names.
Delta Lake Write:

hBaseDF.write.format("delta")...save("s3a://league_stand_entries/delta/"): Writes DataFrame to Databricks Delta Lake in the specified S3 path, overwriting existing data (SaveMode.Overwrite).
SparkSession Stop:

spark.stop(): Stops the SparkSession after job completion.
getLastRunTimestamp() Function:

Placeholder function to retrieve the last run timestamp, which can be implemented based on specific logic (e.g., from a file or database).
*/
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat

object HBaseToDeltaLake {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val spark = SparkSession.builder()
      .appName("HBase to Databricks")
      .config("spark.hadoop.fs.s3a.access.key", sys.env("AWS_ACCESS_KEY_ID"))  // AWS credentials
      .config("spark.hadoop.fs.s3a.secret.key", sys.env("AWS_SECRET_ACCESS_KEY"))
      .config("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")  // Use S3AFileSystem
      .getOrCreate()

    // HBase Configuration
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.set("hbase.zookeeper.quorum", "zookeeper1.example.com,zookeeper2.example.com,zookeeper3.example.com")  // Zookeeper quorum
    hbaseConf.set("hbase.zookeeper.property.clientPort", "2181")  // Zookeeper client port
    hbaseConf.set(TableInputFormat.INPUT_TABLE, "UEFA_CHAMPIONS_LEAGUE")  // HBase table name

    // Define the scan object to fetch specific columns and apply timestamp filter
    val scan = new Scan()
    scan.addColumn(Bytes.toBytes("team"), Bytes.toBytes("name"))  // Fetch team name
    scan.addColumn(Bytes.toBytes("team"), Bytes.toBytes("country"))  // Fetch team country
    scan.addColumn(Bytes.toBytes("match"), Bytes.toBytes("date"))  // Fetch match date
    scan.addColumn(Bytes.toBytes("match"), Bytes.toBytes("opponent"))  // Fetch match opponent
    scan.addColumn(Bytes.toBytes("match"), Bytes.toBytes("score"))  // Fetch match score
    scan.setCaching(500)  // Set caching for scan
    scan.setCacheBlocks(false)  // Disable cache blocks
    
    // Use your timestamp filter here (example getLastRunTimestamp() function)
    val timestamp = getLastRunTimestamp()  // Get last run timestamp
    scan.setTimeRange(timestamp, System.currentTimeMillis())  // Apply time range filter

    // Set scan configuration to HBase configuration
    hbaseConf.set(TableInputFormat.SCAN, TableMapReduceUtil.convertScanToString(scan))

    // Create HBase RDD using newAPIHadoopRDD
    val hBaseRDD = spark.sparkContext.newAPIHadoopRDD(
      hbaseConf,
      classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[Result]
    )

    import spark.implicits._

    // Map HBase RDD to DataFrame
    val hBaseDF = hBaseRDD.map {
      case (_, result) =>
        // Extract values from HBase Result object
        val rowKey = Bytes.toString(result.getRow)  // Get row key
        val teamName = Bytes.toString(result.getValue(Bytes.toBytes("team"), Bytes.toBytes("name")))  // Get team name
        val teamCountry = Bytes.toString(result.getValue(Bytes.toBytes("team"), Bytes.toBytes("country")))  // Get team country
        val matchDate = Bytes.toString(result.getValue(Bytes.toBytes("match"), Bytes.toBytes("date")))  // Get match date
        val matchOpponent = Bytes.toString(result.getValue(Bytes.toBytes("match"), Bytes.toBytes("opponent")))  // Get match opponent
        val matchScore = Bytes.toString(result.getValue(Bytes.toBytes("match"), Bytes.toBytes("score")))  // Get match score
        (rowKey, teamName, teamCountry, matchDate, matchOpponent, matchScore)  // Return tuple
    }.toDF("rowKey", "teamName", "teamCountry", "matchDate", "matchOpponent", "matchScore")  // Convert RDD to DataFrame with column names

    // Write DataFrame to Databricks Delta Lake
    hBaseDF.write
      .format("delta")
      .mode(SaveMode.Overwrite)  // Overwrite mode (can be Append if incremental)
      .save("s3a://league_stand_entries/delta/")  // S3 path for Delta Lake

    // Stop SparkSession
    spark.stop()
  }

  // Function to get last run timestamp (implement based on your requirement)
  def getLastRunTimestamp(): Long = {
    // Implement logic to retrieve the last run timestamp, e.g., from a file or a database
    0L  // Placeholder; replace with actual logic
  }
}
