package com.hpsk.bigdata.offline.analystics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestPreparement {
	
	public static void main(String[] args) {
		
		// jdbc 
		// 1.加载驱动
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			throw new RuntimeException("加载驱动出现错误！");
		}
		
		// 2.获取连接
		String url = "jdbc:mysql://poche.com:3306/report?useUnicode=true&characterEncoding=utf8";
		String user = "root";
		String password = "123456";
		Connection connection = null; // 当connection连接对象过多，引入连接池  c3p0
			// 连接池 ： 事先创建指定数量的连接对象   共享使用
		try {
			connection.setAutoCommit(false); // 关闭自动提交
			connection = DriverManager.getConnection(url, user, password);
			
			String sql = "INSERT INTO `stats_user`(`platform_dimension_id`,"
					+ "`date_dimension_id`,"
					+ "`new_install_users`,"
					+ "`created`)"
					+ "VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `new_install_users` = ?";
			
			PreparedStatement prpt = connection.prepareStatement(sql);
			prpt.setInt(1, 1);
			prpt.setInt(2, 2);
			prpt.setInt(3, 234567);
			prpt.setString(4, "123455567");
			prpt.setInt(5, 234567);
			
			// 执行
			prpt.executeUpdate();
			
			String  findSql = 
					"Select `new_install_users` from `stats_user` "
					+ "where `platform_dimension_id`= ? ";
			PreparedStatement prpt2 = connection.prepareStatement(findSql);
			prpt.setInt(1, 1);
			
			// 执行
			ResultSet resultSet = prpt.executeQuery();
			
			while(resultSet.next()){
				int newInstallUsers = resultSet.getInt("new_install_users");
				System.out.println(newInstallUsers);
			}
			
			connection.commit();
			
		} catch (SQLException e) {
			e.printStackTrace();
			
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException("获取连接出现异常！");
		}finally{
			if(connection!= null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
