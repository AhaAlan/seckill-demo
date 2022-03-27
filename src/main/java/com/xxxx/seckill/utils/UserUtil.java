package com.xxxx.seckill.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.seckill.pojo.User;
import com.xxxx.seckill.vo.RespBean;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 生成用户工具类
 * 使用工具类创建大量用户，用于测试秒杀
 */
public class UserUtil {
	private static void createUser(int count) throws Exception {
		List<User> users = new ArrayList<>(count);	//count为新建的用户数量
		//生成用户
		for (int i = 0; i < count; i++) {
			User user = new User();
			user.setId(13000000000L + i);
			user.setLoginCount(1);
			user.setNickname("user" + i);
			user.setRegisterDate(new Date());
			user.setSalt("1a2b3c");
			user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));
			users.add(user);
		}
		System.out.println("create user");

		//插入数据库
		 Connection conn = getConn();//获取到连接
		 String sql = "insert into t_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";	//？sql的参数占位符
		 PreparedStatement pstmt = conn.prepareStatement(sql);
		 for (int i = 0; i < users.size(); i++) {
		 	User user = users.get(i);
		 	pstmt.setInt(1, user.getLoginCount());
		 	pstmt.setString(2, user.getNickname());
		 	pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
		 	pstmt.setString(4, user.getSalt());
		 	pstmt.setString(5, user.getPassword());
		 	pstmt.setLong(6, user.getId());
		 	pstmt.addBatch();	//批处理
		 }
		 pstmt.executeBatch();	//执行批量
		 pstmt.close();
		 conn.close();	//关闭连接
		 System.out.println("insert to db");

		//登录，生成userTicket，因为jmeter测试需要用户的userTikcet
		String urlString = "http://localhost:8080/login/doLogin";	//访问的IP地址与接口
		File file = new File("C:\\Users\\jafari\\Desktop\\seckill\\config.txt");
		if (file.exists()) {
			file.delete();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		file.createNewFile();
		raf.seek(0);
		for (User user : users) {
			URL url = new URL(urlString);
			HttpURLConnection co = (HttpURLConnection) url.openConnection();
			co.setRequestMethod("POST");    //登录，所以是post请求
			co.setDoOutput(true);
			OutputStream out = co.getOutputStream();
			String params = "mobile=" + user.getId() + "&password=" + MD5Util.inputPassToFromPass("123456");    //入参
			out.write(params.getBytes());
			out.flush();
			InputStream inputStream = co.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int len = 0;
			while ((len = inputStream.read(buff)) >= 0) {	//输入
				bout.write(buff, 0, len);	//输出
			}
			inputStream.close();
			bout.close();
			String response = new String(bout.toByteArray());//拿到响应结果
			ObjectMapper mapper = new ObjectMapper();
			RespBean respBean = mapper.readValue(response, RespBean.class);
			String userTicket = ((String) respBean.getObj());	//拿到userTicket
			System.out.println("create userTicket : " + user.getId());

			String row = user.getId() + "," + userTicket;
			raf.seek(raf.length());
			raf.write(row.getBytes());
			raf.write("\r\n".getBytes());
			System.out.println("write to file : " + user.getId()); //谁写入了文件
		}
		raf.close();
		System.out.println("over");
	}

	//插入数据库方法
	private static Connection getConn() throws Exception {
		String url = "jdbc:mysql://localhost:3306/seckill?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8";
		String username = "root";
		String password = "root";
		String driver = "com.mysql.cj.jdbc.Driver";
		Class.forName(driver);
		return DriverManager.getConnection(url, username, password);
	}

	public static void main(String[] args) throws Exception {
		createUser(5000);
	}
}
