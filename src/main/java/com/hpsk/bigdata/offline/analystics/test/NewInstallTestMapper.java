package com.hpsk.bigdata.offline.analystics.test;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.hpsk.bigdata.offline.analystics.common.DateEnum;
import com.hpsk.bigdata.offline.analystics.common.EventLogConstants;
import com.hpsk.bigdata.offline.analystics.common.KpiType;
import com.hpsk.bigdata.offline.analystics.dimension.key.base.BrowserDimension;
import com.hpsk.bigdata.offline.analystics.dimension.key.base.DateDimension;
import com.hpsk.bigdata.offline.analystics.dimension.key.base.KpiDimension;
import com.hpsk.bigdata.offline.analystics.dimension.key.base.PlatformDimension;
import com.hpsk.bigdata.offline.analystics.dimension.key.stats.StatsCommonDimension;
import com.hpsk.bigdata.offline.analystics.dimension.key.stats.StatsUserDimension;
import com.hpsk.bigdata.offline.analystics.util.TimeUtil;
/**
 * <ImmutableBytesWritable, Result, KEYOUT, VALUEOUT>
 * @author 江城子
 *
 */

public class NewInstallTestMapper extends TableMapper<StatsUserDimension, Text> {
	
	private BrowserDimension defaultBrowser = new BrowserDimension("", "");
	private KpiDimension userKpi = new KpiDimension(KpiType.NEW_INSTALL_USER.name);
	private KpiDimension browerKpi = new KpiDimension(KpiType.BROWSER_NEW_INSTALL_USER.name);
	private byte[] family = EventLogConstants.BYTES_EVENT_LOGS_FAMILY_NAME;
	StatsUserDimension outputKey = new StatsUserDimension();
	Text outputValue = new Text();
	
	@Override
	protected void map(
			ImmutableBytesWritable key,
			Result value,Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		
		/**
		 * 获取输出的key和value需要的所有字段
		 */
		String uuid = this.getValue(value,EventLogConstants.LOG_COLUMN_NAME_UUID);
		String s_time = this.getValue(value, EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME);
		long time = TimeUtil.parseNginxServerTime2Long(s_time);
		String pl = this.getValue(value, EventLogConstants.LOG_COLUMN_NAME_PLATFORM);
		String ver = this.getValue(value, EventLogConstants.LOG_COLUMN_NAME_VERSION);
		String browsername = this.getValue(value, EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME);
		String browserversion = this.getValue(value, EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION);
		/**
		 * 非法值过滤
		 */
		if(uuid == null || s_time == null){
			return;
		}
		/**
		 * 构造维度
		 */
		//构造时间维度
		DateDimension dayDimension = DateDimension.buildDate(time, DateEnum.DAY);
//		DateDimension weekDimension = DateDimension.buildDate(time, DateEnum.WEEK);
		//构造平台维度
		List<PlatformDimension> platforms = PlatformDimension.buildList(pl, ver);
		//构造浏览器维度
		List<BrowserDimension> browsers = BrowserDimension.buildList(browsername, browserversion);
	
		/**
		 * 输出：
		 * 		-》将构造好的维度，封装到key中
		 * 		-》将uuid封装到value中
		 */
		//封装value
		this.outputValue.set(uuid);
		//封装key
		StatsCommonDimension statscommon = this.outputKey.getStatsCommon();	
		/**
		 * 第一种分析：时间+平台+KPI
		 */
		this.outputKey.setBrowser(defaultBrowser);
		for(PlatformDimension platform : platforms){
			statscommon.setPlatform(platform);
			//时间维度
			statscommon.setDate(dayDimension);
			//kpi:userkpi
			statscommon.setKpi(userKpi);
			context.write(this.outputKey, this.outputValue);
			
			/**
			 * 第二种分析：时间+平台+KPI+浏览器
			 */
			statscommon.setKpi(browerKpi);
			for(BrowserDimension browser:browsers){
				this.outputKey.setBrowser(browser);
				context.write(this.outputKey, this.outputValue);
			}
			
		}
		
	}

	public String getValue(Result value, String ColumnName) {
		// TODO Auto-generated method stub
		return Bytes.toString(value.getValue(family, Bytes.toBytes(ColumnName)));
	}

}
