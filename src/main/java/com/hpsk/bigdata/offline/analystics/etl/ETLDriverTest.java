package com.hpsk.bigdata.offline.analystics.etl;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.hpsk.bigdata.offline.analystics.common.EventLogConstants;
import com.hpsk.bigdata.offline.analystics.common.GlobalConstants;
import com.hpsk.bigdata.offline.analystics.util.TimeUtil;

public class ETLDriverTest implements Tool {

	private Configuration conf = new Configuration();
	private byte[] family = EventLogConstants.BYTES_EVENT_LOGS_FAMILY_NAME;
	
	@Override
	public Configuration getConf() {
		// TODO Auto-generated method stub
		return this.conf;
	}

	@Override
	public void setConf(Configuration that) {
		// TODO Auto-generated method stub
		//本地运行时，要指定以下配置，集群运行需要注释掉
		that.set("fs.defaultFS", "hdfs://poche.com:8020");
		that.set("hbase.zookeeper.quorum","poche.com");
		this.conf = HBaseConfiguration.create(that);
	}

	@Override
	public int run(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		Job job = Job.getInstance(conf, "etl-job");
		job.setJarByClass(ETLDriverTest.class);
		/**
		 * 获取日期:yyyy-MM-dd
		 */
		String parserDate = this.getDate(arg0,conf);
		long time1 = TimeUtil.parseString2Long(parserDate);
		String dateTime = TimeUtil.parseLong2String(time1, "yyyyMMdd");
		//dateTime="20151220";
		//input
		//输入路径的格式：/eventLogs/20151220/
		Path inputPath = new Path(GlobalConstants.HDFS_LOGS_PATH_PREFIX+"/"+dateTime);
		FileInputFormat.setInputPaths(job, inputPath);
		//map
		job.setMapperClass(ETlMapperTest.class);
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(Put.class);
		//shuffle
		//reduce
		//output
		/**
		 * 创建表:  event_logs20151220
		 */
		String tablename = this.CreateHbaseTable(dateTime,conf);
		
		/**
		 * 集群运行使用下面的语句，本地运行需要注释掉
		 */
//		TableMapReduceUtil.initTableReducerJob(
//				tablename, 
//				null, 
//				job
//				);
		/**
		 * 本地运行时使用下面语句，集群运行需要注释掉，本地运行最后一个参数adddependecy需要为false
		 */
		TableMapReduceUtil.initTableReducerJob(
				tablename, 
				null, 
				job, 
				null, 
				null, 
				null, 
				null, 
				false
				);
		//submit
		return job.waitForCompletion(true)? 0:1;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			int status = ToolRunner.run(new ETLDriverTest(), args);
			System.exit(status);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String CreateHbaseTable(String dateTime, Configuration conf) {
		// TODO Auto-generated method stub
		//event_logs20151220
		String table = EventLogConstants.HBASE_NAME_EVENT_LOGS+dateTime;
		Connection conn = null;
		HBaseAdmin admin = null;
		try {
			conn = ConnectionFactory.createConnection(conf);
			admin = (HBaseAdmin) conn.getAdmin();
			if(admin.tableExists(table)){
				admin.disableTable(table);
				admin.deleteTable(table);
			}
			HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(table));
			HColumnDescriptor cf = new HColumnDescriptor(family);
			desc.addFamily(cf);
			admin.createTable(desc);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(admin != null){
				try {
					admin.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(conn != null){
				try {
					conn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return table;
	}

	public String getDate(String[] args, Configuration conf) {
		// TODO Auto-generated method stub
		String date = null;
		for(int i=0;i<args.length;i++){
			if("-d".equals(args[i])){
				date = args[i+1];
			}
		}
		if(date == null || !TimeUtil.isValidateRunningDate(date)){
			date = TimeUtil.getYesterday(TimeUtil.DATE_FORMAT);
		}
		
		
		conf.set(GlobalConstants.RUNNING_DATE_PARAMES, date);
		return date;
	}

	

}
