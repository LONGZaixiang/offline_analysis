package com.hpsk.bigdata.offline.analystics.util;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.hpsk.bigdata.offline.analystics.common.EventLogConstants;
import com.hpsk.bigdata.offline.analystics.common.EventLogConstants.EventEnum;

/**
 * 用于对hbase表的 数据进行过滤，返回一个scan对象
 * @author 江城子
 *
 */
public class ScanUtil {
	
	private byte[] family = EventLogConstants.BYTES_EVENT_LOGS_FAMILY_NAME;

	public Scan handleScan(String dateTime){
		
		//create the scan instance
		Scan scan = new Scan();
		String tablename = EventLogConstants.HBASE_NAME_EVENT_LOGS+dateTime;
		scan.setAttribute(scan.SCAN_ATTRIBUTES_TABLE_NAME, Bytes.toBytes(tablename));
		/**
		 * 值：en=e_l
		 */
		String column = EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME;
		String value = EventEnum.LAUNCH.alias;
		FilterList filterList = new FilterList();
		Filter valueFilter = new SingleColumnValueFilter(
				family, 
				Bytes.toBytes(column), 
				CompareOp.EQUAL, 
				Bytes.toBytes(value)
				);
		filterList.addFilter(valueFilter);
		/**
		 * 列标签的过滤：en,uuid,s_time,pl,ver,browsername,browserversion
		 */
		byte[][] prefixes ={
			Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_EVENT_NAME),
			Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_UUID),
			Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME),
			Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_PLATFORM),
			Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_VERSION),
			Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME),
			Bytes.toBytes(EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION)
		};
		Filter columnFilter = new MultipleColumnPrefixFilter(prefixes);
		filterList.addFilter(columnFilter);
		
		scan.setFilter(filterList);
		
		return scan;
	}
}
