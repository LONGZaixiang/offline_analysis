package com.hpsk.bigdata.offline.analystics.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import com.hpsk.bigdata.offline.analystics.dimension.key.stats.StatsUserDimension;

public class NewInstallTestReduce extends
		Reducer<StatsUserDimension, Text, StatsUserDimension, IntWritable> {
	
	IntWritable outputValue = new IntWritable();
	Set<String> sets = new HashSet<String>();

	@Override
	protected void reduce(
			StatsUserDimension key,
			Iterable<Text> values,Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		this.sets.clear();
		for(Text uuid : values){
			this.sets.add(uuid.toString());
		}
		int rssize = this.sets.size();
		this.outputValue.set(rssize);
		context.write(key, this.outputValue);
		
	}
}
