# hbase-to-databricks
Optimized data transfer from HBase/Phoenix to Databricks using Apache Nifi and Spark

# HBase to Databricks Data Transfer

This project demonstrates how to efficiently transfer data from HBase/Phoenix to Databricks using Apache Nifi and Apache Spark.

## Database/Connection details

HBase Table: UEFA_CHAMPIONS_LEAGUE

Column Family: team

Qualifiers: name, country

Column Family: match

Qualifiers: date, opponent, score

AWS S3 Bucket: league_stand_entries

S3 Path: s3a://league_stand_entries/hbase-to-databricks/

Databricks Delta Lake Path: s3a://league_stand_entries/delta/

## HBase Connection Details

Zookeeper Quorum: zookeeper1.example.com,zookeeper2.example.com,zookeeper3.example.com

Zookeeper Port: 2181

AWS Credentials: Stored in the environment variables AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
