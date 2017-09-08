package com.hpsk.bigdata.offline.analystics.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.hpsk.bigdata.offline.analystics.dimension.key.stats.StatsUserDimension;
import com.hpsk.bigdata.offline.analystics.etl.ETLDriverTest;
import com.hpsk.bigdata.offline.analystics.util.ScanUtil;

public class NewInstallTestDriver implements Tool {

	private Configuration conf = new Configuration();
	private ScanUtil scanutil = new ScanUtil();

	@Override
	public void setConf(Configuration that) {
		// TODO Auto-generated method stub
		that.set("fs.defaultFS", "hdfs://bigdata-training01.hpsk.com:8020");
		that.set("hbase.zookeeper.quorum","bigdata-training01.hpsk.com");
		this.conf = HBaseConfiguration.create(that);
	}

	@Override
	public Configuration getConf() {
		// TODO Auto-generated method stub
		return this.conf;
	}

	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Job job = Job.getInstance(conf, "nu_job");
		job.setJarByClass(NewInstallTestDriver.class);
		
		List<Scan> scans = new ArrayList<Scan>();
		Scan scan = scanutil.handleScan("20151220");
		scans.add(scan);
		//input&&map
		//集群使用以下方法
//		TableMapReduceUtil.initTableMapperJob(
//				scans, 
//				mapper, 
//				outputKeyClass, 
//				outputValueClass, 
//				job
//				);
		//本地模式使用以下方法
	
		TableMapReduceUtil.initTableMapperJob(
				scans, 
				NewInstallTestMapper.class, 
				StatsUserDimension.class, 
				Text.class, 
				job, 
				false
				);
		//reduce
		job.setReducerClass(NewInstallTestReduce.class);
		job.setOutputKeyClass(StatsUserDimension.class);
		job.setOutputValueClass(IntWritable.class);
		//output
		FileOutputFormat.setOutputPath(job, new Path("/new-install/out1"));
		//submit
		return job.waitForCompletion(true) ? 0:1;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			int status = ToolRunner.run(new NewInstallTestDriver(), args);
			System.exit(status);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
