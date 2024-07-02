# 1. Apache Nifi Setup
## Import the Nifi Flow:
Import the nifi/nifi_flow.xml file into your Nifi instance.

## Configure the GetHBase processor to connect to your HBase instance
Set the Table Name to UEFA_CHAMPIONS_LEAGUE.
Configure Zookeeper Quorum to zookeeper1.example.com,zookeeper2.example.com,zookeeper3.example.com.
Set Zookeeper Client Port to 2181.
Specify Column Families: team, match.
Specify Columns: team:name, team:country, match:date, match:opponent, match:score.
Set Time Range Start and End using ${time.start} and ${time.end}.

## Configure UpdateAttribute Processor:
Set filename to hbase-to-databricks-${now():format('yyyyMMddHHmmss')}.json for unique filenames.

## Configure PutS3Object Processor:
Set Access Key and Secret Key using ${AWS_ACCESS_KEY_ID} and ${AWS_SECRET_ACCESS_KEY}.
Set Bucket to league_stand_entries.
Set Object Key to hbase-to-databricks/${filename}.
Set Region to us-west-2.

## Start the Nifi Flow:
Ensure all processors are connected and start the flow.


# 2. Apache Spark Setup
## Dependencies:
Ensure your Spark environment includes dependencies for HBase and AWS S3.

## Update Spark Job Configuration:
Modify spark/HBaseToDeltaLake.scala to match your HBase table, AWS S3 bucket, and specific configurations.

## Submit Spark Job:
Command: spark-submit --class HBaseToDeltaLake --master your-spark-master spark/HBaseToDeltaLake.scala


# 3. Databricks Setup
## S3 Access:
Ensure Databricks has IAM roles and permissions to access the S3 bucket league_stand_entries.

## Create Delta Lake Table:
Create a Delta Lake table in Databricks pointing to: s3a://league_stand_entries/delta/.
