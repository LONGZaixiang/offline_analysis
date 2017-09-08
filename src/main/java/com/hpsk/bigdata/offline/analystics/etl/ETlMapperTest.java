package com.hpsk.bigdata.offline.analystics.etl;

import java.io.IOException;
import java.util.Map;
import java.util.zip.CRC32;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.hpsk.bigdata.offline.analystics.common.EventLogConstants;
import com.hpsk.bigdata.offline.analystics.common.EventLogConstants.EventEnum;
import com.hpsk.bigdata.offline.analystics.util.TimeUtil;

public class ETlMapperTest extends Mapper<LongWritable, Text, NullWritable, Put> {
	
	private LogParser logParse = new LogParser();
	private CRC32 crc = new CRC32();
	private byte[] family = EventLogConstants.BYTES_EVENT_LOGS_FAMILY_NAME;

	@Override
	protected void map(LongWritable key, Text value,Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		String line = value.toString();
		Map<String, String> logInfo = logParse.handleLogParser(line);
		/**
		 * 从集合中取出相应的字段用于构造rowkey
		 * s_time：转换成以毫秒为的单位
		 * u_ud
		 * u_md
		 * en
		 */
		String s_time = logInfo.get(EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME);
		long time = TimeUtil.parseNginxServerTime2Long(s_time);
		String u_ud = logInfo.get(EventLogConstants.LOG_COLUMN_NAME_UUID);
		String u_md = logInfo.get(EventLogConstants.LOG_COLUMN_NAME_MEMBER_ID);
		String event_alias = logInfo.get(EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME);
		/**
		 * 过滤
		 */
		if(StringUtils.isBlank(s_time) || StringUtils.isBlank(u_ud)){
			return;
		}
		EventEnum ee = EventEnum.valueOfAlias(event_alias);
		switch (ee) {
		case LAUNCH:
		case PAGEVIEW:
		case CHARGEREQUEST:
		case CHARGESUCCESS:
		case CHARGEREFUND:
		case EVENT:
			String rowkey = this.CreateRow(time,u_ud,u_md,event_alias);
			Put put = new Put(Bytes.toBytes(rowkey));
			for(Map.Entry<String, String> map : logInfo.entrySet()){
				put.addColumn(
						family, 
						Bytes.toBytes(map.getKey()), 
						Bytes.toBytes(map.getValue())
						);
			}
			context.write(NullWritable.get(), put);
			break;

		default:
			return;
		}
		
	}

	public String CreateRow(long time, String u_ud, String u_md,
			String event_alias) {
		// TODO Auto-generated method stub
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append(time+"_");
		crc.reset();
		crc.update(Bytes.toBytes(u_ud));
		if(StringUtils.isNotBlank(u_md)){
		crc.update(Bytes.toBytes(u_md));
		}
		crc.update(Bytes.toBytes(event_alias));
		sbuilder.append(crc.getValue() % 100000000L);
		return sbuilder.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(
				new ETlMapperTest().CreateRow(1450569601351L,
						"4B16B8BB-D6AA-4118-87F8-C58680D22657", "4B16B8BB", "e_l")
				);
	}
	
}
