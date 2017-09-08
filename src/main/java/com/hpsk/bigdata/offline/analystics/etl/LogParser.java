package com.hpsk.bigdata.offline.analystics.etl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hpsk.bigdata.offline.analystics.common.EventLogConstants;
import com.hpsk.bigdata.offline.analystics.util.IPSeekerExt;
import com.hpsk.bigdata.offline.analystics.util.UserAgentUtil;
import com.hpsk.bigdata.offline.analystics.util.IPSeekerExt.RegionInfo;
import com.hpsk.bigdata.offline.analystics.util.UserAgentUtil.UserAgentInfo;

/**
 * 用于解析map读入的每条记录，并将每个字段及其值返回给map
 * @author 江城子
 * 
 * 
 * 		-》字段过滤提取
			-》按照^A进行分割
				-》IP
				-》timestamp
				-》hostname
				-》URI
			-》对URI进行分割，按照？进行分割
				-》请求资源
				-》用户的行为信息
			-》对用户的行为信息进行分割，按照&进行分割
				-》每一个keyvalue
			-》对每一个keyvalue进行分割，按照=进行分割
				-》key
				-》value
		-》字段格式化
			-》对编码的value进行解码
		-》字段的补全
			-》通过解析IP地址得到
				-》国家
				-》省份
				-》城市
			-》通过解析客户端得到
				-》浏览器版本
				-》浏览器类型
				-》操作系统版本
				-》操作系统类型
			-》将所有的key和value存储map集合中
 *
 */
public class LogParser {
	
	private Logger logger = LoggerFactory.getLogger(LogParser.class);
	
	public Map<String, String> handleLogParser(String logText) throws UnsupportedEncodingException{
		
		Map<String, String> logInfo = new HashMap<String,String>();		
		if(StringUtils.isNotBlank(logText)){
			String[] splits = logText.split("\\^A");
			if(4 == splits.length){
				//分割后需要的是ip地址 、Servertime时间、 hostname、 以及url
				String ip = splits[0];
				logInfo.put(EventLogConstants.LOG_COLUMN_NAME_IP, ip);
				String s_time = splits[1];
				logInfo.put(EventLogConstants.LOG_COLUMN_NAME_SERVER_TIME, s_time);
				String host = splits[2];
				logInfo.put(EventLogConstants.LOG_COLUMN_NAME_HTTP_HOST,host);
				String uri = splits[3];
				
				//解析URI
				this.AnalysisURI(uri,logInfo);
				//解析IP
				this.AnalysisIP(ip,logInfo);
				//解析客户端
				this.AnalysisUserAgent(logInfo);
				
			}else{
				logger.error("该条记录不合法 ： "+logText);
			}
		}
		
		return logInfo;
	}

	/**
	 * 用于解析用户客户端，得到浏览器名称、版本、操作系统名称和版本
	 * @param logInfo
	 */
	public void AnalysisUserAgent(Map<String, String> logInfo) {
		// TODO Auto-generated method stub
		String agent = logInfo.get(EventLogConstants.LOG_COLUMN_NAME_USER_AGENT);
		UserAgentInfo agentInfo = UserAgentUtil.analyticUserAgent(agent);
		String browsername = agentInfo.getBrowserName();
		String browserversion = agentInfo.getBrowserVersion();
		String osname = agentInfo.getOsName();
		String osversion = agentInfo.getOsVersion();
		if(StringUtils.isNotBlank(browsername)){
			logInfo.put(EventLogConstants.LOG_COLUMN_NAME_BROWSER_NAME, browsername);
		}
		if(StringUtils.isNotBlank(browserversion)){
			logInfo.put(EventLogConstants.LOG_COLUMN_NAME_BROWSER_VERSION, browserversion);
		}
		if(StringUtils.isNotBlank(osname)){
			logInfo.put(EventLogConstants.LOG_COLUMN_NAME_OS_NAME, osname);
		}
		if(StringUtils.isNotBlank(osversion)){
			logInfo.put(EventLogConstants.LOG_COLUMN_NAME_OS_VERSION, osversion);
		}
	}

	/**
	 * 用于解析IP，得到国家省份城市
	 * 调用了IPSeekerExt类 在项目其他路径中可找到 单例模式
	 * @param ip
	 * @param logInfo
	 */
	public void AnalysisIP(String ip, Map<String, String> logInfo) {
		// TODO Auto-generated method stub
		if(StringUtils.isNotBlank(ip)){
			IPSeekerExt ipseek = IPSeekerExt.getInstance();
			RegionInfo regioninfo = ipseek.analyseIp(ip);
			String country = regioninfo.getCountry();
			String province = regioninfo.getProvince();
			String city = regioninfo.getCity();
			if(StringUtils.isNotBlank(country)){
				logInfo.put(EventLogConstants.LOG_COLUMN_NAME_COUNTRY, country);
			}
			if(StringUtils.isNotBlank(province)){
				logInfo.put(EventLogConstants.LOG_COLUMN_NAME_PROVINCE, province);
			}
			if(StringUtils.isNotBlank(city)){
				logInfo.put(EventLogConstants.LOG_COLUMN_NAME_CITY, city);
			}
		}
	}

	/**
	 * 用于解析URI
	 * 将log中的url后半部分keyvalues对封装到Map集合中
	 * @param uri
	 * @param logInfo
	 * @throws UnsupportedEncodingException
	 */
	public void AnalysisURI(String uri, Map<String, String> logInfo) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		if(StringUtils.isNotBlank(uri)){
			String[] splits = uri.split("\\?");
			if(2 == splits.length){
				//分割后的内容为 域名/地址?keyvalues对 需要对后者进行处理
				String data = splits[1];
				String[] keyvalues = data.split("&");
				for(String keyvalue : keyvalues){
					String key = keyvalue.split("=")[0];
					String value = keyvalue.split("=")[1];
					String realvalue = URLDecoder.decode(value, "utf-8");
					//源文件内容被编码过 但具体编码方式不清 老师给的decode示例
					logInfo.put(key, realvalue);
					
				}
			}
		}
	}

}
