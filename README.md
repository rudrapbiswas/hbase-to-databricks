# hbase-to-databricks
Optimized data transfer from HBase/Phoenix to Databricks using Apache Nifi and Spark

# HBase to Databricks Data Transfer

This project demonstrates how to efficiently transfer data from HBase/Phoenix to Databricks using Apache Nifi and Apache Spark.

## Project Structure

```plaintext
hbase-to-databricks/
├── nifi/
│   └── nifi_flow.xml
├── spark/
│   └── HBaseToDeltaLake.scala
├── README.md
└── .gitignore

## Prerequisites

Apache Nifi
Apache Spark
HBase/Phoenix
AWS S3
Databricks
Hadoop (for HBase configuration)

# Prerequisites

1. Apache Nifi Setup
Import the nifi/nifi_flow.xml file into your Nifi instance.
Configure the GetHBase processor to connect to your HBase instance.
Configure the PutS3Object processor with your AWS S3 credentials and bucket details.
Start the Nifi flow.
2. Apache Spark Setup
Configure your Spark environment with the necessary dependencies for HBase and AWS S3.
Update the spark/HBaseToDeltaLake.scala file with your table name, AWS S3 bucket, and any other specific configurations.
3. Submit the Spark job:
spark-submit --class HBaseToDeltaLake --master your-spark-master spark/HBaseToDeltaLake.scala
3. Databricks Setup
Ensure Databricks is configured to read from your AWS S3 bucket.
Create a Delta Lake table in Databricks and point it to the S3 location where the Spark job writes the data.

# Configuration Details
Nifi
GetHBase Processor: Reads data from HBase using a timestamp filter for incremental extraction.
PutS3Object Processor: Writes the extracted data directly to AWS S3.
Spark
HBaseToDeltaLake.scala: Contains the Spark job to read data from HBase, transform it, and write it to Databricks Delta Lake.


Database/Connection details

HBase Table: UEFA_CHAMPIONS_LEAGUE
Column Family: team
Qualifiers: name, country
Column Family: match
Qualifiers: date, opponent, score

AWS S3 Bucket: league_stand_entries
S3 Path: s3a://league_stand_entries/hbase-to-databricks/
Databricks Delta Lake Path: s3a://league_stand_entries/delta/
HBase Connection Details:
Zookeeper Quorum: zookeeper1.example.com,zookeeper2.example.com,zookeeper3.example.com
Zookeeper Port: 2181
AWS Credentials: Stored in the environment variables AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
